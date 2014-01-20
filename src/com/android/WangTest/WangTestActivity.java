package com.android.WangTest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class WangTestActivity extends Activity implements
        SurfaceHolder.Callback, Camera.PreviewCallback
{
    // �������
    private Camera mCamera = null;
    private SurfaceView PicSurfaceView;
    DataReceiver receiver;
    // ��������
    // For Debug
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    //
    private int previewWidth;
    private int previewHeight;
    //
    private byte[][] imageBuffer;

    public File file = new File("/sdcard/color.bin");// �ɼ�yuv�������ļ�������
    boolean shot = false;
    // ��ݼ�¼����
    public boolean PictureRecOn = false;
    public boolean DataRecOn = false;
    // ����֡��
    private TextView fpsView;// ����Ƶ��
    private TriggerAccumulater tAccumulater;
    // ��ݴ��洢����
    PackageData packDataObj;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // �ؼ�TextView��ʾ֡��
        fpsView = (TextView) findViewById(R.id.textView);
        fpsView.setVisibility(0);
        // ��ʼ��actualSurfaceView
        PicSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        PicSurfaceView.getHolder().addCallback(this);
        PicSurfaceView.getHolder().setType(
                SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        receiver = new DataReceiver();
        // ����������ݽ��ն���

        // ��ʼ�������豸
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
        mChatService.BluetoothReceiverObj = receiver;
        // Initialize the buffer for outgoing messages
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null)
        {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // ֡�ʼ����߳�
        tAccumulater = new TriggerAccumulater(1000);
        // �����ݴ洢�����ʼ��
        packDataObj = new PackageData();
    }

    // Surface������д
    public void surfaceCreated(SurfaceHolder holder)
    {
        mCamera = Camera.open();
        Parameters parameters = mCamera.getParameters();
        previewWidth = parameters.getPreviewSize().width;
        previewHeight = parameters.getPreviewSize().height;
        int prvFormat = parameters.getPreviewFormat();
        int bytesPerPix = ImageFormat.getBitsPerPixel(prvFormat);
        mCamera.setDisplayOrientation(90);
        // ����ͼ�񻺳���
        int imageBufferLength = (int) (bytesPerPix * previewWidth
                * previewHeight / 8);
        //imageBuffer = new byte[imageBufferLength];
        imageBuffer = new byte[ 32 ][];
        //imageBuffer = new byte[4][];
        for (int i = 0; i < imageBuffer.length; i++)
        {
            imageBuffer[ i ] = new byte[ imageBufferLength ];
        }
        mCamera.setParameters(parameters);
        mCamera.setPreviewCallbackWithBuffer(this);
        try
        {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException exception)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height)
    {
        Parameters parameters = mCamera.getParameters();
        //parameters.setPreviewSize(width, height);
        mCamera.setParameters(parameters);
        for (int i = 0; i < imageBuffer.length; i++)
        {
            mCamera.addCallbackBuffer(imageBuffer[ i ]);
        }
        mCamera.startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        if (mCamera != null)
        {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.layout.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.pictureRec:
                PictureRecOn = true;

                break;
            case R.id.dataRec:
                DataRecOn = true;

                break;
            case R.id.OpenBT:
                if (!mBluetoothAdapter.isEnabled())
                {
                    Intent enableIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                break;
            case R.id.deviceList:
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                break;
            case R.id.StopNav:
                DataRecOn = false;

                break;
            case R.id.StopPicture:
                PictureRecOn = false;
                try
                {
                    packDataObj.DataRec(PictureRecOn, "", null, 0);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                break;
            case R.id.shot:
                shot = true;
                break;
            case R.id.End:
                mCamera.release();
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
                System.exit(0);
                break;
            default:
                break;
        }
        return false;
    }

    int PreviewDiv = 0;
    int lastTick = 0;
    int Running = 0;

    public void getmlr(byte[] data)
    {

        int i = 0;
        int j = 0;
        int temLeft = 0;
        int temRight = 0;
        int pLeft, pRight;
        int bFoundLeft = 0;
        int bFoundRight = 0;
        int TripLen = 4;


        int bLeftEnd = 0;
        int bRightEnd = 0;
        int bMidEnd = 0;


        BlackLineData[ Img_row - 1 ] = LastFieldMid1;
        for (i = Img_row - 2; i > 3 && bMidEnd != 1; i--)
        {

            bFoundLeft = 0;
            bFoundRight = 0;

            for (pLeft = BlackLineData[ i + 1 ]; pLeft > 2; pLeft--)
            {
                if (data[ i * previewWidth + pLeft ] >= 0)
                {

                    bFoundLeft = 1;
                    LeftBlack[ i ] = pLeft;
                    pLeft = 1;

                }
            }
            if (bFoundLeft != 1)
                LeftBlack[ i ] = 1;

            for (pRight = BlackLineData[ i + 1 ]; pRight < Img_col - 2; pRight++)
            {
                if (data[ i * previewWidth + pRight ] >= 0 && data[ i * previewWidth + pRight + 2 ] >= 0)
                {
                    bFoundRight = 1;
                    RightBlack[ i ] = pRight;
                    pRight = Img_col;

                }
            }
            if (bFoundRight != 1)
                RightBlack[ i ] = Img_col - 1;

            BlackLineData[ i ] = (LeftBlack[ i ] + RightBlack[ i ]) / 2;

            LastFieldMid1 = BlackLineData[ Img_row - 5 ];
            LastFieldMid2 = BlackLineData[ Img_row - 6 ];
        }
    }

    int[] BlackLineData = new int[ 480 ];
    int[] LeftBlack = new int[ 480 ];
    int[] RightBlack = new int[ 480 ];
    int Img_row = 480;
    int Img_col = 640;

    int LastFieldMid1 = 320;
    int LastFieldMid2 = 320;

    public void onPreviewFrame(byte[] data, Camera camera)
    {
        // TODO:: haliluya
        Running++;
        int line = 150;
        int mid = previewWidth / 2;
        int left = 0, right = 0;

        /*
        for (int i=mid;i>=0 &&data[line*previewWidth+i]<0;--i,right++);
        for (int i=mid;i<previewWidth &&data[line*previewWidth+i]<0;++i,left++);
        */
        getmlr(data);

        int Err = (BlackLineData[ 240 ]
                + BlackLineData[ 241 ] +
                BlackLineData[ 239 ] +
                BlackLineData[ 238 ] +
                BlackLineData[ 237 ]) / 5 - 320;
        Err = Err / 5 + 50;

        //int res= (right-left)*20/mid+50;
        if (Err > 60) Err = 60;
        if (Err < 40) Err = 40;
        byte[] result = new byte[ 1 ];
        result[ 0 ] = (byte) (Err);
        fpsView.setText(right + " " + left + "s:" + result[ 0 ]);
        if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
            mChatService.write(result);
        camera.addCallbackBuffer(data);
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {

                case MESSAGE_STATE_CHANGE:
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1)
                    {
                        case BluetoothChatService.STATE_CONNECTED:
                            Toast.makeText(WangTestActivity.this, "connected",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            Toast.makeText(WangTestActivity.this, "connecting",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                            Toast.makeText(WangTestActivity.this, "listen",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothChatService.STATE_NONE:
                            Toast.makeText(WangTestActivity.this, "none",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
            }
        }
    };

    // �ص�����deviceList����WangTestActivity����ѡ���豸��������
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode)
        {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK)
                {
                    // Get the device MAC address
                    String address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);
                }
                break;
            default:
                break;
        }
    }
}