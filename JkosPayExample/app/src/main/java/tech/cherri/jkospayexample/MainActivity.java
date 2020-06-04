package tech.cherri.jkospayexample;

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

import tech.cherri.tpdirect.api.TPDJkoPay;
import tech.cherri.tpdirect.api.TPDJkoPayResult;
import tech.cherri.tpdirect.api.TPDServerType;
import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.callback.TPDJkoPayResultListener;
import tech.cherri.tpdirect.exception.TPDJkoPayException;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_READ_PHONE_STATE = 101;

    private Button jkoPayGetPrimeBTN;
    private Button jkoPayPayByPrimeBTN;
    private Button jkoPayRedirectBTN;
    private Button refreshBTN;
    private TextView getPrimeResultStateTV;
    private TextView jkoPayResultTV;
    private EditText merchantIdInput;
    private EditText urlInput;
    private Context context;

    private TPDJkoPay tpdJkoPay;
    public String prime;
    public String paymentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(tech.cherri.jkospayexample.R.layout.activity_main);
        setupViews();
        setupInputChangeListner();

        context = this;
        Toast.makeText(this, "SDK version is " + TPDSetup.getVersion(), Toast.LENGTH_SHORT).show();

        //Setup environment.
        TPDSetup.initInstance(getApplicationContext(),
                Constants.APP_ID, Constants.APP_KEY, TPDServerType.Sandbox);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions();
        } else {
            prepareJkoPay();
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
        prepareJkoPay();
    }

    private void prepareJkoPay() {
        boolean isJkoPayAvailable = TPDJkoPay.isJkoPayAvailable(this.getApplicationContext());
        Toast.makeText(this, "isJkoPayAvailable : "
                + isJkoPayAvailable, Toast.LENGTH_SHORT).show();
        if (isJkoPayAvailable) {
            try {
                tpdJkoPay = new TPDJkoPay(getApplicationContext(), urlInput.getText().toString());
                initOnClickListener();
            } catch (TPDJkoPayException e) {
                showMessage(e.getMessage());
            }
        } else {
            jkoPayGetPrimeBTN.setEnabled(false);
            jkoPayPayByPrimeBTN.setEnabled(false);
            jkoPayRedirectBTN.setEnabled(false);
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
                    prepareJkoPay();
                }
            }
        };

        EditText merchantIdInput = (EditText) findViewById(R.id.merchantIdInput);
        merchantIdInput.addTextChangedListener(textWatcher);

        EditText urlInput = (EditText) findViewById(R.id.urlInput);
        urlInput.addTextChangedListener(textWatcher);
    }

    private void initOnClickListener() {

        jkoPayGetPrimeBTN.setOnClickListener(new GetPrime(this, tpdJkoPay));
        jkoPayPayByPrimeBTN.setOnClickListener(new PayByPrime(this, context, merchantIdInput.getText().toString()));
        jkoPayRedirectBTN.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tpdJkoPay.redirectWithUrl(paymentUrl);
            }
        });

        refreshBTN.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getPrimeResultStateTV.setText("");
                jkoPayResultTV.setText("");
                prime = "";
                paymentUrl = "";

                jkoPayGetPrimeBTN.setEnabled(true);
                jkoPayPayByPrimeBTN.setEnabled(true);
                jkoPayRedirectBTN.setEnabled(true);
            }
        });
    }

    private void setupViews() {

        merchantIdInput = (EditText) findViewById(R.id.merchantIdInput);
        urlInput = (EditText) findViewById(R.id.urlInput);
        getPrimeResultStateTV = (TextView) findViewById(R.id.getPrimeResultStateTV);
        jkoPayResultTV = (TextView) findViewById(R.id.jkosPayResultTV);
        jkoPayGetPrimeBTN = (Button) findViewById(R.id.jkosPayGetPrimeBTN);
        jkoPayPayByPrimeBTN = (Button) findViewById(R.id.jkosPayPayByPrimeBTN);
        jkoPayRedirectBTN = (Button) findViewById(R.id.jkosPayRedirectBTN);
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
            if (tpdJkoPay == null) {
                prepareJkoPay();
            }

            Log.i(TAG, "Correct intent.getDataString()：" + intent.getDataString());

            if (intent.getDataString().startsWith("http")) {
                showProgressDialog();
                tpdJkoPay.parseToJkoPayResult(getApplicationContext(), intent.getData(), new TPDJkoPayResultListener() {

                    @Override
                    public void onParseSuccess(TPDJkoPayResult tpdJkoPayResult) {
                        hideProgressDialog();
                        if (tpdJkoPayResult != null) {
                            jkoPayResultTV.setText("Result from Universal Links" + "\nstatus:" + tpdJkoPayResult.getStatus()
                                    + "\nrec_trade_id:" + tpdJkoPayResult.getRecTradeId()
                                    + "\nbank_transaction_id:" + tpdJkoPayResult.getBankTransactionId()
                                    + "\norder_number:" + tpdJkoPayResult.getOrderNumber());
                        }
                    }

                    @Override
                    public void onParseFail(int i, String s) {
                        hideProgressDialog();
                        jkoPayResultTV.setText("Parse JKO Pay result failed  status : 915 , msg : Error");
                    }
                });
            } else {
                Uri uri = intent.getData();
                jkoPayResultTV.setText("Result from URI"
                        + "\nstatus:" + uri.getQueryParameter("status")
                        + "\nrec_trade_id:" + uri.getQueryParameter("rec_trade_id")
                        + "\nbank_transaction_id:" + uri.getQueryParameter("bank_transaction_id")
                        + "\norder_number:" + uri.getQueryParameter("order_number"));
            }

            jkoPayGetPrimeBTN.setEnabled(false);
            jkoPayPayByPrimeBTN.setEnabled(false);
            jkoPayRedirectBTN.setEnabled(false);
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
