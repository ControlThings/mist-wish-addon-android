LOCAL_PATH := $(call my-dir)
 
include $(CLEAR_VARS)
 
# Here we give our module name and source file(s)


WISH_MODULES = deps/mbedtls-2.1.2/library deps/cBSON wish_app wish_rpc deps/ed25519/src deps/bson mist

WISH_SRC := 
LOCAL_MODULE    := mist
LOCAL_SRC_FILES := wish_bridge_jni.c jni_utils.c wish_core_api.c mist_node_api_jni.c wish_app_deps/wish_platform.c wish_app_deps/wish_debug.c wish_app_deps/wish_utils.c $(foreach sdir,$(WISH_MODULES),$(wildcard $(sdir)/*.c))
LOCAL_C_INCLUDES := deps/mbedtls-2.1.2/include deps/cBSON wish deps/ed25519/src wish_rpc wish_app wish_app_deps port/unix deps/bson/ deps/uthash/include mist
LOCAL_LDLIBS := -llog
LOCAL_CFLAGS := -O2 -Wall -Wno-pointer-sign -Werror -fvisibility=hidden -Wno-unused-variable

#Put each function in own section, so that linker can discard unused code
LOCAL_CFLAGS += -ffunction-sections -fdata-sections 
#instruct linker to discard unsused code:
LOCAL_LDFLAGS += -Wl,--gc-sections

NDK_LIBS_OUT=jniLibs

include $(BUILD_SHARED_LIBRARY)
