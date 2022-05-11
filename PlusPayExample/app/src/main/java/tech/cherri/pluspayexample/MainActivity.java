package tech.cherri.pluspayexample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;


import tech.cherri.tpdirect.api.TPDServerType;
import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.api.pluspay.TPDPlusPay;
import tech.cherri.tpdirect.api.pluspay.TPDPlusPayResult;
import tech.cherri.tpdirect.callback.TPDGetPrimeFailureCallback;
import tech.cherri.tpdirect.callback.TPDPlusPayGetPrimeSuccessCallback;
import tech.cherri.tpdirect.callback.TPDPlusPayResultListener;
import tech.cherri.tpdirect.exception.TPDCustomException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        TPDPlusPayGetPrimeSuccessCallback, TPDGetPrimeFailureCallback, ResultListener, TPDPlusPayResultListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_READ_PHONE_STATE = 101;
    private Button plusPayBtn;
    private TPDPlusPay tpdPlusPay;

    private EditText itemIdEditText;
    private EditText itemNameEditText;
    private EditText itemQuantityEditText;
    private EditText itemPriceEdittext;

    private TextView actionResult;
    private ProgressDialog progressDialog;

    private boolean isActivityVisible;
    private String temporaryResultString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "SDK version is " + TPDSetup.getVersion());

        //Setup environment
        TPDSetup.initInstance(getApplicationContext(),
                Constants.APP_ID, Constants.APP_KEY, TPDServerType.Sandbox);

        findViews();
        preparePlusPay();
        handleIncomingIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume, " + temporaryResultString);
        this.isActivityVisible = true;
        hideProgressDialog();
        if (!this.temporaryResultString.isEmpty()) {
            Log.d(TAG, "onResume, load result from temp string, because activity is currently visible.");
            this.actionResult.setText(temporaryResultString);
            this.temporaryResultString = "";
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        this.isActivityVisible = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        Log.d(TAG, "handleIncomingIntent , intent: " + intent.toString());
        if (intent == null || intent.getData() == null) {
            return;
        }
        if (intent.getData().toString().startsWith("https")) {
            androidAppLink(intent.getData());
        } else {
            androidDeepLink(intent.getData());
        }
    }

    private void androidAppLink(Uri data) {
        if (tpdPlusPay == null) {
            preparePlusPay();
        }
        showProgressDialog();
        tpdPlusPay.parseToPlusPayResult(getApplicationContext(), data, this);
    }

    private void androidDeepLink(Uri data) {
        String result = "Result:" +
                "\nstatus: " + data.getQueryParameter("status") +
                "\nrec_trade_id:" + data.getQueryParameter("rec_trade_id") +
                "\nbank_transaction_id:" + data.getQueryParameter("bank_transaction_id") +
                "\norder_number:" + data.getQueryParameter("order_number");
        this.actionResult.setText(result);
    }

    private void findViews() {
        plusPayBtn = findViewById(R.id.PlusPayButton);
        plusPayBtn.setOnClickListener(this);

        itemIdEditText = ((EditText) findViewById(R.id.itemIdEditText));
        itemNameEditText = ((EditText) findViewById(R.id.itemNameEditText));
        itemQuantityEditText = ((EditText) findViewById(R.id.itemQuantityEditText));
        itemPriceEdittext = ((EditText) findViewById(R.id.itemPriceEdittext));

        actionResult = (TextView) findViewById(R.id.actionResult);
    }

    private void preparePlusPay() {
        boolean isPlusPayAvailable = TPDPlusPay.isPlusPayAvailable(this.getApplicationContext());
        String payName = getString(R.string.pay_name);
        if (isPlusPayAvailable) {
            try {
                tpdPlusPay = new TPDPlusPay(getApplicationContext(), Constants.RETURN_URL);
                plusPayBtn.setVisibility(View.VISIBLE);
                plusPayBtn.setText(payName);
                actionResult.setText(payName + " is Available.");
            } catch (TPDCustomException e) {
                e.printStackTrace();
                actionResult.setText(payName + "is not available");
            }
        } else {
            actionResult.setText(payName+ " is not available");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.PlusPayButton) {
            showProgressDialog();
            tpdPlusPay.getPrime(this, this);
        }

    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Loading...");
        }
        try {
            progressDialog.show();
        }
        catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        }

    }

    protected void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onSuccess(String prime) {
        String text = "your prime is = " + prime;
        this.actionResult.setText(text);
        showProgressDialog();

        String details = "[{\"item_id\": \"" + itemIdEditText.getText().toString()
                + "\",\"item_name\": \"" + itemNameEditText.getText().toString()
                + " \",\"item_quantity\":" + itemQuantityEditText.getText().toString()
                + ",\"item_price\":" + itemPriceEdittext.getText().toString() + "}]";

        PlusPayPayByPrimeTask plusPayPayByPrimeTask = new PlusPayPayByPrimeTask(
                this, prime, details, this);

        plusPayPayByPrimeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @Override
    public void onFailure(int status, String msg) {
        hideProgressDialog();
        this.actionResult.setText(msg);
    }

    @Override
    public void onTaskSuccess(JSONObject jsonObject) {
        hideProgressDialog();
        try {
            String paymentUrl = jsonObject.getString("payment_url");
            String text = "Pay-by-prime: payment_url = " + paymentUrl;
            this.actionResult.setText(text);
            tpdPlusPay.redirectWithUrl(paymentUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskFailed(String resultString) {
        hideProgressDialog();
        this.actionResult.setText(resultString);
    }

    @Override
    public void onParseSuccess(TPDPlusPayResult result) {
        hideProgressDialog();
        String text = "status:" + result.getStatus()
                + "\nrec_trade_id:" + result.getRecTradeId()
                + "\nbank_transaction_id:" + result.getBankTransactionId()
                + "\norder_number:" + result.getOrderNumber();
        Log.d("MainActivity", "onParseSuccess" + text);
        if (this.isActivityVisible) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    actionResult.setText(text);
                }
            });
        }
        else {
            Log.d(TAG, "onParseSuccess, save result to temp string, because activity is currently not visible.");
            this.temporaryResultString = text;
        }

    }

    @Override
    public void onParseFail(int status, String msg) {
        Log.d(TAG, "onParseFail, msg"+ msg);
        hideProgressDialog();
        String text = "Parse atome Pay result failed  status : " + status + " , msg : " + msg;
        if (this.isActivityVisible) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    actionResult.setText(text);
                }
            });
        }
        else {
            Log.d(TAG, "onParseFail, save result to temp string, because activity is currently not visible.");
            this.temporaryResultString = text;
        }
    }
}