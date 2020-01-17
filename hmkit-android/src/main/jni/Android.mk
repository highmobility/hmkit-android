CORE_PATH := ../../../../hm-java-core-jni
LOCAL_PATH := $(CORE_PATH)/src/main/jni

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
LOCAL_C_INCLUDES += $(CORE_PATH)/hmkit-core
LOCAL_C_INCLUDES += $(CORE_PATH)/hmkit-crypto-c

LOCAL_SRC_FILES := $(LOCAL_PATH)/hmbtcore.cpp
LOCAL_SRC_FILES += $(LOCAL_PATH)/hmkit_core_crypto_hal.c
LOCAL_SRC_FILES += $(LOCAL_PATH)/hmkit_core_debug_hal.c
LOCAL_SRC_FILES += $(LOCAL_PATH)/hmkit_core_connectivity_hal.c
LOCAL_SRC_FILES += $(LOCAL_PATH)/hmkit_core_persistence_hal.c
LOCAL_SRC_FILES += $(LOCAL_PATH)/hmkit_core_api_callback.c

LOCAL_SRC_FILES += $(CORE_PATH)/hmkit-crypto-c/Crypto.c

LOCAL_SRC_FILES += $(CORE_PATH)/hmkit-core/hmkit_core.c
LOCAL_SRC_FILES += $(CORE_PATH)/hmkit-core/hmkit_core_log.c
LOCAL_SRC_FILES += $(CORE_PATH)/hmkit-core/hmkit_core_cert.c
LOCAL_SRC_FILES += $(CORE_PATH)/hmkit-core/hmkit_core_conf_access.c
LOCAL_SRC_FILES += $(CORE_PATH)/hmkit-core/hmkit_core_api.c

LOCAL_STATIC_LIBRARIES := crypto
LOCAL_STATIC_LIBRARIES += ssl

include $(BUILD_SHARED_LIBRARY)