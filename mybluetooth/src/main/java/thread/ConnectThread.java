package thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;


/**
 * Created by user on 2018/12/7.
 */

public class ConnectThread extends Thread {
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private BluetoothAdapter mAdapter;
    private Handler mHandler;
    int arg = 0;
    private ReadThread read;
    public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter, Handler handler,int arg){
            mDevice = device;
        BluetoothSocket tmp = null;
        mAdapter = adapter;
        mHandler = handler;
        this.arg = arg;

        try {
            tmp = mDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        mSocket = tmp;


    }

    public BluetoothDevice getDevice(){
        return mDevice;
    }

    @Override
    public void run() {
        mAdapter.cancelDiscovery();

        try {
            mSocket.connect();
            Message msg=mHandler.obtainMessage();
             msg.arg1 = arg;
             msg.what = 12345;
             msg.obj = this;
             msg.sendToTarget();

        } catch (IOException e) {
            e.printStackTrace();
            try {
                mSocket.close();
            } catch (IOException closeException) {

            }
            return;

        }

        if(mSocket!=null){
            manageConnectedSocket(mSocket,arg);
        }

    }
    public void cancel() {
        try {
            read.close();
            mSocket.close();
        } catch (IOException e) {

        }
    }

    private void manageConnectedSocket(BluetoothSocket socket,int arg) {
        read = new ReadThread(socket,mHandler,arg);//开启读取数据线程；
        read.start();

    }

    public BluetoothSocket getsocket(){
        if (mSocket != null) {
            return mSocket;
        }
        return  null;
    }
}
