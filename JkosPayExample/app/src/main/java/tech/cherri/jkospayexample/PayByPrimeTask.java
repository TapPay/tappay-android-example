package tech.cherri.jkospayexample;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import tech.cherri.tpdirect.api.TPDAPIHelper;
import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.constant.TPDErrorConstants;
import tech.cherri.tpdirect.constant.TPDNetworkConstants;
import tech.cherri.tpdirect.model.TPDTaskListener;
import tech.cherri.tpdirect.utils.SDKLog;


public class PayByPrimeTask extends AsyncTask<JSONObject, Void, JSONObject> {

    private static final String TAG = "PayByPrimeTask";
    private WeakReference<Context> weakContext;
    private JSONObject requestJO;
    private int timeoutConnection = 4000;
    private int timeoutSO = 30000;
    private MainActivity mainActivity;

    public PayByPrimeTask(MainActivity mainActivity, Context context, String merchantId) throws
            JSONException {
        this.weakContext = new WeakReference<>(context);
        this.mainActivity = mainActivity;

        JSONObject json = new JSONObject();
        json.put("partner_key", Constants.PARTNER_KEY);
        json.put("prime", mainActivity.prime);
        json.put("merchant_id", merchantId);
        json.put("amount", 1);
        json.put("currency", "TWD");
        json.put("order_number", "SN0001");
        json.put("details", "item descriptions");
        JSONObject cardHolderJO = new JSONObject();
        cardHolderJO.put("phone_number", "+886912345678");
        cardHolderJO.put("name", "Cardholder");
        cardHolderJO.put("email", "Cardholder@email.com");

        json.put("cardholder", cardHolderJO);

        JSONObject resultUrlJO = new JSONObject();
        resultUrlJO.put("frontend_redirect_url", Constants.FRONTEND_REDIRECT_URL_EXAMPLE);
        resultUrlJO.put("backend_notify_url", Constants.BACKEND_NOTIFY_URL_EXAMPLE);

        json.put("result_url", resultUrlJO);
        this.requestJO = json;
    }

    public void startTask() {
        this.execute(requestJO);
    }


    @Override
    protected JSONObject doInBackground(JSONObject... requestJOArr) {
        StringBuilder stringBuilder = new StringBuilder("");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(TPDNetworkConstants.KEY_RESPOND_CODE, Integer.MAX_VALUE);

            Context context = weakContext.get();
            if (context == null) {
                return null;
            }

            URL url = new URL(Constants.TAPPAY_DOMAIN + Constants.TAPPAY_PAY_BY_PRIME_URL);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(timeoutConnection);
            urlConnection.setReadTimeout(timeoutSO);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Content-Language", Locale.getDefault().getLanguage());
            urlConnection.setRequestProperty("x-api-key", Constants.PARTNER_KEY);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);

            //For TLS1.2
//            TPNetworkHelper.updateSecurityProvider(context, urlConnection);

            //For certificate pinning
//            TPNetworkHelper.validatePinning(urlConnection);

            Log.i(TAG, "request=" + requestJOArr[0].toString());

            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(requestJOArr[0].toString());
            wr.flush();
            wr.close();
            int responseCode = urlConnection.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),
                    "utf-8"));
            String singleLine;
            while ((singleLine = br.readLine()) != null) {
                stringBuilder.append(singleLine);
            }

            br.close();

            Log.i(TAG, "response=" + stringBuilder.toString());
            jsonObject = new JSONObject(stringBuilder.toString());
            jsonObject.put(TPDNetworkConstants.KEY_RESPOND_CODE, responseCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    protected void onPostExecute(JSONObject json) {
        super.onPostExecute(json);
        try {
            int httpRespondCode = json.getInt(TPDNetworkConstants.KEY_RESPOND_CODE);
            if (httpRespondCode == HttpURLConnection.HTTP_OK) {
                if (json.getInt("status") == 0) {
                    String paymentUrl = json.getString("payment_url");
                    mainActivity.paymentUrl = paymentUrl;
                    mainActivity.showMessage("Payment Url：" + paymentUrl);
                } else {
                    mainActivity.showMessage("Pay by prime error with status: " + json.getInt("status") + ", msg: " + json.getString("msg"));
                }
            } else {
                mainActivity.showMessage("Network error with httpRespondCode：" + httpRespondCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
