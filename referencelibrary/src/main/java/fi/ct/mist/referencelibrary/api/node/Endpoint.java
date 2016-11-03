package fi.ct.mist.referencelibrary.api.node;


public class Endpoint {

    public static final int bool = 1;
    public static final int integer = 2;
    public static final int _float = 3;

    private boolean dataBoolean;
    private int dataInt;
    private float dataFloat;

    private boolean updateBoolean;
    private int updateInt;
    private float updateFloat;

    private int _type;
    private String typeAsString;

    String _name;
    String _lable = null;
    String _unit = null;
    boolean _read = false;
    boolean _write = false;
    boolean _invoke = false;

    Singnals _singnal;
    Callback _callback;
    private boolean flag = false;

    public Endpoint(String epName, int type) {
        this._name = epName;
        this._type = type;
        switch (type) {
            case bool:
                typeAsString = "bool";
                return;
            case integer:
                typeAsString = "integer";
                return;
            case _float:
                typeAsString = "float";
                return;
        }
    }

    public void readable() {
        _read = true;
    }

    public void writable() {
        _write = true;
    }

    public void invokable() {
        _invoke = true;
    }

    public void label(String label) {
        this._lable = label;
    }

    public void unit(String unit) {
        this._unit = unit;
    }

    public int getType() {
        return _type;
    }

    public String getTypeAsString() {
        return typeAsString;
    }

    public String getName() {
        return _name;
    }

    public int getInt() {
        return dataInt;
    }

    public boolean getBoolean() {
        return dataBoolean;
    }

    public float getFloat() {
        return dataFloat;
    }

    public void setInt(int data) {
        if (this.dataInt != data) {
            this.dataInt = data;
            callback();
        }
    }

    public void setBoolean(boolean data) {
        if (this.dataBoolean != data) {
            this.dataBoolean = data;
            callback();
        }
    }

    public void setFloat(float data) {
        if (this.dataFloat != data) {
            this.dataFloat = data;
            callback();
        }
    }

    public void updateInt(int data) {
            this.updateInt = data;
            onValuChanged();
    }

    public void updateBoolean(boolean data) {
            this.updateBoolean = data;
            onValuChanged();
    }

    public void updateFloat(float data) {
            this.updateFloat = data;
            onValuChanged();
    }

    public boolean getUpdateBoolean() {
        return updateBoolean;
    }

    public int getUpdateInt() {
        return updateInt;
    }

    public float getUpdateFloat() {
        return updateFloat;
    }

    private void callback() {
        _callback.cb();
    }

    private void onValuChanged() {
        _singnal.onValuChanged();
    }

    //to app
    public void registerCallback(Callback callback) {
        _callback = callback;
    }

    public interface Callback {
        public void cb();
    }

    //to device
    public void registerSignal(Singnals singnal) {
        _singnal = singnal;
    }


    public interface Singnals {
        public void onValuChanged();
    }
}
