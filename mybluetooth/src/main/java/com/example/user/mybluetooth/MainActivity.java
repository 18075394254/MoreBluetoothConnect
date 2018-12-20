package com.example.user.mybluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import application.MyApplication;
import model.BlueInfo;
import thread.AcceptThread;
import thread.ConnectThread;
import utils.BlueUtils;


public class MainActivity extends AppCompatActivity {
    BluetoothAdapter mBluetoothAdapter = null ;
    HomeAdapter mHomeAdapter = null;
    private ArrayList<BlueInfo> infolist = new ArrayList<>();
    private Button btn_discovery;
    private RecyclerView mRecyclerView;
    private EditText sendText;
    private Button btn_send;
    private TextView mTextView;
    private ArrayList<ConnectThread> mListThreads = new ArrayList<>();
    int bluePosition = 0;
    boolean connectFlag = false;
    AcceptThread acceptThread;
    private ConnectThread thread;
    //表示当前是设备几
    int currentFlag = 0;
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int flag = msg.what;
           // BluetoothDevice device = (BluetoothDevice) msg.obj;

            if (flag == 12345){
                int arg = msg.arg1;

                connectFlag = true;
                mListThreads.add((ConnectThread) msg.obj);
                infolist.get(bluePosition).setState("已连接");
                mHomeAdapter.notifyItemChanged(arg);
               // Toast.makeText(MainActivity.this,"蓝牙 "+device+" 连接上了",Toast.LENGTH_SHORT).show();
            }else{
                String message = (String) msg.obj;
                mTextView.append("设备"+flag+"发送消息："+message+"\r\n");
            }


        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_discovery = (Button) findViewById(R.id.btn_discovery);
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        sendText = (EditText) findViewById(R.id.editText);
        btn_send = (Button) findViewById(R.id.sendBtn);
        mTextView = (TextView) findViewById(R.id.textView);

