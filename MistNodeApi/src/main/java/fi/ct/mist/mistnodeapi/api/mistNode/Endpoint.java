package fi.ct.mist.mistnodeapi.api.mistNode;

/**
 * Created by jan on 11/1/16.
 */

class Endpoint {
    static final int MIST_TYPE_BOOL = 0;
    static final int MIST_TYPE_FLOAT = 1;
    static final int MIST_TYPE_INT = 2;
    static final int MIST_TYPE_STRING = 3;
    /* ... */

    private MistNodeApi mistNodeApi;

    private Endpoint next;

    public Endpoint getNext() {
        return next;
    }

    private Endpoint firstChild;
    private Endpoint parent;

    private String id;
    private String label;
    private String unit;
    private int type;

    private boolean readable;
    protected boolean writable;
    private boolean invokable;

    private Invokable invokeCallback;

    protected Endpoint(String id, String label, int type, String unit, boolean readable) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.unit = unit;
        this.readable = readable;
        mistNodeApi = MistNodeApi.getInstance();
    }

    public void addNext(Endpoint e) {
        if (next == null) {
            next = e;
        } else {
            next.addNext(e);
        }
    }

    public void addChild(Endpoint child) {
        /* FIXME unimplemented */
        if (firstChild == null) {
            firstChild = child;
        } else {
            firstChild.addNext(child);
        }
        child.setParent(this);
    }

    public void setParent(Endpoint parent) {
        this.parent = parent;
    }

    public void setReadable(boolean readable) {
        this.readable = readable;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isWritable() {
        return writable;
    }

    public boolean isInvokable() {
        return invokable;
    }

    public void setInvokable(Endpoint.Invokable i) {
        this.invokable = true;
        invokeCallback = i;
    }

    public interface Invokable {
        byte[] invoke(byte[] args);
    }

    void update(boolean newValue) {
        /* Call low-level C method for updating new values through JNI */
        mistNodeApi.updateBool(id, newValue);
    }

    void update(int newValue) {
        mistNodeApi.updateInt(id, newValue);
    }

    void update(double newValue) {
        mistNodeApi.updateFloat(id, newValue);
    }

    void update(String newValue) {
        mistNodeApi.updateString(id, newValue);
    }


}
