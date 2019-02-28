package ch.supsi.ist.camre.paths;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.linearref.LengthLocationMap;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

import org.codehaus.jackson.map.ObjectMapper;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import ch.supsi.ist.camre.paths.data.Path;
import ch.supsi.ist.camre.paths.data.SimpleElement;

public class TestFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GeolocationWorker geoloaGeolocationWorker;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TestFragment newInstance(String param1, String param2) {
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public TestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_test, container, false);


        Button b = (Button) view.findViewById(R.id.btnTest1);
        b.setOnClickListener(this);
        b = (Button) view.findViewById(R.id.btnTest2);
        b.setOnClickListener(this);
        b = (Button) view.findViewById(R.id.btnTest3);
        b.setOnClickListener(this);
        b = (Button) view.findViewById(R.id.btnTest4);
        b.setOnClickListener(this);
        b = (Button) view.findViewById(R.id.btnTest5);
        b.setOnClickListener(this);
        b = (Button) view.findViewById(R.id.btnTest6);
        b.setOnClickListener(this);
        b = (Button) view.findViewById(R.id.btnTest7);
        b.setOnClickListener(this);
        b = (Button) view.findViewById(R.id.btnTest8);
        b.setOnClickListener(this);
        b = (Button) view.findViewById(R.id.btnTest9);
        b.setOnClickListener(this);
        b = (Button) view.findViewById(R.id.btnTest10);
        b.setOnClickListener(this);
        b = (Button) view.findViewById(R.id.btnTest11);
        b.setOnClickListener(this);
        b = (Button) view.findViewById(R.id.btnTest12);
        b.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        System.out.println("CLICK");

        switch (view.getId()) {
            case R.id.btnTest1:
                test1();
                break;
            case R.id.btnTest2:
                test2();
                break;
            case R.id.btnTest3:
                test3();
                break;
            case R.id.btnTest4:
                test4();
                break;
            case R.id.btnTest5:
                test5();
                break;
            case R.id.btnTest6:
                test6();
                break;
            case R.id.btnTest7:
                test7();
                break;
            case R.id.btnTest8:
                test8();
                break;
            case R.id.btnTest9:
                test9();
                break;
            case R.id.btnTest10:
                test10();
                break;
            case R.id.btnTest11:
                test11();
                break;
            case R.id.btnTest12:
                test12();
                break;
        }
    }

    private void test1() {

        SimpleElement simpleElement;

        String json = "{\n" +
            "   \"title\":\"title 1\",\n" +
            "   \"description\":\"description 1\",\n" +
            "   \"subtitle\":\"subtitle 1\",\n" +
            "   \"type\":\"type 1\"\n" +
            "}";

        ObjectMapper mapper = new ObjectMapper();
        try {
            simpleElement = mapper.readValue(json, SimpleElement.class);
            System.out.println("Conversion ok!");
            System.out.println("SimpleElement: " + simpleElement.getTitle() + " " + simpleElement.getDescription());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void test2() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.test_content_frame, new SimpleElementListFragment())
                .commit();

    }

    private void test3() {

        Path path;
        String json = "{\n" +
                "   \"name\":\"name value\",\n" +
                "   \"path\":\"path value\",\n" +
                "   \"geometry\": { \n" +
                "       \"type\":\"LineString\",\n" +
                "       \"coordinates\": [[0,1],[1,2],[3,4],[5,6]]\n" +
                "    },\n" +
                "   \"characteristics\": {\n"+
                "       \"difficulty\": \"www.camre.ch/difficulty/T1\","+
                "       \"protection\": \"www.camre.ch/protection/sidewalk\","+
                "       \"risk\": \"www.camre.ch/risk/traffic\","+
                "       \"hazard\": \"www.camre.ch/hazard/low\","+
                "       \"circular\": \"false\","+
                "       \"activity\": \"www.camre.ch/activity/hicking\""+
                "   },\n"+
                "   \"leftSide\": [\n" +
                "       {\n"+
                "           \"type\":\"www.camre.ch/properties/scarp/terrain/down\""+
                "       },\n"+
                "       {\n"+
                "           \"type\":\"www.camre.ch/properties/scarp/terrain/up\""+
                "       }\n"+
                "   ],\n" +
                "   \"rightSide\": [\n" +
                "       {\n"+
                "           \"type\":\"www.camre.ch/properties/scarp/terrain/up\""+
                "       },\n"+
                "       {\n"+
                "           \"type\":\"www.camre.ch/properties/scarp/terrain/down\""+
                "       }\n"+
                "   ]" +
                "}";

        System.out.println("JSON: <<<<<<<<<<<<<<<<<<<<<<<<<<<<\n" +json  );

        ObjectMapper mapper = new ObjectMapper();
        try {
            path = mapper.readValue(json, Path.class);
            System.out.println("Conversion ok!");
            System.out.println(" - Path: " + path.getName() );
            System.out.println(" - Left: " + path.getLeftSide().size() + " elements" );
            System.out.println(" - Right: " + path.getRightSide().size() + " elements" );
            System.out.println("JSON JSON JSON JSON JSON JSON JSON JSON JSON JSON JSON ");
            System.out.println(mapper.writeValueAsString(path));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void test4() {
        Database database = ((Application)getActivity().getApplication()).database;
        ObjectMapper mapper = new ObjectMapper();

        Query query = database.createAllDocumentsQuery();
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        QueryEnumerator result = null;
        try {
            result = query.run();
            System.out.println("Results found: " + result.getCount());
            Path path;
            Map<String, Object> map;
            Document doc;
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();

                System.out.println("Doc ID: " + row.getDocumentId());

                doc = row.getDocument();

                map = doc.getProperties();

                try {
                    path = mapper.readValue(
                            mapper.writeValueAsString(doc.getProperties()), Path.class);

                    if (path!=null){
                        System.out.println("Path name: " + path.getName());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }



    private void test5() {

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.test_content_frame, new PathListFragment())
                .commit();
    }


    private void test6() {

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.test_content_frame, new TestAnimatorFragment())
                .commit();

    }

    private void test7() {

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.test_content_frame, new MapFragment())
                .commit();

    }


    private void test8() {

        Database database = ((Application)getActivity().getApplication()).database;
        ObjectMapper mapper = new ObjectMapper();

        Query query = database.createAllDocumentsQuery();
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        QueryEnumerator result = null;
        try {
            result = query.run();
            Path path;
            Map<String, Object> map;
            Document doc;
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();

                System.out.println("Doc ID: " + row.getDocumentId());

                doc = row.getDocument();

                map = doc.getProperties();

                try {
                    path = mapper.readValue(
                            mapper.writeValueAsString(doc.getProperties()), Path.class);

                    if (path!=null){
                        System.out.println("Path name: " + path.getName());
                        Intent intent = new Intent(getActivity(), WalkerActivityTest.class);
                        intent.putExtra("Path", path);
                        startActivity(intent);
                        break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        /*
        Path path = new Path();
        path.setName("TEST 8");
        Intent intent = new Intent(getActivity(), WalkerActivityTest.class);
        intent.putExtra("Path", path);
        startActivity(intent);*/

    }


    private void test9() {

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.test_content_frame, new Login())
                .commit();

    }

    private void test10(){

        /*Fragment f = getFragmentManager()
                .findFragmentByTag("GeolocationWorker");

        if(f!=null || f.isDetached()){ // is paused
            getFragmentManager().beginTransaction().attach(f).commit();
        }else{

        }*/

        geoloaGeolocationWorker = new GeolocationWorker();

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .attach(geoloaGeolocationWorker)
                .commit();

    }


    private void test11(){

        Database database = ((Application)getActivity().getApplication()).database;
        ObjectMapper mapper = new ObjectMapper();

        Query query = database.createAllDocumentsQuery();
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        QueryEnumerator result = null;
        try {
            result = query.run();
            System.out.println("Results found: " + result.getCount());
            Path path;
            Map<String, Object> map;
            Document doc;
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();

                System.out.println("Doc ID: " + row.getDocumentId());

                doc = row.getDocument();

                map = doc.getProperties();

                try {
                    path = mapper.readValue(
                            mapper.writeValueAsString(doc.getProperties()), Path.class);

                    if (path!=null){
                        System.out.println("Path name: " + path.getName());
                        System.out.println("Path BBOX: " + path.getGeometry().getLineString().getBoundary());
                        //CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private void test12() {
        String lineString = "LINESTRING (8.961447476264043 46.02754458589287,8.96141456863371 46.02798589301621,8.961373731309566 46.028490287677954,8.961401716444293 46.02868334873935,8.96155960395605 46.02901860800806,8.961431455604435 46.029171043945894,8.961745542880353 46.02954917650945)";
        String pointString = "POINT (8.961527988503681 46.0285348203348)";
        WKTReader rdr = new WKTReader(new GeometryFactory());
        try {

            Geometry line = rdr.read(lineString);
            Geometry point = rdr.read(pointString);

            LocationIndexedLine lineRef = new LocationIndexedLine(line);
            double lineLength = line.getLength();

            System.out.println("Lenght: " + lineLength);

            LinearLocation loc = lineRef.project(point.getCoordinate());
            LengthLocationMap locationMap = new LengthLocationMap(line);

            double distanceAlong = locationMap.getLength(loc);
            double percentAlong = distanceAlong / lineLength;

            System.out.println("Distance: " + distanceAlong);
            System.out.println("Percent: " + percentAlong);

            Coordinate coordinate = lineRef.extractPoint(loc);

            System.out.println("Coords: " + coordinate.toString());

            //CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");

        } catch (ParseException e) {

            e.printStackTrace();

        }
    }



}
