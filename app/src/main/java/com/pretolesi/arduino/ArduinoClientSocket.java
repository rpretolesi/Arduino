package com.pretolesi.arduino;

import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

/**
 * Created by RPRETOLESI on 11/02/2015.
 */
public class ArduinoClientSocket
{
    private static byte ENQ = 0x05;

    private Context m_context = null;
    private Socket m_clientSocket = null;
    private SocketAddress m_socketAddress = null;
    private DataOutputStream m_dataOutputStream = null;
    private DataInputStream m_dataInputStream = null;
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

            if(cmd != null)
            {
                cmd.reset();
            }

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
                byte[] byteCmd = cmd.get();
                if(byteCmd != null)
                {
                    m_dataOutputStream.write(byteCmd, 0, byteCmd.length);
                }
                else
                {
                    m_dataOutputStream.write(ENQ);
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

    public boolean getData()
    {
        boolean bRes = false;
        if (m_dataInputStream != null)
        {
/*
            try
            {
                byte[] byteCmd = new byte[16];
                for(int i = 0; i < 1; i++)
//                for(int i = 0; i < 16; i++)
                {
                    byteCmd[i] = m_dataInputStream.readByte();
                }
                // m_dataInputStream.read(byteCmd, 0, byteCmd.length);

                m_strLastError = "";
                bRes = true;
            }
            catch (SocketTimeoutException ex)
            {
                m_strLastError = m_context.getString(R.string.comm_status_timeout);
                closeConnection();
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
*/
            // attendo per non sovraccaricare CPU
            try {
                Thread.sleep(100, 0);
            } catch (InterruptedException e) {
            }
            bRes = true;
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
