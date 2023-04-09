package utils

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.vfs.VirtualFile
import io.flutter.pub.PubRoot
import io.flutter.utils.FlutterModuleUtils
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream

/**
 * 基于Module来处理Assets
 */
object FileHelperNew {

    /**
     * 获取所有可用的Flutter Module的Asset配置
     */
    @JvmStatic
    fun getPubspecConfigList(project: Project): List<PubspecConfig> {
        val modules = project.allModules()
        val folders = mutableListOf<PubspecConfig>()
        for (module in modules) {
            if (FlutterModuleUtils.isFlutterModule(module)) {
                val moduleDir = module.guessModuleDir()
                if (moduleDir != null) {
                    calculatePubspecConfigList(module).forEach {
                        folders.add(it)
                    }
                }
            }
        }
        return folders
    }

    /**
     * 获取需要自动生成目录路径
     */
    @JvmStatic
    fun getAutoFolder(project: Project): MutableList<String> {
        var folderList = mutableListOf<String>()

        val configs = getPubspecConfigList(project)
        for (config in configs) {
            val list = try {
                readSetting(config, Constants.key_auto_folder) as MutableList<String>?
                    ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }
            if (list.isNotEmpty()) {
                folderList = list
                break
            }
        }
        return folderList
    }


    @JvmStatic
    fun shouldActivateFor(project: Project): Boolean {
        return FlutterModuleUtils.hasFlutterModule(project)
    }

    fun tryGetAssetsList(map: Map<*, *>): MutableList<*>? {
        (map["flutter"] as? Map<*, *>)?.let {
            return it["assets"] as? MutableList<*>
        }
        return null
    }

    @JvmStatic
    private fun calculatePubspecConfigList(module: Module): List<PubspecConfig> {
        if (!FlutterModuleUtils.isFlutterModule(module)) {
            return listOf()
        }

        val list = mutableListOf<PubspecConfig>()
        val moduleDir = module.guessModuleDir()
        val curModuleConfig = getSinglePubSpecConfig(moduleDir)
        if (curModuleConfig != null) {
            list.add(curModuleConfig)
        }
        moduleDir?.children?.forEach {
            val pubSpecPath = "${it.path}/pubspec.yaml"
            if (File(pubSpecPath).exists()) {
                val config = getSinglePubSpecConfig(it)
                if (config != null) {
                    list.add(config)
                }
            }
        }

        return list
    }

    @JvmStatic
    private fun getSinglePubSpecConfig(virtualFile: VirtualFile?): PubspecConfig? {
        try {
            val pubRoot = PubRoot.forDirectory(virtualFile)
            if (virtualFile != null && pubRoot != null) {
                val fis = FileInputStream(pubRoot.pubspec.path)
                val pubConfigMap = Yaml().load(fis) as? Map<String, Any>
                if (pubConfigMap != null) {
                    val assetVFiles = mutableListOf<VirtualFile>()
                    (pubConfigMap["flutter"] as? Map<*, *>)?.let { configureMap ->
                        (configureMap["assets"] as? ArrayList<*>)?.let { list ->
                            for (path in list) {
                                virtualFile.findFileByRelativePath(path as String)?.let {
                                    if (it.isDirectory) {
                                        val index = path.indexOf("/")
                                        val assetsPath = if (index == -1) {
                                            path
                                        } else {
                                            path.substring(0, index)
                                        }
                                        val assetVFile = virtualFile.findChild(assetsPath)
                                            ?: virtualFile.createChildDirectory(this, assetsPath)
                                        if (!assetVFiles.contains(assetVFile)) {
                                            assetVFiles.add(assetVFile)
                                        }
                                    } else {
                                        if (!assetVFiles.contains(it)) {
                                            assetVFiles.add(it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return PubspecConfig(
                        virtualFile,
                        pubRoot,
                        pubConfigMap,
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return null
    }

    /**
     * 读取配置
     */
    private fun readSetting(config: PubspecConfig, key: String): Any? {
        (config.map[Constants.key_config] as? Map<*, *>)?.let { configureMap ->
            return configureMap[key]
        }
        return null
    }
}

/**
 * 模块Flutter配置信息
 */
data class PubspecConfig(
    val virtualFile: VirtualFile?,
    val pubRoot: PubRoot,
    val map: Map<String, Any>,
)
