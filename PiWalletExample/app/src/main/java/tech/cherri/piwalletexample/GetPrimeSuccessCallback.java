package tech.cherri.piwalletexample;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import tech.cherri.tpdirect.callback.TPDPiWalletPayGetPrimeSuccessCallback;

public class GetPrimeSuccessCallback implements TPDPiWalletPayGetPrimeSuccessCallback {
    private final TextView resultText;
    private final MainActivity activity;

    public GetPrimeSuccessCallback(MainActivity mainActivity) {
        resultText = mainActivity.resultText;
        this.activity = mainActivity;
    }

    @Override
    public void onSuccess(String prime) {
        String text = "your prime is = " + prime;
        this.resultText.setText(text);
        activity.showProgressDialog();

        PayByPrimeResultListener listener = activity.resultCallback;

        MyPayByPrimeTaskForPiWallet payByPrimeTask =
                new MyPayByPrimeTaskForPiWallet(prime, Constants.REPLACE_THIS_MERCHANT_ID, listener);

        payByPrimeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


}
