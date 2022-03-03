package tech.cherri.atomepayexample;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import tech.cherri.tpdirect.utils.SDKLog;

public class MyAtomePayByPrimeTask extends AsyncTask<String, Void, JSONObject> {
    private final String prime;
    private final ResultListener listener;
    private final String merchantId;
    private final String secretString;
    private JSONObject jsonRequest;
    private String targetUrl;

    public MyAtomePayByPrimeTask(Context context, String prime, String merchantId, String secretStringRequiredByAtomePay, ResultListener listener) {
        this.prime = prime;
        this.merchantId = merchantId;
        this.secretString = secretStringRequiredByAtomePay;
        this.listener = listener;
        this.targetUrl = Constants.PAY_BY_PRIME_URL;

        jsonRequest = new JSONObject();

        try {
            jsonRequest.put("prime", prime);
            jsonRequest.put("merchant_id", merchantId);
            jsonRequest.put("amount", 50);
            jsonRequest.put("details", secretString);
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
            SDKLog.d("test", "HttpResult = " + HttpResult);

            String HttpMsgResult = connection.getResponseMessage();
            SDKLog.d("test", "HttpMsgResult = " + HttpMsgResult);


            String responseStr = getResponseString(connection);
            SDKLog.d("test", "response = " + responseStr);

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
        SDKLog.d("test", "request = " + jsonRequest.toString());
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
