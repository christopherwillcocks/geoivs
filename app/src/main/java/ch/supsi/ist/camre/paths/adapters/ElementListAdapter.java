package ch.supsi.ist.camre.paths.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import ch.supsi.ist.camre.paths.R;
import ch.supsi.ist.camre.paths.data.Element;

public class ElementListAdapter extends ArrayAdapter<Element>{

    private LayoutInflater inflater;
    private static HashMap<String,String> dictionary;

    private class ViewHolder {
        TextView title;
        TextView subtitle;
    }

    public ElementListAdapter(Context context, int resource, ArrayList<Element> model) {

        super(context, resource, model);
        inflater = LayoutInflater.from(getContext());

        if (dictionary == null) {
            //System.out.println("ElementListAdapter dictionary:");
            String[] values = context.getResources().getStringArray(R.array.elements_surface_type);
            dictionary = new HashMap<String, String>();
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
        }else{
            System.out.println("ElementListAdapter NOT INITIALIZING:");
        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Element c = getItem(position);

        ViewHolder holder = null;

        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.row_element_list, null);
            holder.title = (TextView)convertView.findViewById(R.id.listtitle);
            holder.subtitle = (TextView)convertView.findViewById(R.id.listsubtitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (c != null) {
            holder.title.setText(dictionary.get(c.getType()));
            holder.subtitle.setText(c.getType());
        }

        return convertView;
    }
}