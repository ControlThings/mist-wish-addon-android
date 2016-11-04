package fi.ct.mist.reference;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import fi.ct.mist.referencelibrary.Mist;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private Intent mist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mist = new Intent(this, Mist.class);
        mist.putExtra("receiver", onConnect);
        startService(mist);
    }

    private ResultReceiver onConnect = new ResultReceiver(new Handler()) {
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Torch torch = new Torch(getBaseContext());
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
