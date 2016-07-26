package tw.com.softworld.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Catherine on 2016/7/19.
 * Soft-World Inc.
 * catherine919@soft-world.com.tw
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MyBluetoothAdapter {
    private static final String TAG = "MyBluetoothAdapter";
    private static final int TIMEOUT = 30000;//milliseconds
    private static final int BLUETOOTH_DISCOVERABLE_DURATION = 600;//seconds
    private static final int TARGET_API = Build.VERSION_CODES.JELLY_BEAN_MR2;
    private static final boolean disableLE = false;

    private Context ctx;
    private Handler handler;
    private Handler timerHandler;

    private BluetoothAdapter bluetoothAdapter;
    private boolean isLEEnable;
    private boolean isRegister;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private ArrayList<BluetoothDevice> bluetoothDevices;


    MyBluetoothAdapter(Context ctx, Handler handler) {
        this.ctx = ctx;
        this.handler = handler;
        bluetoothDevices = new ArrayList<>();
        timerHandler = new Handler();
    }

    /**
     * step1
     * Check for Bluetooth Support, and initializes Bluetooth adapter.
     */
    public void init() {
        if ((Build.VERSION.SDK_INT >= TARGET_API) && !disableLE) {
            BluetoothManager manager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = manager.getAdapter();
        } else
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (bluetoothAdapter == null) {
            sendMessage(ResultCodes.BE_UNAVAILABLE, "Device does not support Bluetooth.", handler);
        } else {
            if (disableLE)
                isLEEnable = false;
            else {
                isLEEnable = isLEEnable();
                if (isLEEnable) {
                    leScanCallback = new BluetoothAdapter.LeScanCallback() {
                        @Override
                        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                            if (!bluetoothDevices.contains(device)) {
                                bluetoothDevices.add(device);
                                sendList(ResultCodes.GOT_DEVICES, bluetoothDevices, handler);
                            }
                        }
                    };
                }
            }
        }
    }

    /**
     * Check for Bluetooth LE Support.
     */
    public boolean isLEEnable() {
        if (ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) && Build.VERSION.SDK_INT >= TARGET_API) {
            sendMessage(ResultCodes.SENT_SUCCESSFULLY, "Device supports Bluetooth LE.", handler);
            return true;
        } else {
            sendMessage(ResultCodes.BE_UNAVAILABLE, "Device does not support Bluetooth LE.", handler);
            return false;
        }

    }

    /**
     * Turn on the local Bluetooth.
     */
    public boolean isBluetoothEnable() {
        //Bluetooth is disabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            //Asking for turning on the bluetooth by popping up a dialog
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            intent.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BLUETOOTH_DISCOVERABLE_DURATION);
            Activity act = (Activity) ctx;
            act.startActivityForResult(intent, ResultCodes.OPEN_BLUETOOTH);

            //Turn on the local Bluetooth adapter without explicit user action to turn on Bluetooth.
//            bluetoothAdapter.enable();
            return false;
        } else
            return true;
    }

    /**
     * Turn off the local Bluetooth.
     */
    public void disableBluetooth() {
        if (bluetoothAdapter != null)
            bluetoothAdapter.disable();
    }

    public void getDeviceInfo() {
        String UUID = "null";
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
            ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);
            for (ParcelUuid uuid : uuids) {
                UUID = uuid.getUuid().toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendMessage(ResultCodes.SENT_SUCCESSFULLY, "name:" + bluetoothAdapter.getName() + "\naddress:" + bluetoothAdapter.getAddress() + "\nuuid:" + UUID, handler);
    }

    /**
     * Start to scan or stop scanning.
     * You can not scan by classic Bluetooth and Bluetooth LE at the same time,
     * if Bluetooth LE is enable, Bluetooth LE is the first.
     *
     * @param enable set true to start ,and set false to stop.
     */
    public void scan(boolean enable) {
        if (isLEEnable) {
            //bluetooth LE
            if (enable) {
                bluetoothAdapter.startLeScan(leScanCallback);

                //Add a timer
                timerHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bluetoothAdapter.stopLeScan(leScanCallback);
                        String msg = "\nDevices:\n";
                        for (BluetoothDevice device : bluetoothDevices) {
                            msg += device.getName() + "  " + device.getAddress() + "\n";
                        }
                        Log.d(TAG, "[LE]");
                        Log.d(TAG, msg);
//                    sendList(ResultCodes.GOT_DEVICES, bluetoothDevices, handler);
                    }
                }, TIMEOUT);
            } else
                bluetoothAdapter.stopLeScan(leScanCallback);
        } else {
            //bluetooth 3.0
            if (enable) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

                isRegister = true;
                ctx.registerReceiver(mReceiver, filter);
                bluetoothAdapter.startDiscovery();
            } else {
                isRegister = false;
                ctx.unregisterReceiver(mReceiver);
                bluetoothAdapter.cancelDiscovery();
            }
        }
    }

    /**
     * Receive scanning result on Bluetooth 3.0
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    Log.d(TAG, "STATE_ON");
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "ACTION_DISCOVERY_STARTED");

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "[classic]ACTION_DISCOVERY_FINISHED");
                String msg = "\nDevices:\n";
                for (BluetoothDevice device : bluetoothDevices) {
                    msg += device.getName() + "  " + device.getAddress() + "\n";
                }
                Log.d(TAG, msg);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!bluetoothDevices.contains(device)) {
                    bluetoothDevices.add(device);
                    sendList(ResultCodes.GOT_DEVICES, bluetoothDevices, handler);
                }
            }
        }
    };

    /**
     * Send messages with handler
     *
     * @param resultCode result
     * @param message    string
     * @param handler    process
     */
    private static void sendMessage(int resultCode, String message,
                                    Handler handler) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("MSG", message);
        msg.setData(bundle);
        msg.what = resultCode;
        handler.sendMessage(msg);
    }

    /**
     * Send messages with handler
     *
     * @param resultCode result
     * @param message    ArrayList
     * @param handler    process
     */
    private static void sendList(int resultCode, ArrayList message,
                                 Handler handler) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("ParcelableArrayList", message);
        msg.setData(bundle);
        msg.what = resultCode;
        handler.sendMessage(msg);
    }

    public void release() {
        if (isRegister)
            ctx.unregisterReceiver(mReceiver);
    }
}
