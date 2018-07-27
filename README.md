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

Before a release:

* Update the "version = 1.4.0" in all of the deploy.settings files.
* Update the dependencies from local to remote for all of the packages:

>in hmkit-crypto replace 
>`//implementation project(':hm-java-utils')` with `implementation('com.highmobility:hmkit-utils:1.4.0')`

>in hmkit-android replace 
```implementation project(':hm-java-crypto')
implementation project(':hm-java-utils')``` with
`implementation("com.highmobility:hmkit-crypto:$rootProject.ext.cryptoVersion")`


By default, releases will be pushed to our dev jfrog repo. Call `./gradlew artifactoryPublish` to release all of
the packages.

To release to the release jfrog repo and bintray, replace

`repo = gradle-dev-local` with `repo = gradle-release-local` in all of the deploy.settings files.

Call `./gradlew artifactoryPublish && ./gradlew bintrayUpload`. Revert the repo to gradle-dev-local.