### What is this repository for?

* This is the Android SDK with Mobile and Wear reference apps.

### How do I get set up?

* git submodule init
* git submodule update --recursive --remote

Supported devices: Lollipop 5.0+ with chipset support for BLE peripheral mode, https://stackoverflow.com/questions/26482611/chipsets-devices-supporting-android-5-ble-peripheral-mode https://altbeacon.github.io/android-beacon-library/beacon-transmitter-devices.html - list of some devices.

### Compile core
* If you wish to re compile the bt core, you need to install the NDK through Android SDK Manager(in SDK tools tab).
```
cd {PROJECT_DIR}/hmlink/src/main/jni 
ndk-build
```


### Releasing

This project bundles all of the Android SDK packages: hmkit-android, hmkit-crypto and hmkit-utils.

For a release, update the "version = 1.5.0" in all of the deploy.settings files(if needed).

Set the release environment in project's build.gradle.
call ./gradlew artifactoryPublish to release all of the packages.
call ./gradlew :hmkit-android:artifactoryPublish to release specific packages.

If pushing the same version number, in dev package will be overwritten, in release rejected.

If releasing to prod, also call "./gradlew bintrayUpload".