package utils

object Constants {
    /**
     * 配置map的key
     */
    const val key_config = "code_helper"
    /**
     * 输出目录的key
     */
    const val KEY_OUTPUT_DIR = "output_dir"

    /**
     * 输出文件的类名
     */
    const val KEY_CLASS_NAME = "class_name"

    /**
     * 是否自动检测
     */
    const val KEY_AUTO_DETECTION = "auto_detection"

    /**
     * 命名是否根据上级目录决定
     */
    const val KEY_NAMED_WITH_PARENT = "named_with_parent"

    /**
     * 输出的文件名
     */
    const val KEY_OUTPUT_FILENAME = "output_filename"

    /**
     * 分割文件的正则
     */
    const val FILENAME_SPLIT_PATTERN = "filename_split_pattern"

    /**
     * 忽略的目录
     */
    const val PATH_IGNORE = "path_ignore"

    /**
     * 默认目录
     */
    const val default_output_dir = "generated"
    const val default_class_name = "Assets"
    const val default_filename_split_pattern = "[-_]"
}