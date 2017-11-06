package tech.cherri.androidpayexample;

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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.identity.intents.model.UserAddress;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.WalletConstants;

import tech.cherri.tpdirect.api.TPDAndroidPay;
import tech.cherri.tpdirect.api.TPDCardInfo;
import tech.cherri.tpdirect.api.TPDCart;
import tech.cherri.tpdirect.api.TPDConsumer;
import tech.cherri.tpdirect.api.TPDMerchant;
import tech.cherri.tpdirect.api.TPDPaymentItem;
import tech.cherri.tpdirect.api.TPDServerType;
import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.callback.TPDAndroidPayListener;
import tech.cherri.tpdirect.callback.TPDTokenFailureCallback;
import tech.cherri.tpdirect.callback.TPDTokenSuccessCallback;
import tech.cherri.tpdirect.exception.TPDAndroidPayException;

public class MainActivity extends FragmentActivity implements View.OnClickListener, TPDAndroidPayListener, TPDTokenFailureCallback, TPDTokenSuccessCallback {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_READ_PHONE_STATE = 101;
    private static final int REQUEST_CODE_MASKED_WALLET = 102;
    private static final int REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET = 103;
    private FrameLayout frameLayout;
    private TPDAndroidPay tpdAndroidPay;
    private int[] allowedNetworks = new int[]{WalletConstants.CardNetwork.VISA, WalletConstants.CardNetwork.MASTERCARD};

    private TPDCart tpdCart;
    private Button confirmBTN;

    private MaskedWallet maskedWallet;
    private TextView resultStateTV;
    private TextView buyerInformationTV;
    private TextView shippingInformationTV;
    private TextView totalAmountTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();

        Log.d(TAG, "SDK version is " + TPDSetup.getVersion());

