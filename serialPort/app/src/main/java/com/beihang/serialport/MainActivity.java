package com.beihang.serialport;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();
    //正在使用的设备
    private UsbSerialDriver mSerialDevice;
    //系统的USB服务
    public UsbManager mUsbManager;

    private TextView mTitleTextView;
    private TextView mDumpTextView;
    private ScrollView mScrollView;

    private final ExecutorService mExecutor= Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIOManager;

    private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {
        @Override
        public void onNewData(final byte[] data) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.updateReceivedData(data);
                }
            });
        }

        @Override
        public void onRunError(Exception e) {
            Log.d(TAG, "Runner stopped.");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mTitleTextView = (TextView) findViewById(R.id.mTitleTextView);
        mDumpTextView = (TextView) findViewById(R.id.mDumpTextView);
        mScrollView = (ScrollView) findViewById(R.id.mScrollView);
    }

    @Override
    protected void onPause(){
        super.onPause();
        stopIOManager();
        if(mSerialDevice !=null){
            try{
                mSerialDevice.close();
            }catch(IOException e){

            }
            mSerialDevice=null;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        mSerialDevice= UsbSerialProber.acquire(mUsbManager);
        Log.d(TAG, "Resumed, mSerialDevice=" + mSerialDevice);
        if (mSerialDevice == null) {
            mTitleTextView.setText("No serial device.");
        } else {
            try {
                mSerialDevice.open();
            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                mTitleTextView.setText("Error opening device: " + e.getMessage());
                try {
                    mSerialDevice.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                mSerialDevice = null;
                return;
            }
            mTitleTextView.setText("Serial device: " + mSerialDevice);
        }
        onDeviceStateChange();
    }
    private void stopIOManager() {
        if (mSerialIOManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIOManager.stop();
            mSerialIOManager = null;
        }
    }

    private void startIOManager() {
        if (mSerialDevice != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIOManager = new SerialInputOutputManager(mSerialDevice, mListener);
            mExecutor.submit(mSerialIOManager);
        }
    }

    private void onDeviceStateChange() {
        stopIOManager();
        startIOManager();
    }

    private void updateReceivedData(byte[] data) {
        final String message = "Read " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n";
        mDumpTextView.append(message);
        mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
    }

}
