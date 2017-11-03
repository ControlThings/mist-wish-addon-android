package wishApp.request;

import java.util.ArrayList;

import mistNode.request.Callback;


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

    public interface DisconectCb extends Callback {
        public void cb(boolean state);
    }


    public interface ListCb extends Callback {
        public void cb(ArrayList<wishApp.Connection> connections);
    }
}
