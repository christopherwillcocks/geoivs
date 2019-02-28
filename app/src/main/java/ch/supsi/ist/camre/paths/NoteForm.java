package ch.supsi.ist.camre.paths;



import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import ch.supsi.ist.camre.paths.data.Note;
import ch.supsi.ist.camre.paths.data.Point;
import ch.supsi.ist.camre.paths.utils.VIEW;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class NoteForm extends Fragment implements View.OnClickListener{

    private ImageView image;
    private EditText lat;
    private EditText lon;
    private EditText description;

    private MenuItem next, prev;

    private Note note;

    NoteMapFragment map;

    public NoteForm() {
        // Required empty public constructor
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_note_form, container, false);

        image = (ImageView)view.findViewById(R.id.note_image);


        lat = (EditText) view.findViewById(R.id.note_point_lat);
        lon = (EditText) view.findViewById(R.id.note_point_lon);

        description =  (EditText) view.findViewById(R.id.note_description);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            note = (Note)bundle.getSerializable("Note");
            System.out.println("Note given: ");// + note.get);
        }else{
            note = new Note();
            System.out.println("No Note given");
        }

        bundle.putSerializable("center", note.getPoint());
        bundle.putBoolean("geolocation",true);
        bundle.putInt("zoom", 17);

        map = new NoteMapFragment();
        map.setArguments(bundle);
        getFragmentManager().beginTransaction()
            .replace(R.id.note_map, map, VIEW.NOTE_MAP.toString())
            .commit();

        view.findViewById(R.id.element_btn_map)
                .setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(note.getUri()!=null) {

            if(note.getUri().endsWith(".3gp")){
                Picasso.with(this.getActivity())
                        .load(R.drawable.mic)
                        .resizeDimen(R.dimen.note_preview_img_width, R.dimen.note_preview_img_height)
                                //.centerInside()
                        .centerCrop()
                        .into(image);

                /*image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Note", getNote());
                        ((WalkerActivityTest)getActivity()).openFragment(VIEW.NOTE_AUDIO_CAPTURE, bundle);
                    }
                });*/
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        System.out.println(Uri.parse(note.getUri()));
                        intent.setDataAndType(Uri.parse("file://"+note.getUri()), "video/3gpp");

                        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(note.getUri());
                        String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                        System.out.println(Uri.parse(note.getUri()));
                        System.out.println(note.getUri());
                        System.out.println("mimetype: " + mimetype);

                        startActivity(intent);
                        /*
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        //intent.setPackage("com.google.android.music");
                        intent.setDataAndType(Uri.parse(note.getUri()), "video/*");
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        }*/

                        /*Intent intent = new Intent(MediaStore.Audio);
                        intent.setDataAndType(Uri.parse(note.getUri()), "audio/*");
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        }*/

                    }
                });

            }else if(note.getUri().endsWith(".mp4")){

                Picasso.with(this.getActivity())
                        .load(R.drawable.video)
                        .resizeDimen(R.dimen.note_preview_img_width, R.dimen.note_preview_img_height)
                                //.centerInside()
                        .centerCrop()
                        .into(image);

               /* image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Note", getNote());
                        ((WalkerActivityTest)getActivity()).openFragment(VIEW.NOTE_VIDEO_PREVIEW, bundle);
                    }
                });*/

                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file://"+note.getUri()), "video/*");
                        startActivity(intent);
                    }
                });

            }else {
                Picasso.with(this.getActivity())
                        .load(Uri.parse(note.getUri()))
                        .resizeDimen(R.dimen.note_preview_img_width, R.dimen.note_preview_img_height)
                                //.centerInside()
                        .centerCrop()
                        .into(image);

                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file://"+note.getUri()), "image/*");
                        startActivity(intent);
                    }
                });
            }


        }
        if(note.getPoint()!=null){
            lat.setText( (note.getPoint().getGeometry().getLat()).toString() );
            lon.setText( (note.getPoint().getGeometry().getLon()).toString() );
        }
        description.setText(note.getDescription());

        class GenericTextWatcher implements TextWatcher {
            private View view;
            private GenericTextWatcher(View view) {
                this.view = view;
            }
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                switch(view.getId()){
                    case R.id.note_description:
                        note.setDescription(text);
                        break;
                    case R.id.note_point_lat:
                        note.getPoint().setGeometry(new Point(
                                note.getPoint().getGeometry().getLon(),
                                Double.parseDouble(text)
                        ));
                        break;
                    case R.id.note_point_lon:
                        note.getPoint().setGeometry(new Point(
                                Double.parseDouble(text),
                                note.getPoint().getGeometry().getLat()
                        ));
                }
            }
        }

        lat.addTextChangedListener(new GenericTextWatcher(lat));
        lon.addTextChangedListener(new GenericTextWatcher(lon));
        description.addTextChangedListener(new GenericTextWatcher(description));

    }


    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, getActivity().getMenuInflater());
        menu.clear();
        System.out.println("************************************************************************");
        System.out.println("************************* Closed ? " + note.getElement().getParent().isClosed() + "**********");
        System.out.println("************************************************************************");

        if(!note.getElement().getParent().isClosed()){

            System.out.println("  > INFALTING STANDARD MENU");
            inflater.inflate(R.menu.note_form, menu);
            inflater.inflate(R.menu.note_map, menu);

        }else{

            System.out.println("  > INFALTING NAVIGATOR");
            inflater.inflate(R.menu.navigator, menu);
            next = menu.findItem(R.id.action_next);
            prev = menu.findItem(R.id.action_prev);

            if (this.note.getElement().getNotes().size()>1){ // Check if more then one

                int index = this.note.getElement().getNotes().indexOf(this.note);

                if (index==(this.note.getElement().getNotes().size()-1)){ // Check has next
                    next.setVisible(false);
                }
                if (index==0){ // Check has next
                    prev.setVisible(false);
                }

            }else{
                next.setVisible(false);
                prev.setVisible(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int index = this.note.getElement().getNotes().indexOf(this.note);
        int lastIndex = this.note.getElement().getNotes().size()-1;

        switch (item.getItemId()) {

            case R.id.action_prev:

                if (--index==0){ // Check if is first
                    prev.setVisible(false);
                }else {
                    prev.setVisible(true);
                }
                next.setVisible(true);

                System.out.println(">>>>>>>>>>>>>>>>>>>>>>> Previews note: " + index + "/" + lastIndex);

                this.note = this.note.getElement().getNotes().get(index);

                this.onResume();

                return true;

            case R.id.action_next:

                if (++index==lastIndex){ // Check if is last
                    next.setVisible(false);
                }else {
                    next.setVisible(true);
                }
                prev.setVisible(true);

                System.out.println(">>>>>>>>>>>>>>>>>>>>>>> Previews note: " + index + "/" + lastIndex);

                this.note = this.note.getElement().getNotes().get(index);

                this.onResume();



                return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {

        Bundle bundle = new Bundle();
        bundle.putSerializable("Note", this.note);
        bundle.putSerializable("center", this.note.getPoint());
        bundle.putBoolean("geolocation",true);
        bundle.putInt("zoom", 17);
        ((WalkerActivityTest)getActivity()).openFragment(
                VIEW.NOTE_MAP, bundle);

    }

    @Override
    public void onPause(){
        super.onPause();
        getFragmentManager()
                .beginTransaction()
                .remove(map)
                .commit();
    }

}
