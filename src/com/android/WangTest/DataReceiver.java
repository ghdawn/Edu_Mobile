package com.android.WangTest;

import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: ghdawn
 * Date: 13-10-29
 * Time: 下午3:47
 * To change this template use File | Settings | File Templates.
 */
public class DataReceiver implements BluetoothReceiver
{
    @Override
    public void OnReceiveMessage(int what, int arg1, int arg2, Object obj)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void OnReceiveData(byte[] Data)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < Data.length; ++i)
            sb.append(Data[ i ]);
        Log.d("hhhhh", sb.toString());
        //Toast.makeText(window,Data.toString(),Toast.LENGTH_LONG).show();
    }
}
