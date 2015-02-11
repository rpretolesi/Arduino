package com.pretolesi.arduino;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by RPRETOLESI on 11/02/2015.
 */
public class ArduinoClientSocket extends Socket
{
    private SocketAddress m_socketAddress = null;
    private DataOutputStream m_dataOutputStream = null;
    private DataInputStream m_dataInputStream = null;
    private String m_strLastError = "";

    public boolean connectToArduino(String strHost, int iPort, int iTimeout)
    {
        boolean bRes = false;
        try
        {
            m_socketAddress = new InetSocketAddress(strHost , iPort);
            super.connect(m_socketAddress);
            super.setSoTimeout(iTimeout);

            m_dataOutputStream = new DataOutputStream(super.getOutputStream());
            m_dataInputStream = new DataInputStream(super.getInputStream());

            bRes = true;
            m_strLastError = "";
        }
        catch (Exception ex)
        {
            m_strLastError = ex.getMessage();

            m_socketAddress = null;
            // close Output stream
            if (m_dataOutputStream != null)
            {
                try
                {
                    m_dataOutputStream.close();
                }
                catch (IOException ioex_1)
                {
                    m_strLastError = ioex_1.getMessage();
                }
            }
            // close Input stream
            if (m_dataInputStream != null)
            {
                try
                {
                    m_dataInputStream.close();
                }
                catch (IOException ioex_2)
                {
                    m_strLastError = ioex_2.getMessage();
                }
            }
        }
        finally
        {
        }

        return bRes;
    }

    public boolean sendCommand(Command cmd)
    {
        boolean bRes = false;
        if (m_dataInputStream != null)
        {
            try
            {
                m_dataOutputStream.write(cmd.get(), 0, cmd.getLength());
                m_strLastError = "";
            }
            catch (Exception ex)
            {
                m_strLastError = ex.getMessage();
            }
        }
        return bRes;
    }

    public String getLastError()
    {
        return m_strLastError;
    }
}
