package tech.cherri.piwalletexample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import tech.cherri.tpdirect.api.TPDPiWallet;
import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.callback.TPDPiWalletResultListener;
import tech.cherri.tpdirect.exception.TPDPiWalletException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    public PayByPrimeResultListener resultCallback;
    TextView resultText;
    TPDPiWallet tpdPiWalletPay;
    private ImageButton piWalletButton;
    private ProgressDialog progressDialog;
    private Context context;
    private GetPrimeSuccessCallback successPrimeGetterCallback;
    private GetPrimeFailCallback failurePrimeGetterCallback;
    private TPDPiWalletResultListener piWalletResultCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findviews();

        TPDSetup.initInstance(this.getApplicationContext(),
                Constants.APP_ID, Constants.APP_KEY, Constants.SERVER_TYPE);
        Log.d(TAG, "SDK version is " + TPDSetup.getVersion());
        preparePiWallet();
        prepareAllCallbackObjects();
        handleIncomingIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null) {
            Log.d("MainActivity", "incoming intent is null");
            return;
        }

        Log.d("MainActivity", intent.toString());
        if (intent.getData() == null) {
            return;
        }
        Log.d("MainActivity", intent.getData().toString());
        if (intent.getData().toString().startsWith("https")) {
            doUniversalLink(intent.getData());
        } else {
            doDeepLink(intent.getData());
        }
    }

    private void doDeepLink(Uri data) {

        String result = "Result:" +
                "\nstatus: " + data.getQueryParameter("status") +
                "\nrec_trade_id:" + data.getQueryParameter("rec_trade_id") +
                "\nbank_transaction_id:" + data.getQueryParameter("bank_transaction_id") +
                "\norder_number:" + data.getQueryParameter("order_number");
        this.resultText.setText(result);
    }

    private void doUniversalLink(Uri data) {
        showProgressDialog();
        tpdPiWalletPay.parseToPiWalletResult(context, data, piWalletResultCallback);
    }

    private void prepareAllCallbackObjects() {
        successPrimeGetterCallback = new GetPrimeSuccessCallback(this);
        failurePrimeGetterCallback = new GetPrimeFailCallback(resultText, progressDialog);
        resultCallback = new PayByPrimeResultCallback(this);
        piWalletResultCallback = new PiWalletResultCallback(this);
    }

    private void preparePiWallet() {
        context = this.getApplicationContext();
        boolean isPiWalletInstalled = TPDPiWallet.isPiWalletInstalled(context);
        if (!isPiWalletInstalled) {
            resultText.setText("Pi Wallet is not installed.");
            piWalletButton.setVisibility(View.INVISIBLE);
            return;
        }

        try {
            tpdPiWalletPay = new TPDPiWallet(context, Constants.THIS_APP);
            resultText.setText("Pi Wallet is available.");
        } catch (TPDPiWalletException e) {
            e.printStackTrace();
            resultText.setText("Pi Wallet is not available.");
        }
    }

    private void findviews() {
        resultText = findViewById(R.id.resultText);
        piWalletButton = findViewById(R.id.payButton);
        piWalletButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.payButton) {
            showProgressDialog();
            tpdPiWalletPay.getPrime(this.successPrimeGetterCallback, this.failurePrimeGetterCallback);
        }
    }

    void showProgressDialog() {
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

    void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}