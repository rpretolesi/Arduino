package com.pretolesi.arduino;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.lang.Math;
import java.util.Vector;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import SQL.SQLContract;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener
{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private static byte SOH = 0x01;
    private static byte EOT = 0x04;
    private static byte ENQ = 0x05;
    private static byte ACK = 0x06;



    // Comando da inviare
    /*
        m_byteCommandHolder[0] = SOH
        m_byteCommandHolder[1] = Drive Command. Bit Nr: 0 = FWD, 1 = REW
        m_byteCommandHolder[2] = Fork Command. Bit Nr: 0 = UP, 1 = DOWN, 2 = OPEN, 3 = CLOSE,
        m_byteCommandHolder[3] = Disp.
        m_byteCommandHolder[4] = Disp.
        m_byteCommandHolder[5] = Throttle FWD 0-100
        m_byteCommandHolder[6] = Throttle REW 0-100
        m_byteCommandHolder[7] = Steering RIGHT 0-100
        m_byteCommandHolder[8] = Steering LEFT 0-100
        m_byteCommandHolder[9] = Fork Speed UP 0-100
        m_byteCommandHolder[10] = Fork Speed DOWN 0-100
        m_byteCommandHolder[11] = Fork Speed OPEN 0-100
        m_byteCommandHolder[12] = Fork Speed CLOSE 0-100
        m_byteCommandHolder[13] = Disp.
        m_byteCommandHolder[14] = Disp
        m_byteCommandHolder[15] = EOT

     */

    // Command to send
    private static ArduinoClientSocket m_acs;
    private static Message m_Message;

    // Task di comunicazione
    private static CommunicationTask m_CommunicationTask = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        // Set up the action bar.

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

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

        // Inizializzo i comandi da inviare
        // Inizializzo il client di comunicazione
        if(m_acs == null)
        {
            m_acs = new ArduinoClientSocket(getApplicationContext());
        }
        if(m_Message == null)
        {
            m_Message = new Message();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        String str = "";
        // Avvio il Task di comunicazione
        m_CommunicationTask = new CommunicationTask();
        m_CommunicationTask.execute(m_acs, m_Message);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if(m_CommunicationTask != null)
        {
            m_CommunicationTask.cancel(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent = null;

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            intent = SettingsActivity.makeSettingsActivity(getApplicationContext());
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());

        String strTAG = mSectionsPagerAdapter.getFragmentTag(mViewPager.getId(), tab.getPosition());

        BaseFragment bf = (BaseFragment)getSupportFragmentManager().findFragmentByTag(strTAG);
        if(bf != null)
        {
            bf.onTabSelected(tab, fragmentTransaction);
        }
/*
        if(tab.getPosition() == 1)
        {

            PatientPainTherapyFragment pptf = (PatientPainTherapyFragment)getSupportFragmentManager().findFragmentByTag(strTAG);
            if(pptf != null)
            {
                if(pptf.isResumed() == true)
                {
                    pptf.UpdateFragment();
                }
            }
        }
*/
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        String strTAG = mSectionsPagerAdapter.getFragmentTag(mViewPager.getId(), tab.getPosition());

        BaseFragment bf = (BaseFragment)getSupportFragmentManager().findFragmentByTag(strTAG);
        if(bf != null)
        {
            bf.onTabUnselected(tab, fragmentTransaction);
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        String strTAG = mSectionsPagerAdapter.getFragmentTag(mViewPager.getId(), tab.getPosition());

        BaseFragment bf = (BaseFragment)getSupportFragmentManager().findFragmentByTag(strTAG);
        if(bf != null)
        {
            bf.onTabReselected(tab, fragmentTransaction);
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
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
                fragment = DriveFragment.newInstance(position + 1);
            }
            if (position == 1)
            {
                fragment = C_1_Fragment.newInstance(position + 1);
            }
            if (position == 2)
            {
                fragment = C_2_Fragment.newInstance(position + 1);
            }
            if (position == 3)
            {
                fragment = SAC_Fragment.newInstance(position + 1);
            }

            return fragment;
        }

        @Override
        public int getCount() {
            // Show xx total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.drive_title_section_sensor_drive).toUpperCase(l);
                case 1:
                    return getString(R.string.drive_title_section_digital_drive).toUpperCase(l);
                case 2:
                    return getString(R.string.drive_title_section_analog_drive).toUpperCase(l);
                case 3:
                    return getString(R.string.drive_title_section_sample_code).toUpperCase(l);
            }
            return null;
        }

        private String getFragmentTag(int viewPagerId, int fragmentPosition)
        {
            return "android:switcher:" + viewPagerId + ":" + fragmentPosition;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends BaseFragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.main_fragment, container, false);
            return rootView;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DriveFragment extends BaseFragment implements SensorEventListener
    {
        // Sensori
        private SensorManager m_SensorManager;
        private Sensor m_Accelerometer;
        private Sensor m_Magnetometer;
        float[] m_faGravity;
        float[] m_faGeomagnetic;
        float m_AzimPitchRoll[] = null;
        float m_AzimOld;
        float m_PitchOld;
        float m_AzimPitchRollRaw[] = null;
        private long m_LastUpdate;

        private Button m_drive_id_btn_drive_wheel_start_stop;
        private boolean m_bDriveWheelStartStopStatus;
        private Button m_drive_id_btn_drive_fork_start_stop;
        private boolean m_bDriveForkStartStopStatus;

        private TextView m_drive_text_tv_value_up;
        private TextView m_id_tv_byte_1a2_1_out;
        private TextView m_id_tv_byte_1a2_1_in;
        private TextView m_id_tv_byte_1a2_2_out;
        private TextView m_id_tv_byte_1a2_2_in;
        private TextView m_id_tv_byte_1a2_3_out;
        private TextView m_id_tv_byte_1a2_3_in;
        private TextView m_id_tv_byte_1a2_4_out;
        private TextView m_id_tv_byte_1a2_4_in;
        private TextView m_id_tv_byte_5a9_out;
        private TextView m_id_tv_byte_5a9_in;
        private TextView m_id_tv_byte_6a10_out;
        private TextView m_id_tv_byte_6a10_in;
        private TextView m_id_tv_byte_7a11_out;
        private TextView m_id_tv_byte_7a11_in;
        private TextView m_id_tv_byte_8a12_out;
        private TextView m_id_tv_byte_8a12_in;

        private TextView m_drive_text_tv_value_down;
        private TextView m_drive_text_tv_value_left;
        private TextView m_drive_text_tv_value_right;
        private TextView m_drive_id_tv_communication_status;

        // Dati
        private float m_settings_id_et_comm_frame_delay;
        private float m_fSensorFeedbackAmplK;
        private float m_fSensorLowPassFilterK;
        private float m_fSensorMaxOutputValue;
        private float m_fSensorMinValueStartOutput;

        private float m_fAzimTare;
        private float m_fPitchTare;


        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static DriveFragment newInstance(int sectionNumber) {
            DriveFragment fragment = new DriveFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public DriveFragment()
        {
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            Resumed();
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            Paused();
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }

        @Override
        public void onActivityCreated (Bundle savedInstanceState)
        {
            super.onActivityCreated(savedInstanceState);

            m_drive_id_btn_drive_wheel_start_stop = (Button) getActivity().findViewById(R.id.drive_id_btn_drive_wheel_start_stop);
            m_drive_id_btn_drive_fork_start_stop = (Button) getActivity().findViewById(R.id.drive_id_btn_drive_wheel_fork_start_stop);
            m_drive_text_tv_value_up = (TextView) getActivity().findViewById(R.id.drive_id_tv_value_up);
            m_drive_text_tv_value_down = (TextView) getActivity().findViewById(R.id.drive_id_tv_value_down);
            m_drive_text_tv_value_left = (TextView) getActivity().findViewById(R.id.drive_id_tv_value_left);
            m_drive_text_tv_value_right = (TextView) getActivity().findViewById(R.id.drive_id_tv_value_right);
            m_id_tv_byte_1a2_1_out = (TextView) getActivity().findViewById(R.id.id_tv_byte_1a2_1_out);
            m_id_tv_byte_1a2_1_in = (TextView) getActivity().findViewById(R.id.id_tv_byte_1a2_1_in);
            m_id_tv_byte_1a2_2_out = (TextView) getActivity().findViewById(R.id.id_tv_byte_1a2_2_out);
            m_id_tv_byte_1a2_2_in = (TextView) getActivity().findViewById(R.id.id_tv_byte_1a2_2_in);
            m_id_tv_byte_1a2_3_out = (TextView) getActivity().findViewById(R.id.id_tv_byte_1a2_3_out);
            m_id_tv_byte_1a2_3_in = (TextView) getActivity().findViewById(R.id.id_tv_byte_1a2_3_in);
            m_id_tv_byte_1a2_4_out = (TextView) getActivity().findViewById(R.id.id_tv_byte_1a2_4_out);
            m_id_tv_byte_1a2_4_in = (TextView) getActivity().findViewById(R.id.id_tv_byte_1a2_4_in);
            m_id_tv_byte_5a9_out = (TextView) getActivity().findViewById(R.id.id_tv_byte_5a9_out);
            m_id_tv_byte_5a9_in = (TextView) getActivity().findViewById(R.id.id_tv_byte_5a9_in);
            m_id_tv_byte_6a10_out = (TextView) getActivity().findViewById(R.id.id_tv_byte_6a10_out);
            m_id_tv_byte_6a10_in = (TextView) getActivity().findViewById(R.id.id_tv_byte_6a10_in);
            m_id_tv_byte_7a11_out = (TextView) getActivity().findViewById(R.id.id_tv_byte_7a11_out);
            m_id_tv_byte_7a11_in = (TextView) getActivity().findViewById(R.id.id_tv_byte_7a11_in);
            m_id_tv_byte_8a12_out = (TextView) getActivity().findViewById(R.id.id_tv_byte_8a12_out);
            m_id_tv_byte_8a12_in = (TextView) getActivity().findViewById(R.id.id_tv_byte_8a12_in);
            m_drive_id_tv_communication_status = (TextView) getActivity().findViewById(R.id.drive_id_tv_communication_status);

            // Set an OnClickListener
            m_drive_id_btn_drive_wheel_start_stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (m_Message != null) {
                        if (!m_bDriveWheelStartStopStatus) {

                            // Inizializo
                            m_drive_text_tv_value_up.setText(getString(R.string.drive_text_tv_throttle_fwd));
                            m_drive_text_tv_value_down.setText(getString(R.string.drive_text_tv_throttle_rev));
                            m_drive_text_tv_value_left.setText(getString(R.string.drive_text_tv_steering_left));
                            m_drive_text_tv_value_right.setText(getString(R.string.drive_text_tv_steering_right));

                            m_bDriveWheelStartStopStatus = true;
                            m_drive_id_btn_drive_wheel_start_stop.setText(R.string.drive_text_btn_drive_wheel_stop);

                            // Fork
                            m_bDriveForkStartStopStatus = false;
                            m_drive_id_btn_drive_fork_start_stop.setText(R.string.drive_text_btn_drive_fork_start);

                            // Eseguo la tara dei valori dei sensori
                            if (m_AzimPitchRoll != null) {
//                                m_fAzimTare = m_AzimPitchRoll[1];
//                                m_fPitchTare = m_AzimPitchRoll[2];
                            }
                        } else {
                            m_bDriveWheelStartStopStatus = false;
                            m_drive_id_btn_drive_wheel_start_stop.setText(R.string.drive_text_btn_drive_wheel_start);

                            initializeData();
                        }
                    }
                }
            });

            m_drive_id_btn_drive_fork_start_stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (m_Message != null) {
                        if (!m_bDriveForkStartStopStatus) {

                            // Inizializo
                            m_drive_text_tv_value_up.setText(getString(R.string.drive_text_tv_fork_up));
                            m_drive_text_tv_value_down.setText(getString(R.string.drive_text_tv_fork_down));
                            m_drive_text_tv_value_left.setText(getString(R.string.drive_text_tv_fork_open));
                            m_drive_text_tv_value_right.setText(getString(R.string.drive_text_tv_fork_close));

                            m_bDriveForkStartStopStatus = true;
                            m_drive_id_btn_drive_fork_start_stop.setText(R.string.drive_text_btn_drive_fork_stop);

                            // Wheel
                            m_bDriveWheelStartStopStatus = false;
                            m_drive_id_btn_drive_wheel_start_stop.setText(R.string.drive_text_btn_drive_wheel_start);

                            // Eseguo la tara dei valori dei sensori
                            if (m_AzimPitchRoll != null) {
//                                m_fAzimTare = m_AzimPitchRoll[1];
//                                m_fPitchTare = m_AzimPitchRoll[2];
                            }
                        } else {
                            m_bDriveForkStartStopStatus = false;
                            m_drive_id_btn_drive_fork_start_stop.setText(R.string.drive_text_btn_drive_fork_start);

                            initializeData();
                        }
                    }
                }
            });

            // Get reference to SensorManager
            m_SensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);

            if(m_SensorManager != null)
            {
                // Get reference to Sensor
                m_Accelerometer = m_SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                m_Magnetometer = m_SensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

                if(m_Accelerometer != null && m_Magnetometer != null)
                {
                    // Sensori disponibili
                }
            }

            // Inizializzo variabili per set sensori
            m_fSensorFeedbackAmplK = 1.0f;
            m_fSensorLowPassFilterK = 0.1f;
            m_fSensorMaxOutputValue = 1.0f;
            m_fSensorMinValueStartOutput = 10.0f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.drive_fragment, container, false);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            Resumed();
        }

        private void Resumed(){

            initializeData();

            // Registro Listeners
            if(m_CommunicationTask != null) {
                m_CommunicationTask.registerListener(this);
            }

            // Prelevo i dati dei sensori
            String strCommFrameDelay = SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.COMM_FRAME_DELAY);
            String strSettSensorFeedbackAmplK = SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_FEEDBACK_AMPL_K);
            String strSettSensorLowPassFilterK = SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_LOW_PASS_FILTER_K);
            String strSettSensorMaxOutputValue = SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_MAX_OUTPUT_VALUE);
            String strSensorMinValueStartOutput = SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_MIN_VALUE_START_OUTPUT);

            try{
                m_settings_id_et_comm_frame_delay = Integer.parseInt(strCommFrameDelay);
            } catch (Exception ignored) {
            }
            try{
                m_fSensorFeedbackAmplK = Float.valueOf(strSettSensorFeedbackAmplK);
            } catch (Exception ignored) {
            }
            try{
                m_fSensorLowPassFilterK = Float.valueOf(strSettSensorLowPassFilterK);
            } catch (Exception ignored) {
            }
            try{
                m_fSensorMaxOutputValue = Float.valueOf(strSettSensorMaxOutputValue);
            } catch (Exception ignored) {
            }
            try{
                m_fSensorMinValueStartOutput = Float.valueOf(strSensorMinValueStartOutput);
            } catch (Exception ignored) {
            }

            m_SensorManager.registerListener(this, m_Accelerometer, SensorManager.SENSOR_DELAY_UI);
            m_SensorManager.registerListener(this, m_Magnetometer, SensorManager.SENSOR_DELAY_UI);
        }

        @Override
        public void onPause() {
            super.onPause();
            Paused();
         }

        private void Paused(){
            if(m_CommunicationTask != null) {
                m_CommunicationTask.unregisterListener(this);
            }

            m_SensorManager.unregisterListener(this);
        }

        @Override
        public void onNewCommunicationStatus(String[] strStatus) {
            // Aggiorno lo stato
            if(m_drive_id_tv_communication_status != null) {
                m_drive_id_tv_communication_status.setText(strStatus[0] + " - " + strStatus[1]);
            }

            if(m_Message != null) {
                if(m_bDriveWheelStartStopStatus) {
                    if(m_id_tv_byte_1a2_1_in != null)
                        m_id_tv_byte_1a2_1_in.setText(formatDataToArrayBinaryStringIn(m_Message.getDataByte(1),1));
                    if(m_id_tv_byte_1a2_2_in != null)
                        m_id_tv_byte_1a2_2_in.setText(formatDataToArrayBinaryStringIn(m_Message.getDataByte(1),1));
                    if(m_id_tv_byte_1a2_3_in != null)
                        m_id_tv_byte_1a2_3_in.setText(formatDataToArrayBinaryStringIn(m_Message.getDataByte(1),1));
                    if(m_id_tv_byte_1a2_4_in != null)
                        m_id_tv_byte_1a2_4_in.setText(formatDataToArrayBinaryStringIn(m_Message.getDataByte(1),1));
                    if(m_id_tv_byte_5a9_in != null)
                        m_id_tv_byte_5a9_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(5),5));
                    if(m_id_tv_byte_6a10_in != null)
                        m_id_tv_byte_6a10_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(6),6));
                    if(m_id_tv_byte_7a11_in != null)
                        m_id_tv_byte_7a11_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(7),7));
                    if(m_id_tv_byte_8a12_in != null)
                        m_id_tv_byte_8a12_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(8),8));

                    // Aggiorno lo stato dei colori
                    if(m_Message.getDriveWheelFWD()) {
                        m_drive_text_tv_value_up.setTextColor(Color.GREEN);
                    } else {
                        m_drive_text_tv_value_up.setTextColor(Color.BLACK);
                    }
                    if(m_Message.getDriveWheelREV()) {
                        m_drive_text_tv_value_down.setTextColor(Color.GREEN);
                    } else {
                        m_drive_text_tv_value_down.setTextColor(Color.BLACK);
                    }
                    if(m_Message.getDriveWheelLEFT()) {
                        m_drive_text_tv_value_left.setTextColor(Color.GREEN);
                    } else {
                        m_drive_text_tv_value_left.setTextColor(Color.BLACK);
                    }
                    if(m_Message.getDriveWheelRIGHT()) {
                        m_drive_text_tv_value_right.setTextColor(Color.GREEN);
                    } else {
                        m_drive_text_tv_value_right.setTextColor(Color.BLACK);
                    }
                }
                if(m_bDriveForkStartStopStatus) {
                    if(m_id_tv_byte_1a2_1_in != null)
                        m_id_tv_byte_1a2_1_in.setText(formatDataToArrayBinaryStringIn(m_Message.getDataByte(2),2));
                    if(m_id_tv_byte_1a2_2_in != null)
                        m_id_tv_byte_1a2_2_in.setText(formatDataToArrayBinaryStringIn(m_Message.getDataByte(2),2));
                    if(m_id_tv_byte_1a2_3_in != null)
                        m_id_tv_byte_1a2_3_in.setText(formatDataToArrayBinaryStringIn(m_Message.getDataByte(2),2));
                    if(m_id_tv_byte_1a2_4_in != null)
                        m_id_tv_byte_1a2_4_in.setText(formatDataToArrayBinaryStringIn(m_Message.getDataByte(2),2));
                    if(m_id_tv_byte_5a9_in != null)
                        m_id_tv_byte_5a9_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(9),9));
                    if(m_id_tv_byte_6a10_in != null)
                        m_id_tv_byte_6a10_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(10),10));
                    if(m_id_tv_byte_7a11_in != null)
                        m_id_tv_byte_7a11_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(11),11));
                    if(m_id_tv_byte_8a12_in != null)
                        m_id_tv_byte_8a12_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(12),12));

                    // Aggiorno lo stato dei colori
                    if(m_Message.getDriveForkUp()) {
                        m_drive_text_tv_value_up.setTextColor(Color.GREEN);
                    } else {
                        m_drive_text_tv_value_up.setTextColor(Color.BLACK);
                    }

                    if(m_Message.getDriveForkDown()) {
                        m_drive_text_tv_value_down.setTextColor(Color.GREEN);
                    } else {
                        m_drive_text_tv_value_down.setTextColor(Color.BLACK);
                    }

                    if(m_Message.getDriveForkOpen()) {
                        m_drive_text_tv_value_left.setTextColor(Color.GREEN);
                    } else {
                        m_drive_text_tv_value_left.setTextColor(Color.BLACK);
                    }

                    if(m_Message.getDriveForkClose()) {
                        m_drive_text_tv_value_right.setTextColor(Color.GREEN);
                    } else {
                        m_drive_text_tv_value_right.setTextColor(Color.BLACK);
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                m_faGravity = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                m_faGeomagnetic = event.values;
            }
            if (m_faGravity != null && m_faGeomagnetic != null) {
                float faRot[] = new float[9];
                float faIncl[] = new float[9];
                boolean bRes = SensorManager.getRotationMatrix(faRot, faIncl, m_faGravity, m_faGeomagnetic);
                if (bRes) {
                    if(m_AzimPitchRoll == null) {
                        m_AzimPitchRoll = new float[3];
                    }
                    if(m_AzimPitchRollRaw == null) {
                        m_AzimPitchRollRaw = new float[3];
                    }

                    SensorManager.getOrientation(faRot, m_AzimPitchRollRaw);
                    long actualTime = System.currentTimeMillis();
                    if (actualTime - m_LastUpdate > m_settings_id_et_comm_frame_delay) {
                        m_LastUpdate = actualTime;

                        float rawX = m_AzimPitchRollRaw[0];
                        float rawY = m_AzimPitchRollRaw[1];
                        float rawZ = m_AzimPitchRollRaw[2];

                        // Apply low-pass filter
                        m_AzimPitchRoll[0] = lowPass(rawX, m_AzimPitchRoll[0], m_fSensorLowPassFilterK);
                        m_AzimPitchRoll[1] = lowPass(rawY, m_AzimPitchRoll[1], m_fSensorLowPassFilterK);
                        m_AzimPitchRoll[2] = lowPass(rawZ, m_AzimPitchRoll[2], m_fSensorLowPassFilterK);

                        float fAzim = (m_AzimPitchRoll[1] - m_fAzimTare) * m_fSensorFeedbackAmplK;
                        if(Math.abs(fAzim) > Math.abs(m_fSensorMaxOutputValue))
                        {
                            if(fAzim < 0.0f)
                            {
                                fAzim = -Math.abs(m_fSensorMaxOutputValue);
                            }
                            if(fAzim > 0.0f)
                            {
                                fAzim = Math.abs(m_fSensorMaxOutputValue);
                            }
                        }

                        float fPitch = (m_AzimPitchRoll[2] - m_fPitchTare) *  m_fSensorFeedbackAmplK;
                        if(Math.abs(fPitch) > Math.abs(m_fSensorMaxOutputValue))
                        {
                            if(fPitch < 0.0f)
                            {
                                fPitch = -Math.abs(m_fSensorMaxOutputValue);
                            }
                            if(fPitch > 0.0f)
                            {
                                fPitch = Math.abs(m_fSensorMaxOutputValue);
                            }
                        }

                        // Set Command for Drive
                        if((Math.abs(fAzim - m_AzimOld) >= 10.0f) || (Math.abs(fPitch - m_PitchOld) >= 10.0f)) {
                            m_AzimOld = fAzim;
                            m_PitchOld = fPitch;
                            if((fAzim + 10) >= 250) {
                                fAzim = 250;
                            }
                            if((fAzim - 10) <= -250) {
                                fAzim = -250;
                            }
                            if((fPitch + 10) >= 250) {
                                fPitch = 250;
                            }
                            if((fPitch - 10) <= -250) {
                                fPitch = -250;
                            }

                            DriveWheel(fAzim, fPitch);
                            DriveFork(fAzim, fPitch);
                        }
                    }
                }
            }
        }

        private void DriveWheel(float fAzim, float fPitch) {
            // Aggiorno i miei dati
            // Converto l'acceleratore da 0 a 100 e parto con la posizione attuale
            float fThrottle = 0;
            float fThrottleFWD = 0;
            float fThrottleREV = 0;
            float fSteering = 0;
            float fSteeringLEFT = 0;
            float fSteeringRIGHT = 0;
            if(m_bDriveWheelStartStopStatus) {

                // Throttle
                fThrottle = Math.abs(fAzim);
                if(fAzim > 0) {
                    fThrottleFWD = fThrottle;
                    fThrottleREV = 0;
                    m_Message.setThrottleREV(floatTobyte(0.0f));
                    if(fThrottleFWD > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveWheelFWD(true);
                        m_Message.setThrottleFWD(floatTobyte(fThrottleFWD));
                    }
                    else
                    {
                        m_Message.setDriveWheelFWD(false);
                        m_Message.setThrottleFWD(floatTobyte(0.0f));
                    }
                }
                if(fAzim < 0) {
                    fThrottleFWD = 0;
                    m_Message.setThrottleFWD(floatTobyte(0.0f));
                    fThrottleREV = fThrottle;
                    if(fThrottleREV > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveWheelREV(true);
                        m_Message.setThrottleREV(floatTobyte(fThrottleREV));
                    }
                    else
                    {
                        m_Message.setDriveWheelREV(false);
                        m_Message.setThrottleREV(floatTobyte(0.0f));
                    }
                }

                // Steering
                fSteering = Math.abs(fPitch);
                // Send command only after a threshold
                if(fPitch < 0) {
                    fSteeringLEFT = fSteering;
                    fSteeringRIGHT = 0;
                    m_Message.setSteeringRIGHT(floatTobyte(0.0f));
                    if(fSteeringLEFT > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveWheelLEFT(true);
                        m_Message.setSteeringLEFT(floatTobyte(fSteeringLEFT));
                    }
                    else
                    {
                        m_Message.setDriveWheelLEFT(false);
                        m_Message.setSteeringLEFT(floatTobyte(0.0f));
                    }
                }
                if(fPitch > 0) {
                    fSteeringLEFT = 0;
                    m_Message.setSteeringLEFT(floatTobyte(0.0f));
                    fSteeringRIGHT = fSteering;
                    if(fSteeringRIGHT > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveWheelRIGHT(true);
                        m_Message.setSteeringRIGHT(floatTobyte(fSteeringRIGHT));
                    }
                    else
                    {
                        m_Message.setDriveWheelRIGHT(false);
                        m_Message.setSteeringRIGHT(floatTobyte(0.0f));
                    }
                }

                // Send Command
                if(m_Message.isCommandActionChanged()) {
                    m_Message.setCommandAsToSend();
                }

                setTextDataOutDriveWheel();

            }
        }

        private void DriveFork(float fAzim, float fPitch) {
            // Aggiorno i miei dati
            // Converto l'acceleratore da 0 a 100 e parto con la posizione attuale
            float fForkSpeedUpDown = 0;
            float fForkUP = 0;
            float fForkDOWN = 0;
            float fForkSpeedOpenClose = 0;
            float fForkOPEN = 0;
            float fForkCLOSE = 0;
            if(m_bDriveForkStartStopStatus) {

                // Fork Up and Down
                fForkSpeedUpDown = Math.abs(fAzim);
                if(fAzim > 0) {
                    fForkUP = fForkSpeedUpDown;
                    fForkDOWN = 0;
                    m_Message.setDriveSpeedForkDOWN(floatTobyte(0.0f));
                    if(fForkUP > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveForkUp(true);
                        m_Message.setDriveSpeedForkUP(floatTobyte(fForkUP));
                    }
                    else
                    {
                        m_Message.setDriveForkUp(false);
                        m_Message.setDriveSpeedForkUP(floatTobyte(0.0f));
                    }
                }
                if(fAzim < 0) {
                    fForkUP = 0;
                    m_Message.setDriveSpeedForkUP(floatTobyte(0.0f));
                    fForkDOWN = fForkSpeedUpDown;
                    if(fForkDOWN > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveForkDown(true);
                        m_Message.setDriveSpeedForkDOWN(floatTobyte(fForkDOWN));
                    }
                    else
                    {
                        m_Message.setDriveForkDown(false);
                        m_Message.setDriveSpeedForkDOWN(floatTobyte(0.0f));
                    }
                }

                // Fork Open and Close
                fForkSpeedOpenClose = Math.abs(fPitch);
                // Send command only after a threshold
                if(fPitch < 0) {
                    fForkOPEN = fForkSpeedOpenClose;
                    fForkCLOSE = 0;
                    m_Message.setDriveSpeedForkOPEN(floatTobyte(0.0f));
                    if(fForkOPEN > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveForkOpen(true);
                        m_Message.setDriveSpeedForkOPEN(floatTobyte(fForkOPEN));
                    }
                    else
                    {
                        m_Message.setDriveForkOpen(false);
                        m_Message.setDriveSpeedForkOPEN(floatTobyte(0.0f));
                    }
                }
                if(fPitch > 0) {
                    fForkOPEN = 0;
                    m_Message.setDriveSpeedForkOPEN(floatTobyte(0.0f));
                    fForkCLOSE = fForkSpeedOpenClose;
                    if(fForkCLOSE > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveForkClose(true);
                        m_Message.setDriveSpeedForkCLOSE(floatTobyte(fForkCLOSE));
                    }
                    else
                    {
                        m_Message.setDriveForkClose(false);
                        m_Message.setDriveSpeedForkCLOSE(floatTobyte(0.0f));
                    }
                }

                // Send Command
                if(m_Message.isCommandActionChanged()) {
                    m_Message.setCommandAsToSend();
                }

                setTextDataOutDriveFork();

            }

        }
        private void setTextDataOutDriveWheel() {
            m_id_tv_byte_1a2_1_out.setText(formatDataToArrayBinaryStringOut(m_Message.getActionByte(1), 1));
            m_id_tv_byte_5a9_out.setText(formatDataToArrayStringOut(m_Message.getActionByte(5), 5));
            m_id_tv_byte_1a2_2_out.setText(formatDataToArrayBinaryStringOut(m_Message.getActionByte(1), 1));
            m_id_tv_byte_6a10_out.setText(formatDataToArrayStringOut(m_Message.getActionByte(6), 6));
            m_id_tv_byte_1a2_3_out.setText(formatDataToArrayBinaryStringOut(m_Message.getActionByte(1), 1));
            m_id_tv_byte_7a11_out.setText(formatDataToArrayStringOut(m_Message.getActionByte(7), 7));
            m_id_tv_byte_1a2_4_out.setText(formatDataToArrayBinaryStringOut(m_Message.getActionByte(1), 1));
            m_id_tv_byte_8a12_out.setText(formatDataToArrayStringOut(m_Message.getActionByte(8), 8));
        }

        private void setTextDataOutDriveFork() {
            m_id_tv_byte_1a2_1_out.setText(formatDataToArrayBinaryStringOut(m_Message.getActionByte(2), 2));
            m_id_tv_byte_5a9_out.setText(formatDataToArrayStringOut(m_Message.getActionByte(9), 9));
            m_id_tv_byte_1a2_2_out.setText(formatDataToArrayBinaryStringOut(m_Message.getActionByte(2), 2));
            m_id_tv_byte_6a10_out.setText(formatDataToArrayStringOut(m_Message.getActionByte(10), 10));
            m_id_tv_byte_1a2_3_out.setText(formatDataToArrayBinaryStringOut(m_Message.getActionByte(2), 2));
            m_id_tv_byte_7a11_out.setText(formatDataToArrayStringOut(m_Message.getActionByte(11), 11));
            m_id_tv_byte_1a2_4_out.setText(formatDataToArrayBinaryStringOut(m_Message.getActionByte(2), 2));
            m_id_tv_byte_8a12_out.setText(formatDataToArrayStringOut(m_Message.getActionByte(12), 12));
        }

        private void initializeData(){

            // Wheel
            m_bDriveWheelStartStopStatus = false;
            m_drive_id_btn_drive_wheel_start_stop.setText(R.string.drive_text_btn_drive_wheel_start);
            // Fork
            m_bDriveForkStartStopStatus = false;
            m_drive_id_btn_drive_fork_start_stop.setText(R.string.drive_text_btn_drive_fork_start);

            m_drive_text_tv_value_up.setText(getString(R.string.default_string_value_char));
            m_drive_text_tv_value_up.setTextColor(Color.BLACK);
            m_drive_text_tv_value_down.setText(getString(R.string.default_string_value_char));
            m_drive_text_tv_value_down.setTextColor(Color.BLACK);
            m_drive_text_tv_value_left.setText(getString(R.string.default_string_value_char));
            m_drive_text_tv_value_left.setTextColor(Color.BLACK);
            m_drive_text_tv_value_right.setText(getString(R.string.default_string_value_char));
            m_drive_text_tv_value_right.setTextColor(Color.BLACK);
            m_id_tv_byte_1a2_1_out.setText(getString(R.string.default_string_value_bit));
            m_id_tv_byte_1a2_1_in.setText(getString(R.string.default_string_value_bit));
            m_id_tv_byte_5a9_out.setText(getString(R.string.default_string_value_array));
            m_id_tv_byte_5a9_in.setText(getString(R.string.default_string_value_array));
            m_id_tv_byte_1a2_2_out.setText(getString(R.string.default_string_value_bit));
            m_id_tv_byte_1a2_2_in.setText(getString(R.string.default_string_value_bit));
            m_id_tv_byte_6a10_out.setText(getString(R.string.default_string_value_array));
            m_id_tv_byte_6a10_in.setText(getString(R.string.default_string_value_array));
            m_id_tv_byte_1a2_3_out.setText(getString(R.string.default_string_value_bit));
            m_id_tv_byte_1a2_3_in.setText(getString(R.string.default_string_value_bit));
            m_id_tv_byte_7a11_out.setText(getString(R.string.default_string_value_array));
            m_id_tv_byte_7a11_in.setText(getString(R.string.default_string_value_array));
            m_id_tv_byte_1a2_4_out.setText(getString(R.string.default_string_value_bit));
            m_id_tv_byte_1a2_4_in.setText(getString(R.string.default_string_value_bit));
            m_id_tv_byte_8a12_out.setText(getString(R.string.default_string_value_array));
            m_id_tv_byte_8a12_in.setText(getString(R.string.default_string_value_array));
            m_drive_id_tv_communication_status.setText(getString(R.string.default_string_value_char));

            m_Message.resetCommand();
            // Send Command
            if(m_Message.isCommandActionChanged()) {
                m_Message.setCommandAsToSend();
            }
        }
    }

    /**
     * A fragment with buttons.
     */
    public static class C_1_Fragment extends BaseFragment  implements CompoundButton.OnCheckedChangeListener {

        private ToggleButton m_toggleButton_byte_1_1;
        private ToggleButton m_toggleButton_byte_1_2;
        private ToggleButton m_toggleButton_byte_1_3;
        private ToggleButton m_toggleButton_byte_1_4;
        private ToggleButton m_toggleButton_byte_1_5;
        private ToggleButton m_toggleButton_byte_1_6;
        private ToggleButton m_toggleButton_byte_1_7;
        private ToggleButton m_toggleButton_byte_1_8;
        private TextView m_id_tv_byte_1_out;
        private TextView m_id_tv_byte_1_in;

        private ToggleButton m_toggleButton_byte_2_1;
        private ToggleButton m_toggleButton_byte_2_2;
        private ToggleButton m_toggleButton_byte_2_3;
        private ToggleButton m_toggleButton_byte_2_4;
        private ToggleButton m_toggleButton_byte_2_5;
        private ToggleButton m_toggleButton_byte_2_6;
        private ToggleButton m_toggleButton_byte_2_7;
        private ToggleButton m_toggleButton_byte_2_8;
        private TextView m_id_tv_byte_2_out;
        private TextView m_id_tv_byte_2_in;

        private TextView m_c1_id_tv_communication_status;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static C_1_Fragment newInstance(int sectionNumber) {
            C_1_Fragment fragment = new C_1_Fragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public C_1_Fragment() {
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            Resumed();
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            Paused();
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }

        @Override
        public void onActivityCreated (Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            m_toggleButton_byte_1_1 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_1_1);
            m_toggleButton_byte_1_2 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_1_2);
            m_toggleButton_byte_1_3 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_1_3);
            m_toggleButton_byte_1_4 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_1_4);
            m_toggleButton_byte_1_5 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_1_5);
            m_toggleButton_byte_1_6 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_1_6);
            m_toggleButton_byte_1_7 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_1_7);
            m_toggleButton_byte_1_8 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_1_8);
            m_id_tv_byte_1_out = (TextView) getActivity().findViewById(R.id.id_tv_byte_1_out);
            m_id_tv_byte_1_in = (TextView) getActivity().findViewById(R.id.id_tv_byte_1_in);

            m_toggleButton_byte_2_1 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_1);
            m_toggleButton_byte_2_2 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_2);
            m_toggleButton_byte_2_3 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_3);
            m_toggleButton_byte_2_4 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_4);
            m_toggleButton_byte_2_5 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_5);
            m_toggleButton_byte_2_6 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_6);
            m_toggleButton_byte_2_7 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_7);
            m_toggleButton_byte_2_8 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_8);
            m_id_tv_byte_2_out = (TextView) getActivity().findViewById(R.id.id_tv_byte_2_out);
            m_id_tv_byte_2_in = (TextView) getActivity().findViewById(R.id.id_tv_byte_2_in);

            m_c1_id_tv_communication_status = (TextView) getActivity().findViewById(R.id.c1_id_tv_communication_status);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.c_1_drive_fragment, container, false);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            Resumed();
        }

        private void Resumed() {
            initializeData();

            // Registro Listeners
            if(m_CommunicationTask != null) {
                m_CommunicationTask.registerListener(this);
            }

            // Registro Listeners
            m_toggleButton_byte_1_1.setOnCheckedChangeListener(this);
            m_toggleButton_byte_1_2.setOnCheckedChangeListener(this);
            m_toggleButton_byte_1_3.setOnCheckedChangeListener(this);
            m_toggleButton_byte_1_4.setOnCheckedChangeListener(this);
            m_toggleButton_byte_1_5.setOnCheckedChangeListener(this);
            m_toggleButton_byte_1_6.setOnCheckedChangeListener(this);
            m_toggleButton_byte_1_7.setOnCheckedChangeListener(this);
            m_toggleButton_byte_1_8.setOnCheckedChangeListener(this);
            m_toggleButton_byte_2_1.setOnCheckedChangeListener(this);
            m_toggleButton_byte_2_2.setOnCheckedChangeListener(this);
            m_toggleButton_byte_2_3.setOnCheckedChangeListener(this);
            m_toggleButton_byte_2_4.setOnCheckedChangeListener(this);
            m_toggleButton_byte_2_5.setOnCheckedChangeListener(this);
            m_toggleButton_byte_2_6.setOnCheckedChangeListener(this);
            m_toggleButton_byte_2_7.setOnCheckedChangeListener(this);
            m_toggleButton_byte_2_8.setOnCheckedChangeListener(this);

        }

        @Override
        public void onPause() {
            super.onPause();

            Paused();
        }

        private void Paused() {
            if(m_CommunicationTask != null) {
                m_CommunicationTask.unregisterListener(this);
            }

            m_toggleButton_byte_1_1.setOnCheckedChangeListener(null);
            m_toggleButton_byte_1_2.setOnCheckedChangeListener(null);
            m_toggleButton_byte_1_3.setOnCheckedChangeListener(null);
            m_toggleButton_byte_1_4.setOnCheckedChangeListener(null);
            m_toggleButton_byte_1_5.setOnCheckedChangeListener(null);
            m_toggleButton_byte_1_6.setOnCheckedChangeListener(null);
            m_toggleButton_byte_1_7.setOnCheckedChangeListener(null);
            m_toggleButton_byte_1_8.setOnCheckedChangeListener(null);
            m_toggleButton_byte_2_1.setOnCheckedChangeListener(null);
            m_toggleButton_byte_2_2.setOnCheckedChangeListener(null);
            m_toggleButton_byte_2_3.setOnCheckedChangeListener(null);
            m_toggleButton_byte_2_4.setOnCheckedChangeListener(null);
            m_toggleButton_byte_2_5.setOnCheckedChangeListener(null);
            m_toggleButton_byte_2_6.setOnCheckedChangeListener(null);
            m_toggleButton_byte_2_7.setOnCheckedChangeListener(null);
            m_toggleButton_byte_2_8.setOnCheckedChangeListener(null);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void onNewCommunicationStatus(String[] strStatus) {
            // Aggiorno lo stato
            if(m_c1_id_tv_communication_status != null) {
                m_c1_id_tv_communication_status.setText(strStatus[0] + " - " + strStatus[1]);
            }

            if(m_id_tv_byte_1_in != null) {
                m_id_tv_byte_1_in.setText(formatDataToArrayBinaryStringIn(m_Message.getDataByte(1), 1));
            }

            if(m_id_tv_byte_2_in != null) {
                m_id_tv_byte_2_in.setText(formatDataToArrayBinaryStringIn(m_Message.getDataByte(2), 2));
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(m_Message != null) {
                switch (buttonView.getId()) {
                    case R.id.toggleButton_byte_1_1:
                        m_Message.setActionBit(1, 1, isChecked);
                        break;
                    case R.id.toggleButton_byte_1_2:
                        m_Message.setActionBit(1, 2, isChecked);
                        break;
                    case R.id.toggleButton_byte_1_3:
                        m_Message.setActionBit(1, 3, isChecked);
                        break;
                    case R.id.toggleButton_byte_1_4:
                        m_Message.setActionBit(1, 4, isChecked);
                        break;
                    case R.id.toggleButton_byte_1_5:
                        m_Message.setActionBit(1, 5, isChecked);
                        break;
                    case R.id.toggleButton_byte_1_6:
                        m_Message.setActionBit(1, 6, isChecked);
                        break;
                    case R.id.toggleButton_byte_1_7:
                        m_Message.setActionBit(1, 7, isChecked);
                        break;
                    case R.id.toggleButton_byte_1_8:
                        m_Message.setActionBit(1, 8, isChecked);
                        break;

                    case R.id.toggleButton_byte_2_1:
                        m_Message.setActionBit(2, 1, isChecked);
                        break;
                    case R.id.toggleButton_byte_2_2:
                        m_Message.setActionBit(2, 2, isChecked);
                        break;
                    case R.id.toggleButton_byte_2_3:
                        m_Message.setActionBit(2, 3, isChecked);
                        break;
                    case R.id.toggleButton_byte_2_4:
                        m_Message.setActionBit(2, 4, isChecked);
                        break;
                    case R.id.toggleButton_byte_2_5:
                        m_Message.setActionBit(2, 5, isChecked);
                        break;
                    case R.id.toggleButton_byte_2_6:
                        m_Message.setActionBit(2, 6, isChecked);
                        break;
                    case R.id.toggleButton_byte_2_7:
                        m_Message.setActionBit(2, 7, isChecked);
                        break;
                    case R.id.toggleButton_byte_2_8:
                        m_Message.setActionBit(2, 8, isChecked);
                        break;
                }

                m_id_tv_byte_1_out.setText(formatDataToArrayBinaryStringOut(m_Message.getActionByte(1), 1));
                m_id_tv_byte_2_out.setText(formatDataToArrayBinaryStringOut(m_Message.getActionByte(2), 2));

                if(m_Message.isCommandActionChanged()) {
                    m_Message.setCommandAsToSend();
                }
            }
        }

        private void initializeData(){

            m_toggleButton_byte_1_1.setChecked(false);
            m_toggleButton_byte_1_2.setChecked(false);
            m_toggleButton_byte_1_3.setChecked(false);
            m_toggleButton_byte_1_4.setChecked(false);
            m_toggleButton_byte_1_5.setChecked(false);
            m_toggleButton_byte_1_6.setChecked(false);
            m_toggleButton_byte_1_7.setChecked(false);
            m_toggleButton_byte_1_8.setChecked(false);
            m_toggleButton_byte_2_1.setChecked(false);
            m_toggleButton_byte_2_2.setChecked(false);
            m_toggleButton_byte_2_3.setChecked(false);
            m_toggleButton_byte_2_4.setChecked(false);
            m_toggleButton_byte_2_5.setChecked(false);
            m_toggleButton_byte_2_6.setChecked(false);
            m_toggleButton_byte_2_7.setChecked(false);
            m_toggleButton_byte_2_8.setChecked(false);

            m_id_tv_byte_1_out.setText(getString(R.string.default_string_value_bit));
            m_id_tv_byte_1_in.setText(getString(R.string.default_string_value_bit));

            m_id_tv_byte_2_out.setText(getString(R.string.default_string_value_bit));
            m_id_tv_byte_2_in.setText(getString(R.string.default_string_value_bit));

            m_c1_id_tv_communication_status.setText(getString(R.string.default_string_value_char));

            m_Message.resetCommand();
            // Send Command
            if(m_Message.isCommandActionChanged()) {
                m_Message.setCommandAsToSend();
            }
        }
    }

    /**
     * A Fragment with seek bar
     */
    public static class C_2_Fragment extends BaseFragment implements SeekBar.OnSeekBarChangeListener {

        private SeekBar m_seekBar_byte_3;
        private SeekBar m_seekBar_byte_4;
        private SeekBar m_seekBar_byte_5;
        private SeekBar m_seekBar_byte_6;
        private SeekBar m_seekBar_byte_7;
        private SeekBar m_seekBar_byte_8;
        private SeekBar m_seekBar_byte_9;
        private SeekBar m_seekBar_byte_10;
        private SeekBar m_seekBar_byte_11;
        private SeekBar m_seekBar_byte_12;
        private SeekBar m_seekBar_byte_13;
        private SeekBar m_seekBar_byte_14;

        private TextView m_c_id_tv_byte_3_out;
        private TextView m_c_id_tv_byte_4_out;
        private TextView m_c_id_tv_byte_5_out;
        private TextView m_c_id_tv_byte_6_out;
        private TextView m_c_id_tv_byte_7_out;
        private TextView m_c_id_tv_byte_8_out;
        private TextView m_c_id_tv_byte_9_out;
        private TextView m_c_id_tv_byte_10_out;
        private TextView m_c_id_tv_byte_11_out;
        private TextView m_c_id_tv_byte_12_out;
        private TextView m_c_id_tv_byte_13_out;
        private TextView m_c_id_tv_byte_14_out;

        private TextView m_c_id_tv_byte_3_in;
        private TextView m_c_id_tv_byte_4_in;
        private TextView m_c_id_tv_byte_5_in;
        private TextView m_c_id_tv_byte_6_in;
        private TextView m_c_id_tv_byte_7_in;
        private TextView m_c_id_tv_byte_8_in;
        private TextView m_c_id_tv_byte_9_in;
        private TextView m_c_id_tv_byte_10_in;
        private TextView m_c_id_tv_byte_11_in;
        private TextView m_c_id_tv_byte_12_in;
        private TextView m_c_id_tv_byte_13_in;
        private TextView m_c_id_tv_byte_14_in;

        private TextView m_c2_id_tv_communication_status;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static C_2_Fragment newInstance(int sectionNumber) {
            C_2_Fragment fragment = new C_2_Fragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public C_2_Fragment() {
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            Resumed();
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            Paused();
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }

        @Override
        public void onActivityCreated (Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            m_seekBar_byte_3 = (SeekBar) getActivity().findViewById(R.id.seekBar_byte_3);
            m_seekBar_byte_4 = (SeekBar) getActivity().findViewById(R.id.seekBar_byte_4);
            m_seekBar_byte_5 = (SeekBar) getActivity().findViewById(R.id.seekBar_byte_5);
            m_seekBar_byte_6 = (SeekBar) getActivity().findViewById(R.id.seekBar_byte_6);
            m_seekBar_byte_7 = (SeekBar) getActivity().findViewById(R.id.seekBar_byte_7);
            m_seekBar_byte_8 = (SeekBar) getActivity().findViewById(R.id.seekBar_byte_8);
            m_seekBar_byte_9 = (SeekBar) getActivity().findViewById(R.id.seekBar_byte_9);
            m_seekBar_byte_10 = (SeekBar) getActivity().findViewById(R.id.seekBar_byte_10);
            m_seekBar_byte_11 = (SeekBar) getActivity().findViewById(R.id.seekBar_byte_11);
            m_seekBar_byte_12 = (SeekBar) getActivity().findViewById(R.id.seekBar_byte_12);
            m_seekBar_byte_13 = (SeekBar) getActivity().findViewById(R.id.seekBar_byte_13);
            m_seekBar_byte_14 = (SeekBar) getActivity().findViewById(R.id.seekBar_byte_14);

            m_c_id_tv_byte_3_out = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_3_out);
            m_c_id_tv_byte_4_out = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_4_out);
            m_c_id_tv_byte_5_out = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_5_out);
            m_c_id_tv_byte_6_out = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_6_out);
            m_c_id_tv_byte_7_out = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_7_out);
            m_c_id_tv_byte_8_out = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_8_out);
            m_c_id_tv_byte_9_out = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_9_out);
            m_c_id_tv_byte_10_out = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_10_out);
            m_c_id_tv_byte_11_out = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_11_out);
            m_c_id_tv_byte_12_out = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_12_out);
            m_c_id_tv_byte_13_out = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_13_out);
            m_c_id_tv_byte_14_out = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_14_out);

            m_c_id_tv_byte_3_in = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_3_in);
            m_c_id_tv_byte_4_in = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_4_in);
            m_c_id_tv_byte_5_in = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_5_in);
            m_c_id_tv_byte_6_in = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_6_in);
            m_c_id_tv_byte_7_in = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_7_in);
            m_c_id_tv_byte_8_in = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_8_in);
            m_c_id_tv_byte_9_in = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_9_in);
            m_c_id_tv_byte_10_in = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_10_in);
            m_c_id_tv_byte_11_in = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_11_in);
            m_c_id_tv_byte_12_in = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_12_in);
            m_c_id_tv_byte_13_in = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_13_in);
            m_c_id_tv_byte_14_in = (TextView) getActivity().findViewById(R.id.c_id_tv_byte_14_in);

            m_c2_id_tv_communication_status = (TextView) getActivity().findViewById(R.id.c2_id_tv_communication_status);

            // Prelevo i dati dei sensori
            String strSetSensorMaxOutputValue = SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_MAX_OUTPUT_VALUE);
            int iSetSensorMaxOutputValue = 0;
            try{
                iSetSensorMaxOutputValue = Integer.parseInt(strSetSensorMaxOutputValue);
            } catch (Exception ignored) {
            }

            m_seekBar_byte_3.setMax(iSetSensorMaxOutputValue);
            m_seekBar_byte_4.setMax(iSetSensorMaxOutputValue);
            m_seekBar_byte_5.setMax(iSetSensorMaxOutputValue);
            m_seekBar_byte_6.setMax(iSetSensorMaxOutputValue);
            m_seekBar_byte_7.setMax(iSetSensorMaxOutputValue);
            m_seekBar_byte_8.setMax(iSetSensorMaxOutputValue);
            m_seekBar_byte_9.setMax(iSetSensorMaxOutputValue);
            m_seekBar_byte_10.setMax(iSetSensorMaxOutputValue);
            m_seekBar_byte_11.setMax(iSetSensorMaxOutputValue);
            m_seekBar_byte_12.setMax(iSetSensorMaxOutputValue);
            m_seekBar_byte_13.setMax(iSetSensorMaxOutputValue);
            m_seekBar_byte_14.setMax(iSetSensorMaxOutputValue);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.c_2_drive_fragment, container, false);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            Resumed();
        }

        private void Resumed() {

            initializeData();

            // Registro Listeners
            if(m_CommunicationTask != null) {
                m_CommunicationTask.registerListener(this);
            }

            m_seekBar_byte_3.setOnSeekBarChangeListener(this);
            m_seekBar_byte_4.setOnSeekBarChangeListener(this);
            m_seekBar_byte_5.setOnSeekBarChangeListener(this);
            m_seekBar_byte_6.setOnSeekBarChangeListener(this);
            m_seekBar_byte_7.setOnSeekBarChangeListener(this);
            m_seekBar_byte_8.setOnSeekBarChangeListener(this);
            m_seekBar_byte_9.setOnSeekBarChangeListener(this);
            m_seekBar_byte_10.setOnSeekBarChangeListener(this);
            m_seekBar_byte_11.setOnSeekBarChangeListener(this);
            m_seekBar_byte_12.setOnSeekBarChangeListener(this);
            m_seekBar_byte_13.setOnSeekBarChangeListener(this);
            m_seekBar_byte_14.setOnSeekBarChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            Paused();
        }
        private void Paused() {
            if(m_CommunicationTask != null) {
                m_CommunicationTask.unregisterListener(this);
            }

            m_seekBar_byte_3.setOnSeekBarChangeListener(null);
            m_seekBar_byte_4.setOnSeekBarChangeListener(null);
            m_seekBar_byte_5.setOnSeekBarChangeListener(null);
            m_seekBar_byte_6.setOnSeekBarChangeListener(null);
            m_seekBar_byte_7.setOnSeekBarChangeListener(null);
            m_seekBar_byte_8.setOnSeekBarChangeListener(null);
            m_seekBar_byte_9.setOnSeekBarChangeListener(null);
            m_seekBar_byte_10.setOnSeekBarChangeListener(null);
            m_seekBar_byte_11.setOnSeekBarChangeListener(null);
            m_seekBar_byte_12.setOnSeekBarChangeListener(null);
            m_seekBar_byte_13.setOnSeekBarChangeListener(null);
            m_seekBar_byte_14.setOnSeekBarChangeListener(null);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void onNewCommunicationStatus(String[] strStatus) {
            // Aggiorno lo stato
            if(m_c2_id_tv_communication_status != null) {
                m_c2_id_tv_communication_status.setText(strStatus[0] + " - " + strStatus[1]);
            }

            if(m_c_id_tv_byte_3_in != null)
                m_c_id_tv_byte_3_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(3),3));
            if(m_c_id_tv_byte_4_in != null)
                m_c_id_tv_byte_4_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(4),4));
            if(m_c_id_tv_byte_5_in != null)
                m_c_id_tv_byte_5_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(5),5));
            if(m_c_id_tv_byte_6_in != null)
                m_c_id_tv_byte_6_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(6),6));
            if(m_c_id_tv_byte_7_in != null)
                m_c_id_tv_byte_7_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(7),7));
            if(m_c_id_tv_byte_8_in != null)
                m_c_id_tv_byte_8_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(8),8));
            if(m_c_id_tv_byte_9_in != null)
                m_c_id_tv_byte_9_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(9),9));
            if(m_c_id_tv_byte_10_in != null)
                m_c_id_tv_byte_10_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(10),10));
            if(m_c_id_tv_byte_11_in != null)
                m_c_id_tv_byte_11_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(11),11));
            if(m_c_id_tv_byte_12_in != null)
                m_c_id_tv_byte_12_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(12),12));
            if(m_c_id_tv_byte_13_in != null)
                m_c_id_tv_byte_13_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(13),13));
            if(m_c_id_tv_byte_14_in != null)
                m_c_id_tv_byte_14_in.setText(formatDataToArrayStringIn(m_Message.getDataByte(14),14));

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            switch (seekBar.getId())
            {
                case R.id.seekBar_byte_3 :
                    m_c_id_tv_byte_3_out.setText(formatDataToArrayStringOut(progress, 3));
                    m_Message.setActionByte((byte)progress,3);
                    break;
                case R.id.seekBar_byte_4 :
                    m_c_id_tv_byte_4_out.setText(formatDataToArrayStringOut(progress, 4));
                    m_Message.setActionByte((byte)progress,4);
                    break;
                case R.id.seekBar_byte_5 :
                    m_c_id_tv_byte_5_out.setText(formatDataToArrayStringOut(progress, 5));
                    m_Message.setActionByte((byte)progress,5);
                    break;
                case R.id.seekBar_byte_6 :
                    m_c_id_tv_byte_6_out.setText(formatDataToArrayStringOut(progress, 6));
                    m_Message.setActionByte((byte)progress,6);
                    break;
                case R.id.seekBar_byte_7 :
                    m_c_id_tv_byte_7_out.setText(formatDataToArrayStringOut(progress, 7));
                    m_Message.setActionByte((byte)progress,7);
                    break;
                case R.id.seekBar_byte_8 :
                    m_c_id_tv_byte_8_out.setText(formatDataToArrayStringOut(progress, 8));
                    m_Message.setActionByte((byte)progress,8);
                    break;
                case R.id.seekBar_byte_9 :
                    m_c_id_tv_byte_9_out.setText(formatDataToArrayStringOut(progress, 9));
                    m_Message.setActionByte((byte)progress,9);
                    break;
                case R.id.seekBar_byte_10 :
                    m_c_id_tv_byte_10_out.setText(formatDataToArrayStringOut(progress, 10));
                    m_Message.setActionByte((byte)progress,10);
                    break;
                case R.id.seekBar_byte_11 :
                    m_c_id_tv_byte_11_out.setText(formatDataToArrayStringOut(progress, 11));
                    m_Message.setActionByte((byte)progress,11);
                    break;
                case R.id.seekBar_byte_12 :
                    m_c_id_tv_byte_12_out.setText(formatDataToArrayStringOut(progress, 12));
                    m_Message.setActionByte((byte)progress,12);
                    break;
                case R.id.seekBar_byte_13 :
                    m_c_id_tv_byte_13_out.setText(formatDataToArrayStringOut(progress, 13));
                    m_Message.setActionByte((byte)progress,13);
                    break;
                case R.id.seekBar_byte_14 :
                    m_c_id_tv_byte_14_out.setText(formatDataToArrayStringOut(progress, 14));
                    m_Message.setActionByte((byte)progress,14);
                    break;
            }

            if(m_Message.isCommandActionChanged()) {
                m_Message.setCommandAsToSend();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
        private void initializeData(){

            m_seekBar_byte_3.setProgress(0);
            m_seekBar_byte_4.setProgress(0);
            m_seekBar_byte_5.setProgress(0);
            m_seekBar_byte_6.setProgress(0);
            m_seekBar_byte_7.setProgress(0);
            m_seekBar_byte_8.setProgress(0);
            m_seekBar_byte_9.setProgress(0);
            m_seekBar_byte_10.setProgress(0);
            m_seekBar_byte_11.setProgress(0);
            m_seekBar_byte_12.setProgress(0);
            m_seekBar_byte_13.setProgress(0);
            m_seekBar_byte_14.setProgress(0);

            m_c_id_tv_byte_3_out.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_4_out.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_5_out.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_6_out.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_7_out.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_8_out.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_9_out.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_10_out.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_11_out.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_12_out.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_13_out.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_14_out.setText(R.string.default_string_value_array);

            m_c_id_tv_byte_3_in.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_4_in.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_5_in.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_6_in.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_7_in.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_8_in.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_9_in.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_10_in.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_11_in.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_12_in.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_13_in.setText(R.string.default_string_value_array);
            m_c_id_tv_byte_14_in.setText(R.string.default_string_value_array);

             m_c2_id_tv_communication_status.setText(getString(R.string.default_string_value_char));

            m_Message.resetCommand();
            // Send Command
            if(m_Message.isCommandActionChanged()) {
                m_Message.setCommandAsToSend();
            }
        }

    }
    /**
     * A Fragment with sample arduino code
     */
    public static class SAC_Fragment extends BaseFragment {

        private PretolesiEditText m_sac_id_tv_sample_code;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static SAC_Fragment newInstance(int sectionNumber) {
            SAC_Fragment fragment = new SAC_Fragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public SAC_Fragment() {
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

        @Override
        public void onActivityCreated (Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            m_sac_id_tv_sample_code = (PretolesiEditText) getActivity().findViewById(R.id.sac_id_tv_sample_code);
            m_sac_id_tv_sample_code.setAsReadOnly();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.sac_fragment, container, false);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            Resumed();
        }

        private void Resumed() {
            initializeData();
        }

        @Override
        public void onPause() {
            super.onPause();
            Paused();
        }

        private void Paused() {
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void onNewCommunicationStatus(String[] strStatus) {
        }

        private void initializeData(){

            m_sac_id_tv_sample_code.setText("/*\n" +
                    " TCP IP Server\n" +
                    "\n" +
                    "  Circuit:\n" +
                    "  * WiFi shield attached\n" +
                    "\n" +
                    "  created 03 Feb 2015\n" +
                    "  by Riccardo Pretolesi\n" +
                    "\n" +
                    "\n" +
                    "*/\n" +
                    "\n" +
                    "#include <SPI.h>\n" +
                    "#include <WiFi.h>\n" +
                    "\n" +
                    "char ssid[] = \"PretolesiWiFi\";          //  your network SSID (name)\n" +
                    "char pass[] = \"01234567\";   // your network password\n" +
                    "\n" +
                    "int status = WL_IDLE_STATUS;\n" +
                    "int m_ServerTCPPort = 502;\n" +
                    "WiFiServer m_server(m_ServerTCPPort);\n" +
                    "\n" +
                    "boolean m_bOneShotClientConnected = false;\n" +
                    "boolean m_bOneShotClientDisconnected = false;\n" +
                    "\n" +
                    "// Dati di comunicazione\n" +
                    "byte SOH = 0x01;\n" +
                    "byte EOT = 0x04;\n" +
                    "byte ENQ = 0x05;\n" +
                    "byte ACK = 0x06;\n" +
                    "\n" +
                    "\n" +
                    "int m_iNrByteToRead = 0;\n" +
                    "int m_iNrByteRead = 0;\n" +
                    "byte m_byteRead[16] = {0};\n" +
                    "boolean m_bENQInProgress = false;\n" +
                    "boolean m_bSOHInProgress = false;\n" +
                    "byte m_byteFirstByteRead = 0;\n" +
                    "byte m_byteToWrite[16] = {0};\n" +
                    "unsigned long  m_ulTimeoutRefMillisecs = 0;\n" +
                    "void setup()\n" +
                    "{\n" +
                    "  // initialize serial:\n" +
                    "  Serial.begin(9600);\n" +
                    "\n" +
                    "  // Deselect SD Card\n" +
                    "  pinMode(4, OUTPUT);\n" +
                    "  digitalWrite(4, 1);\n" +
                    "\n" +
                    "  // check for the presence of the shield:\n" +
                    "  if (WiFi.status() == WL_NO_SHIELD)\n" +
                    "  {\n" +
                    "    Serial.println(\"WiFi shield not present\");\n" +
                    "    // don't continue:\n" +
                    "    while(true);\n" +
                    "  }\n" +
                    "\n" +
                    "   Serial.println(\"Attempting to connect to WPA network...\");\n" +
                    "   Serial.print(\"SSID: \");\n" +
                    "   Serial.println(ssid);\n" +
                    "\n" +
                    "   status = WiFi.begin(ssid, pass);\n" +
                    "   if ( status != WL_CONNECTED) {\n" +
                    "     Serial.println(\"Couldn't get a wifi connection\");\n" +
                    "     while(true);\n" +
                    "   }\n" +
                    "   else {\n" +
                    "     m_server.begin();\n" +
                    "     Serial.print(\"Connected to wifi.\");\n" +
                    "\n" +
                    "     // Print WiFi Status\n" +
                    "     printWifiServerStatus();\n" +
                    "   }\n" +
                    "}\n" +
                    "\n" +
                    "\n" +
                    "void loop()\n" +
                    "{\n" +
                    "\n" +
                    "  // WiFi Communication\n" +
                    "  Communication();\n" +
                    "}\n" +
                    "\n" +
                    "void Communication()\n" +
                    "{\n" +
                    "   // wait for a new client:\n" +
                    "  WiFiClient m_client = m_server.available();\n" +
                    "  // when the client sends the first byte, say hello:\n" +
                    "  if(m_client != NULL)\n" +
                    "  {\n" +
                    "    if(m_client.connected())\n" +
                    "    {\n" +
                    "      m_bOneShotClientDisconnected = false;\n" +
                    "      if(m_bOneShotClientConnected == false)\n" +
                    "      {\n" +
                    "        m_bOneShotClientConnected = true;\n" +
                    "\n" +
                    "         // clear input buffer:\n" +
                    "        m_client.flush();\n" +
                    "\n" +
                    "        // Init buffer data\n" +
                    "        m_iNrByteRead = 0;\n" +
                    "        for(int indice_1 = 0; indice_1 < 16; indice_1++)\n" +
                    "        {\n" +
                    "          m_byteRead[indice_1] = 0;\n" +
                    "        }\n" +
                    "        m_byteToWrite[0] = ACK;\n" +
                    "        m_byteToWrite[15] = EOT;\n" +
                    "\n" +
                    "        // Reference for timeout\n" +
                    "        m_ulTimeoutRefMillisecs = millis();\n" +
                    "\n" +
                    "        Serial.println(\"Client Connected.\");\n" +
                    "      }\n" +
                    "\n" +
                    "      // Read and write operation....\n" +
                    "      // Checking the first byte....\n" +
                    "      // Devono essere 16\n" +
                    "      m_iNrByteToRead = m_client.available();\n" +
                    "      if (m_iNrByteToRead >= 1)\n" +
                    "      {\n" +
                    "        // Reference for timeout\n" +
                    "        m_ulTimeoutRefMillisecs = millis();\n" +
                    "\n" +
                    "        if(m_bENQInProgress == false && m_bSOHInProgress == false)\n" +
                    "        {\n" +
                    "          // Check the message\n" +
                    "          // Read the first byte\n" +
                    "          m_byteFirstByteRead = m_client.read();\n" +
                    "          m_iNrByteRead = m_iNrByteRead + 1;\n" +
                    "          // Just a enquiry....\n" +
                    "          if(m_byteFirstByteRead == ENQ)\n" +
                    "          {\n" +
                    "            m_bENQInProgress = true;\n" +
                    "          }\n" +
                    "          // Data to read....\n" +
                    "          if(m_byteFirstByteRead == SOH)\n" +
                    "          {\n" +
                    "            m_bSOHInProgress = true;\n" +
                    "          }\n" +
                    "        }\n" +
                    "\n" +
                    "        // Just a enquiry....\n" +
                    "        if(m_bENQInProgress == true)\n" +
                    "        {\n" +
                    "//          Serial.println(\"ENQ byte read.\");\n" +
                    "\n" +
                    "          for(int index_1 = 0; index_1 < 16; index_1++)\n" +
                    "          {\n" +
                    "            m_client.write(m_byteToWrite[index_1]);\n" +
                    "//            Serial.print(\"ENQ byte write: \");\n" +
                    "//            Serial.print(m_byteToWrite[index_1]);\n" +
                    "//            Serial.print(\" index: \");\n" +
                    "//            Serial.println(index_1);\n" +
                    "          }\n" +
                    "          m_iNrByteRead = 0;\n" +
                    "          m_bENQInProgress = false;\n" +
                    "        }\n" +
                    "\n" +
                    "        // Data to read....\n" +
                    "        if(m_bSOHInProgress == true)\n" +
                    "        {\n" +
                    "          for(int index_0 = m_iNrByteRead; index_0 < m_iNrByteToRead; index_0++)\n" +
                    "          {\n" +
                    "            m_byteRead[m_iNrByteRead] = m_client.read();\n" +
                    "//            Serial.print(\"SOH byte read: \");\n" +
                    "//            Serial.print(m_byteRead[m_iNrByteRead]);\n" +
                    "//            Serial.print(\" index: \");\n" +
                    "//            Serial.println(m_iNrByteRead);\n" +
                    "            m_iNrByteRead = m_iNrByteRead + 1;\n" +
                    "            if(m_iNrByteRead >= 16)\n" +
                    "            {\n" +
                    "              m_iNrByteRead = 0;\n" +
                    "              m_bSOHInProgress = false;\n" +
                    "\n" +
                    "              // Check the last char...\n" +
                    "              if(m_byteRead[15] == EOT)\n" +
                    "              {\n" +
                    "                // Store the result and write back....\n" +
                    "                // Here i can use the data received....\n" +
                    "                // Digital\n" +
                    "                boolean b_1_1 = ((m_byteRead[1] & 0b00000001) == 1);\n" +
                    "                boolean b_1_2 = ((m_byteRead[1] & 0b00000010) == 2);\n" +
                    "                boolean b_1_3 = ((m_byteRead[1] & 0b00000100) == 4);\n" +
                    "                boolean b_1_4 = ((m_byteRead[1] & 0b00001000) == 8);\n" +
                    "                boolean b_1_5 = ((m_byteRead[1] & 0b00010000) == 16);\n" +
                    "                boolean b_1_6 = ((m_byteRead[1] & 0b00100000) == 32);\n" +
                    "                boolean b_1_7 = ((m_byteRead[1] & 0b01000000) == 64);\n" +
                    "                boolean b_1_8 = ((m_byteRead[1] & 0b10000000) == 128);\n" +
                    "                // ...\n" +
                    "\n" +
                    "                // Analogic\n" +
                    "                byte byte_3 = m_byteRead[3];\n" +
                    "                byte byte_4 = m_byteRead[4];\n" +
                    "                byte byte_5 = m_byteRead[5];\n" +
                    "                // ...\n" +
                    "                byte byte_14 = m_byteRead[14];\n" +
                    "\n" +
                    "\n" +
                    "                // Write back ....\n" +
                    "                // Digital\n" +
                    "                m_byteToWrite[1] = 1;\n" +
                    "                // ...\n" +
                    "\n" +
                    "                // Analogic\n" +
                    "                m_byteToWrite[3] = 1;\n" +
                    "                m_byteToWrite[4] = 100;\n" +
                    "                m_byteToWrite[5] = 200;\n" +
                    "                // ...\n" +
                    "                m_byteToWrite[14] = 250;\n" +
                    "\n" +
                    "\n" +
                    "                /*\n" +
                    "                 * Test\n" +
                    "                 *\n" +
                    "                for(int index_2 = 1; index_2 < 15; index_2++)\n" +
                    "                {\n" +
                    "                   m_byteToWrite[index_2] = m_byteRead[index_2];\n" +
                    "                }\n" +
                    "                */\n" +
                    "\n" +
                    "                for(int index_3 = 0; index_3 < 16; index_3++)\n" +
                    "                {\n" +
                    "                   m_client.write(m_byteToWrite[index_3]);\n" +
                    "//                   Serial.print(\"SOH byte write: \");\n" +
                    "//                   Serial.print(m_byteToWrite[index_3]);\n" +
                    "//                   Serial.print(\" index: \");\n" +
                    "//                   Serial.println(index_3);\n" +
                    "                }\n" +
                    "              }\n" +
                    "              else\n" +
                    "              {\n" +
                    "                Serial.println(\"EOT Error. \");\n" +
                    "                m_client.stop();\n" +
                    "              }\n" +
                    "              break;\n" +
                    "            }\n" +
                    "          }\n" +
                    "        }\n" +
                    "      }\n" +
                    "\n" +
                    "      // Timeout\n" +
                    "      if(millis() < m_ulTimeoutRefMillisecs)\n" +
                    "      {\n" +
                    "        m_ulTimeoutRefMillisecs = millis();\n" +
                    "\n" +
                    "        Serial.println(\"Reset millis().\");\n" +
                    "      }\n" +
                    "      if((millis() - m_ulTimeoutRefMillisecs) > 5000)\n" +
                    "      {\n" +
                    "        m_client.stop();\n" +
                    "\n" +
                    "        m_bOneShotClientConnected = false;\n" +
                    "\n" +
                    "        Serial.println(\"Stop for Timeout.\");\n" +
                    "      }\n" +
                    "    }\n" +
                    "    else\n" +
                    "    {\n" +
                    "      m_bOneShotClientConnected = false;\n" +
                    "\n" +
                    "      if(m_bOneShotClientDisconnected == false)\n" +
                    "      {\n" +
                    "        m_bOneShotClientDisconnected = true;\n" +
                    "\n" +
                    "        m_client.stop();\n" +
                    "\n" +
                    "        Serial.println(\"Client Disconnected.\");\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "  else\n" +
                    "  {\n" +
                    "    m_bOneShotClientConnected = false;\n" +
                    "\n" +
                    "    if(m_bOneShotClientDisconnected == false)\n" +
                    "    {\n" +
                    "      m_bOneShotClientDisconnected = true;\n" +
                    "\n" +
                    "      Serial.println(\"Client Null.\");\n" +
                    "    }\n" +
                    "  }\n" +
                    "}\n" +
                    "\n" +
                    "void printWifiServerStatus()\n" +
                    "{\n" +
                    "  // print the SSID of the network you're attached to:\n" +
                    "  Serial.print(\"SSID: \");\n" +
                    "  Serial.println(WiFi.SSID());\n" +
                    "\n" +
                    "  // print your WiFi shield's IP address:\n" +
                    "  IPAddress ip = WiFi.localIP();\n" +
                    "  Serial.print(\"PCP/IP Address: \");\n" +
                    "  Serial.println(ip);\n" +
                    "  Serial.print(\"TCP/IP Port: \");\n" +
                    "  Serial.println(m_ServerTCPPort);\n" +
                    "\n" +
                    "  // print the received signal strength:\n" +
                    "  long rssi = WiFi.RSSI();\n" +
                    "  Serial.print(\"signal strength (RSSI):\");\n" +
                    "  Serial.print(rssi);\n" +
                    "  Serial.println(\" dBm\");\n" +
                    "}");

            m_Message.resetCommand();
            // Send Command
            if(m_Message.isCommandActionChanged()) {
                m_Message.setCommandAsToSend();
            }
        }

    }

    private class CommunicationTask extends AsyncTask<Object, String, Void> {
        private List<CommunicationStatus> lCSListener = new Vector<CommunicationStatus>();

        // Imposto il listener
        public synchronized void registerListener(CommunicationStatus listener) {
            lCSListener.add(listener);
        }
        public synchronized void unregisterListener(CommunicationStatus listener) {
            lCSListener.remove(listener);
        }

        // Funzione richiamata ogni volta che ci sono dei dati da aggiornare
        private void onUpdate(String[] strStatus) {
            // Check if the Listener was set, otherwise we'll get an Exception when we try to call it
            if(lCSListener != null) {
                for (CommunicationStatus cs : lCSListener) {
                    cs.onNewCommunicationStatus(strStatus);
                }
            }
        }

        @Override
        protected Void doInBackground(Object...obj) {
            //Prendo i parametri
            ArduinoClientSocket acs = (ArduinoClientSocket) obj[0];
            Message msg = (Message) obj[1];
            String strStatus = "";
            String strError = "";
            int iCommFrame = 0;

            // Dati di set
            String strIpAddress = "";
            int iPort = 0;
            int iTimeout = 0;
            int iCommFrameDelay = 0;

            byte[] byteToRead = new byte[64];

            try {
                while (!isCancelled() && acs != null && msg != null) {

                    if (acs.isConnected() == false) {
                        // Pubblico i dati
                        strStatus = getString(R.string.comm_status_connecting);
                        strError = "";
                        this.publishProgress(strStatus, strError,"");

                        try {
                            strIpAddress = SQLContract.Settings.getParameter(getApplicationContext(), SQLContract.Parameter.IP_ADDRESS);
                            iPort = Integer.parseInt(SQLContract.Settings.getParameter(getApplicationContext(), SQLContract.Parameter.PORT));
                            iTimeout = Integer.parseInt(SQLContract.Settings.getParameter(getApplicationContext(), SQLContract.Parameter.TIMEOUT));
                            iCommFrameDelay = Integer.parseInt(SQLContract.Settings.getParameter(getApplicationContext(), SQLContract.Parameter.COMM_FRAME_DELAY));
                        }
                        catch (Exception ex) {
                        }
                        if(strIpAddress.equals("") == false && iPort > 0 && iTimeout > 0) {
                            if (acs.connectToArduino(strIpAddress, iPort, iTimeout) == true) {
                                strStatus = getString(R.string.comm_status_connected);
                                strError = "";
                                this.publishProgress(strStatus, strError, "");
                            } else {
                                strStatus = getString(R.string.comm_status_error);
                                strError = acs.getLastError();
                                this.publishProgress(strStatus, strError,"");

                                // attendo per non sovraccaricare CPU
                                try {
                                    Thread.sleep(3000, 0);
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                        else
                        {
                            strStatus = getString(R.string.comm_status_error);
                            strError = getString(R.string.db_data_server_error);
                            this.publishProgress(strStatus, strError,"");
                            // attendo per non sovraccaricare CPU
                            try {
                                Thread.sleep(3000, 0);
                            } catch (InterruptedException e) {
                            }
                        }
                    } else {
                        long lTime_1;
                        long lTime_2;
                        if(acs.sendData(msg)) {
                            lTime_1 = acs.getGetSendAnswerTimeMilliseconds();

                            if(acs.getData(msg)) {
                                lTime_2 = acs.getSendGetAnswerTimeMilliseconds();
                                 // Tutto Ok, posso leggere i dati ricevuti


                                 // Faccio avanzare una barra ad ogni frame
                                 iCommFrame = iCommFrame + 1;
                                 if(iCommFrame > 16) {
                                     iCommFrame = 1;
                                 }
                                 strStatus = getString(R.string.comm_status_online);
                                 strError = "";
                                 for(int index = 0; index < 20; index++){
                                     if(index < iCommFrame) {
                                         strError = strError + "-";
                                     }
                                     else
                                     {
                                         strError = strError + " ";
                                     }
                                 }
                                 strError = strError + String.valueOf(lTime_1) + " - " + String.valueOf(lTime_2);
                                 this.publishProgress(strStatus, strError, "");

                                // attendo per non sovraccaricare CPU
                                try {
                                    if((iCommFrameDelay * 2) < 10)
                                    {
                                        iCommFrameDelay = 10;
                                    }
                                    Thread.sleep((iCommFrameDelay * 2), 0);
                                } catch (InterruptedException e) {
                                }
                            } else {
                                 strStatus = getString(R.string.comm_status_error);
                                 strError = acs.getLastError();
                                 this.publishProgress(strStatus, strError, "");
                                 // attendo per non sovraccaricare CPU
                                 try {
                                     Thread.sleep(3000, 0);
                                 } catch (InterruptedException e) {
                                 }
                            }
                        } else {
                            strStatus = getString(R.string.comm_status_error);
                            strError = acs.getLastError();
                            this.publishProgress(strStatus, strError, "");
                            // attendo per non sovraccaricare CPU
                            try {
                                Thread.sleep(3000, 0);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
                strError = "";
            } catch (Exception ex) {
                strError = ex.getMessage();
            }

            // Pubblico i dati
            acs.closeConnection();
            strStatus = getString(R.string.comm_status_closed);
            this.publishProgress(strStatus,strError,"");

            return null;
        }


        @Override
        protected void onProgressUpdate(String... strStatus) {
            super.onProgressUpdate(strStatus);
            // Aggiorno i dati
            onUpdate(strStatus);
        }
    }


    // Funzioni di supporto
    static byte floatTobyte(float f) {

        short sh_1 = 0;
        byte byteRes = 0;

        if(f < 0.0f) {
            f = 0.0f;
        }
        if(f > 255.0f) {
            f = 255.0f;
        }

        DecimalFormat df = new DecimalFormat("###");
        sh_1 = Short.valueOf(df.format(f));
        byteRes = (byte) (sh_1 & 0xFF);

        return byteRes;
    }

    static String formatDataToArrayStringOut(int iData, int iIndex) {
        return "-[" + Integer.toString(iIndex) + "]-" + Integer.toString(iData) + "->";
    }
    static String formatDataToArrayStringIn(int iData, int iIndex) {
        return "<-[" + Integer.toString(iIndex) + "]-" + Integer.toString(iData) + "-";
    }
    static String formatDataToArrayBinaryStringOut(int iData, int iIndex) {
        String str = String.format("%8s", Integer.toBinaryString(iData & 0xFF)).replace(' ', '0');
        return "-[" + Integer.toString(iIndex) + "]-" + str + "->";
    }
    static String formatDataToArrayBinaryStringIn(int iData, int iIndex) {
        String str = String.format("%8s", Integer.toBinaryString(iData & 0xFF)).replace(' ', '0');
        return "<-[" + Integer.toString(iIndex) + "]-" + str + "-";
    }
    static short floatToshort(float f) {
        byte byte_1 = floatTobyte(f);
        short sh_1 = (short)(byte_1 & 0xff);
        return sh_1;
    }
    // Deemphasize transient forces
    static float lowPass(float current, float gravity, float alpha) {
        return gravity * alpha + current * ((float)1.0 - alpha);
    }
}
