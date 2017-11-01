package mistNodeApi;

/**
 * Created by jeppe on 8/19/16.
 */
public class MistIdentity {

    private byte[] uid;
    private String alias;
    private boolean privkey;

    public byte[] getUid() {
        return uid;
    }

    public void setUid(byte[] uid) {
        this.uid = uid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean getPrivkey() {
        return privkey;
    }

    public void setPrivkey(boolean privkey) {
        this.privkey = privkey;
    }
}