        openBlue();
        canDiscovered();
        registerBroadcast();
       /* acceptThread = new AcceptThread(mBluetoothAdapter,mHandler);
        acceptThread.start();*/
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mHomeAdapter = new HomeAdapter(infolist);
        mRecyclerView.setAdapter(mHomeAdapter);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.HORIZONTAL));

        //搜索按钮的点击事件
        btn_discovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infolist.clear();
                //安卓6.0蓝牙搜索还要动态设置模糊定位权限
                requestBluetoothPermission();
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this,"发送"+sendText.getText().toString(),Toast.LENGTH_SHORT).show();
                long start;
                long end;
                if (mListThreads.size() > 0 ){
                    for (int i = 0;i < mListThreads.size();i++){
                        start = System.currentTimeMillis();
                        Log.i("MainActivity","发送数据时间为 = "+ start);
                        Log.i("MainActivity","mListThreads.size() = "+ mListThreads.size());
                        BlueUtils.sendMessage(sendText.getText().toString(),mListThreads.get(i).getsocket());
                        end = System.currentTimeMillis();


                    }
                }else{
                    Toast.makeText(MainActivity.this,"没有蓝牙设备连接",Toast.LENGTH_SHORT).show();

                }
               /* if (connectFlag){
                    acceptThread.sendMessage(sendText.getText().toString());
                    //BlueUtils.sendMessage(sendText.getText().toString(),thread.getsocket());
                }else{
                    Toast.makeText(MainActivity.this,"没有蓝牙设备连接",Toast.LENGTH_SHORT).show();
                }*/
            }
        });

        //列表中按钮的点击事件
        mHomeAdapter.setOnItemClickLitener(new HomeAdapter.OnItemClickLitener()
        {

            @Override
            public void onItemClick(View view, int position)
            {
                //判断是否在搜索，是的话就取消搜索
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                bluePosition = position;

                if (infolist.get(position).getState().equals("已连接")){
                    Toast.makeText(MainActivity.this, "该蓝牙已经连接过了！",
                            Toast.LENGTH_SHORT).show();
                }else {
                    String deviceAddress = infolist.get(position).getAddress();
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                    pairDevice(device);
                    thread = new ConnectThread(device, mBluetoothAdapter, mHandler, bluePosition);
                    thread.start();
                }
            }

        });


    }



    private void openBlue(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //表示不支持蓝牙功能
        if (mBluetoothAdapter != null){
            return;
        }
        //蓝牙功能未打开时打开蓝牙
        if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
    }

    private void closeBlue(){
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON){
            mBluetoothAdapter.disable();
        }
    }

    //使本机蓝牙在300秒内可被发现
    private void canDiscovered(){
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            discoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);//设置被发现的时间
            startActivity(discoverIntent);
        }
    }



    //注册广播接收器
    private void registerBroadcast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver,intentFilter);

        //监听系统蓝牙状态的广播
        IntentFilter filter1=new IntentFilter();
        filter1.addAction("android.bluetooth.device.action.ACL_CONNECTED");
        filter1.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        MainActivity.this.registerReceiver(connectReceiver, filter1);

    }
    //监听系统的蓝牙断开信息
    private final BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        public void onReceive (Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                connectFlag = true;
                Toast.makeText(MyApplication.getContext(), "连接到蓝牙"+device+" 上 ", Toast.LENGTH_SHORT).show();
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Log.d("aaa", " ACTION_ACL_DISCONNECTED");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(MyApplication.getContext(), device.getName()+"蓝牙断开连接", Toast.LENGTH_SHORT).show();

                if (infolist.size() > 0){
                    for (int i =0 ;i < infolist.size();i++){
                        if(infolist.get(i).getAddress().equals(device.getAddress())){
                            infolist.get(bluePosition).setState("已断开");
                            mHomeAdapter.notifyItemChanged(bluePosition);
                        }

                    }
                }
                if (mListThreads.size() > 0){
                    for (int i =0 ;i < mListThreads.size();i++){
                        if(mListThreads.get(i).getDevice().getAddress().equals(device.getAddress())){
                            mListThreads.get(i).cancel();
                            mListThreads.remove(i);
                            break;
                        }

                    }
                }
                /*if (acceptThread.checkBlueNum()){
                    connectFlag = true;
                }else{
                    connectFlag = false;
                }*/

            }
        }

    };
    //广播
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(MainActivity.this,"发现设备"+device.getName(),Toast.LENGTH_SHORT).show();
                infolist.add(new BlueInfo(device.getName(),device.getAddress(),"未连接"));
                mHomeAdapter.notifyDataSetChanged();
                // 搜索完成时
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                    Toast.makeText(MainActivity.this, "搜索完成，点击连接蓝牙", Toast.LENGTH_SHORT).show();
                    btn_discovery.setText("搜索完成");
            }
        }
    };

    //设备的配对
    private void pairDevice(BluetoothDevice device ){

        try {
            Method createBondMethod = BluetoothDevice.class.getMethod("createBond");

            createBondMethod.invoke(device);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //安卓开发6.0蓝牙权限授权
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        switch (requestCode) {
            case 23:
                Log.i("wp123", "grantResults[0]=" + grantResults[0]);
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //授权成功
                    Toast.makeText(MainActivity.this, "正在搜索蓝牙设备...", Toast.LENGTH_SHORT).show();
                    doDiscovery();
                } else {
                    //授权拒绝
                }
                break;

        }
    }

    private void requestBluetoothPermission(){
        //判断系统版本
        Log.i("wp123", "系统版本为" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            //判断这个权限是否已经授权过

            if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){

                //判断是否需要 向用户解释，为什么要申请该权限,该方法只有在用户在上一次已经拒绝过你的这个权限申请才会调用。
                if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION))
                    Toast.makeText(this, "Need bluetooth permission.", Toast.LENGTH_SHORT).show();

                  /*  参数1 Context
                * 参数2 需要申请权限的字符串数组，支持一次性申请多个权限，对话框逐一询问
                * 参数3 requestCode 主要用于回调的时候检测*/

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},23);
                return;
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 23);

            }
        } else {
            Toast.makeText(MainActivity.this, "正在搜索蓝牙设备...", Toast.LENGTH_SHORT).show();
            doDiscovery();
        }
    }
    // 开始搜索蓝牙设备
    private void doDiscovery() {

        //判断是否在搜索，是的话就取消搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mListThreads.size() > 0){
            for (int i = 0;i < mListThreads.size();i++){
                mListThreads.get(i).cancel();
            }
        }
        // 确认在页面销毁时关闭搜索蓝牙功能
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // 取消广播注册
        this.unregisterReceiver(mReceiver);
        this.unregisterReceiver(connectReceiver);
        this.finish();
    }
}

class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder>
{
    private ArrayList<BlueInfo> list = null;

    public HomeAdapter(ArrayList<BlueInfo> list){
        this.list = list;
    }

    public interface OnItemClickLitener
    {
        void onItemClick(View view, int position);
        //void onItemLongClick(View view , int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)
    {
        holder.tv_name.setText(list.get(position).getName());
        holder.tv_address.setText(list.get(position).getAddress());
        holder.btn_connect.setText(list.get(position).getState());
        // 如果设置了回调，则设置点击事件
        if (mOnItemClickLitener != null)
        {
            holder.btn_connect.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.btn_connect, pos);
                }
            });


        }

    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {

        TextView tv_name;
        TextView tv_address;
        Button btn_connect;


        public MyViewHolder(View view)
        {
            super(view);
            tv_name = (TextView) view.findViewById(R.id.name);
            tv_address = (TextView)view.findViewById(R.id.address);
            btn_connect = (Button)view.findViewById(R.id.connect);

        }
    }
}

