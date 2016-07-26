package tw.com.softworld.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

import tw.com.softworld.messagescenter.AsyncResponse;
import tw.com.softworld.messagescenter.ErrorMessages;
import tw.com.softworld.messagescenter.Server;

/**
 * Created by Catherine on 2016/7/19.
 * Soft-World Inc.
 * catherine919@soft-world.com.tw
 */
public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    //content
    private FragmentTabHost tabHost;
    private ViewPager mainContent;

    private Class[] classes = {BluetoothInfo.class, BluetoothScanner.class};
    private String[] tags = {"INFO", "SCAN"};
    private String[] titles = {"INFO", "SCAN"};
    private BluetoothInfo FragmentInfo;
    private BluetoothScanner FragmentScanner;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initEvent();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabHost = (FragmentTabHost) findViewById(R.id.tab_host);
        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        //Change the color of tab divider or you just set null
        tabHost.getTabWidget().setDividerDrawable(R.color.colorPrimary);
        mainContent = (ViewPager) findViewById(R.id.vp);

        for (int i = 0; i < tags.length; i++) {
            TabHost.TabSpec newTabSpec = tabHost.newTabSpec(titles[i]);
            //Set the tab label
            newTabSpec.setIndicator(tags[i]);
            tabHost.addTab(newTabSpec, classes[i], null);
        }

        tabHost.setCurrentTab(0);
        mainContent.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        if (FragmentInfo == null)
                            FragmentInfo = new BluetoothInfo();
                        return FragmentInfo;
                    case 1:
                        if (FragmentScanner == null)
                            FragmentScanner = new BluetoothScanner();
                        return FragmentScanner;
                    default:
                        if (FragmentInfo == null)
                            FragmentInfo = new BluetoothInfo();
                        return FragmentInfo;
                }

            }

            @Override
            public int getCount() {
                return tags.length;
            }
        });
    }

    private void initEvent() {
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                mainContent.setCurrentItem(tabHost.getCurrentTab());
            }
        });
        mainContent.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabHost.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ResultCodes.OPEN_BLUETOOTH) {
            switch (resultCode) {
                case 600:
                    Log.d(TAG, "OK");
                    //Ask BluetoothInfo fragment to doing something.
                    AsyncResponse ar = new AsyncResponse() {
                        @Override
                        public void onFailure(int errorCode) {
                            switch (errorCode) {
                                case ErrorMessages.NULL_POINTER:
                                    //Action names or messages are null.
                                    Log.e(TAG, "MULTIPLE_VALUE");
                                    break;
                                case ErrorMessages.MULTIPLE_VALUE:
                                    //You can't send multiple types of broadcast messages with same actionName at the same time, You need to rename this action.
                                    Log.e(TAG, "NULL_POINTER");
                                    break;
                            }
                        }
                    };

                    Server sv = new Server(MainActivity.this, ar);
                    sv.pushString("OPEN_BLUETOOTH", "open");

                    break;
                case Activity.RESULT_CANCELED:
                    Log.d(TAG, "CANCEL");
                    break;
            }

        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}
