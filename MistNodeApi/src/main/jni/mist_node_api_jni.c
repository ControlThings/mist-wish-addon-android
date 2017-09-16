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
static struct mist_model *model;
static wish_app_t *app;
static jobject mistNodeApiInstance;




struct endpoint_data {
    char *endpoint_id;
    jobject invokable_object;
    jobject writable_object;
    struct endpoint_data *next;
};

static struct endpoint_data *endpoint_head = NULL;

static struct endpoint_data *lookup_ep_data_by_ep_id(char *ep_id) {
    struct endpoint_data *ep_data = NULL;
    LL_FOREACH(endpoint_head, ep_data) {
        if (strcmp(ep_data->endpoint_id, ep_id) == 0) {
            return ep_data;
        }
    }
    return NULL;
}

static enum mist_error hw_read(mist_ep *ep, mist_buf *result) {
    enum mist_error retval = MIST_ERROR;
    android_wish_printf("in hw_read, ep %s", ep->id);

    struct endpoint_data *ep_data = lookup_ep_data_by_ep_id(ep->id);
    if (ep_data == NULL) {
        WISHDEBUG(LOG_CRITICAL, "Could not find ep_data");
        return MIST_ERROR;
    }

    result->base = ep->data.base;
    result->len = ep->data.len;
    retval = MIST_NO_ERROR;
    android_wish_printf("exiting hw_read, ep %s", ep->id);
    return retval;
}

static enum mist_error hw_write(mist_ep *ep, mist_buf value) {
    enum mist_error retval = MIST_ERROR;
    android_wish_printf("in hw_write");

    struct endpoint_data *ep_data = lookup_ep_data_by_ep_id(ep->id);
    if (ep_data == NULL) {
        WISHDEBUG(LOG_CRITICAL, "Could not find ep_data");
        return MIST_ERROR;
    }

    JNIEnv * my_env = NULL;
    bool did_attach = false;
    if (getJNIEnv(javaVM, &my_env, &did_attach)) {
        android_wish_printf("Method invocation failure, could not get JNI env");
        return MIST_ERROR;
    }

    jclass writeCallbackClass = (*my_env)->GetObjectClass(my_env, ep_data->writable_object);
    if (writeCallbackClass == NULL) {
        WISHDEBUG(LOG_CRITICAL, "Cannot get writable callback class");
        return MIST_ERROR;
    }

    switch (ep->type) {
    case MIST_TYPE_BOOL:
        {
            jmethodID writeMethodId = (*my_env)->GetMethodID(my_env, writeCallbackClass, "write", "(Z)V");
            if (writeMethodId == NULL) {
                WISHDEBUG(LOG_CRITICAL, "Cannot get write method");
                return MIST_ERROR;
            }
            bool bool_data = *((bool *)value.base);
            (*my_env)->CallVoidMethod(my_env, ep_data->writable_object, writeMethodId, bool_data);
        }
        break;
    case MIST_TYPE_STRING:
        {
            jmethodID writeMethodId = (*my_env)->GetMethodID(my_env, writeCallbackClass, "write", "(Ljava/lang/String;)V");
            if (writeMethodId == NULL) {
                WISHDEBUG(LOG_CRITICAL, "Cannot get write method for String value");
                return MIST_ERROR;
            }
            char *str_data = (char *) value.base;
            /* Create Java string with new value. Returns local reference. */
            jstring java_string = (*my_env)->NewStringUTF(my_env, str_data);
            if (java_string != NULL) {
                (*my_env)->CallVoidMethod(my_env, ep_data->writable_object, writeMethodId, java_string);
            }
            else {
                WISHDEBUG(LOG_CRITICAL, "Cannot construct Java string in hw_write");
            }
        }
        break;
    case MIST_TYPE_INT:
        {
            jmethodID writeMethodId = (*my_env)->GetMethodID(my_env, writeCallbackClass, "write", "(I)V");
            if (writeMethodId == NULL) {
                WISHDEBUG(LOG_CRITICAL, "Cannot get write method");
                return MIST_ERROR;
            }
            int data = *((int *)value.base);
            (*my_env)->CallVoidMethod(my_env, ep_data->writable_object, writeMethodId, data);
        }
        break;
    case MIST_TYPE_FLOAT:
        {
            jmethodID writeMethodId = (*my_env)->GetMethodID(my_env, writeCallbackClass, "write", "(D)V");
            if (writeMethodId == NULL) {
                WISHDEBUG(LOG_CRITICAL, "Cannot get write method");
                return MIST_ERROR;
            }
            double data = *((double *)value.base);
            (*my_env)->CallVoidMethod(my_env, ep_data->writable_object, writeMethodId, data);
        }
        break;
        break;
    case MIST_TYPE_INVOKE:
        break;
    }

