package fi.ct.mist.reference;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import fi.ct.mist.referencelibrary.Mist;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private Intent mist;
    Torch torch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Initialise Mist */
        mist = new Intent(this, Mist.class);
        //mist.putExtra("receiver", onConnect);
        torch = new Torch(getBaseContext());

        startService(mist);
    }

    /*
    private ResultReceiver onConnect = new ResultReceiver(new Handler()) {
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.d(TAG, "onConnect");
        }
    };
    */

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
