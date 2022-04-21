package tech.cherri.piwalletexample;

import android.os.AsyncTask;
import android.widget.TextView;

import tech.cherri.tpdirect.callback.TPDPiWalletGetPrimeSuccessCallback;

public class GetPrimeSuccessCallback implements TPDPiWalletGetPrimeSuccessCallback {
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

        String details = "[{\"item_id\": \"" + "item_1"
                + "\",\"item_name\": \"" + "item_No_1"
                + " \",\"item_quantity\":" + "1"
                + ",\"item_price\":" + "100" + "}]";

        PayByPrimeResultListener listener = activity.resultCallback;

        MyPayByPrimeTaskForPiWallet payByPrimeTask =
                new MyPayByPrimeTaskForPiWallet(prime, listener, details);

        payByPrimeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


}
