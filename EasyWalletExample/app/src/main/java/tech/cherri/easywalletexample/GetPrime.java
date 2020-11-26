package tech.cherri.easywalletexample;

import android.util.Log;
import android.view.View;

import tech.cherri.tpdirect.api.TPDEasyWallet;
import tech.cherri.tpdirect.callback.TPDEasyWalletGetPrimeSuccessCallback;
import tech.cherri.tpdirect.callback.TPDGetPrimeFailureCallback;

public class GetPrime implements View.OnClickListener, TPDGetPrimeFailureCallback, TPDEasyWalletGetPrimeSuccessCallback {

    private MainActivity mainActivity;
    private TPDEasyWallet tpdEasyWallet;

    public GetPrime(MainActivity mainActivity, TPDEasyWallet tpdEasyWallet) {
        this.mainActivity = mainActivity;
        this.tpdEasyWallet = tpdEasyWallet;
    }

    @Override
    public void onClick(View view) {
        if (tpdEasyWallet != null) {
            tpdEasyWallet.getPrime(this, this);
        } else {
            Log.i("GetPrime", "tpdEasyWallet == null");
        }
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
