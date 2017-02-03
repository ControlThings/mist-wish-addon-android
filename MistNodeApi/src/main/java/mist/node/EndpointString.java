package mist.node;

/**
 * Created by jan on 2/3/17.
 */

public class EndpointString extends Endpoint {
    private Writable writeCallback;

    public EndpointString(String id, String label) {
        super(id, label, MIST_TYPE_STRING, "", true);
    }

    public interface Writable {
        void write(String newValue);
    }

    public void setWritable(EndpointString.Writable w) {
        this.writable = true;
        this.writeCallback = w;
    }

    public void update(String newValue) {
        super.update(newValue);
    }
}
