package tech.cherri.ipassmoneyexample;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import tech.cherri.tpdirect.api.ipassmoney.TPDIpassMoney;
import tech.cherri.tpdirect.api.ipassmoney.TPDIpassMoneyResult;
import tech.cherri.tpdirect.callback.TPDIpassMoneyResultListener;
import tech.cherri.tpdirect.exception.TPDCustomException;


public class IpassMoneyFragment extends Fragment implements View.OnClickListener {

    public IpassMoneyFragment(boolean remember) {
        this.remember = remember;
    }

    public static final String TAG = IpassMoneyFragment.class.getSimpleName();

    private TextView tvMerchantId, tvGetPrimeResultState, tvPayByPrimeResultState, tvIpassMoneyResult;
    private EditText etMerchantId, inputUrl;
    private Button btnGetPrime, btnPayByPrime, btnRefresh;
    private ProgressBar progressCircular;

    private TPDIpassMoney tpdIpassMoney;

    private String txPrime;
    private boolean remember;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ipass_money, container, false);

        setupView(view);

        try {
            tpdIpassMoney = new TPDIpassMoney(requireActivity().getApplicationContext(), inputUrl.getText().toString());
        } catch (TPDCustomException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (remember) {
            etMerchantId.setText("IPASS.MONEY.ONLINE.BINDING.TEST");
            Toast.makeText(requireActivity().getApplicationContext(), "Binding", Toast.LENGTH_SHORT).show();
        } else {
            etMerchantId.setText("IPASS.MONEY.ONLINE.EC.TEST");
            Toast.makeText(requireActivity().getApplicationContext(), "EC", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleIncomingIntent(Intent intent) {
        Log.d(TAG, "handleIncomingIntent, intent: " + intent.toString());
        if (intent == null || intent.getData() == null) {
            return;
        }
        Log.d(TAG, "handleIncomingIntent, data: " + intent.getData());
        if (intent.getData().toString().startsWith("http")) {
            Toast.makeText(requireActivity().getApplicationContext(), "Universal Link", Toast.LENGTH_SHORT).show();
            androidAppLink(intent.getData());
        } else {
            Toast.makeText(requireActivity().getApplicationContext(), "Deep Link", Toast.LENGTH_SHORT).show();
            androidDeepLink(intent.getData());
        }
    }

    private void androidAppLink(Uri data) {
        if (tpdIpassMoney == null) {
            this.prepareIpassMoney();
        }

        this.showLoadingProgressbar();

        tpdIpassMoney.parseToIpassMoneyResult(requireActivity().getApplicationContext(), data, new TPDIpassMoneyResultListener() {
            @Override
            public void onParseSuccess(TPDIpassMoneyResult result) {
                dismissLoadingProgressbar();

                Log.d(TAG, "onParseSuccess, result " + result.toString());
                String text = "status:" + result.getStatus()
                        + "\nrec_trade_id:" + result.getRecTradeId()
                        + "\nbank_transaction_id:" + result.getBankTransactionId()
                        + "\norder_number:" + result.getOrderNumber();
                tvIpassMoneyResult.setText(text);
            }

            @Override
            public void onParseFail(int status, String msg) {
                dismissLoadingProgressbar();

                Log.d(TAG, "onParseFail, status : " + status + " , msg : " + msg);
                String text = "status : " + status + " , msg : " + msg;
                tvIpassMoneyResult.setText(text);
            }
        });
    }

    private void androidDeepLink(Uri data) {
        String result = "Result:" +
                "\nstatus: " + data.getQueryParameter("status") +
                "\nrec_trade_id:" + data.getQueryParameter("rec_trade_id") +
                "\nbank_transaction_id:" + data.getQueryParameter("bank_transaction_id") +
                "\norder_number:" + data.getQueryParameter("order_number");
        tvIpassMoneyResult.setText(result);
    }

    private void prepareIpassMoney() {
        String payName = "IpassMoney";
        try {
            Toast.makeText(requireActivity().getApplicationContext(), "prepareIpassMoney > " + inputUrl.getText().toString(), Toast.LENGTH_SHORT).show();
            tpdIpassMoney = new TPDIpassMoney(requireActivity().getApplicationContext(), inputUrl.getText().toString());
            tvIpassMoneyResult.setText(payName + " is Available.");
        } catch (TPDCustomException e) {
            Log.e(TAG, "prepareIpassMoney exception: " + Log.getStackTraceString(e));
            tvIpassMoneyResult.setText(payName + "is not available");
        }
    }

    private void setupView(View view) {

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
                    prepareIpassMoney();
                }
            }
        };

        tvMerchantId = view.findViewById(R.id.tvMerchantId);
        tvGetPrimeResultState = view.findViewById(R.id.tvGetPrimeResultState);
        tvPayByPrimeResultState = view.findViewById(R.id.tvPayByPrimeResultState);
        tvIpassMoneyResult = view.findViewById(R.id.tvIpassMoneyResult);

        btnGetPrime = view.findViewById(R.id.btnGetPrime);
        btnPayByPrime = view.findViewById(R.id.btnPayByPrime);
        btnRefresh = view.findViewById(R.id.btnRefresh);
        btnGetPrime.setOnClickListener(this);
        btnPayByPrime.setOnClickListener(this);
        btnRefresh.setOnClickListener(this);

        etMerchantId = view.findViewById(R.id.etMerchantId);
        inputUrl = view.findViewById(R.id.inputUrl);
        inputUrl.addTextChangedListener(textWatcher);

        progressCircular = view.findViewById(R.id.progressCircular);
        this.dismissLoadingProgressbar();
    }

    @Override
    public void onClick(View v) {

        // get prime
        if (v.getId() == R.id.btnGetPrime) {
            this.showLoadingProgressbar();

            tpdIpassMoney.getPrime(prime -> {
                String text = "your prime is = " + prime;
                tvGetPrimeResultState.setText(text);
                this.dismissLoadingProgressbar();

                this.txPrime = prime;
            }, (status, prime) -> {
                String text = "your prime is = " + prime;
                tvGetPrimeResultState.setText(text);
                this.dismissLoadingProgressbar();

                this.txPrime = prime;
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

            IpassMoneyFragment ipassMoneyFragment =
                    (IpassMoneyFragment) requireActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.FRAGMENT_TAG);

            PayByPrimeTask payByPrimeTask = new PayByPrimeTask(this.txPrime, details, ipassMoneyFragment, this.remember);

            payByPrimeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        // clear result textview
        if (v.getId() == R.id.btnRefresh) {
            tvGetPrimeResultState.setText("");
            tvPayByPrimeResultState.setText("");
            tvIpassMoneyResult.setText("");
        }
    }

    /**
    *  Progressbar
    */
    private void showLoadingProgressbar() {
        progressCircular.setVisibility(View.VISIBLE);
    }

    private void dismissLoadingProgressbar() {
        progressCircular.setVisibility(View.GONE);
    }

    public void showMessage(String result, boolean isSuccess, String paymentUrl) {
        Log.d(TAG, result);

        this.dismissLoadingProgressbar();

        if (isSuccess) {
            Log.d(TAG, "onTaskSuccess, result : " + result);
            tpdIpassMoney.redirectWithUrl(paymentUrl);
        } else {
            Log.d(TAG, "onTaskFailed, result : " + result);
        }

        tvPayByPrimeResultState.setText(result);
    }

}