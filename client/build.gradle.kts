// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  application
  id("com.android.application") version "8.2.0" apply false
  id("org.jetbrains.kotlin.android") version "1.8.10" apply false
  id("com.google.gms.google-services") version "4.4.0" apply false
  kotlin("plugin.serialization") version "1.8.10" apply false
}