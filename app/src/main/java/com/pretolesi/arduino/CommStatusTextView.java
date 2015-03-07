package com.pretolesi.arduino;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by ricca_000 on 07/03/2015.
 */
public class CommStatusTextView extends TextView  {

    public CommStatusTextView(Context context) {
        super(context);
    }

    public CommStatusTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommStatusTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setStatusAndError(ProgressUpdateData.Status sStatus, String strError){

        setText(this.getContext().getString(sStatus.getStringResID()) + " - " + strError);

        switch (sStatus){
            case OFFLINE:
                setTextColor(Color.GRAY);
                break;
            case CONNECTING:
                setTextColor(Color.YELLOW);
                break;
            case CONNECTED:
                setTextColor(Color.GREEN);
                break;
            case ONLINE:
                setTextColor(Color.GREEN);
                break;
            case ERROR:
                setTextColor(Color.RED);
                break;
            case TIMEOUT:
                setTextColor(Color.BLUE);
                break;
            case CLOSED:
                setTextColor(Color.RED);
                break;
        }

    }
}

