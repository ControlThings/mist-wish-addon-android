package fi.ct.mist.referencelibrary.api.mistNode;

/**
 * Created by jan on 11/1/16.
 */

class MistNodeApi {

    private MistNodeApi() {}


    private static class MistNodeApiHolder {
        private static final MistNodeApi INSTANCE = new MistNodeApi();
    }

    public static MistNodeApi getInstance() {
        return MistNodeApiHolder.INSTANCE;
    }

    native void updateBool(String epName, boolean newValue);
    native void updateInt(String epName, int newValue);
    native void updateString(String epName, String newValue);
    native void updateFloat(String epName, double newValue);
    native void addEndpoint(Endpoint ep);
    native void startMistApp(String appName);


}
