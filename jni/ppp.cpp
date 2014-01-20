#include <jni.h>
#include<stdio.h>
#include<math.h>
#include<string>
#include "G2ProtocolProcess.h"

extern "C" 
{
	S32 receiveState=0;
	U8 receiveBuffer[256]={0};
	iFlyGFrameStruct iFlyGFrameObj;
	U8 S0=0xA5;
	U8 S1=0x5A;
	U8 Command=0x61;
	
	U8 CalcSum(U8* Buffer,S32 Length)
	{
	    U8 sum=0;
	    for(S32 i=0;i<Length;i++)
	    {
	        sum+=Buffer[i];
	    }
	    return sum;
	}

	U8 CheckSum(U8* Buffer,S32 Length)
	{
	    U8 sum=CalcSum(Buffer,Length-1);
	    if(sum!=Buffer[Length-1])
	        return 0;
	    return 1;
	}

	U16 CRC16Encode(U8 *buf, S32 len)
	{
	    U16 crc_gen = 0xa001;  /*1,1000,0000,0000,0101B*/
	    U16 crc;
	    S32 i, j;

	    crc = 0xffff;
	    if (len != 0)
	    {
	        for (i = 0; i < len; i++)
	        {
	            crc ^= (U16)(buf[i]);
	            for (j = 0; j < 8; j++)
	            {
	                if ((crc & 0x01) == 0x01)
	                {
	                    crc >>= 1;
	                    crc ^= crc_gen;
	                }
	                else
	                {
	                    crc >>= 1;
	                }
	            }
	        }
	    }
	    return crc;
	}


	U8 CheckBuffer(U8* Buffer,S32 Length)
	{
		
	    if(Length<10)
	        return 0;
	    if(Buffer[4]!=Command)
	        return 0;
	    if(CheckSum(Buffer,Length)==0)
	        return 0;
	    if(CRC16Encode(Buffer,Length-1)!=0)
	        return 0;/**/
	    return 1;
	}

	F32 U8toF32(U8* data)
	{
	    F32 f=0;
	    memcpy((void*)&f,data,4);
	    return f;
	}

	void FillFrame(U8* Buffer)
	{
	    U16 DataFlag=0,P;
		DataFlag=U8toU16(Buffer[5],Buffer[6]);

		iFlyGFrameObj.DataFlag=DataFlag;
		iFlyGFrameObj.Status=Buffer[7];

		P=8;
		if((DataFlag&0x0001)==0x0001)
		{
			memcpy((U8*)&iFlyGFrameObj.Euler[0],&Buffer[P],6);
			P+=6;
		}
		if((DataFlag&0x0002)==0x0002)
		{
			memcpy((U8*)&iFlyGFrameObj.Q[0],&Buffer[P],16);
			P+=16;
		}
		if((DataFlag&0x0004)==0x0004)
		{
			memcpy((U8*)&iFlyGFrameObj.DEuler[0],&Buffer[P],6);
			P+=6;
		}
		if((DataFlag&0x0008)==0x0008)
		{
			memcpy((U8*)&iFlyGFrameObj.AngRateN[0],&Buffer[P],6);
			P+=6;
		}
		if((DataFlag&0x0010)==0x0010)
		{
			memcpy((U8*)&iFlyGFrameObj.AngRateB[0],&Buffer[P],6);
			P+=6;
		}
		if((DataFlag&0x0020)==0x0020)
		{
			S32 T;
			memcpy((U8*)&iFlyGFrameObj.Pos[0],&Buffer[P],12);
			P+=12;
			T=iFlyGFrameObj.Pos[0];
			iFlyGFrameObj.Pos[0]=iFlyGFrameObj.Pos[1];
			iFlyGFrameObj.Pos[1]=T;
		}
		if((DataFlag&0x0040)==0x0040)
		{
			memcpy((U8*)&iFlyGFrameObj.VelNEDSign,&Buffer[P],7);
			P+=7;
		}
		if((DataFlag&0x0080)==0x0080)
		{
			memcpy((U8*)&iFlyGFrameObj.VelXYZSign,&Buffer[P],7);
			P+=7;
		}
		if((DataFlag&0x0100)==0x0100)
		{
			memcpy((U8*)&iFlyGFrameObj.AccNED[0],&Buffer[P],6);
			P+=6;
		}
		if((DataFlag&0x0200)==0x0200)
		{
			memcpy((U8*)&iFlyGFrameObj.AccXYZ[0],&Buffer[P],6);
			P+=6;
		}
		if((DataFlag&0x0400)==0x0400)
		{
			memcpy((U8*)&iFlyGFrameObj.AirSpeed,&Buffer[P],2);
			P+=2;
		}
		if((DataFlag&0x0800)==0x0800)
		{
			memcpy((U8*)&iFlyGFrameObj.AirHeight,&Buffer[P],4);
			P+=4;
		}
		if((DataFlag&0x1000)==0x1000)
		{
			S32 T;
			memcpy((U8*)&iFlyGFrameObj.GPSPos[0],&Buffer[P],12);
			P+=12;
			T=iFlyGFrameObj.GPSPos[0];
			iFlyGFrameObj.GPSPos[0]=iFlyGFrameObj.GPSPos[1];
			iFlyGFrameObj.GPSPos[1]=T;
		}
		if((DataFlag&0x2000)==0x2000)
		{
			memcpy((U8*)&iFlyGFrameObj.PDOP,&Buffer[P],2);
			P+=2;
		}
		if((DataFlag&0x4000)==0x4000)
		{
			memcpy((U8*)&iFlyGFrameObj.SatNum,&Buffer[P],1);
			P+=1;
			//ÁÙÊ±×´Ì¬±íÊ¾
			iFlyGFrameObj.Status=(iFlyGFrameObj.Status&0x3F);
			iFlyGFrameObj.Status=(iFlyGFrameObj.Status|(iFlyGFrameObj.SatNum&0xC0));

		}
		if((DataFlag&0x8000)==0x8000)
		{
			memcpy((U8*)&iFlyGFrameObj.Year,&Buffer[P],7);
			P+=7;
		}
		{
			memcpy((U8*)&iFlyGFrameObj.Cnt,&Buffer[P],4);
			P+=1;
		}
	}

	void CalcNav(U8* NavU8,F32* NavF32,F64* NavF64)
	{
	    S32 u8p=0;
	    S32 f32p=0;
	    S32 f64p=0;

	    NavU8[u8p++]=(U8)iFlyGFrameObj.DataFlag;//DataFlagL
	    NavU8[u8p++]=(U8)(iFlyGFrameObj.DataFlag>>8);//DataFlagH
	    NavU8[u8p++]=iFlyGFrameObj.Status;//Status
	    //Euler
	    NavF32[f32p++]=iFlyGFrameObj.Euler[0]/10.0;
	    NavF32[f32p++]=iFlyGFrameObj.Euler[1]/10.0;
	    NavF32[f32p++]=iFlyGFrameObj.Euler[2]/10.0;
	    //Q
	    NavF32[f32p++]=iFlyGFrameObj.Q[0];
	    NavF32[f32p++]=iFlyGFrameObj.Q[1];
	    NavF32[f32p++]=iFlyGFrameObj.Q[2];
	    NavF32[f32p++]=iFlyGFrameObj.Q[3];
	    //DEuler
	    NavF32[f32p++]=iFlyGFrameObj.DEuler[0]/10.0;
	    NavF32[f32p++]=iFlyGFrameObj.DEuler[1]/10.0;
	    NavF32[f32p++]=iFlyGFrameObj.DEuler[2]/10.0;
	    //AngRateN
	    NavF32[f32p++]=iFlyGFrameObj.AngRateN[0]/10.0;
	    NavF32[f32p++]=iFlyGFrameObj.AngRateN[1]/10.0;
	    NavF32[f32p++]=iFlyGFrameObj.AngRateN[2]/10.0;
	    //AngRateB
	    NavF32[f32p++]=iFlyGFrameObj.AngRateB[0]/10.0;
	    NavF32[f32p++]=iFlyGFrameObj.AngRateB[1]/10.0;
	    NavF32[f32p++]=iFlyGFrameObj.AngRateB[2]/10.0;
	    //Pos
	    NavF64[f64p++]=iFlyGFrameObj.Pos[0]/1e7;
	    NavF64[f64p++]=iFlyGFrameObj.Pos[1]/1e7;
	    NavF64[f64p++]=iFlyGFrameObj.Pos[2]/1e3;
	    //VelNED
	    NavF32[f32p++]=iFlyGFrameObj.VelNED[0]/100.0*(((iFlyGFrameObj.VelNEDSign&0x01)==0x01)?-1:1);
	    NavF32[f32p++]=iFlyGFrameObj.VelNED[1]/100.0*(((iFlyGFrameObj.VelNEDSign&0x02)==0x02)?-1:1);
	    NavF32[f32p++]=iFlyGFrameObj.VelNED[2]/100.0*(((iFlyGFrameObj.VelNEDSign&0x04)==0x04)?-1:1);
	    //VelXYZ
	    NavF32[f32p++]=iFlyGFrameObj.VelXYZ[0]/100.0*(((iFlyGFrameObj.VelXYZSign&0x01)==0x01)?-1:1);
	    NavF32[f32p++]=iFlyGFrameObj.VelXYZ[1]/100.0*(((iFlyGFrameObj.VelXYZSign&0x02)==0x02)?-1:1);
	    NavF32[f32p++]=iFlyGFrameObj.VelXYZ[2]/100.0*(((iFlyGFrameObj.VelXYZSign&0x04)==0x04)?-1:1);
	    //AccNED
	    NavF32[f32p++]=iFlyGFrameObj.AccNED[0]/100.0;
	    NavF32[f32p++]=iFlyGFrameObj.AccNED[1]/100.0;
	    NavF32[f32p++]=iFlyGFrameObj.AccNED[2]/100.0;
	    //AccXYZ
	    NavF32[f32p++]=iFlyGFrameObj.AccXYZ[0]/100.0;
	    NavF32[f32p++]=iFlyGFrameObj.AccXYZ[1]/100.0;
	    NavF32[f32p++]=iFlyGFrameObj.AccXYZ[2]/100.0;
	    //AirSpeed
	    NavF32[f32p++]=iFlyGFrameObj.AirSpeed;
	    //CaliAirspeed
	    NavF32[f32p++]=iFlyGFrameObj.CaliAirspeed;
	    //Pos
	    NavF64[f64p++]=iFlyGFrameObj.GPSPos[0]/1e7;
	    NavF64[f64p++]=iFlyGFrameObj.GPSPos[1]/1e7;
	    NavF64[f64p++]=iFlyGFrameObj.GPSPos[2]/1e3;
	    //PDOP
	    NavF32[f32p++]=iFlyGFrameObj.PDOP/100.0;
	    //SatNum
	    NavU8[u8p++]=iFlyGFrameObj.SatNum;
	    //DateTime
	    NavU8[u8p++]=(U8)iFlyGFrameObj.Year;//YearL
	    NavU8[u8p++]=(U8)(iFlyGFrameObj.Year>>8);//YearH
	    NavU8[u8p++]=iFlyGFrameObj.Month;//Month
	    NavU8[u8p++]=iFlyGFrameObj.Day;//Day
	    NavU8[u8p++]=iFlyGFrameObj.Hour;//Hour
	    NavU8[u8p++]=iFlyGFrameObj.Minute;//Minute
	    NavU8[u8p++]=iFlyGFrameObj.Second;//Second
	    NavU8[u8p++]=iFlyGFrameObj.Cnt;//Cnt
	}
	U8 G2Byte(U8* Buffer,S32 Offset,S32 Length,U8* NavU8,F32* NavF32,F64* NavF64)
	{
	    U8 isReceived=0;
	    for(S32 i=Offset;i<Offset+Length;i++)
	    {
	        U8 b=Buffer[i];
	        if(receiveState==0)
	        {
	            if(b==S0)
	            {
	                receiveBuffer[receiveState]=b;
	                receiveState=1;
	            }
	        }
	        else if(receiveState==1)
	        {
	            if(b==S1)
	            {
	                receiveBuffer[receiveState]=b;
	                receiveState=2;
	            }
	            else
	            {
	                receiveState=0;
	            }
	        }
	        else if(receiveState==2)
	        {
	            receiveBuffer[receiveState]=b;
	            receiveState=3;
	        }
	        else if(receiveState==3)
	        {
	            receiveBuffer[receiveState]=b;
	            receiveState=4;
	        }
	        else if(receiveState>=4)
	        {
	            receiveBuffer[receiveState]=b;
	            receiveState++;
	            U16 len=(U16)((receiveBuffer[2])|(receiveBuffer[3]<<8));
	            /*if(receiveState==len+4)
	            {
	                if(CheckBuffer(receiveBuffer,len+4))
	                {
	                    FillFrame(receiveBuffer);
	                    CalcNav(NavU8,NavF32,NavF64);
	                    isReceived=1;
	                }
	                receiveState=0;
	            }*/
	            if(len>=256-4)
	            {
	               receiveState=0;
	            }
	            else if(receiveState==len+4)
	            {
	                if(CheckBuffer(receiveBuffer,len+4))
	                {
	                   FillFrame(receiveBuffer);
	                   CalcNav(NavU8,NavF32,NavF64);
	                   isReceived=1;
	                }
	                receiveState=0;
	            }
	        }

	    }
	    return isReceived;
	}
	
	JNIEXPORT jbyte JNICALL Java_com_android_WangTest_nativeCode_ProcessG2Byte(JNIEnv *env, jobject thiz, jbyteArray Buffer, jint Offset, jint Length, jbyteArray NavU8, jfloatArray NavF32, jdoubleArray NavF64)
	{
		S8 isReceived;
		jbyte* bufferp = (env)->GetByteArrayElements(Buffer,0);
		jbyte* navu8p = (env)->GetByteArrayElements(NavU8,0);
		jfloat* navf32p = (env)->GetFloatArrayElements(NavF32,0);
		jdouble* navf64p = (env)->GetDoubleArrayElements(NavF64,0);
		
		isReceived = (S8)G2Byte((U8*)bufferp,Offset,Length,(U8*) navu8p,(F32*) navf32p,(F64*) navf64p);
		
	    (env)->ReleaseByteArrayElements(Buffer,bufferp,0);
	    (env)->ReleaseByteArrayElements(NavU8,navu8p,0);
	    (env)->ReleaseFloatArrayElements(NavF32,navf32p,0);
	    (env)->ReleaseDoubleArrayElements(NavF64,navf64p,0);
	    
	    return isReceived;
	}
}
	
