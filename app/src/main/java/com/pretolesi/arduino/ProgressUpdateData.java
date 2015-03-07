package com.pretolesi.arduino;

/**
 * Created by ricca_000 on 07/03/2015.
 */
public class ProgressUpdateData {
    private static final String TAG = "ProgressUpdateData";

    private Status m_sStatus;
    private String m_strError;
    private boolean m_bConnected;

    public ProgressUpdateData(){
        m_sStatus = Status.CLOSED;
        m_strError = "";
        m_bConnected = false;
    }

    public ProgressUpdateData(Status sStatus, String strError, boolean bConnected){
        m_sStatus = sStatus;
        m_strError = strError;
        m_bConnected = bConnected;
    }

    public void setData(Status sStatus, String strError, boolean bConnected){
        m_sStatus = sStatus;
        m_strError = strError;
        m_bConnected = bConnected;
    }

    public void setData(ProgressUpdateData pud){
        m_sStatus = pud.getStatus();
        m_strError = pud.getError();
        m_bConnected = pud.isConnected();
    }

    public void resetData(){
        m_sStatus = Status.CLOSED;
        m_strError = "";
        m_bConnected = false;
    }

    public Status getStatus(){
        return m_sStatus;
    }

    public String getError(){
        return m_strError;
    }

    public boolean isConnected(){
        return m_bConnected;
    }

    public static enum Status
    {
        OFFLINE(0, R.string.comm_status_offline),
        CONNECTING(1, R.string.comm_status_connecting),
        CONNECTED(2, R.string.comm_status_connected),
        ONLINE(3, R.string.comm_status_online),
        ERROR(4, R.string.comm_status_error),
        TIMEOUT(5, R.string.comm_status_timeout),
        CLOSED(6, R.string.comm_status_closed);

        private int ID;
        private int StringResID;

        private Status(int ID, int StringResID) {
            this.ID = ID;
            this.StringResID = StringResID;
        }

        public int getID() {
            return ID;
        }

        public int getStringResID() {
            return StringResID;
        }
    }
}
