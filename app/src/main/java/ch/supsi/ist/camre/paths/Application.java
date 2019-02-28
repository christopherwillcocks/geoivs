package ch.supsi.ist.camre.paths;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;

/**
 * Created by milan antonovic on 22/08/14.
 */
public class Application extends android.app.Application{
    private Manager manager;
    protected Database database;
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        try {
            /*
             * In Java the Manager instance and all the objects descending
             * from it may be used on any thread.
             */
            manager = new Manager(new AndroidContext(mContext), Manager.DEFAULT_OPTIONS);
            if (!Manager.isValidDatabaseName(HomeActivity.DBNAME)) {
                Log.e(PathForm.class.getName(), "Bad database name");
            }else {
                Log.e(PathForm.class.getName(), "Good database name");
                database = manager.getDatabase(HomeActivity.DBNAME);
            }
        } catch (IOException e) {
            Log.e(Application.class.getName(), "Cannot create Manager instance", e);
            return;
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

}
