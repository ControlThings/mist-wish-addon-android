package wishApp;

/**
 * Created by jeppe on 12/15/16.
 */

public class LocalDiscovery {

    private String alias;
    private String type;
    private byte[] luid;
    private byte[] ruid;
    private byte[] rhid;
    private byte[] pubkey;
    private boolean claim;


    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getLuid() {
        return luid;
    }

    public void setLuid(byte[] luid) {
        this.luid = luid;
    }

    public byte[] getRuid() {
        return ruid;
    }

    public void setRuid(byte[] ruid) {
        this.ruid = ruid;
    }

    public byte[] getRhid() {
        return rhid;
    }

    public void setRhid(byte[] rhid) {
        this.rhid = rhid;
    }


    public byte[] getPubkey() {
        return pubkey;
    }

    public void setPubkey(byte[] pubkey) {
        this.pubkey = pubkey;
    }

    public boolean isClaim() {
        return claim;
    }

    public void setClaim(boolean claim) {
        this.claim = claim;
    }
}

