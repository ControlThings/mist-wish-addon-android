package newApi.wishRequest;


import java.util.List;

/**
 * Created by jeppe on 10/24/17.
 */

public class Identity {

    /**
     *
     * @param alias String alias
     * @param callback Identity.CreateCb
     * @return
     */
    public static int create(String alias, CreateCb callback) {
        return IdentityCreate.request(alias, callback);
    }

    /**
     *
     * @param uid Byte array of identity uid
     * @param callback Identity.ExportCb
     * @return
     */
    public static int export(byte[] uid, ExportCb callback) {
        return IdentityExport.request(uid, callback);
    }

    /**
     *
     * @param identity a BSON representation of wish identity {alias, uid, pubkey}
     * @param callback Identity.ImportCb
     */
    public static int _import(byte[] identity, ImportCb callback) {
       return IdentityImport.request(identity, callback);
    }

    /**
     *
     * @param callback Identity.ListCb
     * @return
     */
    public static int list(ListCb callback) {
        return IdentityList.request(callback);
    }

    /**
     *
     * @param uid Byte array of identity uid
     * @param callback Identity.GetCb
     * @return
     */
    public static int get(byte[] uid, GetCb callback) {
        return IdentityGet.request(uid, callback);
    };

    /**
     *
     * @param uid Byte array of identity uid
     * @param callback Identity.RemoveCb
     * @return
     */
    public static int remove(byte[] uid, RemoveCb callback) {
        return IdentityRemove.request(uid, callback);
    };

    public abstract static class CreateCb extends Callback {
        public abstract void cb (newApi.Identity identity);
    }

    public abstract static class ExportCb extends Callback {
        public abstract void cb(byte[] bsonData, byte[] bsonRaw);
    }

    public abstract static class ImportCb extends Callback {
        public abstract void cb(String name, byte[] uid);
    }

    public abstract static class ListCb extends Callback {
        public abstract void cb (List<newApi.Identity> identities);
    }

    public abstract static class GetCb extends Callback {
        public abstract void cb (newApi.Identity identity);
    }

    public abstract static class RemoveCb extends Callback {
        public abstract void cb (boolean value);
    }
}
