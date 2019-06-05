package ch.supsi.ist.camre.paths.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * Created by milan antonovic on 03/10/14.
 *
 *
 *
 {
     "multimedia": "53bc09f83fb6ed27711a0bfd",
     "description": "dettaglio 2",
     "point": {
         "geometry": {
            "type": "Point",
            "coordinates": [7.1,12.2]
         },
         "timestamp": "2014-02-19T13:52:26Z",
         "altitude": 110.52,
         "heading": 37.44
     }
 }
 *
 */
public class Note implements Serializable {

    private String multimedia; // this is the id
    private String description;
    private Position point;

    private String uri; // this is the url can be local / or webbased
    //private Uri uri;

    @JsonIgnore
    private Element element;

    public Note(){}

    //public Note(Uri uri, Position location){
    public Note(String uri, Position location){
        System.out.println("NOTE: created new note ("+uri+")");
        this.uri = uri;
        this.point = location;
    }

    public String getMultimedia() {
        return multimedia;
    }

    public void setMultimedia(String multimedia) {
        this.multimedia = multimedia;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Position getPoint() {
        return point;
    }

    public void setPoint(Position point) {
        this.point = point;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @JsonIgnore
    public Element getElement() {
        return element;
    }

    @JsonIgnore
    public void setElement(Element element) {
        this.element = element;
    }

}
