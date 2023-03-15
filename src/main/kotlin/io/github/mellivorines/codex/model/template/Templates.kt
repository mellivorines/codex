package io.github.mellivorines.codex.model.template


data class Templates(
    val java: Framework,
    val kotlin: Framework
)

data class Framework(
    val default: List<TemplateInfo>,
    val jimmer: List<TemplateInfo>,
    val mybatis: List<TemplateInfo>,
    val mybatisPlus: List<TemplateInfo>,
    val mybatisPlusMixed: List<TemplateInfo>,
    val springDataMongodb: List<TemplateInfo>
)

data class TemplateInfo(
    val className: String,
    val outPath: String,
    val templateName: String
)
