package ch.supsi.ist.camre.paths;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.google.android.gms.location.LocationListener;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import ch.supsi.ist.camre.paths.data.DataChangedListener;
import ch.supsi.ist.camre.paths.data.Element;
import ch.supsi.ist.camre.paths.data.Note;
import ch.supsi.ist.camre.paths.data.Path;
import ch.supsi.ist.camre.paths.data.Point;
import ch.supsi.ist.camre.paths.data.Position;
import ch.supsi.ist.camre.paths.utils.VIEW;

public class WalkerActivityTest extends Activity implements DataChangedListener{

    ProgressDialog progress;


    static final int REQUEST_ELEMENT_IMAGE_CAPTURE = 1;
    static final int REQUEST_ELEMENT_VIDEO_CAPTURE = 2;

    private Database database;

    private Path path;

    WalkerActivityFooter locationWorker;

    // Geo-Location configuration

    private Menu menu;

    private Position lastPosition;

    private ActionBar actionBar;
    private int activeTab = 0;

    /*
    public enum VIEW {
        PATH, SURVEY, ELEMENT, MAP, NOTE_PICTURE_CAPTURE, NOTE
    }
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = ((Application)getApplication()).database;
        setContentView(R.layout.activity_walker_activity_test);
        // Clear action bar title
        setTitle("");


        if(savedInstanceState==null){
            // Adding footer acting as gps worker

            SharedPreferences.Editor editorHarvesting = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editorHarvesting.putBoolean("HARVESTING", false);
            editorHarvesting.commit();

            locationWorker = new WalkerActivityFooter();
            getFragmentManager().beginTransaction()
                    .replace(R.id.walkerPanelFooter,
                            locationWorker, VIEW.LOCATION_WORKER.toString())
                    .commit();

            // Extracting serialized Path
            Intent intent = getIntent();

            System.out.println("Intent: " + intent.getAction());

            this.path = (Path)intent.getSerializableExtra("Path");

            System.out.println("Path: " + this.path.getName());

            Bundle bundle = new Bundle();
            bundle.putSerializable("Path", this.path);
            // Opening fragment PathForm
            openFragment(VIEW.PATH, bundle);
        }else{
            this.path = (Path)savedInstanceState.getSerializable("Path");
            activeTab = savedInstanceState.getInt("activeTab");
            locationWorker = (WalkerActivityFooter)getFragmentManager().findFragmentByTag(VIEW.LOCATION_WORKER.toString());
        }
        if(this.path!=null) {
            System.out.println("Path: " + this.path.getName());
        }
        this.path.addListener(this);
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        System.out.println("WalkerActivityTest: onSaveInstanceState");
        System.out.println("  - name: " + path.getName());
        path.resetListeners();
        outState.putSerializable("Path", this.path);
        outState.putInt("activeTab", this.activeTab);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        System.out.println("WalkerActivityTest: onRestoreInstanceState");
        if(savedInstanceState.containsKey("Path")) {
            this.path = (Path) savedInstanceState.getSerializable("Path");
            this.path.addListener(this);
            System.out.println("   - Path restored: " + this.path.getName());
        }else{
            System.out.println("   - Path not present.");
        }
    }

    protected void onResume (){
        super.onResume();
        initTabs();
        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(this);
        if(settings.getBoolean(SettingsActivity.GPS_ENABLED,
                SettingsActivity.GPS_ONLY_DEFAULT)){
            locationWorker.connectGPS();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        System.out.println("WalkerActivityTest: Destroyed!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return true;
    }

    private void initTabs(){

        // Create a tab listener that is called when the user changes tabs.

        if(actionBar.getTabCount()==0) {

            ActionBar.TabListener tabListener = new ActionBar.TabListener() {
                public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                    Bundle bundle;

                    System.out.println("<<<<<<<<<<<<<<<<< activeTab: " + tab.getPosition());

                    if(activeTab == tab.getPosition()){
                        return;
                    }else if (activeTab==0 || (path.isClosed() && tab.getPosition() == 2) ||  activeTab==3){
                        System.out.println("Saving..");
                        path.notifyChanges();
                        System.out.println("Saved!");
                    }

                    if(tab.getPosition() == 0){

                        bundle = new Bundle();
                        bundle.putSerializable("Path", path);
                        Fragment pathForm = getFragmentManager()
                                .findFragmentByTag(VIEW.PATH.toString());
                        if (pathForm == null) {
                            pathForm = new PathFormTest();
                            if (bundle != null) {
                                pathForm.setArguments(bundle);
                            }
                        } else {
                            pathForm.getArguments().putSerializable("Path",
                                    bundle.getSerializable("Path"));
                        }
                        ft.replace(R.id.walkerPanelTest, pathForm, VIEW.PATH.toString());

                    }else if (!path.isClosed() && tab.getPosition() == 1){

                        bundle = new Bundle();
                        bundle.putSerializable("Path", path);
                        Fragment surveyForm = getFragmentManager()
                                .findFragmentByTag(VIEW.SURVEY.toString());
                        if (surveyForm == null) {
                            surveyForm = new SurveyForm();
                            if (bundle != null) {
                                surveyForm.setArguments(bundle);
                            }
                        } else {
                            surveyForm.getArguments().putSerializable("Path",
                                    bundle.getSerializable("Path"));
                        }
                        ft.replace(R.id.walkerPanelTest, surveyForm, VIEW.SURVEY.toString());

                    }else if ( (path.isClosed() && tab.getPosition() == 1) || (!path.isClosed() && tab.getPosition() == 2)){

                        bundle = new Bundle();
                        bundle.putSerializable("Path", path);
                        Fragment pathViewer = getFragmentManager()
                                .findFragmentByTag(VIEW.PATH_VIEWER.toString());
                        if (pathViewer == null) {
                            pathViewer = new PathViewer();
                            if (bundle != null) {
                                pathViewer.setArguments(bundle);
                            }
                        } else {
                            pathViewer.getArguments().putSerializable("Path",
                                    bundle.getSerializable("Path"));
                        }
                        ft.replace(R.id.walkerPanelTest, pathViewer, VIEW.PATH_VIEWER.toString());

                    }else if ( (path.isClosed() && tab.getPosition() == 2) || (!path.isClosed() && tab.getPosition() == 3)){

                        if(path.getGeometry().getCoordinates().size()>0){

                            ArrayList<Double> coords = path.getGeometry().getLastPoint();

                            TimeZone tz = TimeZone.getTimeZone("UTC");
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            df.setTimeZone(tz);

                            Position pos = new Position();
                            pos.setAltitude(0.0);
                            pos.setHeading(0.0);
                            pos.setGeometry(new Point(coords.get(0), coords.get(1)));
                            pos.setTimestamp(df.format(new Date()));

                            Bundle bundle2 = new Bundle();
                            bundle2.putSerializable("Path", path);
                            bundle2.putBoolean("geolocation", true);
                            bundle2.putBoolean("collect", true);

                            Fragment pathMap = getFragmentManager()
                                    .findFragmentByTag("MAIN_" + VIEW.PATH_MAP.toString());
                            if (pathMap == null) {
                                pathMap = new MainMapFragment();
                                if (bundle2 != null) {
                                    pathMap.setArguments(bundle2);
                                }
                            } else {
                                pathMap.getArguments().putSerializable("Path",
                                        bundle2.getSerializable("Path"));
                            }

                            getFragmentManager().beginTransaction()
                                    .replace(R.id.walkerPanelTest, pathMap, "MAIN_" + VIEW.PATH_MAP.toString())
                                    .addToBackStack(null)
                                    .commit();

                        }else {

                            progress = new ProgressDialog(WalkerActivityTest.this);
                            progress.setTitle("Waiting GPS");
                            progress.setMessage("Wait for position..");
                            progress.setCancelable(true);
                            progress.show();

                            locationWorker.addFusionLocationListener(new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {


                                    System.out.println("WalkerActivity: onLocationChanged");
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("Path", path);
                                    bundle.putSerializable("center", getPosition(location));
                                    bundle.putBoolean("geolocation", true);
                                    bundle.putBoolean("collect", true);
                                    bundle.putInt("zoom", 17);

                                    Fragment pathMap = getFragmentManager()
                                            .findFragmentByTag("MAIN_" + VIEW.PATH_MAP.toString());
                                    if (pathMap == null) {
                                        pathMap = new MainMapFragment();
                                        if (bundle != null) {
                                            pathMap.setArguments(bundle);
                                        }
                                    } else {
                                        pathMap.getArguments().putSerializable("Path",
                                                bundle.getSerializable("Path"));
                                    }

                                    getFragmentManager().beginTransaction()
                                            .replace(R.id.walkerPanelTest, pathMap, "MAIN_" + VIEW.PATH_MAP.toString())
                                            .addToBackStack(null)
                                            .commit();

                                    //openFragment(VIEW.PATH_MAP, bundle);
                                    locationWorker.removeFusionLocationListener(this);
                                    progress.dismiss();
                                }
                            });
                        }
                    }
                    activeTab = tab.getPosition();
                }

                public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                    // hide the given tab
                }

                public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                    // probably ignore this event
                }

            };

            ActionBar.Tab tab = actionBar.newTab()
                    .setText(getText(R.string.tab_info))
                    .setTabListener(tabListener);
            actionBar.addTab(tab, false);

            if(!path.isClosed()){
                tab = actionBar.newTab()
                        .setText(getText(R.string.tab_survey))
                        .setTabListener(tabListener);
                actionBar.addTab(tab, false);
            }

            tab = actionBar.newTab()
                    .setText(getText(R.string.tab_path))
                    .setTabListener(tabListener);
            actionBar.addTab(tab, false);

            tab = actionBar.newTab()
                    .setText(getText(R.string.tab_map))
                    .setTabListener(tabListener);
            actionBar.addTab(tab, false);

            actionBar.setSelectedNavigationItem(activeTab);

            if(this.path==null || this.path.get_id() == null || this.path.get_id().equals("")){
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            }

        }
    }

    public void openFragment(VIEW view, Bundle bundle){

        System.out.println("WalkerActivityTest: openFragment > " + view.toString());

        Fragment fragment = getFragmentManager()
                .findFragmentByTag(view.toString());

        switch (view) {

            case PATH:
                if(fragment==null) {
                    fragment = new PathFormTest();
                    fragment.setArguments(bundle);
                }else{
                    fragment.getArguments().putSerializable("Path",
                            bundle.getSerializable("Path"));
                }
                getFragmentManager().beginTransaction()
                        .replace(R.id.walkerPanelTest, fragment, VIEW.PATH.toString())
                        .commit();
                break;

            case SURVEY:
                if(fragment==null) {
                    fragment = new SurveyForm();
                    fragment.setArguments(bundle);
                }else{
                    fragment.getArguments().putSerializable("Path",
                            bundle.getSerializable("Path"));
                }
                getFragmentManager().beginTransaction()
                        .replace(R.id.walkerPanelTest, fragment, VIEW.SURVEY.toString())
                        .addToBackStack(null)
                        .commit();
                break;

            case ELEMENT:
                if(fragment==null) {
                    fragment = new ElementForm();
                    fragment.setArguments(bundle);
                }else{
                    fragment.getArguments().putSerializable("Element",
                            bundle.getSerializable("Element"));
                }
                getFragmentManager().beginTransaction()
                        .replace(R.id.walkerPanelTest, fragment, VIEW.ELEMENT.toString())
                        .addToBackStack(null)
                        .commit();
                //setTitle("Elemento: " + ((Element)bundle.getSerializable("Element")).getName());
                break;

            case MAP:
                if(fragment==null) {
                    fragment = new ElementMapFragment();
                    fragment.setArguments(bundle);
                }else{
                    fragment.getArguments().putSerializable("Element",
                            bundle.getSerializable("Element"));
                }
                getFragmentManager().beginTransaction()
                        .replace(R.id.walkerPanelTest, fragment, VIEW.MAP.toString())
                        .addToBackStack(null)
                        .commit();
                break;

            case NOTE_MAP:
                if(fragment==null) {
                    fragment = new NoteMapFragment();
                    fragment.setArguments(bundle);
                }else{
                    fragment.getArguments().putSerializable("Note",
                            bundle.getSerializable("Note"));
                }
                getFragmentManager().beginTransaction()
                        .replace(R.id.walkerPanelTest, fragment, VIEW.NOTE_MAP.toString())
                        .addToBackStack(null)
                        .commit();
                break;

            case NOTE:
                if(fragment==null) {
                    fragment = new NoteForm();
                    fragment.setArguments(bundle);
                }else{
                    fragment.getArguments().putSerializable("Note",
                            bundle.getSerializable("Note"));
                }
                getFragmentManager().beginTransaction()
                        .replace(R.id.walkerPanelTest, fragment, VIEW.NOTE.toString())
                        .addToBackStack(null)
                        .commit();
                break;

            case NOTE_PICTURE_CAPTURE:
                System.out.println("Starting activity..");
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity( getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        System.err.println("Ex:" + ex.toString());
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_ELEMENT_IMAGE_CAPTURE);
                    }else{
                        System.out.println("Foto not exists..");
                    }
                }
                break;

            case NOTE_VIDEO_CAPTURE:
                System.out.println("Starting activity..");
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity( getPackageManager()) != null) {
                    File videoFile = null;
                    try {
                        videoFile = createVideoFile();
                    } catch (IOException ex) {
                        System.err.println("Ex:" + ex.toString());
                    }
                    // Continue only if the File was successfully created
                    if (videoFile != null) {
                        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(videoFile));
                        startActivityForResult(takeVideoIntent, REQUEST_ELEMENT_VIDEO_CAPTURE);
                    }else{
                        System.out.println("Video not exists..");
                    }
                }
                break;

            case NOTE_VIDEO_PREVIEW:

                if(fragment==null) {
                    fragment = new VideoPlayer();
                    fragment.setArguments(bundle);
                }else{
                    fragment.getArguments().putSerializable("Note",
                            bundle.getSerializable("Note"));
                }
                getFragmentManager().beginTransaction()
                        .replace(R.id.walkerPanelTest, fragment, VIEW.NOTE_VIDEO_PREVIEW.toString())
                        .addToBackStack(null)
                        .commit();


                break;

            case NOTE_AUDIO_CAPTURE:

                if(fragment==null) {
                    fragment = new DialogAudioRecorder();
                    if(bundle!=null) {
                        fragment.setArguments(bundle);
                    }
                }else{
                    if(bundle!=null) {
                        fragment.getArguments().putSerializable("Note",
                                bundle.getSerializable("Note"));
                    }
                }

                getFragmentManager().beginTransaction()
                        .replace(R.id.walkerPanelTest, fragment, VIEW.NOTE_AUDIO_CAPTURE.toString())
                        .commit();

                /*((DialogFragment)fragment)
                        .show(getFragmentManager(), VIEW.NOTE_AUDIO_CAPTURE.toString());*/

