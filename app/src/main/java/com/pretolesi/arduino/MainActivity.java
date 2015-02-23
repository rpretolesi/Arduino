package com.pretolesi.arduino;

import java.text.DecimalFormat;
import java.util.Locale;
import java.lang.Math;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import SQL.SQLContract;

public class MainActivity extends ActionBarActivity
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
/*
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
*/
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
/*
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
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
*/
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
                fragment = CBFragment.newInstance(position + 1);
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
                    return getString(R.string.drive_title_section_drive).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
//                case 2:
//                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
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
    public static class DriveFragment extends Fragment implements SensorEventListener
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
        private boolean m_bDriveWheelStartStopStatus_FP_Stop;
        private Button m_drive_id_btn_drive_fork_start_stop;
        private boolean m_bDriveForkStartStopStatus;
        private boolean m_bDriveForkStartStopStatus_FP_Stop;

        private TextView m_drive_id_tv_command_queue;
        private TextView m_drive_text_tv_value_up;
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
        public void onActivityCreated (Bundle savedInstanceState)
        {
            super.onActivityCreated(savedInstanceState);

            m_drive_id_btn_drive_wheel_start_stop = (Button) getActivity().findViewById(R.id.drive_id_btn_drive_wheel_start_stop);
            m_drive_id_btn_drive_fork_start_stop = (Button) getActivity().findViewById(R.id.drive_id_btn_drive_wheel_fork_start_stop);
            m_drive_id_tv_command_queue = (TextView) getActivity().findViewById(R.id.drive_id_tv_command_queue);
            m_drive_text_tv_value_up = (TextView) getActivity().findViewById(R.id.drive_id_tv_value_up);
            m_drive_text_tv_value_down = (TextView) getActivity().findViewById(R.id.drive_id_tv_value_down);
            m_drive_text_tv_value_left = (TextView) getActivity().findViewById(R.id.drive_id_tv_value_left);
            m_drive_text_tv_value_right = (TextView) getActivity().findViewById(R.id.drive_id_tv_value_right);
            m_drive_id_tv_communication_status = (TextView) getActivity().findViewById(R.id.drive_id_tv_communication_status);

            // Set an OnClickListener
            m_drive_id_btn_drive_wheel_start_stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (m_Message != null) {
                        if (!m_bDriveWheelStartStopStatus) {
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
                        }
                    }
                }
            });

            m_drive_id_btn_drive_fork_start_stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (m_Message != null) {
                        if (!m_bDriveForkStartStopStatus) {
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


            // Registro Listeners
            if(m_CommunicationTask != null) {
                m_CommunicationTask.setCommunicationStatusListener(new CommunicationStatus() {
                    @Override
                    public void onNewCommunicationStatus(String[] strStatus) {
                        // Aggiorno lo stato
                        if(m_drive_id_tv_communication_status != null) {
                            m_drive_id_tv_communication_status.setText(strStatus[0] + " - " + strStatus[1]);
                        }
                        if(m_drive_id_tv_command_queue != null) {
                            m_drive_id_tv_command_queue.setText(getText(R.string.comm_status_queue) + strStatus[2]);
                        }
                        if(m_Message != null) {
                            if(m_bDriveWheelStartStopStatus) {
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
                                // Aggiorno lo stato dei colori
                                if(m_Message.getDriveForkUpStatus()) {
                                    m_drive_text_tv_value_up.setTextColor(Color.GREEN);
                                } else if(m_Message.getDriveForkUp()) {
                                    m_drive_text_tv_value_up.setTextColor(Color.YELLOW);
                                } else {
                                    m_drive_text_tv_value_up.setTextColor(Color.BLACK);
                                }

                                if(m_Message.getDriveForkDownStatus()) {
                                    m_drive_text_tv_value_down.setTextColor(Color.GREEN);
                                } else if(m_Message.getDriveForkDown()) {
                                    m_drive_text_tv_value_down.setTextColor(Color.YELLOW);
                                } else {
                                    m_drive_text_tv_value_down.setTextColor(Color.BLACK);
                                }

                                if (!m_Message.getDriveForkOpenStatus()) {
                                    if(m_Message.getDriveForkOpen()) {
                                        m_drive_text_tv_value_left.setTextColor(Color.YELLOW);
                                    } else {
                                        m_drive_text_tv_value_left.setTextColor(Color.BLACK);
                                    }
                                } else {
                                    m_drive_text_tv_value_left.setTextColor(Color.GREEN);
                                }

                                if(m_Message.getDriveForkCloseStatus()) {
                                    m_drive_text_tv_value_right.setTextColor(Color.GREEN);
                                }
                                else if(m_Message.getDriveForkClose()) {
                                    m_drive_text_tv_value_right.setTextColor(Color.YELLOW);
                                } else {
                                    m_drive_text_tv_value_right.setTextColor(Color.BLACK);
                                }
                            }
                        }
                    }
                });
            }

            m_SensorManager.registerListener(this, m_Accelerometer, SensorManager.SENSOR_DELAY_UI);
            m_SensorManager.registerListener(this, m_Magnetometer, SensorManager.SENSOR_DELAY_UI);

        }

        @Override
        public void onPause() {
            super.onPause();

            if(m_CommunicationTask != null) {
                m_CommunicationTask.setCommunicationStatusListener(null);
            }

            m_SensorManager.unregisterListener(this);
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
                m_bDriveWheelStartStopStatus_FP_Stop = false;

                // Throttle
                fThrottle = Math.abs(fAzim);
                if(fAzim > 0) {
                    fThrottleFWD = fThrottle;
                    fThrottleREV = 0;
                    if(fThrottleFWD > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveWheelFWD(true);
                    }
                    else
                    {
                        m_Message.setDriveWheelFWD(false);
                    }
                    m_Message.setThrottleFWD(floatTobyte(fThrottleFWD));
                }
                if(fAzim < 0) {
                    fThrottleFWD = 0;
                    fThrottleREV = fThrottle;

                    if(fThrottleREV > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveWheelREV(true);
                    }
                    else
                    {
                        m_Message.setDriveWheelREV(false);
                    }
                    m_Message.setThrottleREV(floatTobyte(fThrottleREV));
                }

                // Steering
                fSteering = Math.abs(fPitch);
                // Send command only after a threshold
                if(fPitch < 0) {
                    fSteeringLEFT = fSteering;
                    fSteeringRIGHT = 0;
                    if(fSteeringLEFT > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveWheelLEFT(true);
                    }
                    else
                    {
                        m_Message.setDriveWheelLEFT(false);
                    }
                    m_Message.setSteeringLEFT(floatTobyte(fSteeringLEFT));
                }
                if(fPitch > 0) {
                    fSteeringLEFT = 0;
                    fSteeringRIGHT = fSteering;
                    if(fSteeringRIGHT > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveWheelRIGHT(true);
                    }
                    else
                    {
                        m_Message.setDriveWheelRIGHT(false);
                    }
                    m_Message.setSteeringRIGHT(floatTobyte(fSteeringRIGHT));
                }

                // Send Command
                if(m_Message.isCommandActionChanged()) {
                    m_Message.setRequest();
                    m_Message.setCommandAsToSend();
                }

                m_drive_text_tv_value_up.setText(getString(R.string.drive_text_tv_throttle_fwd) + "-" + String.valueOf(floatToshort(fThrottleFWD)));
                m_drive_text_tv_value_down.setText(getString(R.string.drive_text_tv_throttle_rev) + "-" + String.valueOf(floatToshort(fThrottleREV)));
                m_drive_text_tv_value_left.setText(getString(R.string.drive_text_tv_steering_left) + "-" + String.valueOf(floatToshort(fSteeringLEFT)));
                m_drive_text_tv_value_right.setText(getString(R.string.drive_text_tv_steering_right) + "-" + String.valueOf(floatToshort(fSteeringRIGHT)));

            } else {
                if(!m_bDriveWheelStartStopStatus_FP_Stop) {
                    m_bDriveWheelStartStopStatus_FP_Stop = true;

                    m_Message.setDriveWheelFWD(false);
                    m_Message.setDriveWheelREV(false);
                    m_Message.setDriveWheelLEFT(false);
                    m_Message.setDriveWheelRIGHT(false);

                    // Send Command
                    if(m_Message.isCommandActionChanged()) {
                        m_Message.setRequest();
                        m_Message.setCommandAsToSend();
                    }

                    m_drive_text_tv_value_up.setText(getString(R.string.drive_text_tv_throttle_fwd) + "-" + String.valueOf(floatToshort(fThrottleFWD)));
                    m_drive_text_tv_value_down.setText(getString(R.string.drive_text_tv_throttle_rev) + "-" + String.valueOf(floatToshort(fThrottleREV)));
                    m_drive_text_tv_value_left.setText(getString(R.string.drive_text_tv_steering_left) + "-" + String.valueOf(floatToshort(fSteeringLEFT)));
                    m_drive_text_tv_value_right.setText(getString(R.string.drive_text_tv_steering_right) + "-" + String.valueOf(floatToshort(fSteeringRIGHT)));
                }
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
                m_bDriveForkStartStopStatus_FP_Stop = false;

                // Fork Up and Down
                fForkSpeedUpDown = Math.abs(fAzim);
                if(fAzim > 0) {
                    fForkUP = fForkSpeedUpDown;
                    fForkDOWN = 0;
                    if(fForkUP > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveForkUp(true);
                    }
                    else
                    {
                        m_Message.setDriveForkUp(false);
                    }
                    m_Message.setDriveSpeedForkUP(floatTobyte(fForkUP));
                }
                if(fAzim < 0) {
                    fForkUP = 0;
                    fForkDOWN = fForkSpeedUpDown;
                    if(fForkDOWN > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveForkDown(true);
                    }
                    else
                    {
                        m_Message.setDriveForkDown(false);
                    }
                    m_Message.setDriveSpeedForkDOWN(floatTobyte(fForkDOWN));
                }

                // Fork Open and Close
                fForkSpeedOpenClose = Math.abs(fPitch);
                // Send command only after a threshold
                if(fPitch < 0) {
                    fForkOPEN = fForkSpeedOpenClose;
                    fForkCLOSE = 0;
                    if(fForkOPEN > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveForkOpen(true);
                    }
                    else
                    {
                        m_Message.setDriveForkOpen(false);
                    }
                    m_Message.setDriveSpeedForkOPEN(floatTobyte(fForkOPEN));
                }
                if(fPitch > 0) {
                    fForkOPEN = 0;
                    fForkCLOSE = fForkSpeedOpenClose;
                    if(fForkCLOSE > m_fSensorMinValueStartOutput)
                    {
                        m_Message.setDriveForkClose(true);
                    }
                    else
                    {
                        m_Message.setDriveForkClose(false);
                    }
                    m_Message.setDriveSpeedForkCLOSE(floatTobyte(fForkCLOSE));
                }

                // Send Command
                if(m_Message.isCommandActionChanged()) {
                    m_Message.setRequest();
                    m_Message.setCommandAsToSend();
                }

                m_drive_text_tv_value_up.setText(getString(R.string.drive_text_tv_fork_up) + "-" + String.valueOf(floatToshort(fForkUP)));
                m_drive_text_tv_value_down.setText(getString(R.string.drive_text_tv_fork_down) + "-" + String.valueOf(floatToshort(fForkDOWN)));
                m_drive_text_tv_value_left.setText(getString(R.string.drive_text_tv_fork_open) + "-" + String.valueOf(floatToshort(fForkOPEN)));
                m_drive_text_tv_value_right.setText(getString(R.string.drive_text_tv_fork_close) + "-" + String.valueOf(floatToshort(fForkCLOSE)));

            } else {
                if(!m_bDriveForkStartStopStatus_FP_Stop) {
                    m_bDriveForkStartStopStatus_FP_Stop = true;

                    m_Message.setDriveForkUp(false);
                    m_Message.setDriveForkDown(false);
                    m_Message.setDriveForkOpen(false);
                    m_Message.setDriveForkClose(false);

                    // Send Command
                    if(m_Message.isCommandActionChanged()) {
                        m_Message.setRequest();
                        m_Message.setCommandAsToSend();
                    }

                    m_drive_text_tv_value_up.setText(getString(R.string.drive_text_tv_fork_up) + "-" + String.valueOf(floatToshort(fForkUP)));
                    m_drive_text_tv_value_down.setText(getString(R.string.drive_text_tv_fork_down) + "-" + String.valueOf(floatToshort(fForkDOWN)));
                    m_drive_text_tv_value_left.setText(getString(R.string.drive_text_tv_fork_open) + "-" + String.valueOf(floatToshort(fForkOPEN)));
                    m_drive_text_tv_value_right.setText(getString(R.string.drive_text_tv_fork_close) + "-" + String.valueOf(floatToshort(fForkCLOSE)));
                }
            }

        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class CBFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

        private ToggleButton m_toggleButton_byte_1_1;
        private ToggleButton m_toggleButton_byte_1_2;
        private ToggleButton m_toggleButton_byte_1_3;
        private ToggleButton m_toggleButton_byte_1_4;
        private ToggleButton m_toggleButton_byte_1_5;
        private ToggleButton m_toggleButton_byte_1_6;
        private ToggleButton m_toggleButton_byte_1_7;
        private ToggleButton m_toggleButton_byte_1_8;

        private ToggleButton m_toggleButton_byte_2_1;
        private ToggleButton m_toggleButton_byte_2_2;
        private ToggleButton m_toggleButton_byte_2_3;
        private ToggleButton m_toggleButton_byte_2_4;
        private ToggleButton m_toggleButton_byte_2_5;
        private ToggleButton m_toggleButton_byte_2_6;
        private ToggleButton m_toggleButton_byte_2_7;
        private ToggleButton m_toggleButton_byte_2_8;

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

        private TextView m_drive_id_tv_byte_3;
        private TextView m_drive_id_tv_byte_4;
        private TextView m_drive_id_tv_byte_5;
        private TextView m_drive_id_tv_byte_6;
        private TextView m_drive_id_tv_byte_7;
        private TextView m_drive_id_tv_byte_8;
        private TextView m_drive_id_tv_byte_9;
        private TextView m_drive_id_tv_byte_10;
        private TextView m_drive_id_tv_byte_11;
        private TextView m_drive_id_tv_byte_12;
        private TextView m_drive_id_tv_byte_13;
        private TextView m_drive_id_tv_byte_14;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static CBFragment newInstance(int sectionNumber) {
            CBFragment fragment = new CBFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public CBFragment() {
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

            m_toggleButton_byte_2_1 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_1);
            m_toggleButton_byte_2_2 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_2);
            m_toggleButton_byte_2_3 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_3);
            m_toggleButton_byte_2_4 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_4);
            m_toggleButton_byte_2_5 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_5);
            m_toggleButton_byte_2_6 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_6);
            m_toggleButton_byte_2_7 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_7);
            m_toggleButton_byte_2_8 = (ToggleButton) getActivity().findViewById(R.id.toggleButton_byte_2_8);

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

            m_drive_id_tv_byte_3 = (TextView) getActivity().findViewById(R.id.drive_id_tv_byte_3);
            m_drive_id_tv_byte_4 = (TextView) getActivity().findViewById(R.id.drive_id_tv_byte_4);
            m_drive_id_tv_byte_5 = (TextView) getActivity().findViewById(R.id.drive_id_tv_byte_5);
            m_drive_id_tv_byte_6 = (TextView) getActivity().findViewById(R.id.drive_id_tv_byte_6);
            m_drive_id_tv_byte_7 = (TextView) getActivity().findViewById(R.id.drive_id_tv_byte_7);
            m_drive_id_tv_byte_8 = (TextView) getActivity().findViewById(R.id.drive_id_tv_byte_8);
            m_drive_id_tv_byte_9 = (TextView) getActivity().findViewById(R.id.drive_id_tv_byte_9);
            m_drive_id_tv_byte_10 = (TextView) getActivity().findViewById(R.id.drive_id_tv_byte_10);
            m_drive_id_tv_byte_11 = (TextView) getActivity().findViewById(R.id.drive_id_tv_byte_11);
            m_drive_id_tv_byte_12 = (TextView) getActivity().findViewById(R.id.drive_id_tv_byte_12);
            m_drive_id_tv_byte_13 = (TextView) getActivity().findViewById(R.id.drive_id_tv_byte_13);
            m_drive_id_tv_byte_14 = (TextView) getActivity().findViewById(R.id.drive_id_tv_byte_14);

            // Prelevo i dati dei sensori
            String strSetSensorMaxOutputValue = SQLContract.Settings.getParameter(getActivity().getApplicationContext(), SQLContract.Parameter.SETT_SENSOR_MAX_OUTPUT_VALUE);
            int iSetSensorMaxOutputValue = 0;
            try{
                iSetSensorMaxOutputValue = Integer.parseInt(strSetSensorMaxOutputValue);
            } catch (Exception ignored) {
            }

            m_seekBar_byte_3.setMax(iSetSensorMaxOutputValue);
            m_seekBar_byte_3.setOnSeekBarChangeListener(this);
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
            View rootView = inflater.inflate(R.layout.cb_drive_fragment, container, false);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
