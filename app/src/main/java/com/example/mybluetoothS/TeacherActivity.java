package com.example.mybluetoothS;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;

import androidx.core.app.ActivityCompat;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

//import com.example.test1.Adapter.DeviceAdapter;

import java.util.List;
import java.util.Set;


public class TeacherActivity extends Activity implements View.OnClickListener,
    OnItemClickListener {
        ProgressBar pbSearchBle;
        // 本地蓝牙设备信息
        private com.example.mybluetoothS.BluetoothDevice bluetoothDevice;

        // Intent request codes
        private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
        private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
        private static final int REQUEST_ENABLE_BT = 3;
        private static final int REQUEST_GET_IMG = 4;

        // Debugging test
        private static final String TAG = BluetoothChat.class.getSimpleName();
        private Button visible,searchButton,chatroomButton;
        private BluetoothAdapter BA;
        private Set<android.bluetooth.BluetoothDevice> pairedDevices;
        private ArrayList<String> mDeviceList = new ArrayList<String>();
        private TextView tv_click_advice;
        private String stu_id = "-1";
        private int time=0;
        private static final int REQUEST_CODE_GPS = 1;
        final long lTimeToGiveUp_ms = System.currentTimeMillis() + 10000;
        String sNewName = "00000000";
        private static int REQUEST_ENABLE_BLUETOOTH = 1;//请求码
        BluetoothAdapter bluetoothAdapter;//蓝牙适配器

        private TextView scanDevices;//扫描设备
        private LinearLayout loadingLay;//加载布局

//    private RxPermissions rxPermissions = new RxPermissions(this);//权限请求
//    闪退

        List<android.bluetooth.BluetoothDevice> deviceList = new ArrayList<>();//数据来源
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
            // Set up the window layout
            setContentView(R.layout.activity_teacher);
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                    R.layout.header_tea); // titlebar为自己标题栏的布局

            visible = (Button)findViewById(R.id.tea_button_get_vis);
