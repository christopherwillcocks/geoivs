package ch.supsi.ist.camre.paths.data;


import java.util.ArrayList;

/**
 * Created by milan antonovic on 28/08/14.
 */
public class SimpleElement {

    private String title;
    private String description;
    private String subtitle;
    private String type;

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SimpleElement(){}

    public SimpleElement(String title, String subtitle, String description, String type) {
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.type = type;
    }

    /*private JSONArray getJSONNode(){
        JSONArray = null;
        try {
            json = (new ObjectMapper()).readTree("{ " +
                    "\"title\": \"\"" +
                    "}");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }*/

    /*public static ArrayList<SimpleElement> getSideList(){
        ArrayList<SimpleElement> ret = new ArrayList<SimpleElement>();

        String desc = "Cras eget pretium turpis, id lobortis leo. Ut lacinia sollicitudin magna. Cras id mollis dolor. Sed a elit mattis, lacinia nisl id, sollicitudin tortor. Proin blandit ultrices scelerisque. Quisque vitae ipsum blandit, tincidunt arcu et, suscipit mauris. Proin iaculis sapien eu odio luctus, sit amet commodo magna suscipit. Vivamus eu imperdiet orci. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Ut ut neque semper augue convallis finibus eget vitae est. Suspendisse at elementum ex, a aliquet diam.";
        String sub = "Ut lacinia sollicitudin magna";

        ret.add(new SimpleElement("Scarpata in discesa",sub,desc,"http://www.camre.ch/properties/scarp/terrain/down"));
        ret.add(new SimpleElement("Scarpata in salita",sub,desc,"http://www.camre.ch/properties/scarp/terrain/up"));
        ret.add(new SimpleElement("Muro in pietra",sub,desc,"http://www.camre.ch/properties/wall/stone"));
        ret.add(new SimpleElement("Muro in cemento",sub,desc,"http://www.camre.ch/properties/wall/cement"));
        ret.add(new SimpleElement("Staccionata in legno",sub,desc,"http://www.camre.ch/properties/fence/wood"));
        ret.add(new SimpleElement("Bosco",sub,desc,"http://www.camre.ch/properties/woods"));

        return ret;
    }*/

    public static ArrayList<SimpleElement> getSideList(String[] list){
        ArrayList<SimpleElement> ret = new ArrayList<SimpleElement>();
        for (int c=0 ;c<list.length;c++){
            String[] item = list[c].split(";");
            ret.add(new SimpleElement(item[1],item[2],"",item[0]));
        }
        return ret;
    }

    /*public static ArrayList<SimpleElement> getSurfaceList(){
        ArrayList<SimpleElement> ret = new ArrayList<SimpleElement>();

        String desc = "Cras eget pretium turpis, id lobortis leo. Ut lacinia sollicitudin magna. Cras id mollis dolor. Sed a elit mattis, lacinia nisl id, sollicitudin tortor. Proin blandit ultrices scelerisque. Quisque vitae ipsum blandit, tincidunt arcu et, suscipit mauris. Proin iaculis sapien eu odio luctus, sit amet commodo magna suscipit. Vivamus eu imperdiet orci. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Ut ut neque semper augue convallis finibus eget vitae est. Suspendisse at elementum ex, a aliquet diam.";
        String sub = "Ut lacinia sollicitudin magna";

        ret.add(new SimpleElement("Pavimento",sub,desc,"http://www.camre.ch/properties/surface/pavement"));
        ret.add(new SimpleElement("Sterrato",sub,desc,"http://www.camre.ch/properties/surface/gravel"));
        ret.add(new SimpleElement("Asfalto",sub,desc,"http://www.camre.ch/properties/surface/asphalt"));
        ret.add(new SimpleElement("Legno",sub,desc,"http://www.camre.ch/properties/surface/wood"));
        ret.add(new SimpleElement("Ciottoli",sub,desc,"http://www.camre.ch/properties/surface/shingle"));

        return ret;
    }*/
}
