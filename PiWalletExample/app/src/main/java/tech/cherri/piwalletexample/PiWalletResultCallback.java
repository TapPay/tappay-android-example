package tech.cherri.piwalletexample;

import tech.cherri.tpdirect.api.TPDPiWalletPayResult;
import tech.cherri.tpdirect.callback.TPDPiWalletResultListener;

public class PiWalletResultCallback implements TPDPiWalletResultListener {
    private final MainActivity activity;

    public PiWalletResultCallback(MainActivity mainActivity) {
        this.activity = mainActivity;
    }

    @Override
    public void onParseSuccess(TPDPiWalletPayResult tpdPiWalletPayResult) {
        activity.hideProgressDialog();
        String text = "status:" + tpdPiWalletPayResult.getStatus()
                + "\nrec_trade_id:" + tpdPiWalletPayResult.getRecTradeId()
                + "\nbank_transaction_id:" + tpdPiWalletPayResult.getBankTransactionId()
                + "\norder_number:" + tpdPiWalletPayResult.getOrderNumber();
        activity.resultText.setText(text);
    }

    @Override
    public void onParseFail(int status, String msg) {
        activity.hideProgressDialog();
        String text = "Parse atome Pay result failed  status : " + status + " , msg : " + msg;
        activity.resultText.setText(text);
    }
}
