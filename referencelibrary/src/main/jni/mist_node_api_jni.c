//
// Created by jan on 11/3/16.
//

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "mist_app.h"
#include "mist_follow_funcs.h"
#include "mist_model.h"

/*
To re-create JNI interface header file:
javah -classpath ../../../../referencelibrary/build/intermediates/classes/debug/:/home/jan/Android/Sdk/platforms/android-16/android.jar -o mist_node_api_jni.h fi.ct.mist.referencelibrary.api.mistNode.MistNodeApi
*/
#include "mist_node_api_jni.h"
#include "jni_utils.h"
#include "mist_node_api_helper.h"

#include "utlist.h"

static JavaVM *javaVM;
static struct mist_model *model;
static wish_app_t *app;



struct endpoint_data {
    char *endpoint_id;
    union {
        bool bool_value;
        double float_value;
        int int_value;
        char* string_value;
    };
    enum mist_type type;
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

static enum mist_error hw_read(mist_ep *ep, void *result) {
    enum mist_error retval = MIST_ERROR;
    android_wish_printf("in hw_read");

    struct endpoint_data *ep_data = lookup_ep_data_by_ep_id(ep->id);
    if (ep_data == NULL) {
        WISHDEBUG(LOG_CRITICAL, "Could not find ep_data");
        return MIST_ERROR;
    }

    switch (ep->type) {
    case MIST_TYPE_BOOL:
        {
            bool *bool_result = result;
            *bool_result = ep_data->bool_value;
        }
        break;
    case MIST_TYPE_STRING:
        break;
    case MIST_TYPE_INT:
        break;
    case MIST_TYPE_FLOAT:
        break;
    case MIST_TYPE_INVOKE:
        break;
    }

    retval = MIST_NO_ERROR;
    return retval;
}

static enum mist_error hw_write(mist_ep *ep, void *value) {
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
            bool bool_data = *((bool *)value);
            (*my_env)->CallVoidMethod(my_env, ep_data->writable_object, writeMethodId, bool_data);
        }
        break;
    case MIST_TYPE_STRING:
        break;
    case MIST_TYPE_INT:
        break;
    case MIST_TYPE_FLOAT:
        break;
    case MIST_TYPE_INVOKE:
        break;
    }

    return retval;
}

static enum mist_error hw_invoke(mist_ep *ep, mist_buf args, mist_buf response) {
    enum mist_error retval = MIST_ERROR;
    android_wish_printf("in hw_invoke");

    return retval;
}

/*
 * Class:     fi_ct_mist_referencelibrary_api_mistNode_MistNodeApi
 * Method:    updateBool
 * Signature: (Ljava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_fi_ct_mist_referencelibrary_api_mistNode_MistNodeApi_updateBool
  (JNIEnv *env, jobject java_this, jstring java_epID, jboolean java_newValue) {
    android_wish_printf("in updateBool");
    char *id_str =  (char*) (*env)->GetStringUTFChars(env, java_epID, NULL);
    struct endpoint_data *ep_data = lookup_ep_data_by_ep_id(id_str);
    if (ep_data == NULL) {
        WISHDEBUG(LOG_CRITICAL, "Could not find ep_data");
        return;
    }
    ep_data->bool_value = java_newValue;
    mist_value_changed(model, id_str);
}

/*
 * Class:     fi_ct_mist_referencelibrary_api_mistNode_MistNodeApi
 * Method:    updateInt
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_fi_ct_mist_referencelibrary_api_mistNode_MistNodeApi_updateInt
  (JNIEnv *env, jobject java_this, jstring java_epName, jint java_newValue) {
    android_wish_printf("in updateInt");
}

/*
 * Class:     fi_ct_mist_referencelibrary_api_mistNode_MistNodeApi
 * Method:    updateString
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_fi_ct_mist_referencelibrary_api_mistNode_MistNodeApi_updateString
  (JNIEnv *env, jobject java_this, jstring java_epName, jstring java_newValue) {
    android_wish_printf("in updateString");
}

/*
 * Class:     fi_ct_mist_referencelibrary_api_mistNode_MistNodeApi
 * Method:    updateFloat
 * Signature: (Ljava/lang/String;D)V
 */
JNIEXPORT void JNICALL Java_fi_ct_mist_referencelibrary_api_mistNode_MistNodeApi_updateFloat
  (JNIEnv *env, jobject java_this, jstring java_epName, jdouble java_newValue) {
    android_wish_printf("in updateFloat");

}

/*
 * Class:     fi_ct_mist_referencelibrary_api_mistNode_MistNodeApi
 * Method:    addEndpoint
 * Signature: (Lfi/ct/mist/referencelibrary/api/mistNode/Endpoint;)V
 */
