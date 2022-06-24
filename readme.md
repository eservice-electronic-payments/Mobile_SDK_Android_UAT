## Pre-condition

Please note that the call from the Merchant Server to the IPG Gateway API for obtaining an IPG Gateway “Session Token” has been updated. (This update is due to additional data fields mandated by 3DS-Version 2 compliance). It is a prerequisite that the Session Token passed to the Mobile SDK is obtained from that updated Get Session Token API endpoint.

## Installation

#### Manually (Recommended)

1. Clone this project or download zip file with source code, by clicking green button in top right corner:

![Download Button](readMeImages/cloneOrDownload.PNG)

2. If zip file was downloaded, extract it.
3. Open extracted or cloned folder, and run command in terminal:
```
git checkout <version>
```
Where `<version>` is version of SDK you want to use, e.g. `git checkout 1.0`.
All available version you can find [here](https://github.com/eservice-electronic-payments/Mobile_SDK_Android_UAT/releases)

4. Open your project in Android Studio.
5. Open file `settings.gradle` - it is in your project's root directory and add this line:
```groovy
include ':sdk', ':nsoft-libs'
```
6. In app-level build.gradle file add the dependency:
```groovy
implementation project(":sdk")
```
7. Make sure the integration mode is switched from **JitPack**(default) to **Manually**, open file `build.gradle` in sdk module: 
   1. comment out these two plugins: 
      1. `apply plugin: 'maven-publish'` 
      2. `apply plugin: 'com.kezong.fat-aar'`
   2. comment out this `afterEvaluate` block:
      ```groovy
       afterEvaluate {
           publishing {
               publications {
                   release(MavenPublication) {
                       from components.release
                       groupId = 'com.evopayments.sdk'
                       artifactId = 'uat'
                       version = '2.1.1'
                   }
               }
          }
       }
      ```
   3. comment out this line: `embed project(path: ':nsoft-libs', configuration: 'default')`
   4. uncomment this line: `// implementation project(":nsoft-libs")`


#### JitPack (Not Recommended)

1. Add the JitPack repository in your root build.gradle at the end of repositories:

```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
2. Add the dependency of mobile sdk via Repo and Tag:

```groovy
dependencies {
   	implementation 'com.github.eservice-electronic-payments:<repo>:<tag>'
}
```

Where `<repo>` is the repository name: `Mobile_SDK_Android_UAT`; `<tag>` is the version of SDK you want to use. Here is an example: 
```implementation 'com.github.eservice-electronic-payments:Mobile_SDK_Android_UAT:2.0.4'```

## Usage

1. To use Android SDK it's necessary to fetch mobile cashier url and token from API:
​
We use http get method to obtain session token.
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
    merchantLandingPageUrl: String,
    language: String,       //"en"
    myriadFlowId: String,
    customerFirstName: String, // Jan
    customerLastName: String,  // Mobile
    merchantNotificationUrl: String,
    customerAddressStreet: String,
    customerAddressHouseName: String,
    customerAddressCity: String,
    customerAddressPostalCode: String,
    customerAddressCountry: String, // ISO code
    customerAddressState: String,	// ISO code
    customerPhone: String,
    customerEmail: String,
    customerIPAddress: String,
)
```

2. Once the cashier url and token are retrieved, call this method from your activity to display web page via SDK:
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
