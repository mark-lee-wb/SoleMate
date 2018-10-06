package solemate.solemate;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ApiConnector { //Connector class
    public JSONObject GetYourJson()
    {
        // URL for getting the json
        String url = "https://api.thingspeak.com/channels/595680/fields/1/last.json";

        // Get HttpResponse Object from url.
        // Get HttpEntity from Http Response Object
        HttpEntity httpEntity = null;

        try
        {
            DefaultHttpClient httpClient = new DefaultHttpClient();  // Default HttpClient
            HttpGet httpGet = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            httpEntity = httpResponse.getEntity();

        } catch (ClientProtocolException e) {

            // Signals error in http protocol
            e.printStackTrace();

            //Log Errors Here

        } catch (IOException e) {
            e.printStackTrace();
        }


        // Convert HttpEntity into JSON Array
        JSONObject jsonObject = null;
        String entityResponse = null;
        System.out.println("httpentity" + httpEntity);

        if (httpEntity != null) {
            try {
                entityResponse = EntityUtils.toString(httpEntity);
                Log.e("Entity Response  : ", entityResponse);
                jsonObject = new JSONObject(entityResponse);
                System.out.println("fkme" + jsonObject);

            }  catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

}