    return retval;
}

static enum mist_error hw_invoke(mist_ep *ep, mist_buf args) {
    enum mist_error retval = MIST_ERROR;
    android_wish_printf("in hw_invoke (NOT IMPLEMENTED)");

    return retval;
}

/*
 * Class:     fi_ct_mist_mistnodeapi_api_mistNode_MistNodeApi
 * Method:    updateBool
 * Signature: (Ljava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNodeApi_updateBool
  (JNIEnv *env, jobject java_this, jstring java_epID, jboolean java_newValue) {
    android_wish_printf("in updateBool");
    char *id_str =  (char*) (*env)->GetStringUTFChars(env, java_epID, NULL);
    struct endpoint_data *ep_data = lookup_ep_data_by_ep_id(id_str);
    if (ep_data == NULL) {
        WISHDEBUG(LOG_CRITICAL, "Could not find ep_data");
        return;
    }

    mist_ep *ep = NULL;
    if (MIST_NO_ERROR != mist_find_endpoint_by_name(model, id_str, &ep)) {
        WISHDEBUG(LOG_CRITICAL, "Could not find mist_ep by id!");
        return;
    }
    if (ep == NULL) {
        WISHDEBUG(LOG_CRITICAL, "mist_ep unexpectedly NULL!");
        return;
    }

    memcpy(ep->data.base, &java_newValue, sizeof(jboolean));
    mist_value_changed(model, id_str);
    (*env)->ReleaseStringUTFChars(env, java_epID, id_str);
    android_wish_printf("exiting updateBool");
}

/*
 * Class:     fi_ct_mist_mistnodeapi_api_mistNode_MistNodeApi
 * Method:    updateInt
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNodeApi_updateInt
  (JNIEnv *env, jobject java_this, jstring java_epID, jint java_newValue) {
    android_wish_printf("in updateInt %i", java_newValue);
    char *id_str =  (char*) (*env)->GetStringUTFChars(env, java_epID, NULL);
    struct endpoint_data *ep_data = lookup_ep_data_by_ep_id(id_str);
    if (ep_data == NULL) {
        WISHDEBUG(LOG_CRITICAL, "Could not find ep_data");
        return;
    }
    mist_ep *ep = NULL;
    if (MIST_NO_ERROR != mist_find_endpoint_by_name(model, id_str, &ep)) {
        WISHDEBUG(LOG_CRITICAL, "Could not find mist_ep by id!");
        return;
    }
    if (ep == NULL) {
        WISHDEBUG(LOG_CRITICAL, "mist_ep unexpectedly NULL!");
        return;
    }

    memcpy(ep->data.base, &java_newValue, sizeof(int));
    mist_value_changed(model, id_str);
    (*env)->ReleaseStringUTFChars(env, java_epID, id_str);
    android_wish_printf("exiting updateInt");
}

/*
 * Class:     fi_ct_mist_mistnodeapi_api_mistNode_MistNodeApi
 * Method:    updateString
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNodeApi_updateString
  (JNIEnv *env, jobject java_this, jstring java_epID, jstring java_newValue) {
    android_wish_printf("in updateString");
    char *id_str =  (char*) (*env)->GetStringUTFChars(env, java_epID, NULL);
    android_wish_printf("updateString ep is %s", id_str);
    struct endpoint_data *ep_data = lookup_ep_data_by_ep_id(id_str);
    if (ep_data == NULL) {
        android_wish_printf("Could not find ep_data");
    }
    else {
        jsize str_len = (*env)->GetStringUTFLength(env, java_newValue);
        char* str = (char *) (*env)->GetStringUTFChars(env, java_newValue, NULL);
        size_t str_copy_len = str_len + 1; /* str length + null terminate */

        char* str_copy = malloc(str_copy_len);
        if (str_copy == NULL) {
            android_wish_printf("malloc fail in updateString");
            return;
        }
        memset(str_copy, 0, str_copy_len);
        memcpy(str_copy, str, str_len);

        mist_ep *ep = NULL;
        if (MIST_NO_ERROR != mist_find_endpoint_by_name(model, id_str, &ep)) { //This is the point where it fails when you deal with nested mist.name ep
            android_wish_printf("Could not find mist_ep by id!");
            return;
        }
        if (ep == NULL) {
            android_wish_printf("mist_ep unexpectedly NULL!");
            return;
        }

        /* If ep_data->string_value is non null it means that we have old value. Free it. */

        if (ep->data.base != NULL) {
            free(ep->data.base);
        }
        ep->data.base = str_copy;
        ep->data.len = str_copy_len;

        android_wish_printf("called mist_value_changed");
        mist_value_changed(model, id_str);
        android_wish_printf("returned from mist_value_chanded");
        (*env)->ReleaseStringUTFChars(env, java_newValue, str);
    }

    (*env)->ReleaseStringUTFChars(env, java_epID, id_str);
    android_wish_printf("exiting updateString");
}

