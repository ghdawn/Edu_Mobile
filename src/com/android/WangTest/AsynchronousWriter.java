package com.android.WangTest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class AsynchronousWriter extends OutputStream
{
    class DataSection
    {
        byte[] Data;
        int Count;
        public int ID;

        public DataSection(int Length)
        {
            Data = new byte[ Length ];
        }

        public int GetLength()
        {
            return Data.length;
        }

        public int GetCount()
        {
            return Count;
        }

        public int GetRemain()
        {
            return GetLength() - GetCount();
        }

        public void ClearData()
        {
            Count = 0;
        }

        public int FillData(byte[] Data, int Offset, int Length)
        {
            int l = GetRemain() < Length ? GetRemain() : Length;
            System.arraycopy(Data, Offset, this.Data, Count, l);
            Count = l;
            return l;
        }

        public int FillData(byte[] Data)
        {
            return FillData(Data, 0, Data.length);
        }
    }

    int DataSectionLength;
    int MaxDataSectionNum;
    OutputStream Stream;

    Semaphore writerSema;

    LinkedList<DataSection> DataSectionsFree = new LinkedList<DataSection>();
    LinkedList<DataSection> DataSectionsUnFree = new LinkedList<DataSection>();

    Object writerLocker = new Object();

    public AsynchronousWriter(OutputStream Stream, int DataSectionLength,
                              int MaxDataSectionNum)
    {
        this.Stream = Stream;
        this.DataSectionLength = DataSectionLength;
        this.MaxDataSectionNum = MaxDataSectionNum;

        writerSema = new Semaphore(1);

        Thread thread = new Thread(new BackgroundWriter());
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    public AsynchronousWriter(OutputStream Stream, int DataSectionLength)
    {
        this(Stream, DataSectionLength, 0);
    }

    public AsynchronousWriter(OutputStream Stream)
    {
        this(Stream, 4096);
    }

    class BackgroundWriter implements Runnable
    {
        public void run()
        {
            while (true)
            {
                try
                {
                    writerSema.acquire();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                if (Stream == null)
                    break;
                WriteBackground();
            }
        }
    }

    private void WriteBackground()
    {
        // System.out.println("Writing:"+DataSectionsUnFree.size());
        // Log.i("AS", "Writing:"+DataSectionsUnFree.size());
        while (DataSectionsUnFree.size() > 0)
        {
            DataSection ds;
            synchronized (writerLocker)
            {
                synchronized (DataSectionsUnFree)
                {
                    if (DataSectionsUnFree.size() <= 0)
                        break;
                    ds = DataSectionsUnFree.remove();
                }
                try
                {
                    // Log.i("AS", "ID:" + ds.ID);
                    Stream.write(ds.Data, 0, ds.GetCount());
                    Stream.flush();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            ds.ClearData();
            synchronized (DataSectionsFree)
            {
                DataSectionsFree.add(ds);
            }
        }
        // Log.i("AS", "End");
    }

    DataSection GetAvailableDataSection()
    {
        synchronized (DataSectionsFree)
        {
            if (DataSectionsFree.size() > 0)
            {
                return DataSectionsFree.removeFirst();
            }
        }
        return new DataSection(DataSectionLength);
    }

    public int ID = 0;

    @Override
    public void write(int b) throws IOException
    {
        byte[] bs = new byte[ 1 ];
        bs[ 0 ] = (byte) b;
        write(bs);
    }

    @Override
    public void write(byte[] buffer) throws IOException
    {
        write(buffer, 0, buffer.length);
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException
    {
        while (count > 0)
        {
            DataSection ds = GetAvailableDataSection();
            int l = ds.GetLength() < count ? ds.GetLength() : count;
            ds.FillData(buffer, offset, l);
            ds.ID = ID;
            ID++;
            offset += l;
            count -= l;
            synchronized (DataSectionsUnFree)
            {
                DataSectionsUnFree.add(ds);
            }
        }
        if (writerSema.availablePermits() == 0)
            writerSema.release();
    }

    @Override
    public void flush() throws IOException
    {
        WriteBackground();
    }

    @Override
    public void close() throws IOException
    {
        flush();
        Stream.close();
        Stream = null;
    }
}
