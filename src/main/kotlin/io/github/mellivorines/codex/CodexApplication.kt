package io.github.mellivorines.codex

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CodexApplication

fun main(args: Array<String>) {
    runApplication<CodexApplication>(*args)
}
