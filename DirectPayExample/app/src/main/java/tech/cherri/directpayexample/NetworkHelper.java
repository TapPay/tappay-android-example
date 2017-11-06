package tech.cherri.directpayexample;

/**
 * Created by Amber on 2017/1/17.
 */

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;


public class NetworkHelper {

    private int timeoutConnection = 4000;
    private int timeoutSO = 30000;


    // Setup parameters
    private boolean retryIfNetworkFailed = false;
    private JSONObject inputJSONObj;
    private String targetURL;
    private String appKey;


    public NetworkHelper(String url) {
        inputJSONObj = new JSONObject();
        targetURL = url;
    }


    public JSONObject doPost() {
        StringBuilder chaine = new StringBuilder("");
        JSONObject outputJSONObj = new JSONObject();
        try {
            outputJSONObj.put("respcode", Integer.MAX_VALUE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try{
            URL url = new URL(targetURL);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(timeoutConnection);
            connection.setReadTimeout(timeoutSO);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("x-api-key", appKey);
            connection.setRequestProperty("Content-Language", Locale.getDefault().getLanguage());
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(inputJSONObj.toString());
            wr.flush();
            wr.close();
            int HttpResult = connection.getResponseCode();
            String HttpMsgResult = connection.getResponseMessage();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
            String line = "";
            while ((line = br.readLine()) != null) {
                chaine.append(line);
            }
            br.close();
            try {
                outputJSONObj = new JSONObject(chaine.toString()); // Get entity
                outputJSONObj.put("respcode", HttpResult);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            // writing exception to log
            e.printStackTrace();
        }
        return outputJSONObj;
    }



    public NetworkHelper addInputEntity(String key, Object value) {
        try {
            inputJSONObj.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }
}

