package com.pretolesi.arduino;

import java.util.ArrayList;

/**
 * Created by RPRETOLESI on 03/02/2015.
 */
public interface ProgressUpdate
{

    public abstract void onProgressUpdate(ProgressUpdateData[] pud);
    public abstract void onProgressUpdateConnectionChanged(ProgressUpdateData[] pud);
}
