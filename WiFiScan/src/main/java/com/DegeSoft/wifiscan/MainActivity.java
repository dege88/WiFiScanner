package com.DegeSoft.wifiscan;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.content.BroadcastReceiver;
import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import android.net.wifi.ScanResult;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.net.wifi.WifiManager;
import android.widget.ShareActionProvider;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    StringBuilder sb = new StringBuilder();
    private final Handler handler = new Handler();

    ArrayList<String> FiMACAddresses = new ArrayList<String>();
    ArrayList<String> FiSSIDs = new ArrayList<String>();

    private ShareActionProvider mShareActionProvider;
    Intent sendIntent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        if(mainWifi.isWifiEnabled()==false)
        {
            mainWifi.setWifiEnabled(true);
        }

        doInback();
    }

    public void doInback()
    {
        handler.postDelayed(new Runnable() {

            @Override
            public void run()
            {
                mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

                receiverWifi = new WifiReceiver();
                registerReceiver(receiverWifi, new IntentFilter(
                        WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                mainWifi.startScan();
                doInback();
            }
        }, 1000);

    }

    @Override
    protected void onPause()
    {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        // Return true to display menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_reset) {
            FiMACAddresses.clear();
            FiSSIDs.clear();
            TextView textView = (TextView) findViewById(R.id.section_text);
            textView.setText("");
            return true;
        }
        else if(id == R.id.menu_item_share)
        {
            startActivity(sendIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }
    }

    class WifiReceiver extends BroadcastReceiver
    {
        public void onReceive(Context c, Intent intent)
        {
            boolean changed = false;
            List<ScanResult> wifiList;
            wifiList = mainWifi.getScanResults();
            for(int i = 0; i < wifiList.size(); i++)
            {
                if(!FiMACAddresses.contains(wifiList.get(i).BSSID))
                {
                    changed = true;
                    FiMACAddresses.add(wifiList.get(i).BSSID);
                    FiSSIDs.add(wifiList.get(i).SSID);
                }
            }
            if(changed)
            {
                String temp = "";
                TextView textView = (TextView) findViewById(R.id.section_text);
                for(int i = 0; i < FiMACAddresses.size();i++)
                {
                    temp = temp + FiMACAddresses.get(i) + " ";
                    temp = temp + FiSSIDs.get(i) + System.getProperty("line.separator");
                }
                textView.setText(temp);
                sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, temp);
                sendIntent.setType("text/plain");
                setShareIntent(sendIntent);
            }


        }

    }

}
