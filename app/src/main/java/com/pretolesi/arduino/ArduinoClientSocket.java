package com.pretolesi.arduino;

import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 * Created by RPRETOLESI on 11/02/2015.
 */
public class ArduinoClientSocket
{
    private static byte SOH = 0x01;
    private static byte EOT = 0x04;
    private static byte ENQ = 0x05;
    private static byte ACK = 0x06;

    private Context m_context = null;
    private Socket m_clientSocket = null;
    private SocketAddress m_socketAddress = null;
    private DataOutputStream m_dataOutputStream = null;
    private DataInputStream m_dataInputStream = null;
    private byte m_byteInputStreamBuf[] = null;
    private int m_NrOfByteInInputStreamBuf = 0;
    private String m_strLastError = "";
    private long m_timeMillisecondsSend = 0;
    private long m_timeMillisecondsGet = 0;

    public ArduinoClientSocket(Context context){
        m_context = context;
    }

    public boolean connectToArduino(String strHost, int iPort, int iTimeout, Command cmd)
    {
        boolean bRes = false;
        try
        {
            // Prima chiudo la connessione
            closeConnection();

            m_socketAddress = new InetSocketAddress(strHost , iPort);
            if(m_clientSocket == null)
            {
                m_clientSocket = new Socket();
                if(m_clientSocket != null)
                {
                    m_clientSocket.connect(m_socketAddress);
                    m_clientSocket.setSoTimeout(iTimeout);
                    m_dataOutputStream = new DataOutputStream(m_clientSocket.getOutputStream());
                    m_dataInputStream = new DataInputStream(m_clientSocket.getInputStream());
                    bRes = true;
                    m_strLastError = "";
                }
            }
            m_timeMillisecondsSend = 0;
            m_timeMillisecondsGet = 0;
            m_byteInputStreamBuf = new byte[16];
            m_NrOfByteInInputStreamBuf = 0;
        }
        catch (Exception ex)
        {
            m_strLastError = ex.getMessage();
            closeConnection();
        }
        finally
        {
        }

        return bRes;
    }
    public boolean isConnected() {
        boolean bRes = false;
        if (m_clientSocket != null && m_dataInputStream != null && m_dataInputStream != null) {
            bRes = m_clientSocket.isConnected();
        }
        return bRes;
    }

    public boolean sendData(Command cmd)
    {
        boolean bRes = false;
        if (m_dataOutputStream != null)
        {
            try
            {
                byte[] byteCmd = null;
                try {
                    byteCmd = Arrays.copyOf(cmd.getCommandAction(), cmd.getCommandAction().length);
                }
                catch (Exception ex) {
                }
                if(byteCmd != null)
                {
                    m_dataOutputStream.write(byteCmd, 0, byteCmd.length);
                }

                m_strLastError = "";
                bRes = true;
            }
            catch (Exception ex)
            {
                m_strLastError = ex.getMessage();
                closeConnection();
            }
        }

        m_timeMillisecondsSend = System.currentTimeMillis();

        return bRes;
    }

    public boolean getData(Command cmd)
    {
        boolean bRes = false;
        if (m_dataInputStream != null)
        {

            try
            {
                int iByteRead = 0;

                iByteRead = m_dataInputStream.read(m_byteInputStreamBuf, m_NrOfByteInInputStreamBuf, m_byteInputStreamBuf.length - m_NrOfByteInInputStreamBuf);
                if(iByteRead > 0)
                {
                    m_NrOfByteInInputStreamBuf = m_NrOfByteInInputStreamBuf + iByteRead;
                    if(m_NrOfByteInInputStreamBuf == 16)
                    {
                        m_NrOfByteInInputStreamBuf = 0;
                        if((m_byteInputStreamBuf[0] == ACK) && (m_byteInputStreamBuf[15] == EOT))
                        {
                            cmd.setCommandData(m_byteInputStreamBuf);
                            Arrays.fill(m_byteInputStreamBuf, (byte)0);
                        }
                    }
                }

                m_strLastError = "";
                bRes = true;
            } catch (SocketTimeoutException ex) {
                m_strLastError = "";
                bRes = true;
//                m_strLastError = m_context.getString(R.string.comm_status_timeout);
//                closeConnection();
            } catch (EOFException eofx) {
                m_strLastError = m_context.getString(R.string.comm_status_eof);
                closeConnection();
            } catch (Exception ex) {
                m_strLastError = ex.getMessage();
                closeConnection();
            }
        }

        m_timeMillisecondsGet = System.currentTimeMillis();

        return bRes;
    }
/*
    public boolean getData1(Command cmd)
    {
        boolean bRes = false;
        if (m_dataInputStream != null)
        {

            try
            {
                //int iByteRead = 0;
                byte byteRead = 0;

                while(true)
                {

                    byteRead = m_dataInputStream.readByte();
//                iByteRead = m_dataInputStream.read(m_byteInputStreamBuf, m_NrOfByteInInputStreamBuf, m_byteInputStreamBuf.length - m_NrOfByteInInputStreamBuf);
                    m_byteInputStreamBuf[m_NrOfByteInInputStreamBuf] = byteRead;
                    //               if(iByteRead > 0)
                    //               {
//                m_NrOfByteInInputStreamBuf = m_NrOfByteInInputStreamBuf + iByteRead;
                    m_NrOfByteInInputStreamBuf = m_NrOfByteInInputStreamBuf + 1;
                    if(m_NrOfByteInInputStreamBuf == 16)
                    {
                        m_NrOfByteInInputStreamBuf = 0;
                        if((m_byteInputStreamBuf[0] == ACK) && (m_byteInputStreamBuf[15] == EOT))
                        {
                            cmd.setData(m_byteInputStreamBuf);
                        }
                    }
                    //              }
                }

                //          m_strLastError = "";
                //               bRes = true;
            }
            catch (SocketTimeoutException ex)
            {
                m_strLastError = "";
                bRes = true;
//                m_strLastError = m_context.getString(R.string.comm_status_timeout);
//                closeConnection();
            }
            catch (EOFException eofx)
            {
                m_strLastError = m_context.getString(R.string.comm_status_eof);
                closeConnection();
            }
            catch (Exception ex)
            {
                m_strLastError = ex.getMessage();
                closeConnection();
            }


//            bRes = true;

        }

        m_timeMillisecondsGet = System.currentTimeMillis();

        return bRes;
    }
*/
    public long getSendGetAnswerTimeMilliseconds()
    {
        return (m_timeMillisecondsGet - m_timeMillisecondsSend);
    }

    public long getGetSendAnswerTimeMilliseconds()
    {
        return (m_timeMillisecondsSend - m_timeMillisecondsGet);
    }

    public void closeConnection()
    {
        m_socketAddress = null;

        // Chiudo il socket
        if(m_clientSocket != null)
        {
            try
            {
                m_clientSocket.close();
            } catch (IOException ioex_1)
            {
                m_strLastError = ioex_1.getMessage();
            }
        }
        m_clientSocket = null;

        // close Output stream
        if (m_dataOutputStream != null)
        {
            try
            {
                m_dataOutputStream.close();
            }
            catch (IOException ioex_2)
            {
                m_strLastError = ioex_2.getMessage();
            }
        }
        m_dataOutputStream = null;

        // close Input stream
        if (m_dataInputStream != null)
        {
            try
            {
                m_dataInputStream.close();
            }
            catch (IOException ioex_3)
            {
                m_strLastError = ioex_3.getMessage();
            }
        }
        m_dataInputStream = null;
    }

    public String getLastError()
    {
        return m_strLastError;
    }
}
