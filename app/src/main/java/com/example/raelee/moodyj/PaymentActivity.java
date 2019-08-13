package com.example.raelee.moodyj;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.net.URISyntaxException;

public class PaymentActivity extends AppCompatActivity {

    private WebView mainWebView;
    private final String APP_SCHEME = "iamporttest://";
    private static final String TAG = "PaymentActivity";
    String email;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Intent getEmail = getIntent();
        email = getEmail.getStringExtra("email");

        // 웹뷰 세팅하기
        mainWebView = (WebView) findViewById(R.id.mainWebView);
        mainWebView.setWebViewClient(new InicisWebViewClient(this));
        mainWebView.addJavascriptInterface(new WebAppInterface(this), "Android");

        // 자바스크립트 사용하기 위해서 websetting 생성
        WebSettings settings = mainWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        // 자바인터페이스를 통해 이메일 주소를 자바스크립트에 전달.

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(mainWebView, true);
        }

        Intent intent = getIntent();
        Uri intentData = intent.getData();

        // 보여질 페이지는 테스트결제 페이지
        if ( intentData == null ) {
            Log.e(TAG, "intentData == null");
            // mainWebView.loadUrl("http://ec2-54-180-82-222.ap-northeast-2.compute.amazonaws.com/Server/iamportpay.php?email="+email); // 기존 서버
            // mainWebView.loadUrl("http://ec2-54-180-85-211.ap-northeast-2.compute.amazonaws.com/Server/iamportpay.php?email="+email); // 백업 서버
            mainWebView.loadUrl("http://ec2-13-125-241-11.ap-northeast-2.compute.amazonaws.com/Server/iamportpay.php?email="+email); // 수정 서버 (190718)

        } else {
            //isp 인증 후 복귀했을 때 결제 후속조치
            String url = intentData.toString();
            if (url.startsWith(APP_SCHEME) ) {
                Log.e(TAG, "intentData == string url "+ url);
                String redirectURL = url.substring(APP_SCHEME.length()+3);
                Log.e(TAG, "redirectURL "+ redirectURL);
                mainWebView.loadUrl(redirectURL);
            }
        }
    }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String url = intent.toString();
        if ( url.startsWith(APP_SCHEME) ) {
            String redirectURL = url.substring(APP_SCHEME.length()+3);
            Log.e(TAG, "redirectURL @onNewIntent "+ redirectURL);
            mainWebView.loadUrl(redirectURL);
        }
    }

    public class InicisWebViewClient extends WebViewClient {
        // 웹뷰 안에 있는 하이퍼링크를 누를때 다른 브라우저에서 해당 링크를 열지 않고, 웹뷰 안에서 이동할 수 있도록 설정한다

        private Activity activity;

        public InicisWebViewClient(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:")) {
                Intent intent = null;

                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME); //IntentURI처리
                    Uri uri = Uri.parse(intent.getDataString());
                    Log.e(TAG, "shouldOverrideUrlLoading : " + uri);
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                } catch (URISyntaxException ex) {
                    return false;
                } catch (ActivityNotFoundException e) {
                    if ( intent == null )	return false;

                    if ( handleNotFoundPaymentScheme(intent.getScheme()) )	return true;

                    String packageName = intent.getPackage();
                    if (packageName != null) {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                        return true;
                    }

                    return false;
                }
            } else {
                    Intent sendEmail = new Intent(PaymentActivity.this, StreamingMainActivity.class);
                    sendEmail.putExtra("email", email);
                    startActivity(sendEmail);
                    finish();
                    //activity.startActivity(new Intent(PaymentActivity.this, StreamingMainActivity.class));
            }

            return false;
        }

        /**
         * @param scheme
         * @return 해당 scheme에 대해 처리를 직접 하는지 여부
         *
         * 결제를 위한 3rd-party 앱이 아직 설치되어있지 않아 ActivityNotFoundException이 발생하는 경우 처리합니다.
         * 여기서 handler되지않은 scheme에 대해서는 intent로부터 Package정보 추출이 가능하다면 다음에서 packageName으로 market이동합니다.
         *
         */
        protected boolean handleNotFoundPaymentScheme(String scheme) {
            //PG사에서 호출하는 url에 package정보가 없어 ActivityNotFoundException이 난 후 market 실행이 안되는 경우
            if ( PaymentScheme.ISP.equalsIgnoreCase(scheme) ) {
                Log.e(TAG, "url에 패키지 정보가 없어 마켓 실행이 안되는 경우 : 1 ISP"+ scheme );
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PaymentScheme.PACKAGE_ISP)));

                return true;
            } else if ( PaymentScheme.BANKPAY.equalsIgnoreCase(scheme) ) {
                Log.e(TAG, "url에 패키지 정보가 없어 마켓 실행이 안되는 경우 : 2 BANKPAY"+ scheme );
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PaymentScheme.PACKAGE_BANKPAY)));
                return true;
            }

            return false;
        }

    }
}