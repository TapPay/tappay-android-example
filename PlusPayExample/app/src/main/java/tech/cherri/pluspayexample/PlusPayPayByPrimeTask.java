package tech.cherri.pluspayexample;

import android.content.Context;
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

import tech.cherri.tpdirect.utils.SDKLog;

public class PlusPayPayByPrimeTask extends AsyncTask<String, Void, JSONObject> {
    private static final String TAG = PlusPayPayByPrimeTask.class.getSimpleName();
    private final String prime;
    private final ResultListener listener;
    private final String details;
    private JSONObject jsonRequest;
    private JSONObject extraInfo;
    private JSONObject shopperInfo;
    private JSONObject shippingAddress;
    private JSONObject billingAddress;
    private JSONObject resultUrl;
    private JSONObject cardHolder;
    private String targetUrl;

    public PlusPayPayByPrimeTask(Context context, String prime, String details, ResultListener listener) {
        this.prime = prime;
        this.details = details;
        this.listener = listener;
        this.targetUrl = Constants.TAPPAY_DOMAIN + Constants.TAPPAY_PAY_BY_PRIME_URL;

        jsonRequest = new JSONObject();
        extraInfo = new JSONObject();
        shopperInfo = new JSONObject();
        shippingAddress = new JSONObject();
        billingAddress = new JSONObject();
        resultUrl = new JSONObject();
        cardHolder = new JSONObject();

        try {
            shippingAddress.put("country_code", "TW");
            shippingAddress.put("lines", "台北市中正區羅斯福路100號六樓");
            shippingAddress.put("postcode", "100");

            billingAddress.put("country_code", "TW");
            billingAddress.put("lines", "台北市中正區羅斯福路100號六樓");
            billingAddress.put("postcode", "100");

            shopperInfo.put("shipping_address", shippingAddress);
            shopperInfo.put("billing_address", billingAddress);
            extraInfo.put("shopper_info", shopperInfo);

            resultUrl.put("frontend_redirect_url", Constants.FRONTEND_REDIRECT_URL_EXAMPLE);
            resultUrl.put("backend_notify_url", Constants.BACKEND_NOTIFY_URL_EXAMPLE);

            cardHolder.put("phone_number", "+8860932123456");
            cardHolder.put("name", "test");
            cardHolder.put("email", "test@gmail.com");

            jsonRequest.put("prime", prime);
            jsonRequest.put("partner_key", Constants.PARTNER_KEY);
            jsonRequest.put("merchant_id", Constants.MERCHANT_ID);
            jsonRequest.put("amount", 50);
            jsonRequest.put("details", this.details);
            jsonRequest.put("extra_info", extraInfo);
            jsonRequest.put("result_url", resultUrl);
            jsonRequest.put("cardholder", cardHolder);

        } catch (JSONException e) {
            Log.e(TAG,"PlusPayPayByPrimeTask error: " + Log.getStackTraceString(e));
        }
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        JSONObject outputJSONObj = new JSONObject();

        try {
            HttpURLConnection connection = prepareHttpURLConnection();
            writeRequestTo(connection);

            Log.d(TAG,"doInBackground responseCode: " + connection.getResponseCode());
            Log.d(TAG,"doInBackground responseMessage(): " + connection.getResponseMessage());

            String responseStr = getResponseString(connection);
            Log.d(TAG,"doInBackground responseStr: " + responseStr);

            outputJSONObj = new JSONObject(responseStr);
        } catch (Exception e) {
            Log.e(TAG,"PlusPayPayByPrimeTask doInBackground error: " + Log.getStackTraceString(e));
            outputJSONObj = new JSONObject();
            try {
                outputJSONObj.put("status", -1);
                outputJSONObj.put("exception", e.toString());
            } catch (JSONException jsonException) {
                Log.e(TAG,"PlusPayPayByPrimeTask doInBackground error: " + Log.getStackTraceString(e));
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
            Log.e(TAG,"PlusPayPayByPrimeTask onPostExecute error: " + Log.getStackTraceString(e));
            listener.onTaskFailed(e.toString());
        }

    }

    @NonNull
    private HttpURLConnection prepareHttpURLConnection() throws IOException {
        URL url = new URL(targetUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(40000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("x-api-key", Constants.PARTNER_KEY);
        connection.setRequestMethod("POST");
        return connection;
    }

    private void writeRequestTo(HttpURLConnection connection) throws IOException {
        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
        wr.write(jsonRequest.toString());
        Log.d(TAG,"writeRequestTo request: " + jsonRequest.toString());
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
