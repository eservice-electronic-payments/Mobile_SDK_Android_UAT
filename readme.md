## Installation
1. In project-level build.gradle file add jitpack repository to your project path
```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

2. In module-level build.gradle file add the dependency
​
```groovy
dependencies {
    implementation 'com.github.intelligentpayments:androidSDK:<latest_version>'
}
```
[![JitPack](https://img.shields.io/jitpack/v/github/intelligentpayments/androidSDK?color=brightgreen&label=JitPack&style=plastic)](https://jitpack.io/#intelligentpayments/androidSDK)
​

Replace `<latest_version>` with latest library version from  JitPack
​
### Update
​
In case of update, just replace `<latest_version>` with the newer one.

## Usage

1. To use Android SDK it's necessary to fetch mobile cashier url and token from API:
​
In our example, in repository we use `Communication.kt` to obtain session token.
Below is a Payload model used in request:
​
```kotlin
class DemoTokenParameters(  //example values
    customerId: String,     //"lovelyrita"
    currency: String,       //"PLN"
    country: String,        //"PL"
    amount: String,         //"2.00"
    action: String,         //"AUTH"
    allowOriginUrl: String, //"http://example.com"
    merchantLandingPageUrl: String, //"https://ptsv2.com/t/ipgmobilesdktest"
    language: String,               //"en"
    myriadFlowId: String,
    customerFirstName: String,      //"Jan"
    customerLastName: String,       //"Mobile"
    merchantNotificationUrl: String //"https://ptsv2.com/t/66i1s-1534805666/post
)
```
For more information check `fetchToken()` method in `MainActivity.kt`.
​
​

2. Then to display web page via SDK, call this method from your activity:
​

**Kotlin**
```kotlin
class YourActivity: Activity() {
    fun startPayment() {
        startEvoPaymentActivityForResult(
        	EVO_PAYMENT_REQUEST_CODE,
	        merchantId,
	        mobileCashierUrl,
	        token,
	        myriadFlowId
        )
    }
}
```
or if you use **Java**:
```java
public class YourActivity extends Activity {
    private void startPayment() {
        EvoPaymentActivityKt.startEvoPaymentActivityForResult(
            this, //Activity
            EVO_PAYMENT_REQUEST_CODE,
            merchantId,
            mobileCashierUrl,
            token,
            myriadFlowId
        );
    }
}
```
3. Then in the same activity you have to override method `onActivityResults(...)` to receive payment results.
​
- `requestCode` will be exactly the same  as provided in `startEvoPaymentActivityForResult`
- `resultCode` can take following values:
```
PAYMENT_SUCCESSFUL = 1
PAYMENT_CANCELED = 2
PAYMENT_FAILED = 3
PAYMENT_UNDETERMINED = 4
PAYMENT_SESSION_EXPIRED = 5
```
​
Sample implementation of `onActivityResult(...)` can looks like this:
​

**Kotlin**
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == EVO_PAYMENT_REQUEST_CODE) {
        when (resultCode) {
            EvoPaymentActivity.PAYMENT_SUCCESSFUL      -> onPaymentSuccessful()
            EvoPaymentActivity.PAYMENT_CANCELED        -> onPaymentCancelled()
            EvoPaymentActivity.PAYMENT_FAILED          -> onPaymentFailed()
            EvoPaymentActivity.PAYMENT_UNDETERMINED    -> onPaymentUndetermined()
            EvoPaymentActivity.PAYMENT_SESSION_EXPIRED -> onSessionExpired()
        }
    }
}
```
​
**Java**
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == EVO_PAYMENT_REQUEST_CODE) {
        switch (resultCode) {
            case EvoPaymentActivity.PAYMENT_SUCCESSFUL:
                onPaymentSuccessful();
                break;
            case EvoPaymentActivity.PAYMENT_CANCELED:
                onPaymentCancelled();
                break;
            case EvoPaymentActivity.PAYMENT_FAILED:
                onPaymentFailed();
                break;
            case EvoPaymentActivity.PAYMENT_UNDETERMINED:
                onPaymentUndetermined();
                break;
            case EvoPaymentActivity.PAYMENT_SESSION_EXPIRED:
                onSessionExpired();
                break;
        }
    }
}
```