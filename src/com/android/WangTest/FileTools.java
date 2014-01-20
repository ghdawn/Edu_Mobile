package com.android.WangTest;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class FileTools
{
    public static boolean CreateFolder(String Path)
    {
        File folder = new File(Path);
        if (!folder.exists())
        {
            return folder.mkdirs();
        }
        return true;
    }

    public static File GetFile(String FileName, boolean DelOld)
    {
        File file = new File(FileName);
        if (!CreateFolder(file.getPath()))
        {
            return null;
        }
        if (file.exists() && DelOld)
        {
            file.delete();
        }
        if (!file.exists())
        {
            try
            {
                if (!file.createNewFile())
                {
                    return null;
                }
            } catch (IOException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }

    public static long GetLongTickInMillis()
    {
        Calendar c = Calendar.getInstance();
        return c.getTimeInMillis();
    }

    public static int GetIntTickInMillis()
    {
        return (int) (GetLongTickInMillis() % Integer.MAX_VALUE);
    }
}
