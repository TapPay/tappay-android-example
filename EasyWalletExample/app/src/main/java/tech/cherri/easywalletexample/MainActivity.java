package tech.cherri.easywalletexample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import tech.cherri.tpdirect.api.TPDEasyWallet;
import tech.cherri.tpdirect.api.TPDEasyWalletResult;
import tech.cherri.tpdirect.api.TPDJkoPay;
import tech.cherri.tpdirect.api.TPDServerType;
import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.callback.TPDEasyWalletResultListener;
import tech.cherri.tpdirect.exception.TPDEasyWalletException;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_READ_PHONE_STATE = 101;

    private Button easyWalletGetPrimeBTN;
    private Button easyWalletPayByPrimeBTN;
    private Button easyWalletRedirectBTN;
    private Button refreshBTN;
    private TextView getPrimeResultStateTV;
    private TextView easyWalletResultTV;
    private EditText merchantIdInput;
    private EditText urlInput;
    private EditText paymentUrlInput;
    private Context context;

    private TPDEasyWallet tpdEasyWallet;
    public String prime;
    public String paymentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
        setupInputChangeListner();

        context = this;
        Toast.makeText(this, "SDK version is " + TPDSetup.getVersion(), Toast.LENGTH_SHORT).show();

        //Setup environment.
        TPDSetup.initInstance(getApplicationContext(),
                Constants.APP_ID, Constants.APP_KEY, Constants.SERVER_TYPE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions();
        } else {
            prepareEasyWallet();
        }

        handleIncomingIntent(getIntent());
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "PERMISSION IS ALREADY GRANTED");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        }
        prepareEasyWallet();
    }

    private void prepareEasyWallet() {
        boolean isEasyWalletAvailable = TPDEasyWallet.isAvailable(this.getApplicationContext());
        Toast.makeText(this, "isEasyWalletAvailable : "
                + isEasyWalletAvailable, Toast.LENGTH_SHORT).show();
        if (isEasyWalletAvailable) {
            try {
                tpdEasyWallet = new TPDEasyWallet(getApplicationContext(), urlInput.getText().toString());
                initOnClickListener();
            } catch (TPDEasyWalletException e) {
                showMessage(e.getMessage());
            }
        } else {
            easyWalletGetPrimeBTN.setEnabled(false);
            easyWalletPayByPrimeBTN.setEnabled(false);
            easyWalletRedirectBTN.setEnabled(false);
        }
    }

    private void setupInputChangeListner() {

        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() != 0) {
                    prepareEasyWallet();
                }
            }
        };

        EditText merchantIdInput = (EditText) findViewById(R.id.merchantIdInput);
        merchantIdInput.addTextChangedListener(textWatcher);

        EditText urlInput = (EditText) findViewById(R.id.urlInput);
        urlInput.addTextChangedListener(textWatcher);

        EditText paymentUrlInput = (EditText) findViewById(R.id.paymentUrlInput);
        paymentUrlInput.addTextChangedListener(textWatcher);
    }

    private void initOnClickListener() {

        easyWalletGetPrimeBTN.setOnClickListener(new GetPrime(this, tpdEasyWallet));
        easyWalletPayByPrimeBTN.setOnClickListener(new PayByPrime(this, context, merchantIdInput.getText().toString()));
        easyWalletRedirectBTN.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tpdEasyWallet.redirectWithUrl(paymentUrl == null ? paymentUrlInput.getText().toString() : paymentUrl);
            }
        });

        refreshBTN.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getPrimeResultStateTV.setText("");
                easyWalletResultTV.setText("");
                prime = "";
                paymentUrl = "";

                easyWalletGetPrimeBTN.setEnabled(true);
                easyWalletPayByPrimeBTN.setEnabled(true);
                easyWalletRedirectBTN.setEnabled(true);
            }
        });
    }

    private void setupViews() {

        merchantIdInput = (EditText) findViewById(R.id.merchantIdInput);
        urlInput = (EditText) findViewById(R.id.urlInput);
        paymentUrlInput = (EditText) findViewById(R.id.paymentUrlInput);
        getPrimeResultStateTV = (TextView) findViewById(R.id.getPrimeResultStateTV);
        easyWalletResultTV = (TextView) findViewById(R.id.easyWalletResultTV);
        easyWalletGetPrimeBTN = (Button) findViewById(R.id.easyWalletGetPrimeBTN);
        easyWalletPayByPrimeBTN = (Button) findViewById(R.id.easyWalletPayByPrimeBTN);
        easyWalletRedirectBTN = (Button) findViewById(R.id.easyWalletRedirectBTN);
        refreshBTN = (Button) findViewById(R.id.refreshBTN);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {

        Log.i(TAG, "handleIncomingIntent intent.getDataString()：" + intent.getDataString());

        if (intent.getDataString() != null && intent.getDataString().contains(urlInput.getText().toString())) {
            if (tpdEasyWallet == null) {
                prepareEasyWallet();
            }

            Log.i(TAG, "Correct intent.getDataString()：" + intent.getDataString());

            if (intent.getDataString().startsWith("http")) {
                showProgressDialog();
                tpdEasyWallet.parseToEasyWalletResult(getApplicationContext(), intent.getData(), new TPDEasyWalletResultListener() {

                    @Override
                    public void onParseSuccess(TPDEasyWalletResult tpdEasyWalletResult) {
                        hideProgressDialog();
                        if (tpdEasyWalletResult != null) {
                            easyWalletResultTV.setText("Result from Universal Links" + "\nstatus:" + tpdEasyWalletResult.getStatus()
                                    + "\nrec_trade_id:" + tpdEasyWalletResult.getRecTradeId()
                                    + "\nbank_transaction_id:" + tpdEasyWalletResult.getBankTransactionId()
                                    + "\norder_number:" + tpdEasyWalletResult.getOrderNumber());
                        }
                    }

                    @Override
                    public void onParseFail(int i, String s) {
                        hideProgressDialog();
                        easyWalletResultTV.setText("Parse Easy Wallet result failed  status : 915 , msg : Error");
                    }
                });
            } else {
                Uri uri = intent.getData();
                easyWalletResultTV.setText("Result from URI"
                        + "\nstatus:" + uri.getQueryParameter("status")
                        + "\nrec_trade_id:" + uri.getQueryParameter("rec_trade_id")
                        + "\nbank_transaction_id:" + uri.getQueryParameter("bank_transaction_id")
                        + "\norder_number:" + uri.getQueryParameter("order_number"));
            }

            easyWalletGetPrimeBTN.setEnabled(false);
            easyWalletPayByPrimeBTN.setEnabled(false);
            easyWalletRedirectBTN.setEnabled(false);
        }
    }

    public void showMessage(String s) {
        Log.d(TAG, s);
        if (getPrimeResultStateTV != null) {
            getPrimeResultStateTV.setText(getPrimeResultStateTV.getText() + s + "\n");
        }
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
