/*
 * SPDX-FileCopyrightText: 2015 - 2025 Rime community
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@file:Suppress("UnstableApiUsage")

plugins {
    id("com.osfans.trime.app-convention")
    id("com.osfans.trime.native-app-convention")
    id("com.osfans.trime.data-checksums")
    id("com.osfans.trime.native-cache-hash")
    id("com.osfans.trime.opencc-data")
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
}

// 拍台文 fork 版號：gradle.properties 的 taigiVersion（x.y.z，與 git tag vX.Y.Z 一致）。
// versionCode 必須逐版遞增，Android 才能覆蓋升級（Obtainium 亦依此判斷）；
// 1_00_000_000 起跳，保證大於上游沿用的 20260901。
val taigiVersion: String = providers.gradleProperty("taigiVersion").get()
val taigiVersionCode: Int =
    Regex("""(\d+)\.(\d+)\.(\d+)""").matchEntire(taigiVersion)?.destructured?.let { (major, minor, patch) ->
        100_000_000 + major.toInt() * 1_000_000 + minor.toInt() * 1_000 + patch.toInt()
    } ?: error("taigiVersion must be x.y.z, got '$taigiVersion'")

android {
    namespace = "com.osfans.trime"
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.soanseng.phahtaibun" // 拍台文特化版: 與官方同文可並存
        minSdk = 21
        targetSdk = 36
        versionCode = taigiVersionCode
        versionName = taigiVersion

        multiDexEnabled = true
        buildConfigField("String", "BUILDER", "\"${project.builder}\"")
        buildConfigField("long", "BUILD_TIMESTAMP", project.buildTimestamp)
        buildConfigField("String", "BUILD_COMMIT_HASH", "\"${project.buildCommitHash}\"")
        buildConfigField("String", "BUILD_GIT_REPO", "\"${project.buildGitRepo}\"")
        buildConfigField("String", "BUILD_VERSION_NAME", "\"${project.buildVersionName}\"")
    }

    base {
        // https://www.norio.be/blog/archivesBaseName-removed-from-gradle9.html
        archivesName = "${android.defaultConfig.applicationId}-$buildVersionName"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        resValues = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig =
                project.signKeyFile?.let {
                    signingConfigs.create("release") {
                        storeFile = it
                        storePassword = project.signKeyStorePwd
                        keyAlias = project.signKeyAlias
                        keyPassword = project.signKeyPwd
                    }
                }

            resValue("string", "trime_app_name", "@string/app_name_release")
        }
        debug {
            applicationIdSuffix = ".debug"

            resValue("string", "trime_app_name", "@string/app_name_debug")
        }
        all {
            // remove META-INF/version-control-info.textproto
            @Suppress("UnstableApiUsage")
            vcsInfo.include = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // hack workaround lint gradle 8.0.2
    lint {
        checkReleaseBuilds = false
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    packaging {
        resources {
            excludes +=
                setOf(
                    "/META-INF/*.version",
                    "/META-INF/*.kotlin_module", // cannot be excluded actually
                    "/META-INF/androidx/**",
                    "/DebugProbesKt.bin",
                    "/kotlin-tooling-metadata.json",
                )
        }
    }
}

aboutLibraries {
    collect {
        configPath.set(file("licenses").takeIf { it.exists() })
        fetchRemoteLicense.set(false)
        fetchRemoteFunding.set(false)
        includePlatform.set(false)
    }
    export {
        excludeFields.set(
            setOf("generated", "developers", "organization", "scm", "funding", "content"),
        )
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// 拍台文 fork: 上游 patches/lua.patch 不會自動套用, 這裡在 CMake configure 前套用
// (bionic API21 arm32 的 fseeko/ftello 未宣告, Lua LUA_USE_POSIX 分支編不過).
val applyUpstreamLuaPatch: TaskProvider<Exec> =
    tasks.register<Exec>("applyUpstreamLuaPatch") {
        description = "Apply patches/lua.patch to thirdparty lua5.4 (idempotent)"
        val target = "app/src/main/jni/librime/plugins/librime-lua/thirdparty"
        val patchFile = rootProject.file("patches/lua.patch").absolutePath
        commandLine(
            "bash",
            "-c",
            "patch -N --forward -p1 -d '$target' -i '$patchFile' || true",
        )
        workingDir = rootProject.projectDir
    }

tasks.matching { it.name.startsWith("configureCMake") }.configureEach {
    dependsOn(applyUpstreamLuaPatch)
}

// 拍台文 fork: 上游 generateDataChecksums 沒掛進 build, 沒跑會讓 DataManager.sync
// 在開 App 時炸 (assets.open("checksums.json") 找不到) — 綁進 preBuild 保證產出.
tasks.named("preBuild") {
    dependsOn("generateDataChecksums")
}

dependencies {
    ksp(project(":codegen"))
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.autofill)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.recyclerview)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.flexbox)
    implementation(libs.bravh)
    implementation(libs.timber)
    implementation(libs.xxpermissions)
    implementation(libs.kodein.di)
    implementation(libs.snakeyaml)
    implementation(libs.splitties.bitflags)
    implementation(libs.splitties.systemservices)
    implementation(libs.splitties.views.dsl)
    implementation(libs.splitties.views.dsl.constraintlayout)
    implementation(libs.splitties.views.dsl.coordinatorlayout)
    implementation(libs.splitties.views.dsl.recyclerview)
    implementation(libs.splitties.views.recyclerview)
    implementation(libs.aboutlibraries.core)
    implementation(libs.iconics.core)
    implementation(libs.community.material.typeface) {
        artifact { type = "aar" }
    }

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    androidTestImplementation(libs.junit)
}

configurations {
    all {
        // remove Baseline Profile Installer or whatever it is...
        exclude(group = "androidx.profileinstaller", module = "profileinstaller")
    }
}
