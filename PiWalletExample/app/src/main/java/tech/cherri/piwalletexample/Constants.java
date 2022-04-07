package tech.cherri.piwalletexample;

import tech.cherri.tpdirect.api.TPDServerType;

public class Constants {
    public static final TPDServerType SERVER_TYPE = TPDServerType.Production;
//    public static final TPDServerType SERVER_TYPE = TPDServerType.Sandbox; -- sandbox

    public static final String TAPPAY_DOMAIN = "https://prod-main.sit.tappaysdk.com/tpc";
//    public static final String TAPPAY_DOMAIN = "https://sandbox-main.sit.tappaysdk.com/tpc"; --sandbox

    public static final String TAPPAY_PAY_BY_PRIME_URL = "/payment/pay-by-prime";
    public static final String FRONTEND_REDIRECT_URL_EXAMPLE = "https://example.com/front-end-redirect";
    public static final String BACKEND_NOTIFY_URL_EXAMPLE = "https://example.com/back-end-notify";

    public static final String PARTNER_KEY = "PARTNER KEY";
    public static final String APP_KEY = "APP KEY";
    public static final Integer APP_ID = -1; // your app id

    public static final String THIS_APP = "UNIVERSAL LINK"; // for universal links
    public static final String REPLACE_THIS_PAY_BY_PRIME_DOMAIN = "REPLACE ME";
    public static final String REPLACE_THIS_MERCHANT_ID = "pi.test";
}
