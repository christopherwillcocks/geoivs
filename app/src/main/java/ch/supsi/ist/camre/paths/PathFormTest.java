package ch.supsi.ist.camre.paths;



import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.gms.location.LocationListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import ch.supsi.ist.camre.paths.adapters.KeyValuePairAdapter;
import ch.supsi.ist.camre.paths.data.KeyValuePair;
import ch.supsi.ist.camre.paths.data.Path;
import ch.supsi.ist.camre.paths.data.Point;
import ch.supsi.ist.camre.paths.data.Position;
import ch.supsi.ist.camre.paths.utils.VIEW;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class PathFormTest extends Fragment
        implements  AdapterView.OnItemSelectedListener,
                    CompoundButton.OnCheckedChangeListener{

    private Path path;

    private boolean isDirty;

    private EditText name;
    private EditText path_path;
    private Spinner status;
    private Spinner difficulty;
    private Spinner protection;
    private Spinner risk;
    private Spinner hazard;
    private Spinner activity;
    private Switch circular;

    private WalkerActivityFooter worker;

    public PathFormTest() {
        // Required empty public constructor
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            path = (Path)bundle.getSerializable("Path");
        } else {
            path = new Path();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_path_form_test, container, false);

        setHasOptionsMenu(true);

        name = (EditText) view.findViewById(R.id.path_name);
        path_path = (EditText) view.findViewById(R.id.path_path);

        status = (Spinner) view.findViewById(R.id.path_status);
        status.setAdapter(new KeyValuePairAdapter(
                        getActivity().getApplicationContext(),
                        R.layout.row_spinner,
                        KeyValuePair.getList(
                                getResources().getStringArray(R.array.path_status))
                )
        );

        difficulty = (Spinner) view.findViewById(R.id.path_characteristics_difficulty);
        difficulty.setAdapter(new KeyValuePairAdapter(
                        getActivity().getApplicationContext(),
                        R.layout.row_spinner,
                        KeyValuePair.getList(
                                getResources().getStringArray(R.array.path_characteristics_difficulty))
                )
        );

        protection = (Spinner) view.findViewById(R.id.path_characteristics_protection);
        protection.setAdapter(new KeyValuePairAdapter(
                        getActivity().getApplicationContext(),
                        R.layout.row_spinner,
                        KeyValuePair.getList(
                                getResources().getStringArray(R.array.path_characteristics_protection))
                )
        );

        risk = (Spinner) view.findViewById(R.id.path_characteristics_risk);
        risk.setAdapter(new KeyValuePairAdapter(
                        getActivity().getApplicationContext(),
                        R.layout.row_spinner,
                        KeyValuePair.getList(
                                getResources().getStringArray(R.array.path_characteristics_risk))
                )
        );

        hazard = (Spinner) view.findViewById(R.id.path_characteristics_hazard);
        hazard.setAdapter(new KeyValuePairAdapter(
                        getActivity().getApplicationContext(),
                        R.layout.row_spinner,
                        KeyValuePair.getList(
                                getResources().getStringArray(R.array.path_characteristics_hazard))
                )
        );

        activity = (Spinner) view.findViewById(R.id.path_characteristics_activity);
        activity.setAdapter(new KeyValuePairAdapter(
                        getActivity().getApplicationContext(),
                        R.layout.row_spinner,
                        KeyValuePair.getList(
                                getResources().getStringArray(R.array.path_characteristics_activity))
                )
        );
        activity.setOnItemSelectedListener(this);

        circular = (Switch) view.findViewById(R.id.path_characteristics_circular);


        if(path.isClosed()){
            name.setEnabled(false);
            path_path.setEnabled(false);
            status.setEnabled(false);
            difficulty.setEnabled(false);
            protection.setEnabled(false);
            risk.setEnabled(false);
            hazard.setEnabled(false);
            activity.setEnabled(false);
            circular.setEnabled(false);
        }

        worker = (WalkerActivityFooter)getFragmentManager().findFragmentByTag(VIEW.LOCATION_WORKER.toString());

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if(!path.isClosed()){
            if(this.path==null || this.path.get_id() == null || this.path.get_id().equals("")){
                inflater.inflate(R.menu.path_form_new, menu);
            }else{
                inflater.inflate(R.menu.walker_menu, menu);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        System.out.println("PathForm: onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putSerializable("Path", path);
    }

    @Override
    public void onPause() {
        Fragment map = getFragmentManager()
                .findFragmentByTag(VIEW.PATH_MAP.toString());
        getFragmentManager()
                .beginTransaction()
                .remove(map)
                .commit();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(this.path==null || this.path.get_id() == null || this.path.get_id().equals("")){
            //getActivity().setTitle(getString(R.string.path_new_form));
            this.path = new Path();
        }

        name.setText(path.getName());
        path_path.setText(path.getPath());
        status.setSelection(
                ((KeyValuePairAdapter)status.getAdapter()).
                        indexOf(path.getStatus())
        );
        System.out.println("Setting selection: " + ((KeyValuePairAdapter)status.getAdapter()).
                indexOf(path.getStatus()));
        difficulty.setSelection(
                ((KeyValuePairAdapter)difficulty.getAdapter()).
                        indexOf(path.getCharacteristics().getDifficulty())
        );
        protection.setSelection(
                ((KeyValuePairAdapter)protection.getAdapter()).
                        indexOf(path.getCharacteristics().getProtection())
        );
        risk.setSelection(
                ((KeyValuePairAdapter) risk.getAdapter()).
                        indexOf(path.getCharacteristics().getRisk())
        );
        hazard.setSelection(
                ((KeyValuePairAdapter) hazard.getAdapter()).
                        indexOf(path.getCharacteristics().getHazard())
        );
        activity.setSelection(
                ((KeyValuePairAdapter) activity.getAdapter()).
                        indexOf(path.getCharacteristics().getActivity())
        );
        circular.setChecked(path.getCharacteristics().getCircular());

        // INITIALIZING FIELDS CHANGE LISTENERS
        class GenericTextWatcher implements TextWatcher {
            private View view;
            private GenericTextWatcher(View view) {
                System.out.println("GenericTextWatcher: constructed ("+path.getName()+")");
                this.view = view;
            }
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                System.out.println("GenericTextWatcher: afterTextChanged: " + text);
                switch(view.getId()){
                    case R.id.path_name:
                        path.setName(text);
                        isDirty = true;
                        break;
                    case R.id.path_path:
                        path.setPath(text);
                        isDirty = true;
                        break;
                }
                System.out.println("afterTextChanged: Dirty status: " + isDirty);
            }
        }

        name.addTextChangedListener(new GenericTextWatcher(name));
        path_path.addTextChangedListener(new GenericTextWatcher(path_path));
        status.setOnItemSelectedListener(this);
        difficulty.setOnItemSelectedListener(this);
        protection.setOnItemSelectedListener(this);
        risk.setOnItemSelectedListener(this);
        hazard.setOnItemSelectedListener(this);
        circular.setOnCheckedChangeListener(this);



        if(path.getGeometry().getCoordinates().size()>0){

            ArrayList<Double> coords = path.getGeometry().getCoordinates().get(path.getGeometry().getCoordinates().size()-1);

            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            df.setTimeZone(tz);

            Position pos = new Position();
            pos.setAltitude(0.0);
            pos.setHeading(0.0);
            pos.setGeometry(new Point(coords.get(0), coords.get(1)));
            pos.setTimestamp(df.format(new Date()));

            Bundle bundle = new Bundle();
            bundle.putSerializable("Path", path);
            //bundle.putSerializable("center", pos);
            //bundle.putInt("zoom", 17);
            bundle.putBoolean("geolocation", true);
            PathMapFragment map = new PathMapFragment();
            map.setArguments(bundle);
            getFragmentManager().beginTransaction()
                    .replace(R.id.path_map, map, VIEW.PATH_MAP.toString())
                    .commit();

        }else {

            worker.addFusionLocationListener(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

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

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Path", path);
                    bundle.putSerializable("center", pos);
                    bundle.putBoolean("geolocation", true);
                    bundle.putInt("zoom", 17);
                    PathMapFragment map = new PathMapFragment();
                    map.setArguments(bundle);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.path_map, map, VIEW.PATH_MAP.toString())
                            .commit();
                    worker.removeFusionLocationListener(this);
                }
            });
        }
        isDirty = false;
        System.out.println("Dirty status is set: " + isDirty);

    }

    /*
        LISTEN FOR SWITCH ACTION
     */
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        if (compoundButton instanceof Switch) {
            switch (compoundButton.getId()) {
                case R.id.path_characteristics_circular:
                    path.getCharacteristics().setCircular(checked);
                    isDirty = true;
            }
        }
        System.out.println("onCheckedChanged: Dirty status: " + isDirty);
    }

    /*
        LISTEN FOR SPINNER ITEM SELECTIONS
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (view!=null && view.getParent() instanceof Spinner) {
            Spinner spinner = (Spinner) view.getParent();
            KeyValuePair kvp;
            switch (spinner.getId()) {
                case R.id.path_status:
                    kvp = (KeyValuePair) status.getItemAtPosition(i);
                    path.setStatus(kvp.getKey());
                    isDirty = true;
                    break;
                case R.id.path_characteristics_difficulty:
                    kvp = (KeyValuePair) difficulty.getItemAtPosition(i);
                    path.getCharacteristics().setDifficulty(kvp.getKey());
                    isDirty = true;
                    break;
                case R.id.path_characteristics_protection:
                    kvp = (KeyValuePair) protection.getItemAtPosition(i);
                    path.getCharacteristics().setProtection(kvp.getKey());
                    isDirty = true;
                    break;
                case R.id.path_characteristics_risk:
                    kvp = (KeyValuePair) risk.getItemAtPosition(i);
                    path.getCharacteristics().setRisk(kvp.getKey());
                    isDirty = true;
                    break;
                case R.id.path_characteristics_hazard:
                    kvp = (KeyValuePair) hazard.getItemAtPosition(i);
                    path.getCharacteristics().setHazard(kvp.getKey());
                    isDirty = true;
                    break;
                case R.id.path_characteristics_activity:
                    kvp = (KeyValuePair) activity.getItemAtPosition(i);
                    path.getCharacteristics().setActivity(kvp.getKey());
                    isDirty = true;
                    break;
            }
            System.out.println("onItemSelected: Dirty status: " + isDirty);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

}
