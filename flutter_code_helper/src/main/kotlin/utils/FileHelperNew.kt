package utils

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.vfs.VirtualFile
import io.flutter.pub.PubRoot
import io.flutter.utils.FlutterModuleUtils
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream

/**
 * 基于Module来处理Assets
 */
object FileHelperNew {

    /**
     * 获取所有可用的Flutter Module的Asset配置
     */
    @JvmStatic
    fun getAssets(project: Project): List<ModulePubSpecConfig> {
        val modules = project.allModules()
        val folders = mutableListOf<ModulePubSpecConfig>()
        for (module in modules) {
            if (FlutterModuleUtils.isFlutterModule(module)) {
                val moduleDir = module.guessModuleDir()
                if (moduleDir != null) {
                    getPubSpecConfig(module)?.let {
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

        val assets = getAssets(project)
        for (config in assets) {
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
    fun getPubSpecConfig(module: Module): ModulePubSpecConfig? {
        try {
            val moduleDir = module.guessModuleDir()
            val pubRoot = PubRoot.forDirectory(moduleDir)
            if (moduleDir != null && pubRoot != null) {
                val fis = FileInputStream(pubRoot.pubspec.path)
                val pubConfigMap = Yaml().load(fis) as? Map<String, Any>
                if (pubConfigMap != null) {
                    val assetVFiles = mutableListOf<VirtualFile>()
                    (pubConfigMap["flutter"] as? Map<*, *>)?.let { configureMap ->
                        (configureMap["assets"] as? ArrayList<*>)?.let { list ->
                            for (path in list) {
                                moduleDir.findFileByRelativePath(path as String)?.let {
                                    if (it.isDirectory) {
                                        val index = path.indexOf("/")
                                        val assetsPath = if (index == -1) {
                                            path
                                        } else {
                                            path.substring(0, index)
                                        }
                                        val assetVFile = moduleDir.findChild(assetsPath)
                                            ?: moduleDir.createChildDirectory(this, assetsPath)
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
                    return ModulePubSpecConfig(
                        module,
                        pubRoot,
                        assetVFiles,
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
    private fun readSetting(config: ModulePubSpecConfig, key: String): Any? {
        (config.map[Constants.key_config] as? Map<*, *>)?.let { configureMap ->
            return configureMap[key]
        }
        return null
    }


    private fun VirtualFile.findOrCreateChildDir(requestor: Any, name: String): VirtualFile {
        val child = findChild(name)
        return child ?: createChildDirectory(requestor, name)
    }

}

/**
 * 模块Flutter配置信息
 */
data class ModulePubSpecConfig(
    val module: Module,
    val pubRoot: PubRoot,
    val assetVFiles: List<VirtualFile>,
    val map: Map<String, Any>,
    val isFlutterModule: Boolean = FlutterModuleUtils.isFlutterModule(module)
)
