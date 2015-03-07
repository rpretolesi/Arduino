package com.pretolesi.arduino;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Command class
 */
public class Message {
    private static final String TAG = "Message";

    //    private static byte ACK = 0x06;
    private static byte SOH = 0x01;
    private static byte EOT = 0x04;
    private static byte ENQ = 0x05;

    private ReentrantLock m_Lock = null;

    // Dati da inviare
    private byte[] m_byteCommand = null;
    private boolean m_bCommandActionSet = false;
    private boolean m_bCommandSetAsToSend = false;

    // Dati Ricevuti
    private byte[] m_byteData = null;

    public Message(){

        m_Lock = new ReentrantLock();

        m_byteCommand = new byte[16];

        m_byteCommand[0] = SOH;
        m_byteCommand[15] = EOT;

        m_bCommandActionSet = false;
        m_bCommandSetAsToSend = false;

        m_byteData = new byte[16];
    }

    // Richiesta dati
    public void setData(byte[] byteData) {
        m_Lock.lock();
        try {
            m_byteData = Arrays.copyOf(byteData, m_byteData.length);
        }
        finally {
            m_Lock.unlock();
        }
    }

    public int getDataByte(int index) {
        m_Lock.lock();
        try {
            return (m_byteData[index] & 0xFF);
        } finally {
            m_Lock.unlock();
        }
    }

    public void setCommandAsToSend() {
        m_Lock.lock();
        try {
            m_bCommandSetAsToSend = true;
        } catch (Exception ex) {

        } finally {
            m_Lock.unlock();
        }
    }

    public byte[] getCommand() {

        m_Lock.lock();
        try {
            byte[] byteRes = null;

            if(m_bCommandSetAsToSend) {
                m_byteCommand[0] = SOH;
                byteRes = Arrays.copyOf(m_byteCommand, m_byteCommand.length); ;

                m_bCommandActionSet = false;
                m_bCommandSetAsToSend = false;
            } else {
                byteRes = new byte[1];
                byteRes[0] = ENQ;
            }

            return byteRes;

        } catch (Exception ignored) {
        } finally {
            m_Lock.unlock();
        }

        return null;
    }

    public void resetCommand() {

        m_Lock.lock();
        try {
                m_byteCommand[0] = SOH;
                m_byteCommand[1] = 0;
                m_byteCommand[2] = 0;
                m_byteCommand[3] = 0;
                m_byteCommand[4] = 0;
                m_byteCommand[5] = 0;
                m_byteCommand[6] = 0;
                m_byteCommand[7] = 0;
                m_byteCommand[8] = 0;
                m_byteCommand[9] = 0;
                m_byteCommand[10] = 0;
                m_byteCommand[11] = 0;
                m_byteCommand[12] = 0;
                m_byteCommand[13] = 0;
                m_byteCommand[14] = 0;
                m_byteCommand[15] = EOT;

                m_bCommandActionSet = true;

        } catch (Exception ignored) {
        } finally {
            m_Lock.unlock();
        }
    }

    public void resetData() {

        m_Lock.lock();
        try {
            m_byteData[0] = 0;
            m_byteData[1] = 0;
            m_byteData[2] = 0;
            m_byteData[3] = 0;
            m_byteData[4] = 0;
            m_byteData[5] = 0;
            m_byteData[6] = 0;
            m_byteData[7] = 0;
            m_byteData[8] = 0;
            m_byteData[9] = 0;
            m_byteData[10] = 0;
            m_byteData[11] = 0;
            m_byteData[12] = 0;
            m_byteData[13] = 0;
            m_byteData[14] = 0;
            m_byteData[15] = 0;

        } catch (Exception ignored) {
        } finally {
            m_Lock.unlock();
        }
    }

    public int getActionLength() {
        m_Lock.lock();
        try {
            return m_byteCommand.length;
        } finally {
            m_Lock.unlock();
        }
    }

