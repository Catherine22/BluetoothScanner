package tw.com.softworld.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import tw.com.softworld.messagescenter.Client;
import tw.com.softworld.messagescenter.CustomReceiver;
import tw.com.softworld.messagescenter.Result;


/**
 * Created by Catherine on 2016/7/19.
 * Soft-World Inc.
 * catherine919@soft-world.com.tw
 */
public class BluetoothInfo extends Fragment {
    private static final String TAG = "BluetoothInfo";
    private MyBluetoothAdapter myBluetoothAdapter;
    private View mRootView;
    private TextView info;
    private ListView devices;
    private DevicesAdapter adapter;
    private List<BluetoothDevice> bluetoothDevices;
    private BluetoothDevice device;
    private Client client;


    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_bluetooth_info, container, false);
            info = (TextView) mRootView.findViewById(R.id.tv_info);
            devices = (ListView) mRootView.findViewById(R.id.lv_devices);
            devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "touch " + position);
                    //connect
                    device = bluetoothDevices.get(position);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Address:" + device.getAddress());
                    sb.append("\nBondState:" + device.getBondState());
                    sb.append("\nType:" + device.getType());
//                    ParcelUuid[] uuids = device.getUuids();

                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle(device.getName());
                    dialog.setMessage(sb);
                    dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.setNegativeButton("CONNECT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialog.show();
                }
            });
            if (myBluetoothAdapter == null) {
                myBluetoothAdapter = new MyBluetoothAdapter(getActivity(), bluetoothHandler);
                myBluetoothAdapter.init();

                if (myBluetoothAdapter.isBluetoothEnable()) {
                    myBluetoothAdapter.getDeviceInfo();
                    myBluetoothAdapter.scan(true);
                }
            }

            CustomReceiver cr = new CustomReceiver() {
                @Override
                public void onBroadcastReceive(Result result) {
                    //Use getInt(), getBundle()...ect, depending on what your server side sends
                    String messages = result.getString();
                    Log.d(TAG, "I got:" + messages);
                    if (myBluetoothAdapter.isBluetoothEnable()) {
                        myBluetoothAdapter.getDeviceInfo();
                        myBluetoothAdapter.scan(true);
                    }
                }
            };
            client = new Client(getActivity(), cr);
            client.gotMessages("OPEN_BLUETOOTH");

        }
        return mRootView;
    }


    /**
     * handler for send thread
     */
    private Handler bluetoothHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            Bundle bundle;
            String message;
            switch (msg.what) {
                case ResultCodes.BE_UNAVAILABLE:
                    bundle = msg.getData();
                    message = bundle.getString("MSG");
                    info.append(message + "\n");
                    break;
                case ResultCodes.SENT_SUCCESSFULLY:
                    bundle = msg.getData();
                    message = bundle.getString("MSG");
                    info.append(message + "\n");
                    break;
                case ResultCodes.FAILED_TO_SEND:
                    bundle = msg.getData();
                    message = bundle.getString("MSG");
                    info.append(message + "\n");
                    break;
                case ResultCodes.SEND_TIMEOUT:
                    bundle = msg.getData();
                    message = bundle.getString("MSG");
                    info.append(message + "\n");
                    break;
                case ResultCodes.GOT_DEVICES:
                    bundle = msg.getData();
                    bluetoothDevices = bundle.getParcelableArrayList("ParcelableArrayList");
                    if (adapter == null) {
                        adapter = new DevicesAdapter(getActivity(), bluetoothDevices);
                        devices.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    @Override
    public void onResume() {
//        myBluetoothAdapter.isBluetoothEnable();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        myBluetoothAdapter.release();
        client.release();
        super.onDestroy();
    }
}