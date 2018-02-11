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