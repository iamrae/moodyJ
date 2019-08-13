package com.example.raelee.moodyj;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class LogInActivity extends Activity {

    private static final String TAG = LogInActivity.class.getSimpleName();

    // 로그인 화면
    // 스트리밍 페이지 누르기 > "로그인이 필요한 기능입니다" 메시지 띄운 후 본 화면으로 이동한다

    ImageView linkSignUp, buttonLogIn; // 회원가입, 로그인 이미지뷰 생성
    EditText email, password; // 회원가입, 로그인 입력 부분
    ViewFlipper ViewFlipper;
    HttpPost httpPost;
    StringBuffer stringBuffer;
    HttpResponse response;
    HttpClient httpClient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 타이틀바 삭제
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // notification bar 삭제
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // title/notification bar 삭제 후 레이아웃 띄우기
        this.setContentView(R.layout.activity_log_in);

        // FontAwesome 서체를 사용하여 아이콘 배치
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        linkSignUp = findViewById(R.id.linkSignUp);
        buttonLogIn = findViewById(R.id.buttonLogIn);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);


        //회원가입 페이지로 이동하는 버튼 이벤트
        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSign = new Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(intentSign);
            }
        });


        //로그인하기 버튼 이벤트
        buttonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(LogInActivity.this, "",
                        "Validating user...", true);
                new Thread(new Runnable() {
                    public void run() {
                        login();
                    }
                }).start();
            }
        });
    }

    // 로그인 버튼 누르면 서버로 자료 전송
    void login() {

        try {

            httpClient = new DefaultHttpClient();
            // httpPost = new HttpPost("http://ec2-54-180-82-222.ap-northeast-2.compute.amazonaws.com/Server/php_login.php"); // 기존 서버
            // httpPost = new HttpPost("http://ec2-54-180-85-211.ap-northeast-2.compute.amazonaws.com/Server/php_login.php"); // 백업 서버
            httpPost = new HttpPost("http://ec2-13-125-241-11.ap-northeast-2.compute.amazonaws.com/Server/php_login.php"); // 수정 서버(190718)

            nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("email", email.getText().toString()));
            nameValuePairs.add(new BasicNameValuePair("password", password.getText().toString()));

            Log.e(TAG, "nameValuePairs에 값 저장: 이메일 " + email.getText().toString()+ " 비밀번호 " + password.getText().toString());

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpClient.execute(httpPost);
            Log.e(TAG, "httpClient 에서 httpPost 방식으로 nameValuePairs 를 전송 " + nameValuePairs);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpClient.execute(httpPost, responseHandler);
            System.out.println("Response : " + response);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   // textView.setText("Response from PHP : " + response);
                    Log.e(TAG, "Response from PHP " + response);
                    dialog.dismiss();
                }
            });

            if (response.equalsIgnoreCase("User Found")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LogInActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                    }
                });

                // 로그인 성공 후 메인화면으로 이동한다
                Intent intent = new Intent(LogInActivity.this, StreamingMainActivity.class);
                intent.putExtra("email", email.getText().toString());
                // 로그인한 회원의 이메일 주소를 인텐트에 담아 전송
                startActivity(intent);

                finish();

            } else {
                Toast.makeText(LogInActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
            }


        } catch(Exception e) {
            dialog.dismiss();
            System.out.println("Exception : " + e.getMessage());
        }

    }


}