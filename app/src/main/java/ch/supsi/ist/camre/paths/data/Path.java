package ch.supsi.ist.camre.paths.data;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by milan antonovic on 22/08/14.
 */
public class Path implements Serializable{

    private String _rev;
    private String _id;

    private boolean closed;

    private String name;
    private String path;

    private String status;
    private String timestamp;

    private Characteristics characteristics;

    private Author author;

    private ArrayList<Element> leftSide;
    private ArrayList<Element> rightSide;
    private ArrayList<Element> surface;
    private ArrayList<Element> bridge;

    private Linestring geometry;

    @JsonIgnore
    private ArrayList<DataChangedListener> listeners = new ArrayList<DataChangedListener>();

    //private Techinfo techinfo;
    //private Author author;

    public Path(){

        this.characteristics = new Characteristics();
        this.leftSide = new ArrayList<Element>();
        this.rightSide = new ArrayList<Element>();
        this.surface = new ArrayList<Element>();
        this.bridge = new ArrayList<Element>();

        this.geometry = new Linestring();

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);

        this.timestamp = df.format(new Date());
    }

    @JsonIgnore
    public void addListener(DataChangedListener l) {
        System.out.println("Path: addListener("+l.getClass().getName()+")");
        if(!listeners.contains(l) && l!=null){
            listeners.add(l);
        }
        System.out.println("  - Listeners: " + listeners.size());
    }

    public void resetListeners(){
        listeners.removeAll(listeners);
    }

    @JsonIgnore
    public void notifyChanges() {
        System.out.println("Notifying to " + listeners.size() + " listeners");
        for(DataChangedListener l : listeners) {
            l.dataChanged(this);
        }
    }

    public ArrayList<Element> getLeftSide() {
        return leftSide;
    }

    public void setLeftSide(ArrayList<Element> leftSide) {
        this.leftSide = leftSide;
        this.setParents(this.leftSide);
    }

    public ArrayList<Element> getRightSide() {
        return rightSide;
    }

    public void setRightSide(ArrayList<Element> rightSide) {
        this.rightSide = rightSide;
        this.setParents(this.rightSide);
    }

    public ArrayList<Element> getBridge() {
        return bridge;
    }

    public void setBridge(ArrayList<Element> bridge) {
        this.bridge = bridge;
        this.setParents(this.bridge);
    }


    public ArrayList<Element> getSurface() {
        return surface;
    }

    public void setSurface(ArrayList<Element> surface) {
        this.surface = surface;
        this.setParents(this.surface);
    }

    public String get_rev() {
        return _rev;
    }

    public void set_rev(String _rev) {
        this._rev = _rev;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        //System.out.println("Path: setName("+name+")");
        /*StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(Path.class.getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0) {
                System.out.println("  - Caller: " + ste.getClassName());
            }
        }*/
        this.name = name;
    }

    public Characteristics getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(Characteristics characteristics) {
        this.characteristics = characteristics;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Linestring getGeometry() {
        return geometry;
    }

    public void setGeometry(Linestring geometry) {
        this.geometry = geometry;
    }

    @JsonIgnore
    public String toJSON(){
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> map = om.convertValue(this, Map.class);
        try {
            return om.writeValueAsString(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @JsonIgnore
    public static ArrayList<Path> getList(Database database){
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Path> ret = null;
        Query query = database.createAllDocumentsQuery();
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        QueryEnumerator result = null;
        try {
            result = query.run();
            //System.out.println("Results found: " + result.getCount());
            ret = new ArrayList<Path>(result.getCount());
            Path path;
            Map<String, Object> map;
            Document doc;
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                //System.out.println("Doc ID: " + row.getDocumentId());
                doc = row.getDocument();
                map = doc.getProperties();
                try {
                    path = mapper.readValue(
                            mapper.writeValueAsString(doc.getProperties()), Path.class);
                    if (path!=null){
                        //System.out.println("Path name: " + path.getValue());
                        ret.add(path);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return ret;
    }


    @JsonIgnore
    public void validate(){

    }

    @JsonIgnore
    public static Path initialize(String oid, Database database){
        System.out.println("Loading data from DB..");
        Document doc = database.getDocument(oid);
        if(doc != null){
            System.out.println("****************************************************************");
            System.out.println(doc.getUserProperties().toString());
            ObjectMapper mapper = new ObjectMapper();
            try {
                System.out.println("****************************************************************");
                System.out.println( mapper.writeValueAsString(doc.getProperties()));
                return mapper.readValue(
                        mapper.writeValueAsString(doc.getProperties()), Path.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Path();
    }

    @JsonIgnore
    public void addLeft(Element element){
        element.setParent(this);
        this.leftSide.add(element);
        System.out.println("Added element " + element.getType() + " to the rightSide (tatal: " + this.leftSide.size() + ")");
    }

    @JsonIgnore
    public void addRight(Element element){
        element.setParent(this);
        this.rightSide.add(element);
    }

    @JsonIgnore
    public void addSurface(Element element){
        element.setParent(this);
        this.surface.add(element);
    }

    @JsonIgnore
    public void addBridge(Element element){
        element.setParent(this);
        this.bridge.add(element);
    }

    @JsonIgnore
    private void setParents(ArrayList<Element> elements){
        Iterator<Element> iterator = elements.iterator();
        while (iterator.hasNext()){
            Element element = iterator.next();
            element.setParent(this);
        }
    }

    @JsonIgnore
    public void remove(Element element){
        if(!this.leftSide.remove(element)){
            if(!this.rightSide.remove(element)){
                this.surface.remove(element);
            }
        }
    }


}
