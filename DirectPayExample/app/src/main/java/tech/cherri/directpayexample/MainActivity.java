package tech.cherri.directpayexample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import tech.cherri.tpdirect.api.TPDCard;
import tech.cherri.tpdirect.api.TPDForm;
import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.callback.TPDCardGetPrimeSuccessCallback;
import tech.cherri.tpdirect.callback.TPDFormUpdateListener;
import tech.cherri.tpdirect.callback.TPDGetPrimeFailureCallback;
import tech.cherri.tpdirect.callback.dto.TPDCardInfoDto;
import tech.cherri.tpdirect.callback.dto.TPDMerchantReferenceInfoDto;
import tech.cherri.tpdirect.constant.TPDConstants;
import tech.cherri.tpdirect.model.TPDStatus;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_READ_PHONE_STATE = 101;

    private TPDForm tpdForm;
    private TextView tipsTV;
    private Button payBTN;
    private TPDCard tpdCard;
    private TextView statusTV;
    private Button getDeviceIdBTN;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();

        Log.d(TAG, "SDK version is " + TPDSetup.getVersion());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions();
        } else {
            startTapPaySetting();
        }
    }

    private void setupViews() {
        statusTV = (TextView) findViewById(R.id.statusTV);
        tipsTV = (TextView) findViewById(R.id.tipsTV);
        payBTN = (Button) findViewById(R.id.payBTN);
        payBTN.setOnClickListener(this);
        payBTN.setEnabled(false);
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "PERMISSION IS ALREADY GRANTED");
            startTapPaySetting();
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
                startTapPaySetting();
                break;
            default:
                break;
        }
    }


    private void startTapPaySetting() {
        Log.d(TAG, "startTapPaySetting");
        //1.Setup environment.
//        TPDSetup.initInstance(getApplicationContext(),
//                Constants.APP_ID, Constants.APP_KEY, Constants.SERVER_TYPE);

        TPDSetup.initInstanceWithRba(getApplicationContext(),
                Constants.APP_ID, Constants.APP_KEY, Constants.RBA_APP_ID, Constants.RBA_APP_KEY, Constants.SERVER_TYPE);

        //2.Setup input form
        tpdForm = (TPDForm) findViewById(R.id.tpdCardInputForm);
        tpdForm.setTextErrorColor(Color.RED);
        tpdForm.setOnFormUpdateListener(new TPDFormUpdateListener() {
            @Override
            public void onFormUpdated(TPDStatus tpdStatus) {
                tipsTV.setText("");
                if (tpdStatus.getCardNumberStatus() == TPDStatus.STATUS_ERROR) {
                    tipsTV.setText("Invalid Card Number");
                } else if (tpdStatus.getExpirationDateStatus() == TPDStatus.STATUS_ERROR) {
                    tipsTV.setText("Invalid Expiration Date");
                } else if (tpdStatus.getCcvStatus() == TPDStatus.STATUS_ERROR) {
                    tipsTV.setText("Invalid CCV");
                }
                payBTN.setEnabled(tpdStatus.isCanGetPrime());
            }
        });


        //3.Setup TPDCard with form and callbacks.
        TPDCardGetPrimeSuccessCallback tpdCardGetPrimeSuccessCallback = new TPDCardGetPrimeSuccessCallback() {
            @Override
            public void onSuccess(String prime, TPDCardInfoDto cardInfo, String cardIdentifier, TPDMerchantReferenceInfoDto merchantReferenceInfo) {

                Log.d("TPDirect getPrime", "prime:  " + prime);
                Log.d("TPDirect getPrime", "cardInfo:  " + cardInfo);
                Log.d("TPDirect getPrime", "cardIdentifier:  " + cardIdentifier);
                Log.d("TPDirect getPrime", "merchantReferenceInfo:  " + merchantReferenceInfo);

                Toast.makeText(MainActivity.this,
                        "Get Prime Success",
                        Toast.LENGTH_SHORT).show();

                String resultStr = "prime is " + prime + "\n\n" +
                        "cardInfo is " + cardInfo + "\n\n" +
                        "cardIdentifier is " + cardIdentifier + "\n\n" +
                        "merchantReferenceInfo is " + merchantReferenceInfo + "\n\n" +
                        "Use below cURL to proceed the payment : \n"
                        + ApiUtil.generatePayByPrimeCURLForSandBox(prime, Constants.PARTNER_KEY,
                        Constants.MERCHANT_ID);

                statusTV.setText(resultStr);
                Log.d(TAG, resultStr);

            }
        };
        TPDGetPrimeFailureCallback tpdGetPrimeFailureCallback = new TPDGetPrimeFailureCallback() {
            @Override
            public void onFailure(int status, String msg) {
                Log.d("TPDirect createToken", "failure: " + status + ": " + msg);
                Toast.makeText(MainActivity.this,
                        "Create Token Failed\n" + status + ": " + msg,
                        Toast.LENGTH_SHORT).show();
            }
        };

        tpdCard = TPDCard.setup(tpdForm).onSuccessCallback(tpdCardGetPrimeSuccessCallback)
                .onFailureCallback(tpdGetPrimeFailureCallback);

        //For getFraudId
        getDeviceIdBTN = (Button) findViewById(R.id.getDeviceIdBTN);
        getDeviceIdBTN.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.getDeviceIdBTN:
                //GetFraudId for PayByToken
                String deviceId = TPDSetup.getInstance(this).getRbaDeviceId();
                Toast.makeText(this, "DeviceId is:" + deviceId, Toast.LENGTH_SHORT).show();
                break;
            case R.id.payBTN:
                //4. Calling API for obtaining prime.
                if (tpdCard != null) {
                    tpdCard.getPrime();
                }
                break;
        }

    }


}
