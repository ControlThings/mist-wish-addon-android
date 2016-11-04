//
// Created by jan on 6/16/16.
//

#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>
#include <stdlib.h>
#include <stdio.h>

/* To Re-generate device_api.h JNI linkage file from method definitions in DeviceApiJni:
 * javah -classpath ../../../build/intermediates/classes/debug:/home/jan/Android/Sdk/platforms/android-16/android.jar -o device_api.h fi.ct.mist.api.device.DeviceApiJni
 */
#include "device_api.h"
#include "android_mist.h"
#include "device_app_interface.h"
#include "jni_utils.h"
#include "cbson.h"
#include "wish_app.h"

static JavaVM *javaVM;
static jobject deviceApiJniInstance;

extern wish_app_t *app; /* created in start_mist_app(), initialised as NULL in android_mist.c.  */


JNIEXPORT void JNICALL Java_fi_ct_mist_referencelibrary_api_node_NodeApiJni_register(JNIEnv *env, jobject jthis) {
    android_wish_printf("in register()");

    /* Register a refence to the JVM */
    if ((*env)->GetJavaVM(env,&javaVM) < 0) {
        android_wish_printf("Failed to GetJavaVM");
        return;
    }

    /* Create a global reference to the DeviceApiJni instance here */
    deviceApiJniInstance = (*env)->NewGlobalRef(env, jthis);
    if (deviceApiJniInstance == NULL) {
        android_wish_printf("Out of memory!");
        return;
    }

}

/*
 * This function is called by onRead, onWrite of the DeviceApiJni, when they want to send away their result
 *
 * Class:     fi_ct_mist_api_device_DeviceApiJni
 * Method:    result
 * Signature: ([B[B)V
 */
JNIEXPORT void JNICALL Java_fi_ct_mist_referencelibrary_api_node_NodeApiJni_result(JNIEnv *env, jobject jthis, jbyteArray java_buffer, jbyteArray java_peer) {
    android_wish_printf("in result");

    Java_fi_ct_mist_api_mist_MistApiJni_receive_1mist_1api(env, jthis, java_peer, java_buffer);

#if 0
    size_t buffer_length = (*env)->GetArrayLength(env, java_buffer);
    uint8_t *buffer = (uint8_t *) calloc(buffer_length, 1);
    if (buffer == NULL) {
        android_wish_printf("Malloc fails");
        return;
    }
    (*env)->GetByteArrayRegion(env, java_buffer, 0, buffer_length, buffer);

    size_t peer_length = (*env)->GetArrayLength(env, java_peer);
    uint8_t *peer = (uint8_t *) calloc(peer_length, 1);
    if (peer == NULL) {
        android_wish_printf("Malloc fails");
        return;
    }
    (*env)->GetByteArrayRegion(env, java_peer, 0, peer_length, peer);

    android_wish_printf("Calling wish app send!");
    if (wish_app_send(app, peer, peer_length, buffer, buffer_length)) {
        android_wish_printf("Wish app send fail");
    }

    free(buffer);
    free(peer);
#endif
}

/**
 * This function is called by android_mist.c:jni_on_frame, when a frame containing an "op" element is received to Mist.
 * This function will just forward the frame and peer documents over JNI to class DeviceApiJni.
 *
 * Note that the JNI method register() must be called before this.
 */
void device_on_frame(void *app_ctx, uint8_t *data, size_t data_len, uint8_t *peer_doc) {
    android_wish_printf("in device on frame");

    bool did_attach = false;
    JNIEnv * my_env = NULL;
    if (getJNIEnv(javaVM, &my_env, &did_attach)) {
        android_wish_printf("Method invocation failure, could not get JNI env");
        return;
    }

    /* Transform the peer_info_doc to java byte[] */
    int32_t peer_doc_len = bson_get_doc_len(peer_doc);
    jbyteArray java_peer_doc = (*my_env)->NewByteArray(my_env, peer_doc_len);
    (*my_env)->SetByteArrayRegion(my_env, java_peer_doc, 0, peer_doc_len, (const jbyte *) peer_doc);


    /* Transform the data document to java byte[] */
    jbyteArray java_buffer = (*my_env)->NewByteArray(my_env, data_len );
    (*my_env)->SetByteArrayRegion(my_env, java_buffer, 0, data_len, (const jbyte *) data);

    if (deviceApiJniInstance == NULL) {
        android_wish_printf("Method invocation failure, DeviceApiJni instance is null.");
        return;
    }

    jclass deviceApiJniClass = (*my_env)->GetObjectClass(my_env, deviceApiJniInstance);
    jmethodID deviceOnFrameMethodId = (*my_env)->GetMethodID(my_env, deviceApiJniClass, "deviceOnFrame", "([B[B)V");
    if (deviceOnFrameMethodId == NULL) {
        android_wish_printf("Method cannot be found");
        return;
    }
    /* Actually call the deviceOnFrame() method in DeviceApiJni */
    (*my_env)->CallVoidMethod(my_env, deviceApiJniInstance, deviceOnFrameMethodId, java_buffer, java_peer_doc);

    if (did_attach) {
        detachThread(javaVM);
    }

}

