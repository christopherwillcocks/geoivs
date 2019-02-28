package ch.supsi.ist.camre.paths.data;


import java.util.ArrayList;

/**
 * Created by milan antonovic on 28/08/14.
 */
public class KeyValuePair {

    private String key;
    private String value;

    public KeyValuePair(){}

    private KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static ArrayList<KeyValuePair> getList(String[] list){
        ArrayList<KeyValuePair> ret = new ArrayList<KeyValuePair>();
        for (int c=0 ;c<list.length;c++){
            String[] item = list[c].split(";");
            ret.add(new KeyValuePair(item[0],item[1]));
        }
        return ret;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
