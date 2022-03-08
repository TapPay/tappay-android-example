package tech.cherri.atomepayexample;

import android.content.Context;
import android.os.AsyncTask;

import tech.cherri.tpdirect.utils.SDKLog;

public class ApiUtil {
    public static void callAtomePayByPrime(
            Context context, String prime, String itemId,
            String itemName, String itemQuantity, String itemPrice, ResultListener listener) {

        SDKLog.d("ApiUtil", "callAtomePayByPrimeWithMiddleServer, prime =" + prime);
        String details = "[{\"item_id\": \"" + itemId
                + "\",\"item_name\": \"" + itemName
                + " \",\"item_quantity\":" + itemQuantity
                + ",\"item_price\":" + itemPrice + "}]";

        AtomePayByPrimeTask atomePayByPrimeTask = new AtomePayByPrimeTask(
                context, prime, details, listener);


        atomePayByPrimeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }
}
