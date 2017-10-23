package mist.request;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.json.JSONObject;

import mist.Peer;
import mist.node.MistNode;

/**
 * Created by jeppe on 11/30/16.
 */

public class Control {


    public static void read(Peer peer, String epid, Control.ReadCb callback) {
        ControlRead.request(peer, epid, callback);
    }

    public abstract static class ReadCb extends Callback {
        public void cbBoolean(Boolean data) {};
        public void cbInt(int data) {};
        public void cbFloat(double data) {};
        public void cbString(String data) {};
    }

}