        //Setup environment.
        TPDSetup.initInstance(getApplicationContext(),
                Integer.parseInt(getString(R.string.app_id)), getString(R.string.app_key), TPDServerType.Sandbox);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions();
        } else {
            prepareAndroidPay();
        }

    }

    private void setupViews() {
        totalAmountTV = (TextView) findViewById(R.id.totalAmountTV);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

        buyerInformationTV = (TextView) findViewById(R.id.buyerInformationTV);
        shippingInformationTV = (TextView) findViewById(R.id.shippingInformationTV);

        confirmBTN = (Button) findViewById(R.id.androidPayConfirmBTN);
        confirmBTN.setOnClickListener(this);
        confirmBTN.setEnabled(false);

        resultStateTV = (TextView) findViewById(R.id.resultStateTV);
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "PERMISSION IS ALREADY GRANTED");
            prepareAndroidPay();
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
                prepareAndroidPay();
                break;
            default:
                break;
        }
    }

    private void prepareAndroidPay() {
        TPDMerchant tpdMerchant = new TPDMerchant();
        tpdMerchant.setMerchantName(getString(R.string.merchant_name));
        tpdMerchant.setAndroidMerchantId(getString(R.string.android_merchant_id));
        tpdMerchant.setCountryCode(getString(R.string.country_code));
        tpdMerchant.setCurrencyCode(getString(R.string.currency_code));
        tpdMerchant.setPublicKey(getString(R.string.public_key));
        tpdMerchant.setSupportedNetworks(allowedNetworks);

        TPDConsumer tpdConsumer = new TPDConsumer();
        tpdConsumer.setPhoneNumberRequired(true);
        tpdConsumer.setShippingAddressRequired(true);

        //Add the goods from your shopping page.
        tpdCart = new TPDCart();
        TPDPaymentItem paymentItemBook = new TPDPaymentItem("book", "12.00", LineItem.Role.REGULAR);
        TPDPaymentItem paymentItemDiscount = new TPDPaymentItem("discount", "-2.00", LineItem.Role.REGULAR);
        TPDPaymentItem paymentItemShipping = new TPDPaymentItem("shipping", "3.00", LineItem.Role.SHIPPING);
        tpdCart.addPaymentItem(paymentItemBook);
        tpdCart.addPaymentItem(paymentItemDiscount);
        tpdCart.addPaymentItem(paymentItemShipping);
        totalAmountTV.setText("總金額 : " + tpdCart.calculateCartTotal() + " 元");

        tpdAndroidPay = new TPDAndroidPay(this, tpdMerchant, tpdConsumer);
        tpdAndroidPay.canUserPayWithNetworks(this, allowedNetworks);
    }


    @Override
    public void onReadyToPayChecked(boolean isReadyToPay, String msg) {
        Log.d(TAG, "Android Pay availability is " + isReadyToPay);
        if (isReadyToPay) {
            frameLayout.setVisibility(View.VISIBLE);
            try {
                tpdAndroidPay.generateBuyButton(frameLayout, REQUEST_CODE_MASKED_WALLET, tpdCart, null);
            } catch (TPDAndroidPayException e) {
                showMessage(e.getMessage());
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int errorCode = -1;
        if (data != null) {
            errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
        }

        switch (requestCode) {
            case REQUEST_CODE_MASKED_WALLET:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data != null) {
                            Log.d(TAG, "MaskedWallet obtained");
                            maskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);

                            //取得用戶資訊
                            revealPaymentInfo(maskedWallet);

                            confirmBTN.setEnabled(true);
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d(TAG, "User has canceled Android Pay");
                        showMessage("Canceled");
                        break;
                    default:
                        Log.d(TAG, "Error :" + errorCode);
                        handleError(errorCode);
                        break;
                }
                break;
            case REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data != null && data.hasExtra(WalletConstants.EXTRA_FULL_WALLET)) {
                            Log.d(TAG, "FullWallet obtained");
                            FullWallet fullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
                            getPrimeFromTapPay(fullWallet);
                        } else if (data != null && data.hasExtra(WalletConstants.EXTRA_MASKED_WALLET)) {
                            Log.d(TAG, "MaskWallet obtained within FullWallet Request code");
                            showMessage("MaskWallet obtained within FullWallet Request code");
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        showMessage("Canceled");
                        break;
                    default:
                        handleError(errorCode);
                        break;
                }
                break;
            case WalletConstants.RESULT_ERROR:
                handleError(errorCode);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void revealPaymentInfo(MaskedWallet maskedWallet) {
        String email = maskedWallet.getEmail();

        UserAddress userAddress = maskedWallet.getBuyerShippingAddress();

        String shippingAddr = userAddress.getAdministrativeArea()
                + userAddress.getAddress5()
                + userAddress.getAddress4()
                + userAddress.getAddress3()
                + userAddress.getAddress2()
                + userAddress.getAddress1();

        String phoneNumber = userAddress.getPhoneNumber();
        String cardType = maskedWallet.getInstrumentInfos()[0].getInstrumentType();
        String cardNum = maskedWallet.getInstrumentInfos()[0].getInstrumentDetails();
        String cardholderName = maskedWallet.getBuyerBillingAddress().getName();


        buyerInformationTV.setText("Email:" + email + "\n"
                + "Card Type : " + cardType + "\n"
                + "Card Info : " + cardNum + "\n"
                + "Cardholder Name : " + cardholderName + "\n"
                + "Phone Number : " + phoneNumber + "\n"
        );
        shippingInformationTV.setText("Shipping Address:" + shippingAddr + "\n");
    }


    private void getPrimeFromTapPay(FullWallet fullWallet) {
        showProgressDialog();
        tpdAndroidPay.getPrime(fullWallet, this, this);
    }


    @Override
    public void onSuccess(String prime, TPDCardInfo cardInfo) {
        hideProgressDialog();
        Log.d(TAG, "prime =" + prime);
        resultStateTV.setText("Your prime is " + prime);
    }

    @Override
    public void onFailure(int status, String reportMsg) {
        hideProgressDialog();
        Log.d(TAG, "TPDirect createToken failure : " + status + ", msg : " + reportMsg);
        resultStateTV.setText("TPDirect createToken failure : " + status + ", msg : " + reportMsg);
    }

    @Override
    public void onClick(View view) {
        try {
            tpdAndroidPay.confirmWallet(maskedWallet, tpdCart, REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET);
        } catch (TPDAndroidPayException e) {
            showMessage(e.getMessage());
        }
    }


    protected void handleError(int errorCode) {
        switch (errorCode) {
            case WalletConstants.ERROR_CODE_SPENDING_LIMIT_EXCEEDED:
                showMessage("ERROR_CODE_SPENDING_LIMIT_EXCEEDED");
                break;
            case WalletConstants.ERROR_CODE_INVALID_PARAMETERS:
                showMessage("ERROR_CODE_INVALID_PARAMETERS");
                break;
            case WalletConstants.ERROR_CODE_AUTHENTICATION_FAILURE:
                showMessage("ERROR_CODE_AUTHENTICATION_FAILURE");
                break;
            case WalletConstants.ERROR_CODE_BUYER_ACCOUNT_ERROR:
                showMessage("ERROR_CODE_BUYER_ACCOUNT_ERROR");
                break;
            case WalletConstants.ERROR_CODE_MERCHANT_ACCOUNT_ERROR:
                showMessage("ERROR_CODE_MERCHANT_ACCOUNT_ERROR");
                break;
            case WalletConstants.ERROR_CODE_SERVICE_UNAVAILABLE:
                showMessage("ERROR_CODE_SERVICE_UNAVAILABLE");
                break;
            case WalletConstants.ERROR_CODE_UNSUPPORTED_API_VERSION:
                showMessage("ERROR_CODE_UNSUPPORTED_API_VERSION");
                break;
            case WalletConstants.ERROR_CODE_UNKNOWN:
                showMessage("ERROR_CODE_UNKNOWN");
                break;
            default:
                showMessage("Error_code:" + errorCode);
                break;
        }
    }

    private void showMessage(String s) {
        resultStateTV.setText(s);
    }


    public ProgressDialog mProgressDialog;

    protected void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);

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
