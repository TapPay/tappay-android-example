package tech.cherri.piwalletexample;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyPayByPrimeTaskForPiWallet extends AsyncTask<String, Void, JSONObject> {

    private final JSONObject jsonRequest;
    private String targetUrl = Constants.REPLACE_THIS_PAY_BY_PRIME_DOMAIN;
    private final PayByPrimeResultListener listener;

    public MyPayByPrimeTaskForPiWallet(String prime, String merchantId, PayByPrimeResultListener listener) {
        jsonRequest = new JSONObject();
        this.listener = listener;


//        String secretString = "[{\"item_id\": \"" + itemId
//                + "\",\"item_name\": \"" + itemName
//                + " \",\"item_quantity\":" + itemQuantity
//                + ",\"item_price\":" + itemPrice + "}]";
        try {
            jsonRequest.put("prime", prime);
            jsonRequest.put("merchant_id", merchantId);
            jsonRequest.put("amount", 50);
//            jsonRequest.put("details", secretString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        JSONObject outputJSONObj = new JSONObject();

        try {
            HttpURLConnection connection = prepareHttpURLConnection();
            writeRequestTo(connection);

            int HttpResult = connection.getResponseCode();
            Log.d("test", "HttpResult = " + HttpResult);

            String HttpMsgResult = connection.getResponseMessage();
            Log.d("test", "HttpMsgResult = " + HttpMsgResult);


            String responseStr = getResponseString(connection);
            Log.d("test", "response = " + responseStr);

            outputJSONObj = new JSONObject(responseStr); // Get entity
        } catch (Exception e) {
            e.printStackTrace();
            outputJSONObj = new JSONObject();
            try {
                outputJSONObj.put("status", -1);
                outputJSONObj.put("exception", e.toString());
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
        return outputJSONObj;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {

        try {
            int status = jsonObject.getInt("status");
            switch (status) {
                case 0:
                    listener.onTaskSuccess(jsonObject);
                    break;
                case -1:
                    String exceptionString = jsonObject.getString("exception");
                    listener.onTaskFailed(exceptionString);
                    break;
                default:
                    listener.onTaskFailed(jsonObject.toString());
            }
        } catch (Exception e) {
            listener.onTaskFailed(e.toString());
            e.printStackTrace();
        }

    }

    @NonNull
    private HttpURLConnection prepareHttpURLConnection() throws IOException {
        URL url = new URL(targetUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(40000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        return connection;
    }

    private void writeRequestTo(HttpURLConnection connection) throws IOException {
        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
        wr.write(jsonRequest.toString());
        Log.d("test", "request = " + jsonRequest.toString());
        wr.flush();
        wr.close();
    }

    @NonNull
    private String getResponseString(HttpURLConnection connection) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"));
        StringBuilder responseStr = new StringBuilder("");
        String line = "";

        while ((line = br.readLine()) != null) {
            responseStr.append(line);
        }
        br.close();
        return responseStr.toString();
    }

}
