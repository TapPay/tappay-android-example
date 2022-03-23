# tappay-android-example

TapPay SDK example code for Android Platform.


TapPay Android SDK is used to get token(i.e. prime) on Android platform for charging a credit card.

>Obtain your app id and keys here.
     https://www.tappaysdk.com/en

## 如需使用 RBA 相關功能
請將 /DirectPayExample/app/libs/android-A1.0.0.aar 此檔案與 tpdirect.aar 一同放置於 lib 中

# Demo
## Direct Pay
![direct pay demo](https://media.giphy.com/media/xUOxf4aa0035sXkfeg/giphy.gif)


## Google Pay
![google pay demo](Gif/google_pay_demo.gif)

## LINE Pay
![line pay demo](Gif/line_pay_demo.gif)

## Samsung Pay
![samsung pay demo](Gif/samsung_pay_demo.gif)


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
    
## Google Pay

1. Import tpdirect.aar into your project.
2. Add dependencies into your app's **build.gradle**
    ```
    compile 'com.android.support:appcompat-v7:24.1.1'
    compile 'com.google.android.gms:play-services-wallet:16.0.0'
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
    - TPDMerchant for Google Pay process 
    ```
    TPDMerchant tpdMerchant = new TPDMerchant();
    tpdMerchant.setSupportedNetworks(allowedNetworks);
    tpdMerchant.setSupportedAuthMethods(allowedAuthMethods);
    tpdMerchant.setMerchantName("Your merchant name");
    ```
    - TPDConsumer for requiring consumer's payment detail.
    ```
    TPDConsumer tpdConsumer = new TPDConsumer();
    tpdConsumer.setPhoneNumberRequired(true);
    tpdConsumer.setShippingAddressRequired(true);
    tpdConsumer.setEmailRequired(true);
    ```

6. Setup TPDGooglePay with TPDMerchant and TPDConsumer.
    ```Java
    TPDGooglePay tpdGooglePay = new TPDGooglePay(this, tpdMerchant, tpdConsumer);
    ```

7. Check Google Pay availability.
    ```
    tpdGooglePay.isGooglePayAvailable(TPDGooglePayListener var1);
    ```

8. Obtain PaymentData.
    ```
    tpdGooglePay.requestPayment(TransactionInfo.newBuilder()
                    .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                    .setTotalPrice("1")
                    .setCurrencyCode("TWD")
                    .build(), LOAD_PAYMENT_DATA_REQUEST_CODE);
    ```
    
9. Get Prime from TapPay.
    ```
     tpdGooglePay.getPrime(paymentData, TPDTokenSuccessCallback, TPDTokenFailureCallback);
    ```
    
    
## LINE Pay

1. Import tpdirect.aar into your project.
2. Use TPDSetup to initialize the SDK and setup environment.
    ```Java
    TPDSetup.initInstance(getApplicationContext(), "APP_ID", "APP_KEY"
    , TPDServerType.Sandbox);
    ```
3. Add below intent-filter to an Activity for receiving LINE Pay Result in AndroidManifest.xml and set launch mode to "SingleTask"
    
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
4. Add below queries element to manifest for LINE Pay package visibility in Android 11 and later version

```
   <queries>
        <!-- for line pay open -->
        <package android:name="jp.naver.line.android" />
   </queries>
```

5. Check LINE Pay availability.
    ```
    boolean isLinePayAvailable =TPDLinePay.isLinePayAvailable(Context context);
    ```
    
6. Setup TPDLinePay with uri which is formed with host and scheme(both declared in Step3).
 
   For example:
    ```
    TPDLinePay tpdLinePay = new TPDLinePay(Context context, "linepayexample://tech.cherri");
    ```
    
7. Open corresponding LinePay payment method by paymentUrl obtained from TapPay pay-by-prime API
    ```
     tpdLinePay.redirectWithUrl(paymentUrl);
    ```
8. Receive LinePayResult in Activity life cycle "onCreate" or "onNewIntent" (depend on the activity had been destroyed or not)
    ```
    tpdLinePay.parseToLinePayResult(Context context, intent.getData(), TPDLinePayResultListener listener);
    ```
9. Obtain TPDLinePayResult in "onParseSuccess"
TPDLinePayResult has:
    - status (0 = Success , 924 = Canceled by User)
    - recTradeId 
    - bankTransactionId 
    - orderNumber


## Samsung Pay

1. Import tpdirect.aar and samsungpay-1.x.jar into your project.

2. Add below meta data to your application tag in AndroidManifest.xml
    
    For example :
    ```xml
      <application
        android:icon="@mipmap/ic_launcher"
        android:theme="@style/AppTheme"
        .
        .
        .>

          <!--Set to 'N' if in release mode.-->
        <meta-data
            android:name="debug_mode"
            android:value="Y" />

        <meta-data
            android:name="spay_sdk_api_level"
            android:value="1.8" />
        
        <!--Debug Key is valid for 3 months;-->
        <!--Remove below metadata if in release mode-->
        <meta-data
            android:name="spay_debug_api_key"
            android:value=“{Your debug_api_key obtained from Samsung}” />

       <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    ```
3. Use TPDSetup to initialize the SDK and setup environment.
    ```Java
    TPDSetup.initInstance(getApplicationContext(), "APP_ID", "APP_KEY"
    , TPDServerType.Sandbox);
    ```

4. Create TPDMerchant for Samsung Pay process 
    ```
    TPDMerchant tpdMerchant = new TPDMerchant();
    tpdMerchant.setMerchantName(“Your Merchant Name");
    tpdMerchant.setSupportedNetworks(allowedNetworks);
    tpdMerchant.setSamsungMerchantId(“Your SamsungMerchantId obtained from TapPay Portal”));
    tpdMerchant.setCurrencyCode("TWD");

    ```
5. Setup TPDSamsungPay with TPDMerchant and service Id
    ```Java
    TPDSamsungPay tpdSamsungPay = new TPDSamsungPay({Your Activity}, "Your serviceId obtained from Samsung", tpdMerchant);
    ```

6. Check Samsung Pay availability.
    ```
    boolean isSamsungPayAvailable =tpdSamsungPay.isSamsungPayAvailable(TPDSamsungPayStatusListener listener);
    ```
    
7. Get Prime from TapPay.
    ```
    tpdSamsungPay.getPrime(itemTotalAmount, shippingPrice, tax, totalAmount, TPDTokenSuccessCallback, TPDTokenFailureCallback);
    ```

## JKOPAY

1. Import tpdirect.aar into your project.
2. Use TPDSetup to initialize the SDK and setup environment.
``` android
TPDSetup.initInstance(getApplicationContext(),
                Constants.APP_ID, Constants.APP_KEY, TPDServerType.Sandbox);
```
3. Add below intent-filter to an Activity for receiving JKO Pay Result with App Link in AndroidManifest.xml and set launch mode to "SingleTask"

For example :
``` xml
<activity
    android:name=".MainActivity"
    android:launchMode="singleTask">

    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data
            android:host="your host"
            android:pathPattern="/your path"
            android:scheme="https" />

    </intent-filter>
</activity>
```

4. Add below queries element to manifest for JKO Pay package visibility in Android 11 and later version

```
   <queries>
        <!-- for jko pay open -->
        <package android:name="com.jkos.app" />
   </queries>
```

5. Check JKO Pay availability.

boolean isJkoPayAvailable = TPDJkoPay.isJkoPayAvailable(this.getApplicationContext());

6. Setup TPDJkoPay with universal links (both declared in Step3)
For example:
``` android
TPDJkoPay tpdJkoPay = new TPDJkoPay(getApplicationContext(), "your universal links");
```

7.  Open corresponding JkoPay payment method by paymentUrl obtained from TapPay pay-by-prime API
``` android
tpdJkoPay.redirectWithUrl(paymentUrl);
```

8. Receive JkoPayResult in Activity life cycle "onCreate" or "onNewIntent" (depend on the activity had been destroyed or not)

``` android
tpdJkoPay.parseToJkoPayResult(getApplicationContext(), intent.getData(), TPDJkoPayResultListener listener)
```

8. Obtain TPDJkoPayResult in "onParseSuccess" TPDJkoPayResult has:
``` android
status
recTradeId
bankTransactionId
orderNumber
```

## Easy-Wallet

1. Import tpdirect.aar into your project.
2. Use TPDSetup to initialize the SDK and setup environment.
``` android
TPDSetup.initInstance(getApplicationContext(),
                Constants.APP_ID, Constants.APP_KEY, TPDServerType.Sandbox);
```
3. Add below intent-filter to an Activity for receiving Easy-Wallet Result with App Link in AndroidManifest.xml and set launch mode to "SingleTask"

For example :
``` xml
<activity
    android:name=".MainActivity"
    android:launchMode="singleTask">

    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data
            android:host="your host"
            android:pathPattern="/your path"
            android:scheme="https" />

    </intent-filter>
</activity>
```

4. Add below queries element to manifest for easy-wallet package visibility in Android 11 and later version

```
   <queries>
        <!-- for easy-wallet open -->
        <package android:name="com.easycard.wallet" />
   </queries>
```

5. Check Easy-Wallet availability.

boolean isEasyWalletAvailable = TPDEasyWallet.isAvailable(this.getApplicationContext());

6. Setup TPDEasyWallet with universal links (both declared in Step3)
   For example:
``` android
TPDEasyWallet tpdEasyWallet = new TPDEasyWallet(getApplicationContext(), "your universal links");
```

7.  Open corresponding Easy-Wallet payment method by paymentUrl obtained from TapPay pay-by-prime API
``` android
tpdEasyWallet.redirectWithUrl(paymentUrl);
```

8. Receive EasyWalletResult in Activity life cycle "onCreate" or "onNewIntent" (depend on the activity had been destroyed or not)

``` android
tpdEasyWallet.parseToEasyWalletResult(getApplicationContext(), intent.getData(), TPDEasyWalletResultListener listener)
```

8. Obtain TPDEasyWalletResult in "onParseSuccess" TPDEasyWalletResult has:
``` android
status
recTradeId
bankTransactionId
orderNumber
```

## Atome

1. Import tpdirect.aar into your project.
2. Use TPDSetup to initialize the SDK and setup environment.
``` android
TPDSetup.initInstance(getApplicationContext(),
                Constants.APP_ID, Constants.APP_KEY, TPDServerType.Sandbox);
```
3. Add below intent-filter to an Activity for receiving Atome Result with App Link in AndroidManifest.xml and set launch mode to "SingleTask"

For example :
``` xml
<activity
    android:name=".MainActivity"
    android:launchMode="singleTask">

    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data
            android:host="your host"
            android:pathPattern="/your path"
            android:scheme="https" />

    </intent-filter>
</activity>
```

4. Add below queries element to manifest for Atome package visibility in Android 11 and later version

```
   <queries>
        <!-- for atome pay open -->
        <package android:name="tw.atome.paylater" />
   </queries>
```

5. Check Atome availability.

boolean isAtomePayAvailable = TPDAtomePay.isAtomePayAppAvailable(this.getApplicationContext());

6. Setup TPDAtomePay with universal links (both declared in Step3)
   For example:
``` android
TPDAtomePay tpdAtomePay = new TPDAtomePay(getApplicationContext(), "your universal links");
```

7.  Open corresponding Easy-Wallet payment method by paymentUrl obtained from TapPay pay-by-prime API
``` android
tpdAtomePay.redirectWithUrl(paymentUrl);
```

8. Receive AtomeResult in Activity life cycle "onCreate" or "onNewIntent" (depend on the activity had been destroyed or not)

``` android
tpdAtomePay.parseToAtomePayResult(getApplicationContext(), intent.getData(), TPDAtomePayResultListener listener)
```

8. Obtain TPDAtomePayResult in "onParseSuccess" TPDAtomePayResult has:
``` android
status
recTradeId
bankTransactionId
orderNumber
```

## Setup App Link

1. Setup a config need to use an API return JSON string, API path https://"your host"/"your path"  (both declared in Step3)
JSON string For example:
```
[{
  "relation": ["delegate_permission/common.get_login_creds"],
  "target": {
    "namespace": "android_app",
    "package_name": "your package name",
    "sha256_cert_fingerprints":
    ["your sha256_cert_fingerprints"]
  }
}]
```

2. How to get JSON string?


![](Pic/app-links-1.png)
![](Pic/app-links-2.png)
