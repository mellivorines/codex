package io.github.mellivorines.codex

import org.springframework.util.ClassUtils
import java.io.File

object SystemUtil {
    /**
     * @param [baseDir] 基础路径
     * @return [String] 返回基础路径
     */
    fun getBasePath(baseDir: String): String {
        val packageName = ClassUtils.getPackageName(this.javaClass).replace(".", File.separator)
        return baseDir + File.separator + packageName
    }
}
