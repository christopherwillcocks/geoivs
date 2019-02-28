package ch.supsi.ist.camre.paths.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ch.supsi.ist.camre.paths.R;
import ch.supsi.ist.camre.paths.data.SimpleElement;

public class SimpleElementListAdapter extends ArrayAdapter<SimpleElement> {

    private LayoutInflater inflater;

    private class ViewHolder {
        TextView title;
        TextView subtitle;
    }

    public SimpleElementListAdapter(Context context, int resource, ArrayList<SimpleElement> model) {
        super(context, resource, model);
        inflater = LayoutInflater.from(getContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

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

        SimpleElement c = getItem(position);

        if (c != null) {
            holder.title.setText(c.getTitle());
            holder.subtitle.setText(c.getSubtitle());
        }

        return convertView;
    }

}