package tech.cherri.pxpayplusexample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.api.pxpayplus.TPDPXPayPlus;
import tech.cherri.tpdirect.api.pxpayplus.TPDPXPayPlusResult;
import tech.cherri.tpdirect.callback.TPDPXPayPlusResultListener;
import tech.cherri.tpdirect.exception.TPDCustomException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView tvGetPrimeResultState, tvPayByPrimeResultState, tvPXPayPlusResult;
    private EditText etMerchantId, inputUrl;
    private Button btnGetPrime, btnPayByPrime, btnRefresh, btnRedirectUrl;
    private CheckBox cbRemember;

    private AlertDialog loadingDialog;
    private TPDPXPayPlus tpdPXPayPlus;

    private String txPrime, paymentUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_px_pay_plus);
        setupView();

        Toast.makeText(this, "SDK version is " + TPDSetup.getVersion(), Toast.LENGTH_SHORT).show();

        // Setup environment.
        TPDSetup.initInstance(getApplicationContext(), Constants.APP_ID, Constants.APP_KEY, Constants.SERVER_TYPE);

        try {
            tpdPXPayPlus = new TPDPXPayPlus(getApplicationContext(), inputUrl.getText().toString());
        } catch (TPDCustomException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        handleIncomingIntent(getIntent());
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
        if (intent.getData().toString().startsWith("http")) {
            androidAppLink(intent.getData());
        } else {
            androidDeepLink(intent.getData());
        }
    }

    private void androidAppLink(Uri data) {
        if (tpdPXPayPlus == null) {
            this.preparePXPayPlus();
        }

        this.showLoadingDialog(this);

        tpdPXPayPlus.parseToPXPayPlusResult(getApplicationContext(), data, new TPDPXPayPlusResultListener() {
            @Override
            public void onParseSuccess(TPDPXPayPlusResult result) {
                dismissLoadingDialog();

                Log.d(TAG, "onParseSuccess, result " + result.toString());
                String text = "status:" + result.getStatus()
                        + "\nrec_trade_id:" + result.getRecTradeId()
                        + "\nbank_transaction_id:" + result.getBankTransactionId()
                        + "\norder_number:" + result.getOrderNumber();
                tvPXPayPlusResult.setText(text);
            }

            @Override
            public void onParseFail(int status, String msg) {
                dismissLoadingDialog();

                Log.d(TAG, "onParseFail, status : " + status + " , msg : " + msg);
                String text = "status : " + status + " , msg : " + msg;
                tvPXPayPlusResult.setText(text);
            }
        });
    }

    private void androidDeepLink(Uri data) {
        String result = "Result:" +
                "\nstatus: " + data.getQueryParameter("status") +
                "\nrec_trade_id:" + data.getQueryParameter("rec_trade_id") +
                "\nbank_transaction_id:" + data.getQueryParameter("bank_transaction_id") +
                "\norder_number:" + data.getQueryParameter("order_number");
        tvPXPayPlusResult.setText(result);
    }

    private void preparePXPayPlus() {
        String payName = "PXPayPlus";
        try {
            Toast.makeText(this, "preparePXPayPlus > " + inputUrl.getText().toString(), Toast.LENGTH_SHORT).show();
            tpdPXPayPlus = new TPDPXPayPlus(getApplicationContext(), inputUrl.getText().toString());
            tvPXPayPlusResult.setText(payName + " is Available.");
        } catch (TPDCustomException e) {
            Log.e(TAG, "preparePXPayPlus exception: " + Log.getStackTraceString(e));
            tvPXPayPlusResult.setText(payName + "is not available");
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
                    preparePXPayPlus();
                }
            }
        };

        tvGetPrimeResultState = findViewById(R.id.tvGetPrimeResultState);
        tvPayByPrimeResultState = findViewById(R.id.tvPayByPrimeResultState);
        tvPXPayPlusResult = findViewById(R.id.tvPXPayPlusResult);

        btnGetPrime = findViewById(R.id.btnGetPrime);
        btnPayByPrime = findViewById(R.id.btnPayByPrime);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnRedirectUrl = findViewById(R.id.btnRedirectUrl);
        btnGetPrime.setOnClickListener(this);
        btnPayByPrime.setOnClickListener(this);
        btnRefresh.setOnClickListener(this);
        btnRedirectUrl.setOnClickListener(this);

        etMerchantId = findViewById(R.id.etMerchantId);
        inputUrl = findViewById(R.id.inputUrl);
        inputUrl.addTextChangedListener(textWatcher);

        cbRemember = findViewById(R.id.cbRemember);
        cbRemember.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbRemember.setText("remember = true");
                etMerchantId.setText("px.pay.plus.test.bind");
                Toast.makeText(MainActivity.this, "DEBIT交易", Toast.LENGTH_SHORT).show();
            } else {
                cbRemember.setText("remember = false");
                etMerchantId.setText("px.pay.plus.test.ec");
                Toast.makeText(MainActivity.this, "EC交易", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {

        // get prime
        if (v.getId() == R.id.btnGetPrime) {
            this.showLoadingDialog(this);

            tpdPXPayPlus.getPrime(prime -> {
                String text = "your prime is = " + prime;
                tvGetPrimeResultState.setText(text);
                dismissLoadingDialog();

                txPrime = prime;
            }, (status, msg) -> {
                dismissLoadingDialog();
                tvGetPrimeResultState.setText(msg);
            });
        }

        // pay by prime
        if (v.getId() == R.id.btnPayByPrime) {

            String itemId = "AndroidItemId";
            String itemName = "AndroidPhone";
            int itemQuantity = 1;
            int itemPrice = 8;
            String details = "[{\"item_id\": \"" + itemId
                    + "\",\"item_name\": \"" + itemName
                    + " \",\"item_quantity\":" + itemQuantity
                    + ",\"item_price\":" + itemPrice + "}]";

            Constants.MERCHANT_ID = etMerchantId.getText().toString();

            PayByPrimeTask pxPayPlusPayByPrimeTask = new PayByPrimeTask(this, this.txPrime, details, this);
            pxPayPlusPayByPrimeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        // clear result textview
        if (v.getId() == R.id.btnRefresh) {
            tvGetPrimeResultState.setText("");
            tvPayByPrimeResultState.setText("");
            tvPXPayPlusResult.setText("");
        }

        if (v.getId() == R.id.btnRedirectUrl) {
            if (null != paymentUrl) {
                tpdPXPayPlus.redirectWithUrl(paymentUrl);
            }
        }
    }

    public void showMessage(String result, boolean isSuccess, String paymentUrl) {
        Log.d(TAG, result);

        dismissLoadingDialog();

        if (isSuccess) {
            this.paymentUrl = paymentUrl;
        }

        tvPayByPrimeResultState.setText(result);
    }

    public void showLoadingDialog(Context context) {
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