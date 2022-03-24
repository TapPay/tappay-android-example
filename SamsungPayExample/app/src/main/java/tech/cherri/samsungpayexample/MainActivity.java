package tech.cherri.samsungpayexample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tech.cherri.tpdirect.api.TPDCard;
import tech.cherri.tpdirect.api.TPDMerchant;
import tech.cherri.tpdirect.api.TPDSamsungPay;
import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.callback.TPDGetPrimeFailureCallback;
import tech.cherri.tpdirect.callback.TPDSamsungPayGetPrimeSuccessCallback;
import tech.cherri.tpdirect.callback.TPDSamsungPayStatusListener;
import tech.cherri.tpdirect.callback.dto.TPDCardDto;
import tech.cherri.tpdirect.callback.dto.TPDCardInfoDto;
import tech.cherri.tpdirect.callback.dto.TPDMerchantReferenceInfoDto;
import tech.cherri.tpdirect.constant.TPDErrorConstants;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, TPDSamsungPayStatusListener, TPDSamsungPayGetPrimeSuccessCallback, TPDGetPrimeFailureCallback {
    private static final String TAG = "MainActivity";

    private TPDCard.CardType[] allowedNetworks = new TPDCard.CardType[]{TPDCard.CardType.Visa
            , TPDCard.CardType.MasterCard};

    private static final int REQUEST_READ_PHONE_STATE = 101;
    private TPDSamsungPay tpdSamsungPay;
    private ImageView samsungPayBuyBTN;
    private TextView totalAmountTV, samsungPayResultStateTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();

        Log.d(TAG, "SDK version is " + TPDSetup.getVersion());

        //Setup environment.
        TPDSetup.initInstance(getApplicationContext(),
                Constants.APP_ID, Constants.APP_KEY, Constants.SERVER_TYPE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions();
        } else {
            prepareSamsungPay();
        }

    }

    private void setupViews() {
        totalAmountTV = (TextView) findViewById(R.id.totalAmountTV);
        totalAmountTV.setText("Total amount : 1.00 元");

        samsungPayBuyBTN = (ImageView) findViewById(R.id.samsungPayBuyBTN);
        samsungPayBuyBTN.setOnClickListener(this);

        samsungPayResultStateTV = (TextView) findViewById(R.id.samsungPayResultStateTV);
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "PERMISSION IS ALREADY GRANTED");
            prepareSamsungPay();
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
                prepareSamsungPay();
                break;
            default:
                break;
        }
    }

    public void prepareSamsungPay() {
        TPDMerchant tpdMerchant = new TPDMerchant();
        tpdMerchant.setMerchantName("TapPay Samsung Pay Demo");
        tpdMerchant.setSupportedNetworks(allowedNetworks);
        tpdMerchant.setSamsungMerchantId(Constants.TEST_SAMSUNG_MERCHANT_ID);
        tpdMerchant.setCurrencyCode("TWD");

        tpdSamsungPay = new TPDSamsungPay(this, Constants.TEST_SAMSUNG_PAY_SERVICE_ID_SANDBOX, tpdMerchant);
        tpdSamsungPay.isSamsungPayAvailable(this);
    }


    @Override
    public void onReadyToPayChecked(boolean isReadyToPay, String msg) {
        Log.d(TAG, "Samsung Pay availability : " + isReadyToPay);
        if (isReadyToPay) {
            samsungPayBuyBTN.setVisibility(View.VISIBLE);
        } else {
            showMessage("Cannot use Samsung Pay.");
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.samsungPayBuyBTN) {
            showProgressDialog();
            tpdSamsungPay.getPrime("1", "0", "0", "1", this, this);
        }
    }

    @Override
    public void onSuccess(String prime, TPDCardInfoDto cardInfo, TPDMerchantReferenceInfoDto merchantReferenceInfo, TPDCardDto card) {
        hideProgressDialog();

        try {
            String resultStr = "prime is " + prime + "\n" +
                    "cardInfo is " + cardInfo + "\n" +
                    "merchantReferenceInfo is " + merchantReferenceInfo + "\n" +
                    "card is " + card + "\n\n" +
                    "Use below cURL to proceed the payment : \n"
                    + ApiUtil.generatePayByPrimeCURLForSandBox(prime, Constants.PARTNER_KEY,
                    Constants.MERCHANT_ID);

            showMessage(resultStr);
            Log.d(TAG, "prime = " + prime);
            Log.d(TAG, "cardInfo = " + cardInfo);
            Log.d(TAG, "merchantReferenceInfo = " + merchantReferenceInfo);
            Log.d(TAG, "card = " + card);
            Log.d(TAG, resultStr);
        } finally {
            Log.d(TAG, "finally");
        }
    }

    @Override
    public void onFailure(int status, String reportMsg) {
        hideProgressDialog();
        if (status == TPDErrorConstants.ERROR_TPDSAMSUNGPAY_CANCELED_BY_USER) {
            //Samsung Pay canceled by User
            showMessage(reportMsg);
        } else {
            showMessage("TapPay getPrime failed , status = " + status + ", msg : " + reportMsg);
            Log.d(TAG, "TapPay getPrime failed : " + status + ", msg : " + reportMsg);
        }
    }

    private void showMessage(String s) {
        samsungPayResultStateTV.setText(s);
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
