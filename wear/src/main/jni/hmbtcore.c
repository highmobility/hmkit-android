#include <jni.h>
#include <stddef.h>
#include "hm_bt_core.h"

JNIEXPORT void JNICALL
Java_com_high_1mobility_btcore_HMBTCore_HMBTCoreInit(JNIEnv *env, jobject instance,
                                                     jobject coreInterface) {
    hm_bt_core_init();

    //TODO store callback reference
}

JNIEXPORT void JNICALL
Java_com_high_1mobility_btcore_HMBTCore_HMBTCoreClock(JNIEnv *env, jobject instance) {

    hm_bt_core_clock();
}

JNIEXPORT void JNICALL
Java_com_high_1mobility_btcore_HMBTCore_HMBTCoreSensingReadNotification(JNIEnv *env,
                                                                        jobject instance,
                                                                        jbyteArray mac_) {
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_sensing_read_notification(mac);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_high_1mobility_btcore_HMBTCore_HMBTCoreSensingReadResponse(JNIEnv *env, jobject instance,
                                                                    jbyteArray data_, jint size,
                                                                    jint offset, jbyteArray mac_) {
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_sensing_read_response(data,size,offset,mac);

    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_high_1mobility_btcore_HMBTCore_HMBTCoreSensingWriteResponse(JNIEnv *env, jobject instance,
                                                                     jbyteArray mac_) {
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_sensing_write_response(mac);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_high_1mobility_btcore_HMBTCore_HMBTCoreSensingPingNotification(JNIEnv *env,
                                                                        jobject instance,
                                                                        jbyteArray mac_) {
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_sensing_ping_notification(mac);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_high_1mobility_btcore_HMBTCore_HMBTCoreSensingProcessAdvertisement(JNIEnv *env,
                                                                            jobject instance,
                                                                            jbyteArray mac_,
                                                                            jbyteArray data_,
                                                                            jint size) {
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);

    hm_bt_core_sensing_process_advertisement(mac,data,size);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
}

JNIEXPORT void JNICALL
Java_com_high_1mobility_btcore_HMBTCore_HMBTCoreSensingDiscoveryEvent(JNIEnv *env, jobject instance,
                                                                      jbyteArray mac_) {
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_sensing_discovery_event(mac);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_high_1mobility_btcore_HMBTCore_HMBTCoreSensingScanStart(JNIEnv *env, jobject instance) {

    hm_bt_core_sensing_scan_start();

}

JNIEXPORT void JNICALL
Java_com_high_1mobility_btcore_HMBTCore_HMBTCoreSensingConnect(JNIEnv *env, jobject instance,
                                                               jbyteArray mac_) {
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_sensing_connect(mac);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_high_1mobility_btcore_HMBTCore_HMBTCoreSensingDisconnect(JNIEnv *env, jobject instance,
                                                                  jbyteArray mac_) {
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_sensing_disconnect(mac);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_high_1mobility_btcore_HMBTCore_HMBTCorelinkConnect(JNIEnv *env, jobject instance,
                                                            jbyteArray mac_) {
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_link_connect(mac);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_high_1mobility_btcore_HMBTCore_HMBTCorelinkDisconnect(JNIEnv *env, jobject instance,
                                                               jbyteArray mac_) {
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_link_disconnect(mac);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_high_1mobility_btcore_HMBTCore_HMBTCorelinkIncomingData(JNIEnv *env, jobject instance,
                                                                 jbyteArray data_, jint size,
                                                                 jbyteArray mac_) {
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_link_incoming_data(data,size,mac);

    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}