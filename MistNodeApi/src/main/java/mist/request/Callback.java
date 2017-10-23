package mist.request;

import android.util.Log;

/**
 * Created by jeppe on 3/29/17.
 */

abstract class Callback {

    public static final int BSON_ERROR_CODE = 836;
    public static final String BSON_ERROR_STRING = "Bad BSON structure";

    public void err(int code, String msg) {
        Log.d("Error", msg + " code: " + code);
    };
    public void end(){};
}
