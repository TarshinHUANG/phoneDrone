package com.beihang.phonedrone;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

public class USBActivity extends AppCompatActivity implements View.OnClickListener {
    public final String TAG = USBActivity.class.getName();
    public final String ACTION_USB_PERMISSION = "com.beihang.phonedrone.USB_PERMISSION";

    private TextView titleTv;
    private ImageButton returnBtn;
    private TextView usbDeviceTv;
    private EditText usbEt;
    private TextView messageTv;
    private TextView receiveTv;
    private Button searchBtn;
    private Button sendBtn;
    private Button openBtn;
    private Button clearBtn;
    private StringBuffer message = new StringBuffer();
    private StringBuffer messageSend = new StringBuffer();

    private UsbManager mUsbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);

        initUI();
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver,filter);

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(ACTION_USB_PERMISSION);
        registerReceiver(USBReceiver,filter1);  //注册权限广播接收器，同时在里面打开串口
        Log.d(TAG,"onCreate");
    }

    private void initUI() {
        titleTv = (TextView) findViewById(R.id.titleTv);
        titleTv.setText(R.string.usb_activity);
        returnBtn = (ImageButton) findViewById(R.id.returnBtn);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                USBActivity.this.finish();
            }
        });
        usbDeviceTv = (TextView) findViewById(R.id.usbdevice_tv);
        usbEt = (EditText) findViewById(R.id.usb_et);
        messageTv = (TextView) findViewById(R.id.usbmessage_tv);
        receiveTv = (TextView) findViewById(R.id.receive_tv);
        searchBtn = (Button) findViewById(R.id.search_btn);
        sendBtn = (Button) findViewById(R.id.send_btn);
        openBtn = (Button) findViewById(R.id.open_btn);
        clearBtn = (Button) findViewById(R.id.clear_btn);
        searchBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
        openBtn.setOnClickListener(this);
        clearBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_btn:
                searchUsbDevice();
                break;
            case R.id.open_btn:
                openSerial();
                break;
            case R.id.send_btn:
                sendMessage();
                break;
            case R.id.clear_btn:
                clearMessage();
        }
    }

    private void searchUsbDevice() {
        HashMap<String, UsbDevice> usbDevices = mUsbManager.getDeviceList();
        StringBuffer sb = new StringBuffer();
        if (!usbDevices.isEmpty()) {
            Iterator<UsbDevice> deviceIterator = usbDevices.values().iterator();
            while(deviceIterator.hasNext()) {
                device = deviceIterator.next();
                sb.append(device.getDeviceName() + '\n');
                sb.append("VendorID:" + device.getVendorId()+"  ");
                sb.append("ProductID:" + device.getProductId());
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    usbDeviceTv.setText(sb.toString());
                }
            });
        } else {
            Log.d(TAG,"No device found");
            device = null;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    usbDeviceTv.setText(R.string.no_device);
                }
            });
        }
    }

    //打开串口
    private void openSerial() {
        if (device == null) {
            showToast("No device found!");
        } else {
            if (serialPort != null) {
                showToast("There is already a SerialPort");
                return;
            }
            if (!mUsbManager.hasPermission(device)) {
                PendingIntent pi = PendingIntent.getBroadcast(USBActivity.this,0,new Intent(ACTION_USB_PERMISSION),0);
                mUsbManager.requestPermission(device,pi);
            } else {
                connection = mUsbManager.openDevice(device);
                serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                if (serialPort != null) {
                    if (serialPort.open()) { //Set Serial Connection Parameters.
                        serialPort.setBaudRate(9600);
                        serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                        serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                        serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                        serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                        showToast("SerialPort open");
                    }
                }
            }
        }
    }

    private void sendMessage() {
        String sb = usbEt.getText().toString();
        if (sb.equals("")) {
            showToast("Empty Message");
            return;
        }
        if (serialPort != null) {
            serialPort.write(sb.getBytes());
        } else {
            showToast("SerialPort not open");
            return;
        }
        Utils.addLineToSB(messageSend,"Send",sb);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageTv.setText(messageSend.toString());
            }
        });
    }

    private void clearMessage() {
        messageSend.delete(0,messageSend.length());
        message.delete(0,message.length());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageTv.setText("Message");
                receiveTv.setText("Receive message");
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
                showToast("USB Attached");
                searchUsbDevice();
                //请求USB权限，使得下面广播接收器接收
                if (device != null) {
                    if (!mUsbManager.hasPermission(device)) {
                        PendingIntent pi = PendingIntent.getBroadcast(USBActivity.this,0,new Intent(ACTION_USB_PERMISSION),0);
                        mUsbManager.requestPermission(device,pi);
                    }
                }
            }
            if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                showToast("USB detached");
                device = null;
                connection = null;
                serialPort = null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        usbDeviceTv.setText(R.string.no_device);
                    }
                });
            }
            Log.d(TAG,"onReceive");
        }
    };

    private BroadcastReceiver USBReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = mUsbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            showToast("SerialPort open");
                        }
                    }
                }
            }
        }
    };

    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] data) {
            String sb = null;
            sb = new String(data, StandardCharsets.UTF_8);
            Utils.addLineToSB(message,"Receive",sb);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    receiveTv.setText(message.toString());
                }
            });
        }
    };

    private void showToast(String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(USBActivity.this,str,Toast.LENGTH_LONG).show();
            }
        });
    }
}