/*
 * Class:     fi_ct_mist_mistnodeapi_api_mistNode_MistNodeApi
 * Method:    updateFloat
 * Signature: (Ljava/lang/String;D)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNodeApi_updateFloat
  (JNIEnv *env, jobject java_this, jstring java_epID, jdouble java_newValue) {
    android_wish_printf("in updateFloat %lf", java_newValue);
    char *id_str =  (char*) (*env)->GetStringUTFChars(env, java_epID, NULL);
    struct endpoint_data *ep_data = lookup_ep_data_by_ep_id(id_str);
    if (ep_data == NULL) {
        WISHDEBUG(LOG_CRITICAL, "Could not find ep_data");
        return;
    }
    mist_ep *ep = NULL;
    if (MIST_NO_ERROR != mist_find_endpoint_by_name(model, id_str, &ep)) {
        WISHDEBUG(LOG_CRITICAL, "Could not find mist_ep by id!");
        return;
    }
    if (ep == NULL) {
        WISHDEBUG(LOG_CRITICAL, "mist_ep unexpectedly NULL!");
        return;
    }

    memcpy(ep->data.base, &java_newValue, sizeof(float));
    mist_value_changed(model, id_str);
    (*env)->ReleaseStringUTFChars(env, java_epID, id_str);
    android_wish_printf("exiting updateFloat");
}

/*
 * Class:     fi_ct_mist_mistnodeapi_api_mistNode_MistNodeApi
 * Method:    addEndpoint
 * Signature: (Lmist/node/Endpoint;)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNodeApi_addEndpoint
  (JNIEnv *env, jobject java_this, jobject java_Endpoint) {
    android_wish_printf("in addEndpoint");

    struct endpoint_data *ep_data = wish_platform_malloc(sizeof (struct endpoint_data));
    if (ep_data == NULL) {
        android_wish_printf("Could allocate memory for endpoint callbacks");
        return;
    }
    memset(ep_data, 0, sizeof (struct endpoint_data));

    /* Get the class corresponding to java_Endpoint instance */
    jclass endpointClass = (*env)->GetObjectClass(env, java_Endpoint);

    /* Get "id" field, a String */
    jfieldID idField = (*env)->GetFieldID(env, endpointClass, "id", "Ljava/lang/String;");
    if (idField == NULL) {
        android_wish_printf("Could not get idField");
    }
    jobject idString = (*env)->GetObjectField(env, java_Endpoint, idField);
    if (idString == NULL) {
        android_wish_printf("Could not get idString");
    }
    char *id_str =  (char*) (*env)->GetStringUTFChars(env, idString, NULL);
    ep_data->endpoint_id = strdup(id_str);

    android_wish_printf("addEndpoint: %s", id_str);

    /* Get the parent endpoint's id */
    char *parent_id_str = NULL;
    jobject parentEpIdString = NULL;
    jfieldID parentEpField = (*env)->GetFieldID(env, endpointClass, "parent", "Lmist/node/Endpoint;");
    if (parentEpField != NULL) {
        jobject parentEndpoint = (*env)->GetObjectField(env, java_Endpoint, parentEpField);
        if (parentEndpoint != NULL) {
            jclass parentEndpointClass = (*env)->GetObjectClass(env, parentEndpoint);
            if (parentEndpointClass != NULL) {
                jfieldID parentEpIdField = (*env)->GetFieldID(env, parentEndpointClass, "id", "Ljava/lang/String;");

                if (parentEpIdField != NULL) {
                    parentEpIdString = (*env)->GetObjectField(env, parentEndpoint, parentEpIdField);
                    if (parentEpIdString != NULL) {
                        parent_id_str  =  (char*) (*env)->GetStringUTFChars(env, parentEpIdString, NULL);
                    }
                    else {
                        android_wish_printf("Could not get parent Endpoint's id object");
                        return;
                    }
                }
                else {
                    android_wish_printf("Could not get the parent Endpoint's id field");
                    return;
                }
            }
            else {
                android_wish_printf("Could not get the Endpoint class corresponding to the parent");
                return;
            }
        }
        else {
            android_wish_printf("Parent Endpoint reference is null. This is OK, if you are adding a root-level endpoint.");
        }

    }
    else {
        android_wish_printf("Could not find the parent field from Endpoint");
        return;
    }

    if (parent_id_str != NULL) {
        android_wish_printf("addEndpoint: parent of %s", parent_id_str);
    }

    /* Get "label" field, a String */

    jfieldID labelField = (*env)->GetFieldID(env, endpointClass, "label", "Ljava/lang/String;");
    if (labelField == NULL) {
        android_wish_printf("Could not get labelField");
    }
    jobject labelString = (*env)->GetObjectField(env, java_Endpoint, labelField);
    if (labelString == NULL) {
        android_wish_printf("Could not get labelString");
    }
    char *label_str =  (char*) (*env)->GetStringUTFChars(env, labelString, NULL);

    android_wish_printf("addEndpoint: %s", label_str);

    /* Get "type" field, an int */

    jfieldID typeField = (*env)->GetFieldID(env, endpointClass, "type", "I");
    jint type = (*env)->GetIntField(env, java_Endpoint, typeField);
    android_wish_printf("addEndpoint: type %i", type);
    //ep_data->type = type;

    /* Get "unit" field, a String */

    jfieldID unitField = (*env)->GetFieldID(env, endpointClass, "unit", "Ljava/lang/String;");
    if (unitField == NULL) {
        android_wish_printf("Could not get unitField");
    }
    jobject unitString = (*env)->GetObjectField(env, java_Endpoint, unitField);
    if (unitString == NULL) {
        android_wish_printf("Could not get unitString");
    }
    char *unit_str =  (char*) (*env)->GetStringUTFChars(env, unitString, NULL);
    android_wish_printf("addEndpoint: unit %s", unit_str);

    /* Get readable boolean */
    jfieldID readableField = (*env)->GetFieldID(env, endpointClass, "readable", "Z");
    jboolean readable = (*env)->GetBooleanField(env, java_Endpoint, readableField);

    enum mist_error (*ep_read_fn)(mist_ep* ep, mist_buf* result) = NULL;
    if (readable) {
        android_wish_printf("is readable");
        ep_read_fn = hw_read;
    }

    jfieldID writableField = (*env)->GetFieldID(env, endpointClass, "writable", "Z");
    jboolean writable = (*env)->GetBooleanField(env, java_Endpoint, writableField);

    enum mist_error (*ep_write_fn)(mist_ep* ep, mist_buf value) = NULL;
    /* Get the relevant Endpoint<type>.Writable object reference */
    if (writable) {
        android_wish_printf("is writable");
        jfieldID writableCallbackField = NULL;
        jobject writableCallbackObject = NULL;
        switch (type) {
        case MIST_TYPE_BOOL:
            writableCallbackField = (*env)->GetFieldID(env, endpointClass, "writeCallback", "Lmist/node/EndpointBoolean$Writable;");
            writableCallbackObject = (*env)->GetObjectField(env, java_Endpoint, writableCallbackField);
            break;
        case MIST_TYPE_FLOAT:
            writableCallbackField = (*env)->GetFieldID(env, endpointClass, "writeCallback", "Lmist/node/EndpointFloat$Writable;");
            writableCallbackObject = (*env)->GetObjectField(env, java_Endpoint, writableCallbackField);
            break;
        case MIST_TYPE_INT:
            writableCallbackField = (*env)->GetFieldID(env, endpointClass, "writeCallback", "Lmist/node/EndpointInt$Writable;");
            writableCallbackObject = (*env)->GetObjectField(env, java_Endpoint, writableCallbackField);
            break;
        case MIST_TYPE_STRING:
            writableCallbackField = (*env)->GetFieldID(env, endpointClass, "writeCallback", "Lmist/node/EndpointString$Writable;");
            writableCallbackObject = (*env)->GetObjectField(env, java_Endpoint, writableCallbackField);
            break;
        }
        if (writableCallbackObject == NULL) {
            android_wish_printf("Could not get writable callback object");
            return;
        } else {
            /* Create a global reference for the Callback object */

            ep_data->writable_object = (*env)->NewGlobalRef(env, writableCallbackObject);
            ep_write_fn = hw_write;
        }
    }

    jfieldID invokableField = (*env)->GetFieldID(env, endpointClass, "invokable", "Z");
    jboolean invokable = (*env)->GetBooleanField(env, java_Endpoint, invokableField);

    enum mist_error (*ep_invoke_fn)(mist_ep* ep, mist_buf args) = NULL;

    if (invokable) {
        WISHDEBUG(LOG_CRITICAL, "is invokable");
         /* Get the Endpoint.Invokable object reference field */
        jfieldID invokeCallbackField = (*env)->GetFieldID(env, endpointClass, "invokeCallback", "Lmist/node/Endpoint$Invokable;");
        if (invokeCallbackField == NULL) {
            android_wish_printf("Could not get invokablefield");
            return;
        } else {
            jobject invokeCallbackObject = (*env)->GetObjectField(env, java_Endpoint, invokeCallbackField);
            ep_data->invokable_object = (*env)->NewGlobalRef(env, invokeCallbackObject);
            ep_invoke_fn = hw_invoke;
        }
    }

    /* Actually add the endpoint */

    //mist_add_endpoint(model, id_str, label_str, type, unit_str, ep_read_fn, ep_write_fn, ep_invoke_fn);


    // allocate a new endpoint and space for data
    mist_ep* ep = (mist_ep*) malloc(sizeof(mist_ep));
    if (ep == NULL) {
        android_wish_printf("Out of memory when allocating mist_ep");
        return;
    }
    memset(ep, 0, sizeof(mist_ep));
    ep->data.base = (char*) malloc(8);
    ep->data.len = 8;
    if (ep->data.base == NULL) {
        android_wish_printf("Out of memory when allocating mist_ep data backing");
        free(ep);
        return;
    }
    memset(ep->data.base, 0, 8);

    ep->id = strdup(id_str);
    ep->label = strdup(label_str);
    ep->type = type;

    switch (type) {
    case MIST_TYPE_FLOAT:
        *((double*) ep->data.base) = 0.0;
        break;
    case MIST_TYPE_INT:
        *((int*) ep->data.base) = 0;
        break;
    case MIST_TYPE_BOOL:
        *((bool*) ep->data.base) = false;
        break;
    case MIST_TYPE_INVOKE:
         android_wish_printf("Invocables are not supported at this time");
    case MIST_TYPE_STRING:
        free(ep->data.base);
        ep->data.base = NULL;
        ep->data.len = 0;
        break;
    default:
        android_wish_printf("");
        break;
    }

    if (readable) { ep->read = hw_read; }
    if (writable) { ep->write = hw_write; }
    if (invokable) { ep->invoke = hw_invoke; } //hw_invoke_function;
    ep->unit = NULL;
    ep->next = NULL;
    ep->prev = NULL;
    ep->dirty = false;
    ep->scaling = NULL;

    //char* parent = endpoint_path_from_model(parent_id_str);

    if (MIST_NO_ERROR != mist_add_ep(model, parent_id_str, ep)) {
        android_wish_printf("addEndpoint returned error");
    }

    LL_APPEND(endpoint_head, ep_data);

    /* Clean up the (temporary) references we've created */
    (*env)->ReleaseStringUTFChars(env, idString, id_str);
    if (parent_id_str != NULL) {
        (*env)->ReleaseStringUTFChars(env, parentEpIdString, parent_id_str);
    }
    (*env)->ReleaseStringUTFChars(env, labelString, label_str);
    (*env)->ReleaseStringUTFChars(env, unitString, unit_str);
    android_wish_printf("exiting addEndpoint");
}

