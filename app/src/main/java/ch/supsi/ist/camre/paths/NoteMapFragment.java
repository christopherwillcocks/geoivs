package ch.supsi.ist.camre.paths;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import ch.supsi.ist.camre.paths.data.Element;
import ch.supsi.ist.camre.paths.data.Note;
import ch.supsi.ist.camre.paths.data.Point;
import ch.supsi.ist.camre.paths.data.Position;

/**
 * Created by milan antonovic on 06/10/14.
 */
public class NoteMapFragment extends MapFragment {

    private Point point;
    private Note note;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);
        //setHasOptionsMenu(true);

        if (bundle != null) {
            note = (Note)bundle.getSerializable("Note");
            point = note.getPoint().getGeometry();
        }else{
            note = new Note();
        }

        Button locateStart = (Button)inflater.inflate(
                R.layout.button_map_locate_note,
                (ViewGroup)view.findViewById(R.id.map_container)
        ).findViewById(R.id.map_locate_note);

        locateStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webview.loadUrl("javascript:panto("+point.getLon()+","+point.getLat()+");");
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        //menu.clear();
        if(!note.getElement().getParent().isClosed()){
            super.onCreateOptionsMenu(menu, getActivity().getMenuInflater());
            inflater.inflate(R.menu.note_map, menu);
        }
    }

    @Override
    public void onMapLoaded() {
        super.onMapLoaded();

        Element element = note.getElement();

        if(point != null){
            addPoint(point, "note");
            addPoint(element.getStartPoint().getGeometry(), "start");
            if(element!=null && element.getEndPoint()!=null) {
                addPoint(element.getEndPoint().getGeometry(), "end");
                zoomToExtent();
                addLine(element.getParent().getGeometry(), "surveyPath");
            }else{
                addLine(element.getParent().getGeometry(), "surveyPath");
                zoomToExtent();
            }
        }else if(element!=null && element.getParent().getGeometry()!=null){
            addLine(element.getParent().getGeometry(), "surveyPath");
            zoomToExtent();
        }

        /*if (bundle != null) {
            System.out.println("Note received: ");
            note = (Note)bundle.getSerializable("Note");

            ObjectMapper om = new ObjectMapper();
            try {
                System.out.println(om.writeValueAsString(note));
            } catch (IOException e) {
                e.printStackTrace();
            }
            point = note.getPoint().getGeometry();
            addPoint(point, "note");

            Element element = note.getElement();
            addPoint(element.getStartPoint().getGeometry(), "start");
            if(element.getEndPoint()!=null) {
                addPoint(element.getEndPoint().getGeometry(), "end");
            }

        }else{
            note = new Note();
        }*/
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    @Override
    @JavascriptInterface
    public void updateCenter(String newCenter){
        super.updateCenter(newCenter);
        /*Point center = getCenter();
        note.getPoint().setGeometry(center);
        System.out.println("Center found: " + center.getLat() + " / " + center.getLon());*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        System.out.println("onOptionsItemSelected!!!");

        switch (item.getItemId()) {
            case R.id.note_map_note_point:

                point = getCenter();
                removePoint("note");
                addPoint(point,"note");
                //element.getStartPoint().setGeometry(center);
                return true;


            case R.id.note_map_action_save:

                //element.getStartPoint().setGeometry(start);
                if(point!=null){
                    if(note.getPoint()==null){
                        Position position = new Position();
                        position.setAltitude(0.0);
                        position.setHeading(0.0);
                        position.setGeometry(new Point(point.getLon(), point.getLat()));
                        TimeZone tz = TimeZone.getTimeZone("UTC");
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        df.setTimeZone(tz);
                        position.setTimestamp(df.format(new Date()));
                        note.setPoint(position);
                    }
                    note.getPoint().setGeometry(point);
                }
                note.getElement().getParent().notifyChanges();
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