JNIEXPORT void JNICALL Java_fi_ct_mist_referencelibrary_api_mistNode_MistNodeApi_addEndpoint
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

    WISHDEBUG(LOG_CRITICAL, "addEndpoint: %s", id_str);

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

    WISHDEBUG(LOG_CRITICAL, "addEndpoint: %s", label_str);

    /* Get "type" field, an int */

    jfieldID typeField = (*env)->GetFieldID(env, endpointClass, "type", "I");
    jint type = (*env)->GetIntField(env, java_Endpoint, typeField);
    WISHDEBUG(LOG_CRITICAL, "addEndpoint: type %i", type);
    ep_data->type = type;

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
    WISHDEBUG(LOG_CRITICAL, "addEndpoint: unit %s", unit_str);

    /* Get readable boolean */
    jfieldID readableField = (*env)->GetFieldID(env, endpointClass, "readable", "Z");
    jboolean readable = (*env)->GetBooleanField(env, java_Endpoint, readableField);

    enum mist_error (*ep_read_fn)(mist_ep* ep, void* result) = NULL;
    if (readable) {
        WISHDEBUG(LOG_CRITICAL, "is readable");
        ep_read_fn = hw_read;
    }

    jfieldID writableField = (*env)->GetFieldID(env, endpointClass, "writable", "Z");
    jboolean writable = (*env)->GetBooleanField(env, java_Endpoint, writableField);

    enum mist_error (*ep_write_fn)(mist_ep* ep, void* value) = NULL;
    /* Get the relevant Endpoint<type>.Writable object reference */
    if (writable) {
        WISHDEBUG(LOG_CRITICAL, "is writable");
        jfieldID writableCallbackField = NULL;
        jobject writableCallbackObject = NULL;
        switch (type) {
        case MIST_TYPE_BOOL:
            writableCallbackField = (*env)->GetFieldID(env, endpointClass, "writeCallback", "Lfi/ct/mist/referencelibrary/api/mistNode/EndpointBoolean$Writable;");
            writableCallbackObject = (*env)->GetObjectField(env, java_Endpoint, writableCallbackField);
            break;
        case MIST_TYPE_FLOAT:
            break;
        case MIST_TYPE_INT:
            break;
        case MIST_TYPE_STRING:
            break;
        }
        if (writableCallbackObject == NULL) {
            WISHDEBUG(LOG_CRITICAL, "Could not get writable callback object");
            return;
        } else {
            /* Create a global reference for the Callback object */

            ep_data->writable_object = (*env)->NewGlobalRef(env, writableCallbackObject);
            ep_write_fn = hw_write;
        }
    }

    jfieldID invokableField = (*env)->GetFieldID(env, endpointClass, "invokable", "Z");
    jboolean invokable = (*env)->GetBooleanField(env, java_Endpoint, invokableField);

    enum mist_error (*ep_invoke_fn)(mist_ep* ep, mist_buf args, mist_buf response) = NULL;

    if (invokable) {
        WISHDEBUG(LOG_CRITICAL, "is invokable");
         /* Get the Endpoint.Invokable object reference field */
        jfieldID invokeCallbackField = (*env)->GetFieldID(env, endpointClass, "invokeCallback", "Lfi/ct/mist/referencelibrary/api/mistNode/Endpoint$Invokable;");
        if (invokeCallbackField == NULL) {
            android_wish_printf("Could not get invokablefield");
            return;
        }
        jobject invokeCallbackObject = (*env)->GetObjectField(env, java_Endpoint, invokeCallbackField);
        ep_data->invokable_object = invokeCallbackObject;
        ep_invoke_fn = hw_invoke;
    }

    /* Actually add the endpoint */

    mist_add_endpoint(model, id_str, label_str, type, unit_str, ep_read_fn, ep_write_fn, ep_invoke_fn);
    LL_APPEND(endpoint_head, ep_data);

    /* Clean up the (temporary) references we created */
    (*env)->ReleaseStringUTFChars(env, idString, id_str);
    (*env)->ReleaseStringUTFChars(env, idString, label_str);
    (*env)->ReleaseStringUTFChars(env, idString, unit_str);
}

/*
 * Class:     fi_ct_mist_referencelibrary_api_mistNode_MistNodeApi
 * Method:    startMistApp
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_fi_ct_mist_referencelibrary_api_mistNode_MistNodeApi_startMistApp
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

    mist_app_context_t *mist_app = start_mist_app();
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


void mist_follow_task_signal(void) {
    mist_follow_task();
}

wish_app_t *get_mist_node_app(void) {
    return app;
}