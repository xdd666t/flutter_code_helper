package utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import io.flutter.utils.FlutterModuleUtils
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequence
import utils.PluginUtils.showNotify
import java.io.File

class FileGenerator(private val project: Project) {
    /**
     * 将所选择目录及子目录添加到yaml配置
     */
    fun buildYaml(file: VirtualFile) {
        saveChanges()
        val modules = FileHelperNew.getAssets(project)
        var module: ModulePubSpecConfig? = null
        for (m in modules) {
            if (file.path.startsWith(m.pubRoot.path)) {
                module = m
                break
            }
        }
        if (module != null && FlutterModuleUtils.isFlutterModule(module.module)) {
            val paths = mutableListOf<String>()
            val rootPath = "${module.pubRoot.path}/"
            if (file.isDirectory) {
                traversalDir(file, rootPath, paths)
            } else {
                paths.add(file.path.removePrefix(rootPath))
            }

            processWritePubspec(module, paths)
            showNotify("Flutter: Configuration complete.")
        } else {
            showNotify("This module is not flutter module")
        }
    }

    fun autoBuildYaml() {
        saveChanges()
        val modules = FileHelperNew.getAssets(project)
        var module: ModulePubSpecConfig? = null
        for (item in modules) {
            if (FlutterModuleUtils.isFlutterModule(item.module)) {
                module = item
                break
            }
        }
        if (module != null) {
            val rootPath = "${module.pubRoot.path}/"
            val paths = mutableListOf<String>()
            val folderList = FileHelperNew.getAutoFolder(project)
            if (folderList.isNotEmpty()) {
                paths.addAll(folderList)
                for (folder in folderList) {
                    val directory = File("$rootPath$folder")
                    recurDir(directory, paths)
                }
            }
            // 处理数据
            paths.forEachIndexed { index, value ->
                val replaceValue = value.replace(rootPath, "")
                paths[index] = if (!replaceValue.endsWith("/")) "$replaceValue/" else replaceValue
            }

            processWritePubspec(module, paths)
        }
    }

    private fun processWritePubspec(module: ModulePubSpecConfig, paths: MutableList<String>) {
        ApplicationManager.getApplication().invokeLater {
            val moduleAssets = FileHelperNew.tryGetAssetsList(module.map)
            // 移除记录列表中, 已经存在的路径
            if (moduleAssets != null) {
                val moduleDir = module.module.guessModuleDir()
                moduleAssets.removeIf {
                    var parentPath = moduleDir?.path
                    var path = it as String
                    path = path.removeSuffix(File.separator)
                    if (path.contains(File.separator)) {
                        val subIndex = path.lastIndexOf(File.separator)
                        parentPath = "$parentPath${File.separator}${path.substring(0, subIndex + 1)}"
                        path = path.substring(subIndex + 1, path.length)
                    }
                    val asset = File(parentPath, path)
                    !asset.exists()
                }
                paths.removeIf {
                    moduleAssets.contains(it)
                }
            }

            val yamlFile = module.pubRoot.pubspec.toPsiFile(project) as? YAMLFile ?: return@invokeLater
            val psiElement = yamlFile.node.getChildren(null)
                .firstOrNull()?.psi?.children?.firstOrNull()?.children?.firstOrNull { it.text.startsWith("flutter:") }
                ?: return@invokeLater

            val yamlMapping = psiElement.children.first() as YAMLMapping
            WriteCommandAction.runWriteCommandAction(project) {
                var assetsValue = yamlMapping.keyValues.firstOrNull { it.keyText == "assets" }
                val stringBuilder = StringBuilder()
                moduleAssets?.forEach {
                    stringBuilder.append("    - $it\n")
                }
                paths.forEach {
                    stringBuilder.append("    - $it\n")
                }
                stringBuilder.removeSuffix("\n")
                if (assetsValue == null) {
                    assetsValue = YAMLElementGenerator.getInstance(project)
                        .createYamlKeyValue("assets", stringBuilder.toString())
                    yamlMapping.putKeyValue(assetsValue)
                } else {
                    val yamlValue = PsiTreeUtil.collectElementsOfType(
                        YAMLElementGenerator.getInstance(project)
                            .createDummyYamlWithText(stringBuilder.toString()),
                        YAMLSequence::class.java
                    ).iterator().next()
                    assetsValue.setValue(yamlValue)
                }
            }
            saveChanges()
        }
    }

    private fun saveChanges() {
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().saveAll()
            PsiDocumentManager.getInstance(project).commitAllDocumentsUnderProgress()
        }
    }

    private fun traversalDir(file: VirtualFile, rootPath: String, list: MutableList<String>) {
        if (file.isDirectory) {
            list.add("${file.path.removePrefix(rootPath)}/")
            file.children.forEach {
                if (it.isDirectory) {
                    traversalDir(it, rootPath, list)
                }
            }
        }
    }

    private fun recurDir(file: File, list: MutableList<String>) {
        if (file.isDirectory) {
            val dirList = file.listFiles()?.filter { it.isDirectory } ?: emptyList()
            for (dir in dirList) {
                if (dir.isDirectory) {
                    recurDir(dir, list)
                }
                list.add(dir.path)
            }
        }
    }
}