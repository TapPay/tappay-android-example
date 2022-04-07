package tech.cherri.piwalletexample;

import org.json.JSONObject;

public interface PayByPrimeResultListener {
    void onTaskSuccess(JSONObject jsonObject);

    void onTaskFailed(String resultString);
}