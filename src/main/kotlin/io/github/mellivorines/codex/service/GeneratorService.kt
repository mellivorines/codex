package io.github.mellivorines.codex.service

import io.github.mellivorines.codex.model.database.Table

interface GeneratorService {
    fun generateModule(language: String?, module: String?, framework: String?): List<Table>?
}
