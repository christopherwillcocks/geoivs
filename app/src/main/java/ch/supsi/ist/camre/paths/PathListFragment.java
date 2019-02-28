package ch.supsi.ist.camre.paths;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import ch.supsi.ist.camre.paths.adapters.PathListAdapter;
import ch.supsi.ist.camre.paths.data.Element;
import ch.supsi.ist.camre.paths.data.Note;
import ch.supsi.ist.camre.paths.data.Path;
import ch.supsi.ist.camre.paths.utils.VIEW;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ch.supsi.ist.camre.paths.PathListFragment.OnPathSelectedListener} interface
 * to handle interaction events.
 *
 */
public class PathListFragment
        extends Fragment
        implements AdapterView.OnItemClickListener, AbsListView.MultiChoiceModeListener, DialogInterface.OnDismissListener {

    private ListView listView;
    private OnPathSelectedListener mListener;
    private PathListAdapter adapter;
    private ArrayList<Path> list;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback;
    ProgressDialog progress;

    ActionMode amode;

    public PathListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_path_list, container, false);
        setHasOptionsMenu(true);
        listView = (ListView) view.findViewById(R.id.pathListView);

        list = Path.getList(((Application)getActivity().getApplication()).database);
        adapter = new PathListAdapter(
                getActivity().getApplicationContext() , R.layout.row_path_list, list);
        /*adapter = new PathListAdapter(
                getActivity().getApplicationContext() , R.layout.row_path_list, new ArrayList<Path>(0));*/
        adapter.setNotifyOnChange(true);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
