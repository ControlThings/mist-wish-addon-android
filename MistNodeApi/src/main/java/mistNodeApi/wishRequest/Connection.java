package mistNodeApi.wishRequest;

import java.util.ArrayList;

import mistNodeApi.ErrorCallback;


/**
 * Created by jeppe on 11/30/16.
 */

public class Connection {

    public static void disconect(int cid, DisconectCb callback) {
        ConnectionDisconnect.request(cid, callback);
    }

    public static void list(ListCb callback) {
        ConnectionList.request(callback);
    }

    public interface DisconectCb extends ErrorCallback {
        public void cb(boolean state);
    }


    public interface ListCb extends ErrorCallback {
        public void cb(ArrayList<mistNodeApi.Connection> connections);
    }
}
