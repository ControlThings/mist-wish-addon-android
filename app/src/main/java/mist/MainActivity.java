package mist;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import fi.ct.mist.reference.R;


// implements mist receiver
public class MainActivity extends AppCompatActivity implements MistReceiver.Receiver {

    private final String TAG = "MainActivity";

    private Intent mist;
    FlashLight light;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Resopnse receiver for mist service (onConnected)
        MistReceiver mistReceiver = new MistReceiver(this);

        // Init Mist
        mist = new Intent(this, Mist.class);
        mist.putExtra("receiver", mistReceiver);

        // Start Mist service
        startService(mist);
    }

    // Mist is runing and connected to wish
    @Override
    public void onConnected() {
        light = new FlashLight(getBaseContext());
        /*
        Test t = new Test();
        t.run();*/
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //light.cleanup();
        stopService(mist);
    }
}
