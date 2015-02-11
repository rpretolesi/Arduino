package com.pretolesi.arduino;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.lang.Math;
import java.util.concurrent.LinkedBlockingQueue;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import SQL.SQLContract;

public class MainActivity extends ActionBarActivity{

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
    private static Command m_Command;
    private static ArduinoClientSocket m_acs;

    // Metto in una lista, i dati da passare alla funzione AsyncTask
    private ArrayList<Object> m_alOParameter;
    private ArrayList<String> m_alstrStatus;

    // Task di comunicazione
    private static CommunicationTask m_CommunicationTask = null;


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

        // Inizializzo tutte le variabili
        if(m_alOParameter == null)
        {
            m_alOParameter = new ArrayList<>(2);
        }

        if(m_alstrStatus == null)
        {
            m_alstrStatus = new ArrayList<>(2);
        }

        // Inizializzo i comandi da inviare
        if(m_Command == null)
        {
            m_Command = new Command();
        }

        // Inizializzo il client di comunicazione
        if(m_acs == null)
        {
            m_acs = new ArduinoClientSocket();
        }

        // Preparo i parametri per passare al thread i dati.
        if(m_alOParameter != null)
        {
            if(m_Command != null) {
                m_alOParameter.add(0, m_Command);
            }
            if(m_acs != null) {
                m_alOParameter.add(1, m_acs);
            }
        }


