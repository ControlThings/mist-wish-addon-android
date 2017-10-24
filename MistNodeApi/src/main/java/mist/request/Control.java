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

    public static int model(Peer peer, ModelCb callback){
        return ControlModel.request(peer, callback);
    };

    public static int read(Peer peer, String epid, Control.ReadCb callback) {
       return ControlRead.request(peer, epid, callback);
    }

    public static int write(Peer peer, String epid, Boolean state, Control.WriteCb callback) {
        return ControlWrite.request(peer, epid, state, callback);
    }
    public static int write(Peer peer, String epid, int state, Control.WriteCb callback) {
        return ControlWrite.request(peer, epid, state, callback);
    }
    public static int write(Peer peer, String epid, float state, Control.WriteCb callback) {
        return ControlWrite.request(peer, epid, state, callback);
    }
    public static int write(Peer peer, String epid, String state, Control.WriteCb callback) {
        return ControlWrite.request(peer, epid, state, callback);
    }

    public static int invoke(Peer peer, String epid, byte[] bson, InvokeCb callback) {
        return ControlInvoke.request(peer, epid, bson, callback);
    }

    public static int follow(Peer peer, FollowCb callback) {
        return ControlFollow.request(peer, callback);
    }

    public abstract static class ModelCb extends Callback {
        public abstract void cb(BsonDocument data);
    }

    public abstract static class ReadCb extends Callback {
        public void cbBoolean(Boolean data) {};
        public void cbInt(int data) {};
        public void cbFloat(double data) {};
        public void cbString(String data) {};
    }

    public abstract static class WriteCb extends Callback {
        public abstract void cb();
    }

    public abstract static class InvokeCb extends Callback {
        public abstract void cb(byte[] bson);
    }

    public abstract static class FollowCb extends Callback {
        public void cbBool(String epid, boolean value) {};
        public void cbInt(String epid, int value) {};
        public void cbFloat(String epid, float value) {};
        public void cbString(String epid, String value) {};
    }
}
