package ch.supsi.ist.camre.paths;

import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import ch.supsi.ist.camre.paths.adapters.KeyValuePairAdapter;
import ch.supsi.ist.camre.paths.data.KeyValuePair;
import ch.supsi.ist.camre.paths.data.Path;


public class PathForm extends Fragment
        implements
            AdapterView.OnItemSelectedListener,
            CompoundButton.OnCheckedChangeListener{

    private Path path;

    private EditText name;
    private EditText path_path;

    private Spinner status;
    private Spinner difficulty;
    private Spinner protection;
    private Spinner risk;
    private Spinner hazard;
    private Spinner activity;

    private Switch circular;

    private class GenericTextWatcher implements TextWatcher{

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
                    break;
                case R.id.path_path:
                    path.setPath(text);
                    break;
            }
        }
    }

    public PathForm() {
        // Required empty public constructor
        System.out.println("PathForm: constructor called");
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

        System.out.println("PathForm: onCreate called");

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            path = (Path)bundle.getSerializable("Path");
            System.out.println("  - Bundle with path received ("+path.getName()+") " + this.toString());
        }else{
            path = new Path();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        System.out.println("PathForm: onCreateView called");

        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_path_form, container, false);

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

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            path = (Path)bundle.getSerializable("Path");
            System.out.println("  - Bundle with path received ("+path.getName()+") " + this.toString());
        }else{
            path = new Path();
        }

        //this.setRetainInstance(false);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        System.out.println("PathForm: onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putSerializable("Path", path);
    }

    @Override
    public void onResume(){
        super.onResume();

        System.out.println("PathForm: onResumed (" + path.getName() + ") " + this.toString());

        name.setText(path.getName());
        path_path.setText(path.getPath());
        status.setSelection(
                ((KeyValuePairAdapter)status.getAdapter()).
                        indexOf(path.getStatus())
        );
        difficulty.setSelection(
                ((KeyValuePairAdapter)difficulty.getAdapter()).
                        indexOf(path.getCharacteristics().getDifficulty())
        );
        protection.setSelection(
                ((KeyValuePairAdapter)protection.getAdapter()).
                        indexOf(path.getCharacteristics().getProtection())
        );
        risk.setSelection(
                ((KeyValuePairAdapter)risk.getAdapter()).
                        indexOf(path.getCharacteristics().getRisk())
        );
        hazard.setSelection(
                ((KeyValuePairAdapter)hazard.getAdapter()).
                        indexOf(path.getCharacteristics().getHazard())
        );
        activity.setSelection(
                ((KeyValuePairAdapter)activity.getAdapter()).
                        indexOf(path.getCharacteristics().getActivity())
        );
        circular.setChecked(path.getCharacteristics().getCircular());

        // Init listeners

        name.addTextChangedListener(new GenericTextWatcher(name));
        path_path.addTextChangedListener(new GenericTextWatcher(path_path));
        status.setOnItemSelectedListener(this);
        difficulty.setOnItemSelectedListener(this);
        protection.setOnItemSelectedListener(this);
        risk.setOnItemSelectedListener(this);
        hazard.setOnItemSelectedListener(this);
        circular.setOnCheckedChangeListener(this);

    }

    /*
        LISTEN FOR CLICK IN (HEADER) OPTION MENU
     */
    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_done:

                //Toast.makeText(this.getActivity(), "Saving path..", Toast.LENGTH_SHORT).show();

                //getActivity().getFragmentManager().executePendingTransactions();

                path.notifyChanges();

                return true;
            default:
                return false;
        }

    }*/

    /*
        LISTEN FOR SWITCH ACTION
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){

        //System.out.println("Switch isChecked? " + isChecked);

        if (buttonView instanceof Switch) {
            switch (buttonView.getId()) {
                case R.id.path_characteristics_circular:
                    path.getCharacteristics().setCircular(isChecked);
            }
        }

    }

    /*
        LISTEN FOR SPINNER ITEM SELECTIONS
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if (view!=null && view.getParent() instanceof Spinner) {
            Spinner spinner = (Spinner) view.getParent();
            KeyValuePair kvp = null;
            switch (spinner.getId()) {
                case R.id.path_status:
                    kvp = (KeyValuePair) status.getItemAtPosition(i);
                    path.setStatus(kvp.getKey());
                    break;
                case R.id.path_characteristics_difficulty:
                    kvp = (KeyValuePair) difficulty.getItemAtPosition(i);
                    path.getCharacteristics().setDifficulty(kvp.getKey());
                    break;
                case R.id.path_characteristics_protection:
                    kvp = (KeyValuePair) protection.getItemAtPosition(i);
                    path.getCharacteristics().setProtection(kvp.getKey());
                    break;
                case R.id.path_characteristics_risk:
                    kvp = (KeyValuePair) risk.getItemAtPosition(i);
                    path.getCharacteristics().setRisk(kvp.getKey());
                    break;
                case R.id.path_characteristics_hazard:
                    kvp = (KeyValuePair) hazard.getItemAtPosition(i);
                    path.getCharacteristics().setHazard(kvp.getKey());
                    break;
                case R.id.path_characteristics_activity:
                    kvp = (KeyValuePair) activity.getItemAtPosition(i);
                    path.getCharacteristics().setActivity(kvp.getKey());
                    break;
            }
            /*if(kvp!=null) {
                System.out.println(" > " + kvp.getKey() + ": " + kvp.getValue());
            }*/
        }
    }

    /*
        LISTEN FOR SPINNER ITEM NOT SELECTED
     */
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

    /*
        DECLARE PERSONAL OPTION MENU
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.route_form, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

}