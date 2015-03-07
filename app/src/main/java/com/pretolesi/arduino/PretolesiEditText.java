package com.pretolesi.arduino;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;
import android.util.AttributeSet;
import android.widget.Toast;

/**
 *  Custom EditText
 */
public class PretolesiEditText extends EditText {

    private static final String TAG = "PretolesiEditText";


    private ScaleGestureDetector m_ScaleGestureDetector;
    private GestureDetector m_GestureDetector;

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(m_ScaleGestureDetector != null){
            m_ScaleGestureDetector.onTouchEvent(event);
        }
        if(m_GestureDetector != null){
            m_GestureDetector.onTouchEvent(event);
        }

        return super.onTouchEvent(event);
    }

    public void setAsReadOnly() {
        setKeyListener(null);
        if(m_ScaleGestureDetector == null){
            m_ScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener(){
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    float size = getTextSize();
                    float factor = detector.getScaleFactor();
                    float product = size*factor;
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, product);

                    return true;
                }
            });
        }

        if(m_GestureDetector == null){
            m_GestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener(){

                @Override
                public boolean onDown(MotionEvent e) {
                    return false;
                }

                @Override
                public void onShowPress(MotionEvent e) {

                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    setSelection(0);
                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    return false;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    selectAll();
                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(getContext().getString(R.string.drive_title_section_sample_code).toUpperCase(), getText());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getContext(), getContext().getString(R.string.drive_title_section_sample_code).toUpperCase() + " " + "Copied and Ready to Paste", Toast.LENGTH_SHORT).show();
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    return false;
                }
            });
        }
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
