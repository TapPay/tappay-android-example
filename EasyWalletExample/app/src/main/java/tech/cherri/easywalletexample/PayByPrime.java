package tech.cherri.easywalletexample;

import android.content.Context;
import android.view.View;

import org.json.JSONException;

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
