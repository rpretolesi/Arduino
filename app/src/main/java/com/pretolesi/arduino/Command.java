package com.pretolesi.arduino;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Command class
 */
public class Command {
//    private static byte ACK = 0x06;

    private ReentrantLock m_LockCommand = null;

    private byte[] m_byteCommandAction = null;
    private boolean m_bCommandActionChanged = false;

    private byte[] m_byteCommandTest = null;
    private BlockingQueue<byte[]> m_abqCommandActionQueue;

    public Command(){
        byte SOH = 0x01;
        byte EOT = 0x04;
        byte ENQ = 0x05;

        m_LockCommand = new ReentrantLock();

        m_byteCommandAction = new byte[16];
        m_byteCommandAction[0] = SOH;
        m_byteCommandAction[15] = EOT;
        m_bCommandActionChanged = false;

        m_byteCommandTest = new byte[16];
        m_byteCommandTest[0] = ENQ;
        m_byteCommandTest[15] = EOT;

        m_abqCommandActionQueue = new ArrayBlockingQueue<>(3);
        m_abqCommandActionQueue.clear();
    }

    public void reset(){
        m_LockCommand.lock();
        try {
            if(m_abqCommandActionQueue != null) {
                m_abqCommandActionQueue.clear();
            }
        }
        finally {
            m_LockCommand.unlock();
        }
    }

    public void set(byte[] value) {
        m_LockCommand.lock();
        try {
            m_byteCommandAction = value;
            m_bCommandActionChanged = true;
        } finally {
            m_LockCommand.unlock();
        }
    }

    public byte[] get() {
        byte[] byteRes = null;
        m_LockCommand.lock();
        try {
//            byteRes = m_abqCommandActionQueue.poll();
//            if(byteRes == null) {
                // No data
                //byteRes = m_byteCommandTest;
                byteRes = m_byteCommandAction;
//            }

        } catch (Exception ex) {

        } finally {
            m_LockCommand.unlock();
        }

        return byteRes;
    }
    public int getLength() {
        m_LockCommand.lock();
        try {
            return m_byteCommandAction.length;
        } finally {
            m_LockCommand.unlock();
        }
    }

    public int getQueueLength() {
        m_LockCommand.lock();
        try {
            return m_abqCommandActionQueue.size();
        } finally {
            m_LockCommand.unlock();
        }
    }

    public void setByte(byte value, int index) {
        m_LockCommand.lock();
        try {
            m_byteCommandAction[index] = value;
            m_bCommandActionChanged = true;
        } finally {
            m_LockCommand.unlock();
        }
    }

    public void setThrottleFWD(byte value) {
        m_LockCommand.lock();
        try {
            m_byteCommandAction[5] = value;
            m_bCommandActionChanged = true;
        } finally {
            m_LockCommand.unlock();
         }
    }

    public void setThrottleREV(byte value) {
        m_LockCommand.lock();
        try {
            m_byteCommandAction[6] = value;
            m_bCommandActionChanged = true;
        } finally {
            m_LockCommand.unlock();
        }
     }

    public void setDriveFWD(boolean value) {
        m_LockCommand.lock();
        try {
            m_byteCommandAction[1] = (byte)(m_byteCommandAction[1] & 0b11111100);
            if(value == true) {
                m_byteCommandAction[1] = (byte)(m_byteCommandAction[1] | 0b00000001);
            }
            m_bCommandActionChanged = true;
        } finally {
            m_LockCommand.unlock();
        }
    }

    public void setDriveREV(boolean value) {
        m_LockCommand.lock();
        try {
            m_byteCommandAction[1] = (byte)(m_byteCommandAction[1] & 0b11111100);
            if(value == true) {
                m_byteCommandAction[1] = (byte)(m_byteCommandAction[1] | 0b00000010);
            }
            m_bCommandActionChanged = true;
        } finally {
            m_LockCommand.unlock();
        }
    }

    public void setSteeringLEFT(byte value) {
        m_LockCommand.lock();
        try {
            m_byteCommandAction[7] = value;
            m_bCommandActionChanged = true;
        } finally {
            m_LockCommand.unlock();
        }
    }

    public void setSteeringRIGHT(byte value) {
        m_LockCommand.lock();
        try {
            m_byteCommandAction[8] = value;
            m_bCommandActionChanged = true;
        } finally {
            m_LockCommand.unlock();
        }
    }

    public void setDriveLEFT(boolean value) {
        m_LockCommand.lock();
        try {
            m_byteCommandAction[1] = (byte)(m_byteCommandAction[1] & 0b11001111);
            if(value == true) {
                m_byteCommandAction[1] = (byte)(m_byteCommandAction[1] | 0b00010000);
            }
            m_bCommandActionChanged = true;
        } finally {
            m_LockCommand.unlock();
        }
    }

    public void setDriveRIGHT(boolean value) {
        m_LockCommand.lock();
        try {
            m_byteCommandAction[1] = (byte)(m_byteCommandAction[1] & 0b11001111);
            if(value == true) {
                m_byteCommandAction[1] = (byte)(m_byteCommandAction[1] | 0b00100000);
            }
            m_bCommandActionChanged = true;
        } finally {
            m_LockCommand.unlock();
        }
    }

    public boolean isCommandChange() {
        m_LockCommand.lock();
        try {
            return m_bCommandActionChanged;
        } finally {
            m_LockCommand.unlock();
        }
    }

    public boolean setCommand() {
        boolean bRes = false;
        m_LockCommand.lock();
        try {
            bRes = m_abqCommandActionQueue.offer(m_byteCommandAction);
            m_bCommandActionChanged = false;
        } finally {
            m_LockCommand.unlock();
        }
        return bRes;
    }
}
