package tech.cherri.pxpayplusexample;

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

public class PayByPrimeTask extends AsyncTask<String, Void, JSONObject> {
    private final static String TAG = PayByPrimeTask.class.getSimpleName();
    private final String details;
    private JSONObject jsonRequest;
    private JSONObject extraInfo;
    private JSONObject shopperInfo;
    private JSONObject shippingAddress;
    private JSONObject billingAddress;
    private JSONObject resultUrl;
    private JSONObject cardHolder;
    private String targetUrl;
    private MainActivity mainActivity;

    public PayByPrimeTask(Context context, String prime, String details, MainActivity mainActivity) {
        this.details = details;
        this.mainActivity = mainActivity;

        String mainServerUrl = Constants.TAPPAY_DOMAIN;
        Log.d(TAG,"mainServerUrl: " + mainServerUrl);
        this.targetUrl = mainServerUrl + Constants.TAPPAY_PAY_BY_PRIME_URL;

        jsonRequest = new JSONObject();
        extraInfo = new JSONObject();
        shopperInfo = new JSONObject();
        shippingAddress = new JSONObject();
        billingAddress = new JSONObject();
        resultUrl = new JSONObject();
        cardHolder = new JSONObject();

        try {
//            shippingAddress.put("country_code", "TW");
//            shippingAddress.put("lines", "台北市中正區羅斯福路100號六樓");
//            shippingAddress.put("postcode", "100");
//
//            billingAddress.put("country_code", "TW");
//            billingAddress.put("lines", "台北市中正區羅斯福路100號六樓");
//            billingAddress.put("postcode", "100");

//            shopperInfo.put("shipping_address", shippingAddress);
//            shopperInfo.put("billing_address", billingAddress);
//            extraInfo.put("shopper_info", shopperInfo);

            resultUrl.put("frontend_redirect_url", "https://www.google.com.tw");
            resultUrl.put("backend_notify_url", "https://www.google.com.tw");

            cardHolder.put("phone_number", "+8860924951774");
            cardHolder.put("name", "test");
            cardHolder.put("email", "test@gmail.com");
            cardHolder.put("bank_member_id", "test24951774");

            jsonRequest.put("prime", prime);
            jsonRequest.put("partner_key", Constants.PARTNER_KEY);
            jsonRequest.put("merchant_id", Constants.MERCHANT_ID);
            jsonRequest.put("amount", 8);
            jsonRequest.put("currency", "TWD");
            jsonRequest.put("details", this.details);
//            jsonRequest.put("extra_info", extraInfo);
            jsonRequest.put("result_url", resultUrl);
            jsonRequest.put("cardholder", cardHolder);

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
                    Log.d(TAG, "success, result : " + jsonObject);
                    String paymentUrl = jsonObject.getString("payment_url");
                    String text = "Pay-by-prime: payment_url = " + paymentUrl;
                    this.mainActivity.showMessage(text, true, paymentUrl);
                    break;
                case -1:
                    Log.d(TAG, "failed, result : " + jsonObject);
                    this.mainActivity.showMessage(jsonObject.toString(), false, "");
                    break;
                default:
                    this.mainActivity.showMessage(jsonObject.toString(), false, "");
            }
        } catch (Exception e) {
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
        connection.setRequestProperty("x-api-key", Constants.PARTNER_KEY);
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
