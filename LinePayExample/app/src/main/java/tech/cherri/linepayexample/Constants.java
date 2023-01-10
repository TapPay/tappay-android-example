package tech.cherri.linepayexample;


import tech.cherri.tpdirect.api.TPDServerType;

public class Constants {

    public static final TPDServerType SERVER_TYPE = TPDServerType.Sandbox;
    public static final String TAPPAY_DOMAIN_SANDBOX = "https://sandbox.tappaysdk.com";
    public static final String TAPPAY_PAY_BY_PRIME_URL = "/tpc/payment/pay-by-prime";
    public static final String FRONTEND_REDIRECT_URL_EXAMPLE = "https://example.com/front-end-redirect";
    public static final String BACKEND_NOTIFY_URL_EXAMPLE = "https://example.com/back-end-notify";


    public static final String TAPPAY_LINEPAY_RESULT_CALLBACK_URI = "linepayexample://tech.cherri";

    public static final String PARTNER_KEY = "your partner key"; //your partner key
    public static final String APP_KEY = "your app key"; //your app key
    public static final Integer APP_ID = 0; //your app id
    public static final String MERCHANT_ID = "your merchant id"; //your merchant id
}
