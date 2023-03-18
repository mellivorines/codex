import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

var groupString = "io.github.mellivorines"
var versionString = "0.0.1"
var artifactString = "codex"

plugins {
    id("org.springframework.boot") version "3.0.4"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    id("org.jetbrains.dokka") version "1.6.10"

    id("maven-publish")
    id("signing")
}
group = groupString
version = versionString

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-freemarker")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.freemarker:freemarker:2.3.32")
    implementation("org.apache.velocity:velocity-engine-core:2.3")

    implementation("com.mysql:mysql-connector-j:8.0.32")
    implementation("com.microsoft.sqlserver:mssql-jdbc:12.2.0.jre11")
    implementation("org.postgresql:postgresql:42.5.4")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("com.github.xiaoymin:knife4j-openapi3-jakarta-spring-boot-starter:4.0.0")
    implementation("cn.hutool:hutool-all:5.8.15")

    dokkaHtmlPlugin("org.jetbrains.dokka:dokka-base:1.6.0")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks {
    withType(Jar::class) {
        if (archiveClassifier.get() == "javadoc") {
            dependsOn(dokkaHtml)
            from("build/dokka/html")
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = groupString
            artifactId = artifactString
            version = versionString
            from(components["java"])
        }
        create<MavenPublication>("mavenJava") {
            pom {
                name.set("codex")
                description.set("A concise description of my library")
                url.set("https://github.com/mellivorines/codex.git")
                properties.set(
                    mapOf(
                        "myProp" to "value",
                        "prop.with.dots" to "anotherValue"
                    )
                )
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("mellivorines")
                        name.set("mellivorines")
                        email.set("lilinxi015@163.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://example.com/my-library.git")
                    developerConnection.set("scm:git:ssh://example.com/my-library.git")
                    url.set("https://github.com/mellivorines/codex")
                }
            }
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
        }
    }
    repositories {
        val sonatypeUsername = ""
        val sonatypePassword = ""
        maven {
            val releasesRepoUrl = layout.buildDirectory.dir("repos/releases")
            val snapshotsRepoUrl = layout.buildDirectory.dir("repos/snapshots")
//            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }
}




