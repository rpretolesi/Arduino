package com.pretolesi.arduino;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by RPRETOLESI on 06/02/2015.
 */
public class Command
{
    private static byte SOH = 0x01;
    private static byte EOT = 0x04;

    private ReentrantLock m_LockCommandHolder = null;
    private byte[] m_byteCommandHolder = null;

    public Command()
    {
        m_LockCommandHolder = new ReentrantLock();

        m_byteCommandHolder = new byte[16];
        m_byteCommandHolder[0] = SOH;
        m_byteCommandHolder[15] = EOT;
    }

    public void setCommand(byte[] value)
    {
        m_LockCommandHolder.lock();
        try
        {
            m_byteCommandHolder = value;
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }

    public byte[] getCommand()
    {
        m_LockCommandHolder.lock();
        try
        {
            return m_byteCommandHolder;
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }

    public byte getCommandByte(int index)
    {
        m_LockCommandHolder.lock();
        try
        {
            return m_byteCommandHolder[index];
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }

    public void setCommandByte(byte value, int index)
    {
        m_LockCommandHolder.lock();
        try
        {
            m_byteCommandHolder[index] = value;
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }

    public void setCommandFWD(boolean value)
    {
        m_LockCommandHolder.lock();
        try
        {
            m_byteCommandHolder[1] = (byte)(m_byteCommandHolder[1] & 0b11111100);
            if(value == true)
            {
                m_byteCommandHolder[1] = (byte)(m_byteCommandHolder[1] | 0b00000001);
            }
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }
    public void setCommandREV(boolean value)
    {
        m_LockCommandHolder.lock();
        try
        {
            m_byteCommandHolder[1] = (byte)(m_byteCommandHolder[1] & 0b11111100);
            if(value == true)
            {
                m_byteCommandHolder[1] = (byte)(m_byteCommandHolder[1] | 0b00000010);
            }
        }
        finally
        {
            m_LockCommandHolder.unlock();
        }
    }
}
