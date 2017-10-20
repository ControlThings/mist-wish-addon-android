//
// Created by jan on 11/3/16.
//

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "mist_app.h"
#include "mist_model.h"
#include "mist_follow.h"

/*
To re-create JNI interface:

 1. Renew header file using javah
      javah -classpath ../../../../mistnodeapi/build/intermediates/classes/debug/:/home/jan/Android/Sdk/platforms/android-16/android.jar -o mist_node_api_jni.h mist.node.MistNodeApi
 2. Fix .c file function names to correspond to new .h file
 3. Fix all calls to java from c
      from:
        jfieldID invokeCallbackField = (*env)->GetFieldID(env, endpointClass, "invokeCallback", "Lfi/ct/mist/nodeApi/Endpoint$Invokable;");
      to:
        jfieldID invokeCallbackField = (*env)->GetFieldID(env, endpointClass, "invokeCallback", "Lmist/node/Endpoint$Invokable;");

*/
#include "mist_node_api_jni.h"
#include "jni_utils.h"
#include "mist_node_api_helper.h"
#include "wish_fs.h"

#include "utlist.h"
#include "filesystem.h"


static JavaVM *javaVM;
static mist_model *model;
static wish_app_t *app;
static jobject mistNodeInstance;

struct endpoint_data {
    mist_ep *ep;
    char *epid; /* The "fullpath id" */
    jobject endpoint_object;
    struct endpoint_data *next;
};

static struct endpoint_data *endpoint_head = NULL;

static struct endpoint_data *lookup_ep_data_by_epid(char *epid) {
    struct endpoint_data *ep_data = NULL;
    LL_FOREACH(endpoint_head, ep_data) {
        if (strcmp(ep_data->epid, epid) == 0) {
            return ep_data;
        }
    }
    return NULL;
}

static jmethodID get_methodID(JNIEnv *env, jobject instance, char *method_name, char* signature) {

    if (instance == NULL) {
        android_wish_printf("get_methodID, instance is NULL!");
        return NULL;
    }

    android_wish_printf("in get_methodid 1");

    jclass clazz = (*env)->GetObjectClass(env, instance);
    android_wish_printf("in get_methodid 2 %x", clazz);
    jmethodID method_id = (*env)->GetMethodID(env, clazz, method_name, signature);
    if (method_id == NULL) {
        android_wish_printf("get_methodID: Cannot get method %s with signature %s", method_name, signature);
        return NULL;
    }

android_wish_printf("in get_methodid ret");
    return method_id;
}

static enum mist_error hw_read(mist_ep *ep,  wish_protocol_peer_t* peer, int request_id) {
    enum mist_error retval = MIST_ERROR;
    android_wish_printf("in hw_read, ep %s", ep->id);

    JNIEnv * env = NULL;
    bool did_attach = false;

    if (javaVM == NULL) {
        android_wish_printf("in hw_read, javaVM is NULL!");
        return MIST_ERROR;
    }

    if (getJNIEnv(javaVM, &env, &did_attach)) {
        android_wish_printf("Method invocation failure, could not get JNI env");
        return MIST_ERROR;
    }

    /* Invoke method "MistNode.read", signature (Lmist/node/Endpoint;[BI)V */
    if (mistNodeInstance == NULL) {
        android_wish_printf("in hw_read, ep %s: mistNodeApiInstance is NULL!");
        return MIST_ERROR;
    }

    jclass mistNodeClass = (*env)->GetObjectClass(env, mistNodeInstance);
    jmethodID readMethodId = (*env)->GetMethodID(env, mistNodeClass, "read", "(Lmist/node/Endpoint;[BI)V");
    if (readMethodId == NULL) {
        android_wish_printf("Cannot get read method");
        return MIST_ERROR;
    }

    char epid[MIST_EPID_LEN] = { 0 };
    mist_ep_full_epid(ep, epid);
    struct endpoint_data *ep_data = lookup_ep_data_by_epid(epid);
    if (ep_data == NULL) {
        android_wish_printf("in hw_read, could not find ep_data for %s!", epid);
        return MIST_ERROR;
    }

