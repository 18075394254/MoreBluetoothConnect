package thread;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.SocketHandler;

/**
 * Created by user on 2018/12/7.
 */

public class ReadThread extends Thread{

    private BluetoothSocket socket;
    private boolean flag = true;
    private BluetoothDevice mDevice;
    int arg =0;
    private Handler mHandler;
             public ReadThread( BluetoothSocket socket, Handler handler,int arg) {
                 this.socket = socket;
                 mHandler = handler;
                // mDevice = device;
                 this.arg = arg;
              }

    public void sendMessage(String s) {
        // String s = ed_message.getText().toString()+"";
        if (socket == null) {
            Log.i("ReadThred", "socket == null");
        } else {
            try {
                Log.i("ReadThred", "socket发送之前");
                OutputStream os = socket.getOutputStream();
                os.write(s.getBytes());
                Log.i("ReadThred", "socket发送成功");

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }



             public void run() {

                  byte[] buffer = new byte[1024];
                 int bytes;
                InputStream mmInStream = null;
                try {
                          mmInStream = this.socket.getInputStream();
                     } catch (IOException e1) {
                             // TODO Auto-generated catch block
                          e1.printStackTrace();
                       }

                 while (flag) {
                     try {

                         // Read from the InputStream
                         if ((bytes = mmInStream.read(buffer)) > 0) {
                             byte[] buf_data = new byte[bytes];
                             for (int i = 0; i < bytes; i++) {
                                 buf_data[i] = buffer[i];
                             }
                             String s = new String(buf_data);
                             Log.i("ReadThred", "s = "+ s);
                             Message msg = new Message();
                             msg.what = arg;
                             msg.obj = s;
                             mHandler.sendMessage(msg);
                         }
                     } catch (IOException e) {
                         try {
                             mmInStream.close();
                         } catch (IOException e1) {
                             // TODO Auto-generated catch block
                             e1.printStackTrace();
                         }
                         break;
                     }

                 }
     }


    public void close() {
        flag = false;
        socket = null;
    }
}
