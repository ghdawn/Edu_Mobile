package com.android.WangTest;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.*;

public class ImageTools
{
    public static boolean RecRaw(String FileName, byte[] Raw)
    {
        File file = FileTools.GetFile(FileName, true);
        if (file != null)
        {
            try
            {
                FileOutputStream Stream = new FileOutputStream(FileName);
                Stream.write(Raw);
                Stream.flush();
                Stream.close();
                return true;
            } catch (FileNotFoundException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean RecJpeg(String FileName, byte[] YUV420SP, int Width, int Height, int Quality)
    {
        YuvImage yuvimage = new YuvImage(YUV420SP, ImageFormat.NV21, Width, Height, null);
        ByteArrayOutputStream jpegStream = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, Width, Height), Quality, jpegStream);
        return RecRaw(FileName, jpegStream.toByteArray());
    }

    public static byte[] Yuv420SP2Jpeg(byte[] yuv, int ImageFormat, int width, int height)
    {
        YuvImage yuv420 = new YuvImage(yuv, ImageFormat, width, height, null);
        ByteArrayOutputStream jpegStream = new ByteArrayOutputStream();
        yuv420.compressToJpeg(new Rect(0, 0, width, height), 90, jpegStream);
        return jpegStream.toByteArray();
    }
}
