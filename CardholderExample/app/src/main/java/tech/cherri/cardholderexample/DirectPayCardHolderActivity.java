package tech.cherri.cardholderexample;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import tech.cherri.tpdirect.api.TPDCardHolder;
import tech.cherri.tpdirect.api.TPDCardholderForm;
import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.callback.TPDGetCardHolderPrimeFailureCallback;
import tech.cherri.tpdirect.callback.TPDGetCardHolerPrimeSuccessCallback;
import tech.cherri.tpdirect.model.TPDStatus;


public class DirectPayCardHolderActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = DirectPayCardHolderActivity.class.getSimpleName();

    private TPDCardHolder tpdCardHolder;
    private TPDCardholderForm tpdCardholderForm;

    private Button btnGetCardHolderPrime;
    private TextView tvGetPrimeResultState;
    private CheckBox cbEmailToggle, cbPhoneNumberToggle;
    private AlertDialog loadingDialog;

    private boolean isTPDCardholderFormReadyToPay = false;
    private String prime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_pay_card_holder);

        // Setup environment
        TPDSetup.initInstance(getApplicationContext(), Constants.APP_ID, Constants.APP_KEY, Constants.SERVER_TYPE);

        this.setupView();

        this.setupListener();
    }

    private void setupView() {
        tpdCardholderForm = findViewById(R.id.tpdCardholderForm);
        btnGetCardHolderPrime = findViewById(R.id.btnGetCardHolderPrime);
        btnGetCardHolderPrime.setOnClickListener(this);
        btnGetCardHolderPrime.setEnabled(false);
        tvGetPrimeResultState = findViewById(R.id.tvGetPrimeResultState);
        cbEmailToggle = findViewById(R.id.cbEmailToggle);
        cbPhoneNumberToggle = findViewById(R.id.cbPhoneNumberToggle);

        cbEmailToggle.setChecked(true);
        cbPhoneNumberToggle.setChecked(true);

        // (Option) Enable email
//        tpdCardholderForm.setEmailColumn(true);
        // (Option) Enable country code and phone number
//        tpdCardholderForm.setPhoneNumberColumn(true);
    }

    private void setupListener() {

        TPDGetCardHolerPrimeSuccessCallback tpdGetCardHolerPrimeSuccessCallback = (prime) -> {
            this.dismissLoadingDialog();
            this.prime = prime;
            Log.d(TAG, "prime:  " + prime);

            String resultStr = "Your prime is " + prime;
            tvGetPrimeResultState.setText(resultStr);
            Log.d(TAG, resultStr);

        };

        TPDGetCardHolderPrimeFailureCallback tpdGetCardHolderPrimeFailureCallback = (status, reportMsg) -> {
            this.dismissLoadingDialog();
            String failText = "failure: " + status + reportMsg;
            Log.d(TAG, failText);
            tvGetPrimeResultState.setText(failText);
        };

        tpdCardholderForm.setTextErrorColor(Color.RED);
        tpdCardholderForm.setOnFormUpdateListener(tpdStatus -> {
            switch (tpdStatus.getNameEnStatus()) {
                case TPDStatus.STATUS_OK:
                    Log.d(TAG, "NameEn Status = OK");
                    break;
                case TPDStatus.STATUS_EMPTY:
                    Log.d(TAG, "NameEn Status = Empty");
                    break;
                case TPDStatus.STATUS_ERROR:
                    Log.d(TAG, "NameEn Status = Error");
                    break;
            }
            switch (tpdStatus.getEmailStatus()) {
                case TPDStatus.STATUS_OK:
                    Log.d(TAG, "Email Status = OK");
                    break;
                case TPDStatus.STATUS_ERROR:
                    Log.d(TAG, "Email Status = Error");
                    break;
            }
            switch (tpdStatus.getCountryCodeStatus()) {
                case TPDStatus.STATUS_OK:
                    Log.d(TAG, "CountryCode Status = OK");
                    break;
                case TPDStatus.STATUS_ERROR:
                    Log.d(TAG, "CountryCode Status = Error");
                    break;
            }
            switch (tpdStatus.getPhoneNumberStatus()) {
                case TPDStatus.STATUS_OK:
                    Log.d(TAG, "PhoneNumber Status = OK");
                    break;
                case TPDStatus.STATUS_ERROR:
                    Log.d(TAG, "PhoneNumber Status = Error");
                    break;
            }

            Log.d(TAG, "status error = " + tpdStatus.isHasAnyError());

            tvGetPrimeResultState.setText("");

            // Cardholder info
            if (tpdStatus.getNameEnStatus() == TPDStatus.STATUS_ERROR) {
                tvGetPrimeResultState.setText("Invalid English Name");
            }
            if (tpdStatus.getEmailStatus() == TPDStatus.STATUS_ERROR) {
                tvGetPrimeResultState.setText("Invalid Email");
            }
            if (tpdStatus.getCountryCodeStatus() == TPDStatus.STATUS_ERROR) {
                tvGetPrimeResultState.setText("Invalid Country Code");
            }
            if (tpdStatus.getPhoneNumberStatus() == TPDStatus.STATUS_ERROR) {
                tvGetPrimeResultState.setText("Invalid Phone Number");
            }

            isTPDCardholderFormReadyToPay = tpdStatus.isCanGetCardHolderPrime();
            Log.d(TAG, "isTPDCardholderFormReadyToPay = " + isTPDCardholderFormReadyToPay);

            btnGetCardHolderPrime.setEnabled(isTPDCardholderFormReadyToPay);
        });

        cbEmailToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tpdCardholderForm.setEmailColumn(isChecked);
        });

        cbPhoneNumberToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tpdCardholderForm.setPhoneNumberColumn(isChecked);
        });

        // Setup TPDCardHolder
        tpdCardHolder = TPDCardHolder.setup(tpdCardholderForm)
                .onSuccessCallback(tpdGetCardHolerPrimeSuccessCallback)
                .onFailureCallback(tpdGetCardHolderPrimeFailureCallback);;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGetCardHolderPrime:
                this.showLoadingDialog(this);
                tpdCardHolder.getPrime();
                break;
        }
    }

    /**
     * Dialog progressbar
     */
    private void showLoadingDialog(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

}