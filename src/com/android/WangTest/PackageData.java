package com.android.WangTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

;

public class PackageData
{
    OutputStream Stream;
    AsynchronousWriter aWriter;

    public synchronized boolean DataRec(boolean State, String FileName, byte[] data, int counter) throws IOException
    {
        int length;
        byte[] lenTemp = new byte[ 4 ];
        byte[] contourTemp = new byte[ 4 ];
        if (State == true)
        {
            if (Stream == null)
            {

                if (FileTools.GetFile(FileName, true) == null)
                    return false;
                Stream = new AsynchronousWriter(new FileOutputStream(FileName), 32 * 1024, 0);
                /*
                Thread thread=new Thread(){
					public void run()
					{
						byte[] ip={-64,-88,1,99};
						int port=9000;
						TCPHelper tHelper = new TCPHelper(null);
						InetAddress locAddress;
						try
						{
							locAddress = InetAddress.getByAddress(ip);
							tHelper.Connect(locAddress,port);
							Stream = tHelper.GetOutputStream();
						}
						catch (UnknownHostException e)
						{
							e.printStackTrace();
						}
						
					}
				};
				thread.start();
				*/
            }
            if (Stream != null)
            {
                length = data.length;
                for (int i = 0; i < lenTemp.length; i++)
                {
                    lenTemp[ i ] = (byte) ((length >> (i * 8)) & 0xff);
                    contourTemp[ i ] = (byte) (((counter >> (i * 8)) & 0xff));
                }
                byte[] input = new byte[ 8 + data.length ];
                System.arraycopy(lenTemp, 0, input, 0, lenTemp.length);
                System.arraycopy(contourTemp, 0, input, lenTemp.length, contourTemp.length);
                System.arraycopy(data, 0, input, lenTemp.length + contourTemp.length, data.length);
                Stream.write(input);
                Stream.flush();
            }
        } else
        {
            if (Stream != null)
            {
                Stream.close();
                Stream = null;
            }
        }
        return false;
    }
}