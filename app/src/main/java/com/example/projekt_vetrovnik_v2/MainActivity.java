package com.example.projekt_vetrovnik_v2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView listView;
    //Broadcast Reciever -> Action Found
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED))
            {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,mBluetoothAdapter.ERROR);
                switch (state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"OnReceive: State OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG,"OnReceive: Turning OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"OnReceive: Turning ON");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"OnReceive: State ON");
                        break;
                }

            }
        }
    };
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,mBluetoothAdapter.ERROR);
                switch (mode)
                {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG,"mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG,"mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG,"mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG,"mBroadcastReceiver2: Conecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG,"mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };
    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG,"Action Found");
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG,"OnReceive: "+device.getName()+": "+device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context,R.layout.device_adapter_view_prava,mBTDevices);
                listView.setAdapter(mDeviceListAdapter);


                }

            }
    };


@Override
protected void onDestroy(){
    Log.d(TAG,"On Destroy:called");
    super.onDestroy();
    unregisterReceiver(mBroadcastReceiver1);
}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button onoffbutton=(Button) findViewById(R.id.onoffbutton);
        mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        Button buttonVisible=(Button) findViewById(R.id.buttonVisible);
        listView = (ListView) findViewById(R.id.tvDeviceName);
        mBTDevices = new ArrayList<>();


        onoffbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"On CLick: enabling/disabling BT");
                ena_dis_BT();
            }
        });

        buttonVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"btVISIBle: Making device discoverable for 300 seconds.");
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
                startActivity(discoverableIntent);

                IntentFilter intentFilter= new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                registerReceiver(mBroadcastReceiver2,intentFilter);
            }
        });
    }
    public void ena_dis_BT()
    {
        if(mBluetoothAdapter==null)
        {
            Log.d(TAG,"EnableDisableBT:Does not have BT capability");
        }
        if(!mBluetoothAdapter.isEnabled())
        {
            Log.d(TAG,"Enable BT.");
            Intent enableBTintent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTintent);
            IntentFilter BTintent= new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1,BTintent);
        }
        if(mBluetoothAdapter.isEnabled()) {
            Log.d(TAG,"Disable BT.");
            mBluetoothAdapter.disable();
            IntentFilter BTintent= new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1,BTintent);
        }
    }

    public void buttonDiscover(View view) {
        Log.d(TAG,"Btn Discover: Looking for unpaired devices.");
        if (mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG,"Btn Discover: Canceling discovery.");
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3,discoverDevicesIntent);

        }
        if (!mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG,"Btn Discover: Starting discovery.");

            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3,discoverDevicesIntent);

        }
    }
    private void checkBTPermissions()
    {
               /*
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            int permissionCheck =checkSelfPermission(this,Manifest.permission.ACCES_FINE_LOCATION);

            if (permissionCheck !=0)
            {
                int permissionCheck1 = this.checkSelfPermission("Manifest.Permission.ACCES_COARSE_LOCATION");
                if (permissionCheck1 != 0) {
                    this.requestPermissions(new String[], {Manifest.permission.ACCESS_FINE_LOCATION});
                } else {
                    Log.d(TAG, "CHECK BTPermissions: No need to check permissions. SDK version < LOLLIPOP");
                }
            }
        };*/
    }
}
