package com.pretolesi.arduino;

import java.util.ArrayList;

/**
 * Created by RPRETOLESI on 03/02/2015.
 */
public interface CommunicationStatus
{
    public abstract void onNewCommunicationStatus(String[] strStatus);
}
