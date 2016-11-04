package fi.ct.mist.referencelibrary.api.mistNode;

/**
 * Created by jan on 11/1/16.
 */

public class EndpointBoolean extends Endpoint {
    private Writable writeCallback;

    public EndpointBoolean(String id, String label) {
        super(id, label, MIST_TYPE_BOOL, "", true);
    }

    public interface Writable {
        void write(boolean newValue);
    }

    public void setWritable(EndpointBoolean.Writable w) {
        this.writable = true;
        this.writeCallback = w;
    }

    public void update(boolean newValue) {
        super.update(newValue);
    }

}