//        list = (Button)findViewById(R.id.button_list);
            searchButton = (Button)findViewById(R.id.tea_choose_search);
            chatroomButton = (Button)findViewById(R.id.tea_choose_chatroom);
            tv_click_advice= (TextView)findViewById(R.id.tea_tv_click_advice);

            pbSearchBle=findViewById(R.id.tea_progress_ser_bluetooth);

            setupClick();
            BA = BluetoothAdapter.getDefaultAdapter();
            if (BA == null) {
                // Device doesn't support Bluetooth
                Toast.makeText(getApplicationContext(),"Your device doesn't support Bluetooth",
                        Toast.LENGTH_LONG).show();
            }

        }
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.multi_chat_room, menu);
            return true;
        }
        @Override
        protected void onDestroy() {
            time=0;
            super.onDestroy();
        }
        /*
         * 点击监听
         */
        public void setupClick() {
            Log.i(TAG, ">>setup click<<");

            // 搜素按钮点击监听
            searchButton.setOnClickListener(this);
            // 消息列表按钮点击监听
            chatroomButton.setOnClickListener(this);

        }
        /* 处理点击事件函数 */

        @Override
        public void onClick(View view) {
            // TODO Auto-generated method stub
            Log.i(TAG, "Onclick");
            if (view == searchButton) {
                Log.i(TAG, "search");
                Intent serverIntent = null;
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, com.example.mybluetoothS.DevicesListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return;
            }
            if (view == chatroomButton) {
                Log.i(TAG, "youclick");
//            Toast.makeText(getApplicationContext(),"you click",
//                    Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setClass(TeacherActivity.this, BluetoothChat.class);
                startActivity(intent);
            }
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            Intent serverIntent = null;
            switch (item.getItemId()) {
                case R.id.startService:
                    // Start bluetooth service
                    Intent enableIntent = new Intent(
                            bluetoothDevice.Action_Bluetooth_On);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    return true;
                case R.id.secure_connect_scan:
                    // Launch the DeviceListActivity to see devices and do scan
                    serverIntent = new Intent(this, com.example.mybluetoothS.DevicesListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                    return true;
                case R.id.insecure_connect_scan:
                    // Launch the DeviceListActivity to see devices and do scan
                    serverIntent = new Intent(this, com.example.mybluetoothS.DevicesListActivity.class);
                    startActivityForResult(serverIntent,
                            REQUEST_CONNECT_DEVICE_INSECURE);
                    return true;
                case R.id.discoverable:
                    // Ensure this device is discoverable by others
                    ensureDiscoverable();
                    return true;
            }
            return false;
        }

        public void ensureDiscoverable() {
            Log.i(TAG, ">>ensure discoverable<<");
            if (bluetoothDevice.getBluetoothScanMode() != bluetoothDevice.Mode_Connectable_Discoverable) {
                Intent discoverableIntent = new Intent(
                        bluetoothDevice.Action_Bluetooth_Discoverable);
                discoverableIntent.putExtra(bluetoothDevice.Data_Dicoverable, 300);
                startActivity(discoverableIntent);
            }
        }
        /**
         *打开蓝牙
         *
         * @return
         */
        public void openBluetooth(View view){
            if (!BA.isEnabled()) {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, 0);
                Toast.makeText(getApplicationContext(),"Turned on"
                        ,Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(),"Already on",
                        Toast.LENGTH_LONG).show();
            }
            openLocation();
            tv_click_advice.setVisibility(View.INVISIBLE);
        }
        /**
         *打开位置权限
         *
         * @return
         */
        private void openLocation() {
//        Toast.makeText(getApplicationContext(),"请打开位置权限",
//                Toast.LENGTH_LONG).show();
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

            // https://developer.android.google.cn/guide/components/intents-filters?hl=zh-cn#ExampleSend
            // https://developer.android.google.cn/reference/android/content/Intent?hl=zh-cn#resolveActivity(android.content.pm.PackageManager)
            // 判断是否有合适的应用能够处理该 Intent，并且可以安全调用 startActivity()。
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_GPS);
            } else {
                Toast.makeText(TeacherActivity.this, "该设备不支持位置服务", Toast.LENGTH_SHORT).show();
            }
            Log.i("BTlist", "permission get");
        }
        /**
         *输入蓝牙名
         *
         * @return
         */
        public void inputName(View view){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Your Student ID");

// Set up the input
            final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
            builder.setView(input);

// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    stu_id = input.getText().toString();
                    sNewName = stu_id;
                    TextView tv_stu_id = (TextView) findViewById(R.id.stu_id);
                    tv_stu_id.setText("Your ID: " + stu_id);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
        /**
         *获取位置权限
         *
         * @return
         */
        private void permissionRequire() {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Log.i("BTlist", "permission get");
        }

        /**
         *修改蓝牙名
         *
         * @return
         */
        public void change(View view){
            Log.i("BTchange", "start change");
            if (BA != null)
            {
                Log.i("BTchange", "BA exist");
                String sOldName = BA.getName();
                if (sOldName.equalsIgnoreCase(sNewName) == false)
                {
                    final Handler myTimerHandler = new Handler();
                    BA.enable();
                    myTimerHandler.postDelayed(
                            new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Log.i("BTchange", "start run");
                                    if (BA.isEnabled())
                                    {
                                        BA.setName(sNewName);
                                        Log.i("BTchange", "start set");
                                        if (sNewName.equalsIgnoreCase(BA.getName()))
                                        {
                                            Log.i("BTchange", "Updated BT Name to " + BA.getName());
                                            Toast.makeText(getApplicationContext(),"new name" + BA.getName(),
                                                    Toast.LENGTH_LONG).show();
//                                        BA.disable();
                                        }
                                    }
                                    Log.i("BTchange", "Waiting1...");
                                    if ((sNewName.equalsIgnoreCase(BA.getName()) == false) && (System.currentTimeMillis() < lTimeToGiveUp_ms))
                                    {
                                        Log.i("BTchange", "Waiting2...");
                                        myTimerHandler.postDelayed(this, 500);
                                        if (BA.isEnabled())
                                            Log.i("BTchange", "Update BT Name: waiting on BT Enable");
                                        else
                                            Log.i("BTchange", "Update BT Name: waiting for Name (" + sNewName + ") to set in");
                                    }
                                }
                            } , 500);
                }
            }
        }
        public void visible(View view){
            Intent getVisible = new Intent(BluetoothAdapter.
                    ACTION_REQUEST_DISCOVERABLE);
            startActivityForResult(getVisible, 0);

        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
}
