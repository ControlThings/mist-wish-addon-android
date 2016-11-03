LOCAL_PATH := $(call my-dir)
 
include $(CLEAR_VARS)
 
# Here we give our module name and source file(s)


WISH_MODULES = deps/mbedtls-2.1.2/library deps/cBSON wish_app wish_rpc deps/ed25519/src deps/bson

WISH_SRC := 
LOCAL_MODULE    := mist
LOCAL_SRC_FILES := android_mist.c jni_utils.c device_api.c wish_core_api.c jni_app_service_ipc.c wish/wish_platform.c wish/wish_debug.c $(foreach sdir,$(WISH_MODULES),$(wildcard $(sdir)/*.c))
LOCAL_C_INCLUDES := deps/mbedtls-2.1.2/include deps/cBSON wish deps/ed25519/src wish_rpc wish_app port/unix deps/bson/ deps/uthash
LOCAL_LDLIBS := -llog
LOCAL_CFLAGS := -O2 -Wall -Wno-pointer-sign -Werror -fvisibility=hidden

#Put each function in own section, so that linker can discard unused code
LOCAL_CFLAGS += -ffunction-sections -fdata-sections 
#instruct linker to discard unsused code:
LOCAL_LDFLAGS += -Wl,--gc-sections

NDK_LIBS_OUT=jniLibs

include $(BUILD_SHARED_LIBRARY)
