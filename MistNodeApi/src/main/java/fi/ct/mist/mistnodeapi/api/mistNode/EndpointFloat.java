package fi.ct.mist.mistnodeapi.api.mistNode;

/**
 * Created by André Kaustell on 2016-12-05.
 */

public class EndpointFloat extends Endpoint {
    private Writable writeCallback;

    public EndpointFloat(String id, String label) {
        super(id, label, MIST_TYPE_FLOAT, "", true);
    }

    public interface Writable {
        void write(double newValue);
    }

    public void setWritable(EndpointFloat.Writable w) {
        this.writable = true;
        this.writeCallback = w;
    }

    public void update(double newValue) {
        super.update(newValue);
    }

}

