package tw.com.softworld.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Catherine on 2016/7/19.
 * Soft-World Inc.
 * catherine919@soft-world.com.tw
 */
public class DevicesAdapter extends BaseAdapter {
    private Context ctx;
    private List<BluetoothDevice> bluetoothDevices;
    private List<BluetoothDevice> temp;

    DevicesAdapter(Context ctx, List<BluetoothDevice> bluetoothDevices) {
        this.ctx = ctx;
        this.bluetoothDevices = bluetoothDevices;
        temp = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return bluetoothDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return bluetoothDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getAddress(int position) {
        return bluetoothDevices.get(position).getAddress();
    }

    public String getName(int position) {
        return bluetoothDevices.get(position).getName();
    }

    @Override
    public void notifyDataSetChanged() {
        temp.clear();
        temp.addAll(bluetoothDevices);
        bluetoothDevices.clear();
        bluetoothDevices.addAll(temp);

        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewTag viewTag;
        Log.d("adapter", "getView " + position);
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.adapter_device, null);

            viewTag = new ViewTag();
            viewTag.name = (TextView) convertView.findViewById(
                    R.id.tv_title);
            viewTag.address = (TextView) convertView.findViewById(
                    R.id.tv_subtitle);

            convertView.setTag(viewTag);
        } else {
            viewTag = (ViewTag) convertView.getTag();
        }
        String title;
        if (getName(position) == null)
            title = "null";
        else
            title = getName(position);
        viewTag.name.setText(title);
        viewTag.address.setText(getAddress(position));

        return convertView;
    }


    class ViewTag {
        TextView name;
        TextView address;
    }
}
