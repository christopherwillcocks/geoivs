package ch.supsi.ist.camre.paths.data;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.linearref.LinearGeometryBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by milan antonovic on 22/08/14.
 */
public class Linestring implements Serializable {

    private String type = "LineString";

    private ArrayList<ArrayList<Double>> coordinates;

    //private ArrayList<Coordinate> coordinatesJts;
    //private LinearGeometryBuilder builder;

    public Linestring(){
        coordinates = new ArrayList<ArrayList<Double>>();
        //coordinatesJts = new ArrayList<Coordinate>();
    }

    public String getType() {
        return type;
    }

    public ArrayList<ArrayList<Double>> getCoordinates() {
        /*ArrayList<ArrayList<Double>> coords = new ArrayList<ArrayList<Double>>(coordinatesJts.size());
        for(Coordinate coord: coordinatesJts){
            ArrayList<Double> add = new ArrayList<Double>(2);
            add.add(coord.y);
            add.add(coord.x);
            coords.add(add);
        }
        return coords;*/
        return coordinates;
    }

    @JsonIgnore
    public ArrayList<Double> getPoint(int position){
        return coordinates.get(position);
    }

    @JsonIgnore
    public void setPoint(int position, ArrayList<Double> point){
        coordinates.set(position, point);
    }

    @JsonIgnore
    public void replaceLastPoint(ArrayList<Double> point){
        coordinates.set((coordinates.size()-1), point);
    }

    @JsonIgnore
    public ArrayList<Double> getLastPoint(){
        return coordinates.get( (coordinates.size()-1) );
    }

    public void setCoordinates(ArrayList<ArrayList<Double>> coordinates) {
        this.coordinates = coordinates;
        /*coordinatesJts = null;
        coordinatesJts = new ArrayList<Coordinate>();
        ArrayList<ArrayList<Double>> coords = new ArrayList<ArrayList<Double>>(coordinatesJts.size());
        for(ArrayList<Double> coord: coordinates){
            coordinatesJts.add(new Coordinate(coord.get(0),coord.get(1)));
        }*/
    }

    /**
     * Longitude / Latitude Array
     *
     * @param coordinates
     */
    @JsonIgnore
    public void addPoint(ArrayList<Double> coordinates){
        this.coordinates.add(coordinates);
        //this.coordinatesJts.add(new Coordinate(coordinates.get(0),coordinates.get(1)));
        //builder.add(new Coordinate(coordinates.get(0),coordinates.get(1)));
    }

    /*@JsonIgnore
    public void addCoordinate(Coordinate coordinate){
        this.coordinatesJts.add(coordinate);
    }*/

    @JsonIgnore
    public LineString getLineString(){
        ArrayList<Coordinate> coordinatesJts = new ArrayList<Coordinate>(this.coordinates.size());
        for (ArrayList<Double> coord: this.coordinates){
            coordinatesJts.add(new Coordinate(coord.get(0),coord.get(1)));
        }
        LinearGeometryBuilder builder = new LinearGeometryBuilder(new GeometryFactory());
        GeometryFactory gf = new GeometryFactory();
        return gf.createLineString(coordinatesJts.toArray(new Coordinate[coordinatesJts.size()]));
    }

}
