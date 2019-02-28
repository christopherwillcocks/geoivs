package ch.supsi.ist.camre.paths;

import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ch.supsi.ist.camre.paths.data.Path;
import ch.supsi.ist.camre.paths.utils.VIEW;

/**
 * A simple {@link android.app.Fragment} subclass.
 *
 */
public class MainMapFragment extends MapFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    Path path;
    private boolean collectPoint;
    private boolean collect;

    private Location lastLocation;

    private int accuracy;
    private int distance;

    public MainMapFragment() {}

    @Override
    public void onMapLoaded() {

        super.onMapLoaded();

        if(path.getGeometry()!=null){
            addLine(path.getGeometry(), "surveyPath");
            zoomToExtent();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if(collect) {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).
                    registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(collect) {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).
                    unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = super.onCreateView(inflater,container,savedInstanceState);

        if (bundle != null) {
            path = (Path)bundle.getSerializable("Path");
            collect = bundle.getBoolean("collect", false);
        }else{
            path = new Path();
            collect = false;
        }


        setHasOptionsMenu(true);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        accuracy = settings.getInt(SettingsActivity.PATH_ACCURACY,
                SettingsActivity.PATH_ACCURACY_DEFAULT);
        distance = settings.getInt(SettingsActivity.PATH_POINTS_DISTANCE,
                SettingsActivity.PATH_POINTS_DISTANCE_DEFAULT);

        System.out.println("Collecting points: " + collectPoint);

        if(collect) {
            collectPoint = settings.getBoolean(SettingsActivity.HARVESTING,
                    SettingsActivity.HARVESTING_DEFAULT);
        }

        System.out.println(" >> Collecting now ? " + collectPoint);

        return view;
    }

    @Override
    public void onGpsStatusChanged(int event, Location location, GpsStatus gpsStatus) {
        System.out.println("SurveyForm: onGpsStatusChanged");
        super.onGpsStatusChanged(event,location,gpsStatus);

        switch (event) {

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

                if (gpsStatus.getTimeToFirstFix() > 0 && location!=null) { // GPS is fixed and position can be used

                    System.out.println("Current Coordinates: " + location.getLongitude() + ", " + location.getLatitude());

                    if (collectPoint && location.getAccuracy()<=accuracy){

                        ArrayList<Double> current = new ArrayList<Double>(2);
                        current.add(location.getLongitude());
                        current.add(location.getLatitude());

                        // At least on point is collected. Minimal distance can be taken into account
                        if(path.getGeometry().getCoordinates().size()>0){

                            ArrayList<Double> last = path.getGeometry().getLastPoint();
                            Location lastLocation = new Location("TEMP");
                            lastLocation.setLatitude(last.get(1));
                            lastLocation.setLongitude(last.get(0));

                            if(lastLocation.distanceTo(location)>=distance){//Location.distanceBetween();
                                path.getGeometry().addPoint(current);
                                this.webview.loadUrl("javascript: addPointToLine(["+location.getLongitude()+","+location.getLatitude()+"], 'surveyPath');");
                                path.notifyChanges();
                            }

                        }else{
                            path.getGeometry().addPoint(current);
                            this.webview.loadUrl("javascript: addPointToLine(["+location.getLongitude()+","+location.getLatitude()+"], 'surveyPath');");
                            path.notifyChanges();
                        }
                    }

                    System.out.println(" >> accuracy: " + location.getAccuracy() + " min accuracy: " + accuracy);
                    System.out.println(" >> collecting: " + collectPoint);
                    System.out.println(" >> points: " + path.getGeometry().getCoordinates().size());

                }else{
                    System.out.println("Waiting for GPS..");
                }
                break;

        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if(!path.isClosed()){
            if(path.getSurface().size()>0){
                inflater.inflate(R.menu.harvesting_menu, menu);
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
                if(settings.getBoolean(SettingsActivity.HARVESTING, SettingsActivity.HARVESTING_DEFAULT)){
                    menu.findItem(R.id.harvesting_action).
                            setIcon(R.drawable.ic_action_hiking_pause);
                }
            }
            inflater.inflate(R.menu.walker_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsActivity.HARVESTING)) {
            if(sharedPreferences.getBoolean(SettingsActivity.HARVESTING, !SettingsActivity.HARVESTING_DEFAULT)){
                collectPoint = true;
            }else{
                collectPoint = false;
            }
        }
        System.out.println("onSharedPreferenceChanged, collectPoint: " + collectPoint);
    }

}
