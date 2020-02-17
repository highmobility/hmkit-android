# HMKit Android

The HMKit Android SDK makes it easy to work with car data using the HIGH MOBILITY API platform. The SDK implements a strong security layer between your Android app and the platform while providing a straightforward native interface to read from, and write to, connected cars.
In addition, the SDK provides a UI component used to initate OAuth2 for the end-user in order to retrieve consent for the sharing of data.

# Table of contents

* [Requirements](#requirements)
* [Getting Started](#getting-started)
* [Setup](#setup)
* [Architecture](#architecture)
* [Contributing](#contributing)
* [Release](#release)
* [Licence](#Licence)

### Requirements

* Android 5.0 Lollipop or higher. 
* For Bluetooth, chipset support for BLE peripheral mode. https://stackoverflow.com/questions/26482611/chipsets-devices-supporting-android-5-ble-peripheral-mode https://altbeacon.github.io/android-beacon-library/beacon-transmitter-devices.html - list of some devices. 

### Getting Started

Get started with HMKit Android 📘[browse the documentation](https://high-mobility.com/learn/tutorials/sdk/android/).

### Setup

* `git submodule update --init --recursive`
* import the Gradle project.
* Build HMKit Core:  
  * Install NDK through Android SDK Manager(Tools tab)
  * `cd hmkit-android/src/main/jni && ndk-build && cd -`
* Run the unit tests: `./gradlew test`
* If there are errors: Try `Gradle clean`, `File > Invalidate caches and restart`.
* Now **hm-android-basic-oauth** or **hm-android-bluetooth-auto-api-explorer** targets can be run with local code.

### Architecture

**General**: HMKit Android is a Java/Kotlin library that handles Bluetooth/Telematics connectivity. Security is implemented via JNI to the HMKit Core C module.

**hmkit-android**: Contains HMKit Android Java/Kotlin classes.

**hmkit-core-jni**: Contains JNI classes to HMKit Core.

**hmkit-crypto**: Contains necessary crypto classes and functions.

**hmkit-utils**: Contains general helper methods and classes.

**hm-android-basic-oauth** and **hm-android-bluetooth-auto-api-explorer**: Sample apps for testing.

### Contributing
We happily accept your patches and contributions to this project. Before starting work, please first discuss the changes that you wish to make with us via [GitHub Issues](https://github.com/highmobility/hmkit-android/issues), [Spectrum](https://spectrum.chat/high-mobility/) or [Slack](https://slack.high-mobility.com/).

See more in 📘[Contributing](CONTRIBUTE.md).

### Release

All of the HMKit Android packages can be released from this project. This includes hmkit-android, hmkit-crypto, hmkit-utils and hmkit-auto-api.

**Pre checks**

* Run the unit and instrumentation tests: `./gradlew test && ./gradlew cAT`

**Release**

* Update the "version = 1.5.0" in all of the deploy.settings files (if necessary).
* Set the release environment in root build.gradle (ext property release = 0/1/2).
* Call `./gradlew artifactoryPublish` to release all of the packages.
* Call `./gradlew :hmkit-utils:artifactoryPublish` to release a specific package.
* If releasing to prod, also call `./gradlew bintrayUpload`.

If pushing the same version number, the package will be overwritten in dev, rejected in release.

### Licence
This repository is using the MIT licence. See more in 📘[LICENCE](LICENCE.md)