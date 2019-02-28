package ch.supsi.ist.camre.paths.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

import ch.supsi.ist.camre.paths.R;
import ch.supsi.ist.camre.paths.data.KeyValuePair;

/**
 * Created by milan antonovic on 04/09/14.
 */
public class KeyValuePairAdapter  extends ArrayAdapter<KeyValuePair> {

    private LayoutInflater inflater;

    private ArrayList<KeyValuePair> cache;

    private class ViewHolder {
        TextView key;
        TextView value;
    }

    public KeyValuePairAdapter(Context context, int resource, ArrayList<KeyValuePair> model) {
        super(context, resource, model);
        cache = model;
        inflater = LayoutInflater.from(getContext());
    }

    public int indexOf(KeyValuePair kvp){
        return cache.indexOf(kvp);
    }

    public int indexOf(String key){
        //System.out.println("Searching key: " + key);
        Iterator<KeyValuePair> iterator = cache.iterator();
        while (iterator.hasNext()){
            KeyValuePair kvp = iterator.next();
            //System.out.println(" > Comparing with: " + kvp.getKey());
            if (kvp.getKey().equals(key)){
                //System.out.println(" > Key found! ("+kvp.getValue()+")");
                return cache.indexOf(kvp);
            }
        }
        //System.out.println(" > Key NOT found!");
        return -1;
    }

    @Override
    public View getView(int position, View convertView,
                        ViewGroup parent) {

        ViewHolder holder = null;

        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.row_spinner, null);
            holder.key = (TextView)convertView.findViewById(R.id.spinnerKey);
            holder.value = (TextView)convertView.findViewById(R.id.spinnerValue);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        KeyValuePair c = getItem(position);

        if (c != null) {
            holder.key.setText(c.getKey());
            holder.value.setText(c.getValue());
            holder.key.setVisibility(View.GONE);
        }

        return convertView;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        //return this.getView(position,convertView,parent);

        ViewHolder holder = null;

        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.row_spinner, null);
            holder.key = (TextView)convertView.findViewById(R.id.spinnerKey);
            holder.value = (TextView)convertView.findViewById(R.id.spinnerValue);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        KeyValuePair c = getItem(position);

        if (c != null) {
            holder.key.setText(c.getKey());
            holder.value.setText(c.getValue());
        }

        return convertView;
    }
}
