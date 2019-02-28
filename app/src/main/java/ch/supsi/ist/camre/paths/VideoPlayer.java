package ch.supsi.ist.camre.paths;



import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import ch.supsi.ist.camre.paths.data.Note;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class VideoPlayer extends Fragment {

    private Note note;
    private VideoView video;
    private static String mFileName = null;

    public VideoPlayer() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);

        video = (VideoView)view.findViewById(R.id.video_view);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            note = (Note)bundle.getSerializable("Note");
            mFileName = note.getUri();
            System.out.println("Note given: ");// + note.get);

            video.setVideoPath(mFileName);

            video.setMediaController(new MediaController(getActivity()));

        }else{
            note = new Note();
            System.out.println("No Note given");
        }

        return view;
    }


    @Override
    public void onResume (){
        super.onResume();
        video.start();
    }


}
