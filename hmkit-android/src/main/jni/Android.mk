LOCAL_PATH := ../../../../hm-java-core-jni/src/main/jni

include $(CLEAR_VARS)
LOCAL_MODULE := crypto
LOCAL_SRC_FILES := $(LOCAL_PATH)/../prebuiltLibs/$(TARGET_ARCH_ABI)/libcrypto.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/openssl
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := ssl
LOCAL_SRC_FILES := $(LOCAL_PATH)/../prebuiltLibs/$(TARGET_ARCH_ABI)/libssl.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/openssl
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := hmbtcore

LOCAL_LDLIBS := -llog
LOCAL_LDLIBS += -lz
LOCAL_C_INCLUDES += ../../../../high-mobility-bt-core
LOCAL_C_INCLUDES += ../../../../crypto-c/Crypto

LOCAL_SRC_FILES := $(LOCAL_PATH)/hmbtcore.c
LOCAL_SRC_FILES += $(LOCAL_PATH)/hm_bt_crypto_hal.c
LOCAL_SRC_FILES += $(LOCAL_PATH)/hm_bt_debug_hal.c
LOCAL_SRC_FILES += $(LOCAL_PATH)/hm_connectivity_hal.c
LOCAL_SRC_FILES += $(LOCAL_PATH)/hm_bt_persistence_hal.c
LOCAL_SRC_FILES += $(LOCAL_PATH)/hm_api_callback.c

LOCAL_SRC_FILES += ../../../../crypto-c/Crypto/Crypto.c

LOCAL_SRC_FILES += ../../../../high-mobility-bt-core/hm_bt_core.c
LOCAL_SRC_FILES += ../../../../high-mobility-bt-core/hm_bt_log.c
LOCAL_SRC_FILES += ../../../../high-mobility-bt-core/hm_cert.c
LOCAL_SRC_FILES += ../../../../high-mobility-bt-core/hm_conf_access.c
LOCAL_SRC_FILES += ../../../../high-mobility-bt-core/hm_api.c

LOCAL_STATIC_LIBRARIES := crypto
LOCAL_STATIC_LIBRARIES += ssl

include $(BUILD_SHARED_LIBRARY)