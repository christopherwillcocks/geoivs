package ch.supsi.ist.camre.paths.data;

import android.content.Context;

import java.util.HashMap;

import ch.supsi.ist.camre.paths.R;

/**
 * Created by milan antonovic on 08/01/15.
 */
public class DataUtils {

    public static HashMap<String,String> getDictionary(Context context){

        HashMap<String,String> dictionary = new HashMap<String, String>();

        String[] values = context.getResources().getStringArray(R.array.elements_surface_type);
        for (int c = 0; c < values.length; c++) {
            String[] item = values[c].split(";");
            dictionary.put(item[0], item[1]);
        }

        values = context.getResources().getStringArray(R.array.elements_side_type);
        for (int c = 0; c < values.length; c++) {
            String[] item = values[c].split(";");
            dictionary.put(item[0], item[1]);
        }
        values = context.getResources().getStringArray(R.array.elements_bridge_type);
        for (int c = 0; c < values.length; c++) {
            String[] item = values[c].split(";");
            dictionary.put(item[0], item[1]);
        }

        return dictionary;
    }
}