    android_wish_printf("Serialise peer to bson %x request_id %i", peer, request_id);
    /* Serialise peer to BSON */
    if (peer != NULL) {
        bson bs;
        bson_init(&bs);
        bson_append_peer(&bs, NULL, peer);
        bson_finish(&bs);

        android_wish_printf("Create peer array");
        jbyteArray java_peer = (*env)->NewByteArray(env, bson_size(&bs));
        if (java_peer == NULL) {
            android_wish_printf("Failed creating peer byte array");
            return MIST_ERROR;
        }
        (*env)->SetByteArrayRegion(env, java_peer, 0, bson_size(&bs), (const jbyte *) bson_data(&bs));
        android_wish_printf("Call method");
        (*env)->CallVoidMethod(env, mistNodeInstance, readMethodId, ep_data->endpoint_object, java_peer, request_id);
        (*env)->DeleteLocalRef(env, java_peer);
        bson_destroy(&bs);
    }
    else {
        /* peer is null, call the read method with null peer excplicitly */
        jbyteArray java_peer = (*env)->NewByteArray(env, 0);
        (*env)->CallVoidMethod(env, mistNodeInstance, readMethodId, ep_data->endpoint_object, java_peer, request_id);
    }

    if (did_attach) {
        detachThread(javaVM);
    }

    android_wish_printf("Exit");
    return retval;
}

static enum mist_error hw_write(mist_ep *ep, wish_protocol_peer_t* peer, int request_id, bson* args) {
    enum mist_error retval = MIST_ERROR;
    android_wish_printf("in hw_write");


    JNIEnv *env = NULL;
    bool did_attach = false;

    if (javaVM == NULL) {
        android_wish_printf("hw_write, javaVM is NULL!");
        return MIST_ERROR;
    }

android_wish_printf("in hw_write 1");
    if (getJNIEnv(javaVM, &env, &did_attach)) {
        android_wish_printf("Method invocation failure, could not get JNI env");
        return MIST_ERROR;
    }
    android_wish_printf("in hw_write 1a");

    /* Invoke method "write", signature (Lmist/node/Endpoint;[BI[B)V */
    jmethodID write_method_id = get_methodID(env, mistNodeInstance, "write", "(Lmist/node/Endpoint;[BI[B)V");
    if (write_method_id == NULL) {
        android_wish_printf("hw_write, Cannot find write method!");
        return MIST_ERROR;
    }
    android_wish_printf("in hw_write 2");

    char epid[MIST_EPID_LEN] = { 0 };
    mist_ep_full_epid(ep, epid);
    struct endpoint_data *ep_data = lookup_ep_data_by_epid(epid);
    if (ep_data == NULL) {
        android_wish_printf("in hw_read, could not find ep_data for %s!", epid);
        return MIST_ERROR;
    }

android_wish_printf("in hw_write 3");
    bson bs;
    bson_init(&bs);
    /* Serialise peer to BSON */
    if (peer != NULL) {
        bson_append_peer(&bs, NULL, peer);
    }
    bson_finish(&bs);

    android_wish_printf("Create peer array");
    jbyteArray java_peer = (*env)->NewByteArray(env, bson_size(&bs));
    if (java_peer == NULL) {
        android_wish_printf("Failed creating peer byte array");
        return MIST_ERROR;
    }
    if (bson_size(&bs) > 0) {
        (*env)->SetByteArrayRegion(env, java_peer, 0, bson_size(&bs), (const jbyte *) bson_data(&bs));
    }

    android_wish_printf("Create args array");
    jbyteArray java_args = NULL;
    if (args != NULL) {
        java_args = (*env)->NewByteArray(env, bson_size(args));
    }
    else {
        java_args = (*env)->NewByteArray(env, 0);
    }

    if (java_args == NULL) {
        android_wish_printf("Failed creating args byte array");
        return MIST_ERROR;
    }

    if (args != NULL) {
        (*env)->SetByteArrayRegion(env, java_args, 0, bson_size(args), (const jbyte *) bson_data(args));
    }

    android_wish_printf("Call method");
    (*env)->CallVoidMethod(env, mistNodeInstance, write_method_id, ep_data->endpoint_object, java_peer, request_id, java_args);
    (*env)->DeleteLocalRef(env, java_peer);
    (*env)->DeleteLocalRef(env, java_args);

    if (did_attach) {
        detachThread(javaVM);
    }
    bson_destroy(&bs);

