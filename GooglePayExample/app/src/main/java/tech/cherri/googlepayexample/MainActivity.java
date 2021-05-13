package tech.cherri.googlepayexample;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.WalletConstants;

import org.json.JSONException;
import org.json.JSONObject;

import tech.cherri.tpdirect.api.TPDCard;
import tech.cherri.tpdirect.api.TPDConsumer;
import tech.cherri.tpdirect.api.TPDGooglePay;
import tech.cherri.tpdirect.api.TPDMerchant;
import tech.cherri.tpdirect.api.TPDServerType;
import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.callback.TPDGetPrimeFailureCallback;
import tech.cherri.tpdirect.callback.TPDGooglePayGetPrimeSuccessCallback;
import tech.cherri.tpdirect.callback.TPDGooglePayListener;
import tech.cherri.tpdirect.callback.dto.TPDCardInfoDto;
import tech.cherri.tpdirect.callback.dto.TPDMerchantReferenceInfoDto;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        TPDGooglePayListener, TPDGetPrimeFailureCallback, TPDGooglePayGetPrimeSuccessCallback {
    private static final String TAG = "MainActivity";
    private TPDCard.CardType[] allowedNetworks = new TPDCard.CardType[]{TPDCard.CardType.Visa
            , TPDCard.CardType.MasterCard
            , TPDCard.CardType.JCB
            , TPDCard.CardType.AmericanExpress};
    private TPDCard.AuthMethod[] allowedAuthMethods = new TPDCard.AuthMethod[]{TPDCard.AuthMethod.Cryptogram3DS};
    private static final int REQUEST_READ_PHONE_STATE = 101;
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 102;
    private TPDGooglePay tpdGooglePay;
    private RelativeLayout googlePaymentBuyBTN;
    private TextView totalAmountTV, googlePaymentResultStateTV;
    private TextView buyerInformationTV;
    private PaymentData paymentData;
    private Button confirmBTN;

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
            prepareGooglePay();
        }

    }

    private void setupViews() {
        totalAmountTV = (TextView) findViewById(R.id.totalAmountTV);
        totalAmountTV.setText("Total amount : 1.00 元");

        googlePaymentBuyBTN = (RelativeLayout) findViewById(R.id.googlePaymentBuyBTN);
        googlePaymentBuyBTN.setOnClickListener(this);
        googlePaymentBuyBTN.setEnabled(false);

        buyerInformationTV = (TextView) findViewById(R.id.buyerInformationTV);

        confirmBTN = (Button) findViewById(R.id.confirmBTN);
        confirmBTN.setOnClickListener(this);
        confirmBTN.setEnabled(false);

        googlePaymentResultStateTV = (TextView) findViewById(R.id.googlePaymentResultStateTV);
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "PERMISSION IS ALREADY GRANTED");
            prepareGooglePay();
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
                prepareGooglePay();
                break;
            default:
                break;
        }
    }

    public void prepareGooglePay() {
        TPDMerchant tpdMerchant = new TPDMerchant();
        tpdMerchant.setSupportedNetworks(allowedNetworks);
        tpdMerchant.setMerchantName(Constants.TEST_MERCHANT_NAME);
        tpdMerchant.setSupportedAuthMethods(allowedAuthMethods);

        TPDConsumer tpdConsumer = new TPDConsumer();
        tpdConsumer.setPhoneNumberRequired(false);
        tpdConsumer.setShippingAddressRequired(false);
        tpdConsumer.setEmailRequired(false);

        tpdGooglePay = new TPDGooglePay(this, tpdMerchant, tpdConsumer);
        tpdGooglePay.isGooglePayAvailable(this);
    }


    @Override
    public void onReadyToPayChecked(boolean isReadyToPay, String msg) {
        Log.d(TAG, "Pay with Google availability : " + isReadyToPay + ", msg : " + msg);
        if (isReadyToPay) {
            googlePaymentBuyBTN.setEnabled(true);
        } else {
            showMessage("Cannot use Pay with Google.");
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.googlePaymentBuyBTN) {
            tpdGooglePay.requestPayment(TransactionInfo.newBuilder()
                    .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                    .setTotalPrice("1")
                    .setCurrencyCode("TWD")
                    .build(), LOAD_PAYMENT_DATA_REQUEST_CODE);
        } else if (view.getId() == R.id.confirmBTN) {
            getPrimeFromTapPay(paymentData);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        confirmBTN.setEnabled(true);
                        paymentData = PaymentData.getFromIntent(data);
                        revealPaymentInfo(paymentData);
                        break;
                    case Activity.RESULT_CANCELED:
                        confirmBTN.setEnabled(false);
                        showMessage("Canceled by User");
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        confirmBTN.setEnabled(false);
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        Log.d(TAG, "AutoResolveHelper.RESULT_ERROR : " + status.getStatusCode() + " , message = " + status.getStatusMessage());
                        showMessage(status.getStatusCode() + " , message = " + status.getStatusMessage());
                        break;
                    default:
                        // Do nothing.
                }
                break;
            default:
                // Do nothing.
        }
    }

    private void revealPaymentInfo(PaymentData paymentData) {
        try {
            JSONObject paymentDataJO = new JSONObject(paymentData.toJson());
            String cardDescription = paymentDataJO.getJSONObject("paymentMethodData").getString
                    ("description");

            buyerInformationTV.setText("Card Description : " + cardDescription + "\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getPrimeFromTapPay(PaymentData paymentData) {
        showProgressDialog();
        tpdGooglePay.getPrime(paymentData, this, this);
    }


    @Override
    public void onSuccess(String prime, TPDCardInfoDto cardInfo, TPDMerchantReferenceInfoDto merchantReferenceInfo) {
        hideProgressDialog();
        String resultStr = "prime is " + prime + "\n" +
                "cardInfo is " + cardInfo + "\n" +
                "merchantReferenceInfo is " + merchantReferenceInfo + "\n\n" +
                "Use below cURL to proceed the payment : \n"
                + ApiUtil.generatePayByPrimeCURLForSandBox(prime, Constants.PARTNER_KEY,
                Constants.MERCHANT_ID);

        showMessage(resultStr);
        Log.d(TAG, "prime = " + prime);
        Log.d(TAG, "cardInfo = " + cardInfo);
        Log.d(TAG, "merchantReferenceInfo = " + merchantReferenceInfo);
        Log.d(TAG, resultStr);
    }

    @Override
    public void onFailure(int status, String msg) {
        hideProgressDialog();
        showMessage("TapPay getPrime failed , status = " + status + ", msg : " + msg);
        Log.d(TAG, "TapPay getPrime failed : " + status + ", msg : " + msg);
    }

    private void showMessage(String s) {
        googlePaymentResultStateTV.setText(s);
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
