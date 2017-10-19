package mist.node;

/**
 * Created by jan on 11/1/16.
 */

class Endpoint {
    static final int MIST_TYPE_BOOL = 0;
    static final int MIST_TYPE_FLOAT = 1;
    static final int MIST_TYPE_INT = 2;
    static final int MIST_TYPE_STRING = 3;
    /* ... */

    private MistNode mistNode;

    private Endpoint next;

    public Endpoint getNext() {
        return next;
    }

    private Endpoint firstChild;

    /** Note: this is accessed from JNI code */
    private Endpoint parent;

    public Endpoint getFirstChild() { return firstChild; }

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
        mistNode = MistNode.getInstance();
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
        invokeCallback = i;
    }

    public interface Invokable {
        byte[] invoke(byte[] args);
    }

    void update(boolean newValue) {
        /* Call low-level C method for updating new values through JNI */
        mistNode.updateBool(id, newValue);
    }

    void update(int newValue) {
        mistNode.updateInt(id, newValue);
    }

    void update(double newValue) {
        mistNode.updateFloat(id, newValue);
    }

    void update(String newValue) {
        mistNode.updateString(id, newValue);
    }


}
