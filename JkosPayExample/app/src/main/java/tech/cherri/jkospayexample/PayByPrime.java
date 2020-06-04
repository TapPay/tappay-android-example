package tech.cherri.jkospayexample;

import android.content.Context;
import android.util.Log;
import android.view.View;

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
import tech.cherri.tpdirect.api.TPDJkoPay;
import tech.cherri.tpdirect.constant.TPDNetworkConstants;
import tech.cherri.tpdirect.utils.SDKLog;

public class PayByPrime implements View.OnClickListener {

    private static final String TAG = "PayByPrime";
    private MainActivity mainActivity;
    private Context context;
    private String merchantId;

    public PayByPrime(MainActivity mainActivity, Context context, String merchantId) {
        this.mainActivity = mainActivity;
        this.context = context;
        this.merchantId = merchantId;
    }

    @Override
    public void onClick(View view) {
        try {
            PayByPrimeTask task = new PayByPrimeTask(mainActivity, context, merchantId);
            task.startTask();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