    return retval;
}

static enum mist_error hw_invoke(mist_ep *ep, wish_protocol_peer_t* peer, int id, bson* args) {
    enum mist_error retval = MIST_ERROR;
    android_wish_printf("in hw_invoke (NOT IMPLEMENTED)");

    return retval;
}

/* Note: Caller is responsible for releasing the pointer returned from this function! Use free(). */
char *get_string_from_obj_field(JNIEnv *env, jobject java_obj, char *field_name) {
    android_wish_printf("in get_string_from_obj_field");
    jclass java_class = (*env)->GetObjectClass(env, java_obj);
    if (java_class == NULL) {
        android_wish_printf("Could not resolve endpoint class while getting value for field name %s", field_name);
        return NULL;
    }

android_wish_printf("in get_string_from_obj_field: 1");
    /* Get "id" field, a String */
    jfieldID field_id = (*env)->GetFieldID(env, java_class, field_name, "Ljava/lang/String;");
    if (field_id == NULL) {
        android_wish_printf("Could not get field id for %s", field_name);
        return NULL;
    }

    android_wish_printf("in get_string_from_obj_field: 2");
    jobject java_string = (*env)->GetObjectField(env, java_obj, field_id);
    if (java_string == NULL) {
        android_wish_printf("Could not get String for %s", field_name);
        return NULL;
    }
    android_wish_printf("in get_string_from_obj_field: 3");
    char *str =  (char*) (*env)->GetStringUTFChars(env, java_string, NULL);
    if (str == NULL) {
        android_wish_printf("Could not GetStringUTF of field %s", field_name);
        return NULL;
    }
android_wish_printf("in get_string_from_obj_field: 4");
    char *copy = strdup(str);
    (*env)->ReleaseStringUTFChars(env, java_string, str);

    return copy;
}

/*
 * Class:     mist_node_MistNode
 * Method:    startMistApp
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNode_startMistApp(JNIEnv *env, jobject java_this, jstring java_appName) {

    android_wish_printf("in startMistApp");
    /* Register a refence to the JVM */
    if ((*env)->GetJavaVM(env,&javaVM) < 0) {
        android_wish_printf("Failed to GetJavaVM");
        return;
    }

    /* Register the platform dependencies with Wish/Mist */

    wish_platform_set_malloc(malloc);
    wish_platform_set_realloc(realloc);
    wish_platform_set_free(free);
    srandom(time(NULL));
    wish_platform_set_rng(random);
    wish_platform_set_vprintf(android_wish_vprintf);
    wish_platform_set_vsprintf(vsprintf);

    /* File system functions are needed for Mist mappings! */
    wish_fs_set_open(my_fs_open);
    wish_fs_set_read(my_fs_read);
    wish_fs_set_write(my_fs_write);
    wish_fs_set_lseek(my_fs_lseek);
    wish_fs_set_close(my_fs_close);
    wish_fs_set_rename(my_fs_rename);
    wish_fs_set_remove(my_fs_remove);

    mistNodeInstance = (*env)->NewGlobalRef(env, java_this);

    mist_app_t *mist_app = start_mist_app();
    if (mist_app == NULL) {
        WISHDEBUG(LOG_CRITICAL, "Failed creating mist app!");
    }

    model = &mist_app->model;
    char *name_str =  (char*) (*env)->GetStringUTFChars(env, java_appName, NULL);
    app = wish_app_create(name_str);
    if (app == NULL) {
        WISHDEBUG(LOG_CRITICAL, "Failed creating wish app!");
    }
    wish_app_add_protocol(app, &mist_app->protocol);
    mist_app->app = app;
    (*env)->ReleaseStringUTFChars(env, java_appName, name_str);

    /* The app will login to core when the Bridge connects, this happens via the wish_app_connected(wish_app_t *app, bool connected) function */
}

