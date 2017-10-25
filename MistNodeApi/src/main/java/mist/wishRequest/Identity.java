package mist.wishRequest;


import java.util.List;

/**
 * Created by jeppe on 10/24/17.
 */

public class Identity {

    public static int list(ListCb callback) {
        return IdentityList.request(callback);
    }

    public static int get(byte[] uid, GetCb callback) {
        return IdentityGet.request(uid, callback);
    };


    public abstract static class ListCb extends Callback {
        public abstract void cb (List<mist.Identity> identities);
    }

    public abstract static class GetCb extends Callback {
        public abstract void cb (mist.Identity identity);
    }

}
