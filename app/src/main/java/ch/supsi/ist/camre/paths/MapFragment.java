package ch.supsi.ist.camre.paths;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.Location;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.Transformation;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;

import java.util.ArrayList;
import java.util.List;

import ch.supsi.ist.camre.paths.data.Linestring;
import ch.supsi.ist.camre.paths.data.Point;
import ch.supsi.ist.camre.paths.utils.Converter;
import ch.supsi.ist.camre.paths.utils.OlWebViewClient;
import ch.supsi.ist.camre.paths.utils.VIEW;


/**
 * A simple {@link Fragment} subclass.
 *
 *
 * Configuration with Bundle:
 *
 * geolocation (boolean): show user position on map with accuracy info
 * zoom (integer): define the zoom level of the
 * center (Position): Define the map center coordinates
 *
 *
 */

public class MapFragment extends Fragment
        implements OlWebViewClient.OnMapLoadedListener,
        WalkerActivityFooter.GpsListener, LocationListener{

    protected WebView webview;


    private boolean gpsOnly;
    private boolean geolocation;
    private boolean followme;

    private ImageButton positioncenter;
    private ProgressBar positionProgress;
    private ImageView positionOk;

    private LinearLayout ll;
    private FrameLayout bm;
    private TextView bottomText;

    private View view;

    public ProgressBar progressBar;

    private OlWebViewClient client;

    protected Bundle bundle;

    //private Point center;
    private ArrayList<Double> center = new ArrayList<Double>();

    private WalkerActivityFooter worker;
    private LocationListener listener;

    protected OnMapInteractionListener mListener;

    private boolean gpscontrols;

    public interface OnMapInteractionListener {
        public void onMapInteraction(int message, Object object);
    }

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        worker = (WalkerActivityFooter)getFragmentManager().findFragmentByTag(VIEW.LOCATION_WORKER.toString());

        view = inflater.inflate(R.layout.fragment_map, container, false);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        this.gpsOnly = settings.getBoolean(SettingsActivity.GPS_ONLY, SettingsActivity.GPS_ONLY_DEFAULT);

        //setLocationStrategy(settings.getBoolean(SettingsActivity.GPS_ONLY, SettingsActivity.GPS_ONLY_DEFAULT));

        //setHasOptionsMenu(true);

        view.findViewById(R.id.map_bottom_btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int finalHeight = (int) Converter.convertDpToPixel(60, ll.getContext());
                Animation b = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        bm.getLayoutParams().height = (int) (finalHeight * (1-interpolatedTime));
                        bm.requestLayout();
                        if(interpolatedTime>=1){
                            view.findViewById(R.id.map_bottom_btn_ok).setVisibility(View.INVISIBLE);
                            view.findViewById(R.id.map_bottom_btn_undo).setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public boolean willChangeBounds() {return true;}
                };
                b.setDuration(500);
                b.setInterpolator(new AccelerateInterpolator());
                bm.startAnimation(b);
            }
        });

        view.findViewById(R.id.map_bottom_btn_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locateMeAction = false;
                final int finalHeight = (int) Converter.convertDpToPixel(60, ll.getContext());
                Animation b = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        bm.getLayoutParams().height = (int) (finalHeight * (1-interpolatedTime));
                        bm.requestLayout();
                    }
                    @Override
                    public boolean willChangeBounds() {return true;}
                };
                b.setDuration(500);
                b.setStartTime(AnimationUtils.currentAnimationTimeMillis()+5000);
                b.setInterpolator(new AccelerateInterpolator());
                bm.startAnimation(b);
            }
        });

        bundle = this.getArguments();

        if(bundle!=null){

            if (bundle.containsKey("gpscontrols")){
                gpscontrols = bundle.getBoolean("gpscontrols",false);
            }
            if(bundle.containsKey("geolocation") && bundle.getBoolean("geolocation")){
                geolocation = true;
                followme = true;
            }else{
                geolocation = false;
                followme = false;
            }
        }


        //System.out.println("gpscontrols = " + gpscontrols);

        progressBar = (ProgressBar)view.findViewById(R.id.progressBar);

        positioncenter = (ImageButton)view.findViewById(R.id.map_position_best);

        positionProgress = (ProgressBar)view.findViewById(R.id.map_position_progress);
        positionOk = (ImageView)view.findViewById(R.id.map_position_ok);

        ll = (LinearLayout)view.findViewById(R.id.map_position_container);

        bm = (FrameLayout)view.findViewById(R.id.map_bottom_frame);
        bottomText = (TextView)view .findViewById(R.id.map_bottom_text);

        positioncenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View animatedView) {

                webview.loadUrl("javascript: locating = true; locateMe();");
                followme = true;

                if(gpsOnly) {

                    locations = new ArrayList<Location>();
                    timeout = System.currentTimeMillis() + time;
                    firstLocation = null;
                    finalLocation = null;

                    locateMeAction = true;

                    //worker.addCustomGpsListener(MapFragment.this);
                    //worker.removeFusionLocationListener(MapFragment.this);

                    final int finalHeight = (int) Converter.convertDpToPixel(60, ll.getContext());

                    Animation b = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            //System.out.println("targetWidth: " + finalHeight + " * interpolatedTime: " + interpolatedTime + " = " + (int) (finalHeight * interpolatedTime));
                            bm.getLayoutParams().height = (int) (finalHeight * interpolatedTime);
                            bm.requestLayout();
                        }
                        @Override
                        public boolean willChangeBounds() {
                            return true;
                        }
                    };


                    positionOk.setVisibility(View.INVISIBLE);
                    positionProgress.setVisibility(View.VISIBLE);

                    //bottomText.setText("Obtaining gps fix");
                    b.setDuration(500);
                    b.setInterpolator(new BounceInterpolator());
                    bm.startAnimation(b);

                }

            }
        });


        view.findViewById(R.id.map_position_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webview.loadUrl("javascript: locating = true; locateMe();");
                followme = true;
            }
        });

        view.findViewById(R.id.map_position_zoom_full).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomToExtent();
            }
        });

        webview = (WebView) view.findViewById(R.id.map_webview);

        // Enable Javascript
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //webSettings.setAllowFileAccessFromFileURLs(true); //Maybe you don't need this rule
        //webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setUseWideViewPort(true);

        webview.loadUrl("file:///android_asset/www/map.html");
        webview.addJavascriptInterface(this, "Camre");
        client = new OlWebViewClient(webview, bundle);
        webview.setWebViewClient(client);
        client.registerMapReady(this);

        return view;
    }


    /*@Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        //menu.clear();
        System.out.println("MapFragment: onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, getActivity().getMenuInflater());
        inflater.inflate(R.menu.map_menu, menu);
        if(gpscontrols) {
            inflater.inflate(R.menu.path_survey, menu);
            SharedPreferences settings =
                    PreferenceManager.getDefaultSharedPreferences(this.getActivity());

            System.out.println(" > harvesting: " + settings.getBoolean("HARVESTING", false));
            if(settings.getBoolean("HARVESTING", false)){
                menu.findItem(R.id.harvesting_action).
                        setIcon(R.drawable.ic_action_hiking_pause);
            }

            if(!settings.getBoolean(SettingsActivity.GPS_ENABLED,
                    SettingsActivity.GPS_ONLY_DEFAULT)){
                MenuItem item = menu.findItem(R.id.survey_action_record);
                item.setIcon(R.drawable.ic_action_record_disabled);
                item = menu.findItem(R.id.survey_action_pause);
                item.setVisible(false);
            }
        }
        MenuItem gpsItem = menu.findItem(R.id.path_map_gps);
        gpsItem.setChecked(gpsOnly);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("onOptionsItemSelected: id = " + item.getItemId());
        switch (item.getItemId()) {

            case R.id.path_map_gps:

                item.setChecked(!item.isChecked());
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(SettingsActivity.GPS_ONLY, item.isChecked());
                editor.commit();
                setLocationStrategy(item.isChecked());
                return true;

            case R.id.path_settings:

                Intent intent = new Intent(this.getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;

        }
        return false;
    }*/

    /**
     * On map loaded event. Here a geolocation listener is initialized if the bondle
     * contains a true boolean with key "geolocation"
     */
    @Override
    public void onMapLoaded() {
        System.out.println("onMapLoaded: ");
    }

    /**
     *
     * Add a point in the ol.layer.Vector layer (vectorLayer). The name is used as feature id
     * and as parameter of the feature attributes.
     *
     * @param point
     * @param name
     */
    protected void addPoint(Point point, String name){
        this.webview.loadUrl("javascript:addPoint(["+point.getLon()+","+point.getLat()+"],\""+name+"\");");
    }

    /**
     * Remove the point using the attribute "name" given when added using addPoint(Point point, String name)
     *
     * @param name
     */
    protected void removePoint(String name){
        this.webview.loadUrl("javascript:removePoint(\""+name+"\");");
    }

    /**
     *
     * Add a point in the ol.layer.Vector layer (vectorLayer). The name is used as feature id
     * and as parameter of the feature attributes.
     *
     * @param point
     * @param name
     */
    protected void addLine(Linestring point, String name){
        //System.out.println("Coordinates: " + point.getCoordinates().toString());
        this.webview.loadUrl("javascript:addLine("+point.getCoordinates().toString()+",\""+name+"\");");
    }

    /**
     * Remove the point using the attribute "name" given when added using addPoint(Point point, String name)
     *
     * @param name
     */
    protected void removeLine(String name){
        //this.webview.loadUrl("javascript:removePoint(\""+name+"\");");
    }

    protected void zoomToExtent(){
        stopFollowing();
        this.webview.loadUrl("javascript:zoomToExtent();");
    }


    protected void zoomToExtent(Double minx, Double miny, Double maxx, Double maxy){
        stopFollowing();
        this.webview.loadUrl("javascript:zoomToBox(" + minx + "," + miny + "," + maxx + "," + maxy + ");");
    }

    /**
     * Update the location if the map is initialized with the geolocation option.
     *
     * @param point
     * @param accuracy
     */
    protected void setPosition(Point point, float accuracy){
        //System.out.println(" > Setting position");
        this.webview.loadUrl("javascript:myPosition.setProperties({accuracy: "+accuracy+"});");
        this.webview.loadUrl("javascript:myPosition.setGeometry(new ol.geom.Point(" +
                "ol.proj.transform(["+point.getLon()+","+point.getLat()+"], 'EPSG:4326', 'EPSG:3857')));");
        if(followme){
            webview.loadUrl("javascript:locateMe();");
        }
    }

    @JavascriptInterface
    public void stopFollowing(){
        followme = false;
    }

    /**
     * Return the center point of the map.
     *
     * @return center
     */
    public Point getCenter() {
        return new Point(center);
    }

    /**
     * Function called from JavaScript (utils.js) every time the map event 'moveend' is raised.
     * The local variable center is updated, and can be accessed using the  getCenter() method.
     *
     * @param newCenter
     */
    @JavascriptInterface
    public void updateCenter(String newCenter){

        String[] point = newCenter.split(",");
        center = new ArrayList<Double>();

        center.add((double) Math.round(Double.parseDouble(point[0]) * 10000000) / 10000000);
        center.add((double)Math.round(Double.parseDouble(point[1])*10000000)/10000000);
        //center.add(Double.parseDouble(point[1]));

    }

    @Override
    public void onResume() {
        super.onResume();
        setLocationStrategy(this.gpsOnly);
        //worker.addCustomGpsListener(this);
        //worker.addFusionLocationListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        worker.removeFusionLocationListener(this);
        worker.removeGpsListener(this);
    }

    public void setMapInteractionListener(OnMapInteractionListener listener){
        mListener = listener;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(!gpsOnly){
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            setPosition(new Point(lon, lat), location.getAccuracy());
        }
    }

    private void resetDefaultPositioning(){

        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        bottomText.setText("Best accuracy found: " + finalLocation.getAccuracy());

        this.setLocationStrategy(this.gpsOnly);

        locateMeAction = false;

        view.findViewById(R.id.map_bottom_btn_ok).setVisibility(View.VISIBLE);
        view.findViewById(R.id.map_bottom_btn_undo).setVisibility(View.INVISIBLE);

        positionOk.setVisibility(View.VISIBLE);
        positionProgress.setVisibility(View.INVISIBLE);

    }

    // Calculate best position variable
    private List<Location> locations;
    private Location firstLocation;
    private Location finalLocation;
    private long timeout;
    private long time = 60000;
    private float tolleratedAccuracy = 20; // metres
    private int samplingCounter = 3; // metres

    private boolean locateMeAction = false;

    @Override
    public void onGpsStatusChanged(int event, Location location, GpsStatus gpsStatus) {

        switch (event) {

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

                if (gpsStatus.getTimeToFirstFix() > 0 && location!=null) { // GPS is fixed and position can be used

                    if(locateMeAction) {

                        // Getting first position
                        if (firstLocation == null) {
                            firstLocation = location;
                            bottomText.setText("Accuracy: " + location.getAccuracy());
                            //System.out.println(" >>>>>>>>>>>>>>>>>>>> First accuracy: " + location.getAccuracy());
                        } else {
                            locations.add(location);

                            //System.out.println(" >>>>>>>>>>>>>>>>>>>> Accuracy " + locations.size() + ": " + location.getAccuracy());

                            if (locations.size() == samplingCounter) {

                                Location aMin = null;
                                for (Location loc : locations) {
                                    if (aMin == null) {
                                        aMin = loc;
                                    } else {
                                        if (loc.getAccuracy() < aMin.getAccuracy()) {
                                            aMin = loc;
                                        }
                                    }
                                }
                                if (aMin.getAccuracy() < firstLocation.getAccuracy()) {

                                    float gain = 100 * ((firstLocation.getAccuracy() - aMin.getAccuracy()) / firstLocation.getAccuracy());

                                    if (gain >= 10f) {

                                        //System.out.println(">>>>> Great gain: " + gain);
                                        firstLocation = aMin;
                                        locations.clear();

                                        bottomText.setText("Accuracy: " + aMin.getAccuracy());

                                    } else {
                                        //System.out.println(">>>>> Slow gain: " + gain);
                                        if (aMin.getAccuracy() < tolleratedAccuracy) {
                                            //System.out.println(">>>>> Min accuracy is ok: Atoll=" + tolleratedAccuracy);
                                            // END GEOLOCATION
                                            finalLocation = aMin;
                                            resetDefaultPositioning();

                                            bottomText.setText("Accuracy: " + aMin.getAccuracy());

                                            //System.out.println(" >>>>>>>>>>>>>>>>>>>> 1 Finale location found, accuracy: " + finalLocation.getAccuracy());
                                        } else {
                                            //System.out.println(">>>>> Trying again");
                                            firstLocation = aMin;
                                            locations.clear();
                                            //System.out.println(">>>> Timeout ? " + " timeout: " + timeout + " currentTimeMillis: " + System.currentTimeMillis() + " = " + (timeout < System.currentTimeMillis()));
                                            bottomText.setText("Accuracy: " + aMin.getAccuracy());
                                            if (timeout < System.currentTimeMillis()) {
                                                finalLocation = aMin;
                                                resetDefaultPositioning();
                                                //System.out.println(" >>>>>>>>>>>>>>>>>>>> 2 Finale location found, accuracy: " + finalLocation.getAccuracy());
                                            }
                                        }
                                    }

                                    double lat = aMin.getLatitude();
                                    double lon = aMin.getLongitude();
                                    setPosition(new Point(lon, lat), aMin.getAccuracy());
                                    webview.loadUrl("javascript:locateMe();");

                                } else {
                                    //System.out.println(">>>>> Bad accuracy..");
                                    locations.clear();
                                    //System.out.println(">>>> Timeout ? " + " timeout: " + timeout + " currentTimeMillis: " + System.currentTimeMillis() + " = " + (timeout < System.currentTimeMillis()));

                                    if (firstLocation.getAccuracy() < tolleratedAccuracy) {
                                        System.out.println(">>>>> Min accuracy is ok: Atoll=" + tolleratedAccuracy);
                                        // END GEOLOCATION
                                        finalLocation = firstLocation;
                                        resetDefaultPositioning();
                                        bottomText.setText("Accuracy: " + firstLocation.getAccuracy());
                                        //System.out.println(" >>>>>>>>>>>>>>>>>>>> 4 Finale location found, accuracy: " + finalLocation.getAccuracy());
                                    } else if (timeout < System.currentTimeMillis()) {
                                        finalLocation = firstLocation;
                                        resetDefaultPositioning();
                                        bottomText.setText("Accuracy: " + firstLocation.getAccuracy());
                                        //System.out.println(" >>>>>>>>>>>>>>>>>>>> 3 (Time OUT) Finale location found, accuracy: " + finalLocation.getAccuracy());
                                    }

                                    double lat = firstLocation.getLatitude();
                                    double lon = firstLocation.getLongitude();
                                    setPosition(new Point(lon, lat), firstLocation.getAccuracy());
                                    webview.loadUrl("javascript:locateMe();");

                                }
                            }
                        }
                    }else{
                        //System.out.println("GPS fixed.. Positioning: " + location.getElapsedRealtimeNanos());
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        setPosition(new Point(lon, lat), location.getAccuracy());
                    }
                }/*else{
                    System.out.println("Waiting for GPS..");
                }*/
                break;
        }
    }

    private void setLocationStrategy(boolean gpsOnly){
        //System.out.println("setLocationStrategy: " + gpsOnly);
        //System.out.println("  >> geolocation: " + geolocation);
        this.gpsOnly = gpsOnly;
        if(geolocation) {
            if(gpsOnly){
                //System.out.println("Setting GPS only listener");
                worker.removeFusionLocationListener(this);
                worker.addCustomGpsListener(this);
            }else {
                //System.out.println("Setting FUSE only listener");
                worker.removeGpsListener(this);
                worker.addFusionLocationListener(this);
            }
        }
    }
}
