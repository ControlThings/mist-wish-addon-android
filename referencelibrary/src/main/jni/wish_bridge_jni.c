//
// Created by jan on 6/7/16.
//
#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>
#include <stdlib.h>
#include <stdio.h>

#include "wish_app.h"
#include "app_service_ipc.h"

/* To re-generate the JNI Header file: javah -classpath ../../../../referencelibrary/build/intermediates/classes/debug/:/home/jan/Android/Sdk/platforms/android-16/android.jar -o wish_bridge_jni.h fi.ct.mist.referencelibrary.WishBridgeJni */
#include "wish_bridge_jni.h"
#include "jni_utils.h"
#include "mist_node_api_helper.h"

static JavaVM *javaVM;

static jobject wishAppBridgeInstance;

/*
* Class:     fi_ct_mist_WishAppJni
* Method:    register
* Signature: (Lfi/ct/mist/WishAppBridge;)V
*/
JNIEXPORT void JNICALL Java_fi_ct_mist_referencelibrary_WishBridgeJni_register(JNIEnv *env, jobject jthis, jobject wishAppBridge) {
    /* Register a refence to the JVM */
    if ((*env)->GetJavaVM(env,&javaVM) < 0) {
        android_wish_printf("Failed to GetJavaVM");
        return;
    }

    /* Create a global reference to the WishAppBridge instance here */
    wishAppBridgeInstance = (*env)->NewGlobalRef(env, wishAppBridge);
    if (wishAppBridgeInstance == NULL) {
        android_wish_printf("Out of memory!");
        return;
    }

}

uint8_t my_wsid[WISH_WSID_LEN];

/** Implementation of send_app_to_core.
 * It will transform the arguments into Java objects and calls receiveAppToCore defined in
 */
void send_app_to_core(uint8_t *wsid, uint8_t *data, size_t len) {
    android_wish_printf("Send app to core");

    memcpy(my_wsid, wsid, WISH_WSID_LEN);

    /* This will be set to true, if the thread of execution was not a JavaVM thread */
    bool did_attach = false;

    JNIEnv * my_env = NULL;
    if (getJNIEnv(javaVM, &my_env, &did_attach)) {
        android_wish_printf("Method invocation failure, could not get JNI env");
        return;
    }

    /* For method signature strings, see:
     * http://docs.oracle.com/javase/1.5.0/docs/guide/jni/spec/types.html#wp16432 */
    jclass wishAppBridgeClass = (*my_env)->GetObjectClass(my_env, wishAppBridgeInstance);
    jmethodID receiveAppToCoreMethodId = (*my_env)->GetMethodID(my_env, wishAppBridgeClass, "receiveAppToCore", "([B[B)V");
    if (receiveAppToCoreMethodId == NULL) {
        android_wish_printf("Method cannot be found");
        return;
    }

    /* See JNI spec: http://docs.oracle.com/javase/6/docs/technotes/guides/jni/spec/functions.html#array_operations */
    jbyteArray java_buffer = (*my_env)->NewByteArray(my_env, len);
    jbyteArray java_wsid = (*my_env)->NewByteArray(my_env, WISH_WSID_LEN);
    /* See http://docs.oracle.com/javase/1.5.0/docs/guide/jni/spec/types.html for information on
     * how the "C" char and "JNI" jchar are the same thing! */
    (*my_env)->SetByteArrayRegion(my_env, java_buffer, 0, len, (const jbyte *) data);
    (*my_env)->SetByteArrayRegion(my_env, java_wsid, 0, WISH_WSID_LEN, (const jbyte *) wsid);

    (*my_env)->CallVoidMethod(my_env, wishAppBridgeInstance, receiveAppToCoreMethodId, java_wsid, java_buffer);

    if (did_attach) {
        detachThread(javaVM);
    }

    (*my_env)->DeleteLocalRef(my_env, java_buffer);
    (*my_env)->DeleteLocalRef(my_env, java_wsid);
}

JNIEXPORT void JNICALL Java_fi_ct_mist_referencelibrary_WishBridgeJni_receive_1core_1to_1app(JNIEnv *env, jobject jthis, jbyteArray java_data) {
    android_wish_printf("Receive core to app");

    size_t data_length = (*env)->GetArrayLength(env, java_data);
    uint8_t *data = (uint8_t *) calloc(data_length, 1);
    if (data == NULL) {
        android_wish_printf("Malloc fails");
        return;
    }
    (*env)->GetByteArrayRegion(env, java_data, 0, data_length, data);
    wish_app_t *dst_app = wish_app_find_by_wsid(my_wsid);
    if (dst_app != NULL) {
        android_wish_printf("Found app by wsid");
        wish_app_determine_handler(dst_app, data, data_length);
    }
    else {
        android_wish_printf("Cannot find app by wsid");
    }

    free(data);
}

/*
 * Class:     fi_ct_mist_referencelibrary_WishBridgeJni
 * Method:    connected
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_fi_ct_mist_referencelibrary_WishBridgeJni_connected
  (JNIEnv *env, jobject jthis, jboolean connected) {
    /* Call wish_app_login on our app */
    wish_app_connected(get_mist_node_app(), connected);

}