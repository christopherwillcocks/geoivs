package ch.supsi.ist.camre.paths;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.legacy.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
//import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

// import com.loopj.android.http.PersistentCookieStore;

import org.json.JSONObject;

import java.util.Objects;

import ch.supsi.ist.camre.paths.data.Path;
import ch.supsi.ist.camre.paths.utils.Requests;


public class HomeActivity extends Activity
        implements Login.LoginHandler,
        PathListFragment.OnPathSelectedListener{

    public static final String DBNAME = "camrechpaths";
    public static final String PREFS = "camrech";
    public static final String PREFS_USER = "user";

    // private PersistentCookieStore myCookieStore;
    private ProgressDialog progress;

    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private DrawerLayout drawerLayout;
    private ListView drawerList;

    Fragment activeFragment;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_home);

        mTitle = mDrawerTitle = getTitle();

        String[] navigationDrawerTitles = getResources().getStringArray(R.array.navigation_drawer_array);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerList = findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        drawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, navigationDrawerTitles));

        // Set the list's click listener
        //drawerList.setOnItemClickListener(new DrawerItemClickListener());
        drawerList.setOnItemClickListener((parent, view, pos, id) -> {
            drawerLayout.addDrawerListener(
                    new DrawerLayout.SimpleDrawerListener()
                    {
                        @Override
                        public void onDrawerClosed(View drawerView)
                        {
                            selectItem(pos);
                        }
                    });
            drawerLayout.closeDrawer(drawerList);
        });

         mDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Objects.requireNonNull(getActionBar()).setTitle(mTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Objects.requireNonNull(getActionBar()).setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.addDrawerListener(mDrawerToggle);

        Objects.requireNonNull(getActionBar()).setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }


    @Override
    protected void onResume() {
        super.onResume();


        SharedPreferences settings = getSharedPreferences(PREFS, 0);

        FragmentManager fragmentManager = getFragmentManager();



        if(settings.contains(PREFS_USER)){

            System.out.println(settings.getString(PREFS_USER,""));

            activeFragment = new PathListFragment();

            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, activeFragment)
                    .commit();

        }else{
            System.out.println("Setting without USER preferences..");
            activeFragment = new Login();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame,activeFragment )
                    .commit();
        }


    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {

        switch (position){
            case 0:
                activeFragment = new TestFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, activeFragment)
                        .commit();
                break;
            case 1:
                activeFragment = new PathListFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, activeFragment)
                        .commit();
                break;
            case 2:
                Intent intent = new Intent(this, WalkerActivityTest.class);
                intent.putExtra("Path", new Path());
                startActivity(intent);
                break;
            case 4:
                activeFragment = new TestFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, activeFragment)
                        .commit();
                break;
            case 5:

                progress = new ProgressDialog(this);
                progress.setTitle("Logging out");
                progress.setMessage("Waiting for server response");
                progress.setCancelable(true);
                progress.show();
                Requests requests = new Requests(getApplicationContext());
                requests.addHandler(json -> {
                    SharedPreferences settings = getSharedPreferences(PREFS, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.remove(PREFS_USER);
                    editor.apply();
                    activeFragment = new Login();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, activeFragment)
                            .commit();
                    progress.dismiss();
                });
                requests.executeGet("logout");
                break;
            default:
                activeFragment = new TestFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, activeFragment)
                        .commit();
                break;
        }

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        Objects.requireNonNull(getActionBar()).setTitle("Cam.Re - " + mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPathSelected(Path path) {

        Intent intent = new Intent(this, WalkerActivityTest.class);
        intent.putExtra("Path", path);
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        bundle.putSerializable("Path",path);
        startActivity(intent);

    }

    @Override
    public void onSuccess(JSONObject json) {
        SharedPreferences settings = getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_USER, json.toString());
        editor.apply();
        FragmentManager fragmentManager = getFragmentManager();
        activeFragment = new PathListFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, activeFragment)
                .commit();
    }

    @Override
    public void onFailure(JSONObject json) {

    }

    @Override
    public void onSkip() {
        FragmentManager fragmentManager = getFragmentManager();
        activeFragment = new PathListFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, activeFragment)
                .commit();
    }

}
