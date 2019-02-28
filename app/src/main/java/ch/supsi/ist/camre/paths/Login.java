package ch.supsi.ist.camre.paths;



import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import ch.supsi.ist.camre.paths.utils.Requests;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class Login extends Fragment implements Requests.HandlerInterface, View.OnClickListener {

    private Requests requests;
    private static LoginHandler handler;

    public Login() {
        // Required empty public constructor
    }

    public interface LoginHandler {
        public void onSuccess(JSONObject json);
        public void onFailure(JSONObject json);
        public void onSkip();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_login, container, false);
        Button b = (Button) v.findViewById(R.id.email_sign_in_button);
        b.setOnClickListener(this);
        ((Button) v.findViewById(R.id.email_skip))
                .setOnClickListener(this);
        requests = new Requests(v.getContext().getApplicationContext());
        requests.addHandler(this);
        requests.clearCookies();
        return v;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof LoginHandler){
            handler =(LoginHandler)activity;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.email_sign_in_button:
                System.out.println("logging in!");

                RequestParams params = new RequestParams();
                params.put("email", "user2@camre.ch");
                params.put("password", "123123");
                //params.put("next", "/user"); // redirect to user info

                requests.executePost("login",params);

                break;
            case R.id.email_skip:
                handler.onSkip();
        }
    }

    @Override
    public void onResult(JSONObject json) {
        if (json != null) {

            try {
                if (json.getBoolean("success")) {
                    // User is authenticated
                    handler.onSuccess(json.getJSONObject("data"));
                    Toast.makeText(
                            getActivity(),
                            getText(R.string.welcome) + " " + json.getJSONObject("data").getString("nickname"),
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    // Login error
                    handler.onFailure(json);
                    Toast.makeText(
                            getActivity(),
                            getText(R.string.access_denied),
                            Toast.LENGTH_LONG
                    ).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            requests.executeGet("user");
        }
    }
}
