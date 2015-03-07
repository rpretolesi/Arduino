package SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by RPRETOLESI on 28/01/2015.
 */
public class SQLContract
{
    private static ReentrantLock m_LockCommandHolder = new ReentrantLock();;

    public static final String DATABASE_NAME = "SymptomManagement.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TEXT_TYPE = " TEXT";
    public static final String INT_TYPE = " INT";
    public static final String IMAGE_TYPE = " BLOB";
    public static final String COMMA_SEP = ",";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private SQLContract()
    {
    }
    public enum Parameter
    {
        SCHEDULED_REMINDER_FREQUENCY(0, "0"),
        SCHEDULED_UPDATE_FREQUENCY(1, "0"),
        IP_ADDRESS(2, "192.168.1.1"),
        PORT(3, "502"),
        TIMEOUT(4, "3"),
        COMM_FRAME_DELAY(5, "100"),
        SETT_SENSOR_FEEDBACK_AMPL_K(10, "500.0"),
        SETT_SENSOR_LOW_PASS_FILTER_K(11, "0.5"),
        SETT_SENSOR_MAX_OUTPUT_VALUE(12, "250"),
        SETT_SENSOR_MIN_VALUE_START_OUTPUT(13, "10");

        private int value;
        private String defaultValue;

        private Parameter(int value, String defaultValue) {
            this.value = value;
            this.defaultValue = defaultValue;
        }

        public int getValue() {
            return value;
        }

        public String getDefaultValue() {
            return defaultValue;
        }
    }
    /*
     * Parametri dell'app
    */
    public static abstract class Settings implements BaseColumns
    {
        public static final String TABLE_NAME = "Settings";
        public static final String COLUMN_NAME_PARAMETER_ID = "Parameter_ID";
        public static final String COLUMN_NAME_PARAMETER_VALUE = "Parameter_Value";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME +
                        " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_PARAMETER_ID + INT_TYPE + COMMA_SEP +
                        COLUMN_NAME_PARAMETER_VALUE + TEXT_TYPE +
                        " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static boolean setParameter(Context context, Parameter pType, String strpValue)
        {
            m_LockCommandHolder.lock();

            ContentValues values = null;
            try
            {
                if (context != null && pType != null && strpValue != null)
                {
                    SQLiteDatabase db = SQLHelper.getInstance(context).getDB();

                    values = new ContentValues();
                    values.put(COLUMN_NAME_PARAMETER_ID, pType.getValue());
                    values.put(COLUMN_NAME_PARAMETER_VALUE, strpValue);

                    String selection = COLUMN_NAME_PARAMETER_ID + " = ?";

                    String[] selectionArgs = {String.valueOf(pType.getValue())};

                    // Update the Parameter
                    if (db.update(TABLE_NAME, values, selection, selectionArgs) == 0)
                    {
                        // The Parameter doesn't exist, i will add it
                        if (db.insert(TABLE_NAME, null, values) > 0)
                        {
                            return true;
                        }
                    } else
                    {
                        return true;
                    }
                }
            }
            catch (Exception ex)
            {

            }
            finally
            {
                if(values != null)
                {
                    values.clear();
                }

                m_LockCommandHolder.unlock();
            }

            return false;
        }

        public static String getParameter(Context context, Parameter pType)
        {
            m_LockCommandHolder.lock();

            Cursor cursor = null;
            String strRes = "";
            try
            {
                if(context != null && pType != null)
                {
                    SQLiteDatabase db = SQLHelper.getInstance(context).getDB();

                    // Define a projection that specifies which columns from the database
                    // you will actually use after this query.
                    String[] projection =
                            {
                                    COLUMN_NAME_PARAMETER_VALUE
                            };

                    String selection = COLUMN_NAME_PARAMETER_ID + " = ?";

                    String[] selectionArgs = { String.valueOf(pType.getValue())  };

                    String strDefaultValue = pType.getDefaultValue();

                    // How you want the results sorted in the resulting Cursor
                    String sortOrder = "";

                    cursor = db.query(
                            TABLE_NAME,  // The table to query
                            projection,                               // The columns to return
                            selection,                                // The columns for the WHERE clause
                            selectionArgs,                            // The values for the WHERE clause
                            null,                                     // don't group the rows
                            null,                                     // don't filter by row groups
                            sortOrder                                 // The sort order
                    );

                    if((cursor != null) && (cursor.getCount() > 0))
                    {
                        cursor.moveToFirst();
                        strRes = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PARAMETER_VALUE));
                    }
                    else
                    {
                        strRes = strDefaultValue;
                    }

                }
            }
            catch (Exception ex)
            {

            }
            finally
            {
                if(cursor != null)
                {
                    cursor.close();
                }

                m_LockCommandHolder.unlock();
            }

            return strRes;
        }
    }
}
