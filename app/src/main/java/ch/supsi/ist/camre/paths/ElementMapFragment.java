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
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ch.supsi.ist.camre.paths.data.Element;
import ch.supsi.ist.camre.paths.data.Point;
import ch.supsi.ist.camre.paths.data.Position;

/**
 * Created by milan antonovic on 06/10/14.
 */
public class ElementMapFragment extends MapFragment {

    public static final int STARTPOINTMOVE = 1;
    public static final int ENDPOINTMOVE = 2;

    private Element element;
    private Menu menu;
    private Point start;
    private Point end;

    private Button locateEnd;
    private Button locateStart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = super.onCreateView(inflater,container,savedInstanceState);

        if (bundle != null) {
            element = (Element)bundle.getSerializable("Element");
            start = element.getStartPoint().getGeometry();
            if(element.getEndPoint()!=null) {
                end = element.getEndPoint().getGeometry();
            }
        }else{
            element = new Element();
        }

        setHasOptionsMenu(true);

        FrameLayout mc = (FrameLayout)view.findViewById(R.id.map_container);

        locateStart = (Button)inflater.inflate(
                R.layout.button_map_locate_start,
                (ViewGroup)view.findViewById(R.id.map_container)
        ).findViewById(R.id.map_locate_start);

        locateStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webview.loadUrl("javascript:panto("+start.getLon()+","+start.getLat()+");");
            }
        });

        locateEnd = (Button)inflater.inflate(
                R.layout.button_map_locate_end,
                (ViewGroup)view.findViewById(R.id.map_container)
        ).findViewById(R.id.map_locate_end);

        locateEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webview.loadUrl("javascript:panto("+end.getLon()+","+end.getLat()+");");
            }
        });

        if(element.getEndPoint()==null) {
            //MenuItem item = this.menu.findItem(R.id.element_map_end_point);
            //item.setVisible(false);
            locateEnd.setVisibility(View.INVISIBLE);
        }else{
            //MenuItem item = this.menu.findItem(R.id.element_map_add_end_point);
            //item.setVisible(false);
            locateEnd.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onMapLoaded() {
        super.onMapLoaded();
        if (start != null) {
            addPoint(start, "start");
            if(end != null) {
                addPoint(end, "end");
            }
            zoomToExtent();
            addLine(element.getParent().getGeometry(), "surveyPath");
        } else if(element.getParent().getGeometry()!=null){
            addLine(element.getParent().getGeometry(), "surveyPath");
            zoomToExtent();
        }
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    @Override
    @JavascriptInterface
    public void updateCenter(String newCenter){
        super.updateCenter(newCenter);
    }

    /**
     *
     * Add the START point
     *
     * @param point
     */
    protected void addStartPoint(Point point){
        start = point;
        addPoint(point,"start");
        locateStart.setVisibility(View.VISIBLE);
    }

    /**
     * Remove the START point
     *
     */
    protected void removeStartPoint(){
        removePoint("start");
        locateStart.setVisibility(View.INVISIBLE);
    }

    /**
     *
     * Add the END point
     *
     * @param point
     */
    protected void addEndPoint(Point point){
        end = point;
        addPoint(point,"end");
        locateEnd.setVisibility(View.VISIBLE);
    }

    /**
     * Remove the END point
     *
     */
    protected void removeEndPoint(){
        removePoint("end");
        locateEnd.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.element_map_start_point:

                start = getCenter();
                removePoint("start");
                addPoint(start,"start");

                if(mListener!=null){
                    mListener.onMapInteraction(STARTPOINTMOVE, start);
                }

                return true;

            case R.id.element_map_end_point:

                end = getCenter();
                removePoint("end");
                addPoint(end,"end");

                if(mListener!=null){
                    mListener.onMapInteraction(ENDPOINTMOVE, end);
                }

                return true;

            case R.id.element_map_add_end_point:

                end = getCenter();
                removePoint("end");
                addPoint(end,"end");

                if(mListener!=null){
                    mListener.onMapInteraction(ENDPOINTMOVE, end);
                }

                locateEnd.setVisibility(View.VISIBLE);

                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
