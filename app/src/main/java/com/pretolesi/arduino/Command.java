package com.pretolesi.arduino;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by RPRETOLESI on 06/02/2015.
 */
public class Command extends Object
{
    private static byte SOH = 0x01;
    private static byte EOT = 0x04;

    private ReentrantLock m_LockCommandHolder = null;

    private byte[] m_byteCommandHolder = null;
    private BlockingQueue<byte[]> m_abqCommandQueue;
    private boolean m_bCommandChange;

    public Command()
    {
        m_LockCommandHolder = new ReentrantLock();

        m_byteCommandHolder = new byte[16];
        m_abqCommandQueue = new ArrayBlockingQueue(100);
        m_bCommandChange = false;

        m_byteCommandHolder[0] = SOH;
        m_byteCommandHolder[15] = EOT;
    }

    public void set(byte[] value)
    {
         m_LockCommandHolder.lock();
        try
        {
            m_byteCommandHolder = value;
            m_bCommandChange = true;
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }

    public byte[] get()
    {
        byte[] byteRes = null;
        m_LockCommandHolder.lock();
        try
        {
             byteRes = m_abqCommandQueue.take();

        } catch (InterruptedException ie) {

        } finally
        {
            m_LockCommandHolder.unlock();
        }

        return byteRes;
    }
    public int getLength()
    {
        m_LockCommandHolder.lock();
        try
        {
            return m_byteCommandHolder.length;
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }

    public void setByte(byte value, int index)
    {
        m_LockCommandHolder.lock();
        try
        {
            m_byteCommandHolder[index] = value;
            m_bCommandChange = true;
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }

    public void setThrottleFWD(byte value)
    {
        m_LockCommandHolder.lock();
        try
        {
            m_byteCommandHolder[5] = value;
            m_bCommandChange = true;
        }
        finally
        {
            m_LockCommandHolder.unlock();
         }
    }

    public void setThrottleREV(byte value)
    {
        m_LockCommandHolder.lock();
        try
        {
            m_byteCommandHolder[6] = value;
            m_bCommandChange = true;
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
     }

    public void setDriveFWD(boolean value)
    {
        m_LockCommandHolder.lock();
        try
        {
            m_byteCommandHolder[1] = (byte)(m_byteCommandHolder[1] & 0b11111100);
            if(value == true)
            {
                m_byteCommandHolder[1] = (byte)(m_byteCommandHolder[1] | 0b00000001);
            }
            m_bCommandChange = true;
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }

    public void setDriveREV(boolean value)
    {
        m_LockCommandHolder.lock();
        try
        {
            m_byteCommandHolder[1] = (byte)(m_byteCommandHolder[1] & 0b11111100);
            if(value == true)
            {
                m_byteCommandHolder[1] = (byte)(m_byteCommandHolder[1] | 0b00000010);
            }
            m_bCommandChange = true;
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }

    public void setSteeringLEFT(byte value)
    {
        m_LockCommandHolder.lock();
        try
        {
            m_byteCommandHolder[9] = value;
            m_bCommandChange = true;
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }

    public void setSteeringRIGHT(byte value)
    {
        m_LockCommandHolder.lock();
        try
        {
            m_byteCommandHolder[10] = value;
            m_bCommandChange = true;
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }

    public void setDriveLEFT(boolean value)
    {
        m_LockCommandHolder.lock();
        try
        {
            m_byteCommandHolder[2] = (byte)(m_byteCommandHolder[2] & 0b11111100);
            if(value == true)
            {
                m_byteCommandHolder[2] = (byte)(m_byteCommandHolder[2] | 0b00000001);
            }
            m_bCommandChange = true;
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }

    public void setDriveRIGHT(boolean value)
    {
         m_LockCommandHolder.lock();
        try
        {
            m_byteCommandHolder[2] = (byte)(m_byteCommandHolder[2] & 0b11111100);
            if(value == true)
            {
                m_byteCommandHolder[2] = (byte)(m_byteCommandHolder[2] | 0b00000010);
            }
            m_bCommandChange = true;
         }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }

    public boolean isCommandChange()
    {
        m_LockCommandHolder.lock();
        try
        {
            return m_bCommandChange;
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }

    public boolean setCommand()
    {
        boolean bRes = false;
        m_LockCommandHolder.lock();
        try
        {
            bRes = m_abqCommandQueue.offer(m_byteCommandHolder);
            m_bCommandChange = false;
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
        return bRes;
    }
}
