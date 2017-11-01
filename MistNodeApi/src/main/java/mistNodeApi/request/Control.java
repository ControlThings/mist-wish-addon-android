package mistNodeApi.request;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.json.JSONObject;

import mistNodeApi.Peer;
import mistNodeApi.ErrorCallback;
import mistNodeApi.RequestInterface;

/**
 * Created by jeppe on 11/30/16.
 */

public class Control {

    public static int follow(Peer peer, FollowCb callback) {
        return ControlFollow.request(peer, callback);
    }

    public static void invoke(Peer peer, String epid, InvokeCb callback) {
        ControlInvoke.request(peer, epid, callback);
    }
    public static void invoke(Peer peer, String epid, String value, InvokeCb callback) {
        ControlInvoke.request(peer, epid, value, callback);
    }
    public static void invoke(Peer peer, String epid, Boolean value, InvokeCb callback) {
        ControlInvoke.request(peer, epid, value, callback);
    }
    public static void invoke(Peer peer, String epid, int value, InvokeCb callback) {
        ControlInvoke.request(peer, epid, value, callback);
    }
    public static void invoke(Peer peer, String epid, float value, InvokeCb callback) {
        ControlInvoke.request(peer, epid, value, callback);
    }
    public static void invoke(Peer peer, String epid, byte[] value, InvokeCb callback) {
        ControlInvoke.request(peer, epid, value, callback);
    }
    public static void invoke(Peer peer, String epid, Object value, InvokeCb callback) {
        ControlInvoke.request(peer, epid, value, callback);
    }

    public static void model(Peer peer, ModelCb callback){
        ControlModel.request(peer, callback);
    };

    public static void read(Peer peer, String epid, ReadCb callback) {
        ControlRead.request(peer, epid, callback);
    }

    public static void write(Peer peer, String epid, Boolean state, Control.WriteCb callback) {
        ControlWrite.request(peer, epid, state, callback);
    }
    public static void write(Peer peer, String epid, int state, Control.WriteCb callback) {
        ControlWrite.request(peer, epid, state, callback);
    }
    public static void write(Peer peer, String epid, double state, Control.WriteCb callback) {
        ControlWrite.request(peer, epid, state, callback);
    }
    public static void write(Peer peer, String epid, String state, Control.WriteCb callback) {
        ControlWrite.request(peer, epid, state, callback);
    }

    public static void requestMapping(Peer peer, Peer from, String epFrom, String epTo, RequestMappingCB callback) {
        ControlRequestMapping.request(peer, from, epFrom, epTo, callback);
    }

    public interface FollowCb extends ErrorCallback {
        public void cbBool(String epid, boolean value);
        public void cbInt(String epid, int value);
        public void cbFloat(String epid, double value);
        public void cbString(String epid, String value);
    }

    public interface InvokeCb extends ErrorCallback {
        public void cbBoolean(Boolean data);
        public void cbInt(int data);
        public void cbFloat(double data);
        public void cbString(String data);
        public void cbByte(byte[] data);
        public void cbArray(BsonArray array);
        public void cbDocument(BsonDocument document);
    }

    public interface ModelCb extends ErrorCallback {
        public void cb(JSONObject data);
    }

    public interface ReadCb extends ErrorCallback {
        public void cbBoolean(Boolean data);
        public void cbInt(int data);
        public void cbFloat(double data);
        public void cbString(String data);
    }

    public interface WriteCb extends ErrorCallback {
        public void cb(boolean data);
    }

    public interface RequestMappingCB extends ErrorCallback {
        public void cb();
    }

    public static void cancel(int id) {
        RequestInterface.getInstance().mistApiRequestCancel(id);
    }
}
