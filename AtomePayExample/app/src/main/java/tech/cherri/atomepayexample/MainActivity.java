package tech.cherri.atomepayexample;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONObject;

import tech.cherri.tpdirect.api.TPDAtomePay;
import tech.cherri.tpdirect.api.TPDAtomePayResult;
import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.callback.TPDAtomePayGetPrimeSuccessCallback;
import tech.cherri.tpdirect.callback.TPDAtomePayResultListener;
import tech.cherri.tpdirect.callback.TPDGetPrimeFailureCallback;
import tech.cherri.tpdirect.exception.TPDAtomePayException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        TPDAtomePayGetPrimeSuccessCallback, TPDGetPrimeFailureCallback, ResultListener, TPDAtomePayResultListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_READ_PHONE_STATE = 101;
    ImageButton atomePayBtn;
    private TPDAtomePay tpdAtomePay;
    private String itemId;
    private String itemName;
    private String itemQuantity;
    private String itemPrice;
    private TextView actionResult;
    private ProgressDialog progressDialog;

    private boolean isActivityVisible;
    private String temporaryResultString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "SDK version is " + TPDSetup.getVersion());

        //Setup environment.
        TPDSetup.initInstance(getApplicationContext(),
                Constants.APP_ID, Constants.APP_KEY, Constants.SERVER_TYPE);

        findViews();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions();
//        } else {
//            prepareAtomePay();
//        }
        prepareAtomePay();
        handleIncomingIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume, " + temporaryResultString);
        this.isActivityVisible = true;
        hideProgressDialog();
        if (!this.temporaryResultString.isEmpty()) {
            Log.d("MainActivity", "onResume, load result from temp string, because activity is currently visible.");
            this.actionResult.setText(temporaryResultString);
            this.temporaryResultString = "";
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause");
        this.isActivityVisible = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null) {
            Log.d("terry", "MainActivity handleIncomingIntent:" + intent.toString());
        } else {
            Log.d("terry", "MainActivity handleIncomingIntent: null");
            return;
        }
        if (intent.getData() == null) {
            return;
        }
        if (intent.getData().toString().startsWith("https")) {
            doUniversalLink(intent.getData());
        } else {
            doDeepLink(intent.getData());
        }
    }

    private void doUniversalLink(Uri data) {
        if (tpdAtomePay == null) {
            prepareAtomePay();
        }
        showProgressDialog();
        tpdAtomePay.parseToAtomePayResult(getApplicationContext(), data, this);
    }

    private void doDeepLink(Uri data) {
        String result = "Result:" +
                "\nstatus: " + data.getQueryParameter("status") +
                "\nrec_trade_id:" + data.getQueryParameter("rec_trade_id") +
                "\nbank_transaction_id:" + data.getQueryParameter("bank_transaction_id") +
                "\norder_number:" + data.getQueryParameter("order_number");
        this.actionResult.setText(result);
    }

    private void findViews() {
        atomePayBtn = findViewById(R.id.AtomePayButton);
        atomePayBtn.setOnClickListener(this);

        itemId = ((EditText) findViewById(R.id.itemIdEditText)).getText().toString();
        itemName = ((EditText) findViewById(R.id.itemNameEditText)).getText().toString();
        itemQuantity = ((EditText) findViewById(R.id.itemQuantityEditText)).getText().toString();
        itemPrice = ((EditText) findViewById(R.id.itemPriceEdittext)).getText().toString();

        actionResult = (TextView) findViewById(R.id.actionResult);
    }

    private void prepareAtomePay() {
        boolean isAtomePayAvailable = TPDAtomePay.isAtomePayAppAvailable(this.getApplicationContext());
        if (isAtomePayAvailable) {
            try {
                tpdAtomePay = new TPDAtomePay(this.getApplicationContext(), Constants.RETURN_URL);
                atomePayBtn.setVisibility(View.VISIBLE);
                actionResult.setText("Atome Pay is Available.");
            } catch (TPDAtomePayException e) {
                e.printStackTrace();
                actionResult.setText("Atome Pay is not available");
            }
        } else {
            actionResult.setText("Atome Pay is not available");
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
//        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "PERMISSION IS ALREADY GRANTED");
//            prepareAtomePay();
//        } else {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
//        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.AtomePayButton) {
            showProgressDialog();
            tpdAtomePay.getPrime(this, this);
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

    //TPDAtomePayGetPrimeSuccessCallback
    @Override
    public void onSuccess(String prime) {
        String text = "your prime is = " + prime;
        this.actionResult.setText(text);
        showProgressDialog();


        ApiUtil.callAtomePayByPrime(
                getApplicationContext(),
                prime,
                itemId,
                itemName,
                itemQuantity,
                itemPrice,
                this);

    }

    //TPDGetPrimeFailureCallback
    @Override
    public void onFailure(int status, String msg) {
        hideProgressDialog();
        this.actionResult.setText(msg);
    }

    // ResultListener
    @Override
    public void onTaskSuccess(JSONObject jsonObject) {
        hideProgressDialog();
        try {
            String paymentUrl = jsonObject.getString("payment_url");
            String text = "Pay-by-prime: payment_url = " + paymentUrl;
            this.actionResult.setText(text);
            tpdAtomePay.redirectWithUrl(paymentUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ResultListener
    @Override
    public void onTaskFailed(String resultString) {
        hideProgressDialog();
        this.actionResult.setText(resultString);
    }

    // TPDAtomePayResultListener
    @Override
    public void onParseSuccess(TPDAtomePayResult result) {
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
            Log.d("MainActivity", "onParseSuccess, save result to temp string, because activity is currently not visible.");
            this.temporaryResultString = text;
        }

    }

    // TPDAtomePayResultListener
    @Override
    public void onParseFail(int status, String msg) {
        Log.d("MainActivity", "onParseFail, msg"+ msg);
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
            Log.d("MainActivity", "onParseFail, save result to temp string, because activity is currently not visible.");
            this.temporaryResultString = text;
        }
    }
}