package ch.supsi.ist.camre.paths;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ch.supsi.ist.camre.paths.data.Element;
import ch.supsi.ist.camre.paths.data.Path;


/**
 * A simple {@link Fragment} subclass.
 */
public class Proj4js extends Fragment{

    private ProjListener projListener;

    private WebView webview;

    private ArrayList<ArrayList<Double>> resultPoints;
    private ArrayList<ArrayList<Double>> initalPoints;
    private int index;
    private int maxIndex;

    public interface OnPageReadyListener {
        public void onReady();
    }

    public interface ProjListener {
        public void onReady();
        public void onPathReady(JSONObject result);
    }

    public Proj4js() {}

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        webview = (WebView)inflater.inflate(R.layout.fragment_proj4js, container, false);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.loadUrl("file:///android_asset/www/proj.html");
        webview.addJavascriptInterface(this, "Camre");
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (projListener != null) {
                    projListener.onReady();
                }
            }
        });
        return webview;
    }

    public void registerProjReady(ProjListener projListener){
        this.projListener = projListener;
    }

    public void initProj(){
        resultPoints = new ArrayList<ArrayList<Double>>();
        index=0;
    }

    public void transformPath(Path path){

        // Preparing path
        JSONArray geometry = new JSONArray();
        for (ArrayList<Double> coord : path.getGeometry().getCoordinates()) {
            JSONArray point = new JSONArray();
            point.put(coord.get(0));
            point.put(coord.get(1));
            geometry.put(point);
        }

        JSONObject json = new JSONObject();
        try {
            json.put("geometry", geometry);
            json.put("surface", elementToJSONArray(path.getSurface()));
            json.put("leftside", elementToJSONArray(path.getLeftSide()));
            json.put("rightside", elementToJSONArray(path.getRightSide()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        webview.loadUrl("javascript: convertPath("+json.toString()+")");

    }

    private JSONArray elementToJSONArray(ArrayList<Element> elements){
        JSONArray elementArray = new JSONArray();
        for (Element element: elements){
            JSONArray segment = new JSONArray();
            JSONArray point = new JSONArray();
            point.put(element.getStartPoint().getGeometry().getCoordinates().get(0));
            point.put(element.getStartPoint().getGeometry().getCoordinates().get(1));
            segment.put(point);
            point = new JSONArray();
            if(element.getEndPoint()!=null){
                point.put(element.getEndPoint().getGeometry().getCoordinates().get(0));
                point.put(element.getEndPoint().getGeometry().getCoordinates().get(1));
                segment.put(point);
            }else{
                segment.put(JSONObject.NULL);
            }
            elementArray.put(segment);
        }
        return elementArray;
    }

    @JavascriptInterface
    public void result(String result){
        try {
            projListener.onPathReady(new JSONObject(result));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