        // Avvio il Task di comunicazione
        m_CommunicationTask = new CommunicationTask();
        m_CommunicationTask.execute(m_alOParameter, null, null);

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
                fragment = SettingsFragment.newInstance(position + 1);
            }
            if (position == 1)
            {
                fragment = DriveFragment.newInstance(position + 1);
            }
            if (position == 2)
            {
                fragment = AlarmListFragment.newInstance(position + 1);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show xx total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
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
    public static class SettingsFragment extends Fragment {

        private EditText m_settings_id_et_server_ip_address;
        private Button m_settings_id_btn_start_comm;
        private Button m_settings_id_btn_stop_comm;
        private Button m_settings_id_btn_save;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static SettingsFragment newInstance(int sectionNumber) {
            SettingsFragment fragment = new SettingsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public SettingsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.settings_fragment, container, false);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            super.onActivityCreated(savedInstanceState);

            m_settings_id_et_server_ip_address = (EditText) getActivity().findViewById(R.id.settings_id_et_server_ip_address);
            m_settings_id_btn_start_comm = (Button) getActivity().findViewById(R.id.settings_id_btn_start_comm);
            m_settings_id_btn_stop_comm = (Button) getActivity().findViewById(R.id.settings_id_btn_stop_comm);
            m_settings_id_btn_save = (Button) getActivity().findViewById(R.id.settings_id_btn_save);


            // Set an OnClickListener
            m_settings_id_btn_start_comm.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {


                }
            });

            // Set an OnClickListener
            m_settings_id_btn_stop_comm.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                }
            });

            // Set an OnClickListener
            m_settings_id_btn_save.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    boolean bSaveStatus = true;
                    String strIpAddress = m_settings_id_et_server_ip_address.getText().toString();
                    // set a Parameter
                    if(SQLContract.Settings.setParameter(getActivity().getApplicationContext(), SQLContract.Parameter.IP_ADDRESS, String.valueOf(strIpAddress)) == false)
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
        float m_fAzim_thr_FWD = (float)0.0;
        float m_fAzim_thr_REV = (float)0.0;
        float m_fPitch_thr_LEFT = (float)0.0;
        float m_fPitch_thr_RIGHT = (float)0.0;

        private Button m_drive_id_btn_drive_start_stop;
        private boolean m_bStartStopStatus;
        private boolean m_bStartStopStatus_FP_Stop;

        private TextView m_drive_text_tv_throttle_fwd;
        private TextView m_drive_text_tv_throttle_rev;
        private TextView m_drive_text_tv_steering_left;
        private TextView m_drive_text_tv_steering_right;
        private TextView m_drive_id_tv_communication_status;

        private float m_fThrottleTare;
        private float m_fSteeringTare;


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

            m_drive_id_btn_drive_start_stop = (Button) getActivity().findViewById(R.id.drive_id_btn_drive_start_stop);
            m_drive_text_tv_throttle_fwd = (TextView) getActivity().findViewById(R.id.drive_id_tv_throttle_fwd);
            m_drive_text_tv_throttle_rev = (TextView) getActivity().findViewById(R.id.drive_id_tv_throttle_rev);
            m_drive_text_tv_steering_left = (TextView) getActivity().findViewById(R.id.drive_id_tv_steering_left);
            m_drive_text_tv_steering_right = (TextView) getActivity().findViewById(R.id.drive_id_tv_steering_right);
            m_drive_id_tv_communication_status = (TextView) getActivity().findViewById(R.id.drive_id_tv_communication_status);

            // Set an OnClickListener
            m_drive_id_btn_drive_start_stop.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(m_Command != null)
                    {
                        if(m_bStartStopStatus == false)
                        {
                            m_bStartStopStatus = true;
                            m_drive_id_btn_drive_start_stop.setText(R.string.drive_text_btn_drive_stop);

                            // Eseguo la tara dei valori dei sensori
                            if(m_AzimPitchRoll != null)
                            {
                                m_fThrottleTare = (m_AzimPitchRoll[1] * 200);
                                m_fSteeringTare = (m_AzimPitchRoll[2] * 200);
                            }
                        }
                        else
                        {
                            m_bStartStopStatus = false;
                            m_drive_id_btn_drive_start_stop.setText(R.string.drive_text_btn_drive_start);
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
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.drive_fragment, container, false);
            return rootView;
        }

        @Override
        public void onResume()
        {
            super.onResume();

            // Registro Listeners
            if(m_CommunicationTask != null)
            {
                m_CommunicationTask.setCommunicationStatusListener(new CommunicationStatus()
                {
                    @Override
                    public void onNewCommunicationStatus(String strStatus)
                    {
                        // Aggiorno lo stato
                        if(m_drive_id_tv_communication_status != null)
                        {
                            m_drive_id_tv_communication_status.setText(strStatus);
                        }
                    }
                });
            }

            m_SensorManager.registerListener(this, m_Accelerometer, SensorManager.SENSOR_DELAY_UI);
            m_SensorManager.registerListener(this, m_Magnetometer, SensorManager.SENSOR_DELAY_UI);

        }

        @Override
        public void onPause()
        {
            super.onPause();

            if(m_CommunicationTask != null)
            {
                m_CommunicationTask.setCommunicationStatusListener(null);
            }

            m_SensorManager.unregisterListener(this);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {

        }

        @Override
        public void onSensorChanged(SensorEvent event)
        {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                m_faGravity = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                m_faGeomagnetic = event.values;
            }
            if (m_faGravity != null && m_faGeomagnetic != null)
            {
                float faRot[] = new float[9];
                float faIncl[] = new float[9];
                boolean bRes = SensorManager.getRotationMatrix(faRot, faIncl, m_faGravity, m_faGeomagnetic);
                if (bRes ==  true)
                {
                    if(m_AzimPitchRoll == null)
                    {
                        m_AzimPitchRoll = new float[3];
                    }
                    SensorManager.getOrientation(faRot, m_AzimPitchRoll);

                    // Set Command for Drive
                    Drive();
                }

            }
        }

        private void Drive()
        {
            // Aggiorno i miei dati
            if(m_AzimPitchRoll != null)
            {
                // Converto l'acceleratore da 0 a 100 e parto con la posizione attuale
                float fAzim = 0;
                float fThrottle = 0;
                float fThrottleFWD = 0;
                float fThrottleREV = 0;
                float fPitch = 0;
                float fSteering = 0;
                float fSteeringLEFT = 0;
                float fSteeringRIGHT = 0;
                if(m_bStartStopStatus == true)
                {
                    m_bStartStopStatus_FP_Stop = false;

                    // Throttle
                    fAzim = (m_AzimPitchRoll[1] * 200) - m_fThrottleTare;
                    fThrottle = Math.abs(fAzim);
                    if(fThrottle > 100)
                    {
                        fThrottle = 100;
                    }
                    if(fAzim > 0)
                    {
                        fThrottleFWD = fThrottle;
                        fThrottleREV = 0;
                        if(Math.abs(fAzim - m_fAzim_thr_FWD) > 10)
                        {
                            m_fAzim_thr_FWD = fAzim;

                            m_Command.setDriveFWD(true);
                            m_Command.setThrottleFWD(floatTobyte(fThrottleFWD));
                        }
                    }
                    if(fAzim < 0)
                    {
                        fThrottleFWD = 0;
                        fThrottleREV = fThrottle;
                        if (Math.abs(fAzim - m_fAzim_thr_FWD) > 10)
                        {
                            m_fAzim_thr_REV = fAzim;

                            m_Command.setDriveREV(true);
                            m_Command.setThrottleREV(floatTobyte(fThrottleREV));
                        }
                    }

                    // Steering
                    fPitch = (m_AzimPitchRoll[2] * 200) - m_fSteeringTare;
                    fSteering = Math.abs(fPitch);
                    if(fSteering > 100)
                    {
                        fSteering = 100;
                    }
                    // Send command only after a threshold
                    if(fPitch < 0)
                    {
                        fSteeringLEFT = fSteering;
                        fSteeringRIGHT = 0;
                        if(Math.abs(fPitch - m_fPitch_thr_LEFT) > 10)
                        {
                            m_fPitch_thr_LEFT = fPitch;

                            m_Command.setDriveLEFT(true);
                            m_Command.setSteeringLEFT(floatTobyte(fSteeringLEFT));
                        }
                    }
                    if(fPitch > 0)
                    {
                        fSteeringLEFT = 0;
                        fSteeringRIGHT = fSteering;
                        if(Math.abs(fPitch - m_fPitch_thr_RIGHT) > 10)
                        {
                            m_fPitch_thr_RIGHT = fPitch;

                            m_Command.setDriveRIGHT(true);
                            m_Command.setSteeringRIGHT(floatTobyte(fSteeringRIGHT));
                        }
                    }
                }
                else
                {
                    if(m_bStartStopStatus_FP_Stop == false)
                    {
                        m_bStartStopStatus_FP_Stop = true;

                        m_fAzim_thr_FWD = (float)0.0;
                        m_fAzim_thr_REV = (float)0.0;
                        m_fPitch_thr_LEFT = (float)0.0;
                        m_fPitch_thr_RIGHT = (float)0.0;

                        m_Command.setDriveFWD(false);
                        m_Command.setDriveREV(false);
                        m_Command.setDriveLEFT(false);
                        m_Command.setDriveRIGHT(false);
                    }
                 }

                // Send Command
                if(m_Command.isCommandChange() == true)
                {
                    if(m_Command.setCommand() == false)
                    {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.comm_status_queue_full, Toast.LENGTH_LONG).show();
                    }
                }

                m_drive_text_tv_throttle_fwd.setText(getString(R.string.drive_text_tv_throttle_fwd) + "-" + String.valueOf(floatTobyte(fThrottleFWD)));
                m_drive_text_tv_throttle_rev.setText(getString(R.string.drive_text_tv_throttle_rev) + "-" + String.valueOf(floatTobyte(fThrottleREV)));
                m_drive_text_tv_steering_left.setText(getString(R.string.drive_text_tv_steering_left) + "-" + String.valueOf(floatTobyte(fSteeringLEFT)));
                m_drive_text_tv_steering_right.setText(getString(R.string.drive_text_tv_steering_right) + "-" + String.valueOf(floatTobyte(fSteeringRIGHT)));
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class AlarmListFragment extends ListFragment
    {
        private AlarmListAdapter m_adapter;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static AlarmListFragment newInstance(int sectionNumber) {
            AlarmListFragment fragment = new AlarmListFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public AlarmListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        @Override
        public void onActivityCreated (Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            m_adapter = new AlarmListAdapter(getActivity());
            setListAdapter(m_adapter);
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
        public void onDestroyView()
        {
            super.onDestroyView();

            // free adapter
            setListAdapter(null);
        }
    }

    private class CommunicationTask extends AsyncTask<ArrayList<Object>, String, Void>
    {
        private CommunicationStatus onNewCommunicationStatusListener = null;

        // Imposto il listener
        public synchronized void setCommunicationStatusListener(CommunicationStatus listener)
        {
            onNewCommunicationStatusListener = listener;
        }

        // Funzione richiamata ogni volta che ci sono dei dati da aggiornare
        private void onUpdate(String strStatus)
        {
            // Check if the Listener was set, otherwise we'll get an Exception when we try to call it
            if(onNewCommunicationStatusListener!=null) {
                // Only trigger the event, when we have a username
                onNewCommunicationStatusListener.onNewCommunicationStatus(strStatus);
            }
        }

        @Override
        protected Void doInBackground(ArrayList<Object>...obj)
        {
            //Prendo i parametri
            Command cmd = (Command) obj[0].get(0);
            ArduinoClientSocket acs = (ArduinoClientSocket) obj[0].get(1);
            String strStatus = "";

            byte[] byteToRead = new byte[64];

            while (!isCancelled() && cmd != null && acs != null)
            {

                if(acs.isConnected() == false) {
                    // Pubblico i dati
                    strStatus =  getString(R.string.comm_status_connecting);
                    this.publishProgress(strStatus);

                    // Prelevo indirizzo IP
                    String strIpAddress = SQLContract.Settings.getParameter(getApplicationContext(), SQLContract.Parameter.IP_ADDRESS);
                    if(acs.connectToArduino(strIpAddress, 502, 3000) == true)
                    {
                        strStatus = getString(R.string.comm_status_connected);
                    }
                    else
                    {
                        strStatus = getString(R.string.comm_status_error);
                    }
                    this.publishProgress(strStatus);

                }
                else
                {
                    // Pubblico i dati
                    strStatus = getString(R.string.comm_status_online);
                    this.publishProgress(strStatus);
                    acs.sendCommand(cmd);
                }

            }

            // Pubblico i dati
            strStatus = getString(R.string.comm_status_closed);
            this.publishProgress(strStatus);

            return null;
        }


        @Override
        protected void onProgressUpdate(String... strStatus)
        {
            super.onProgressUpdate(strStatus);
            // Aggiorno i dati
            onUpdate(strStatus[0]);
        }
    }


    // Funzioni di supporto
    static byte floatTobyte(float f)
    {
        if(f < 0)
        {
            f = 0;
        }
        if(f > 127)
        {
            f = 127;
        }
        DecimalFormat df = new DecimalFormat("###");
        return Byte.valueOf(df.format(f));
    }
}