                break;

            case PATH_VIEWER:

                if(fragment==null) {
                    fragment = new PathViewer();
                    if(bundle!=null) {
                        fragment.setArguments(bundle);
                    }
                }else{
                    if(bundle!=null) {
                        fragment.getArguments().putSerializable("Note",
                                bundle.getSerializable("Note"));
                    }
                }

                getFragmentManager().beginTransaction()
                        .replace(R.id.walkerPanelTest, fragment, VIEW.PATH_VIEWER.toString())
                        .commit();


                break;


            case PATH_MAP:

                if(fragment==null) {
                    fragment = new MainMapFragment();
                    fragment.setArguments(bundle);
                }else{
                    fragment.getArguments().putSerializable("Path",
                            bundle.getSerializable("Path"));
                }
                getFragmentManager().beginTransaction()
                        .replace(R.id.walkerPanelTest, fragment, VIEW.PATH_MAP.toString())
                        .addToBackStack(null)
                        .commit();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Bundle bundle;

        switch (item.getItemId()) {

            case R.id.walker_settings:

                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.walker_close:

                new AlertDialog.Builder(this)
                        //.setTitle("Select The Difficulty Level")
                        .setMessage(getString(R.string.action_close_path))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Bundle bundle = new Bundle();
                                bundle.putBoolean("new", true);

                                bundle.putString("type", "surface");
                                bundle.putBoolean("closingAction", true);

                                bundle.putSerializable("Element", path.getSurface().get(path.getSurface().size()-1));
                                openFragment(VIEW.ELEMENT, bundle);

                                /*WalkerActivityTest.this.path.setClosed(true);
                                WalkerActivityTest.this.savePath(WalkerActivityTest.this.path);
                                NavUtils.navigateUpFromSameTask(WalkerActivityTest.this);*/
                            }
                        }
                        )
                        .setNegativeButton(android.R.string.no, null)
                        .show();

                return true;

            case R.id.action_done:

                //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

                item.setVisible(false);

                PathFormTest pft = (PathFormTest)getFragmentManager()
                        .findFragmentByTag(VIEW.PATH.toString());

                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                this.activeTab = 1;
                getActionBar().setSelectedNavigationItem(1);

                this.path = pft.getPath();
                this.savePath(this.path);
                bundle = new Bundle();
                bundle.putSerializable("Path", this.path);
                this.activeTab = 1;
                openFragment(VIEW.SURVEY, bundle);
                break;

            case R.id.harvesting_action:

                SharedPreferences settings =
                        PreferenceManager.getDefaultSharedPreferences(this);
                MenuItem harvestingItem = this.menu.findItem(R.id.harvesting_action);
                boolean harvesting = !settings.getBoolean(SettingsActivity.HARVESTING,
                        SettingsActivity.HARVESTING_DEFAULT);
                if(harvesting){
                    harvestingItem.setIcon(R.drawable.ic_action_hiking_pause);
                }else{
                    harvestingItem.setIcon(R.drawable.ic_action_hiking_play);
                }
                SharedPreferences.Editor editorHarvesting = settings.edit();
                editorHarvesting.putBoolean("HARVESTING", harvesting);
                editorHarvesting.commit();

                break;

            case R.id.survey_action_pause:

                System.out.println("WalkerActivityTest: survey_action_pause");
                locationWorker.disconnectGps();
                MenuItem recItem = this.menu.findItem(R.id.survey_action_record);
                recItem.setIcon(R.drawable.ic_action_record_disabled);
                item.setVisible(false);
                SharedPreferences.Editor editorPause =
                        PreferenceManager.getDefaultSharedPreferences(this).edit();
                editorPause.putBoolean(SettingsActivity.GPS_ENABLED, false);
                editorPause.commit();

                break;

            case R.id.survey_action_record:
                System.out.println("WalkerActivityTest: survey_action_play");

                locationWorker.connectGPS();

                item.setIcon(R.drawable.ic_action_record);
                this.menu.findItem(R.id.survey_action_pause)
                        .setVisible(true);


                SharedPreferences.Editor editorRecord =
                        PreferenceManager.getDefaultSharedPreferences(this).edit();
                editorRecord.putBoolean(SettingsActivity.GPS_ENABLED, true);
                editorRecord.commit();

                break;

            case R.id.element_action_save:

                Toast.makeText(this, "Saving element..", Toast.LENGTH_SHORT)
                        .show();

                ElementForm efr = (ElementForm)getFragmentManager()
                        .findFragmentByTag(VIEW.ELEMENT.toString());


                this.path = efr.getElement().getParent();


                // Modifying last point of previews surface if present
                if(efr.etype.equals("surface")) {
                    int index = this.path.getSurface().indexOf(efr.getElement());
                    if(index>0){
                        this.path.getSurface().get(index-1).setEndPoint(efr.getElement().getStartPoint());
                    }
                }

                // If there are some open elements they will be closed using the
                //   end point of this surface
                if(efr.isClosing()){

                    System.out.println("Checking left side:");
                    for (Element e: path.getLeftSide()){
                        if (e.getEndPoint()==null){
                            System.out.println(" > Closing");
                            e.setEndPoint(efr.getElement().getEndPoint());
                        }
                    }
                    System.out.println("Checking right side:");
                    for (Element e: path.getRightSide()){
                        if (e.getEndPoint()==null){
                            System.out.println(" > Closing");
                            e.setEndPoint(efr.getElement().getEndPoint());
                        }
                    }
                    System.out.println("Checking bridge:");
                    for (Element e: path.getBridge()){
                        if (e.getEndPoint()==null){
                            System.out.println(" > Closing");
                            e.setEndPoint(efr.getElement().getEndPoint());
                        }
                    }

                    this.path.setClosed(true);
                    this.savePath(this.path);
                    NavUtils.navigateUpFromSameTask(WalkerActivityTest.this);

                }else{
                    this.savePath(this.path);
                    getFragmentManager().popBackStack();
                }

                break;

            case R.id.element_action_delete:

                final View cb = getLayoutInflater().inflate(R.layout.prompt_checkbox,null);

                new AlertDialog.Builder(this)
                        //.setTitle("Select The Difficulty Level")
                        .setMessage(getString(R.string.action_delete_element))
                        .setView(cb)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CheckBox keep = (CheckBox) cb.findViewById(R.id.prompt_checkbox_media);


                                ElementForm ef = ((ElementForm)getFragmentManager()
                                        .findFragmentByTag(VIEW.ELEMENT.toString()));

                                if (keep.isChecked()) {
                                    System.out.println("Deleting also media files");
                                    for (Note note: ef.getElement().getNotes()){
                                        if(note.getUri()!=null && !note.getUri().isEmpty()) {
                                            try {
                                                File file = new File(note.getUri());
                                                System.out.println(" del " + note.getUri());
                                                if(file.exists()){
                                                    file.delete();
                                                }
                                            } catch (Exception e) {
                                                System.out.println(e.getMessage());
                                            }
                                        }
                                    }
                                }
                                System.out.println("Deleting Element");

                                path = ef.getElement().getParent();

                                path.remove(ef.getElement());

                                savePath(path);

                                getFragmentManager().popBackStack();

                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();

                break;

            /*case R.id.element_action_map:


                bundle = new Bundle();

                Element ele = ((ElementForm)getFragmentManager()
                        .findFragmentByTag(VIEW.ELEMENT.toString())).getElement();

                bundle.putSerializable("Element", ele);
                bundle.putSerializable("center", ele.getStartPoint());
                bundle.putBoolean("geolocation",true);
                bundle.putInt("zoom", 17);
                openFragment(VIEW.MAP, bundle);

                break;

            case R.id.note_action_map:

                bundle = new Bundle();

                Note note_nam = ((NoteForm)getFragmentManager()
                        .findFragmentByTag(VIEW.NOTE.toString())).getNote();

                bundle.putSerializable("Note", note_nam);
                bundle.putSerializable("center", note_nam.getPoint());
                bundle.putBoolean("geolocation",true);
                bundle.putInt("zoom", 17);
                openFragment(VIEW.NOTE_MAP, bundle);

                break;*/

            case R.id.note_action_delete:

                final View ncb = getLayoutInflater().inflate(R.layout.prompt_checkbox,null);

                new AlertDialog.Builder(this)
                        //.setTitle("Select The Difficulty Level")
                        .setMessage(getString(R.string.action_delete_note))
                        .setView(ncb)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CheckBox keep = (CheckBox) ncb.findViewById(R.id.prompt_checkbox_media);

                                NoteForm ef = ((NoteForm)getFragmentManager()
                                        .findFragmentByTag(VIEW.NOTE.toString()));

                                if (keep.isChecked()) {
                                    if(ef.getNote().getUri()!=null && !ef.getNote().getUri().isEmpty()) {
                                        try {
                                            File file = new File(ef.getNote().getUri());
                                            if(file.exists()){
                                                file.delete();
                                            }
                                        } catch (Exception e) {
                                            System.out.println(e.getMessage());
                                        }
                                    }
                                }

                                ef.getNote().getElement().removeNote(ef.getNote());

                                savePath(ef.getNote().getElement().getParent());

                                getFragmentManager().popBackStack();

                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();

                break;

            case R.id.element_action_camera:
                openFragment(VIEW.NOTE_PICTURE_CAPTURE, null);
                break;

            case R.id.element_action_video:
                openFragment(VIEW.NOTE_VIDEO_CAPTURE, null);
                break;


            case R.id.element_action_audio:

                ElementForm ef = (ElementForm) getFragmentManager()
                        .findFragmentByTag(VIEW.ELEMENT.toString());

                Element element = ef.getElement();
                Note note = new Note(
                        DialogAudioRecorder.createFilePath(), getCurrentPosition());
                //element.addNote(note);

                bundle = new Bundle();
                bundle.putSerializable("Note", note);
                bundle.putSerializable("Element", element);
                openFragment(VIEW.NOTE_AUDIO_CAPTURE, bundle);

                break;

            case R.id.note_action_save:
                NoteForm nf = (NoteForm)getFragmentManager()
                        .findFragmentByTag(VIEW.NOTE.toString());
                this.path = nf.getNote().getElement().getParent();
                this.savePath(this.path);
                getFragmentManager().popBackStack();
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        System.out.println("WalkerActivityTest: onActivityResult");

        if(requestCode == REQUEST_ELEMENT_IMAGE_CAPTURE) {

            if (resultCode == RESULT_OK) {

                progress = new ProgressDialog(this);
                progress.setTitle("Waiting GPS");
                progress.setMessage("Wait for position..");
                progress.setCancelable(true);
                progress.show();

                //locationListeners.add(new LocationListener(){
                locationWorker.addFusionLocationListener(new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        /*double lon = location.getLongitude();
                        double lat = location.getLatitude();

                        TimeZone tz = TimeZone.getTimeZone("UTC");
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        df.setTimeZone(tz);

                        Position pos = new Position();
                        pos.setAltitude(location.getAltitude());
                        pos.setHeading(0.0);
                        pos.setGeometry(new Point(lon, lat));
                        pos.setTimestamp(df.format(new Date()));*/

                        ElementForm ef = (ElementForm) getFragmentManager()
                                .findFragmentByTag(VIEW.ELEMENT.toString());

                        Element element = ef.getElement();
                        Note note = new Note(mCurrentPhotoPath, getPosition(location));
                        element.addNote(note);

                        MediaScannerConnection.scanFile(
                                WalkerActivityTest.this, new String[]{mCurrentPhotoPath}, null, null);

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Note", note);

                        openFragment(VIEW.NOTE, bundle);

                        locationWorker.removeFusionLocationListener(this);

                        progress.dismiss();

                        /*locationListeners.remove(this);
                        mLocationClient.removeLocationUpdates(this);*/

                    }
                });

            }

        }
        else if(requestCode == REQUEST_ELEMENT_VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {

                progress = new ProgressDialog(this);
                progress.setTitle("Waiting GPS");
                progress.setMessage("Wait for position..");
                progress.setCancelable(true);
                progress.show();

                //locationListeners.add(new LocationListener(){
                locationWorker.addFusionLocationListener(new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        progress.dismiss();

                        /*double lon = location.getLongitude();
                        double lat = location.getLatitude();

                        TimeZone tz = TimeZone.getTimeZone("UTC");
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        df.setTimeZone(tz);

                        Position pos = new Position();
                        pos.setAltitude(location.getAltitude());
                        pos.setHeading(0.0);
                        pos.setGeometry(new Point(lon, lat));
                        pos.setTimestamp(df.format(new Date()));*/

                        ElementForm ef = (ElementForm) getFragmentManager()
                                .findFragmentByTag(VIEW.ELEMENT.toString());

                        Element element = ef.getElement();
                        Note note = new Note(mCurrentVideoPath, getPosition(location));
                        element.addNote(note);

                        MediaScannerConnection.scanFile(
                                WalkerActivityTest.this, new String[]{mCurrentVideoPath}, null, null);

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Note", note);

                        openFragment(VIEW.NOTE, bundle);

                        locationWorker.removeFusionLocationListener(this);

                    }
                });

            }
        }
    }

    @Override
    public void dataChanged(Serializable serializable) {
        System.out.println("WalkerActivityTest: dataChanged");
        this.path = (Path)serializable;
        this.savePath(this.path);
    }

    private void savePath(Path toSave){
        //if(!path.isClosed()){
            System.out.println("WalkerActivityTest: saving path");
            ObjectMapper om = new ObjectMapper();
            Map<String, Object> map = om.convertValue(toSave, Map.class);
            Document document;
            if (toSave.get_id()!=null){
                document = database.getDocument(toSave.get_id());
                System.out.println("  - Document will be updated");
            }else{
                document = database.createDocument();
                System.out.println("  - Document will be created");
            }
            try {
                document.putProperties(map);
                toSave.set_id(document.getId());
                toSave.set_rev(document.getCurrentRevisionId());
                try {
                    System.out.println(om.writeValueAsString(toSave));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        //}
    }

    @Override
    public void onStart() {
        System.out.println("WalkerActivityTest: onStart");
        super.onStart();
        // Connect the client.
        //mLocationClient.connect();
    }

    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        System.out.println("WalkerActivityTest: onStop");
        //mLocationClient.disconnect();
        super.onStop();
    }

    protected Position getLastPosition(){
        System.out.println("WalkerActivityTest: getLastPosition > " + lastPosition);
        return lastPosition;
    }

    protected Position getCurrentPosition(){
        return lastPosition;
    }

    public Position getPosition(Location location){
        double lon = location.getLongitude();
        double lat = location.getLatitude();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        Position pos = new Position();
        pos.setAltitude(location.getAltitude());
        pos.setHeading(0.0);
        pos.setGeometry(new Point(lon, lat));
        pos.setTimestamp(df.format(new Date()));
        return pos;
    }

    String mCurrentPhotoPath;
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "camre_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    String mCurrentVideoPath;
    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "camre_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentVideoPath = "file:" + image.getAbsolutePath();
        System.out.println("File created: " + mCurrentVideoPath);
        return image;
    }

    @Override
    public void onBackPressed() {
        if(!path.isClosed()){
            int fragments = getFragmentManager().getBackStackEntryCount();
            System.out.println("WalkerActivityTest: fragment #"+fragments);
            if (fragments == 0) {
                System.out.println("Saving: " + path.getName());
                path.notifyChanges();
            }
        }
        super.onBackPressed();
    }

}