/*
            // Registro Listeners
            if (m_acs != null) {
                m_acs.setOnNewAlarmListener(new NewAlarm()
                {
                    @Override
                    public void onNewAlarm(ArrayList<String> alAlarm)
                    {
                        if(alAlarm != null)
                        {
                        }
                    }
                });
            }
            */
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            switch (seekBar.getId())
            {
                case R.id.seekBar_byte_3 :
                    m_drive_id_tv_byte_3.setText("- " + Integer.toString(progress) + " -");
                    m_Message.setActionByte((byte)progress,3);
                    break;
                case R.id.seekBar_byte_4 :
                    m_drive_id_tv_byte_3.setText("- " + Integer.toString(progress) + " -");
                    m_Message.setActionByte((byte)progress,3);
                    break;
                case R.id.seekBar_byte_5 :
                    m_drive_id_tv_byte_3.setText("- " + Integer.toString(progress) + " -");
                    m_Message.setActionByte((byte)progress,3);
                    break;
                case R.id.seekBar_byte_6 :
                    m_drive_id_tv_byte_3.setText("- " + Integer.toString(progress) + " -");
                    m_Message.setActionByte((byte)progress,3);
                    break;
                case R.id.seekBar_byte_7 :
                    m_drive_id_tv_byte_3.setText("- " + Integer.toString(progress) + " -");
                    m_Message.setActionByte((byte)progress,3);
                    break;
                case R.id.seekBar_byte_8 :
                    m_drive_id_tv_byte_3.setText("- " + Integer.toString(progress) + " -");
                    m_Message.setActionByte((byte)progress,3);
                    break;
                case R.id.seekBar_byte_9 :
                    m_drive_id_tv_byte_3.setText("- " + Integer.toString(progress) + " -");
                    m_Message.setActionByte((byte)progress,3);
                    break;
                case R.id.seekBar_byte_10 :
                    m_drive_id_tv_byte_3.setText("- " + Integer.toString(progress) + " -");
                    m_Message.setActionByte((byte)progress,3);
                    break;
                case R.id.seekBar_byte_11 :
                    m_drive_id_tv_byte_3.setText("- " + Integer.toString(progress) + " -");
                    m_Message.setActionByte((byte)progress,3);
                    break;
                case R.id.seekBar_byte_12 :
                    m_drive_id_tv_byte_3.setText("- " + Integer.toString(progress) + " -");
                    m_Message.setActionByte((byte)progress,3);
                    break;
                case R.id.seekBar_byte_13 :
                    m_drive_id_tv_byte_3.setText("- " + Integer.toString(progress) + " -");
                    m_Message.setActionByte((byte)progress,3);
                    break;
                case R.id.seekBar_byte_14 :
                    m_drive_id_tv_byte_3.setText("- " + Integer.toString(progress) + " -");
                    m_Message.setActionByte((byte)progress,3);
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private class CommunicationTask extends AsyncTask<Object, String, Void> {
        private CommunicationStatus onNewCommunicationStatusListener = null;

        // Imposto il listener
        public synchronized void setCommunicationStatusListener(CommunicationStatus listener) {
            onNewCommunicationStatusListener = listener;
        }

        // Funzione richiamata ogni volta che ci sono dei dati da aggiornare
        private void onUpdate(String[] strStatus) {
            // Check if the Listener was set, otherwise we'll get an Exception when we try to call it
            if(onNewCommunicationStatusListener!=null) {
                // Only trigger the event, when we have a username
                onNewCommunicationStatusListener.onNewCommunicationStatus(strStatus);
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
                        if(acs.sendData(msg) == true) {
                            lTime_1 = acs.getGetSendAnswerTimeMilliseconds();

                            if(acs.getData(msg) == true) {
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

        String str = Integer.toBinaryString(byteRes);

        return byteRes;
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
