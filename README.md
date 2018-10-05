This is the Android HMKit with Bluetooth Explorer sample app.

### Setup

* git submodule update --init --recursive
* import the Gradle project.
* The test app is the Bluetooth Auto API Explorer app. run MainActivity in ble-explorer-app package.
* If there are errors: Try `Gradle clean`, `File > Invalidate caches and restart`.

Supported devices: Lollipop 5.0+ with chipset support for BLE peripheral mode, https://stackoverflow.com/questions/26482611/chipsets-devices-supporting-android-5-ble-peripheral-mode https://altbeacon.github.io/android-beacon-library/beacon-transmitter-devices.html - list of some devices.

### Compile core

To compile the bt core, you need to install the NDK through Android SDK Manager(in SDK tools tab).

```
cd {PROJECT_DIR}/hmlink/src/main/jni 
ndk-build
```

### Release

This project bundles all of the Android SDK packages: hmkit-android, hmkit-crypto and hmkit-utils.

For a release, update the "version = 1.5.0" in all of the deploy.settings files(if needed).

Set the release environment in root build.gradle (ext property release = 0/1/2).
call ./gradlew artifactoryPublish to release all of the packages.
call ./gradlew :hmkit-android:artifactoryPublish to release a specific package.

If pushing the same version number, in dev package will be overwritten, in release rejected.

If releasing to prod, also call "./gradlew bintrayUpload".