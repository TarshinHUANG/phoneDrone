package com.beihang.phonedrone;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class StartActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    public final String TAG = StartActivity.class.getName();
    public final String ACTION_USB_PERMISSION = "com.beihang.phonedrone.USB_PERMISSION";

    private TextView titleTv;
    private TextView eulerTv;
    private TextView usbDeviceTv;
    private ImageButton returnBtn;
    private EditText pitchAngleKpEt;
    private EditText pitchAngleKiEt;
    private EditText pitchAngleKdEt;
    private EditText pitchSpeedKpEt;
    private EditText pitchSpeedKiEt;
    private EditText pitchSpeedKdEt;
    private EditText rollAngleKpEt;
    private EditText rollAngleKiEt;
    private EditText rollAngleKdEt;
    private EditText rollSpeedKpEt;
    private EditText rollSpeedKiEt;
    private EditText rollSpeedKdEt;
    private EditText yawAngleKpEt;
    private EditText yawAngleKiEt;
    private EditText yawAngleKdEt;
    private EditText yawSpeedKpEt;
    private EditText yawSpeedKiEt;
    private EditText yawSpeedKdEt;
    private Button pitchBtn;
    private Button rollBtn;
    private Button yawBtn;
    private Button flyBtn;
    private TextView pidInfoTv;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetic;
    private float[] accelerometerValues = new float[3];  //加速度计三个方向值
    private float[] gyroscopeValues = new float[3];     //三个方向角速度
    private float[] magneticValues = new float[3];     //磁力计
    private float[] orientationValues = new float[3];  //欧拉角
    private int[] motorDuty = new int[4];

    private Thread mControlThread;
    private boolean isRunning = false;
    private UsbManager mUsbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;  //串口对象
    private Control flightControl;  //姿态控制对象
    private filter filterAcc=new filter(); //加速度滤波器
    private filter filterGyro=new filter(); //角速度滤波器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        initUI();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert mSensorManager != null;
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);  //加速度
        gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);  //角速度
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver,filter);

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(ACTION_USB_PERMISSION);
        registerReceiver(USBReceiver,filter1);  //注册权限广播接收器，同时在里面打开串口
        Log.d(TAG,"onCreate");

        searchUsbDevice();
        openSerial();
        flightControl = new Control();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册传感器listener，传感器数据发生变化的时候就会回调onSensorChanged()函数
        mSensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,gyroscope,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,magnetic,SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    /**
     * @param event
     * 传感器数据变化回调函数
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        switch(sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValues = event.values;
                //加速度IIR滤波
                filterAcc.IIR(accelerometerValues);
                for(int i =0;i<3;i++){
                    accelerometerValues[i]=filterAcc.outData[i][0];
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroscopeValues = event.values;
                //角速度一阶滤波
                filterGyro.LPF(gyroscopeValues);
                for(int i=0;i<3;i++){
                    gyroscopeValues[i]=filterGyro.outData1[i];
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = event.values;
                break;
        }
        calOrientation();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 利用加速度和磁力计值计算欧拉角（滤波？？）
     */
    private void calOrientation() {
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R,null, accelerometerValues, magneticValues);
        SensorManager.getOrientation(R,orientationValues);
        for (int i=0; i<orientationValues.length;i++) {
            orientationValues[i] = (float) Math.toDegrees(orientationValues[i]);
        }
        StringBuffer sb3 = new StringBuffer();
        Utils.addLineToSB(sb3,"Orientation",null);
        Utils.addLineToSB(sb3,"Yaw",orientationValues[0]);
        Utils.addLineToSB(sb3,"Pitch",orientationValues[1]);
        Utils.addLineToSB(sb3,"Roll",orientationValues[2]);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                eulerTv.setText(sb3.toString());
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pitch_btn:
                testPitch();
                break;
            case R.id.roll_btn:
                //testRoll();
                isRunning = false;
                break;
            case R.id.yaw_btn:
                //testYaw();
                break;
            case R.id.go_btn:
                break;
        }
    }

    //USB设备插上后寻找USB设备并打开串口
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
                showToast("USB Attached");
                searchUsbDevice();
                //请求USB权限，使得下面广播接收器接收
                if (device != null) {
                    if (!mUsbManager.hasPermission(device)) {
                        PendingIntent pi = PendingIntent.getBroadcast(StartActivity.this,0,new Intent(ACTION_USB_PERMISSION),0);
                        mUsbManager.requestPermission(device,pi);  //请求USB权限，发送意图为ACTION_USB_PERMISSION的广播
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
        }
    };

    //获取到USB权限之后打开串口
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
                            serialPort.setBaudRate(115200);
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

    //串口接收数据后回调函数
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] data) {
            String sb = null;
            sb = new String(data, StandardCharsets.UTF_8);
        }
    };

    //寻找USB设备
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

    //尝试打开串口
    private void openSerial() {
        if (device == null) {
            showToast("No device found!");
        } else {
            if (serialPort != null && serialPort.isOpen()) {
                showToast("There is already a SerialPort");
                return;
            }
            if (!mUsbManager.hasPermission(device)) {
                PendingIntent pi = PendingIntent.getBroadcast(StartActivity.this,0,new Intent(ACTION_USB_PERMISSION),0);
                mUsbManager.requestPermission(device,pi);
            } else {
                connection = mUsbManager.openDevice(device);
                serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                if (serialPort != null) {
                    if (serialPort.open()) { //Set Serial Connection Parameters.
                        serialPort.setBaudRate(115200);
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
    //测试Pitch的PID参数
    private void testPitch() {
        String speedKp = pitchSpeedKpEt.getText().toString();
        String speedKi = pitchSpeedKiEt.getText().toString();
        String speedKd = pitchSpeedKdEt.getText().toString();
        String angleKp = pitchAngleKpEt.getText().toString();
        String angleKi = pitchAngleKiEt.getText().toString();
        String angleKd = pitchAngleKdEt.getText().toString();
        if (speedKp.equals("") || speedKi.equals("") || speedKd.equals("")) {
            showToast("请至少给出内环参数！");
            return;
        }
        float sKp = getInput(speedKp);
        float sKi = getInput(speedKi);
        float sKd = getInput(speedKd);
        float aKp = getInput(angleKp);
        float aKi = getInput(angleKi);
        float aKd = getInput(angleKd);
        flightControl.resetPID();  //PID累计误差和微分清零
        //如果没有给出外环参数，内环PID测试开始
        if (angleKp.equals("")) {
            StringBuffer sb = new StringBuffer();
            sb.append("Pitch内环PID测试:\n").append("Kp:" + speedKp).append("Ki:" + speedKi).append("Kd:" + speedKd);
            showToast("Pitch内环PID测试开始");
            flightControl.setPitchSpeedPID(sKp, sKi, sKd);
            updatePIDText(sb.toString());
            //执行控制循环
            isRunning = true;
            mControlThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //控制线程开始之前确认串口打开
                    if (serialPort != null && !serialPort.isOpen()) {
                        openSerial();
                    }
                    while (isRunning) {
                        if (orientationValues[1] > 30 || orientationValues[1] < -30) {
                            showToast("Mission Cancelled");
                            serialPort.write("s".getBytes());
                            break;
                        } else {
                            //传入参数为当前Pitch，目标Pitch（0），当前角速度
                            int output = (int) flightControl.calPitch(orientationValues[1], 0, gyroscopeValues[0]);
                            //电机输出以1500为基础，加减输出量
                            int code = setMotor(output, 0, 0, 1500);
                            if (code == -1) {
                                showToast("Check SerialPort!");
                                break;
                            }
                        }
                    }
                    if (serialPort != null) {
                        serialPort.close();  //线程结束之后关闭串口，不然会一直发送（不知道为什么）
                    }
                    isRunning = false;
                }
            });
            mControlThread.start();
        } else {
            isRunning = true;
            showToast("Pitch双环测试开始");
            StringBuffer sb = new StringBuffer();
            sb.append("Pitch双环PID测试:\n").append("内环：Kp:" + speedKp).append("Ki:" + speedKi).append("Kd:" + speedKd + "\n");
            sb.append("外环：Kp:" + angleKp).append("Ki:" + angleKi).append("Kd:" + angleKd);
            updatePIDText(sb.toString());

            flightControl.setPitchSpeedPID(sKp, sKi, sKd);
            flightControl.setPitchAnglePID(aKp, aKi, aKd);

            mControlThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (serialPort != null && !serialPort.isOpen()) {
                        openSerial();
                    }

                    while (isRunning) {
                        if (orientationValues[1] > 30 || orientationValues[1] < -30) {
                            showToast("Mission Cancelled");
                            serialPort.write("s".getBytes());
                            break;
                        } else {
                            int output = (int) flightControl.calPitch(orientationValues[1], 0, gyroscopeValues[0]);
                            int code = setMotor(output, 0, 0, 1500);
                            if (code == -1) {
                                showToast("Check SerialPort!");
                                break;
                            }
                        }
                    }
                    if (serialPort != null) {
                        serialPort.close();  //线程结束之后关闭串口，不然会一直发送（不知道为什么）
                    }
                    isRunning = false;
                }
            });
            mControlThread.start();
        }

    }

    private int setMotor(int pitch, int roll, int yaw, int throttle) {
        motorDuty[0] = throttle - pitch - roll + yaw;
        motorDuty[1] = throttle - pitch + roll - yaw;
        motorDuty[2] = throttle + pitch + roll + yaw;
        motorDuty[3] = throttle + pitch - roll - yaw;
        limitOutput(motorDuty);
        String sb = Arrays.toString(motorDuty);
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.write(sb.getBytes());
            return 0;
        } else {
            return -1;
        }
    }

    private void limitOutput(int[] input) {
        for(int i = 0; i < input.length; i++){
            if (input[i]<1000) input[i] = 1000;
            if (input[i]>2000) input[i] = 2000;
        }
    }

    private void initUI() {
        titleTv = (TextView) findViewById(R.id.titleTv);
        titleTv.setText(R.string.start_activity);
        returnBtn = (ImageButton) findViewById(R.id.returnBtn);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartActivity.this.finish();
            }
        });
        eulerTv = (TextView) findViewById(R.id.start_orientation_tv);
        usbDeviceTv = (TextView) findViewById(R.id.start_device_tv);
        pitchAngleKpEt = findViewById(R.id.pitch_angle_kp_et);
        pitchAngleKiEt = findViewById(R.id.pitch_angle_ki_et);
        pitchAngleKdEt = findViewById(R.id.pitch_angle_kd_et);
        pitchSpeedKpEt = findViewById(R.id.pitch_speed_kp_et);
        pitchSpeedKiEt = findViewById(R.id.pitch_speed_ki_et);
        pitchSpeedKdEt = findViewById(R.id.pitch_speed_kd_et);
        rollAngleKpEt = findViewById(R.id.roll_angle_kp_et);
        rollAngleKiEt = findViewById(R.id.roll_angle_ki_et);
        rollAngleKdEt = findViewById(R.id.roll_angle_kd_et);
        rollSpeedKpEt = findViewById(R.id.roll_speed_kp_et);
        rollSpeedKiEt = findViewById(R.id.roll_speed_ki_et);
        rollSpeedKdEt = findViewById(R.id.roll_speed_kd_et);
        yawAngleKpEt = findViewById(R.id.yaw_angle_kp_et);
        yawAngleKiEt = findViewById(R.id.yaw_angle_ki_et);
        yawAngleKdEt = findViewById(R.id.yaw_angle_kd_et);
        yawSpeedKpEt = findViewById(R.id.yaw_speed_kp_et);
        yawSpeedKiEt = findViewById(R.id.yaw_speed_ki_et);
        yawSpeedKdEt = findViewById(R.id.yaw_speed_kd_et);
        pidInfoTv = findViewById(R.id.pid_info_tv);
        pitchBtn = findViewById(R.id.pitch_btn);
        rollBtn = findViewById(R.id.roll_btn);
        yawBtn = findViewById(R.id.yaw_btn);
        flyBtn = findViewById(R.id.go_btn);
        pitchBtn.setOnClickListener(this);
        rollBtn.setOnClickListener(this);
        yawBtn.setOnClickListener(this);
        flyBtn.setOnClickListener(this);
    }

    private void updatePIDText(String symbol) {
        pidInfoTv.setText(symbol);
    }

    private void showToast(String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StartActivity.this, str, Toast.LENGTH_LONG).show();
            }
        });
    }

    private float getInput(String input) {
        if (input.equals("")) {
            return 0;
        }
        else return Float.parseFloat(input);
    }
}
