// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // add the AGP version here
        classpath("com.android.tools.build:gradle:9.3.2")
        // other dependencies that you use in project level
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.9.8")
    }
}

plugins {

}
