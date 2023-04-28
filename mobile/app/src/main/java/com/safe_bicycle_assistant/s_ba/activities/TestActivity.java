package com.safe_bicycle_assistant.s_ba.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

//import com.safe_bicycle_assistant.s_ba.ConnectionServiceAIDL;
import com.safe_bicycle_assistant.s_ba.ActivityAIDL;
import com.safe_bicycle_assistant.s_ba.ConnectionServiceAIDL;
import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.Services.ConnectionService;

public class TestActivity extends AppCompatActivity {
    private final String TAG = "s-ba/TestActivity";
    TextView cadenceTextView;
    TextView detectionTextView;
//    com.safe_bicycle_assistant.s_b
    ConnectionServiceAIDL mConnectionServiceAIDL = null;
    private final IBinder ActivityBinder = new ActivityAIDL.Stub() {
        @Override
        public void testConnection() throws RemoteException {
            Log.d(TAG, "testConnection: ");
        }
        @Override
        public void setTexts(float cadence, int detection) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cadenceTextView.setText("Cadence : " + cadence + " RPM");
                    detectionTextView.setText("Detection bit : " + detection);
                }
            });

        }

    };
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: ");
            mConnectionServiceAIDL = ConnectionServiceAIDL.Stub.asInterface(iBinder);
            Bundle bundle = new Bundle();
            bundle.putBinder("key",ActivityBinder);
            try {
                mConnectionServiceAIDL.reverseConnection(bundle);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: ");
            mConnectionServiceAIDL = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        cadenceTextView = findViewById(R.id.cadenceTextView);
        detectionTextView = findViewById(R.id.detectionTextView);
        Intent intent = new Intent(getApplicationContext(), ConnectionService.class);
        Bundle bundle = new Bundle();

        bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        try {
            mConnectionServiceAIDL.destroyService();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
