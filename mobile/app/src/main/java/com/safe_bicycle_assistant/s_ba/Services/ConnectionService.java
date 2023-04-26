package com.safe_bicycle_assistant.s_ba.Services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.activities.TestActivity;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;

public class ConnectionService extends Service {
    String ip;
    int port;
    private final String TAG = "s-ba/ConnectionService";
    public ConnectionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: Service bounded");
        return null;
    }

    @Override
    public void onCreate() {
        this.ip = "210.107.198.230";
        this.port = 33333;
        NetworkThread networkThread = new NetworkThread(ip,port);
        networkThread.start();
    }
    public class NetworkThread extends Thread {
        int port;
        String ip;
        int BUFFER_SIZE = 100;
        Socket socket;
        public NetworkThread(String ip,int port ) {
            this.ip = ip;
            this.port = port;
        }
        @Override
        public void run() {
            try{
            socket = new Socket(ip,port);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG,"Socket Connected");
            while(true) {
                try {
                    InputStream inputStream = socket.getInputStream();
                    byte[] buffer = new byte[BUFFER_SIZE];
                    inputStream.read(buffer);
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
                    DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
                    
//                    float cadence = dataInputStream.readFloat();
//                    int detection = dataInputStream.readInt();
//                    Log.d(TAG, "run: cadence " + cadence + " detection : " + detection);
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            TextView cadenceView = ((Activity)getApplicationContext()).findViewById(R.id.cadenceTextView);
//                            TextView detectionView = ((Activity)getApplicationContext()).findViewById(R.id.detectionTextView);
//                            cadenceView.setText("Cadence : "+cadence);
//                            detectionView.setText("detection flag : " + detection);
//                        }
//                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
