package com.beihang.phonedrone;

public class Control {
    private PID pitch_angle;  //俯仰角度外环
    private PID pitch_speed;  //俯仰角速度内环
    private PID roll_angle;  //滚转角度外环
    private PID roll_speed;  //滚转角速度内环
    private PID yaw_angle;  //偏航角度外环
    private PID yaw_speed;  //偏航角速度内环

    public Control() {
        pitch_angle = new PID(0, 0);
        pitch_speed = new PID(0,0,0);
        roll_angle = new PID(0, 0);
        roll_speed = new PID(0,0,0);
        yaw_angle = new PID(0, 0);
        yaw_speed = new PID(0,0,0);
    }

    public void setPitchAnglePID(float kp, float ki, float kd) {
        pitch_angle.setPID(kp, ki, kd);
    }

    public void setPitchSpeedPID(float kp, float ki, float kd) {
        pitch_speed.setPID(kp, ki, kd);
    }

    public void setRollAnglePID(float kp, float ki, float kd) {
        roll_angle.setPID(kp, ki, kd);
    }

    public void setRollSpeedPID(float kp, float ki, float kd) {
        roll_speed.setPID(kp, ki, kd);
    }

    public void setYawAnglePID(float kp, float ki, float kd) {
        yaw_angle.setPID(kp, ki, kd);
    }

    public void setYawSpeedPID(float kp, float ki, float kd) {
        yaw_speed.setPID(kp, ki, kd);
    }

    public float calPitch(float pitchNow, float pitchTarget, float pitchRate) {
        float angleOut = pitch_angle.calOutput(pitchNow, pitchTarget, 20);
        float rateOut = pitch_speed.calOutput(pitchRate, angleOut, 200);  //将角度外环的输出作为角速度内环的输入
        return rateOut;
    }

    public float calRoll(float rollNow, float rollTarget, float RollRate) {
        float angleOut = roll_angle.calOutput(rollNow, rollTarget, 20);
        float rateOut = roll_speed.calOutput(RollRate, angleOut, 200);
        return rateOut;
    }

    //对于Yaw，使用calOutputYaw函数，防止两个角度差超过180或低于-180
    public float calYaw(float yawNow, float yawTarget, float yawRate) {
        float angleOut = yaw_angle.calOutputYaw(yawNow, yawTarget, 20);
        float rateOut = yaw_speed.calOutput(yawRate, angleOut, 200);
        return rateOut;
    }

    public void resetPID() {
        pitch_angle.resetPID();
        pitch_speed.resetPID();
        roll_angle.resetPID();
        roll_speed.resetPID();
        yaw_angle.resetPID();
        yaw_speed.resetPID();
    }
}
