plugins {
    kotlin("jvm") version "1.9.20"
    java
    application
    id("me.champeau.jmh") version "0.7.3"
    // id("org.openjdk.jcstress") version "0.15"
}

group = "org.itmo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.openjdk.jcstress:jcstress-core:0.16")
    testAnnotationProcessor("org.openjdk.jcstress:jcstress-core:0.16")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("MainKt")
}

jmh {
    jvmArgs = listOf("--enable-native-access=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED")
}

sourceSets {
    test {
        java.srcDirs("src/test/kotlin", "src/test/java")
    }
}

// JCStress runner task: runs JCStress tests located on the test runtime classpath
// Use: ./gradlew jcstress [-PjcstressArgs="-v -m quick"]
tasks.register<JavaExec>("jcstress") {
    group = "verification"
    description = "Run JCStress stress tests"
    mainClass.set("org.openjdk.jcstress.Main")
    classpath = sourceSets.test.get().runtimeClasspath
    dependsOn("testClasses")

    val argsProp = project.findProperty("jcstressArgs") as String?
    if (!argsProp.isNullOrBlank()) {
        args = argsProp.split("\\s+".toRegex())
    }
}

