package io.github.mellivorines.codex.model.type

data class TypeMappings(
    var typeMappings: List<Type>
)

data class Type(
    var column: String,
    var java: String,
    var kotlin: String
)
