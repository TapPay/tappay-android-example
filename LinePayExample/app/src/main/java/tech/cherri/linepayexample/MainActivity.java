package tech.cherri.linepayexample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import tech.cherri.tpdirect.api.TPDCardInfo;
import tech.cherri.tpdirect.api.TPDLinePay;
import tech.cherri.tpdirect.api.TPDLinePayResult;
import tech.cherri.tpdirect.api.TPDServerType;
import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.callback.TPDTokenFailureCallback;
import tech.cherri.tpdirect.callback.TPDTokenSuccessCallback;
import tech.cherri.tpdirect.exception.TPDLinePayException;

public class MainActivity extends AppCompatActivity implements TPDTokenFailureCallback, TPDTokenSuccessCallback, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_READ_PHONE_STATE = 101;
    private RelativeLayout linePayBTN;
    private TPDLinePay tpdLinePay;
    private TextView getPrimeResultStateTV;
    private TextView linePayResultTV;
    private TextView totalAmountTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();

        Log.d(TAG, "SDK version is " + TPDSetup.getVersion());

        handleIncomingIntent(getIntent());

        //Setup environment.
        TPDSetup.initInstance(getApplicationContext(),
                Integer.parseInt(getString(R.string.global_test_app_id)), getString(R.string.global_test_app_key), TPDServerType.Sandbox);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions();
        } else {
            prepareLinePay();
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "PERMISSION IS ALREADY GRANTED");
            prepareLinePay();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            switch (requestCode) {
                case REQUEST_READ_PHONE_STATE:
                    if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                        Log.d(TAG, "PERMISSION_GRANTED");
                    }
                    prepareLinePay();
                    break;
                default:
                    break;
        }
    }

    private void prepareLinePay() {
        boolean isLinePayAvailable = TPDLinePay.isLinePayAvailable(this.getApplicationContext());
        Toast.makeText(this, "isLinePayAvailable : "
                + isLinePayAvailable, Toast.LENGTH_SHORT).show();
        if(isLinePayAvailable){
            try {
                tpdLinePay = new TPDLinePay(getApplicationContext(), AppConstants.TAPPAY_LINEPAY_RESULT_CALLBACK_URI);
            } catch (TPDLinePayException e) {
                showMessage(e.getMessage());
            }
        }
    }

    private void setupViews() {
        totalAmountTV = (TextView) findViewById(R.id.totalAmountTV);
        totalAmountTV.setText("Total amount : 1.00 元");
        getPrimeResultStateTV = (TextView) findViewById(R.id.getPrimeResultStateTV);
        linePayResultTV = (TextView) findViewById(R.id.linePayResultTV);
        linePayBTN = (RelativeLayout) findViewById(R.id.linePayBTN);
        linePayBTN.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        showProgressDialog();
        tpdLinePay.getPrime(this, this);
    }


    @Override
    public void onSuccess(String prime, TPDCardInfo cardInfo) {
        hideProgressDialog();
        String resultStr = "Your prime is " + prime
                + "\n\nUse below cURL to get payment url with Pay-by-Prime API on your server side: \n"
                + ApiUtil.generatePayByPrimeCURLForSandBox(prime,
                getString(R.string.global_test_partnerKey),
                getString(R.string.global_test_merchant_id));

        showMessage(resultStr);
        Log.d(TAG, resultStr);

        //Proceed LINE Pay with below function. (Note : Should not get prime again before redirect.Otherwise, the SDK can't get transaction result correctly.)
//        tpdLinePay.redirectWithUrl("Your payment url ");

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if(intent.getDataString() != null && intent.getDataString().contains(AppConstants.TAPPAY_LINEPAY_RESULT_CALLBACK_URI)){
            TPDLinePayResult result;
            try {
                result = TPDLinePay.parseToLinePayResult(getApplicationContext(), intent.getData());
            } catch (TPDLinePayException e) {
                result = null;
                showMessage(e.getMessage());
            }

            if (result != null) {
                linePayResultTV.setText("status:" + result.getStatus()
                        + "\nrec_trade_id:" + result.getRecTradeId()
                        + "\nbank_transaction_id:" + result.getBankTransactionId()
                        + "\norder_number:" + result.getOrderNumber());
            }
        }
    }

    @Override
    public void onFailure(int status, String reportMsg) {
        hideProgressDialog();
        showMessage("GetPrime failed , status = "+ status + ", msg : " + reportMsg);
    }

    private void showMessage(String s) {
        getPrimeResultStateTV.setText(s);
    }


    public ProgressDialog mProgressDialog;

    protected void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    protected void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}
