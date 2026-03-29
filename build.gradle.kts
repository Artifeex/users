plugins {
    java
    id("org.springframework.boot") version "3.5.11"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.sandr"
version = "0.0.1-SNAPSHOT"
description = "users"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")

    // Source: https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    // Source: https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-impl
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    // Source: https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-jackson
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.6.3")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // shedlock
    implementation("net.javacrumbs.shedlock:shedlock-spring:6.10.0")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:6.10.0")

    // OpenAPI 3 + Swagger UI (https://springdoc.org)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    // Excel streaming import (SAX/event model — no full-workbook RAM loading)
    implementation("org.apache.poi:poi-ooxml:5.4.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
