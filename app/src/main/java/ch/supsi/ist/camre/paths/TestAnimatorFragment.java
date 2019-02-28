package ch.supsi.ist.camre.paths;



import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ViewAnimator;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class TestAnimatorFragment extends Fragment {

    Button btnNext,btnPrevious;
    ViewAnimator viewAnimator;

    public TestAnimatorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_test_animator, container, false);

        viewAnimator = (ViewAnimator) view.findViewById(R.id.viewAnimator);

        final Animation inAnim = AnimationUtils.loadAnimation(view.getContext(), android.R.anim.slide_in_left);
        final Animation outAnim = AnimationUtils.loadAnimation(view.getContext(),android.R.anim.fade_in);

        viewAnimator.setInAnimation(inAnim);
        viewAnimator.setOutAnimation(outAnim);

        /*viewAnimator.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                viewAnimator.showNext();
            }
        });*/

        Button btn1 = (Button) view.findViewById(R.id.buttonNext);
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                viewAnimator.showPrevious();
            }
        });

        Button btn2 = (Button) view.findViewById(R.id.buttonPrevious);
        btn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                viewAnimator.showNext();
            }
        });

        return view;
    }


}
