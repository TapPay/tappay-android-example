package tech.cherri.gogopayexample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.api.gogopay.TPDGogoPay;
import tech.cherri.tpdirect.api.gogopay.TPDGogoPayResult;
import tech.cherri.tpdirect.callback.TPDGetPrimeFailureCallback;
import tech.cherri.tpdirect.callback.gogopay.TPDGogoPayGetPrimeSuccessCallback;
import tech.cherri.tpdirect.callback.gogopay.TPDGogoPayResultListener;
import tech.cherri.tpdirect.exception.TPDCustomException;


public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        TPDGetPrimeFailureCallback, TPDGogoPayGetPrimeSuccessCallback,
        TPDGogoPayResultListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView tvMerchantId, tvGetPrimeResultState, tvPayByPrimeResultState, tvGogoPayResult;
    private EditText etMerchantId, etInputUrl;
    private Button btnGetPrime, btnPayByPrime, btnRefresh;

    private SwipeRefreshLayout srlRefresh;

    private AlertDialog loadingDialog;
    private TPDGogoPay tpdGogoPay;

    private String txPrime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gogopay);
        setupView();

        Toast.makeText(this, "SDK version is " + TPDSetup.getVersion(), Toast.LENGTH_SHORT).show();

        // Setup environment.
        TPDSetup.initInstance(getApplicationContext(), Constants.APP_ID, Constants.APP_KEY, Constants.SERVER_TYPE);

        try {
            tpdGogoPay = new TPDGogoPay(getApplicationContext(), etInputUrl.getText().toString());
        } catch (TPDCustomException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        handleIncomingIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        Log.d(TAG, "handleIncomingIntent, intent: " + intent.toString());
        if (intent == null || intent.getData() == null) {
            return;
        }

        androidDeepLink(intent.getData());

    }

    private void androidDeepLink(Uri data) {
        String result = "Result:" +
                "\nstatus: " + data.getQueryParameter("status") +
                "\nrec_trade_id:" + data.getQueryParameter("rec_trade_id") +
                "\nbank_transaction_id:" + data.getQueryParameter("bank_transaction_id") +
                "\norder_number:" + data.getQueryParameter("order_number");
        tvGogoPayResult.setText(result);
    }

    private void prepareGogoPay() {
        String payName = "GogoPay";
        try {
            Toast.makeText(this, "prepareGogoPay > " + etInputUrl.getText().toString(), Toast.LENGTH_SHORT).show();
            tpdGogoPay = new TPDGogoPay(getApplicationContext(), etInputUrl.getText().toString());
            tvGogoPayResult.setText(payName + " is Available.");
        } catch (TPDCustomException e) {
            Log.e(TAG, "prepareGogoPay exception: " + Log.getStackTraceString(e));
            tvGogoPayResult.setText(payName + "is not available");
        }
    }

    private void setupView() {

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    prepareGogoPay();
                }
            }
        };

        srlRefresh = findViewById(R.id.srlRefresh);

        srlRefresh.setOnRefreshListener(() -> {
            reloadData();

            srlRefresh.setRefreshing(false);
        });

        tvMerchantId = findViewById(R.id.tvMerchantId);
        tvGetPrimeResultState = findViewById(R.id.tvGetPrimeResultState);
        tvPayByPrimeResultState = findViewById(R.id.tvPayByPrimeResultState);
        tvGogoPayResult = findViewById(R.id.tvGogoPayResult);

        btnGetPrime = findViewById(R.id.btnGetPrime);
        btnPayByPrime = findViewById(R.id.btnPayByPrime);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnGetPrime.setOnClickListener(this);
        btnPayByPrime.setOnClickListener(this);
        btnRefresh.setOnClickListener(this);

        etMerchantId = findViewById(R.id.etMerchantId);
        etInputUrl = findViewById(R.id.etInputUrl);
        etInputUrl.addTextChangedListener(textWatcher);
    }

    @Override
    public void onClick(View v) {

        // get prime
        if (v.getId() == R.id.btnGetPrime) {
            this.showLoadingDialog(this);

            tpdGogoPay.getPrime(this, this);
        }

        // pay by prime
        if (v.getId() == R.id.btnPayByPrime) {

            String details = "[{ \"item_name\": \"Create Merchant Test\",\"item_price\": 99,\"item_quantity\": 1 }]";

            Constants.MERCHANT_ID = etMerchantId.getText().toString();

            GogoPayPayByPrimeTask gogoPayPayByPrimeTask = new GogoPayPayByPrimeTask(this, this.txPrime, details);
            gogoPayPayByPrimeTask.start();
        }

        // clear result textview
        if (v.getId() == R.id.btnRefresh) {
            tvGetPrimeResultState.setText("");
            tvPayByPrimeResultState.setText("");
            tvGogoPayResult.setText("");
        }
    }

    @Override
    public void onFailure(int status, String msg) {
        this.dismissLoadingDialog();
        tvGetPrimeResultState.setText(msg);
    }

    @Override
    public void onSuccess(String prime) {
        String text = "your prime is = " + prime;
        tvGetPrimeResultState.setText(text);
        this.dismissLoadingDialog();

        this.txPrime = prime;
    }

    @Override
    public void onParseSuccess(TPDGogoPayResult result) {
        this.dismissLoadingDialog();

        Log.d(TAG, "onParseSuccess, result " + result.toString());
        String text = "status:" + result.getStatus()
                + "\nrec_trade_id:" + result.getRecTradeId()
                + "\nbank_transaction_id:" + result.getBankTransactionId()
                + "\norder_number:" + result.getOrderNumber();
        tvGogoPayResult.setText(text);
    }

    @Override
    public void onParseFail(int status, String msg) {
        this.dismissLoadingDialog();

        Log.d(TAG, "onParseFail, status : " + status + " , msg : " + msg);
        String text = "status : " + status + " , msg : " + msg;
        tvGogoPayResult.setText(text);
    }

    public void showMessage(String result, boolean isSuccess, String paymentUrl) {
        Log.d(TAG, result);

        dismissLoadingDialog();

        if (isSuccess) {
            tpdGogoPay.redirectWithUrl(paymentUrl);
        }

        tvPayByPrimeResultState.setText(result);
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

    private void reloadData() {
        etMerchantId.setText("GOGOPAY.TEST");
        Constants.MERCHANT_ID = etMerchantId.getText().toString();

        etInputUrl.setText("gogopayexample://gogopay.uri:8888/test");

        tvGetPrimeResultState.setText("");
        tvPayByPrimeResultState.setText("");
        tvGogoPayResult.setText("");
    }
}