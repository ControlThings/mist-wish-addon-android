//
// Created by jan on 8/18/16.
//

/* This file implements the JNI functions defined in WishCoreApiJni.java
 * /
 *
 * The headerfile can be regenerated like this:
 * javah -classpath ../../../build/intermediates/classes/debug:/home/jan/Android/Sdk/platforms/android-16/android.jar -o wish_core_api.h fi.ct.mist.api.wish.WishCoreApiJni
 */

#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>
#include <stdlib.h>
#include <stdio.h>

#include "wish_app.h"
#include "wish_core_api.h"
#include "app_service_ipc.h"
#include "bson.h"
#include "cbson.h"

#include "jni_utils.h"
#include "mist_node_api_helper.h"


extern wish_app_t *app; /* Created by start_mist_app in android_mist.c */

static JavaVM * javaVM;
static jobject wishApiJniInstance;

/*
 * Class:     fi_ct_mist_api_wish_WishCoreApiJni
 * Method:    register
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_fi_ct_mist_api_wish_WishCoreApiJni_register
        (JNIEnv *env, jobject jthis) {
    /* Register a global reference the WishCoreApiJni instance so we can call functions its methods from C */

    wishApiJniInstance = (*env)->NewGlobalRef(env, jthis);
    if (wishApiJniInstance == NULL) {
        android_wish_printf("Out of memory!");
        return;
    }

    /* Register a refence to the JVM */
    if ((*env)->GetJavaVM(env,&javaVM) < 0) {
        android_wish_printf("Failed to GetJavaVM");
        return;
    }




    /* FIXME deregister function for removing the global reference! */
}

/*
 * Class:     fi_ct_mist_api_wish_WishCoreApiJni
 * Method:    receive_wish_api
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_fi_ct_mist_api_wish_WishCoreApiJni_receive_1wish_1api
        (JNIEnv *env, jobject jthis, jbyteArray java_buffer) {
    android_wish_printf("in receive wish api");

#if 0
#error
    if (app == NULL) {
        android_wish_printf("app is NULL, failing");
        return;
    }

    size_t buffer_length = (*env)->GetArrayLength(env, java_buffer);
    uint8_t *buffer = (uint8_t *) calloc(buffer_length, 1);
    if (buffer == NULL) {
        android_wish_printf("Malloc fails");
        return;
    }
    (*env)->GetByteArrayRegion(env, java_buffer, 0, buffer_length, buffer);

    android_wish_printf("Calling wish app send!");
    send_app_to_core(app->wsid, buffer, buffer_length);

    free(buffer);
#endif
    android_wish_printf("returning from receive wish api");
}

/*
 * This function is called by wish_app_determine_handler() when it receives a reply from the local core (in that case there is no 'type' element in message)
 *
 * This function will forward the response from the core using receiveWishApi in WishCoreJni
 */
void send_wish_api(uint8_t *buffer, size_t buffer_len) {
    android_wish_printf("in send wish api");

#if 0
    bool did_attach = false;
    JNIEnv * my_env = NULL;
    if (getJNIEnv(javaVM, &my_env, &did_attach)) {
        android_wish_printf("Method invocation failure, could not get JNI env");
        return;
    }

    android_wish_printf("Length of doc in buffer: %i", bson_get_doc_len(buffer));

    /* Transform the peer_info_doc to java byte[] */
    jbyteArray java_buffer = (*my_env)->NewByteArray(my_env, buffer_len);
    (*my_env)->SetByteArrayRegion(my_env, java_buffer, 0, buffer_len, (const jbyte *) buffer);


    /* Call receiveWishApi */
    jclass wishApiJniClass = (*my_env)->GetObjectClass(my_env, wishApiJniInstance);
    jmethodID receiveWishApiMethodId = (*my_env)->GetMethodID(my_env, wishApiJniClass, "receiveWishApi", "([B)V");
    if (receiveWishApiMethodId == NULL) {
        android_wish_printf("Method cannot be found");
        return;
    }

    android_wish_printf("Calling receiveWishApi");
    (*my_env)->CallVoidMethod(my_env, wishApiJniInstance, receiveWishApiMethodId, java_buffer);
    (*my_env)->DeleteLocalRef(my_env, java_buffer);
#endif

}
