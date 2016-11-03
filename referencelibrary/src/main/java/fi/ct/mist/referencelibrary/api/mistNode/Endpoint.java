package fi.ct.mist.referencelibrary.api.mistNode;

/**
 * Created by jan on 11/1/16.
 */

class Endpoint {
    static final int MIST_TYPE_BOOL = 0;
    static final int MIST_TYPE_INT = 1;
    static final int MIST_TYPE_FLOAT = 2;
    static final int MIST_TYPE_STRING = 3;
    /* ... */




    private MistNodeApi mistNodeApi;

    private Endpoint next;

    public Endpoint getNext() {
        return next;
    }

    private Endpoint firstChild;
    private Endpoint parent;

    private String name;

    private boolean readable;
    protected boolean writable;
    private boolean invokable;

    private int type;

    private Invokable invokeMethod;

    protected Endpoint(String name, int type) {
        this.type = type;
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
        invokeMethod = i;
    }

    public interface Invokable {
        byte[] invoke(byte[] args);
    }

    void update(boolean newValue) {
        /* Call low-level C method for updating new values through JNI */
        mistNodeApi.updateBool(name, newValue);
    }

    void update(int newValue) {
        mistNodeApi.updateInt(name, newValue);
    }

    void update(double newValue) {
        mistNodeApi.updateFloat(name, newValue);
    }

    void update(String newValue) {
        mistNodeApi.updateString(name, newValue);
    }


}
