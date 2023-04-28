package com.safe_bicycle_assistant.s_ba.Services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.safe_bicycle_assistant.s_ba.ActivityAIDL;
import com.safe_bicycle_assistant.s_ba.ConnectionServiceAIDL;

import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.activities.TestActivity;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
public class ConnectionService extends Service {
    String ip;
    int port;
    Thread networkThread;
    ActivityAIDL activityAIDL = null;
    private final String TAG = "s-ba/ConnectionService";
    private final ConnectionServiceAIDL.Stub mConnectionServiceBinder = new ConnectionServiceAIDL.Stub() {
        @Override
        public void testConnection() throws RemoteException {
            Log.d(TAG, "testConnection: ");
        }

        @Override
        public void destroyService() throws RemoteException {
            Log.d(TAG, "destroyService: ");
            networkThread.interrupt();
        }
        @Override
        public void reverseConnection(Bundle bundle) throws RemoteException {
            Log.d(TAG, "reverseConnection: ");
            IBinder binder = bundle.getBinder("key");
            activityAIDL = (ActivityAIDL) binder;
        }

    };
    public ConnectionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: Service bounded");
        return mConnectionServiceBinder;
    }

    @Override
    public void onCreate() {
        this.ip = "210.107.198.230";
        this.port = 33333;
        networkThread = new Thread(new NetworkThreadRunnable(ip,port));
        networkThread.start();
    }

    public class NetworkThreadRunnable implements Runnable{
        int port;
        String ip;
        int BUFFER_SIZE = 100;
        Socket socket;



        public NetworkThreadRunnable(String ip,int port ) {
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
//                    for(int i = 0; i< 4; i++)
//                    {
//                        byte temp;
//                        temp = buffer[i];
//                        buffer[i] = buffer[3-i];
//                        buffer[3-i] = temp;
//                    }
//                    for(int i = 0; i< 4; i++)
//                    {
//                        byte temp;
//                        temp = buffer[i];
//                        buffer[4+i] = buffer[7-i];
//                        buffer[7-i] = temp;
//                    }
                    float cadence = dataInputStream.readFloat();
                    int detection = dataInputStream.readInt();
                    Log.d(TAG, "run: size of Float");
                    Log.d(TAG, "run: cadence " + cadence + " detection : " + detection);
                    Thread.sleep(100);
                    activityAIDL.setTexts(cadence,detection);
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            TextView cadenceView = ((Activity)getApplicationContext()).findViewById(R.id.cadenceTextView);
//                            TextView detectionView = ((Activity)getApplicationContext()).findViewById(R.id.detectionTextView);
//                            cadenceView.setText("Cadence : "+cadence);
//                            detectionView.setText("detection flag : " + detection);
//                        }
//                    });
                }catch (InterruptedException ie)
                {
                    Log.d(TAG, "run: Received Interrupt");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
