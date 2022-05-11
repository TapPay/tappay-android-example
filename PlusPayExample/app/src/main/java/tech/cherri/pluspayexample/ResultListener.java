package tech.cherri.pluspayexample;

import org.json.JSONObject;

public interface ResultListener {
    void onTaskSuccess(JSONObject jsonObject);

    void onTaskFailed(String resultString);
}
