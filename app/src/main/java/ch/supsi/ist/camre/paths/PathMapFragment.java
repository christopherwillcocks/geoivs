package ch.supsi.ist.camre.paths;

import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;

import ch.supsi.ist.camre.paths.data.Element;
import ch.supsi.ist.camre.paths.data.Note;
import ch.supsi.ist.camre.paths.data.Path;
import ch.supsi.ist.camre.paths.data.Point;
import ch.supsi.ist.camre.paths.utils.VIEW;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class PathMapFragment  extends MapFragment{

    Path path;

    public PathMapFragment() {}

    @Override
    public void onMapLoaded() {
        super.onMapLoaded();
        if (bundle != null) {
            path = (Path)bundle.getSerializable("Path");
            if(path.getGeometry()!=null){
                addLine(path.getGeometry(), "surveyPath");
                //MultiPoint line = (MultiPoint)path.getGeometry().getLineString().getBoundary();
                zoomToExtent();
            }
        }else{
            path = new Path();
        }

        WalkerActivityFooter worker = (WalkerActivityFooter)getFragmentManager()
                .findFragmentByTag(VIEW.LOCATION_WORKER.toString());

        worker.addCustomGpsListener(this);
        worker.addFusionLocationListener(this);
        //worker.connectGPS();
    }


}
