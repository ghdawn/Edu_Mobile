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
import android.os.StrictMode;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WangTestActivity extends Activity implements
        SurfaceHolder.Callback, Camera.PreviewCallback
{
    // �������
    private Camera mCamera = null;
    private SurfaceView PicSurfaceView;
    private Button bttakeoff, btland;
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

    DatagramSocket socket;

    class ButtonCommand implements View.OnClickListener
    {

        @Override
        public void onClick(View view)
        {
            if (view == btland)
            {
                byte[] data;
                int num = 290717696;
                String atcmd = "AT*REF=" + Running + "," + num + "\r";
                data = atcmd.getBytes();
                try
                {
                    DatagramPacket pack = new DatagramPacket(data, data.length, InetAddress.getByName("192.168.1.1"), 5556);
                    socket.send(pack);
                } catch (IOException e)
                {
                    fpsView.setText("Send Error");
                }
            } else if (view == bttakeoff)
            {
                byte[] data;
                int num = 290718208;
                String atcmd = "AT*REF=" + Running + "," + num + "\r";
                data = atcmd.getBytes();

                try
                {
                    DatagramPacket pack = new DatagramPacket(data, data.length, InetAddress.getByName("192.168.1.1"), 5556);
                    socket.send(pack);
                } catch (IOException e)
                {
                    fpsView.setText("Send Error");
                }
            }
        }
    }

    class Navigation implements View.OnTouchListener
    {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent)
        {
            float x = motionEvent.getX() - previewWidth / 2;
            float y = motionEvent.getY() - previewHeight / 2;
            if (Math.abs(x) < 20)
                x = 0;
            else if (x > 0) x = 0.1f;
            else x = -0.1f;
            if (Math.abs(y) < 20)
                y = 0;
            else if (y > 0) y = 0.1f;
            else y = -0.1f;

            byte[] data;

            int left_right = Float.floatToRawIntBits(x);
            int front_back = Float.floatToRawIntBits(y);
            String atcmd = "AT*PCMD=" + Running + ",1," + left_right + "," + front_back + ",0,0\r";
            data = atcmd.getBytes();
            fpsView.setText("x:" + x + "  y:" + y);
            try
            {
                DatagramPacket pack = new DatagramPacket(data, data.length, InetAddress.getByName("192.168.1.1"), 5556);
                socket.send(pack);
            } catch (IOException e)
            {
                fpsView.setText("Send Error");
            }
            return true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        // �ؼ�TextView��ʾ֡��
        fpsView = (TextView) findViewById(R.id.textView);
        fpsView.setVisibility(0);

        bttakeoff = (Button) findViewById(R.id.bttakeoff);
        bttakeoff.setOnClickListener(new ButtonCommand());
        btland = (Button) findViewById(R.id.btland);
        btland.setOnClickListener(new ButtonCommand());

        // ��ʼ��actualSurfaceView
        PicSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        PicSurfaceView.getHolder().addCallback(this);
        PicSurfaceView.getHolder().setType(
                SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        PicSurfaceView.setOnTouchListener(new Navigation());
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
        try
        {
            socket = new DatagramSocket();
        } catch (Exception e)
        {
            fpsView.setText("Socket Init Error");
        }

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
        int imageBufferLength = bytesPerPix * previewWidth
                * previewHeight / 8;
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
        for (byte[] anImageBuffer : imageBuffer)
        {
            mCamera.addCallbackBuffer(anImageBuffer);
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

    int Running = 0;

    public void onPreviewFrame(byte[] data, Camera camera)
    {
        // TODO:: haliluya
        Running++;

        byte[] result = new byte[ 1 ];
        result[ 0 ] = (byte) (0);
        //fpsView.setText(right + " " + left + "s:" + result[ 0 ]);
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