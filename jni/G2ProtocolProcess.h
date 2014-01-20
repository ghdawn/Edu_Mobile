#ifndef G2PP
#define G2PP

typedef	unsigned char   U8;
typedef	signed	char	S8;
typedef	unsigned    int U32;
typedef	signed	int 	S32;
typedef	float			F32;
typedef	double		    F64;
typedef	unsigned    short	U16;
typedef	signed	short   S16;
typedef	unsigned    long    long	U64;
typedef	signed	long	long	S64;

//数据类型转换函数
#define U8toU16(x, y) ((U16)(((U16)(y))<<8|((U16)(x))))
#define U8toS16(x, y) ((S16)(((U16)(y))<<8|((U16)(x))))

#define U8toU32(x1,x2,x3,x4) ((U32)(((U32)(x4))<<24|((U32)(x3))<<16|((U32)(x2))<<8|((U16)(x1))))
#define U8toS32(x1,x2,x3,x4) ((S32)(((U32)(x4))<<24|((U32)(x3))<<16|((U32)(x2))<<8|((U16)(x1))))

#define U16toU8(x) (U8*)(&(x))
#define S16toU8(x) (U8*)(&(x))

#pragma pack(1)
struct _iFlyGFrameStruct
{
	U8 S1;//0xA5
	U8 S2;//0x5A
	U16 Length;//0x0062
	U8 Command;//0x61

	/*
	bit0-15:
	0 欧拉角
	1 四元数
	2 欧拉角变化率
	3 角速度（导航系）
	4 角速度（本体系）
	5 位置
	6 NED速度
	7 XYZ速度
	8 NED加速度
	9 XYZ加速度
	10 空速
	11 气压高度
	12 GPS位置
	13 GPS定位精度
	14 卫星数
	15 时间（年月日时分秒）
	*/
	U16 DataFlag;

	//bit0:0=AHRS/DR 1=GPS/INS
	//bit1:0=磁航向 1=GPS航向
	//bit2:1=使用外部位置参考
	//bit3:1=使用外部导航系速度参考
	//bit4:1=使用外部本体系速度参考
	//bit5:1=使用外部姿态参考
	//bit6-7:0=无差分 1=单点定位 2=伪距差分 3=载波相位差分
	/*
	0 NavMode:0=AHRS/DR 1=GPS/INS
	1 UseVelYaw:0=磁航向 1=GPS航向
	2 PosRef:1=使用外部位置参考
	3 VelNEDRef:1=使用外部导航系速度参考
	4 VelXYZRef:1=使用外部本体系速度参考
	5 AttRef:1=使用外部姿态参考
	6 GryoErr:1=陀螺超量程
	7 MagErr:1=磁场受到干扰
	*/
	U8 Status;


	//姿态
	S16 Euler[3];//0.1°
	F32 Q[4];
	S16 DEuler[3];//0.1°/s
	//角速度
	S16 AngRateN[3];//0.1°/s
	S16 AngRateB[3];//0.1°/s
	//位置
	S32 Pos[3];
	//速度
	U8 VelNEDSign;
	U16 VelNED[3];//0.01m/s
	U8 VelXYZSign;
	U16 VelXYZ[3];//0.01m/s
	//加速度
	S16 AccNED[3];//0.01m/s^2
	S16 AccXYZ[3];//0.01m/s^2
	//空速
	U8 AirSpeed;
	U8 CaliAirspeed;
	//气压高度
	S32 AirHeight;
	//GPS原始数据
	S32 GPSPos[3];
	//定位精度PDOP
	U16 PDOP;//0.01m
	//卫星数
	U8 SatNum;//bit0-5:卫星个数 bit6-7:0=无差分 1=单点定位 2=伪距差分 3=载波相位差分
	//时间
	U16 Year;
	U8 Month;
	U8 Day;
	U8 Hour;
	U8 Minute;
	U8 Second;
	//计数器和CRC
	U8 Cnt;
	U16 CRC;
	//校验和
	U8  CheckSum;
};
typedef struct _iFlyGFrameStruct iFlyGFrameStruct;

/*
U8 Section=12
DataFlagL
DataFlagH
Status
SatNum
YearH
YearL
Month
Day
Hour
Minute
Second
Cnt

F32 Section=32
Euler[3]
Q[4]
DEuler[3]
AngRateN[3]
AngRateB[3]
VelNED[3]
VelXYZ[3]
AccNED[3]
AccXYZ[3]
AirSpeed
CaliAirspeed
AirHeight
PDOP

F64 Section=6
Pos[3]
GPSPos[3]
*/

U8 ProcessG2Byte(U8* Buffer,S32 Offset,S32 Length,U8* NavU8,F32* NavF32,F64* NavF64);

#endif
