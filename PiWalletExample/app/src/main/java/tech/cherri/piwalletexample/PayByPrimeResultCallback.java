package tech.cherri.piwalletexample;

import org.json.JSONObject;

public class PayByPrimeResultCallback implements PayByPrimeResultListener {
    private final MainActivity activity;

    public PayByPrimeResultCallback(MainActivity mainActivity) {
        this.activity = mainActivity;
    }

    @Override
    public void onTaskSuccess(JSONObject jsonObject) {
        activity.hideProgressDialog();
        try {
            String paymentUrl = jsonObject.getString("payment_url");
            String text = "Pay-by-prime: payment_url = " + paymentUrl;
            activity.resultText.setText(text);
            activity.tpdPiWalletPay.redirectWithUrl(paymentUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskFailed(String resultString) {
        activity.hideProgressDialog();
        activity.resultText.setText(resultString);
    }
}
