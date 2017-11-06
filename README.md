# tappay-android-example

TapPay SDK example code for Android Platform.


TapPay Android SDK is used to get token(i.e. prime) on Android platform for charging a credit card.


 >Obtain your app id and keys here.
     >https://www.tappaysdk.com/en


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

    
## Android Pay

1. Import tpdirect.aar into your project.
2. Add dependencies into your app's **build.gradle**
    ```
   compile 'com.google.android.gms:play-services-auth:11.0.4' 
   compile ‘com.google.android.gms:play-services-wallet:11.0.4’
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
    - TPDMerchant for Android Pay process 
    - TPDConsumer for requiring consumer's payment detail.

6. Setup TPDAndroidPay with TPDMerchant and TPDConsumer.
    ```Java
    TPDAndroidPay tpdAndroidPay = new TPDAndroidPay(
        FragmentActivity var1
        ,tpdMerchant
        , tpdConsumer);
    ```

7. Check Android Pay availability.
    ```
    tpdAndroidPay.canUserPayWithNetworks(TPDAndroidPayListener var1 ,
    allowedNetworks);
    ```

8. Display Android Pay button. 
    ```
    tpdAndroidPay.generateBuyButton(
        frameLayout,”REQUEST_CODE_MASKED_WALLET
        , tpdCart
        , WalletFragmentOptions options);
    ```
9.  Obtain fullWallet.
    ```
    tpdAndroidPay.confirmWallet(maskedWallet, tpdCart, REQUEST_-
    CODE_RESOLVE_LOAD_FULL_WALLET);
    
    ```
    
10. Get Prime from TapPay.
    ```
    tpdAndroidPay.getPrime(fullWallet, TPDTokenSuccessCallback, TPDToken-
    FailureCallback);
    ```