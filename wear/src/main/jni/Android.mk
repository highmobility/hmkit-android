LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := crypto
LOCAL_SRC_FILES := $(LOCAL_PATH)/../prebuiltLibs/$(TARGET_ARCH_ABI)/libcrypto.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/openssl
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := ssl
LOCAL_SRC_FILES := $(LOCAL_PATH)/../prebuiltLibs/$(TARGET_ARCH_ABI)/libssl.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/openssl
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := hmbtcore

LOCAL_LDLIBS := -llog
LOCAL_C_INCLUDES += ../../../../../high-mobility-bt-core
LOCAL_C_INCLUDES += ../../../../../crypto-c/Crypto
LOCAL_C_INCLUDES += openssl

LOCAL_SRC_FILES := hmbtcore.c

LOCAL_SRC_FILES += hm_bt_crypto_hal.c
LOCAL_SRC_FILES += hm_bt_debug_hal.c
LOCAL_SRC_FILES += hm_bt_hal.c
LOCAL_SRC_FILES += hm_bt_persistence_hal.c
LOCAL_SRC_FILES += hm_ctw_customer.c

LOCAL_SRC_FILES += ../../../../../crypto-c/Crypto/Crypto.c

LOCAL_SRC_FILES += ../../../../../high-mobility-bt-core/hm_bt_core.c
LOCAL_SRC_FILES += ../../../../../high-mobility-bt-core/hm_cert.c
LOCAL_SRC_FILES += ../../../../../high-mobility-bt-core/hm_conf_access.c
LOCAL_SRC_FILES += ../../../../../high-mobility-bt-core/hm_ctw_api.c

LOCAL_SHARED_LIBRARIES := crypto
LOCAL_SHARED_LIBRARIES += ssl

include $(BUILD_SHARED_LIBRARY)