package com.pretolesi.arduino;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.util.AttributeSet;

/**
 *  Custom EditText
 */
public class PretolesiEditText extends EditText {

    private float m_fmin, m_fmax;

    // Default Constructor
    public PretolesiEditText(Context context) {
        super(context);
        m_fmin = 0.0f;
        m_fmax = 0.0f;
    }

    public PretolesiEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_fmin = 0.0f;
        m_fmax = 0.0f;
    }

    public PretolesiEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        m_fmin = 0.0f;
        m_fmax = 0.0f;
    }

    // Custom Constructor
    public PretolesiEditText(Context context, float fmin, float fmax) {
        super(context);
        m_fmin = fmin;
        m_fmax = fmax;
    }

    public PretolesiEditText(Context context, AttributeSet attrs, float fmin, float fmax) {
        super(context, attrs);
        m_fmin = fmin;
        m_fmax = fmax;
    }

    public PretolesiEditText(Context context, AttributeSet attrs, int defStyle, float fmin, float fmax) {
        super(context, attrs, defStyle);
        m_fmin = fmin;
        m_fmax = fmax;
    }

    public void setInputLimit(float fmin, float fmax) {
        m_fmin = fmin;
        m_fmax = fmax;
    }
    public void setInputLimit(int imin, int imax) {
        this.m_fmin = imin;
        this.m_fmax = imax;
    }

    public boolean validateInputLimit(){
        float input = 0.0f;
        try {
            input = Float.parseFloat(this.getText().toString());
        }
        catch (Exception ignored){
        }

        if((m_fmin == 0.f) && (m_fmax == 0.0f)) {
            return true;
        }
        if (!isInRange(m_fmin, m_fmax, input)) {
            makeDialogAlert();
            requestFocus();
            return false;
        }
        return true;
    }

    private boolean isInRange(float a, float b, float c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }

    private void makeDialogAlert() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(String.valueOf(m_fmin) + " - " + String.valueOf(m_fmax))
                .setTitle(R.string.dialog_ifmm_title);

        builder.setPositiveButton(R.string.dialog_ifmm_ok_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}
