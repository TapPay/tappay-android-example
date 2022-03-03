package tech.cherri.atomepayexample;

import org.json.JSONObject;

public interface ResultListener {
    void onTaskSuccess(JSONObject jsonObject);

    void onTaskFailed(String resultString);
}
