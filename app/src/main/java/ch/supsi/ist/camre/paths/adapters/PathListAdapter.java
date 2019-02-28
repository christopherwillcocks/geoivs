package ch.supsi.ist.camre.paths.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import ch.supsi.ist.camre.paths.R;
import ch.supsi.ist.camre.paths.data.KeyValuePair;
import ch.supsi.ist.camre.paths.data.Path;

/**
 * Created by milan antonovic on 03/09/14.
 */
public class PathListAdapter  extends ArrayAdapter<Path> {

    private LayoutInflater inflater;
    private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();


    private static ArrayList<KeyValuePair> status;


    private class ViewHolder {
        TextView title;
        TextView difficulty;
        TextView stats;
    }

    public PathListAdapter(Context context, int resource, ArrayList<Path> model) {
        super(context, resource, model);
        inflater = LayoutInflater.from(getContext());
        status = KeyValuePair.getList(
                getContext().getResources().getStringArray(R.array.path_status));
    }

    public void setNewSelection(int position, boolean value) {
        mSelection.put(position, value);
        notifyDataSetChanged();
    }

    public boolean isPositionChecked(int position) {
        Boolean result = mSelection.get(position);
        return result == null ? false : result;
    }

    public Set<Integer> getCurrentCheckedPosition() {
        return mSelection.keySet();
    }

    public void removeSelection(int position) {
        mSelection.remove(position);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        mSelection = new HashMap<Integer, Boolean>();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.row_path_list, null);
            holder.title = (TextView)convertView.findViewById(R.id.listPathName);
            holder.difficulty = (TextView)convertView.findViewById(R.id.listPathDifficulty);
            holder.stats = (TextView)convertView.findViewById(R.id.listPathStats);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        convertView.setBackgroundColor(Color.parseColor("#FFFFFF")); //default color
        if (mSelection.get(position) != null) {
            convertView.setBackgroundColor(Color.RED);// this is a selected position so make it red
        }

        Path path = getItem(position);

        if (path != null) {
            holder.title.setText(path.getName());
            holder.difficulty.setText(path.getCharacteristics().getDifficulty());
            int points = path.getGeometry().getCoordinates().size();
            int left = path.getLeftSide().size();
            int surface = path.getSurface().size();
            int right = path.getRightSide().size();
            //getContext().getText(R.string.left_side)
            holder.stats.setText("Path points: " + points + "  Left elements: " + left + " Right: " + right + " Surface types: " + surface);

            /*
            <string-array name="path_status">
                <item>http://www.camre.ch/path/status/approved;approved</item>
                <item>http://www.camre.ch/path/status/standby;standby</item>
                <item>http://www.camre.ch/path/status/banned;banned</item>
            </string-array>
            */

            if(path.getStatus().equals(status.get(0).getKey())){
                convertView.findViewById(R.id.listPathStatus).setBackgroundColor(Color.GREEN);
            }else if(path.getStatus().equals(status.get(1).getKey())){
                convertView.findViewById(R.id.listPathStatus).setBackgroundColor(Color.YELLOW);
            }else if(path.getStatus().equals(status.get(2).getKey())){
                convertView.findViewById(R.id.listPathStatus).setBackgroundColor(Color.RED);
            }

            if(path.isClosed()){
                convertView.findViewById(R.id.padlock_img).setVisibility(View.VISIBLE);
            }else{
                convertView.findViewById(R.id.padlock_img).setVisibility(View.INVISIBLE);
            }

        }

        return convertView;
    }
}
