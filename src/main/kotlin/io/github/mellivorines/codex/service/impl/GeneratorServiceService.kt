package io.github.mellivorines.codex.service.impl

import io.github.mellivorines.codex.SystemUtil
import io.github.mellivorines.codex.constants.CommonConstant
import io.github.mellivorines.codex.model.database.Table
import io.github.mellivorines.codex.model.project.Project
import io.github.mellivorines.codex.model.template.TemplateInfo
import io.github.mellivorines.codex.model.template.Templates
import io.github.mellivorines.codex.model.type.TypeMappings
import io.github.mellivorines.codex.service.GeneratorService
import io.github.mellivorines.codex.utils.StringUtils.sneak2camel
import io.github.mellivorines.codex.utils.TemplateUtils
import io.github.mellivorines.codex.utils.TemplateUtils.getListFromJson
import org.springframework.stereotype.Service
import java.io.File
import java.util.regex.Pattern
import kotlin.io.path.Path

@Service
class GeneratorServiceService(private var databaseService: DatabaseService) :
    GeneratorService {

    override fun generateModule(language: String?, module: String?, framework: String?): List<Table>? {
        val allTables = fillingTableInfo(databaseService.getAllTables())
        generator(language, module, framework, allTables)
        return allTables
    }

    /**
     * 生成模块
     */
    fun generator(language: String?, module: String?, framework: String?, allTables: List<Table>?) {
        val dir = if (language == CommonConstant.LANGUAGE_KOTLIN) {
            CommonConstant.DIR_KOTLIN
        } else {
            CommonConstant.DIR_JAVA
        }
        val frame = if (language == CommonConstant.LANGUAGE_KOTLIN) {
            getTemplate()?.kotlin
        } else {
            getTemplate()?.java
        }
        val templatesInfo = when (framework) {
            CommonConstant.FRAMEWORK_JIMMER -> {
                frame?.jimmer
            }

            CommonConstant.FRAMEWORK_MYBATIS -> {
                frame?.mybatis
            }

            CommonConstant.FRAMEWORK_MYBATIS_PLUS -> {
                frame?.mybatisPlus
            }

            CommonConstant.FRAMEWORK_MYBATIS_PLUS_MIXED -> {
                frame?.mybatisPlusMixed
            }

            CommonConstant.FRAMEWORK_SPRING_DATA_MONGODB -> {
                frame?.springDataMongodb
            }

            else -> {
                frame?.default
            }
        }


        val basePath = SystemUtil.getBasePath(dir)
        val project = getProject2JavaType()
        if (allTables != null && project != null) {
            for (table in allTables)
                gen(basePath, module, table, project, templatesInfo)
        }
    }

    fun gen(basePath: String, module: String?, table: Table, project: Project, templatesInfo: List<TemplateInfo>?) {
        val tableFields = databaseService.getTableFields(table.tableName)
        if (tableFields != null) {
            if (tableFields.isNotEmpty()) {
                val data: HashMap<String, Any> = fillingProjectInfo(module, table, project)
                if (templatesInfo != null) {
                    for (temp in templatesInfo) {
                        val createOutFilePath = createOutFilePath(basePath, module, temp.outPath, table.className)
                        val outFilePath = if (temp.templateName.contains(CommonConstant.LANGUAGE_TAG_MAPPER)) {
                                Path(createOutFilePathForMapper(CommonConstant.DIR_RESOURCE, module, temp.outPath, table.className))
                            } else {
                                Path(createOutFilePath)
                            }
                        val templatePath = Path(CommonConstant.DIR_RESOURCE + temp.templateName)

                        TemplateUtils.render(outFilePath, templatePath, data)
                    }
                }

            }
        }
    }

     fun createOutFilePathForMapper(dirResource: String, module: String?, outPath: String, className: String): String {
        val basePath = if (module != null) {
            dirResource+File.separator+ CommonConstant.LANGUAGE_TAG_MAPPER + File.separator + module
        } else {
            dirResource+File.separator
        }
        return basePath + File.separator + outPath.replace("{className}", className)
    }

    /**
     * 构建文件输出路径
     */
    fun createOutFilePath(basedir: String, module: String?, outPath: String, className: String): String {
        val basePath = if (module != null) {
            basedir + File.separator + module
        } else {
            basedir
        }
        return basePath + File.separator + outPath.replace("{className}", className)
    }

    /**
     * 填充项目信息
     */
    fun fillingProjectInfo(module: String?, table: Table, project: Project): HashMap<String, Any> {
        val data: HashMap<String, Any> = HashMap()
        project.packageName?.let { data.put("packageName", it) }
        project.version?.let { data.put("version", it) }
        table.className.let { data.put("ClassName", it) }
        table.className.sneak2camel(true).let { data.put("className", it) }
        table.tableComment.let { data.put("tableComment", it) }
        table.tableName.let { data.put("tableName", it) }
        module?.let { data.put("moduleName", it) }
        project.creator.author?.let { data.put("author", it) }
        project.creator.email?.let { data.put("email", it) }
        project.creator.phone?.let { data.put("phone", it) }
        table.date.let { data.put("date", it) }
        table.tableField?.let { data.put("fieldList", it) }
        return data
    }

    /**
     * 填充缺失的信息
     */
    fun fillingTableInfo(allTables: List<Table>?): List<Table>? {
        val dbType2JavaType = getDBType2JavaType()
        if (allTables != null && dbType2JavaType != null) {
            for (table in allTables) {
                val tableField = table.tableField
                if (tableField != null) {
                    for (field in tableField) {
                        for (type in dbType2JavaType.typeMappings) {
                            if (Pattern.matches(type.column, field.fieldType)) {
                                field.fieldJavaType = type.java
                                field.fieldKotlinType = type.kotlin
                            }
                        }
                    }

                }
            }
        }
        return allTables
    }

    /**
     * 获取模板信息
     */
    fun getTemplate(): Templates? {
        return getListFromJson("/template/template1.json")
    }

    /**
     * 获取数据类型映射信息
     */
    fun getDBType2JavaType(): TypeMappings? {
        return getListFromJson("/template/DBType.json")
    }

    /**
     * 获取项目相关配置
     */
    fun getProject2JavaType(): Project? {
        return getListFromJson("/template/project.json")
    }
}
