package tech.cherri.atomepayexample;

import tech.cherri.tpdirect.api.TPDServerType;

public class Constants {
    public static final TPDServerType SERVER_TYPE = TPDServerType.Production;
//    public static final TPDServerType SERVER_TYPE = TPDServerType.Sandbox; -- sandbox

    public static final String TAPPAY_DOMAIN = "https://prod-main.sit.tappaysdk.com/tpc";
//    public static final String TAPPAY_DOMAIN = "https://sandbox-main.sit.tappaysdk.com/tpc"; --sandbox

    public static final String TAPPAY_PAY_BY_PRIME_URL = "/payment/pay-by-prime";
    public static final String FRONTEND_REDIRECT_URL_EXAMPLE = "https://example.com/front-end-redirect";
    public static final String BACKEND_NOTIFY_URL_EXAMPLE = "https://example.com/back-end-notify";

    public static final String PARTNER_KEY = "72DMgo9RQN2BSW4SmaHWYVOUCEIUDMg9i1JnXKic\n";
    public static final String APP_KEY = "wfQOgMDMXJ5AOentgH1dV6OKLwe50e4Ipyl9BNA9";
    public static final Integer APP_ID = 9; // your app id

    public static final String MERCHANT_ID = "atome.test";

    public static final String PAY_BY_PRIME_URL = "https://tpdirect-demo.tappaysdk.com/create_order_sit";

//    public static final String THIS_APP = "atomepayexample://tech.cherri.atomepayexample"; // for intents
    public static final String RETURN_URL = "https://6971-211-72-111-160.ngrok.io/test"; // for universal links
}
