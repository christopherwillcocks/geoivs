package ch.supsi.ist.camre.paths.utils;

/**
 * Created by milan antonovic on 22/10/14.
 *
 * Using http://loopj.com/android-async-http/
 *
 */

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

/**
 * Created by milan on 08/07/14.
 */
public class Requests extends AsyncHttpResponseHandler{

    private AsyncHttpClient client;

    private PersistentCookieStore cookieStore;
    private Context context;

    private String server = "http://localhost:8888";

    List<HandlerInterface> handlers;

    public Requests(Context context){
        client = new AsyncHttpClient();
        if(context!=null){
            this.context = context;
            this.cookieStore = new PersistentCookieStore(this.context);
            List<Cookie> cookies = this.cookieStore.getCookies();
            for (Cookie cookie: cookies){
                System.out.println(cookie.getName() + ": " + cookie.getValue());
            }
            client.setCookieStore(this.cookieStore);
        }
        client.setEnableRedirects(false);
        handlers = new ArrayList<HandlerInterface>(0);
    }

    public void clearCookies(){

        List<Cookie> cookies = this.cookieStore.getCookies();

        System.out.println("DELETING COOKIES:");
        for (Cookie cookie: cookies){
            System.out.println(cookie.getName() + ": " + cookie.getValue());
            this.cookieStore.deleteCookie(cookie);
        }

        this.cookieStore.clear();

        System.out.println("AFTER CLEAR:");
        for (Cookie cookie: cookies){
            System.out.println(cookie.getName() + ": " + cookie.getValue());
            this.cookieStore.deleteCookie(cookie);
        }

        client.setCookieStore(this.cookieStore);
    }

    public void addHandler(HandlerInterface handler){
        try {
            handlers.add(handler);
        } catch (ClassCastException e) {
            throw new ClassCastException(handler.toString()
                    + " must implement HandlerInterface");
        }
    }

    public interface HandlerInterface {
        // TODO: Update argument type and name
        public void onResult(JSONObject json);
    }

    public void executeGet(String path){
        this.executeGet(path, null);
    }

    public void executeGet(String path, RequestParams params){
        String url = server+"/"+path;
        if (path.indexOf("http")==0 || path.indexOf("https")==0){
            url = path;
        }
        System.out.println("Executing GET: " + url+path);
        client.get(url, params, this);
    }

    /**
     * Execute a POST request passing a JSONObject as body
     */
    public void executePost(String path, JSONObject json) throws UnsupportedEncodingException {
        this.executePost(path, new StringEntity(json.toString()));
    }

    /**
     * Execute a POST request passing a StringEntity as body
     */
    public void executePost(String path, StringEntity entity){
        String url = server+"/";
        if (path.indexOf("http://")==0 || path.indexOf("https://")==0){
            url = path;
        }
        client.post(this.context, url+path, entity, "", this);
    }

    /**
     * Execute a POST request passing form parameters
     *
     *   RequestParams params = new RequestParams();
     *   params.put("email", "user@camre.ch");
     *   params.put("password", "123123");
     *
     */
    public void executePost(String path, RequestParams params){
        String url = server+"/";
        if (path.indexOf("http://")==0 || path.indexOf("https://")==0){
            url = path;
        }
        System.out.println("Connecting to " + url+path + " ? " + params.toString());
        client.post(this.context, url+path, params, this);
    }

    @Override
    public synchronized void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

        String msg = String.format(Locale.US, "Return Status Code: %d", statusCode);
        System.out.println(msg);

        if (headers != null) {
            StringBuilder builder = new StringBuilder();
            System.out.println("HEADERS: ");
            for (Header h : headers) {
                String _h = String.format(Locale.US, "%s : %s", h.getName(), h.getValue());
                System.out.println(" > " + _h);
                builder.append(_h);
                builder.append("\n");
            }
        }

        String responseString = new String(responseBody);

        System.out.println("Response: " + responseString);

        System.out.println("Handlers: ");
        System.out.println(handlers.size());

        for (ListIterator<HandlerInterface> iter = handlers.listIterator(); iter.hasNext(); ) {
            HandlerInterface element = iter.next();
            try {
                element.onResult(new JSONObject(responseString));
            } catch (JSONException e) {
                e.printStackTrace();
                element.onResult(null);
            }
        }

    }

    @Override
    public synchronized void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

        String msg = String.format(Locale.US, "Return Status Code: %d", statusCode);
        System.out.println(msg);

        if (headers != null) {
            StringBuilder builder = new StringBuilder();
            for (Header h : headers) {
                String _h = String.format(Locale.US, "%s : %s", h.getName(), h.getValue());
                System.out.println(_h);
                builder.append(_h);
                builder.append("\n");
            }
        }



        System.out.println("AsyncHttpClient returned error: " + error.getMessage());

        if (responseBody != null) {
            System.out.println("Response: " + new String(responseBody));
        }


        for (ListIterator<HandlerInterface> iter = handlers.listIterator(); iter.hasNext(); ) {
            HandlerInterface element = iter.next();
            if(statusCode==302) {
                element.onResult(null);
            }
        }

    }

    @Override
    public void onRetry(int retryNo) {
        super.onRetry(retryNo);


    }
}