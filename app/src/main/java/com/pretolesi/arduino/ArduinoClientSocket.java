package com.pretolesi.arduino;

import android.content.Context;
import android.util.Log;

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
 * Created by Riccardo Pretolesi on 11/02/2015.
 */
public class ArduinoClientSocket
{
    private static final String TAG = "ArduinoClientSocket";

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
    private boolean m_bWaitingForData = false;

    public ArduinoClientSocket(Context context){
        m_context = context;
    }

    public boolean connectToArduino(Message msg, String strHost, int iPort, int iTimeout)
    {
        boolean bRes = false;
        try
        {
            // Prima chiudo la connessione
            closeConnection(msg);

            m_socketAddress = new InetSocketAddress(strHost , iPort);
            if(m_clientSocket == null)
            {
                m_clientSocket = new Socket();
                if(m_clientSocket != null)
                {
                    m_clientSocket.setSoTimeout(iTimeout);
                    m_clientSocket.connect(m_socketAddress);
                    m_dataOutputStream = new DataOutputStream(m_clientSocket.getOutputStream());
                    m_dataInputStream = new DataInputStream(m_clientSocket.getInputStream());

                    m_strLastError = "";
                    bRes = true;

                    Log.d(TAG,"connectToArduino->" + "Connected");
                }
            }
            m_timeMillisecondsSend = System.currentTimeMillis();
            m_timeMillisecondsGet = System.currentTimeMillis();

            m_byteInputStreamBuf = new byte[16];
            m_NrOfByteInInputStreamBuf = 0;
            m_bWaitingForData = false;
        }
        catch (Exception ex)
        {
            Log.d(TAG,"connectToArduino->" + "Exception ex : " + ex.getMessage());
            m_strLastError = ex.getMessage();
            closeConnection(msg);
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

    public boolean sendData(Message msg)
    {
        boolean bRes = false;
        if (m_dataOutputStream != null)       {
            try
            {
                if(!m_bWaitingForData)
                {
                    byte[] byteCmd = null;
                    try {
                        byteCmd = msg.getCommand();
                    }
                    catch (Exception ex_1) {
                        Log.d(TAG,"sendData->" + "Exception ex_1 : " + ex_1.getMessage());
                        m_strLastError = ex_1.getMessage();
                        bRes = false;
                        closeConnection(msg);
                    }
                    if(byteCmd != null) {
                        m_dataOutputStream.write(byteCmd, 0, byteCmd.length);
                        m_bWaitingForData = true;
                    }
                }

                m_strLastError = "";
                bRes = true;
            }
            catch (Exception ex_2) {
                Log.d(TAG,"sendData->" + "Exception ex_2 : " + ex_2.getMessage());
                m_strLastError = ex_2.getMessage();
                closeConnection(msg);
            }
        }

        m_timeMillisecondsSend = System.currentTimeMillis();

        return bRes;
    }

    public boolean getData(Message msg)
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
                    if(iByteRead != 16)
                    {
                        Log.d(TAG, "getData->" + "(iByteRead > 0), iByteRead : " + iByteRead + ", m_NrOfByteInInputStreamBuf = " + m_NrOfByteInInputStreamBuf);
                    }

                    if(m_NrOfByteInInputStreamBuf == 16)
                    {
                        m_NrOfByteInInputStreamBuf = 0;
                        m_bWaitingForData = false;

                        if((m_byteInputStreamBuf[0] == ACK) && (m_byteInputStreamBuf[15] == EOT))
                        {
                            msg.setData(m_byteInputStreamBuf);
                            m_strLastError = "";
                            bRes = true;
                        }
                        else
                        {
                            // Error
                            Log.d(TAG,"getData->" + "(m_byteInputStreamBuf[0] != ACK) || (m_byteInputStreamBuf[15] != EOT)");
                            m_strLastError = "Protocol Error.";
                            closeConnection(msg);
                        }
                        // Reset
                        Arrays.fill(m_byteInputStreamBuf, (byte) 0);
                    } else {
                        m_strLastError = "";
                        bRes = true;
                    }
                } else if(iByteRead < 0) {
                    Log.d(TAG,"getData->" + "(iByteRead < 0)");
                    m_strLastError = "Stream closed";
                    bRes = false;
                    closeConnection(msg);
                } else {
                    Log.d(TAG,"getData->" + "(iByteRead = 0)");
                    m_strLastError = "";
                    bRes = true;
                }

            } catch (SocketTimeoutException stex) {
                Log.d(TAG,"getData->" + "SocketTimeoutException stex : " + stex.getMessage());
                m_strLastError = m_context.getString(R.string.comm_status_timeout);
                closeConnection(msg);
            } catch (EOFException eofex) {
                Log.d(TAG,"getData->" + "EOFException eofex : " + eofex.getMessage());
                m_strLastError = m_context.getString(R.string.comm_status_eof);
                closeConnection(msg);
            } catch (Exception ex) {
                Log.d(TAG,"getData->" + "Exception ex : " + ex.getMessage());
                m_strLastError = ex.getMessage();
                closeConnection(msg);
            }
        }

        m_timeMillisecondsGet = System.currentTimeMillis();

        return bRes;
    }

    public long getSendGetAnswerTimeMilliseconds()
    {
        return (m_timeMillisecondsGet - m_timeMillisecondsSend);
    }

    public long getGetSendAnswerTimeMilliseconds()
    {
        return (m_timeMillisecondsSend - m_timeMillisecondsGet);
    }

    public void closeConnection(Message msg)
    {
        Log.d(TAG,"closeConnection->" + "closeConnection");

        if(msg != null) {
            msg.resetData();
        }

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
