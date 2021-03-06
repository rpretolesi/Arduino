package SQL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by RPRETOLESI on 28/01/2015.
 */
public class SQLHelper extends SQLiteOpenHelper {
    // Member for Singletone
    private final Context m_Context;
    private static SQLHelper m_Instance;
    private static SQLiteDatabase m_Db;

    /**
     * Constructor takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     *
     * @param context
     *            the application context
     */
    private SQLHelper(Context context)
    {
        super(context, SQLContract.DATABASE_NAME, null, SQLContract.DATABASE_VERSION);
        this.m_Context = context;
    }

    /**
     * Get default instance of the class to keep it a singleton
     *
     * @param context
     *            the application context
     */
    public static SQLHelper getInstance(Context context)
    {
        if (m_Instance == null)
        {
            m_Instance = new SQLHelper(context);
        }
        return m_Instance;
    }

    /**
     * Returns a writable database instance in order not to open and close many
     * SQLiteDatabase objects simultaneously
     *
     * @return a writable instance to SQLiteDatabase
     */
    public SQLiteDatabase getDB()
    {
        if ((m_Db == null) || (!m_Db.isOpen()))
        {
            m_Db = this.getWritableDatabase();
        }

        return m_Db;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQLContract.Settings.SQL_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(SQLContract.Settings.SQL_DELETE_ENTRIES);

        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void close()
    {
        super.close();
        if (m_Db != null)
        {
            m_Db.close();
            m_Db = null;
        }
    }
}
