package tech.cherri.jkospayexample;

import android.util.Log;
import android.view.View;

import tech.cherri.tpdirect.api.TPDCardInfo;
import tech.cherri.tpdirect.api.TPDJkoPay;
import tech.cherri.tpdirect.callback.TPDTokenFailureCallback;
import tech.cherri.tpdirect.callback.TPDTokenSuccessCallback;

public class GetPrime implements View.OnClickListener, TPDTokenFailureCallback, TPDTokenSuccessCallback {

    private MainActivity mainActivity;
    private TPDJkoPay tpdJkoPay;

    public GetPrime(MainActivity mainActivity, TPDJkoPay tpdJkoPay) {
        this.mainActivity = mainActivity;
        this.tpdJkoPay = tpdJkoPay;
    }

    @Override
    public void onClick(View view) {
        if (tpdJkoPay != null) {
            Log.i("GetPrime", "tpdJkoPay != null");
            tpdJkoPay.getPrime(this, this);
        }else{
            Log.i("GetPrime", "tpdJkoPay == null");
        }
    }

    @Override
    public void onSuccess(String prime, TPDCardInfo cardInfo) {
        mainActivity.hideProgressDialog();
        mainActivity.showMessage("Prime：" + prime + "\n");
        mainActivity.prime = prime;
    }

    @Override
    public void onFailure(int status, String reportMsg) {
        mainActivity.hideProgressDialog();
        mainActivity.showMessage("GetPrime failed , status = " + status + ", msg : " + reportMsg);
    }
}
