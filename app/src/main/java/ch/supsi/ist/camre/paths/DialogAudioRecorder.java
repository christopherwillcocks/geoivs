package ch.supsi.ist.camre.paths;



import android.app.Fragment;
import android.app.ProgressDialog;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.location.LocationListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ch.supsi.ist.camre.paths.data.Element;
import ch.supsi.ist.camre.paths.data.Note;
import ch.supsi.ist.camre.paths.data.Point;
import ch.supsi.ist.camre.paths.data.Position;
import ch.supsi.ist.camre.paths.utils.VIEW;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class DialogAudioRecorder extends Fragment implements LocationListener{

    private  Note note;
    private  Element element;
    private WalkerActivityFooter locationWorker;
    private ProgressDialog progress;

    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

    private Button mRecordButton = null;
    private MediaRecorder mRecorder = null;
    boolean mStartRecording = true;

    private Button   mPlayButton = null;
    private MediaPlayer   mPlayer = null;
    boolean mStartPlaying = true;

    public DialogAudioRecorder() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_dialog_audio_recorder, container, false);

        //getDialog().setTitle("Recording Audio Note");

        view.findViewById(R.id.audio_btn_record).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    ((Button) v).setText("Stop");
                } else {
                    ((Button) v).setText("Record");
                }
                mStartRecording = !mStartRecording;
            }
        });

        view.findViewById(R.id.audio_btn_stop).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    ((Button)v).setText("Stop");
                } else {
                    ((Button)v).setText("Play");
                }
                mStartPlaying = !mStartPlaying;
            }
        });

        view.findViewById(R.id.audio_btn_save).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //dismiss();

                locationWorker = (WalkerActivityFooter)getFragmentManager()
                        .findFragmentByTag(VIEW.LOCATION_WORKER.toString());

                progress = new ProgressDialog(DialogAudioRecorder.this.getActivity());
                progress.setTitle("Waiting GPS");
                progress.setMessage("Wait for position..");
                progress.setCancelable(true);
                progress.show();

                locationWorker.addFusionLocationListener(DialogAudioRecorder.this);

            }
        });


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            note = (Note)bundle.getSerializable("Note");
            element = (Element)bundle.getSerializable("Element");
            mFileName = note.getUri();
            System.out.println("Note given: ");// + note.get);
        }else{
            note = new Note();
            mFileName = createFilePath();
            System.out.println("No Note given");
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "camre_" + timeStamp + "_";

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public static String createFilePath(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        filePath += "/" + "camre_" + timeStamp + "_" + ".3gp";
        return filePath;
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }



    @Override
    public void onLocationChanged(Location location) {
        System.out.println("DialogAudioRecorder: onLocationChanged");

        double lon = location.getLongitude();
        double lat = location.getLatitude();

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);

        Position pos = new Position();
        pos.setAltitude(location.getAltitude());
        pos.setHeading(0.0);
        pos.setGeometry(new Point(lon, lat));
        pos.setTimestamp(df.format(new Date()));

        getNote().setPoint(pos);

        if(getNote().getElement()==null) {
                            /*ElementForm ef = (ElementForm) getFragmentManager()
                                    .findFragmentByTag(VIEW.ELEMENT.toString());
                            Element element = ef.getElement();*/
            element.addNote(getNote());
        }
        getNote().getElement().getParent().notifyChanges();
        Bundle bundle = new Bundle();
        bundle.putSerializable("Note", getNote());

        //openFragment(VIEW.PATH_MAP, bundle);
        locationWorker.removeFusionLocationListener(this);

        //System.out.println("Activity: " + DialogAudioRecorder.this.getActivity().getClass().getName());

        progress.dismiss();

        Fragment fragment = getFragmentManager()
                .findFragmentByTag(VIEW.NOTE.toString());

        if(fragment==null) {
            fragment = new NoteForm();
            fragment.setArguments(bundle);
        }else{
            fragment.getArguments().putSerializable("Note",
                    bundle.getSerializable("Note"));
        }

        /*getFragmentManager().beginTransaction()
                .remove(this)
                .commit();*/

        //getFragmentManager().popBackStackImmediate();

        getFragmentManager().beginTransaction()
                .replace(R.id.walkerPanelTest, fragment, VIEW.NOTE.toString())
                .addToBackStack(null)
                .commit();

    }
}
