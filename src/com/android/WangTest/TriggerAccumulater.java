package com.android.WangTest;

import java.util.Timer;
import java.util.TimerTask;

public class TriggerAccumulater
{
    float invInterval;
    int triggers;
    int lastTriggers;
    float triggerFrequency;

    public TriggerAccumulater(int CalcInterval)
    {
        if (CalcInterval <= 0)
        {
            throw new IllegalArgumentException("Interval must >0");
        }
        invInterval = 1000f / CalcInterval;
        Timer timer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                triggerFrequency = (triggers - lastTriggers) * invInterval;
                lastTriggers = triggers;
            }
        };
        timer.scheduleAtFixedRate(task, 0, CalcInterval);
    }

    public float GetTriggerFrequency()
    {
        return triggerFrequency;
    }

    public void Trigger()
    {
        triggers++;
    }

    public int GetTriggers()
    {
        return triggers;
    }

    public void Reset()
    {
        triggers = 0;
        lastTriggers = 0;
        triggerFrequency = 0;
    }
}