/*
 * Class:     mist_node_MistNode
 * Method:    stopMistApp
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNode_stopMistApp(JNIEnv *env, jobject java_this) {
    android_wish_printf("stopMistApp not implemented!");
}


/*
 * Class:     mist_node_MistNode
 * Method:    addEndpoint
 * Signature: (Lmist/node/Endpoint;)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNode_addEndpoint(JNIEnv *env, jobject java_this, jobject java_Endpoint) {
    android_wish_printf("in addEndpoint");

    /* Get the class corresponding to java_Endpoint instance */
    jclass endpointClass = (*env)->GetObjectClass(env, java_Endpoint);
    if (endpointClass == NULL) {
        android_wish_printf("addEndpoint: Could not resolve endpoint class.");
        return;
    }

    android_wish_printf("in addEndpoint 1");

    char *epid_str = get_string_from_obj_field(env, java_Endpoint, "epid");

    if (epid_str == NULL) {
        android_wish_printf("addEndpoint: epid is null!");
        return;
    }
    android_wish_printf("in addEndpoint 2");

    /* Get "label" field, a String */

    char *label_str = get_string_from_obj_field(env, java_Endpoint, "label");

android_wish_printf("in addEndpoint 3");

    /* Get "type" field, an int */
    jfieldID typeField = (*env)->GetFieldID(env, endpointClass, "type", "I");
    jint type = (*env)->GetIntField(env, java_Endpoint, typeField);

   android_wish_printf("in addEndpoint 3a");
    /* Get "unit" field, a String */

    char* unit_str = get_string_from_obj_field(env, java_Endpoint, "unit");
    android_wish_printf("in addEndpoint 4");

    char *id_str = get_string_from_obj_field(env, java_Endpoint, "id");
    char *parent_epid_str = get_string_from_obj_field(env, java_Endpoint, "parent");

    android_wish_printf("in addEndpoint 5");

    android_wish_printf("addEndpoint, epid: %s, id: %s, label: %s, parent full path: %s, type: %i, unit: %s", epid_str, id_str, label_str, parent_epid_str, type, unit_str);

    struct endpoint_data *ep_data = wish_platform_malloc(sizeof (struct endpoint_data));
    if (ep_data == NULL) {
        android_wish_printf("Could allocate memory for endpoint callbacks");
        return;
    }
    memset(ep_data, 0, sizeof (struct endpoint_data));

    ep_data->endpoint_object = (*env)->NewGlobalRef(env, java_Endpoint);
    ep_data->epid = strdup(epid_str);


    /* Get readable boolean */
    jfieldID readableField = (*env)->GetFieldID(env, endpointClass, "readable", "Z");
    jboolean readable = (*env)->GetBooleanField(env, java_Endpoint, readableField);

    jfieldID writableField = (*env)->GetFieldID(env, endpointClass, "writable", "Z");
    jboolean writable = (*env)->GetBooleanField(env, java_Endpoint, writableField);

    jfieldID invokableField = (*env)->GetFieldID(env, endpointClass, "invokable", "Z");
    jboolean invokable = (*env)->GetBooleanField(env, java_Endpoint, invokableField);

    /* Actually add the endpoint to mist lib */

    // allocate a new endpoint and space for data
    mist_ep* ep = (mist_ep*) malloc(sizeof(mist_ep));
    if (ep == NULL) {
        android_wish_printf("Out of memory when allocating mist_ep");
        return;
    }
    memset(ep, 0, sizeof(mist_ep));

    ep->id = id_str; // Note: this must be free'ed when removing endpoint!
    ep->label = label_str; // Note: this must be free'ed when removing endpoint!
    ep->type = type;
    ep_data->ep = ep;

    if (readable) { ep->read = hw_read; }
    if (writable) { ep->write = hw_write; }
    if (invokable) { ep->invoke = hw_invoke; } //hw_invoke_function;

    ep->unit = "";
    ep->next = NULL;
    ep->prev = NULL;
    ep->dirty = false;
    ep->scaling = "";

    if (MIST_NO_ERROR != mist_ep_add(model, parent_epid_str, ep)) {
        android_wish_printf("addEndpoint returned error");
    }

    LL_APPEND(endpoint_head, ep_data);

    if (parent_epid_str != NULL) {
        free(parent_epid_str);
    }

    /* Clean up the (temporary) references we've created */

    android_wish_printf("exiting addEndpoint");
}

