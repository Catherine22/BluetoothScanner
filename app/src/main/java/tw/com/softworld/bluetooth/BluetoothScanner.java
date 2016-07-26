package tw.com.softworld.bluetooth;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by Catherine on 2016/7/19.
 * Soft-World Inc.
 * catherine919@soft-world.com.tw
 */
public class BluetoothScanner extends Fragment {

    private View mRootView;
    private TextView info;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_bluetooth_scanner, container, false);
            info = (TextView) mRootView.findViewById(R.id.tv_info);
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        return mRootView;
    }
}