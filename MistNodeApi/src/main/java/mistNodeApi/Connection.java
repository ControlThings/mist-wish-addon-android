package mistNodeApi;

/**
 * Created by jeppe on 12/28/16.
 */

public class Connection {

    private int cid;
    private byte[] luid;
    private byte[] ruid;
    private byte[] rhid;
    private boolean outgoing;

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
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

    public boolean isOutgoing() {
        return outgoing;
    }

    public void setOutgoing(boolean outgoing) {
        this.outgoing = outgoing;
    }
}
