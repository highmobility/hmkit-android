# HMKit Android

HMKit Android provides access to vehicles via Bluetooth or Telematics.

### Requirements

* Android 5.0 Lollipop or higher.

### Install

Releases are pushed to our jfrog artifactory. To include hmkit-android in your project, add to the 
build.gradle:

```
repositories {
  maven { url "http://jfrog.high-mobility.com/artifactory/gradle-release-local/" }
}

dependencies {
  implementation('com.highmobility:hmkit-android:2.0.0@aar') {
      transitive=true
    }
}
```

Please not Java 1.8 support is required:

```
android {
  ...

  compileOptions {
      targetCompatibility 1.8
      sourceCompatibility 1.8
  }
}
```

Find the latest version name in [the developer center](https://develop.high-mobility.net/downloads/).


### Setup

* git submodule update --init --recursive
* import the Gradle project.
* Build the core:  
Install NDK through Android SDK Manager(Tools tab). Then:
```
cd hmkit-android/src/main/jni && ndk-build && cd -
```
* Build hmkit-android module.
* If there are errors: Try `Gradle clean`, `File > Invalidate caches and restart`.

Supported devices: Lollipop 5.0+ with chipset support for BLE peripheral mode, https://stackoverflow.com/questions/26482611/chipsets-devices-supporting-android-5-ble-peripheral-mode https://altbeacon.github.io/android-beacon-library/beacon-transmitter-devices.html - list of some devices.

### Release

#### Pre checks

* run the unit and instrumentation tests:
```./gradlew test && ./gradlew cAT```

This project bundles all of the Android SDK packages: hmkit-android, hmkit-crypto and hmkit-utils.

For a release, update the "version = 1.5.0" in all of the deploy.settings files(if needed).

Set the release environment in root build.gradle (ext property release = 0/1/2).
call ./gradlew artifactoryPublish to release all of the packages.
call ./gradlew :hmkit-utils:artifactoryPublish to release a specific package.

If pushing the same version number, in dev package will be overwritten, in release rejected.

If releasing to prod, also call "./gradlew bintrayUpload".