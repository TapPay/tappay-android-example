package tech.cherri.paywithgoogleexample;

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

import tech.cherri.tpdirect.api.TPDCardInfo;
import tech.cherri.tpdirect.api.TPDConsumer;
import tech.cherri.tpdirect.api.TPDMerchant;
import tech.cherri.tpdirect.api.TPDPayWithGoogle;
import tech.cherri.tpdirect.api.TPDServerType;
import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.callback.TPDPayWithGoogleListener;
import tech.cherri.tpdirect.callback.TPDTokenFailureCallback;
import tech.cherri.tpdirect.callback.TPDTokenSuccessCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TPDPayWithGoogleListener, TPDTokenFailureCallback, TPDTokenSuccessCallback {
    private static final String TAG = "MainActivity";
    private int[] allowedNetworks = new int[]{
            WalletConstants.CARD_NETWORK_VISA
            , WalletConstants.CARD_NETWORK_MASTERCARD
            , WalletConstants.CARD_NETWORK_JCB
            , WalletConstants.CARD_NETWORK_AMEX};
    private static final int REQUEST_READ_PHONE_STATE = 101;
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 102;
    private TPDPayWithGoogle tpdPayWithGoogle;
    private RelativeLayout googlePaymentBuyBTN;
    private TextView totalAmountTV, googlePaymentResultStateTV;
    private TextView buyerInformationTV;
    private TextView shippingInformationTV;
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
                Integer.parseInt(getString(R.string.global_test_app_id)), getString(R.string.global_test_app_key), TPDServerType.Sandbox);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions();
        } else {
            preparePayWithGoogle();
        }

    }

    private void setupViews() {
        totalAmountTV = (TextView) findViewById(R.id.totalAmountTV);
        totalAmountTV.setText("Total amount : 1.00 元");

        googlePaymentBuyBTN = (RelativeLayout) findViewById(R.id.googlePaymentBuyBTN);
        googlePaymentBuyBTN.setOnClickListener(this);
        googlePaymentBuyBTN.setEnabled(false);

        buyerInformationTV = (TextView) findViewById(R.id.buyerInformationTV);
        shippingInformationTV = (TextView) findViewById(R.id.shippingInformationTV);

        confirmBTN = (Button) findViewById(R.id.confirmBTN);
        confirmBTN.setOnClickListener(this);
        confirmBTN.setEnabled(false);

        googlePaymentResultStateTV = (TextView) findViewById(R.id.googlePaymentResultStateTV);
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "PERMISSION IS ALREADY GRANTED");
            preparePayWithGoogle();
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
                preparePayWithGoogle();
                break;
            default:
                break;
        }
    }

    public void preparePayWithGoogle() {
        TPDMerchant tpdMerchant = new TPDMerchant();
        tpdMerchant.setSupportedNetworks(allowedNetworks);

        TPDConsumer tpdConsumer = new TPDConsumer();
        tpdConsumer.setPhoneNumberRequired(false);
        tpdConsumer.setShippingAddressRequired(false);
        tpdConsumer.setEmailRequired(true);

        tpdPayWithGoogle = new TPDPayWithGoogle(this, tpdMerchant, tpdConsumer);
        tpdPayWithGoogle.canUserPayWithGoogle(this);
    }


    @Override
    public void onReadyToPayChecked(boolean isReadyToPay, String msg) {
        Log.d(TAG, "Pay with Google availability : " + isReadyToPay);
        if (isReadyToPay) {
            googlePaymentBuyBTN.setEnabled(true);
        } else {
            showMessage("Cannot use Pay with Google.");
        }
    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.googlePaymentBuyBTN){
            tpdPayWithGoogle.requestPayment(TransactionInfo.newBuilder()
                    .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                    .setTotalPrice("1")
                    .setCurrencyCode("TWD")
                    .build(), LOAD_PAYMENT_DATA_REQUEST_CODE);
        }else if(view.getId() == R.id.confirmBTN){
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
        String email = paymentData.getEmail();

//        UserAddress userAddress = paymentData.getShippingAddress();
//
//        String shippingAddr = userAddress.getAdministrativeArea()
//                + userAddress.getAddress5()
//                + userAddress.getAddress4()
//                + userAddress.getAddress3()
//                + userAddress.getAddress2()
//                + userAddress.getAddress1();

//        String phoneNumber = userAddress.getPhoneNumber();
        String cardNetwork = paymentData.getCardInfo().getCardNetwork();
        String cardDetails = paymentData.getCardInfo().getCardDetails();
        String cardDescription = paymentData.getCardInfo().getCardDescription();


        buyerInformationTV.setText("Email:" + email + "\n"
                + "Card Network : " + cardNetwork + "\n"
                + "Card Details : " + cardDetails + "\n"
                + "Card Description : " + cardDescription + "\n"

        );
//        shippingInformationTV.setText("Shipping Address:" + shippingAddr + "\n");
    }

    private void getPrimeFromTapPay(PaymentData paymentData) {
        showProgressDialog();
        tpdPayWithGoogle.getPrime(paymentData, this, this);
    }


    @Override
    public void onSuccess(String prime, TPDCardInfo cardInfo) {
        hideProgressDialog();
        String resultStr = "Your prime is " + prime
                + "\n\nUse below cURL to proceed the payment : \n"
                + ApiUtil.generatePayByPrimeCURLForSandBox(prime,
                getString(R.string.global_test_partnerKey),
                getString(R.string.global_test_merchant_id));

        showMessage(resultStr);
        Log.d(TAG, resultStr);
    }

    @Override
    public void onFailure(int status, String reportMsg) {
        hideProgressDialog();
        showMessage("TapPay getPrime failed , status = "+ status + ", msg : " + reportMsg);
        Log.d("TPDirect createToken", "failure : " + status + ", msg : " + reportMsg);
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
