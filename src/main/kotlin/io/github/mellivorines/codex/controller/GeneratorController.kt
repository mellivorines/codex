package io.github.mellivorines.codex.controller

import io.github.mellivorines.codex.model.database.Table
import io.github.mellivorines.codex.service.GeneratorService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api")
class GeneratorController(private var generatorService: GeneratorService) {


    @GetMapping("/generator")
    fun getTableField(
        @RequestParam("language") language: String?,
        @RequestParam("framework") framework: String?,
        @RequestParam("module") module: String?
    ): List<Table>? {
        return generatorService.generateModule(language, module, framework)
    }

}
