package ch.supsi.ist.camre.paths;



import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import ch.supsi.ist.camre.paths.adapters.KeyValuePairAdapter;
import ch.supsi.ist.camre.paths.data.Element;
import ch.supsi.ist.camre.paths.data.KeyValuePair;
import ch.supsi.ist.camre.paths.data.Note;
import ch.supsi.ist.camre.paths.data.Point;
import ch.supsi.ist.camre.paths.data.Position;
import ch.supsi.ist.camre.paths.utils.VIEW;


/**
 * A simple {@link Fragment} subclass.
 */
public class ElementForm extends Fragment  implements
        AdapterView.OnItemSelectedListener{

    Element element;

    private boolean stopListeners = false;

    public String etype;

    private boolean closingAction;

    private boolean listenChanges;

    private ProgressDialog progress;
    private int countAccuracy;
    private boolean waitAccuracy;

    private Spinner status;
    private Spinner type;

    private EditText startLat;
    private EditText startLon;
    private EditText endLat;
    private EditText endLon;
    private EditText width;
    private EditText height;

    private MenuItem next, prev;

    private LinearLayout horizontal;

    private ElementMapFragment map;

    private GenericTextWatcher twWidth;
    private GenericTextWatcher twHeight;
    private GenericTextWatcher twStartLat;
    private GenericTextWatcher twStartLon;
    private GenericTextWatcher twEndLon;
    private GenericTextWatcher twEndLat;

    //private OnPathModifiedListener mCallback;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    //private ImageView mImageView;

    public ElementForm() {
        // Required empty public constructor
        listenChanges = true;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        System.out.println("ElementForm: onCreateView!!");

        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_element_form, container, false);

        horizontal = (LinearLayout)view.findViewById(R.id.element_horizontal);

        Bundle bundle = this.getArguments();

        /*if(bundle.containsKey("new")){
            waitAccuracy = bundle.getBoolean("new");
        }else{
            waitAccuracy = false;
        }*/

        //FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (bundle != null) {
            element = (Element)bundle.getSerializable("Element");
            System.out.println("ElementForm: onCreateView > bundle != null > " + element.getName());
            /*if(savedInstanceState==null && element.getNotes().size()>0){
                for(Note note: element.getNotes()){
                    if(note.getUri()!=null) {
                        Bundle b = new Bundle();
                        b.putSerializable("Note", note);
                        NotePreview notePreview = new NotePreview();
                        notePreview.setArguments(b);
                        ft.add(horizontal.getId(), notePreview);
                    }
                }
            }*/
        }else{
            System.out.println("ElementForm: onCreateView > bundle == null");
            element = new Element();
        }

        bundle.putSerializable("center", element.getStartPoint());
        bundle.putBoolean("geolocation",true);
        bundle.putInt("zoom", 17);

        map = new ElementMapFragment();
        map.setArguments(bundle);

        map.setMapInteractionListener(
                new MapFragment.OnMapInteractionListener() {

            @Override
            public void onMapInteraction(int message, Object object) {

                System.out.println(" Map is interacting!! Message: " + message);
                System.out.println("   > " + etype);

                switch (message){

                    case ElementMapFragment.STARTPOINTMOVE:

                        System.out.println("   > STARTPOINTMOVE");

                        element.getStartPoint().setGeometry((Point)object);

                        System.out.println("   > path size: " + element.getParent().getGeometry().getCoordinates().size());

                        // Add first point of path if not yet set
                        if (etype.equals("surface") &&
                                element.getParent().getGeometry().getCoordinates().size()==1 &&
                                element.getParent().getSurface().size()==1){

                            System.out.println("Modifying first point of the path!!");

                            ArrayList<Double> point = new ArrayList<Double>(2);
                            point.add(((Point)object).getLon());
                            point.add(((Point)object).getLat());

                            element.getParent().getGeometry().replaceLastPoint(point);

                            SharedPreferences.Editor edit = PreferenceManager.
                                    getDefaultSharedPreferences(getActivity()).edit();
                            edit.putBoolean("HARVESTING", true);
                            edit.commit();
                        }

                        startLat.setText( (element.getStartPoint().getGeometry().getLat()).toString() );
                        startLon.setText( (element.getStartPoint().getGeometry().getLon()).toString() );

                        break;

                    case ElementMapFragment.ENDPOINTMOVE:
                        System.out.println("   > ENDPOINTMOVE");

                        if(element.getEndPoint()==null){
                            Position endPosition = new Position();
                            endPosition.setAltitude(0.0);
                            endPosition.setHeading(0.0);
                            endPosition.setGeometry((Point) object);
                            TimeZone tz = TimeZone.getTimeZone("UTC");
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            df.setTimeZone(tz);
                            endPosition.setTimestamp(df.format(new Date()));
                            element.setEndPoint(endPosition);
                        }else{
                            element.getEndPoint().setGeometry((Point) object);
                        }

                        listenChanges = false;
                        endLat.setText( (element.getEndPoint().getGeometry().getLat()).toString() );
                        endLon.setText( (element.getEndPoint().getGeometry().getLon()).toString() );
                        listenChanges = true;

                        break;
                }
            }
        });

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.element_map, map, VIEW.MAP.toString());
        ft.commit();

        etype = bundle.getString("type");

        if(bundle.containsKey("closingAction") && bundle.getBoolean("closingAction")){
            closingAction = true;
        }

        startLat = (EditText) view.findViewById(R.id.element_start_point_lat);
        startLon = (EditText) view.findViewById(R.id.element_start_point_lon);

        endLat = (EditText) view.findViewById(R.id.element_end_point_lat);
        endLon = (EditText) view.findViewById(R.id.element_end_point_lon);

        width = (EditText) view.findViewById(R.id.element_width);
        height = (EditText) view.findViewById(R.id.element_height);

        status = (Spinner) view.findViewById(R.id.element_status);
        status.setAdapter(new KeyValuePairAdapter(
                        getActivity().getApplicationContext(),
                        R.layout.row_spinner,
                        KeyValuePair.getList(
                                getResources().getStringArray(R.array.element_status))
                )
        );

        type = (Spinner) view.findViewById(R.id.element_type);
        if (etype.equals("side")) {
            type.setAdapter(new KeyValuePairAdapter(
                            getActivity().getApplicationContext(),
                            R.layout.row_spinner,
                            KeyValuePair.getList(
                                    getResources().getStringArray(R.array.elements_side_type))
                    )
            );
            view.findViewById(R.id.element_surface_message).setVisibility(View.GONE);
            view.findViewById(R.id.element_end_point_view).setVisibility(View.VISIBLE);

        }else if (etype.equals("surface")) {
            type.setAdapter(new KeyValuePairAdapter(
                            getActivity().getApplicationContext(),
                            R.layout.row_spinner,
                            KeyValuePair.getList(
                                    getResources().getStringArray(R.array.elements_surface_type))
                    )
            );
            if(closingAction) {
                view.findViewById(R.id.element_surface_message).setVisibility(View.GONE);
                view.findViewById(R.id.element_end_point_view).setVisibility(View.VISIBLE);
            }else{
                view.findViewById(R.id.element_surface_message).setVisibility(View.VISIBLE);
                view.findViewById(R.id.element_end_point_view).setVisibility(View.GONE);
            }

        }else{
            type.setAdapter(new KeyValuePairAdapter(
                            getActivity().getApplicationContext(),
                            R.layout.row_spinner,
                            KeyValuePair.getList(
                                    getResources().getStringArray(R.array.elements_bridge_type))
                    )
            );
            view.findViewById(R.id.element_surface_message).setVisibility(View.GONE);
            view.findViewById(R.id.element_end_point_view).setVisibility(View.VISIBLE);
        }

        return view;
    }


    @Override
    public void onStart() {
        System.out.println("SurveyForm: onStart");
        super.onStart();
        //countAccuracy = 0;
        //WalkerActivityFooter worker = (WalkerActivityFooter)getFragmentManager().findFragmentByTag(VIEW.LOCATION_WORKER.toString());
        //worker.addGpsStatusListener(this);
        //worker.addFusionLocationListener(this);
        //worker.connectGPS();
    }

    @Override
    public void onPause(){
        super.onPause();
        getFragmentManager()
                .beginTransaction()
                .remove(map)
                .commit();
    }

    @Override
    public void onStop() {
        System.out.println("SurveyForm: onStop");
        //WalkerActivityFooter worker = (WalkerActivityFooter)getFragmentManager().findFragmentByTag(VIEW.LOCATION_WORKER.toString());
        //worker.removeFusionLocationListener(this);
        //worker.removeGpsStatusistener(this);
        super.onStop();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        System.out.println("ElementForm: onSaveInstanceState");
        //System.out.println("  - name: " + element.getName());
        /*path.resetListeners();
        outState.putSerializable("Path", this.path);
        outState.putInt("activeTab", this.activeTab);*/
    }

    @Override
    public void onResume(){

        System.out.println("ElementForm: onResume");
        super.onResume();

        // Filling form
        if(element.getStartPoint()!=null) {

            startLon.setText((element.getStartPoint().getGeometry().getLon()).toString());
            startLat.setText((element.getStartPoint().getGeometry().getLat()).toString());

            if(getArguments().containsKey("start_accuracy")){
                System.out.println("Bundle contains accuracy!!");
                TextView accuracy = (TextView)getView().findViewById(R.id.element_start_point_accuracy);
                accuracy.setText("±" + getArguments().getFloat("accuracy") + "m");
                accuracy.setVisibility(View.VISIBLE);
            }else{
                System.out.println("Bundle NOT contains accuracy!!");
            }

            if(element.getEndPoint()!=null){
                endLat.setText( (element.getEndPoint().getGeometry().getLat()).toString() );
                endLon.setText( (element.getEndPoint().getGeometry().getLon()).toString() );
            }else{
                endLat.setText("");
                endLon.setText("");
            }


        }else{
            startLat.setText("");
            startLat.setText("");
            endLat.setText("");
            endLon.setText("");
        }


        status.setSelection(
                ((KeyValuePairAdapter)status.getAdapter()).
                        indexOf(element.getStatus())
        );
        type.setSelection(
                ((KeyValuePairAdapter)type.getAdapter()).
                        indexOf(element.getType())
        );
        width.setText(element.getWidth());
        height.setText(element.getHeight());

        if(!element.getParent().isClosed()) {

            twWidth = new GenericTextWatcher(width);
            twHeight = new GenericTextWatcher(height);
            twStartLat = new GenericTextWatcher(startLat);
            twStartLon = new GenericTextWatcher(startLon);
            twEndLon = new GenericTextWatcher(endLon);
            twEndLat = new GenericTextWatcher(endLat);

            status.setOnItemSelectedListener(this);
            type.setOnItemSelectedListener(this);
            width.addTextChangedListener(twWidth);
            height.addTextChangedListener(twHeight);
            startLat.addTextChangedListener(twStartLat);
            startLon.addTextChangedListener(twStartLon);
            endLon.addTextChangedListener(twEndLon);
            endLat.addTextChangedListener(twEndLat);
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if(element.getNotes().size()>0){
            for(Note note: element.getNotes()){
                if(note.getUri()!=null) {
                    Bundle b = new Bundle();
                    b.putSerializable("Note", note);
                    NotePreview notePreview = new NotePreview();
                    notePreview.setArguments(b);
                    ft.add(horizontal.getId(), notePreview);
                }
            }
        }
        ft.commit();

        System.out.println("  > RESUMED.");

    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {

        System.out.println("ElementForm: onCreateOptionsMenu");
        menu.clear();

        if(!element.getParent().isClosed()){

            //super.onCreateOptionsMenu(menu, getActivity().getMenuInflater());
            inflater.inflate(R.menu.element_form, menu);
            if (etype.equals("surface")) {
                if(closingAction){
                    if (element.getEndPoint() == null) {
                        menu.findItem(R.id.element_map_end_point)
                                .setVisible(false);
                    } else {
                        menu.findItem(R.id.element_map_add_end_point)
                                .setVisible(false);
                    }
                }else {
                    menu.findItem(R.id.element_map_end_point).setVisible(false);
                    menu.findItem(R.id.element_map_add_end_point).setVisible(false);
                }
            }else{
                if (element.getEndPoint() == null) {
                    menu.findItem(R.id.element_map_end_point)
                            .setVisible(false);
                } else {
                    menu.findItem(R.id.element_map_add_end_point)
                            .setVisible(false);
                }
            }
        }else{

            inflater.inflate(R.menu.navigator, menu);
            next = menu.findItem(R.id.action_next);
            prev = menu.findItem(R.id.action_prev);
            if(this.element.isSurface()){// Check type
                if (this.element.getParent().getSurface().size()>1){ // Check if more then one

                    int index = this.element.getParent().getSurface().indexOf(this.element);
                    if (index==(this.element.getParent().getSurface().size()-1)){ // Check has next
                        next.setVisible(false);
                    }
                    if (index==0){ // Check has next
                        prev.setVisible(false);
                    }

                }else{
                    next.setVisible(false);
                    prev.setVisible(false);
                }
            }else if(this.element.isLeft()){

            }else if(this.element.isRight()){

            }else if(this.element.isBridge()){

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int index = 0, lastIndex = 0;

        if(this.element.isSurface()){
            index = this.element.getParent().getSurface().indexOf(this.element);
            lastIndex = this.element.getParent().getSurface().size()-1;

        }else if(this.element.isLeft()){
            index = this.element.getParent().getLeftSide().indexOf(this.element);
            lastIndex = this.element.getParent().getLeftSide().size()-1;

        }else if(this.element.isRight()){
            index = this.element.getParent().getRightSide().indexOf(this.element);
            lastIndex = this.element.getParent().getRightSide().size()-1;

        }else if(this.element.isBridge()){
            index = this.element.getParent().getBridge().indexOf(this.element);
            lastIndex = this.element.getParent().getBridge().size()-1;
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>> INDEXES: " + index + "/" + lastIndex);

        stopListeners = true;

        switch (item.getItemId()) {
            case R.id.action_prev:

                if (--index==0){ // Check if is first
                    prev.setVisible(false);
                }else {
                    prev.setVisible(true);
                }
                next.setVisible(true);

                System.out.println(">>>>>>>>>>>>>>>>>>>>>>> Previews element: " + index + "/" + lastIndex);

                this.element = this.element.getParent().getSurface().get(index);

                horizontal.removeAllViews();

                this.onResume();

                map.removeEndPoint();
                map.addEndPoint(element.getEndPoint().getGeometry());
                map.removeStartPoint();
                map.addStartPoint(element.getStartPoint().getGeometry());

                return true;

            case R.id.action_next:

                if (++index==lastIndex){ // Check if is last
                    next.setVisible(false);
                }else {
                    next.setVisible(true);
                }
                prev.setVisible(true);

                System.out.println(">>>>>>>>>>>>>>>>>>>>>>> Next element" + index + "/" + lastIndex);

                this.element = this.element.getParent().getSurface().get(index);


                horizontal.removeAllViews();

                this.onResume();

                if(element.getEndPoint()!=null){
                    map.removeEndPoint();
                    map.addEndPoint(element.getEndPoint().getGeometry());
                }
                map.removeStartPoint();
                map.addStartPoint(element.getStartPoint().getGeometry());

                return true;
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if (view!=null && view.getParent() instanceof Spinner) {

            Spinner spinner = (Spinner) view.getParent();
            KeyValuePair kvp = null;

            /*
                Aggiornamento dati relativi le combo /spinner
             */
            switch (spinner.getId()) {
                case R.id.element_status:
                    kvp = (KeyValuePair) status.getItemAtPosition(i);
                    element.setStatus(kvp.getKey());
                    break;
                case R.id.element_type:
                    kvp = (KeyValuePair) type.getItemAtPosition(i);
                    element.setType(kvp.getKey());
                    break;
            }

            if(kvp!=null) {
                System.out.println("UPDATING ELEMENT OBJECT > " + kvp.getKey() + ": " + kvp.getValue());
            }

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    public boolean isClosing() {
        return closingAction;
    }


    // INITIALIZING FIELDS CHANGE LISTENERS
    private class GenericTextWatcher implements TextWatcher {

        private View view;
        private GenericTextWatcher(View view) {
            this.view = view;
        }
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void afterTextChanged(Editable editable) {

            if(!listenChanges) return;

            String text = editable.toString();

            System.out.println("afterTextChanged: " + text);

            switch(view.getId()){

                case R.id.element_width:
                    element.setWidth(text);
                    break;

                case R.id.element_height:
                    element.setHeight(text);
                    break;

                case R.id.element_start_point_lat:
                    try {
                        element.getStartPoint().setGeometry(new Point(
                                element.getStartPoint().getGeometry().getLon(),
                                Double.parseDouble(startLat.getText().toString())
                        ));
                        // Modifying last point of previews surface if present
                        if(etype.equals("surface")) {
                            int index = element.getParent().getSurface().indexOf(element);
                            if(index>0){
                                element.getParent().getSurface().get(index-1).setEndPoint(element.getStartPoint());
                            }
                        }
                        // update map
                        map.removeStartPoint();
                        map.addStartPoint(element.getStartPoint().getGeometry());
                    }catch (NumberFormatException e){
                        // update map
                        map.removeStartPoint();
                    }
                    break;
                case R.id.element_start_point_lon:
                    try {
                        element.getStartPoint().setGeometry(new Point(
                                Double.parseDouble(startLon.getText().toString()),
                                element.getStartPoint().getGeometry().getLat()
                        ));
                        // Modifying last point of previous surface if present
                        if(etype.equals("surface")) {
                            int index = element.getParent().getSurface().indexOf(element);
                            if(index>0){
                                element.getParent().getSurface().get(index-1).setEndPoint(element.getStartPoint());
                            }
                        }
                        // update map
                        map.removeStartPoint();
                        map.addStartPoint(element.getStartPoint().getGeometry());
                    }catch (NumberFormatException e){
                        // update map
                        map.removeStartPoint();
                    }
                    break;
                case R.id.element_end_point_lat:
                    if (!endLat.getText().toString().equals("")) {
                        if(element.getEndPoint()==null){
                            Position endPosition = new Position();
                            endPosition.setAltitude(0.0);
                            endPosition.setHeading(0.0);
                            endPosition.setGeometry(new Point(0.0,0.0));
                            TimeZone tz = TimeZone.getTimeZone("UTC");
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            df.setTimeZone(tz);
                            endPosition.setTimestamp(df.format(new Date()));
                            element.setEndPoint(endPosition);
                        }
                        try {
                            element.getEndPoint().setGeometry(new Point(
                                    element.getEndPoint().getGeometry().getLon(),
                                    Double.parseDouble(endLat.getText().toString())
                            ));
                            // Modifying last point of next surface if present
                            if(etype.equals("surface")) {
                                int index = element.getParent().getSurface().indexOf(element);
                                if(index<(element.getParent().getSurface().size()-1)){
                                    element.getParent().getSurface().get(index+1).setStartPoint(element.getEndPoint());
                                }
                            }
                            // update map
                            map.removeEndPoint();
                            map.addEndPoint(element.getEndPoint().getGeometry());
                        }catch (NumberFormatException e){
                            // update map
                            map.removeEndPoint();
                        }
                    }
                    break;
                case R.id.element_end_point_lon:
                    if (!endLon.getText().toString().equals("")) {
                        if(element.getEndPoint()==null){
                            Position endPosition = new Position();
                            endPosition.setAltitude(0.0);
                            endPosition.setHeading(0.0);
                            endPosition.setGeometry(new Point(0.0,0.0));
                            TimeZone tz = TimeZone.getTimeZone("UTC");
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            df.setTimeZone(tz);
                            endPosition.setTimestamp(df.format(new Date()));
                            element.setEndPoint(endPosition);
                        }
                        try {
                            element.getEndPoint().setGeometry(new Point(
                                    Double.parseDouble(endLon.getText().toString()),
                                    element.getEndPoint().getGeometry().getLat()
                            ));
                            // Modifying last point of next surface if present
                            if(etype.equals("surface")) {
                                int index = element.getParent().getSurface().indexOf(element);
                                if(index<(element.getParent().getSurface().size()-1)){
                                    element.getParent().getSurface().get(index+1).setStartPoint(element.getEndPoint());
                                }
                            }
                            // update map
                            map.removeEndPoint();
                            map.addEndPoint(element.getEndPoint().getGeometry());
                        }catch (NumberFormatException e){
                            // update map
                            map.removeEndPoint();
                        }
                    }
                    break;
            }
        }
    }
}
