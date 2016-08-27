package com.example.michalszyszka.autify;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static android.bluetooth.BluetoothAdapter.*;

/**
 * Created by michalszyszka on 12.03.2016.
 */
public class BTConnectionReceiver extends BroadcastReceiver {

    private static final String CarBTName = "KIA MOTORS";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)){

            int currentConnectionState = intent.getIntExtra(EXTRA_CONNECTION_STATE, 0);
            BluetoothDevice device = intent.getParcelableExtra(
                    BluetoothDevice.EXTRA_DEVICE);

            String deviceName = device != null ? device.getName() : "";
            String connectionState = "";

            if(CarBTName.equals(deviceName)){

                switch (currentConnectionState){
                    case 0:
                        connectionState = "disconnected";
                        break;
                    case 1:
                        connectionState = "connecting";
                        break;
                    case 2:
                        connectionState = "connected";

                        Intent activityIntent = new Intent(context, MainActivity.class);
                        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        context.startActivity(activityIntent);

                        break;
                    default:
                        connectionState = "unknown";
                        break;
                }
            }

            Log.d(this.getClass().getSimpleName(), "state: " + connectionState + " device: " + deviceName);
        }
    }
}
