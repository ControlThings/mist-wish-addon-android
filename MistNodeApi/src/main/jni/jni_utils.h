//
// Created by jan on 6/7/16.
//

#ifndef WISH_JNI_UTILS_H
#define WISH_JNI_UTILS_H

int android_wish_printf(const char *format, ...);
int android_wish_vprintf(const char *format, va_list arg_list);

int getJNIEnv(JavaVM *vm, JNIEnv **result_env, bool * didAttach);
int detachThread();

#endif //WISH_JNI_UTILS_H
