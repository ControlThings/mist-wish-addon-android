package fi.ct.mist.reference;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import fi.ct.mist.referencelibrary.Mist;
import fi.ct.mist.referencelibrary.api.node.Endpoint;
import fi.ct.mist.referencelibrary.api.node.NodeApi;
import fi.ct.mist.referencelibrary.bridge.BridgeConnection;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";


    private Intent mist;
    NodeApi nodeApi;
    private Mist.ApiHandler _apiHandler;
    boolean apiBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");
        mist = new Intent(this, Mist.class);

        mist.putExtra("receiver", onConnect);

        startService(mist);


        //  boolean bind = bindService(mist, connection, Context.BIND_AUTO_CREATE);
        //  Log.d(TAG, "onCreate state: " + bind);
    }

    private ResultReceiver onConnect = new ResultReceiver(new Handler()) {
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            //process results
            Log.d(TAG, "connected");
            Torch torch = new Torch(getBaseContext());
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
       /*     if (apiBound) {
                _apiHandler.close();
            }
            unbindService(connection);
            stopService(mist);*/
    }
}
