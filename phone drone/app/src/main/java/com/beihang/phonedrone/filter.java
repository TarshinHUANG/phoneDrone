package com.beihang.phonedrone;
public class filter {

	//采用直接性的IIR滤波器，直接差分方程描述，延时器少
	//y(n) = b0*x(n) + b1*x(n-1) + b2*x(n-2)+.... - a1*y(n-1) - a2*y(n-2)-.....
	float[][] inData=new float[3][5];  //存储未滤波的最近5个数据
	float[][] outData=new float[3][5]; //存储滤波的最近5个数据
	float[] b =new float[]{0.0008f, 0.0032f, 0.0048f, 0.0032f, 0.0008f};  //ÏµÊýb
	float[] a =new float[]{1.0000f, -3.0176f, 3.5072f, -1.8476f, 0.3708f};//ÏµÊýa
	void IIR (float[] newData) {
		float z1,z2;
		for (int k=0; k<3;k++) {
			int i;
			for (i=4;i>0;i--) {
				inData[k][i]=inData[k][i-1]; //向后移一位
			}
			inData[k][0]=newData[k]; //补上第一位
			for(z1=0,i=0; i<5;i++) {
				z1+=inData[k][i]*b[i];
			}
			for (i=4;i>0;i--) {
				outData[k][i]=outData[k][i-1]; //向后移一位
			}
			for(z2=0,i=1; i<5;i++) {
				z2+=outData[k][i]*a[i];
			}
			outData[k][0]=z1-z2;
		}
	}

	//一阶滤波
	//float[] inData1=new float[3]; //旧数据
	float[] outData1=new float[3];  //新数据
	float lpfFactor=0.386f;
	void LPF(float[] Data) {
		for(int i=0;i<3;i++) {
			outData1[i]=outData1[i]*(1-lpfFactor)+Data[i]*lpfFactor;
		}
	}
}
