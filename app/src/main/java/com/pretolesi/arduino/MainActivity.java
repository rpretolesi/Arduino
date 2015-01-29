package com.pretolesi.arduino;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;

import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
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

    // Dichiaro il Socket per la comunicazione.
    private Socket m_socketClient;

    // Dichiaro la lista dei comandi da inviare
    private BlockingQueue m_bqCommand;

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

        // Verifico se ho dei dati gia' memorizzati nel DB in modo che all'avvio dell'applicazione possa collegarmi.

        if(m_socketClient == null)
        {
            m_socketClient = new Socket();
            // Prendo l'indirizzo remoto dal DB
            String strIpAddress = SQLContract.Settings.getParameter(getApplicationContext(), SQLContract.Parameter.IP_ADDRESS);

            if(strIpAddress != null)
            {
                try {
                    InetAddress iNetIpAddress = InetAddress.getByName(strIpAddress);
                } catch (UnknownHostException ex) {

                } catch (IOException ex) {
                }
            }


            //m_socketClient.connect();
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
                fragment = DriveWheelsFragment.newInstance(position + 1);
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
            View rootView = inflater.inflate(R.layout.main_fragment, container, false);
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
    public static class DriveWheelsFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static DriveWheelsFragment newInstance(int sectionNumber) {
            DriveWheelsFragment fragment = new DriveWheelsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public DriveWheelsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.main_fragment, container, false);
            return rootView;
        }
    }

    private class DownloadFilesTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void...v)
        {
            return v[0];
        }

        protected void onProgressUpdate(Void... v)
        {

        }

        protected void onPostExecute(Void...v)
        {

        }
    }

}
