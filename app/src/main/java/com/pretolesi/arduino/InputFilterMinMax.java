package com.pretolesi.arduino;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.Spanned;

/**
 *
 */
public class InputFilterMinMax implements InputFilter {

    private float m_min, m_max;
    private Context m_context;

    public InputFilterMinMax(int min, int max) {
        this.m_min = min;
        this.m_max = max;
    }

    public InputFilterMinMax(Context context, float min, float max) {
        this.m_context = context;
        this.m_min = min;
        this.m_max = max;
    }

    public InputFilterMinMax(String min, String max) {
        this.m_min = Integer.parseInt(min);
        this.m_max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            float input = Float.parseFloat(dest.toString() + source.toString());
            if (isInRange(m_min, m_max, input))
                return null;
        } catch (NumberFormatException nfe) { }
        makeDialogAlert();
        return "";
    }

    private boolean isInRange(float a, float b, float c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }

    private void makeDialogAlert() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(m_context);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(String.valueOf(m_min) + " - " + String.valueOf(m_max))
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
