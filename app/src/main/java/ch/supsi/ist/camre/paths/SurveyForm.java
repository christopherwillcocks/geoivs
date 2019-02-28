package ch.supsi.ist.camre.paths;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.google.android.gms.location.LocationListener;
import com.vividsolutions.jts.JTSVersion;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import ch.supsi.ist.camre.paths.adapters.ElementListAdapter;
import ch.supsi.ist.camre.paths.data.Element;
import ch.supsi.ist.camre.paths.data.Note;
import ch.supsi.ist.camre.paths.data.Path;
import ch.supsi.ist.camre.paths.data.Position;
import ch.supsi.ist.camre.paths.data.SimpleElement;
import ch.supsi.ist.camre.paths.utils.OnPathModifiedListener;
import ch.supsi.ist.camre.paths.utils.SwipeDismissListViewTouchListener;
import ch.supsi.ist.camre.paths.utils.VIEW;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnPathModifiedListener} interface
 * to handle interaction events.
 *
 */
public class SurveyForm extends Fragment  implements
        SimpleElementListFragment.OnElementListInteractionListener,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, LocationListener, WalkerActivityFooter.GpsListener,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private Element newElement;

    private MyDataSetObserver myDataSetObserver;
    private ProgressDialog progress;

    private Path path;
    private ListView bridgeList;
    private Button bridgeAdd;
    private View topView;
    private View bottomRightView;
    private View bottomLeftTitleView;
    private View bottomCenterTitleView;
    private View bottomLeftTipView;

    private ElementListAdapter leftAdapter;
    private ElementListAdapter rightAdapter;
    private ElementListAdapter surfaceAdapter;
    private ElementListAdapter bridgeAdapter;

    private SimpleElementListFragment left;
    private SimpleElementListFragment right;
    private SimpleElementListFragment surface;

    private int accuracy;
    private int distance;
    private boolean collectPoint;

    private class MyDataSetObserver extends DataSetObserver {

        /*private Fragment fragment;
        public MyDataSetObserver(Fragment fragment){
            this.fragment = fragment;
        }*/

        @Override
        public void onChanged() {
            if (path!=null) {
                System.out.println("SurveyForm: notifyChanges");
                path.notifyChanges();
            }
        }

        @Override
        public void onInvalidated(){}
    }

    public SurveyForm() {
        // Required empty public constructor
    }


    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.fragment_survey_form, container, false);


        topView = view.findViewById(R.id.survey_half_top);
        bottomRightView = view.findViewById(R.id.survey_half_bottom_right);
        bottomLeftTitleView = view.findViewById(R.id.survey_half_bottom_left_title);
        bottomCenterTitleView = view.findViewById(R.id.survey_half_bottom_center_title);
        bottomLeftTipView  = view.findViewById(R.id.survey_half_bottom_left_tip);

        left = SimpleElementListFragment.newInstance(SimpleElementListFragment.TYPE.SIDE);
        right = SimpleElementListFragment.newInstance(SimpleElementListFragment.TYPE.SIDE);
        surface = SimpleElementListFragment.newInstance(SimpleElementListFragment.TYPE.SURFACE);

        left.register(this);
        right.register(this);
        surface.register(this);

        myDataSetObserver = new MyDataSetObserver();

        ListView listView = (ListView) view.findViewById(R.id.leftActiveList);
        leftAdapter = new ElementListAdapter(
                getActivity().getApplicationContext() ,
                R.layout.row_element_list, new ArrayList<Element>());
        leftAdapter.setNotifyOnChange(true);
        listView.setTextFilterEnabled(true);
        listView.setAdapter(leftAdapter);

        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
                listView,
                new SwipeDismissListViewTouchListener.DismissCallbacks() {
                    public boolean canDismiss(int position) {
                        return true;
                    }
                    public void onDismiss(ListView lv, int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {
                            WalkerActivityFooter worker = (WalkerActivityFooter)getFragmentManager().findFragmentByTag(VIEW.LOCATION_WORKER.toString());
                            leftAdapter.getItem(position).setEndPoint(worker.getLastKnownGpsPosition());
                            Bundle bundle = new Bundle();
                            bundle.putString("type", "side");
                            bundle.putSerializable("Element", leftAdapter.getItem(position));
                            ((WalkerActivityTest)getActivity()).openFragment(
                                    VIEW.ELEMENT, bundle);
                        }
                    }
                });

        leftAdapter.registerDataSetObserver(myDataSetObserver);
        listView.setOnTouchListener(touchListener);
        listView.setOnScrollListener(touchListener.makeScrollListener());
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);

        listView = (ListView) view.findViewById(R.id.rightActiveList);
        rightAdapter = new ElementListAdapter(
                getActivity().getApplicationContext() ,
                R.layout.row_element_list, new ArrayList<Element>());
        rightAdapter.setNotifyOnChange(true);
        listView.setAdapter(rightAdapter);
        touchListener = new SwipeDismissListViewTouchListener(
                listView,
                new SwipeDismissListViewTouchListener.DismissCallbacks() {

                    public boolean canDismiss(int position) {
                        return true;
                    }

                    public void onDismiss(ListView lv, int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {

                            WalkerActivityFooter worker = (WalkerActivityFooter)getFragmentManager().findFragmentByTag(VIEW.LOCATION_WORKER.toString());
                            rightAdapter.getItem(position).setEndPoint(worker.getLastKnownGpsPosition());

                            Bundle bundle = new Bundle();
                            bundle.putString("type", "side");
                            bundle.putSerializable("Element", rightAdapter.getItem(position));
                            ((WalkerActivityTest)getActivity()).openFragment(
                                    VIEW.ELEMENT, bundle);

                            //rightAdapter.remove(rightAdapter.getItem(position));
                        }
                        //rightAdapter.notifyDataSetChanged();
                    }
                });
        rightAdapter.registerDataSetObserver(myDataSetObserver);
        listView.setOnTouchListener(touchListener);
        listView.setOnScrollListener(touchListener.makeScrollListener());
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);



        bridgeList = (ListView) view.findViewById(R.id.bridgeActiveList);
        bridgeAdapter = new ElementListAdapter(
                getActivity().getApplicationContext() ,
                R.layout.row_element_list, new ArrayList<Element>());
        bridgeAdapter.setNotifyOnChange(true);
        bridgeList.setAdapter(bridgeAdapter);
        touchListener = new SwipeDismissListViewTouchListener(
                bridgeList,
                new SwipeDismissListViewTouchListener.DismissCallbacks() {

                    public boolean canDismiss(int position) {
                        return true;
                    }

                    public void onDismiss(ListView lv, int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {

                            WalkerActivityFooter worker = (WalkerActivityFooter)getFragmentManager().findFragmentByTag(VIEW.LOCATION_WORKER.toString());
                            bridgeAdapter.getItem(position).setEndPoint(worker.getLastKnownGpsPosition());

                            Bundle bundle = new Bundle();
                            bundle.putString("type", "bridge");

                            bundle.putSerializable("Element", bridgeAdapter.getItem(position));
                            ((WalkerActivityTest)getActivity()).openFragment(
                                    VIEW.ELEMENT, bundle);
                            bridgeList.setVisibility(View.GONE);
                            bridgeAdd.setVisibility(View.VISIBLE);
                        }
                    }
                });
        bridgeAdapter.registerDataSetObserver(myDataSetObserver);
        bridgeList.setOnTouchListener(touchListener);
        bridgeList.setOnScrollListener(touchListener.makeScrollListener());
        bridgeList.setOnItemLongClickListener(this);
        bridgeList.setOnItemClickListener(this);
        registerForContextMenu(bridgeList);

        listView = (ListView) view.findViewById(R.id.surfaceActiveList);
        surfaceAdapter = new ElementListAdapter(
                getActivity().getApplicationContext() ,
                R.layout.row_element_list, new ArrayList<Element>());
        surfaceAdapter.setNotifyOnChange(true);
        listView.setAdapter(surfaceAdapter);
        touchListener = new SwipeDismissListViewTouchListener(
                listView,
                new SwipeDismissListViewTouchListener.DismissCallbacks() {
                    public boolean canDismiss(int position) {
                        return true;
                    }
                    public void onDismiss(ListView lv, int[] reverseSortedPositions) {
                        surfaceAdapter.notifyDataSetChanged();
                    }
                });
        surfaceAdapter.registerDataSetObserver(myDataSetObserver);
        listView.setOnTouchListener(touchListener);
        listView.setOnScrollListener(touchListener.makeScrollListener());
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);

        bridgeAdd = (Button) view.findViewById(R.id.survey_bridge_start);
        bridgeAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newElement = new Element(new SimpleElement("Bridge", "", "", "http://www.camre.ch/properties/bridge"));

                WalkerActivityFooter worker = (WalkerActivityFooter) getFragmentManager().findFragmentByTag(VIEW.LOCATION_WORKER.toString());

                newElement.setStartPoint(worker.getLastKnownGpsPosition());

                Bundle bundle = new Bundle();
                bundle.putBoolean("new", true);
                bundle.putString("type", "bridge");
                path.addBridge(newElement);

                bundle.putSerializable("Element", newElement);
                ((WalkerActivityTest) getActivity()).openFragment(
                        VIEW.ELEMENT, bundle);

                bridgeAdd.setVisibility(View.GONE);
                bridgeList.setVisibility(View.VISIBLE);

            }
        });

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            path = (Path)bundle.getSerializable("Path");
        } else {
            path = new Path();
        }
        return view;
    }

    @Override
    public void onStart() {
        System.out.println("SurveyForm: onStart");
        super.onStart();
        newElement = null;
        WalkerActivityFooter worker = (WalkerActivityFooter)getFragmentManager()
                .findFragmentByTag(VIEW.LOCATION_WORKER.toString());

        worker.addCustomGpsListener(this);
        worker.addFusionLocationListener(this);
        //worker.connectGPS();
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).
                unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        System.out.println("SurveyForm: onStop");
        WalkerActivityFooter worker = (WalkerActivityFooter)getFragmentManager()
                .findFragmentByTag(VIEW.LOCATION_WORKER.toString());
        worker.removeFusionLocationListener(this);
        worker.removeGpsListener(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        settings.registerOnSharedPreferenceChangeListener(this);

        accuracy = settings.getInt(SettingsActivity.PATH_ACCURACY,
                SettingsActivity.PATH_ACCURACY_DEFAULT);
        distance = settings.getInt(SettingsActivity.PATH_POINTS_DISTANCE,
                SettingsActivity.PATH_POINTS_DISTANCE_DEFAULT);

        collectPoint = settings.getBoolean(SettingsActivity.HARVESTING, SettingsActivity.HARVESTING_DEFAULT);

        getActivity().invalidateOptionsMenu();

        System.out.println("\n********************************************************************************************************************");
        System.out.println("RESUMING SURVEY FORM (WALKER)");
        System.out.println(path.get_id()+ " - " + path.get_rev());

        FragmentManager fragmentManager = getFragmentManager();

        if (path.getSurface().size()==0){
            fragmentManager.beginTransaction()
                    .replace(R.id.surfaceList, surface)
                    .commit();
            topView.setVisibility(View.GONE);
            bottomRightView.setVisibility(View.GONE);
            bottomLeftTitleView.setVisibility(View.GONE);
            bottomCenterTitleView.setVisibility(View.GONE);
            bottomLeftTipView.setVisibility(View.VISIBLE);
        }else{
            fragmentManager.beginTransaction()
                    .replace(R.id.leftList, left)
                    .replace(R.id.rightList, right)
                    .replace(R.id.surfaceList, surface)
                    .commit();
            topView.setVisibility(View.VISIBLE);
            bottomRightView.setVisibility(View.VISIBLE);
            bottomLeftTitleView.setVisibility(View.VISIBLE);
            bottomCenterTitleView.setVisibility(View.VISIBLE);
            bottomLeftTipView.setVisibility(View.GONE);
        }

        leftAdapter.unregisterDataSetObserver(myDataSetObserver);
        rightAdapter.unregisterDataSetObserver(myDataSetObserver);
        surfaceAdapter.unregisterDataSetObserver(myDataSetObserver);
        bridgeAdapter.unregisterDataSetObserver(myDataSetObserver);

        leftAdapter.clear();
        rightAdapter.clear();
        surfaceAdapter.clear();
        bridgeAdapter.clear();

        // LOADING STILL ACTIVE ELEMENTS (LEFT SIDE)
        Iterator<Element> iterator = path.getLeftSide().iterator();
        while (iterator.hasNext()){
            Element element = iterator.next();
            if (element.getEndPoint()==null){
                leftAdapter.add(element);
            }
        }

        // LOADING STILL ACTIVE ELEMENTS (RIGHT SIDE)
        iterator = path.getRightSide().iterator();
        while (iterator.hasNext()){
            Element element = iterator.next();
            if (element.getEndPoint()==null){
                rightAdapter.add(element);
            }
        }

        // LOADING STILL ACTIVE ELEMENTS (SURFACE)
        iterator = path.getSurface().iterator();
        while (iterator.hasNext()){
            Element element = iterator.next();
            if (element.getEndPoint()==null){
                surfaceAdapter.add(element);
            }
        }

        // LOADING STILL ACTIVE ELEMENTS (BRIDGE)
        //iterator = path.getBridge().iterator();
        System.out.println("bridgeList.getVisibility() = " + bridgeList.getVisibility());
        System.out.println("bridgeAdd.getVisibility() = " + bridgeAdd.getVisibility());
        if (path.getBridge().size()==0){
            bridgeList.setVisibility(View.INVISIBLE);
            bridgeAdd.setVisibility(View.VISIBLE);
        }else {
            Element element = path.getBridge().get(path.getBridge().size() - 1);
            if (element.getEndPoint() == null) {
                System.out.println(" > last element without end position");
                bridgeList.setVisibility(View.VISIBLE);
                bridgeAdd.setVisibility(View.INVISIBLE);

                bridgeAdapter.add(element);
            } else {
                System.out.println(" > last element has end position");
            }
        }

        leftAdapter.registerDataSetObserver(myDataSetObserver);
        rightAdapter.registerDataSetObserver(myDataSetObserver);
        surfaceAdapter.registerDataSetObserver(myDataSetObserver);
        bridgeAdapter.registerDataSetObserver(myDataSetObserver);

    }


    @Override
    public void onLocationChanged(Location location) {
        System.out.println("SurveyForm: onLocationChanged");
    }

    @Override
    public void onGpsStatusChanged(int event, Location location, GpsStatus gpsStatus) {
        System.out.println("SurveyForm: onGpsStatusChanged");

        switch (event) {

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

                if (gpsStatus.getTimeToFirstFix() > 0 && location!=null) { // GPS is fixed and position can be used

                    System.out.println("GPS ok: " + location.getLongitude() + ", " + location.getLatitude());
                    System.out.println(" >> accuracy: " + location.getAccuracy() + " min accuracy: " + accuracy);
                    System.out.println(" >> collecting: " + collectPoint);

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
                                path.notifyChanges();
                            }

                            /*try {

                                Geometry nextPoint = new WKTReader().read("POINT ("+location.getLongitude()+" "+location.getLatitude()+")");
                                ArrayList<Double> last = path.getGeometry().getLastPoint();
                                Geometry lastPoint = new WKTReader().read("POINT ("+last.get(0)+" "+last.get(1)+")");

                                if(DistanceOp.distance(lastPoint,nextPoint)>=distance){
                                    path.getGeometry().addPoint(current);
                                    path.notifyChanges();
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }*/

                        }else{
                            path.getGeometry().addPoint(current);
                            path.notifyChanges();
                        }
                    }

                }else{
                    System.out.println("Waiting for GPS..");
                }
                break;

        }
    }

    /*
        LISTEN FOR "SimpleElement" (Lower) SPINNER ITEM SELECTIONS
            - Add elements to the path
     */
    @Override
    public void onItemSelected(SimpleElement simpleElement, SimpleElementListFragment list) {

        newElement = new Element(simpleElement);

        WalkerActivityFooter worker = (WalkerActivityFooter)getFragmentManager().findFragmentByTag(VIEW.LOCATION_WORKER.toString());


        Position position = worker.getLastKnownGpsPosition();
        newElement.setStartPoint(position);

        Bundle bundle = new Bundle();
        bundle.putBoolean("new", true);

        //System.out.println("Adding also accuracy: " +  worker.getLastKnownGpsPosition().getAccuracy());

        bundle.putFloat("start_accuracy", worker.getLastKnownGpsPosition().getAccuracy());

        if (list == left){
            bundle.putString("type", "side");
            path.addLeft(newElement);
        } else if (list == right){
            bundle.putString("type", "side");
            path.addRight(newElement);
        } else if (list == surface){
            bundle.putString("type", "surface");
            path.addSurface(newElement);

            if (path.getGeometry().getCoordinates().size()==0){
                //System.out.println("Adding first point of the path!!");
                ArrayList<Double> point = new ArrayList<Double>(2);
                point.add(position.getGeometry().getLon());
                point.add(position.getGeometry().getLat());
                path.getGeometry().addPoint(point);
            }
        }

        bundle.putSerializable("Element", newElement);
        ((WalkerActivityTest)getActivity()).openFragment(
                VIEW.ELEMENT, bundle);

    }

    /*
        Listen for active element selection to open ElementForm details
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (view.getParent() instanceof ListView) {
            ListView list = (ListView) view.getParent();
            Bundle bundle = new Bundle();
            Element element=null;
            if (list.getId() == R.id.surfaceActiveList) {
                element = surfaceAdapter.getItem(i);
                bundle.putString("type", "surface");
            } else if (list.getId() == R.id.leftActiveList) {
                element = leftAdapter.getItem(i);
                bundle.putString("type", "side");
            } else if (list.getId() == R.id.rightActiveList) {
                element = rightAdapter.getItem(i);
                bundle.putString("type", "side");
            } else if (list.getId() == R.id.bridgeActiveList) {
                element = bridgeAdapter.getItem(i);
                bundle.putString("type", "bridge");
            }
            if(element!=null) {
                bundle.putSerializable("Element", element);
                ((WalkerActivityTest) getActivity()).openFragment(
                        VIEW.ELEMENT, bundle);
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if(path.getSurface().size()>0){
            inflater.inflate(R.menu.harvesting_menu, menu);
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            if(settings.getBoolean(SettingsActivity.HARVESTING, SettingsActivity.HARVESTING_DEFAULT)){
                menu.findItem(R.id.harvesting_action).
                        setIcon(R.drawable.ic_action_hiking_pause);
            }
        }
        inflater.inflate(R.menu.walker_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        if (view instanceof ListView) {
            menu.add(0, R.string.action_edit, 0, R.string.action_edit);
            menu.add(0, R.string.action_open_map, 0, R.string.action_open_map);
            if (view.getId() == R.id.surfaceActiveList) {
                menu.add(0, R.string.action_restore_previous, 0, R.string.action_restore_previous);
            } else if (view.getId() == R.id.leftActiveList) {
                menu.add(0, R.string.action_move_right, 0, R.string.action_move_right);
                menu.add(0, R.string.action_delete, 0, R.string.action_delete);
            } else {
                menu.add(0, R.string.action_move_left, 0, R.string.action_move_left);
                menu.add(0, R.string.action_delete, 0, R.string.action_delete);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Element element;
        if (info.targetView.getParent() instanceof ListView) {
            final ListView list = (ListView) info.targetView.getParent();
            if (list.getId() == R.id.surfaceActiveList) {
                System.out.println("surfaceAdapter");
                element = surfaceAdapter.getItem(info.position);
            }else if (list.getId() == R.id.leftActiveList) {
                System.out.println("leftAdapter");
                element = leftAdapter.getItem(info.position);
            } else {
                System.out.println("rightAdapter");
                element = rightAdapter.getItem(info.position);
            }
            Bundle bundle;
            switch (item.getItemId()){
                case R.string.action_edit:
                    bundle = new Bundle();
                    if (list.getId() == R.id.surfaceActiveList) {
                        bundle.putString("type", "surface");
                    } else if (list.getId() == R.id.leftActiveList) {
                        bundle.putString("type", "side");
                    } else {
                        bundle.putString("type", "side");
                    }
                    bundle.putSerializable("Element", element);
                    ((WalkerActivityTest)getActivity()).openFragment(
                            VIEW.ELEMENT, bundle);
                    break;

                case R.string.action_open_map:
                    bundle = new Bundle();
                    bundle.putSerializable("Element", element);
                    bundle.putSerializable("center", element.getStartPoint());
                    bundle.putBoolean("geolocation",true);
                    bundle.putInt("zoom", 17);
                    ((WalkerActivityTest)getActivity()).openFragment(
                            VIEW.MAP, bundle);
                    break;

                case R.string.action_delete:
                    final View cb = getActivity().getLayoutInflater()
                            .inflate(R.layout.prompt_checkbox,null);
                    new AlertDialog.Builder(getActivity())
                            //.setTitle("Select The Difficulty Level")
                            .setMessage(getString(R.string.action_delete_element))
                            .setView(cb)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    CheckBox keep = (CheckBox) cb.findViewById(R.id.prompt_checkbox_media);
                                    if (keep.isChecked()) {
                                        System.out.println("Deleting also media files");
                                        for (Note note: element.getNotes()){
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
                                    if (list.getId() == R.id.leftActiveList) {
                                        element.getParent().getLeftSide().remove(element);
                                        leftAdapter.remove(leftAdapter.getItem(info.position));
                                        leftAdapter.notifyDataSetChanged();
                                    } else {
                                        element.getParent().getRightSide().remove(element);
                                        rightAdapter.remove(rightAdapter.getItem(info.position));
                                        rightAdapter.notifyDataSetChanged();
                                    }
                                    element.getParent().notifyChanges();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                    break;

                case R.string.action_restore_previous:
                    element.getParent().getSurface().remove(element);
                    surfaceAdapter.remove(surfaceAdapter.getItem(info.position));
                    if(element.getParent().getSurface().size()>0){
                        Element previous = element.getParent().getSurface()
                                .get(element.getParent().getSurface().size()-1);
                        previous.setEndPoint(null);
                        surfaceAdapter.add(previous);
                    }
                    surfaceAdapter.notifyDataSetChanged();
                    element.getParent().notifyChanges();
                    break;

                case R.string.action_move_right:
                    element.getParent().getLeftSide().remove(element);
                    element.getParent().getRightSide().add(element);
                    leftAdapter.remove(leftAdapter.getItem(info.position));
                    leftAdapter.notifyDataSetChanged();
                    rightAdapter.add(element);
                    rightAdapter.notifyDataSetChanged();
                    element.getParent().notifyChanges();
                    break;

                case R.string.action_move_left:
                    element.getParent().getRightSide().remove(element);
                    element.getParent().getLeftSide().add(element);
                    rightAdapter.remove(rightAdapter.getItem(info.position));
                    rightAdapter.notifyDataSetChanged();
                    leftAdapter.add(element);
                    leftAdapter.notifyDataSetChanged();
                    element.getParent().notifyChanges();
                    break;
            }
            return true;
        }
        return false;
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
    }

}
