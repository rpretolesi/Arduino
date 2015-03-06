package com.pretolesi.arduino;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by RPRETOLESI on 11/02/2015.
 */
public class AlarmListAdapter extends BaseAdapter
{
    private static final String TAG = "AlarmListAdapter";

    private ArrayList<String> m_alstr = new ArrayList<>();

    private final Context context;

    // the context is needed to inflate views in getView()
    public AlarmListAdapter(Context context) {
        this.context = context;
    }

    public void updateData(ArrayList<String> alstr)
    {
        this.m_alstr = alstr;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return m_alstr.size();
    }

    @Override
    public String getItem(int position) {
        return m_alstr.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
/*
        if (convertView == null)
        {
            convertView = LayoutInflater.from(context).inflate(R.layout.cb_drive_fragment, parent, false);
        }

        TextView tvTextView = ViewHolder.get(convertView, R.id.alarmlist_id_tv_alarm_text);

        String str = getItem(position);

        if (str != null)
        {
            tvTextView.setText(str);
        }
*/
        return convertView;

    }
}
