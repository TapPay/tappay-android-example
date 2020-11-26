package tech.cherri.jkospayexample;

import android.util.Log;
import android.view.View;

import tech.cherri.tpdirect.api.TPDJkoPay;
import tech.cherri.tpdirect.callback.TPDGetPrimeFailureCallback;
import tech.cherri.tpdirect.callback.TPDJkoPayGetPrimeSuccessCallback;

public class GetPrime implements View.OnClickListener, TPDGetPrimeFailureCallback, TPDJkoPayGetPrimeSuccessCallback {

    private MainActivity mainActivity;
    private TPDJkoPay tpdJkoPay;

    public GetPrime(MainActivity mainActivity, TPDJkoPay tpdJkoPay) {
        this.mainActivity = mainActivity;
        this.tpdJkoPay = tpdJkoPay;
    }

    @Override
    public void onClick(View view) {
        tpdJkoPay.getPrime(this, this);
    }

    @Override
    public void onSuccess(String prime) {
        mainActivity.hideProgressDialog();
        mainActivity.showMessage("Prime：" + prime + "\n");
        mainActivity.prime = prime;
    }

    @Override
    public void onFailure(int status, String msg) {
        mainActivity.hideProgressDialog();
        mainActivity.showMessage("GetPrime failed , status = " + status + ", msg : " + msg);
    }
}
