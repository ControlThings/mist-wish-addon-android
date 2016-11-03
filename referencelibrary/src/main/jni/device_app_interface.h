//
// Created by jan on 6/16/16.
//

/* A temporary linkage between android_mist.c's jni_on_frame and device_api.c */

#ifndef MIST_DEVICE_APP_INTERFACE_H_H
#define MIST_DEVICE_APP_INTERFACE_H_H

void device_on_frame(void *app_ctx, uint8_t *data, size_t data_len, uint8_t *peer_doc);

#endif //MIST_DEVICE_APP_INTERFACE_H_H