/*
        progress = new ProgressDialog(getActivity());
        progress.setTitle("Loading");
        progress.setMessage("Wait for position..");
        progress.setCancelable(true);
        progress.show();

        new Thread() {
            public void run() {
                try {
                    sleep(2000);
                    list = Path.getList(((Application)getActivity().getApplication()).database);


                    adapter = new PathListAdapter(
                            getActivity().getApplicationContext() , R.layout.row_path_list, list);
                    adapter.setNotifyOnChange(true);
                    listView.setAdapter(adapter);

                    //adapter.addAll(list);
                    //adapter.notifyDataSetChanged();
                    //PathListFragment.this.listView.refreshDrawableState();
                } catch (Exception e) {

                }
                progress.dismiss();
            }
        }.start();*/
    }

    /*
        DECLARE PERSONAL OPTION MENU
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //menu.clear();
        inflater.inflate(R.menu.home_element_list, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_list_new:
                Intent intent = new Intent(getActivity(), WalkerActivityTest.class);
                intent.putExtra("Path", new Path());
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (i>-1) {
            if (mListener != null) {
                mListener.onPathSelected(list.get(i));
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPathSelectedListener) activity;
        } catch (ClassCastException e) {
            System.out.println(activity.toString()
                    + " should implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

        System.out.println("onItemCheckedStateChanged: " + position);
        Path path = list.get(position);
        System.out.println(" > Name: " + path.getName());

        if (checked) {
            adapter.setNewSelection(position, checked);
        } else {
            adapter.removeSelection(position);
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate the menu for the CAB
        System.out.println("onCreateActionMode");
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.home_element_list_actionmode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        System.out.println("onPrepareActionMode");
        // Here you can perform updates to the CAB due to
        // an invalidate() request
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        amode = mode;

        switch (item.getItemId()) {

            case R.id.home_element_delete:

                final View cb = getActivity().getLayoutInflater().inflate(R.layout.prompt_checkbox,null);

                new AlertDialog.Builder(getActivity())
                        //.setTitle("Select The Difficulty Level")
                        .setMessage(getString(R.string.action_delete_element))
                        .setView(cb)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                CheckBox keep = (CheckBox) cb.findViewById(R.id.prompt_checkbox_media);

                                Database database = ((Application)getActivity().getApplication()).database;

                                int counter = 0;

                                for (Integer position: adapter.getCurrentCheckedPosition()) {
                                    counter++;
                                    System.out.println("**********************************");
                                    Path path = list.get(position);
                                    System.out.println(path.getName());

                                    if (keep.isChecked()) {

                                        System.out.println("Deleting also media files");

                                        ArrayList<Element> elements = path.getBridge();
                                        elements.addAll(path.getSurface());
                                        elements.addAll(path.getLeftSide());
                                        elements.addAll(path.getRightSide());

                                        for (Element bridge: elements){
                                            for (Note note: bridge.getNotes()){
                                                if(note.getUri()!=null && !note.getUri().isEmpty()) {
                                                    try {
                                                        File file = new File(note.getUri());
                                                        System.out.println(" del " + note.getUri());
                                                        if (file.exists()) {
                                                            file.delete();
                                                        }
                                                    } catch (Exception e) {
                                                        System.out.println(e.getMessage());
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    System.out.println("Deleting Path");

                                    Document doc = database.getDocument(path.get_id());

                                    try {
                                        doc.delete();
                                        System.out.println(" > Deleted!");
                                    } catch (CouchbaseLiteException e) {
                                        e.printStackTrace();
                                    }
                                }

                                //getFragmentManager().popBackStack();

                                //adapter.notifyDataSetChanged();

                                for (Integer position: adapter.getCurrentCheckedPosition()) {
                                    adapter.remove(list.get(position));
                                }

                                dialog.dismiss();

                                Toast.makeText(getActivity(), counter + " paths deleted", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .setOnDismissListener(this)
                        .show();


                return true;

            case R.id.home_element_upload:
                final SyncHttpClient syncClient = new SyncHttpClient();

                final ProgressDialog progress = new ProgressDialog(getActivity());
                progress.setTitle("Uploading");
                progress.setMessage("Please wait..");
                //progress.setCancelable(true);
                //progress.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (Integer position: adapter.getCurrentCheckedPosition()){

                            System.out.println("**********************************");

                            Path path = list.get(position);
                            System.out.println(path.getName());

                                    /*for (Element element: path.getLeftSide()){
                                        System.out.println(">>>> " + element.getType());

                                        // Uploading files
                                        for (Note note: element.getNotes()){
                                            System.out.println(">>>>>>>> " + note.getUri());

                                            // if note is dirty > Upload file
                                            File myFile = new File(note.getUri());
                                            RequestParams params = new RequestParams();
                                            try {
                                                params.put("multimedia", myFile);
                                                syncClient.post(getActivity().getApplicationContext(), "http://localhost:8888/", params, new AsyncHttpResponseHandler() {

                                                    @Override
                                                    public void onStart() {

                                                    }

                                                    @Override
                                                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                                                    }

                                                    @Override
                                                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {

                                                    }
                                                });
                                            } catch(FileNotFoundException e) {

                                            }
                                            // else skip
                                        }

                                    }*/

                            StringEntity entity = null;
                            try {
                                entity = new StringEntity(path.toJSON());

                                syncClient.post(
                                        getActivity().getApplicationContext(),
                                        "http://localhost:8888/",
                                        entity,"application/json",
                                        new AsyncHttpResponseHandler() {

                                            @Override
                                            public void onStart() {
                                                System.out.println("Upload started..");
                                            }

                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                                                System.out.println("Upload onSuccess..");
                                            }

                                            @Override
                                            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                                                System.out.println("Upload onFailure..");
                                            }

                                            @Override
                                            public void onProgress(int bytesWritten, int totalSize){
                                                super.onProgress(bytesWritten,totalSize);
                                                System.out.println("OnProgress: bytesWritten: " + bytesWritten + ", totalSize: " + totalSize);
                                            }
                                        }
                                );

                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            // if path is dirty > Upload path as JSON



                        }
                        //progress.hide();
                    }
                }).start();

                mode.finish();
                return true;

            case R.id.home_element_sync:

                for (Integer position: adapter.getCurrentCheckedPosition()) {
                    System.out.println("**********************************");
                    Path path = list.get(position);
                    System.out.println(path.getName());
                    System.out.println(path.toJSON());
                }

            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        System.out.println("onDestroyActionMode");
        adapter.clearSelection();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        System.out.println("onDismiss dismissed!");
        amode.finish();
        adapter.notifyDataSetChanged();
    }

    public interface OnPathSelectedListener {
        // TODO: Update argument type and name
        public void onPathSelected(Path path);
    }

}
