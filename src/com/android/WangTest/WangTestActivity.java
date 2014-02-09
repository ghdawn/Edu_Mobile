package com.android.WangTest;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WangTestActivity extends Activity
{

    private Button bttakeoff, btland, btNav, btHover;

    // For Debug
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;

    // ����֡��
    private TextView fpsView;// ����Ƶ��
    private SensorManager mSensorManager = null;
    private Sensor mSensor = null;
    SensorEventListener sensorListener;
    DatagramSocket socket;
    int Running = 1;

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
                    SendCMD(0, 0, 0, 0);
                } catch (IOException e)
                {
                    fpsView.setText("Send Error");
                }
            } else if (view == btNav)
            {
                mSensorManager.registerListener(sensorListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
            } else if (view == btHover)
            {
                mSensorManager.unregisterListener(sensorListener);
                SendCMD(0, 0, 0, 0);
            }
        }
    }

    void SendCMD(float left_right, float front_back, float up_down, float rotate)
    {
        String atcmd = "AT*PCMD=" + Running + ",1,"
                + Float.floatToRawIntBits(left_right)
                + "," + Float.floatToRawIntBits(front_back)
                + "," + Float.floatToRawIntBits(up_down)
                + "," + Float.floatToRawIntBits(rotate) + "\r";
        byte[] data = atcmd.getBytes();
        fpsView.setText("x:" + left_right + "  y:" + front_back);
        try
        {
            DatagramPacket pack = new DatagramPacket(data, data.length, InetAddress.getByName("192.168.1.1"), 5556);
            socket.send(pack);
        } catch (IOException e)
        {
            fpsView.setText("Send Error");
        }
        ++Running;
    }

    class Navigation implements View.OnTouchListener
    {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent)
        {
            float x = motionEvent.getX() - 400;
            float y = motionEvent.getY() - 400;
            if (Math.abs(x) < 20)
                x = 0;
            else if (x > 0) x = 0.1f;
            else x = -0.1f;
            if (Math.abs(y) < 20)
                y = 0;
            else if (y > 0) y = 0.1f;
            else y = -0.1f;

            SendCMD(x, y, 0, 0);
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


        fpsView = (TextView) findViewById(R.id.textView);
        fpsView.setVisibility(0);

        bttakeoff = (Button) findViewById(R.id.bttakeoff);
        bttakeoff.setOnClickListener(new ButtonCommand());
        btland = (Button) findViewById(R.id.btland);
        btland.setOnClickListener(new ButtonCommand());
        btHover = (Button) findViewById(R.id.bthover);
        btHover.setOnClickListener(new ButtonCommand());
        btNav = (Button) findViewById(R.id.btNav);
        btNav.setOnClickListener(new ButtonCommand());

        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorListener = new SensorEventListener()
        {

            @Override
            public void onSensorChanged(SensorEvent event)
            {
                float x = event.values[ SensorManager.DATA_X ];
                float y = event.values[ SensorManager.DATA_Y ];

                int range = 2;
                float speed = 0.1f;
                if ((x + range) < 0) x = speed;
                else if ((x - range) > 0) x = -speed;
                else x = 0;

                if ((y + range) < 0) y = speed;
                else if ((y - range) > 0) y = -speed;
                else y = 0;
                SendCMD(y, x, 0, 0);
                //fpsView.setText(str);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy)
            {
                // TODO Auto-generated method stub

            }
        };

        try
        {
            socket = new DatagramSocket();
        } catch (Exception e)
        {
            fpsView.setText("Socket Init Error");
        }

    }


    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.layout.menu, menu);
        return true;
    }

}