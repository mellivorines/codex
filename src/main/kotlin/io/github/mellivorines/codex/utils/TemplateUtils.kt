package io.github.mellivorines.codex.utils

import cn.hutool.core.io.IoUtil
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import freemarker.template.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import java.io.StringReader
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.text.Charsets.UTF_8

object TemplateUtils {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    private lateinit var engine: VelocityEngine
    private var properties = Properties()

    /**
     *
     * @param content：生成的文件流的内容
     * @param templateName：模板名称
     * @param dataModel :填充的数据模型
     */
    fun getContent(content: String, templateName: String, dataModel: Map<String, Any>): String {
        if (dataModel.isEmpty()) {
            return content
        } else {
            val stringReader = StringReader(content)
            val stringWriter = StringWriter()
            try {
                var template = Template(templateName, stringReader, null, UTF_8.toString())
                template.process(dataModel, stringWriter)
            } catch (e: Exception) {
                logger.error(e.message)
                throw Exception(e)
            }
            val result = stringWriter.toString()
            IoUtil.close(stringReader)
            IoUtil.close(stringWriter)

            return result
        }
    }

    /**
     * 从指定路径获取JSON并转换为List
     * @param path json文件路径
     */
    inline fun <reified T> getListFromJson(path: String?): T? {
        val resource = ClassPathResource(path!!)
        val jsonStr: String = IoUtil.read(resource.inputStream, UTF_8)
        val mapper = jacksonObjectMapper()
        return mapper.readValue(jsonStr)
    }

    /**
     * render template file to outFile with context
     */
     fun render(outFile: Path, templateFile: Path, context: Map<String, Any>) {
        properties.setProperty(
            RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
            templateFile.parent.absolutePathString()
        )
        engine = VelocityEngine(properties)
        val outDir = outFile.parent
        if (outDir.notExists()) {
            Files.createDirectories(outDir)
        }

        Files.newBufferedWriter(
            outFile,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE
        ).use {
            engine
                .getTemplate(templateFile.name, StandardCharsets.UTF_8.name())
                .merge(VelocityContext(context), it)


        }
    }
}
