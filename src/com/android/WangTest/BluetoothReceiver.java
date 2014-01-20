package com.android.WangTest;

public interface BluetoothReceiver
{
    public void OnReceiveMessage(int what, int arg1, int arg2, Object obj);

    public void OnReceiveData(byte[] Data);
}
