package tech.cherri.piwalletexample;

import android.app.ProgressDialog;
import android.widget.TextView;

import tech.cherri.tpdirect.callback.TPDGetPrimeFailureCallback;

public class GetPrimeFailCallback implements TPDGetPrimeFailureCallback {
    private final TextView resultText;
    private final ProgressDialog progressDialog;

    public GetPrimeFailCallback(TextView textView, ProgressDialog progressDialog) {
        resultText = textView;
        this.progressDialog = progressDialog;
    }

    @Override
    public void onFailure(int status, String msg) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.resultText.setText(msg);
    }
}
