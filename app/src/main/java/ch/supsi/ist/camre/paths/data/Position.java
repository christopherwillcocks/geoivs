package ch.supsi.ist.camre.paths.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * Created by milan antonovic on 02/09/14.
 */
public class Position implements Serializable {

    Point geometry;
    String timestamp;
    Double altitude;
    Double heading;

    @JsonIgnore
    float accuracy;

    public Point getGeometry() {
        return geometry;
    }

    public void setGeometry(Point geometry) {
        this.geometry = geometry;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestap) {
        this.timestamp = timestap;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    @JsonIgnore
    public float getAccuracy() {
        return accuracy;
    }

    @JsonIgnore
    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public String toString(){
        String ret = "Position: ";
        if (getGeometry()!=null){
            ret += getGeometry().getLon() + "/" + getGeometry().getLat();
        }
        return ret;
    }
}
