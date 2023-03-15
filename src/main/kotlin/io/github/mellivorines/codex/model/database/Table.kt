package io.github.mellivorines.codex.model.database

data class Table(
    var tableName: String,
    var tableComment: String,
    var className: String,
    var date: String,
    var tableField: List<TableField>?
)

data class TableField(
    var tableName: String,
    var fieldName: String,
    var fieldComment: String,
    var fieldType: String,
    var fieldJavaType: String?,
    var fieldTName: String,
    var fieldKotlinType: String?,
    var fieldSize: Int
)
