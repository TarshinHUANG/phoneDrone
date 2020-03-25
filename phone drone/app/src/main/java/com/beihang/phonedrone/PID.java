package com.beihang.phonedrone;

public class PID {
    private float kp;
    private float ki;
    private float kd;

    private float error;
    private float lastError;
    private float errorSum;  //总误差，积分作用
    private float deviation;  //两次误差之差，微分作用

    public PID(float kp, float ki, float kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;

        this.error = 0;
    }

    public PID(float kp, float kd) {
        this.kp = kp;
        this.kd = kd;
        this.ki = 0;
    }

    public void setPID(float kp, float ki, float kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }

    public float calOutput(float input, float target, int maxNum) {
        error = target - input;
        errorSum += error;
        errorSum = limit(errorSum, maxNum);
        deviation = error - lastError;
        lastError = error;

        float kpOut = kp * error;
        float kiOut = ki * errorSum;
        float kdOut = kd * deviation;
        return kpOut + kiOut + kdOut;
    }

    public float calOutputYaw(float input, float target, int maxNum) {
        if (target - input >180 || target - input < -180) {
            if (target > 0 && input < 0) {
                error = target - input - 360;
            }
            if (target < 0 && input > 0) {
                error = target - input + 360;
            }
        } else error = target - input;
        errorSum += error;
        errorSum = limit(errorSum, maxNum);
        deviation = error - lastError;
        lastError = error;

        float kpOut = kp * error;
        float kiOut = ki * errorSum;
        float kdOut = kd * deviation;
        return kpOut + kiOut + kdOut;
    }

    public void resetPID() {
        lastError = 0;
        errorSum = 0;
        deviation = 0;
    }

    private float limit(float input, int maxNum) {
        if (input > maxNum) {
            input = maxNum;
        }
        if (input < -maxNum) {
            input = -maxNum;
        }
        return input;
    }
}
