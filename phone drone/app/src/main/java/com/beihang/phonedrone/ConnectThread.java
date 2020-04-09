package com.beihang.phonedrone;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class ConnectThread extends Thread {
    public final String TAG = "ConnectThread";
    private int BUFFER_SIZE = 200;
    private boolean active = true;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] buffer = new byte[BUFFER_SIZE];  //缓冲区
    private byte[] data;

    public ConnectThread(BluetoothSocket socket, boolean active) {
        this.socket = socket;
        this.active = active;
    }

    @Override
    public void run() {
        if (active) {
            try {
                Log.d(TAG, "Bluetooth connectting");
                socket.connect();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

                int bytes;
                while (true) {
                    //读取数据
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        data = new byte[bytes];
                        System.arraycopy(buffer, 0, data, 0, bytes);
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, "Blue connect fail");
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }

    public void sendMsg(final String msg) {

        byte[] bytes = msg.getBytes();
        if (outputStream != null) {
            try {
                //发送数据
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String readMsg() {
        String str = Arrays.toString(data);
        return str;
    }

}
