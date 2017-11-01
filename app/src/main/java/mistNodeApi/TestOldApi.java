package mistNodeApi;

import android.util.Log;

import java.util.ArrayList;

import mistNodeApi.wishRequest.Identity;

/**
 * Created by jeppe on 10/27/17.
 */

public class TestOldApi {

    private static final String TAG = "old TEST";

    public void test(){

        Identity.list(new Identity.ListCb() {
            @Override
            public void cb(ArrayList<MistIdentity> identityList) {
                for (MistIdentity ide : identityList) {
                    Log.d(TAG, "list: " + ide.getAlias());
                }
            }

            @Override
            public void err(int code, String msg) {}

            @Override
            public void end() {}
        });

/*
        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeString("op", "listPeers");
        writer.writeStartArray("args");
        writer.writeEndArray();
        writer.writeInt32("id", 0);
        writer.writeEndDocument();
        writer.flush();

        MistNode.getInstance().wishRequest(buffer.toByteArray(), new MistNode.RequestCb() {
            @Override
            public void response(byte[] data) {
                Log.d(TAG, "res");
            }

            @Override
            public void end() {

            }

            @Override
            public void err(int code, String msg) {
                Log.d(TAG, "err");
            }
        });

*/
    }
}
