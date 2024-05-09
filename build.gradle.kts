import java.io.FileInputStream
import java.util.*

plugins {

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
//    implementation("org.springframework.boot:spring-security-config")
//    implementation(group:"org.springframework.security", name: 'spring-security-config', version: '3.2.0.RELEASE')
    implementation("com.unboundid:unboundid-ldapsdk:1.1.3")
    implementation("io.jsonwebtoken:jjwt:0.12.0")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")



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
    annotationProcessor("org.mapstruct:mapstruct-processor:${versions["mapstruct"]}")
    implementation("org.mapstruct:mapstruct:${versions["mapstruct"]}")

    // testing
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks {

    withType<JavaCompile> {
        options.compilerArgs = listOf(
            "-Amapstruct.defaultComponentModel=spring",
            "-parameters"
        )
    }

    test {
        useJUnitPlatform()
    }

    checkstyle {
        isShowViolations = true

        checkstyleTest {
            enabled = false
        }

    }

    bootJar {
        enabled = true
    }

    jar {
        enabled = true
    }
}
