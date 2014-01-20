package com.android.WangTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Handler;

public class TCPHelper
{
    int state;
    public String[] stateMsg = { "Unconnect", "Connected", "Disconnected" };

    Socket socket;
    Handler handler;

    public TCPHelper(Handler handler)
    {
        this.handler = handler;
    }

    public Boolean Connect(InetAddress Address, int Port)
    {
        Close();
        try
        {
            socket = new Socket(Address, Port);
            UpdateState(1);
            return true;
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
            UpdateState(0);
        } catch (IOException e)
        {
            e.printStackTrace();
            UpdateState(0);
        }
        return false;
    }

    public void Close()
    {
        if (IsConnected())
        {
            try
            {
                socket.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            UpdateState(2);
        }
    }

    public int GetState()
    {
        return state;
    }

    void UpdateState(int State)
    {
        this.state = State;
        if (handler != null)
        {
            /*
            Message msg = handler.obtainMessage(AndroidTCPServerActivity.MSG_SERVER_START_ERROR);
			msg.obj = "ServerSocket MSG_SERVER_START_ERROR";
			msg.sendToTarget();
			*/
        }
    }

    public Boolean IsConnected()
    {
        return socket != null && socket.isConnected();
    }

    public InputStream GetInputStream()
    {
        InputStream in = null;
        try
        {
            in = socket.getInputStream();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return in;
    }

    public OutputStream GetOutputStream()
    {
        OutputStream out = null;
        if (!IsConnected())
            return null;
        try
        {
            out = socket.getOutputStream();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return out;
    }
}
