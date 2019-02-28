package ch.supsi.ist.camre.paths;



import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ch.supsi.ist.camre.paths.R;
import ch.supsi.ist.camre.paths.data.Note;
import ch.supsi.ist.camre.paths.utils.VIEW;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class NotePreview extends Fragment {

    private TextView description;
    private ImageView image;

    private Note note;


    public NotePreview() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //setHasOptionsMenu(true);

        System.out.println("NotePreview: onCreateView");

        View view = inflater.inflate(R.layout.fragment_note_preview, container, false);

        description = (TextView)view.findViewById(R.id.note_preview_text);
        image = (ImageView)view.findViewById(R.id.note_preview_thumb);


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            note = (Note)bundle.getSerializable("Note");
        }else{
            note = new Note();
        }

        image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("You clicked the image!");
                Bundle b = new Bundle();
                b.putSerializable("Note", note);
                ((WalkerActivityTest)getActivity()).openFragment(VIEW.NOTE,b);
            }
        });

        if(note.getUri().endsWith(".3gp")){
            Picasso.with(this.getActivity())
                    .load(R.drawable.mic)
                    .resizeDimen(R.dimen.note_preview_img_width, R.dimen.note_preview_img_height)
                            //.centerInside()
                    .centerCrop()
                    .into(image);
        }else if(note.getUri().endsWith(".mp4")){
            Picasso.with(this.getActivity())
                    .load(R.drawable.video)
                    .resizeDimen(R.dimen.note_preview_img_width, R.dimen.note_preview_img_height)
                            //.centerInside()
                    .centerCrop()
                    .into(image);
        }else {
            Picasso.with(this.getActivity())
                    .load(Uri.parse(note.getUri()))
                    .resizeDimen(R.dimen.note_preview_img_width, R.dimen.note_preview_img_height)
                            //.centerInside()
                    .centerCrop()
                    .into(image);
        }
        description.setText(note.getDescription());

        return view;
    }

}
