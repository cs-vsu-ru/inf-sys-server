import java.io.FileInputStream
import java.util.*

plugins {
    java
    application
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"

    checkstyle
}

group = "vsu.cs.is"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

val versions = Properties()
versions.load(FileInputStream("${projectDir}/version.properties"))


dependencies {

    // spring
    implementation("org.springframework.boot:spring-boot-starter-web")

    // security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // springdoc
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${versions["springdoc"]}")

    // database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.liquibase:liquibase-core")
    runtimeOnly("org.postgresql:postgresql")

    // observability
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // devtools
    implementation("org.springframework.boot:spring-boot-starter-validation")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")
    implementation("org.mapstruct:mapstruct:${versions["mapstruct"]}")

    // testing
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<JavaCompile> {
    options.compilerArgs = listOf(
        "-Amapstruct.defaultComponentModel=spring"
    )
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Checkstyle>().configureEach {
    reports {

        isShowViolations = true
    }
}

tasks.bootJar {
    enabled = true
}

tasks.jar {
    enabled = true
}
