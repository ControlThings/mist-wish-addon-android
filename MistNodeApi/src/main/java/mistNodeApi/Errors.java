package mistNodeApi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by jeppe on 3/2/17.
 */
public class Errors {

    private static Errors instance;

    static Errors getInstance() {
        if (instance == null) {
            instance = new Errors();
        }
        return instance;
    }


    private int id;
    private HashMap<Integer, MistCb> mistCbHashMap;
    private HashMap<Integer, WishCb> wishCbHashMap;

    private Errors() {
        id = 1;
        mistCbHashMap = new HashMap<>();
        wishCbHashMap = new HashMap<>();
        RequestInterface.getInstance().registerMistRpcErrorHandler(new RequestInterface.Error() {
            @Override
            public void cb(String op, int code, String msg) {
                mistError(op, code, msg);
            }
        });

        RequestInterface.getInstance().registerWishRpcErrorHandler(new RequestInterface.Error() {
            @Override
            public void cb(String op, int code, String msg) {
                wishError(op, code, msg);
            }
        });
    }

    private int registerCb(MistCb cb) {
        mistCbHashMap.put(id, cb);
        return id++;
    }

    private int registerCb(WishCb cb) {
        wishCbHashMap.put(id, cb);
        return id++;
    }

    public static int mist(MistCb callback) {
        return getInstance().registerCb(callback);
    }

    public static int wish(WishCb callback) {
        return getInstance().registerCb(callback);
    }

    public interface MistCb {
        public void cb(String op, int code, String msg);
    }

    public interface WishCb {
        public void cb(String op, int code, String msg);
    }

    public static void cancel(int id) {
        if (getInstance().mistCbHashMap.containsKey(id)) {
            getInstance().mistCbHashMap.remove(id);
            return;
        }
        if (getInstance().wishCbHashMap.containsKey(id)) {
            getInstance().wishCbHashMap.remove(id);
            return;
        }
    }

    static void mistError(String op, int code, String msg) {
        Iterator iterator = getInstance().mistCbHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            MistCb mistCb = (MistCb) pair.getValue();
            mistCb.cb(op, code, msg);
            iterator.remove();
        }
    }

    static void wishError(String op, int code, String msg) {
        Iterator iterator = getInstance().wishCbHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            WishCb wishCb = (WishCb) pair.getValue();
            wishCb.cb(op, code, msg);
            iterator.remove();
        }
    }

   public static void mistError(String op, int code, String msg, byte[] bson) {
        //bsonConsolePrettyPrinter("BSON code: " + code, bson);
        Iterator iterator = getInstance().mistCbHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            MistCb mistCb = (MistCb) pair.getValue();
            mistCb.cb(op, code, msg);
            iterator.remove();
        }
    }

    public static void wishError(String op, int code, String msg, byte[] bson) {
        //bsonConsolePrettyPrinter("BSON code: " + code, bson);
        Iterator iterator = getInstance().wishCbHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            WishCb wishCb = (WishCb) pair.getValue();
            wishCb.cb(op, code, msg);
            iterator.remove();
        }
    }

}