/*
 * Class:     mist_node_MistNode
 * Method:    removeEndpoint
 * Signature: (Lmist/node/Endpoint;)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNode_removeEndpoint(JNIEnv *env, jobject java_this, jobject java_endpoint) {

}

/*
 * Class:     mist_node_MistNode
 * Method:    readResponse
 * Signature: (Ljava/lang/String;I[B)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNode_readResponse(JNIEnv *env, jobject java_this, jstring java_epid, jint request_id, jbyteArray java_data) {
    android_wish_printf("in readResponse");

    if (java_data == NULL) {
        android_wish_printf("in readResponse: java_data is NULL");
        return;
    }

    if (java_epid == NULL) {
        android_wish_printf("in readResponse: java_epid is NULL");
        return;
    }
    size_t data_length = (*env)->GetArrayLength(env, java_data);
    uint8_t *data = (uint8_t *) calloc(data_length, 1);
    if (data == NULL) {
        android_wish_printf("Malloc fails");
        return;
    }
    (*env)->GetByteArrayRegion(env, java_data, 0, data_length, data);

    char *epid_str =  (char*) (*env)->GetStringUTFChars(env, java_epid, NULL);

    bson bs;
    bson_init_with_data(&bs, data);
    mist_read_response(model->mist_app, epid_str, request_id, &bs);

    (*env)->ReleaseStringUTFChars(env, java_epid, epid_str);
    free(data);
}

/*
 * Class:     mist_node_MistNode
 * Method:    readError
 * Signature: (Ljava/lang/String;IILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNode_readError(JNIEnv *env, jobject java_this, jstring java_epid, jint request_id, jint code, jstring java_msg) {
    android_wish_printf("in readError");

    char *epid_str =  (char*) (*env)->GetStringUTFChars(env, java_epid, NULL);
    char *msg_str = (char*) (*env)->GetStringUTFChars(env, java_msg, NULL);

    mist_read_error(model->mist_app, epid_str, request_id, code, msg_str);

    (*env)->ReleaseStringUTFChars(env, java_epid, epid_str);
    (*env)->ReleaseStringUTFChars(env, java_msg, msg_str);
}

/*
 * Class:     mist_node_MistNode
 * Method:    writeResponse
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNode_writeResponse(JNIEnv *env, jobject java_this, jstring java_epid, jint request_id) {
    android_wish_printf("in writeResponse");
    if (java_epid == NULL) {
        android_wish_printf("in writeResponse: java_epid is NULL");
        return;
    }
    char *epid_str =  (char*) (*env)->GetStringUTFChars(env, java_epid, NULL);

    mist_write_response(model->mist_app, epid_str, request_id);

    (*env)->ReleaseStringUTFChars(env, java_epid, epid_str);
}

/*
 * Class:     mist_node_MistNode
 * Method:    writeError
 * Signature: (Ljava/lang/String;IILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNode_writeError(JNIEnv *env, jobject java_this, jstring java_epid, jint request_id, jint code, jstring java_msg) {
    android_wish_printf("in writeError");
    if (java_epid == NULL) {
        android_wish_printf("in writeError: java_epid is NULL");
        return;
    }
    char *epid_str =  (char*) (*env)->GetStringUTFChars(env, java_epid, NULL);
    char *msg_str = (char*) (*env)->GetStringUTFChars(env, java_msg, NULL);

    mist_write_error(model->mist_app, epid_str, request_id, code, msg_str);

    (*env)->ReleaseStringUTFChars(env, java_epid, epid_str);
    (*env)->ReleaseStringUTFChars(env, java_msg, msg_str);
}

/*
 * Class:     mist_node_MistNode
 * Method:    invokeResponse
 * Signature: (Ljava/lang/String;I[B)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNode_invokeResponse(JNIEnv *env, jobject java_this, jstring java_epid, jint request_id, jbyteArray java_result) {

}

/*
 * Class:     mist_node_MistNode
 * Method:    invokeError
 * Signature: (Ljava/lang/String;IILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNode_invokeError(JNIEnv *env, jobject java_this, jstring java_epid, jint request_id, jint code, jstring java_msg) {

}

/*
 * Class:     mist_node_MistNode
 * Method:    changed
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNode_changed(JNIEnv *env, jobject java_this, jstring java_epid) {

}




wish_app_t *get_mist_node_app(void) {
    return app;
}

JavaVM *getJavaVM(void) {
    return javaVM;
}

jobject getMistNodeApiInstance() {
    return mistNodeInstance;
}