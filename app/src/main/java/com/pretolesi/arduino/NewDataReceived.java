package com.pretolesi.arduino;

/**
 * Created by RPRETOLESI on 03/02/2015.
 */
public interface NewDataReceived
{
    public abstract void onNewDataReceived(String username, boolean available);
}
