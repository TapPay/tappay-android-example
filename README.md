# tappay-android-example

TapPay SDK example code for Android Platform.


TapPay Android SDK is used to get token(i.e. prime) on Android platform for charging a credit card.

>Obtain your app id and keys here.
     https://www.tappaysdk.com/en


#

## Usage of each pay
  - [Direct Pay](#direct-pay)
  - [Google Pay](#google-pay)
  - [LINE Pay](#line-pay)
  - [Samsung Pay](#samsung-pay)
  - [JKOPAY](#jkopay)
  - [Easy-Wallet](#easy-wallet)
  - [Atome](#atome)
  - [Pi-Wallet](#pi-wallet)
  - [Plus Pay](#plus-pay)

# 
## Setup Android App link in Android Studio


1. Setup a config need to use an API return JSON string, API path https://"your host"/"your path" 
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

2. Android studio will generate  "assetlinks.json" for you, and you need to save above file to : 
   https://"your host"/.well-known/assetlinks.json


![](Pic/app-links-1.png)
![](Pic/app-links-2.png)

#

## Direct Pay
![direct pay demo](Gif/direct_pay_demo.gif)

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

## 如需使用 RBA 相關功能
- 若你是使用3.9.0 版本,直接使用tpdirect.aar 即可
- 若您是使用3.9.0 之前版本,請將 /DirectPayExample/app/libs/android-A1.0.0.aar 此檔案與 tpdirect.aar 一同放置於 lib 中

#

## Google Pay
![google pay demo](Gif/google_pay_demo.gif)

1. Import tpdirect.aar into your project.
2. Add dependencies into your app's **build.gradle**
    ```
    compile 'com.android.support:appcompat-v7:24.1.1'
    implementation 'com.google.android.gms:play-services-wallet:19.3.0'
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
![line pay demo](Gif/line_pay_demo.gif)

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
![samsung pay demo](Gif/samsung_pay_demo.gif)

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

## Pi-Wallet

1. Import tpdirect.aar into your project.
2. Use TPDSetup to initialize the SDK and setup environment.
``` android
TPDSetup.initInstance(getApplicationContext(),
                Constants.APP_ID, Constants.APP_KEY, TPDServerType.Sandbox);
```
3. Add below intent-filter to an Activity for receiving Pi-Wallet Result with App Link in AndroidManifest.xml and set launch mode to "SingleTask"

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

4. Add below queries element to manifest for Pi-Wallet package visibility in Android 11 and later version

``` xml
   <queries>
        <!-- for pi-wallet production app open  -->
        <package android:name="tw.com.pchome.android.pi" />
        <!-- for pi-wallet test app open -->
        <!--    <package android:name="tw.com.pchome.android.pi.partner" />-->
   </queries>
```

5. Check Pi-Wallet availability.

boolean isPiWalletAvailable = TPDPiWallet.isPiWalletAvailable(this.getApplicationContext());

6. Setup TPDPiWallet with [Android app links](#setup-android-app-link-in-android-studio)
   For example:
``` android
TPDPiWallet tpdPiWallet = new TPDPiWallet(getApplicationContext(), "your android app links");
```

7.  Open corresponding Pi-Wallet payment method by paymentUrl obtained from TapPay pay-by-prime API
``` android
tpdPiWallet.redirectWithUrl(paymentUrl);
```

8. Receive PiWalletResult in Activity life cycle "onCreate" or "onNewIntent" (depend on the activity had been destroyed or not)

``` android
tpdPiWallet.parseToPiWalletResult(getApplicationContext(), intent.getData(), TPDPiWalletResultListener listener)
```

8. Obtain TPDPiWalletResult in "onParseSuccess" TPDPiWalletResult has:
``` android
status
recTradeId
bankTransactionId
orderNumber
```


#

## Plus Pay

1. Import tpdirect.aar into your project.
2. Use TPDSetup to initialize the SDK and setup environment.
``` android
TPDSetup.initInstance(getApplicationContext(),
                Constants.APP_ID, Constants.APP_KEY, TPDServerType.Sandbox);
```
3. Specify intent-filter to an Activity for receiving Plus Pay Result with [Android app links](#setup-android-app-link-in-android-studio) (highly recommand) in AndroidManifest.xml and set launch mode to "SingleTask"

ex:
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

4. Add below queries element to manifest for Plus Pay package visibility in Android 11 and later version

``` xml
    <queries>
        <!-- for plus pay production app -->
        <package android:name="grasea.familife" />
        <!-- for plus pay sandbox app -->
<!--        <package android:name="tw.com.pluspay.vendor.uat" />-->
    </queries>
```

5. Check Plus Pay availability.

``` Java
boolean isPlusPayAvailable = TPDPlusPay.isPlusPayAvailable(this.getApplicationContext());
```

6. Setup TPDPlusPay with [Android app links](#setup-android-app-link-in-android-studio)
   ex:
``` Java
TPDPlusPay tpdPlusPay = new TPDPlusPay(getApplicationContext(), "your android app links");
``` 

7.  Open corresponding Plus Pay payment method by paymentUrl obtained from TapPay pay-by-prime API
``` Java
tpdPlusPay.redirectWithUrl(paymentUrl);
```

8. Receive Plus Pay Result in Activity life cycle "onCreate" or "onNewIntent" (depend on the activity had been destroyed or not)

``` Java
tpdPlusPay.parseToPlusPayResult(getApplicationContext(), data, TPDPlusPayResultListener listener);
```

9.  callback from TPDPlusPayResultListener.onParseSuceess will return following attribute if you need to show in your UI
    
``` 
status
rec_trade_id
bank_transaction_id
order_number
```


#

## PX Pay Plus

1. Import tpdirect.aar into your project.
2. Use TPDSetup to initialize the SDK and setup environment.
``` android
TPDSetup.initInstance(getApplicationContext(),
                Constants.APP_ID, Constants.APP_KEY, TPDServerType.Sandbox);
```
3. Specify intent-filter to an Activity for receiving PX Pay Plus Result with [Android app links](#setup-android-app-link-in-android-studio) (highly recommand) in AndroidManifest.xml and set launch mode to "SingleTask"

ex:
``` xml
<activity
    android:name=".MainActivity"
    android:launchMode="singleTask">

    <intent-filter>
        <action android:name="android.intent.action.MAIN" />

        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>

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

4. Add below queries element to manifest for PX Pay Plus package visibility in Android 8 and later version

``` xml
    <queries>
        <package android:name="com.pxpayplus.app" />
    </queries>
```

5. Setup TPDPXPayPlus with [Android app links](#setup-android-app-link-in-android-studio)
   ex:
``` Java
TPDPXPayPlus tpdPXPayPlus = new TPDPXPayPlus(getApplicationContext(), "your android app links");
``` 

6.  Open corresponding PX Pay Plus payment method by paymentUrl obtained from TapPay pay-by-prime API
``` Java
tpdPXPayPlus.redirectWithUrl(paymentUrl);
```

7. Receive PX Pay Plus Result in Activity life cycle "onCreate" or "onNewIntent" (depend on the activity had been destroyed or not)

``` Java
tpdPXPayPlus.parseToPlusPayResult(getApplicationContext(), data, TPDPXPayPlusResultListener listener);
```

8.  callback from TPDPXPayPlusResultListener.onParseSuceess will return following attribute if you need to show in your UI
    
``` 
status
rec_trade_id
bank_transaction_id
order_number
```

#

## iPass Money

1. Import tpdirect.aar into your project.
2. Use TPDSetup to initialize the SDK and setup environment.
``` android
TPDSetup.initInstance(getApplicationContext(),
                Constants.APP_ID, Constants.APP_KEY, TPDServerType.Sandbox);
```
3. Specify intent-filter to an Activity for receiving iPass Money Result with [Android app links](#setup-android-app-link-in-android-studio) (highly recommand) in AndroidManifest.xml and set launch mode to "SingleTask"

ex:
``` xml
<activity
    android:name=".MainActivity"
    android:launchMode="singleTask">

    <intent-filter>
        <action android:name="android.intent.action.MAIN" />

        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>

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

4. Add below queries element to manifest for iPass Money package visibility in Android 7 and later version

``` xml
    <queries>
        <package android:name="com.ipass.ipassmoney" />
    </queries>
```

5. Setup TPDIpassMoney with [Android app links](#setup-android-app-link-in-android-studio)
   ex:
``` Java
TPDIpassMoney tpdIpassMoney = new TPDIpassMoney(getApplicationContext(), "your android app links");
``` 

6.  Open corresponding iPass Money payment method by paymentUrl obtained from TapPay pay-by-prime API
``` Java
tpdIpassMoney.redirectWithUrl(paymentUrl);
```

7. Receive iPass Money Result in Activity life cycle "onCreate" or "onNewIntent" (depend on the activity had been destroyed or not)

``` Java
tpdIpassMoney.parseToIpassMoneyResult(getApplicationContext(), data, TPDIpassMoneyResultListener listener);
```

8.  callback from TPDIpassMoneyResultListener.onParseSuceess will return following attribute if you need to show in your UI
    
``` 
status
rec_trade_id
bank_transaction_id
order_number
```

