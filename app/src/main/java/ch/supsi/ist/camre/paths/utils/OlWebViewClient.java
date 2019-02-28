package ch.supsi.ist.camre.paths.utils;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.supsi.ist.camre.paths.data.Position;

/**
 * Created by milan antonovic on 06/10/14.
 */
public class OlWebViewClient extends WebViewClient {

    private WebView webView;
    private Bundle bundle;
    private OnMapLoadedListener mListener;

    public interface OnMapLoadedListener {
        public void onMapLoaded();
    }

    public OlWebViewClient(WebView webView, Bundle bundle){
        this.bundle = bundle;
        this.webView = webView;
        this.mListener = null;
    }

    public void registerMapReady(OnMapLoadedListener listener){
        mListener = listener;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        System.out.println("Page is fully loaded!!");
        JSONObject conf = new JSONObject();
        if(bundle!=null){
            if(bundle.containsKey("center")){
                Position position = (Position)bundle.getSerializable("center");
                try {
                    JSONArray coordinates = new JSONArray();
                    coordinates.put(position.getGeometry().getLon());
                    coordinates.put(position.getGeometry().getLat());
                    conf.put("center", coordinates);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //System.out.println("Centered..");
            }
            if(bundle.containsKey("zoom")){
                try {
                    conf.put("zoom",bundle.getInt("zoom"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(bundle.containsKey("geolocation")){
                try {
                    conf.put("geolocation",bundle.getBoolean("geolocation"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        initMap(conf);
        // Notify listener that the map is ready
        if(mListener!=null) {
            mListener.onMapLoaded();
        }
    }


    private void initMap(JSONObject conf){
        //System.out.println(conf.toString());
        this.webView.loadUrl("javascript:initMap("+conf.toString()+");");
    }

}
