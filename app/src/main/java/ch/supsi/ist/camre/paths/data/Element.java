package ch.supsi.ist.camre.paths.data;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by milan antonovic on 29/08/14.
 */
public class Element implements Serializable {

    private String type;
    private String status;
    private Position startPoint;
    private Position endPoint;
    private String width;
    private String height;
    private ArrayList<Note> notes;

    @JsonIgnore
    private Path parent;

    @JsonIgnore
    private String name;

    public Element(){
        this.notes = new ArrayList<Note>();
    }

    public Element(SimpleElement simpleElement){
        this.type = simpleElement.getType();
        this.name = simpleElement.getTitle();
        this.notes = new ArrayList<Note>();
    }

    @JsonIgnore
    public String getName() {
        return name;
    }

    @JsonIgnore
    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public boolean isSurface() {
        return this.getParent().getSurface().contains(this);
    }
    @JsonIgnore
    public boolean isLeft() {
        return this.getParent().getLeftSide().contains(this);
    }
    @JsonIgnore
    public boolean isRight() {
        return this.getParent().getRightSide().contains(this);
    }
    @JsonIgnore
    public boolean isBridge() {
        return this.getParent().getBridge().contains(this);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Position getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Position startPoint) {
        this.startPoint = startPoint;
    }

    public Position getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Position endPoint) {
        this.endPoint = endPoint;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public ArrayList<Note> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<Note> notes) {
        this.setParents(notes);
        this.notes = notes;
    }

    @JsonIgnore
    public void addNote(Note note){
        note.setElement(this);
        this.notes.add(note);
    }

    @JsonIgnore
    public void removeNote(Note note){
        this.notes.remove(note);
    }

    @JsonIgnore
    public Path getParent() {
        return parent;
    }

    @JsonIgnore
    public void setParent(Path parent) {
        this.parent = parent;
    }

    @JsonIgnore
    private void setParents(ArrayList<Note> notes){
        Iterator<Note> iterator = notes.iterator();
        while (iterator.hasNext()){
            Note note = iterator.next();
            note.setElement(this);
        }
    }
}
