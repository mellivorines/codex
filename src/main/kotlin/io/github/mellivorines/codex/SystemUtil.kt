package io.github.mellivorines.codex

import org.springframework.util.ClassUtils
import java.io.File

object SystemUtil {
    fun getBasePath(baseDir: String): String {
        val packageName = ClassUtils.getPackageName(this.javaClass).replace(".", File.separator)
        return baseDir + File.separator + packageName
    }
}
