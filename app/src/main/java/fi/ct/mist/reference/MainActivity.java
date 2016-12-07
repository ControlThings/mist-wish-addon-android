package fi.ct.mist.reference;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import fi.ct.mist.mistnodeapi.Mist;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private Intent mist;
    FlashLight light;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init Mist
        mist = new Intent(this, Mist.class);

        // Initialize driver (contains Mist endpoints and model)
        light = new FlashLight(getBaseContext());

        // Start Mist service
        startService(mist);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        light.cleanup();
        stopService(mist);
    }
}