    public void setActionByte(byte value, int index) {
        m_Lock.lock();
        try {
            m_byteCommand[index] = value;
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }

    public int getActionByte(int index) {
        m_Lock.lock();
        try {
            return (m_byteCommand[index] & 0xFF);
        } finally {
            m_Lock.unlock();
        }
    }
    public void setActionBit(int index, int bit, boolean value) {
        m_Lock.lock();
        int iMask;
        try {
            switch (bit) {
                case 1: iMask = 0b00000001;
                    break;
                case 2: iMask = 0b00000010;
                    break;
                case 3: iMask = 0b00000100;
                    break;
                case 4: iMask = 0b00001000;
                    break;
                case 5: iMask = 0b00010000;
                    break;
                case 6: iMask = 0b00100000;
                    break;
                case 7: iMask = 0b01000000;
                    break;
                case 8: iMask = 0b10000000;
                    break;
                default: iMask = 0b00000000;
                    break;
            }
            if(value) {
                m_byteCommand[index] = (byte)(m_byteCommand[index] | iMask);
            } else {
                m_byteCommand[index] = (byte)(m_byteCommand[index] & ~iMask);
            }

            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }

    public void setThrottleFWD(byte value) {

        m_Lock.lock();
        try {
            m_byteCommand[5] = value;
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
         }
    }

    public void setThrottleREV(byte value) {
        m_Lock.lock();
        try {
            m_byteCommand[6] = value;
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
     }

    public void setDriveWheelFWD(boolean value) {
        m_Lock.lock();
        try {
            m_byteCommand[1] = (byte)(m_byteCommand[1] & 0b11111100);
            if(value == true) {
                m_byteCommand[1] = (byte)(m_byteCommand[1] | 0b00000001);
            }
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }

    public boolean getDriveWheelFWD() {
        m_Lock.lock();
        try {
            return (m_byteData[1] & 0b00000001) > 0 ? true : false;
        } finally {
            m_Lock.unlock();
        }
    }

    public void setDriveWheelREV(boolean value) {
        m_Lock.lock();
        try {
            m_byteCommand[1] = (byte)(m_byteCommand[1] & 0b11111100);
            if(value == true) {
                m_byteCommand[1] = (byte)(m_byteCommand[1] | 0b00000010);
            }
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }

    public boolean getDriveWheelREV() {
        m_Lock.lock();
        try {
            //int iRes = m_byteCommandData[1] & 0b00000001;
            return (m_byteData[1] & 0b00000010) > 0 ? true : false;
        } finally {
            m_Lock.unlock();
        }
    }

    public void setSteeringLEFT(byte value) {
        m_Lock.lock();
        try {
            m_byteCommand[7] = value;
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }

    public void setSteeringRIGHT(byte value) {
        m_Lock.lock();
        try {
            m_byteCommand[8] = value;
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }

    public void setDriveWheelLEFT(boolean value) {
        m_Lock.lock();
        try {
            m_byteCommand[1] = (byte)(m_byteCommand[1] & 0b11001111);
            if(value == true) {
                m_byteCommand[1] = (byte)(m_byteCommand[1] | 0b00010000);
            }
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }

    public boolean getDriveWheelLEFT() {
        m_Lock.lock();
        try {
            //int iRes = m_byteCommandData[1] & 0b00000001;
            return (m_byteData[1] & 0b00010000) > 0 ? true : false;
        } finally {
            m_Lock.unlock();
        }
    }


    public void setDriveWheelRIGHT(boolean value) {
        m_Lock.lock();
        try {
            m_byteCommand[1] = (byte)(m_byteCommand[1] & 0b11001111);
            if(value == true) {
                m_byteCommand[1] = (byte)(m_byteCommand[1] | 0b00100000);
            }
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }

    public boolean getDriveWheelRIGHT() {
        m_Lock.lock();
        try {
            //int iRes = m_byteCommandData[1] & 0b00000001;
            return (m_byteData[1] & 0b00100000) > 0 ? true : false;
        } finally {
            m_Lock.unlock();
        }
    }

    // Fork
    public void setDriveForkUp(boolean value) {
        m_Lock.lock();
        try {
            m_byteCommand[2] = (byte)(m_byteCommand[2] & 0b11111100);
            if(value == true) {
                m_byteCommand[2] = (byte)(m_byteCommand[2] | 0b00000001);
            }
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }
    public boolean getDriveForkUp() {
        m_Lock.lock();
        try {
            //int iRes = m_byteCommandData[1] & 0b00000001;
            return (m_byteData[2] & 0b00000001) > 0 ? true : false;
        } finally {
            m_Lock.unlock();
        }
    }
    public boolean getDriveForkUpStatus() {
        m_Lock.lock();
        try {
            //int iRes = m_byteCommandData[1] & 0b00000001;
            return (m_byteData[2] & 0b00000100) > 0 ? true : false;
        } finally {
            m_Lock.unlock();
        }
    }

    public void setDriveForkDown(boolean value) {
        m_Lock.lock();
        try {
            m_byteCommand[2] = (byte)(m_byteCommand[2] & 0b11111100);
            if(value == true) {
                m_byteCommand[2] = (byte)(m_byteCommand[2] | 0b00000010);
            }
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }
    public boolean getDriveForkDown() {
        m_Lock.lock();
        try {
            //int iRes = m_byteCommandData[1] & 0b00000001;
            return (m_byteData[2] & 0b00000010) > 0 ? true : false;
        } finally {
            m_Lock.unlock();
        }
    }
    public boolean getDriveForkDownStatus() {
        m_Lock.lock();
        try {
            //int iRes = m_byteCommandData[1] & 0b00000001;
            return (m_byteData[2] & 0b00001000) > 0 ? true : false;
        } finally {
            m_Lock.unlock();
        }
    }

    public void setDriveSpeedForkUP(byte value) {
        m_Lock.lock();
        try {
            m_byteCommand[9] = value;
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }
    public void setDriveSpeedForkDOWN(byte value) {
        m_Lock.lock();
        try {
            m_byteCommand[10] = value;
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }

    public void setDriveForkOpen(boolean value) {
        m_Lock.lock();
        try {
            m_byteCommand[2] = (byte)(m_byteCommand[2] & 0b11001111);
            if(value == true) {
                m_byteCommand[2] = (byte)(m_byteCommand[2] | 0b00010000);
            }
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }
    public boolean getDriveForkOpen() {
        m_Lock.lock();
        try {
            return (m_byteData[2] & 0b00010000) > 0 ? true : false;
        } finally {
            m_Lock.unlock();
        }
    }
    public boolean getDriveForkOpenStatus() {
        m_Lock.lock();
        try {
            //int iRes = m_byteCommandData[1] & 0b00000001;
            return (m_byteData[2] & 0b01000000) > 0 ? true : false;
        } finally {
            m_Lock.unlock();
        }
    }

    public void setDriveForkClose(boolean value) {
        m_Lock.lock();
        try {
            m_byteCommand[2] = (byte)(m_byteCommand[2] & 0b11001111);
            if(value == true) {
                m_byteCommand[2] = (byte)(m_byteCommand[2] | 0b00100000);
            }
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }
    public boolean getDriveForkClose() {
        m_Lock.lock();
        try {
            //int iRes = m_byteCommandData[1] & 0b00000001;
            return (m_byteData[2] & 0b00100000) > 0 ? true : false;
        } finally {
            m_Lock.unlock();
        }
    }
    public boolean getDriveForkCloseStatus() {
        m_Lock.lock();
        try {
            //int iRes = m_byteCommandData[1] & 0b00000001;
            return (m_byteData[2] & 0b10000000) > 0 ? true : false;
        } finally {
            m_Lock.unlock();
        }
    }

    public void setDriveSpeedForkOPEN(byte value) {
        m_Lock.lock();
        try {
            m_byteCommand[11] = value;
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }
    public void setDriveSpeedForkCLOSE(byte value) {
        m_Lock.lock();
        try {
            m_byteCommand[12] = value;
            m_bCommandActionSet = true;
        } finally {
            m_Lock.unlock();
        }
    }

    public boolean isCommandActionChanged() {
        m_Lock.lock();
        try {
            return m_bCommandActionSet;
        } finally {
            m_Lock.unlock();
        }
    }

}
