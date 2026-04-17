import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.gradle.internal.os.OperatingSystem

plugins {
    java
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.6.0"
    id("checkstyle")
    id("com.diffplug.spotless") version "6.25.0"
    id("jacoco")
    id("org.sonarqube") version "5.0.0.4638"  // Optionnel
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "demo2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

// ============================================================
// SPOTLESS - FORMATAGE DU CODE
// ============================================================

spotless {
    java {
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        //target("src/**")
        // 🔥 IMPORTANT : Exclure les fichiers générés automatiquement
        targetExclude(
            // Exclure tout le dossier du client généré
            "**/build/**",
            "**/client/**",
            "**/generated/**",
            "**/gen/**",

            // Exclure spécifiquement le client API généré
            "src/main/java/com/example/demo/client/**",
        )
    }
}

// ============================================================
// CHECKSTYLE - QUALITE STATIQUE
// ============================================================

checkstyle {
    toolVersion = "10.17.0"
    configFile = file("$rootDir/config/checkstyle/google_checks_custom.xml")
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required = true
        html.required = true
    }
}

// ============================================================
// GENERATION CLIENT API
// ============================================================

val generateJavaClient by tasks.registering(GenerateTask::class)  {
    //outputDir.set("$projectDir/src/main/java")

    generatorName.set("java")
    groupId.set("com.example")
    id.set("demo-gen")

    outputDir.set("$buildDir/gen")
    inputSpec.set("$projectDir/src/main/resources/api/api.yml")

    apiPackage.set("com.example.demo.client.api")
    invokerPackage.set("com.example.demo.client.invoker")
    modelPackage.set("com.example.demo.client.model")

    library.set("native") // Client Java moderne (HttpClient natif)

    configOptions.set(
        mapOf(
            "serializationLibrary" to "jackson",
            "dateLibrary" to "custom",
            "useJakartaEe" to "true"
        )
    )
    
    typeMappings.set(
        mapOf(
            "Date" to "java.time.LocalDate",
            "DateTime" to "java.time.Instant"
        )
    )

    skipValidateSpec.set(false)
    logToStderr.set(true)
    generateApiTests.set(false)
    generateModelTests.set(false)
}

val generateTsClient by tasks.registering(GenerateTask::class) {

    generatorName.set("typescript-axios")
    inputSpec.set("$rootDir/doc/api.yml")
    outputDir.set("$buildDir/gen-ts")
}


val publishJavaClientToMavenLocal by tasks.registering(Exec::class) {

    dependsOn(generateJavaClient)

    if (OperatingSystem.current().isWindows) {
        commandLine("cmd", "/c", "./.shell/publish_gen_to_maven_local.bat")
    } else {
        commandLine("./.shell/publish_gen_to_maven_local.sh")
    }
}

tasks.named("compileJava") {
    dependsOn(publishJavaClientToMavenLocal)
}


// ============================================================
// JACOCO - COUVERTURE DE CODE
// ============================================================

jacoco {
    toolVersion = "0.8.11"
}


// Définir les exclusions
val jacocoExcludePatterns = listOf(
    "**/client/**",
    "**/model/**",
    "**/api/**",
    "**/invoker/**",
    "**/dto/**",
    "**/config/**",
    "**/*Application.class",
    "**/*Application*.*"
)

// Tâche pour générer le rapport JaCoCo
tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        html.required = true
        xml.required = true
        csv.required = false
    }

    // Exclure les classes des packages spécifiés
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it).matching {
                exclude(jacocoExcludePatterns)
            }
        })
    )

}

// Tâche pour vérifier les seuils de couverture
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)

    // Exclure les classes des packages spécifiés
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it).matching {
                exclude(jacocoExcludePatterns)
            }
        })
    )

    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.50".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.40".toBigDecimal()
            }
        }
    }

}


// ============================================================
// SONARQUBE - ANALYSE CONTINUE (optionnel)
// ============================================================

sonarqube {
    properties {
        property("sonar.projectKey", "votre-projet-key")
        property("sonar.organization", "votre-organization")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/jacoco/jacocoTestReport.xml").get().asFile.toString())
        property("sonar.java.checkstyle.reportPaths",
            layout.buildDirectory.file("reports/checkstyle/main.xml").get().asFile.toString())
        property("sonar.exclusions", "**/client/**, **/model/**, **/dto/**, **/config/**")
    }
}





dependencies {
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.flywaydb:flyway-database-postgresql")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    annotationProcessor("org.projectlombok:lombok")

    // ============================================================
    // TEST DEPENDENCIES (NOUVELLES)
    // ============================================================
    // Spring Boot Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    // Testcontainers pour PostgreSQL
    testImplementation("org.testcontainers:testcontainers:1.20.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("org.testcontainers:postgresql:1.20.4")

    // JavaFaker pour générer des données de test aléatoires
    //testImplementation("com.github.javafaker:javafaker:1.0.2")

    // Lombok pour les tests
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // JUnit Platform
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // AJOUTER ces dépendances pour le client Docker
    testImplementation("com.github.docker-java:docker-java:3.3.6")
    testImplementation("com.github.docker-java:docker-java-transport-httpclient5:3.3.6")

    implementation("io.sentry:sentry-spring-boot-starter:5.5.3")
    implementation("io.sentry:sentry-logback:5.5.3")
}


// ============================================================
// CONFIGURATION DES TESTS
// ============================================================

tasks.withType<Test> {
    useJUnitPlatform()

    // Testcontainers reuse (optionnel)
    systemProperty("testcontainers.reuse.enable", "true")

    // 🔥 IMPORTANT FIX DOCKER
    systemProperty(
        "testcontainers.docker.socket.override",
        "/var/run/docker.sock"
    )
    systemProperty("testcontainers.docker.client.strategy",
        "org.testcontainers.dockerclient.UnixSocketClientProviderStrategy")

    systemProperty("docker.host", "unix:///var/run/docker.sock")

    // Logs plus détaillés
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
    }

    ignoreFailures = false

    // JaCoCo s'attache automatiquement à cette tâche
    finalizedBy(tasks.jacocoTestReport)
}
