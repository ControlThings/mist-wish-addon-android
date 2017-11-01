package mistNodeApi;

import android.util.Log;

import newApi.wishRequest.Identity;

/**
 * Created by jeppe on 10/24/17.
 */

public class Test {

    static final String TAG = "testing";

    public void run() {

        Log.d(TAG, "ss1");
        Identity.create("tester", new Identity.CreateCb() {
            newApi.Identity tester;

            @Override
            public void cb(newApi.Identity identity) {
                tester = identity;
                Log.d(TAG, "ss2");
                Identity.export(identity.getUid(), new Identity.ExportCb() {
                    byte[] test;

                    @Override
                    public void cb(byte[] bsonData, byte[] bsonRaw) {
                        test = bsonData;
                        Identity.remove(tester.getUid(), new Identity.RemoveCb() {
                            @Override
                            public void cb(boolean value) {

                                Identity._import(test, new Identity.ImportCb() {
                                    @Override
                                    public void cb(String name, byte[] uid) {
                                        Log.d(TAG, " imported: " + name);
                                    }

                                    @Override
                                    public void err(int code, String msg) {
                                        super.err(code, msg);

                                        Log.d(TAG, " : " + msg);

                                    }
                                });
                            }
                        });

                    }
                });

            }
        });


    }


}
