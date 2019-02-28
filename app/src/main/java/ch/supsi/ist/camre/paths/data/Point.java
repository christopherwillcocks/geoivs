package ch.supsi.ist.camre.paths.data;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Milan Antonovic on 22/08/14.
 */
public class Point implements Serializable {

    private String type = "Point";
    private ArrayList<Double> coordinates;

    public Point(){}

    public Point(Double lon, Double lat){
        coordinates = new ArrayList<Double>();
        coordinates.add(lon);
        coordinates.add(lat);
    }

    public Point(ArrayList<Double> coordinates){
        this.coordinates = coordinates;
    }

    public String getType() {
        return type;
    }

    /**
     * Return an ArrayList of two Doubles containing the (Lon,Lat / E,N) coordinates in EPSG:4326
     *
     * @return ArrayList<Double>
     */
    public ArrayList<Double> getCoordinates() {
        return coordinates;
    }

    @JsonIgnore
    public Double getLat(){
        return coordinates.get(1);
    }

    @JsonIgnore
    public Double getLon(){
        return coordinates.get(0);
    }

    public void setCoordinates(ArrayList<Double> coordinates) {
        this.coordinates = coordinates;
    }

}
