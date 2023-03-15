package io.github.mellivorines.codex.model.project


data class Project(
    var project: String?,
    var module: String?,
    var packageName: String?,
    var version: String?,
    var openApi: Boolean = false,
    var creator: Creator
)

data class Creator(
    var author: String?,
    var email: String?,
    var phone: String?
)
