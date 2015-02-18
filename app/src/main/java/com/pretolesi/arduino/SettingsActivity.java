package com.pretolesi.arduino;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

import SQL.SQLContract;

/**
 * Settings Activity
 */
public class SettingsActivity extends ActionBarActivity
{
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            Fragment fragment = null;
            if (position == 0)
            {
                fragment = SettingServerFragment.newInstance(position + 1);
            }
            if (position == 1)
            {
                fragment = SettingSensorFragment.newInstance(position + 1);
            }

            return fragment;
        }

        @Override
        public int getCount() {
            // Show xx total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.settings_title_setting_server).toUpperCase(l);
                case 1:
                    return getString(R.string.settings_title_setting_sensor).toUpperCase(l);
//                case 2:
//                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SettingServerFragment extends Fragment
    {

        private EditText m_settings_id_et_server_ip_address;
        private EditText m_settings_id_et_server_port;
        private EditText m_settings_id_et_timeout;
        private EditText m_settings_id_et_comm_frame_delay;
        private Button m_settings_id_btn_save_server;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static SettingServerFragment newInstance(int sectionNumber) {
            SettingServerFragment fragment = new SettingServerFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public SettingServerFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.setting_server_fragment, container, false);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            super.onActivityCreated(savedInstanceState);

            m_settings_id_et_server_ip_address = (EditText) getActivity().findViewById(R.id.settings_id_et_server_ip_address);
            m_settings_id_et_server_port = (EditText) getActivity().findViewById(R.id.settings_id_et_server_port);
            m_settings_id_et_timeout = (EditText) getActivity().findViewById(R.id.settings_id_et_timeout);
            m_settings_id_et_comm_frame_delay = (EditText) getActivity().findViewById(R.id.settings_id_et_comm_frame_delay);
            m_settings_id_btn_save_server = (Button) getActivity().findViewById(R.id.settings_id_btn_save_server);


            // Set an OnClickListener
            m_settings_id_btn_save_server.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    boolean bSaveStatus = true;
                    String strIpAddress = m_settings_id_et_server_ip_address.getText().toString();
                    String strPort = m_settings_id_et_server_port.getText().toString();
                    String strTimeout = m_settings_id_et_timeout.getText().toString();
                    String strCommFrameDelay = m_settings_id_et_comm_frame_delay.getText().toString();
                    // set a Parameter
                    if(SQLContract.Settings.setParameter(getActivity().getApplicationContext(), SQLContract.Parameter.IP_ADDRESS, String.valueOf(strIpAddress)) == false)
                    {
                        bSaveStatus = false;
                    }
                    if(SQLContract.Settings.setParameter(getActivity().getApplicationContext(), SQLContract.Parameter.PORT, String.valueOf(strPort)) == false)
                    {
                        bSaveStatus = false;
                    }
                    if(SQLContract.Settings.setParameter(getActivity().getApplicationContext(), SQLContract.Parameter.TIMEOUT, String.valueOf(strTimeout)) == false)
                    {
                        bSaveStatus = false;
                    }
                    if(SQLContract.Settings.setParameter(getActivity().getApplicationContext(), SQLContract.Parameter.COMM_FRAME_DELAY, String.valueOf(strCommFrameDelay)) == false)
                    {
                        bSaveStatus = false;
                    }

                    if(bSaveStatus == true)
                    {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.db_save_data_ok, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.db_save_data_error, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        @Override
        public void onResume()
        {
            super.onResume();

            // Load the data from Database
            if(m_settings_id_et_server_ip_address != null)
            {
                m_settings_id_et_server_ip_address.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.IP_ADDRESS, "192.168.1.1"));
            }
            if(m_settings_id_et_server_port != null)
            {
                m_settings_id_et_server_port.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.PORT, "502"));
            }
            if(m_settings_id_et_timeout != null)
            {
                m_settings_id_et_timeout.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.TIMEOUT, "3000"));
            }
            if(m_settings_id_et_comm_frame_delay != null)
            {
                m_settings_id_et_comm_frame_delay.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.COMM_FRAME_DELAY, "100"));
            }

        }

        @Override
        public void onPause()
        {
            super.onPause();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SettingSensorFragment extends Fragment
    {

        private EditText m_settings_id_et_sensor_feedback_ampl_k;
        private EditText m_settings_id_et_sensor_low_pass_filter_k;
        private EditText m_settings_id_et_sensor_max_output_value;
        private EditText m_settings_id_et_sensor_min_value_start_output;

        private Button m_settings_id_btn_save_sensor;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static SettingSensorFragment newInstance(int sectionNumber) {
            SettingSensorFragment fragment = new SettingSensorFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public SettingSensorFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.setting_sensor_fragment, container, false);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            super.onActivityCreated(savedInstanceState);

            m_settings_id_et_sensor_feedback_ampl_k = (EditText) getActivity().findViewById(R.id.settings_id_et_sensor_feedback_ampl_k);
            m_settings_id_et_sensor_feedback_ampl_k.setFilters(new InputFilter[]{ new InputFilterMinMax(m_settings_id_et_sensor_feedback_ampl_k.getContext(),1.0f, 1000.0f)});
            m_settings_id_et_sensor_low_pass_filter_k = (EditText) getActivity().findViewById(R.id.settings_id_et_sensor_low_pass_filter_k);
            m_settings_id_et_sensor_low_pass_filter_k.setFilters(new InputFilter[]{ new InputFilterMinMax(m_settings_id_et_sensor_low_pass_filter_k.getContext(),0.10f, 0.9f)});
            m_settings_id_et_sensor_max_output_value = (EditText) getActivity().findViewById(R.id.settings_id_et_sensor_max_output_value);
            m_settings_id_et_sensor_max_output_value.setFilters(new InputFilter[]{ new InputFilterMinMax(m_settings_id_et_sensor_max_output_value.getContext(),0, 255)});
            m_settings_id_et_sensor_min_value_start_output = (EditText) getActivity().findViewById(R.id.settings_id_et_sensor_min_value_start_output);
            m_settings_id_et_sensor_min_value_start_output.setFilters(new InputFilter[]{ new InputFilterMinMax(m_settings_id_et_sensor_min_value_start_output.getContext(),0, 255)});

            m_settings_id_btn_save_sensor = (Button) getActivity().findViewById(R.id.settings_id_btn_save_sensor);


            // Set an OnClickListener
            m_settings_id_btn_save_sensor.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    boolean bSaveStatus = true;
                    String strSensorFeedbackAmplK = m_settings_id_et_sensor_feedback_ampl_k.getText().toString();
                    String strSensorLowPassFilterK = m_settings_id_et_sensor_low_pass_filter_k.getText().toString();
                    String strSensorMaxOutputValue = m_settings_id_et_sensor_max_output_value.getText().toString();
                    String strSensorMinValueStartOutput = m_settings_id_et_sensor_min_value_start_output.getText().toString();


                    // set a Parameter
                    if(SQLContract.Settings.setParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_FEEDBACK_AMPL_K, String.valueOf(strSensorFeedbackAmplK)) == false)
                    {
                        bSaveStatus = false;
                    }
                    if(SQLContract.Settings.setParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_LOW_PASS_FILTER_K, String.valueOf(strSensorLowPassFilterK)) == false)
                    {
                        bSaveStatus = false;
                    }
                    if(SQLContract.Settings.setParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_MAX_OUTPUT_VALUE, String.valueOf(strSensorMaxOutputValue)) == false)
                    {
                        bSaveStatus = false;
                    }
                    if(SQLContract.Settings.setParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_MIN_VALUE_START_OUTPUT, String.valueOf(strSensorMinValueStartOutput)) == false)
                    {
                        bSaveStatus = false;
                    }

                    if(bSaveStatus == true)
                    {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.db_save_data_ok, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.db_save_data_error, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        @Override
        public void onResume()
        {
            super.onResume();

            // Load the data from Database
            if(m_settings_id_et_sensor_feedback_ampl_k != null)
            {
                m_settings_id_et_sensor_feedback_ampl_k.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_FEEDBACK_AMPL_K, "300.0f"));
            }
            if(m_settings_id_et_sensor_low_pass_filter_k != null)
            {
                m_settings_id_et_sensor_low_pass_filter_k.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_LOW_PASS_FILTER_K, "0.3f"));
            }
            if(m_settings_id_et_sensor_max_output_value != null)
            {
                m_settings_id_et_sensor_max_output_value.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_MAX_OUTPUT_VALUE, "250.0f"));
            }
            if(m_settings_id_et_sensor_min_value_start_output != null)
            {
                m_settings_id_et_sensor_min_value_start_output.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_MIN_VALUE_START_OUTPUT, "10.0f"));
            }
        }

        @Override
        public void onPause()
        {
            super.onPause();
        }
    }

    public static Intent makeSettingsActivity(Context context)
    {
        Intent intent = new Intent();
        intent.setClass(context, SettingsActivity.class);
        return intent;
    }}

