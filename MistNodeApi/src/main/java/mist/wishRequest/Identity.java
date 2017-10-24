package mist.wishRequest;



/**
 * Created by jeppe on 10/24/17.
 */

public class Identity {

    public static int get(byte[] uid, GetCb callback){
        return IdentityGet.request(uid, callback);
    };

    public abstract static class GetCb extends Callback {
        public abstract void cb (mist.Identity identity);
    }

}
