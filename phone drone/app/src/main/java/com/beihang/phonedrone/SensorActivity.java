package com.beihang.phonedrone;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    public static final String TAG = SensorActivity.class.getName();

    private SensorManager mSensorManager;
    private TextView gyroscopeTv;
    private TextView accelerometerTv;
    private TextView orientationTv;

    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetic;
    private float[] accelerometerValues = new float[3];
    private float[] gyroscopeValues = new float[3];
    private float[] magneticValues = new float[3];
    private float[] orientationValues = new float[3];
    StringBuffer sb1 = new StringBuffer();
    StringBuffer sb2 = new StringBuffer();
    StringBuffer sb3 = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        initUI();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);  //加速度
        gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);  //角速度
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this,gyroscope,SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this,magnetic,SensorManager.SENSOR_DELAY_UI);
        Log.d(TAG,"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    private void initUI() {
        gyroscopeTv = (TextView) findViewById(R.id.gyroscope_tv);
        accelerometerTv = (TextView) findViewById(R.id.accelerometer_tv);
        orientationTv = (TextView) findViewById(R.id.orientation_tv);
        ((TextView) findViewById(R.id.titleTv)).setText(R.string.sensor_activity);
        ImageButton returnBtn = (ImageButton) findViewById(R.id.returnBtn);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SensorActivity.this.finish();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValues = event.values;
                sb1 = new StringBuffer();
                Utils.addLineToSB(sb1,"Accelerometer",null);
                Utils.addLineToSB(sb1,"acc_X",accelerometerValues[0]);
                Utils.addLineToSB(sb1,"acc_Y",accelerometerValues[1]);
                Utils.addLineToSB(sb1,"acc_Z",accelerometerValues[2]);
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroscopeValues = event.values;
                sb2 = new StringBuffer();
                Utils.addLineToSB(sb2,"Gyroscope",null);
                Utils.addLineToSB(sb2,"gyr_X",gyroscopeValues[0]);
                Utils.addLineToSB(sb2,"gyr_Y",gyroscopeValues[1]);
                Utils.addLineToSB(sb2,"gyr_Z",gyroscopeValues[2]);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = event.values;
                break;
        }
        calOrientation();
        updateTextView();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void calOrientation() {
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R,null, accelerometerValues, magneticValues);
        SensorManager.getOrientation(R,orientationValues);
        for (int i=0; i<orientationValues.length;i++) {
            orientationValues[i] = (float) Math.toDegrees(orientationValues[i]);
        }
        sb3 = new StringBuffer();
        Utils.addLineToSB(sb3,"Orientation",null);
        Utils.addLineToSB(sb3,"Yaw",orientationValues[0]);
        Utils.addLineToSB(sb3,"Pitch",orientationValues[1]);
        Utils.addLineToSB(sb3,"Roll",orientationValues[2]);
    }

    private void updateTextView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                accelerometerTv.setText(sb1.toString());
                gyroscopeTv.setText(sb2.toString());
                orientationTv.setText(sb3.toString());
            }
        });
    }
}
