package ch.supsi.ist.camre.paths;



import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.linearref.LengthLocationMap;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import ch.supsi.ist.camre.paths.data.DataUtils;
import ch.supsi.ist.camre.paths.data.Element;
import ch.supsi.ist.camre.paths.data.OverviewElement;
import ch.supsi.ist.camre.paths.data.Path;
import ch.supsi.ist.camre.paths.utils.VIEW;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class PathViewer extends Fragment implements PathViewerSurfaceView.OnElementSelectedListener {

    PathViewerSurfaceView surfaceView;
    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener;
    //private SeekBar seekBar;
    private ProgressBar seekBar;

    Proj4js proj;

    private Path path;

    private View detailsView;

    private HashMap<String,String> dictionary;

    private View fragment_path_viewer;

    public PathViewer() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragment_path_viewer = inflater.inflate(R.layout.fragment_path_viewer, container, false);

        setHasOptionsMenu(true);

        dictionary = DataUtils.getDictionary(getActivity());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            path = (Path)bundle.getSerializable("Path");
        } else {
            path = new Path();
        }


        ((TextView) fragment_path_viewer.findViewById(R.id.path_name))
                .setText(path.getName());
        ((TextView)fragment_path_viewer.findViewById(R.id.path_characteristics_difficulty))
                .setText(path.getCharacteristics().getDifficulty());

        detailsView = fragment_path_viewer.findViewById(R.id.element_details);

        surfaceView = (PathViewerSurfaceView)fragment_path_viewer.findViewById(R.id.path_viewer_drawing_surface);
        surfaceView.setPath(path);
        surfaceView.setElementSelectedListener(this);

        seekBar = (ProgressBar)fragment_path_viewer.findViewById(R.id.seekBar);
        mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress;
                //Toast.makeText(PathViewer.this.getActivity(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
                surfaceView.centerTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(PathViewer.this.getActivity(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(PathViewer.this.getActivity(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }
        };


        proj = new Proj4js();

        /*proj = (Proj4js)getFragmentManager()
                .findFragmentById(R.id.path_viewer_proj);*/

        proj.registerProjReady(new Proj4js.ProjListener() {
            @Override
            public void onReady() {
                proj.transformPath(path);
            }
            @Override
            public void onPathReady(JSONObject result){

                System.out.println("resultPoints: " + result.toString());

                StringBuilder lineString = new StringBuilder();

                try {
                    lineString.append("LINESTRING (");
                    for (int i = 0; i < result.getJSONArray("geometry").length(); i++) {
                        JSONArray coord = result.getJSONArray("geometry").getJSONArray(i);
                        lineString
                                .append(coord.getDouble(0))
                                .append(" ")
                                .append(coord.getDouble(1));
                        if((i+1) < result.getJSONArray("geometry").length()){
                            lineString.append(",");
                        }
                    }
                    lineString.append(")");

                    WKTReader rdr = new WKTReader(new GeometryFactory());
                    Geometry line = rdr.read(lineString.toString());
                    LocationIndexedLine lineRef = new LocationIndexedLine(line);
                    LengthLocationMap locationMap = new LengthLocationMap(line);

                    double lineLength = line.getLength();

                    seekBar.setMax((int)lineLength);
                    seekBar.setProgress((int)(lineLength/2));
                    //seekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

                    surfaceView.setLength(lineLength);

                    System.out.println("\nSurface SIDE: " + result.getJSONArray("surface").length());

                    for (int i = 0; i < result.getJSONArray("surface").length(); i++) {

                        StringBuilder coord = new StringBuilder();

                        StringBuilder coordEnd = new StringBuilder();
                        StringBuilder coordStart = new StringBuilder();

                        JSONArray start = result.getJSONArray("surface").getJSONArray(i).getJSONArray(0);

                        coordStart.append("POINT (")
                                .append(start.getDouble(0))
                                .append(" ")
                                .append(start.getDouble(1))
                                .append(")");

                        Geometry pointStart = rdr.read(coordStart.toString());
                        LinearLocation locStart = lineRef.project(pointStart.getCoordinate());
                        double metersFrom = locationMap.getLength(locStart);
                        double percentFrom = metersFrom / lineLength;

                        if(result.getJSONArray("surface").getJSONArray(i).get(1)==JSONObject.NULL){

                            surfaceView.addSurface(new OverviewElement(path.getSurface().get(i),
                                    (float)metersFrom, (float)lineLength,
                                    (float)percentFrom, 1.f));
                        }else{

                            JSONArray end = result.getJSONArray("surface").getJSONArray(i).getJSONArray(1);
                            coordEnd.append("POINT (")
                                    .append(end.getDouble(0))
                                    .append(" ")
                                    .append(end.getDouble(1))
                                    .append(")");

                            Geometry pointEnd = rdr.read(coordEnd.toString());
                            LinearLocation locEnd = lineRef.project(pointEnd.getCoordinate());
                            double metersTo = locationMap.getLength(locEnd);
                            double percentTo = metersTo / lineLength;

                            System.out.println("Distance: "+ metersFrom + " - " + metersTo);
                            System.out.println("Percent: "+ percentFrom + " - " + percentTo);

                            surfaceView.addSurface(new OverviewElement(path.getSurface().get(i),
                                    (float)metersFrom, (float)metersTo,
                                    (float)percentFrom, (float)percentTo));

                        }

                    }

                    System.out.println("\nLeft SIDE: " + result.getJSONArray("leftside").length());


                    for (int i = 0; i < result.getJSONArray("leftside").length(); i++) {

                        StringBuilder coordEnd = new StringBuilder();
                        StringBuilder coordStart = new StringBuilder();


                        JSONArray start = result.getJSONArray("leftside").getJSONArray(i).getJSONArray(0);

                        coordStart.append("POINT (")
                                .append(start.getDouble(0))
                                .append(" ")
                                .append(start.getDouble(1))
                                .append(")");

                        Geometry pointStart = rdr.read(coordStart.toString());
                        LinearLocation locStart = lineRef.project(pointStart.getCoordinate());
                        double metersFrom = locationMap.getLength(locStart);
                        double percentFrom = metersFrom / lineLength;

                        if(result.getJSONArray("leftside").getJSONArray(i).get(1)==JSONObject.NULL){

                            surfaceView.addLeftSide(new OverviewElement(path.getLeftSide().get(i),
                                    (float) metersFrom, (float) lineLength,
                                    (float) percentFrom, 1.f));

                        }else {
                            JSONArray end = result.getJSONArray("leftside").getJSONArray(i).getJSONArray(1);
                            coordEnd.append("POINT (")
                                    .append(end.getDouble(0))
                                    .append(" ")
                                    .append(end.getDouble(1))
                                    .append(")");

                            Geometry pointEnd = rdr.read(coordEnd.toString());
                            LinearLocation locEnd = lineRef.project(pointEnd.getCoordinate());
                            double metersTo = locationMap.getLength(locEnd);
                            double percentTo = metersTo / lineLength;

                            System.out.println("Distance: " + metersFrom + " - " + metersTo);
                            System.out.println("Percent: " + percentFrom + " - " + percentTo);

                            surfaceView.addLeftSide(new OverviewElement(path.getLeftSide().get(i),
                                    (float) metersFrom, (float) metersTo,
                                    (float) percentFrom, (float) percentTo));
                        }


                    }

                    System.out.println("\nRight SIDE: " + result.getJSONArray("rightside").length());

                    for (int i = 0; i < result.getJSONArray("rightside").length(); i++) {

                        StringBuilder coordEnd = new StringBuilder();
                        StringBuilder coordStart = new StringBuilder();


                        JSONArray start = result.getJSONArray("rightside").getJSONArray(i).getJSONArray(0);
                        coordStart.append("POINT (")
                                .append(start.getDouble(0))
                                .append(" ")
                                .append(start.getDouble(1))
                                .append(")");

                        Geometry pointStart = rdr.read(coordStart.toString());
                        LinearLocation locStart = lineRef.project(pointStart.getCoordinate());
                        double metersFrom = locationMap.getLength(locStart);
                        double percentFrom = metersFrom / lineLength;

                        if(result.getJSONArray("rightside").getJSONArray(i).get(1)==JSONObject.NULL) {

                            surfaceView.addRightSide(new OverviewElement(path.getRightSide().get(i),
                                    (float) metersFrom, (float) lineLength,
                                    (float) percentFrom, 1.f));

                        }else {

                            JSONArray end = result.getJSONArray("rightside").getJSONArray(i).getJSONArray(1);
                            coordEnd.append("POINT (")
                                    .append(end.getDouble(0))
                                    .append(" ")
                                    .append(end.getDouble(1))
                                    .append(")");

                            Geometry pointEnd = rdr.read(coordEnd.toString());
                            LinearLocation locEnd = lineRef.project(pointEnd.getCoordinate());
                            double metersTo = locationMap.getLength(locEnd);
                            double percentTo = metersTo / lineLength;

                            System.out.println("Distance: " + metersFrom + " - " + metersTo);
                            System.out.println("Percent: " + percentFrom + " - " + percentTo);

                            surfaceView.addRightSide(new OverviewElement(path.getRightSide().get(i),
                                    (float) metersFrom, (float) metersTo,
                                    (float) percentFrom, (float) percentTo));
                        }


                    }

                    //surfaceView.postInvalidate();

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.path_viewer_proj, proj, "PROJ");
        ft.commit();

        Button test = (Button)fragment_path_viewer.findViewById(R.id.button_details);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //surfaceView.gameLoop.setRunning(false);
                Bundle bundle = new Bundle();
                if (path.getSurface().contains(element)){
                    bundle.putString("type", "surface");
                }else if (path.getLeftSide().contains(element)){
                    bundle.putString("type", "side");
                }else if (path.getRightSide().contains(element)){
                    bundle.putString("type", "side");
                }else if (path.getBridge().contains(element)){
                    bundle.putString("type", "bridge");
                }
                bundle.putSerializable("Element", element);
                ((WalkerActivityTest)getActivity()).openFragment(
                        VIEW.ELEMENT, bundle);
            }
        });

        return fragment_path_viewer;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if(!path.isClosed()){
            inflater.inflate(R.menu.walker_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private Element element;
    @Override
    public void onElementSelected(OverviewElement element) {

        this.element = element.getElement();

        ((TextView)detailsView.findViewById(R.id.element_name))
                .setText(dictionary.get(this.element.getType()));

        ((TextView)detailsView.findViewById(R.id.element_length))
                .setText(String.valueOf(element.getMeters()));

        ((TextView)detailsView.findViewById(R.id.element_notes_count))
                .setText(String.valueOf(this.element.getNotes().size()));

        detailsView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onElementDeSelected() {
        detailsView.setVisibility(View.GONE);
    }

    @Override
    public void onOverviewCentered(int meters) {
        //seekBar.setOnSeekBarChangeListener(null);

        if(meters<0)
            seekBar.setVisibility(View.INVISIBLE);
        else
            seekBar.setVisibility(View.VISIBLE);

        seekBar.setProgress(meters);
        //seekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
    }

    @Override
    public void onReady() {
        ((TextView)fragment_path_viewer.findViewById(R.id.full_path_lenght)).setText(String.valueOf(surfaceView.getLength()));
    }

    @Override
    public void onPause(){
        super.onPause();
        getFragmentManager()
                .beginTransaction()
                .remove(proj)
                .commit();
    }
}