/*
 * Class:     fi_ct_mist_mistnodeapi_api_mistNode_MistNodeApi
 * Method:    startMistApp
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_mist_node_MistNodeApi_startMistApp
  (JNIEnv *env, jobject java_this, jstring java_appName) {
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

    mistNodeApiInstance = (*env)->NewGlobalRef(env, java_this);

    mist_app_t *mist_app = start_mist_app();
    if (mist_app == NULL) {
        WISHDEBUG(LOG_CRITICAL, "Failed creating mist app!");
    }

    char *name_str =  (char*) (*env)->GetStringUTFChars(env, java_appName, NULL);
    mist_set_name(mist_app, name_str);

    model = &mist_app->model;

    app = wish_app_create(name_str);
    if (app == NULL) {
        WISHDEBUG(LOG_CRITICAL, "Failed creating wish app!");
    }
    wish_app_add_protocol(app, &mist_app->ucp_handler);
    mist_app->app = app;
    (*env)->ReleaseStringUTFChars(env, java_appName, name_str);

    /* The app will login to core when the Bridge connects, this happens via the wish_app_connected(wish_app_t *app, bool connected) function */
}

wish_app_t *get_mist_node_app(void) {
    return app;
}

JavaVM *getJavaVM(void) {
    return javaVM;
}

jobject getMistNodeApiInstance() {
    return mistNodeApiInstance;
}