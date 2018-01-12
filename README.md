# tappay-android-example

TapPay SDK example code for Android Platform.


TapPay Android SDK is used to get token(i.e. prime) on Android platform for charging a credit card.

>Obtain your app id and keys here.
     https://www.tappaysdk.com/en


# Demo
## Direct Pay
![direct pay demo](https://media.giphy.com/media/xUOxf4aa0035sXkfeg/giphy.gif)


## Pay with Google
![pay with google demo](Gif/pay_with_google_demo.gif)

## Line Pay
![line pay demo](Gif/line_pay_demo.gif)

# Usage

## Direct Pay
1. Import tpdirect.aar into your project.
2. Use TPDSetup to initialize the SDK and setup environment.
    ```Java
    TPDSetup.initInstance(getApplicationContext(), "APP_ID", "APP_KEY"
    , TPDServerType.Sandbox);
    ```
3. Add TPDForm in your layout.
    ```xml
    <tech.cherri.tpdirect.api.TPDForm  
        android:id="@+id/tpdForm"  
        android:layout_width="wrap_content"  
        android:layout_height="wrap_content">  
    </tech.cherri.tpdirect.api.TPDForm>
    ```

4. Setup TPDCard with TPDForm. 
    ```Java
    TPDCard card = TPDCard.setup(TPDForm tpdForm)
        .onSuccessCallback(new TPDTokenSuccessCallback(){
            @Override
            public void onSuccess(String prime, TPDCardInfo cardInfo) {
            //get Prime succeeded. }
            }
        ).onFailureCallback(new TPDTokenFailureCallback(){
            @Override
            public void onFailure(int status, String reportMsg) {
            //get Prime failed. }
        });
    ```

5. Fill credit card information in TPDForm and get Prime from TapPay.
    ```Java
    card.getPrime();
    ```
    
## Pay with Google

1. Import tpdirect.aar into your project.
2. Add dependencies into your app's **build.gradle**
    ```
    compile 'com.google.android.gms:play-services-wallet:11.4.2'
    ```
3. Add below metadata in AndroidManifest.xml
    ```xml
    <meta-data
        android:name="com.google.android.gms.version"
        android:value="@integer/google_play_services_version" /> 
    <meta-data
        android:name="com.google.android.gms.wallet.api.enabled"
        android:value="true" />
    ```

4. Use TPDSetup to initialize the SDK and setup environment.
    ```Java
    TPDSetup.initInstance(getApplicationContext(), "APP_ID", "APP_KEY"
    , TPDServerType.Sandbox);
    ```
5. Create : 
    - TPDMerchant for Pay with Google process 
    ```
    TPDMerchant tpdMerchant = new TPDMerchant();
    tpdMerchant.setSupportedNetworks(allowedNetworks);
    ```
    - TPDConsumer for requiring consumer's payment detail.
    ```
    TPDConsumer tpdConsumer = new TPDConsumer();
    tpdConsumer.setPhoneNumberRequired(true);
    tpdConsumer.setShippingAddressRequired(true);
    tpdConsumer.setEmailRequired(true);
    ```

6. Setup TPDPayWithGoogle with TPDMerchant and TPDConsumer.
    ```Java
    TPDPayWithGoogle tpdPayWithGoogle = new TPDPayWithGoogle(this, tpdMerchant, tpdConsumer);
    ```

7. Check Pay with Google availability.
    ```
    tpdPayWithGoogle.canUserPayWithGoogle(TPDPayWithGoogleListener var1);
    ```

8. Obtain PaymentData.
    ```
    tpdPayWithGoogle.requestPayment(TransactionInfo.newBuilder()
                    .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                    .setTotalPrice("1")
                    .setCurrencyCode("TWD")
                    .build(), LOAD_PAYMENT_DATA_REQUEST_CODE);
    ```
    
9. Get Prime from TapPay.
    ```
     tpdPayWithGoogle.getPrime(paymentData, TPDTokenSuccessCallback, TPDTokenFailureCallback);
    ```
    
    
## Line Pay

1. Import tpdirect.aar into your project.
2. Use TPDSetup to initialize the SDK and setup environment.
    ```Java
    TPDSetup.initInstance(getApplicationContext(), "APP_ID", "APP_KEY"
    , TPDServerType.Sandbox);
    ```
3. Add below intent-filter to an Activity for receiving Line Pay Result in AndroidManifest.xml and set launch mode to "SingleTask"
    
    For example :
    ```xml
     <activity
            android:name=".LinePayActivity"
            android:launchMode="singleTask">
        <intent-filter>
            <action android:name="android.intent.action.VIEW" />

            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />

            <data
                android:host="tech.cherri"
                android:scheme="linepayexample" />
        </intent-filter>
    </activity>
    ```
4. Check Line Pay availability.
    ```
    boolean isLinePayAvailable =TPDLinePay.isLinePayAvailable(Context context);
    ```
    
5. Setup TPDLinePay with uri which is formed with host and scheme(both declared in Step3).
 
   For example:
    ```
    TPDLinePay tpdLinePay = new TPDLinePay(Context context, "linepayexample://tech.cherri");
    ```
    
6. Open corresponding LinePay payment method by paymentUrl obtained from TapPay pay-by-prime API
    ```
     tpdLinePay.redirectWithUrl(paymentUrl);
    ```
7. Receive LinePayResult in Activity life cycle "onCreate" or "onNewIntent" (depend on the activity had been destroyed or not)
    ```
    TPDLinePayResult result;
    try {
        result = TPDLinePay.parseToLinePayResult(Context, intent.getData());
    } catch (TPDLinePayException e) {
        //Error while parsing.
    }
    ```