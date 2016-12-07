//
// Created by jan on 6/7/16.
//

/* To generate android-mist.h:
 * javah -classpath ../../../build/intermediates/classes/debug:/home/jan/Android/Sdk/platforms/android-16/android.jar -o android_wish.h fi.ct.wish.os.WishOsJni
 */

#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>
#include <stdlib.h>
#include <stdio.h>

#include "android_mist.h"
#include "wish_app.h"
#include "wish_protocol.h"
#include "jni_utils.h"
#include "cbson.h"
#include "bson_visitor.h"
#include "wish_debug.h"
#include "wish_platform.h"
#include "device_app_interface.h"

static JavaVM * javaVM;
static jobject mistApiJniInstance;

wish_app_t *app = NULL; /* created in start_mist_app() */

int jni_online(void *app_ctx, uint8_t *peer_info_doc) {


    android_wish_printf("in jni online");
    bson_visit(peer_info_doc, elem_visitor);

    bool did_attach = false;
    JNIEnv * my_env = NULL;
    if (getJNIEnv(javaVM, &my_env, &did_attach)) {
        android_wish_printf("Method invocation failure, could not get JNI env");
        return 1;
    }

    size_t peer_info_doc_len = bson_get_doc_len(peer_info_doc);

    /* Transform the peer_info_doc to java byte[] */
    jbyteArray java_buffer = (*my_env)->NewByteArray(my_env, peer_info_doc_len );
    (*my_env)->SetByteArrayRegion(my_env, java_buffer, 0, peer_info_doc_len, (const jbyte *) peer_info_doc);

    /* Call MistApiJni.online with the java byte[] */
    jclass mistApiJniClass = (*my_env)->GetObjectClass(my_env, mistApiJniInstance);
    jmethodID onlineMethodId = (*my_env)->GetMethodID(my_env, mistApiJniClass, "online", "([B)V");
    if (onlineMethodId == NULL) {
        android_wish_printf("Method cannot be found");
        return 1;
    }

    (*my_env)->CallVoidMethod(my_env, mistApiJniInstance, onlineMethodId, java_buffer);
    (*my_env)->DeleteLocalRef(my_env, java_buffer);

    if (did_attach) {
        detachThread(javaVM);
    }

    return 0;
}

static int jni_on_frame(void *app_ctx, uint8_t *data, size_t data_len, uint8_t *peer_doc) {
    android_wish_printf("ucp frame received");

    char *op_str = NULL;
    int32_t op_str_len = 0;
    if (bson_get_string(data, "op", &op_str, &op_str_len) == BSON_SUCCESS) {
        android_wish_printf("ucp control.something request received");
        device_on_frame(app_ctx, data, data_len,  peer_doc);
    }
    else {
        /* We have received something that is most probably a reply to a RPC command. Call "receiveMistApi" on in MistApiJni */
        android_wish_printf("ucp something else receive, maybe an data + ack to an RPC we sent?");
        bool did_attach = false;
        JNIEnv * my_env = NULL;
        if (getJNIEnv(javaVM, &my_env, &did_attach)) {
            android_wish_printf("Method invocation failure, could not get JNI env");
            return 0;
        }

        /* Transform the peer_info_doc to java byte[] */
        int32_t peer_doc_len = bson_get_doc_len(peer_doc);
        jbyteArray java_peer_doc = (*my_env)->NewByteArray(my_env, peer_doc_len);
        (*my_env)->SetByteArrayRegion(my_env, java_peer_doc, 0, peer_doc_len, (const jbyte *) peer_doc);


        /* Transform the data document to java byte[] */
        jbyteArray java_buffer = (*my_env)->NewByteArray(my_env, data_len );
        (*my_env)->SetByteArrayRegion(my_env, java_buffer, 0, data_len, (const jbyte *) data);

        if (mistApiJniInstance == NULL) {
            android_wish_printf("mistApiJniInstance is not registered yet!");
            return 0;
        }
        jclass mistApiJniClass = (*my_env)->GetObjectClass(my_env, mistApiJniInstance);
        jmethodID receiveMistApiMethodId = (*my_env)->GetMethodID(my_env, mistApiJniClass, "receiveMistApi", "([B)V");
        if (receiveMistApiMethodId == NULL) {
            android_wish_printf("Method cannot be found");
            return 0;
        }
        android_wish_printf("Calling receiveMistApi");
        (*my_env)->CallVoidMethod(my_env, mistApiJniInstance, receiveMistApiMethodId, java_buffer);

        if (did_attach) {
            detachThread(javaVM);
        }

    }
    return 0;
}


wish_protocol_handler_t ucp_handler = {
        .protocol_name = "ucp",
        .on_online = jni_online,
        .on_frame = jni_on_frame,
        /* TODO Register callbacks for frame, peer, etc. */

};

wish_permission_t permissions[2] = { "identity.list", "core.morjens" };

/*
 * Class:     fi_ct_mist_api_mist_MistApiJni
 * Method:    start_mist_app
 * Signature: (Ljava/lang/String;Lfi/ct/mist/api/mist/MistApi;)V
 */
JNIEXPORT void JNICALL Java_fi_ct_mist_api_mist_MistApiJni_start_1mist_1app(JNIEnv *env, jobject jthis, jstring jHumanReadableName) {


    android_wish_printf("Start mist app called");


    /* Register a refence to the JVM */
    if ((*env)->GetJavaVM(env,&javaVM) < 0) {
        android_wish_printf("Failed to GetJavaVM");
        return;
    }

    /* Create a global reference to the MistApiJni instance here */
    mistApiJniInstance = (*env)->NewGlobalRef(env, jthis);
    if (mistApiJniInstance == NULL) {
        android_wish_printf("Out of memory!");
        return;
    }


    /* Register the platform dependencies with Wish/Mist */

    wish_platform_set_malloc(malloc);
    wish_platform_set_free(free);
    wish_platform_set_rng(random);
    wish_platform_set_vprintf(android_wish_vprintf);
    wish_platform_set_vsprintf(vsprintf);


    char *name_str =  (char*) (*env)->GetStringUTFChars(env, jHumanReadableName, NULL);






    if (app != NULL) {
        android_wish_printf("Recycling existing wish app");
    }
    else {
        app = wish_app_create(name_str);
        //, &ucp_handler, 1, permissions, 2
        wish_app_add_protocol(app, &ucp_handler);
        wish_app_login(app);
    }
    if (app == NULL) {
        android_wish_printf("Failed creating wish app");
        return;
    }


    (*env)->ReleaseStringUTFChars(env, jHumanReadableName, name_str);
}

/*
 * Class:     fi_ct_mist_api_mist_MistApiJni
 * Method:    destroy_mist_app
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_fi_ct_mist_api_mist_MistApiJni_destroy_1mist_1app
        (JNIEnv *env, jobject java_this) {
    android_wish_printf("Destroy Mist App called");
    (*env)->DeleteGlobalRef(env, mistApiJniInstance);
}


JNIEXPORT void JNICALL Java_fi_ct_mist_api_mist_MistApiJni_receive_1mist_1api(JNIEnv *env, jobject jthis, jbyteArray java_peer, jbyteArray java_buffer) {
    android_wish_printf("in receive mist api");
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
    if (wish_app_send(app, peer, peer_length, buffer, buffer_length, NULL) == -1) {
        android_wish_printf("Wish app send fail");
    }

    free(buffer);
    free(peer);
}

