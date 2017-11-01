package mistNodeApi.wishRequest;


import mistNodeApi.Connection;
import mistNodeApi.ErrorCallback;
import mistNodeApi.RequestInterface;

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

    public interface VersionCb extends ErrorCallback {
        public void cb(String version);
    }

    public interface SignalsCb extends ErrorCallback {
        public void cb(String signal);
    }

    public static void cancel(int id) {
        RequestInterface.getInstance().wishRequestCancel(id);
    }
}

