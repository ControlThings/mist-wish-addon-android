package wishApp.request;


import wishApp.Connection;
import mistNode.request.Callback;
import node.RequestInterface;

/**
 * Created by jeppe on 11/30/16.
 */

public class Wish {

    public static void version(VersionCb callback) {
        WishVersion.request(callback);
    }

    static int signals(SignalsCb callback) {
        return WishSignals.request(null, callback);
    }

    public static int signals(Connection connection, SignalsCb callback) {
        return WishSignals.request(connection, callback);
    }

    public interface VersionCb extends Callback {
        public void cb(String version);
    }

    public interface SignalsCb extends Callback {
        public void cb(String signal);
    }

    public static void cancel(int id) {
        RequestInterface.getInstance().wishRequestCancel(id);
    }
}

