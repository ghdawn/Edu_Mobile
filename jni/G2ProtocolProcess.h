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

//��������ת������
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
	0 ŷ����
	1 ��Ԫ��
	2 ŷ���Ǳ仯��
	3 ���ٶȣ�����ϵ��
	4 ���ٶȣ�����ϵ��
	5 λ��
	6 NED�ٶ�
	7 XYZ�ٶ�
	8 NED���ٶ�
	9 XYZ���ٶ�
	10 ����
	11 ��ѹ�߶�
	12 GPSλ��
	13 GPS��λ����
	14 ������
	15 ʱ�䣨������ʱ���룩
	*/
	U16 DataFlag;

	//bit0:0=AHRS/DR 1=GPS/INS
	//bit1:0=�ź��� 1=GPS����
	//bit2:1=ʹ���ⲿλ�òο�
	//bit3:1=ʹ���ⲿ����ϵ�ٶȲο�
	//bit4:1=ʹ���ⲿ����ϵ�ٶȲο�
	//bit5:1=ʹ���ⲿ��̬�ο�
	//bit6-7:0=�޲�� 1=���㶨λ 2=α���� 3=�ز���λ���
	/*
	0 NavMode:0=AHRS/DR 1=GPS/INS
	1 UseVelYaw:0=�ź��� 1=GPS����
	2 PosRef:1=ʹ���ⲿλ�òο�
	3 VelNEDRef:1=ʹ���ⲿ����ϵ�ٶȲο�
	4 VelXYZRef:1=ʹ���ⲿ����ϵ�ٶȲο�
	5 AttRef:1=ʹ���ⲿ��̬�ο�
	6 GryoErr:1=���ݳ�����
	7 MagErr:1=�ų��ܵ�����
	*/
	U8 Status;


	//��̬
	S16 Euler[3];//0.1��
	F32 Q[4];
	S16 DEuler[3];//0.1��/s
	//���ٶ�
	S16 AngRateN[3];//0.1��/s
	S16 AngRateB[3];//0.1��/s
	//λ��
	S32 Pos[3];
	//�ٶ�
	U8 VelNEDSign;
	U16 VelNED[3];//0.01m/s
	U8 VelXYZSign;
	U16 VelXYZ[3];//0.01m/s
	//���ٶ�
	S16 AccNED[3];//0.01m/s^2
	S16 AccXYZ[3];//0.01m/s^2
	//����
	U8 AirSpeed;
	U8 CaliAirspeed;
	//��ѹ�߶�
	S32 AirHeight;
	//GPSԭʼ����
	S32 GPSPos[3];
	//��λ����PDOP
	U16 PDOP;//0.01m
	//������
	U8 SatNum;//bit0-5:���Ǹ��� bit6-7:0=�޲�� 1=���㶨λ 2=α���� 3=�ز���λ���
	//ʱ��
	U16 Year;
	U8 Month;
	U8 Day;
	U8 Hour;
	U8 Minute;
	U8 Second;
	//��������CRC
	U8 Cnt;
	U16 CRC;
	//У���
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
