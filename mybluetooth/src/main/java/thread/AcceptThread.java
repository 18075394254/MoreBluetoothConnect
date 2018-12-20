package thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by user on 2018/12/7.
 */

public class AcceptThread extends Thread{
    private BluetoothServerSocket mmServerSocket = null;
    private BluetoothSocket socket_one;
    private BluetoothSocket socket_two;
    private BluetoothSocket socket_three;
    private BluetoothSocket socket_four;
    private Handler mHandler;
    int arg = 0;
    public AcceptThread(BluetoothAdapter mBluetoothAdapter, Handler handler){
        BluetoothServerSocket tmp = null;
        mHandler = handler;
        try {
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("eric", UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

            mmServerSocket = tmp;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
      while(true){
    try{

          //第一个socket
          if (socket_one == null){
              try {

                  socket_one = mmServerSocket.accept();
                  Log.i("AcceptThred","socket_one = "+socket_one);
              } catch (IOException e) {
                  e.printStackTrace();
              }

              if (socket_one != null){
                    arg = 1;
                    manageConnectedSocket(socket_one);//处理相关函数
              }
          }

          //第二个socket
          if (socket_two == null){
              try {
                  socket_two = mmServerSocket.accept();
                  Log.i("AcceptThred","socket_two = "+socket_two);
              } catch (IOException e) {
                  e.printStackTrace();
              }

              if (socket_two != null){
                  arg = 2;
                  manageConnectedSocket(socket_two);//处理相关函数
              }
          }

          //第三个socket
          if (socket_three == null){
              try {
                  socket_three = mmServerSocket.accept();
                  Log.i("AcceptThred","socket_three = "+socket_three);
              } catch (IOException e) {
                  e.printStackTrace();
              }

              if (socket_three != null){
                  arg = 3;
                  manageConnectedSocket(socket_three);//处理相关函数
              }
          }

          //第四个socket
          if (socket_four == null){
              try {
                  socket_four = mmServerSocket.accept();
                  Log.i("AcceptThred","socket_four = "+socket_four);
              } catch (IOException e) {
                  e.printStackTrace();
              }

              if (socket_four != null){
                  arg = 4;
                  manageConnectedSocket(socket_four);//处理相关函数
                  break;
              }

          }
    }catch (Exception e){
            break;
    }
      }
    }



    public boolean checkBlueNum(){
        boolean flag = true;
        if (socket_one == null && socket_two ==null && socket_three ==null && socket_four ==null){
            flag = false;
            return flag;
        }

        return flag;
    }
    public void sendMessage(String s){
        Log.i("AcceptThred","s = "+s);
        if (socket_one == null) {
            Log.i("AcceptThred","socket_one == null");
        } else {
            try {
                Log.i("AcceptThred","socket_one发送之前");
                OutputStream os = socket_one.getOutputStream();
                os.write(s.getBytes());
                Log.i("AcceptThred","socket_one发送成功");

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (socket_two == null) {
            Log.i("AcceptThred","socket_two == null");
        } else {
            try {
                Log.i("AcceptThred","socket_two发送之前");
                OutputStream os = socket_two.getOutputStream();
                os.write(s.getBytes());
                Log.i("AcceptThred","socket_two发送成功");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (socket_three == null) {
            Log.i("AcceptThred","socket_three == null");
        } else {
            try {
                Log.i("AcceptThred","socket_three发送之前");
                OutputStream os = socket_three.getOutputStream();
                os.write(s.getBytes());
                Log.i("AcceptThred","socket_three发送成功");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (socket_four == null) {
            Log.i("AcceptThred","socket_four == null");
        } else {
            try {
                Log.i("AcceptThred","socket_four发送之前");
                OutputStream os = socket_four.getOutputStream();
                os.write(s.getBytes());
                Log.i("AcceptThred","socket_four发送成功");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    private void manageConnectedSocket(BluetoothSocket socket){
        if (socket != null){
            Log.i("AcceptThred","开启监听数据线程");
            ReadThread mReadThread = new ReadThread(socket,mHandler,arg);
            mReadThread.start();//开启读取数据线程
        }
    }

    private void cancel(){
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
