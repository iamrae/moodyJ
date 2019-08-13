package com.example.raelee.moodyj;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class SignUpActivity extends Activity {

    private static final String TAG = SignUpActivity.class.getSimpleName();

    // 회원가입 화면
    // 스트리밍 페이지 누르기 > "로그인이 필요한 기능입니다" 메시지 띄운 후 로그인 화면으로 이동
    // 로그인 화면에서 계정이 없을 시 회원가입하기 버튼을 눌러 본 화면을 연다

    ImageView buttonSignUp; // 회원가입 이미지뷰
    EditText email, password, confirmpw; // 회원가입, 로그인 입력 부분

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 타이틀바 삭제
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // notification bar 삭제
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // title/notification bar 삭제 후 레이아웃 띄우기
        this.setContentView(R.layout.activity_sign_up);

        // FontAwesome 서체를 사용하여 아이콘 배치
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);

        buttonSignUp = findViewById(R.id.buttonSignUp);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmpw = findViewById(R.id.confirmpw);

        //회원가입하기 버튼 클릭 이벤트
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insert(v);
                Intent intent = new Intent(SignUpActivity.this, LogInActivity.class); // 내 음악 목록으로 가기
                startActivity(intent);
            }
        });

    }

    // 사용자가 입력한 값을 스트링으로 변환하여 변수에 담는다
    public void insert(View view) {
        Log.e(TAG, "회원가입 버튼을 누르면 insert 메소드가 실행된다");
        String emailIn = email.getText().toString();
        String passwordIn = password.getText().toString();
        String confirmpwIn = confirmpw.getText().toString();

        Log.e(TAG, "데이터베이스에 메일주소, 비밀번호(2)를 저장한다 " + emailIn + " " + passwordIn + " " + confirmpwIn);
        insertToDatabase(emailIn, passwordIn, confirmpwIn);

    }

    // DATABASE 에 이메일, 비밀번호, 재입력된 비밀번호를 저장한다
    private void insertToDatabase(String emailIn, String passwordIn, String confirmpwIn) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            // 실행 후에는 진행중이라는 다이얼로그 띄운다
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.e(TAG, "DB에 정보 입력하기 전 onPreExcute 메소드 실행");
                loading = ProgressDialog.show(SignUpActivity.this, "Please Wait", null, true, true);
            }

            // 실행 후에는 다이얼로그를 없애고 토스트메시지를 띄운다
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.e(TAG, "DB에 정보 입력한 후 onPostExecute 메소드 실행");
                loading.dismiss();
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                Log.e(TAG, "onPostExecute 토스트 메시지 " + s);
            }

            // 서버로 자료를 보낸다 (백그라운드에서 실행됨)
            @Override
            protected String doInBackground(String... params) {

                try {
                    String emailIn = (String) params[0];
                    String passwordIn = (String) params[1];
                    String confirmpwIn = (String) params[2];

                    Log.e(TAG, "doInBackground : Strings " + emailIn + " " + passwordIn + " " + confirmpwIn);

                    // url encoder 는 뭘 하는것일까
                    // String link = "http://ec2-54-180-82-222.ap-northeast-2.compute.amazonaws.com/Server/php_signup.php"; // 기존 서버
                    // String link = "http://ec2-54-180-85-211.ap-northeast-2.compute.amazonaws.com/Server/php_signup.php"; // 백업 서버
                    String link = "http://ec2-13-125-241-11.ap-northeast-2.compute.amazonaws.com/Server/php_signup.php"; // 수정 서버(190718)

                    String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(emailIn, "UTF-8");
                    data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(passwordIn, "UTF-8");
                    data += "&" + URLEncoder.encode("confirmPw", "UTF-8") + "=" + URLEncoder.encode(confirmpwIn, "UTF-8");

                    // url 연결하기
                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();
                    Log.e(TAG, "wr.write(data) where data is " + data);


                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response (서버의 응답을 읽어온다)
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        Log.e(TAG, "Reading Server Response : " + line);
                        break;
                    }
                    return sb.toString();
                } catch (Exception e) {
                    Log.e(TAG, "Exception e : " + e.getMessage());
                    return new String("Exception: " + e.getMessage());
                }
            }
        }

        InsertData task = new InsertData();
        Log.e(TAG, "Insert Data task");
        task.execute(emailIn, passwordIn, confirmpwIn);
        Log.e(TAG, "task.execute " + emailIn + " " + passwordIn + " " + confirmpwIn);
    }
}
