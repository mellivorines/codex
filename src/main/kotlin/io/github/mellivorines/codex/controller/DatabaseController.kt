package io.github.mellivorines.codex.controller

import io.github.mellivorines.codex.model.database.Table
import io.github.mellivorines.codex.model.database.TableField
import io.github.mellivorines.codex.service.DatabaseService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class DatabaseController(private var databaseService: DatabaseService) {

    @GetMapping("/tableField")
    fun getTableField(@RequestParam("table") table: String): List<TableField>? {
        return databaseService.getTableFields(table)
    }

    @GetMapping("/allTable")
    fun getAllTable(): List<Table>? {
        return databaseService.getAllTables()
    }
}
