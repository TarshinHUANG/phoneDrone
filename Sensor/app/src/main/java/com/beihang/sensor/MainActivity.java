package com.beihang.sensor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    //定义系统的Sensor管理器
    private SensorManager sensorManager;
    private TextView accelerateSensor;
    private TextView orientationSenor;
    private TextView gyroscopeSensor;
    private TextView magnetSensor;
    private TextView gravitySensor;
    private TextView lightSensor;
    private TextView heartrateSensor;
    private TextView sensorList;
    private android.widget.Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取界面上的组件
        accelerateSensor =findViewById(R.id.accelerateSensor);
        orientationSenor=findViewById(R.id.orientationSensor);
        gyroscopeSensor=findViewById(R.id.gyroscopeSensor);
        magnetSensor=findViewById(R.id.magnetSensor);
        gravitySensor=findViewById(R.id.gravitySensor);
        lightSensor=findViewById(R.id.lightSensor);
        heartrateSensor=findViewById(R.id.heartrateSensor);
        sensorList =findViewById(R.id.sensorList);
        sensorList.setMovementMethod(ScrollingMovementMethod.getInstance());    //设置纵向滚动

        sensorList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    //通知父控件不要干扰
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_MOVE){
                    //通知父控件不要干扰
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });

        button=findViewById(R.id.button);
        //获取系统的传感器管理服务
        sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
    }

    public void onClick(View v)
    {
        //StringBuffer sensorString=null;
        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //得到手机上所有的传感器
        List<Sensor> listSensor = manager.getSensorList(Sensor.TYPE_ALL);
        int i = 1;

        for (Sensor sensor : listSensor) {
            //sensorString.append("sensor " ,0,5);
            Log.d("sensor " + i, sensor.getName());
            sensorList.append("\nsensor " + i+sensor.getName());
            i++;
        }
        //通过调用getDefaultSensor方法获取某一个类型的默认传感器
        //Sensor s = manager.getDefaultSensor(Sensor.TYPE_LIGHT);
        //sensorList.setText(sensorString);
        //Log.d("list",sensorString.toString());

    }


    @Override
    public void onResume()
    {
        super.onResume();
        //为系统的加速度传感器注册监听器
        sensorManager.registerListener((SensorEventListener) this,  //设置传感事件监听器
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),   //设置传感器对象
                SensorManager.SENSOR_DELAY_UI);    //设置延时
        sensorManager.registerListener((SensorEventListener) this,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener((SensorEventListener) this,sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener((SensorEventListener) this,sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener((SensorEventListener) this,sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener((SensorEventListener) this,sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE),SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //取消注册
        sensorManager.unregisterListener((SensorEventListener) this);
    }

    @Override
    public void onSensorChanged (SensorEvent event)
    {
        float [] values=event.values;
        int sensorType=event.sensor.getType();
        String sensordata;
        //判断哪个传感器发生改变
        switch(sensorType)
        {
            case Sensor.TYPE_GYROSCOPE:
                sensordata="绕X方向的角加速度："+values[0]+"\n绕Y方向的角加速度："+values[1]+"\n绕Z方向上的角加速度："+values[2];
                gyroscopeSensor.setText(sensordata);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                sensordata="X方向上的磁场速度："+values[0]+"\nY方向上的磁场速度："+values[1]+"\nZ方向上的磁场速度："+values[2];
                magnetSensor.setText(sensordata);
                break;
            case Sensor.TYPE_GRAVITY:
                sensordata="X方向上的重力："+values[0]+"\nY方向上的重力："+values[1]+"\nZ方向上的重力："+values[2];
                gravitySensor.setText(sensordata);
                break;
            case Sensor.TYPE_ACCELEROMETER:
                sensordata="X方向上的总加速度："+values[0]+"\nY方向上的总加速度："+values[1]+"\nZ方向上的总加速度："+values[2];
                accelerateSensor.setText(sensordata);
                break;
            case Sensor.TYPE_LIGHT:
                sensordata="光强为："+values[0];
                lightSensor.setText(sensordata);
                break;
//            case Sensor.TYPE_HEART_BEAT:
//                sensordata="X方向上的磁场速度："+values[0]+"\nY方向上的磁场速度："+values[1]+"\nZ方向上的磁场速度："+values[2];
//                gyroscopeSensor.setText(sensordata);
//                break;
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
}
