package com.pretolesi.arduino;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Locale;

import SQL.SQLContract;

/**
 * Settings Activity
 */
public class SettingsActivity extends ActionBarActivity implements ActionBar.TabListener {

    private static final String TAG = "SettingsActivity";

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

        final ActionBar actionBar = getSupportActionBar();


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
        }

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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

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
            }
            return null;
        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SettingServerFragment extends Fragment {

        private EditText m_settings_id_et_server_ip_address;
        private PretolesiEditText m_settings_id_et_server_port;
        private PretolesiEditText m_settings_id_et_timeout;
        private PretolesiEditText m_settings_id_et_comm_frame_delay;
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
            m_settings_id_et_server_port = (PretolesiEditText) getActivity().findViewById(R.id.settings_id_et_server_port);
            m_settings_id_et_server_port.setInputLimit(1, 65535);
            m_settings_id_et_timeout = (PretolesiEditText) getActivity().findViewById(R.id.settings_id_et_timeout);
            m_settings_id_et_timeout.setInputLimit(1, 100);
            m_settings_id_et_comm_frame_delay = (PretolesiEditText) getActivity().findViewById(R.id.settings_id_et_comm_frame_delay);
            m_settings_id_et_comm_frame_delay.setInputLimit(50, 5000);

            m_settings_id_btn_save_server = (Button) getActivity().findViewById(R.id.settings_id_btn_save_server);


            // Set an OnClickListener
            m_settings_id_btn_save_server.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    boolean bSaveStatus = true;
                    String strIpAddress = m_settings_id_et_server_ip_address.getText().toString();
                    String strPort = m_settings_id_et_server_port.getText().toString();
                    String strTimeout = m_settings_id_et_timeout.getText().toString();
                    String strCommFrameDelay = m_settings_id_et_comm_frame_delay.getText().toString();

                    // set a Parameter
                    if(!validateInputData(getView()))
                        return;

                    if(!SQLContract.Settings.setParameter(getActivity().getApplicationContext(), SQLContract.Parameter.IP_ADDRESS, String.valueOf(strIpAddress))) {
                        bSaveStatus = false;
                    }
                    if(!SQLContract.Settings.setParameter(getActivity().getApplicationContext(), SQLContract.Parameter.PORT, String.valueOf(strPort))) {
                        bSaveStatus = false;
                    }
                    if(!SQLContract.Settings.setParameter(getActivity().getApplicationContext(), SQLContract.Parameter.TIMEOUT, String.valueOf(strTimeout))) {
                        bSaveStatus = false;
                    }
                    if(!SQLContract.Settings.setParameter(getActivity().getApplicationContext(), SQLContract.Parameter.COMM_FRAME_DELAY, String.valueOf(strCommFrameDelay))) {
                        bSaveStatus = false;
                    }

                    if(bSaveStatus) {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.db_save_data_ok, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.db_save_data_error, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();

            // Load the data from Database
            if(m_settings_id_et_server_ip_address != null) {
                m_settings_id_et_server_ip_address.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.IP_ADDRESS));
            }
            if(m_settings_id_et_server_port != null) {
                m_settings_id_et_server_port.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.PORT));
            }
            if(m_settings_id_et_timeout != null) {
                m_settings_id_et_timeout.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.TIMEOUT));
            }
            if(m_settings_id_et_comm_frame_delay != null) {
                m_settings_id_et_comm_frame_delay.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.COMM_FRAME_DELAY));
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
    public static class SettingSensorFragment extends Fragment {

        private PretolesiEditText m_settings_id_et_sensor_feedback_ampl_k;
        private PretolesiEditText m_settings_id_et_sensor_low_pass_filter_k;
        private PretolesiEditText m_settings_id_et_sensor_max_output_value;
        private PretolesiEditText m_settings_id_et_sensor_min_value_start_output;

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
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            m_settings_id_et_sensor_feedback_ampl_k = (PretolesiEditText) getActivity().findViewById(R.id.settings_id_et_sensor_feedback_ampl_k);
            m_settings_id_et_sensor_feedback_ampl_k.setInputLimit(1.0f, 1000.0f);
            m_settings_id_et_sensor_low_pass_filter_k = (PretolesiEditText) getActivity().findViewById(R.id.settings_id_et_sensor_low_pass_filter_k);
            m_settings_id_et_sensor_low_pass_filter_k.setInputLimit(0.10f, 0.9f);
            m_settings_id_et_sensor_max_output_value = (PretolesiEditText) getActivity().findViewById(R.id.settings_id_et_sensor_max_output_value);
            m_settings_id_et_sensor_max_output_value.setInputLimit(5, 255);
            m_settings_id_et_sensor_min_value_start_output = (PretolesiEditText) getActivity().findViewById(R.id.settings_id_et_sensor_min_value_start_output);
            m_settings_id_et_sensor_min_value_start_output.setInputLimit(5, 255);

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

                    if(!validateInputData(getView()))
                        return;

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
                m_settings_id_et_sensor_feedback_ampl_k.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_FEEDBACK_AMPL_K));
            }
            if(m_settings_id_et_sensor_low_pass_filter_k != null)
            {
                m_settings_id_et_sensor_low_pass_filter_k.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_LOW_PASS_FILTER_K));
            }
            if(m_settings_id_et_sensor_max_output_value != null)
            {
                m_settings_id_et_sensor_max_output_value.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_MAX_OUTPUT_VALUE));
            }
            if(m_settings_id_et_sensor_min_value_start_output != null)
            {
                m_settings_id_et_sensor_min_value_start_output.setText(SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_MIN_VALUE_START_OUTPUT));
            }
        }

        @Override
        public void onPause()
        {
            super.onPause();
        }

    }

    private static boolean validateInputData(View v){
        View v_1 = v;
        if (v_1 != null) {
            if (v_1 instanceof ViewGroup) {
                for (int i_1 = 0, count_1 = ((ViewGroup) v_1).getChildCount(); i_1 < count_1; ++i_1) {
                    View v_2 = ((ViewGroup) v_1).getChildAt(i_1);
                    if (v_2 != null) {
                        if (v_2 instanceof ViewGroup) {
                            for (int i_2 = 0, count_2 = ((ViewGroup) v_2).getChildCount(); i_2 < count_2; ++i_2) {
                                View v_3 = ((ViewGroup) v_2).getChildAt(i_2);
                                if (v_3 != null) {
                                    if (v_3 instanceof ViewGroup) {
                                    } else if (v_3 instanceof PretolesiEditText) {
                                        if(!((PretolesiEditText)v_3).validateInputLimit())
                                            return false;
                                    }
                                }
                            }
                        } else if (v_2 instanceof PretolesiEditText) {
                            if(!((PretolesiEditText)v_2).validateInputLimit())
                                return false;
                        }
                    }
                }
            }else if (v_1 instanceof PretolesiEditText) {
                if(!((PretolesiEditText)v_1).validateInputLimit())
                    return false;
            }
        }

        return true;
    }

    public static Intent makeSettingsActivity(Context context)
    {
        Intent intent = new Intent();
        intent.setClass(context, SettingsActivity.class);
        return intent;
    }
}

