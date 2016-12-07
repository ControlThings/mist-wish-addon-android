package fi.ct.mist.reference;

import android.content.Intent;
import android.content.res.Configuration;
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
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mist = new Intent(this, Mist.class);
        torch = new Torch(getBaseContext());
        startService(mist);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        torch.cleanup();
        stopService(mist);
    }
}
