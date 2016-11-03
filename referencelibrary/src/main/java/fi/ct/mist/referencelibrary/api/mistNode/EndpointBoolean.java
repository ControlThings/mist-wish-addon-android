package fi.ct.mist.referencelibrary.api.mistNode;

/**
 * Created by jan on 11/1/16.
 */

public class EndpointBoolean extends Endpoint {
    private Writable writeMethod;

    public EndpointBoolean(String name) {
        super(name, MIST_TYPE_BOOL);
    }

    public interface Writable {
        void write(boolean newValue);
    }

    public void setWritable(EndpointBoolean.Writable w) {
        this.writable = true;
        this.writeMethod = w;
    }

    public void update(boolean newValue) {
        super.update(newValue);
    }

}
