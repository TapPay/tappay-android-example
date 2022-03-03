package tech.cherri.atomepayexample;

import android.content.Context;
import android.os.AsyncTask;

import tech.cherri.tpdirect.utils.SDKLog;

public class ApiUtil {
    public static void callAtomePayByPrimeWithMiddleServer(
            Context context, String prime, String merchantId, String itemId,
            String itemName, String itemQuantity, String itemPrice, ResultListener listener) {

        SDKLog.d("ApiUtil", "callAtomePayByPrimeWithMiddleServer, prime =" + prime);
        String secretStringRequiredByAtomePay = "[{\"item_id\": \"" + itemId
                + "\",\"item_name\": \"" + itemName
                + " \",\"item_quantity\":" + itemQuantity
                + ",\"item_price\":" + itemPrice + "}]";

        MyAtomePayByPrimeTask atomePayByPrimeTask = new MyAtomePayByPrimeTask(
                context, prime, merchantId, secretStringRequiredByAtomePay, listener);


        atomePayByPrimeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }
}
