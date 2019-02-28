package ch.supsi.ist.camre.paths;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ch.supsi.ist.camre.paths.adapters.SimpleElementListAdapter;
import ch.supsi.ist.camre.paths.data.SimpleElement;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SimpleElementListFragment.OnElementListInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SimpleElementListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SimpleElementListFragment extends Fragment
        implements /*SwipeDismissListViewTouchListener.DismissCallbacks,*/ AdapterView.OnItemClickListener{

    public enum TYPE {
        SURFACE, SIDE
    }

    private TYPE type;

    private SimpleElementListAdapter adapter;

    private OnElementListInteractionListener mListener;

    private ArrayList<SimpleElement> list;

    public static SimpleElementListFragment newInstance(TYPE type) {
        SimpleElementListFragment fragment = new SimpleElementListFragment();
        fragment.setType(type);
        return fragment;
    }

    public SimpleElementListFragment() {
        this.type = TYPE.SIDE;
    }

    private void setType(TYPE type){
        this.type = type;
    }

    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_element_list, container, false);

        ListView listView = (ListView) view.findViewById(R.id.listView);

        switch (this.type) {
            case SURFACE:
                list = SimpleElement.getSideList(getResources().getStringArray(R.array.elements_surface_type));
                break;
            case SIDE:
                list = SimpleElement.getSideList(getResources().getStringArray(R.array.elements_side_type));
                break;
        }

        adapter = new SimpleElementListAdapter(getActivity().getApplicationContext() , R.layout.row_element_list, list);
        adapter.setNotifyOnChange(true);
        listView.setAdapter(adapter);

        /*SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
                listView,
                new SwipeDismissListViewTouchListener.DismissCallbacks() {

                    public boolean canDismiss(int position) {
                        return true;
                    }

                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        System.out.println("ondismiss..");
                        for (int position : reverseSortedPositions) {
                            adapter.remove(adapter.getItem(position));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

                //new SwipeDismissListViewTouchListener(listView,this);
         listView.setOnTouchListener(touchListener);
         listView.setOnScrollListener(touchListener.makeScrollListener());*/

        listView.setOnItemClickListener(this);

        return view;
    }

    /*@Override
    public boolean canDismiss(int position) {
        return false;
    }

    @Override
    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
        System.out.println("ondismiss..");
        for (int position : reverseSortedPositions) {
            adapter.remove(adapter.getItem(position));
        }
        adapter.notifyDataSetChanged();
    }*/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //System.out.println("SimpleElementListFragment.onAttache: "+activity.getComponentName().toString());
        try {
            mListener = (OnElementListInteractionListener) activity;
        } catch (ClassCastException e) {
            /*throw new ClassCastException(activity.toString()
                    + " must implement OnElementListInteractionListener");*/
            System.out.println(activity.toString()
                    + " should implement OnElementListInteractionListener");
        }
    }

    public void register(OnElementListInteractionListener me){
        mListener = me;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (i>-1) {
            if (mListener != null) {
                mListener.onItemSelected(list.get(i), this);
            }
        }
    }

    public interface OnElementListInteractionListener {
        // TODO: Update argument type and name
        public void onItemSelected(SimpleElement simpleElement, SimpleElementListFragment list);
    }

}
