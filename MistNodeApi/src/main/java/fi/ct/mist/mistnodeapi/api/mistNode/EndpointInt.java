package fi.ct.mist.mistnodeapi.api.mistNode;

/**
 * Created by André Kaustell on 2016-12-05.
 */

public class EndpointInt extends Endpoint {
    private Writable writeCallback;

    public EndpointInt(String id, String label) {
        super(id, label, MIST_TYPE_INT, "", true);
    }

    public interface Writable {
        void write(int newValue);
    }

    public void setWritable(EndpointInt.Writable w) {
        this.writable = true;
        this.writeCallback = w;
    }

    public void update(int newValue) {
        super.update(newValue);
    }

}

