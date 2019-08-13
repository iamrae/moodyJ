package com.example.raelee.moodyj;

import android.app.VoiceInteractor;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class StreamingMainActivity extends AppCompatActivity implements StrRecyclerViewAdapter.ItemListener {

    // 로그인 후 본 페이지로 이동
    // 상단에는 스트리밍 시작 버튼 + 다른 사람의 방송을 볼 수 있는 링크들이 있고,
    // 하단에는 유료, 무료 회원 여부에 따라 각기 다른 페이지를 보여준다
    // 무료 회원의 경우 결제를 유도하는 메시지와 결제 연결 버튼을 표시하고,
    // 유료 회원의 경우 각 사용자에게 적합한 선곡 모음을 보여준다

    private static final String TAG = "StreamingMainActivity";
    private final String APP_SCHEME = "paymentdone://";

    ImageView buttonPay, buttonSync; // 구매하기, 동기화하기 버튼
    RecyclerView recyclerViewStreamer; // 스트리밍 중인 사용자 보여주는 리사이클러 뷰
    RecyclerView recyclerViewPaidUser; // 유료회원에게만 보여지는 추천 메뉴(리사이클러 뷰)
    LinearLayout layoutFreeUser; // 무료회원에게만 보여지는 레이아웃 (구매 버튼 포함)
    LinearLayout layoutPaidUserFirst; // 유료 + 동기화하기 전의 회원에게만 보여지는 레이아웃 (동기화 버튼 포함)

    String email; // 로그인한 사용자의 이메일 주소
    boolean isSynced; // 유료회원이 처음 결제 후 곡추천을 받기 위한 동기화여부를 확인하는 변수

    //ArrayList arrayListRec; // 추천 목록 담아주는 리스트;
    //ArrayList arrayListStreamer; // 스트리밍 유저 리스트;
    ArrayList<StreamerList> arrayListStreamer = new ArrayList<>();
    static ArrayList<RecommendedLists> arrayRecomList = new ArrayList<>();

    StrRecyclerViewAdapter adapterStreamer;
    RecRecyclerViewAdapter adapterRecom;
    Context context;

    JSONArray recSongs; // 추천받을 곡 목록

    LinearLayoutManager linearLayoutManager;
    //AudioServiceInterface mInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 타이틀바 삭제
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // notification bar 삭제
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // title/notification bar 삭제 후 레이아웃 띄우기
        this.setContentView(R.layout.activity_streaming_main);

        // FontAwesome 서체를 사용하여 아이콘 배치
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);

        // 로그인한 사용자의 이메일주소를 받아 변수에 저장
        Intent getEmail = getIntent();
        email = getEmail.getStringExtra("email");

        context = getApplicationContext();

        Log.e(TAG, "onCreate: signed in as " + email);

        buttonPay = findViewById(R.id.buttonPay);
        buttonSync = findViewById(R.id.buttonSync);

        // 유료회원인지 무료회원인지 서버에 요청하여 확인
        checkUser(email);

        // 유료/무료 회원 여부에 따라 다른 레이아웃 정의 및 연결
        layoutFreeUser = findViewById(R.id.view_freeuser);
        layoutPaidUserFirst = findViewById(R.id.view_paiduser_first);
        recyclerViewPaidUser = findViewById(R.id.recycler_view_paiduser);
        recyclerViewStreamer = findViewById(R.id.recycler_view_stream);

        // 리사이클러뷰 생성 후 리사이클러뷰에 올릴 추천 관련 데이터를 ArrayList에 채우기
        // createRecommendedList();

        // 스트리밍 목록 ArrayList에 채우기
        createStreamingList();

        //mInterface = new AudioServiceInterface(getApplicationContext());
    }

    private void createStreamingList() {
        // 임의로 값 넣어주고 추후 스트리밍 목록 받아서 넣어주자


        StreamerList streamerList = new StreamerList();
        streamerList.setUserId("iamrae112");
        streamerList.setThumbnail("album01.jpg");
        arrayListStreamer.add(streamerList);

        streamerList = new StreamerList();
        streamerList.setUserId("slee667");
        streamerList.setThumbnail("album02.jpg");
        arrayListStreamer.add(streamerList);

        streamerList = new StreamerList();
        streamerList.setUserId("moodyJ");
        streamerList.setThumbnail("album03.jpg");
        arrayListStreamer.add(streamerList);

        streamerList = new StreamerList();
        streamerList.setUserId("trySthNew");
        streamerList.setThumbnail("album04.jpg");
        arrayListStreamer.add(streamerList);

        streamerList = new StreamerList();
        streamerList.setUserId("streamer");
        streamerList.setThumbnail("album05.jpg");
        arrayListStreamer.add(streamerList);

        streamerList = new StreamerList();
        streamerList.setUserId("whyNot");
        streamerList.setThumbnail("album06.jpg");
        arrayListStreamer.add(streamerList);

        streamerList = new StreamerList();
        streamerList.setUserId("noMatterWhat");
        streamerList.setThumbnail("album07.jpg");
        arrayListStreamer.add(streamerList);

    }

    private void createRecommendedList(String title, String artist, String fileName) {

        RecommendedLists recomList = new RecommendedLists();
        recomList.setTitle(title);
        recomList.setArtist(artist);
        recomList.setFileName(fileName);
        arrayRecomList.add(recomList);

    }

    @Override
    protected void onStart(){
        super.onStart();

        Log.e(TAG, "onStart: recommended list 뜨기 전인가");
        adapterStreamer = new StrRecyclerViewAdapter(this, arrayListStreamer, this);
        recyclerViewStreamer.setAdapter(adapterStreamer);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewStreamer.setLayoutManager(linearLayoutManager);


        if (layoutFreeUser.getVisibility() == View.VISIBLE) {

            buttonPay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(StreamingMainActivity.this, PaymentActivity.class); // 내 음악 목록으로 가기
                    intent.putExtra("email", email);
                    startActivity(intent);
                }
            });
        }


/*
        if (!isSynced) {
            // 동기화하기 전이라면
            //layoutPaidUserFirst.setVisibility(View.VISIBLE);
            buttonSync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, "onStart()에서 sync버튼 눌림");
                    // 싱크버튼을 누르면 db에 값을 보내주어야 한다.
                    DBHelper dbHelper = new DBHelper(context, "MusicDescriptorTable.db", null, 1);

                    //dbHelper.getResult();
                    //dbHelper.getObject();
                    //Log.e(TAG, "show resultSet" + dbHelper.getResult());

                    JSONArray syncJson = dbHelper.getResult();
                    new SendDataToServer().execute(String.valueOf(syncJson));
                }
            });

        }*/


        /*
        // 유료회원 리사이클러뷰 어댑터와 데이터 정의 및 연결
        RecRecyclerViewAdapter adapterPaidUser = new RecRecyclerViewAdapter(this, arrayRecomList, this);
        Log.e(TAG, "onStart: layoutAdapter " + arrayRecomList);
        recyclerViewPaidUser.setAdapter(adapterPaidUser);*/

        /*
        AutoFitGridLayoutManager gridLayoutManager = new AutoFitGridLayoutManager(this, 500);
        recyclerViewPaidUser.setLayoutManager(gridLayoutManager);
        */

        /*
        // 추천곡 리사이클러뷰 어댑터와 데이터 정의 및 연결
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerViewPaidUser.setLayoutManager(gridLayoutManager);
        */

    }

    private void checkUser(String email) {
        class CheckUserTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                try {
                    String email = (String) params[0];

                    // 유료/무료 회원 여부를 확인하기 위한 페이지 URL과 넘겨줄 회원 아이디 지정
                    // String link = "http://ec2-54-180-82-222.ap-northeast-2.compute.amazonaws.com/Server/php_checkuserpaid.php"; // 기존 서버
                    // String link = "http://ec2-54-180-85-211.ap-northeast-2.compute.amazonaws.com/Server/php_checkuserpaid.php"; // 백업 서버
                    String link = "http://ec2-13-125-241-11.ap-northeast-2.compute.amazonaws.com/Server/php_checkuserpaid.php"; // 수정 서버(190718)
                    String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");

                    // URL 연결하기
                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

                    writer.write(data);
                    writer.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder builder = new StringBuilder();
                    String line = null;

                    // Read Server Response (서버의 응답을 읽어온다)
                    while ((line = reader.readLine()) != null){
                        builder.append(line);
                        Log.e(TAG, "Reading Server Response (email): " + line);
                        break;
                    }
                    return builder.toString();
                } catch (Exception e) {
                    Log.e(TAG, "Exception e : " + e.getMessage());
                    return new String("Exception: " + e.getMessage());
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                    Log.e(TAG, "유료회원 확인 후 :"+ s);
                //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
                if (s.equalsIgnoreCase("this is a paid account")) {
                    Log.e(TAG, "유료회원 확인 메시지를 받은 후 : "+ s);
                    if (!isSynced) {
                        layoutFreeUser.setVisibility(View.INVISIBLE);
                        layoutPaidUserFirst.setVisibility(View.VISIBLE);
                        buttonSync.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Log.e(TAG, "buttonSync is clicked + 메일 체크 후에 생긴 sync 버튼 누름");

                                // 싱크버튼을 누르면 db에 값을 보내주어야 한다.
                                DBHelper dbHelper = new DBHelper(context, "MusicDescriptorTable.db", null, 1);
                                //Log.e(TAG, "DBHelper?? :" + dbHelper.getResult());

                                // asyncTask 로 모든 곡 정보 동기화하기
                                JSONArray syncJson = dbHelper.getResult();
                                new SendDataToServer().execute(String.valueOf(syncJson)); // 이미 서버의 DB에 들어가 있으니 지금은 주석처리 해두자

/*
                                // volley 사용해서 한번에 하나씩만 보내는 방법
                                dbHelper.getObject();
                                Log.e(TAG, "show getObject" + dbHelper.getObject());
                                JSONObject jsonObject = dbHelper.getObject();
                                uploadJson(jsonObject);
*/
                                isSynced = true;
                            }
                        });

                    } else {
                        recyclerViewPaidUser.setVisibility(View.VISIBLE);
                        layoutFreeUser.setVisibility(View.INVISIBLE);
                    }
                }
                //tv_outPut.setText(s);
            }
        }

        // AsyncTask를 통해 HttpURLConnection 수행.
        CheckUserTask checkUserTask = new CheckUserTask();
        checkUserTask.execute(email);
    }

    // sqlite DB 에서 받아온 json 파일을 서버로 보내는 메소드 (called when sync button is clicked)
    class SendDataToServer extends AsyncTask <String,String,String> {

        @Override
        protected String doInBackground(String... params) {
            String JsonResponse = null;
            String JsonDATA = params[0];

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                Log.e(TAG, "SendDataToServer asyncTask 클래스 실행됨");
                // URL url = new URL("http://ec2-54-180-82-222.ap-northeast-2.compute.amazonaws.com/Server/php_syncDB.php"); // 기존 서버
                // URL url = new URL("http://ec2-54-180-85-211.ap-northeast-2.compute.amazonaws.com/Server/php_syncDB.php"); // 백업 서버
                URL url = new URL("http://ec2-13-125-241-11.ap-northeast-2.compute.amazonaws.com/Server/php_syncDB.php"); // 수정 서버(190718)
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                // is output buffer writter
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");

                //set headers and method
                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                writer.write(JsonDATA);

                // json data
                writer.close();
                InputStream inputStream = urlConnection.getInputStream();

                //input stream
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                StringBuilder builder = new StringBuilder();
                String line = null;

                // Read Server Response (서버의 응답을 읽어온다)
                while ((line = reader.readLine()) != null){
                    builder.append(line);
                    Log.e(TAG, "Reading Server Response after sending data : " + line);
                    break;
                }
                return builder.toString();

            } catch (IOException e) {
                e.printStackTrace();
                return new String("Exception: " + e.getMessage());
            }
        }

    @Override
        protected void onPostExecute(String s) {
        super.onPostExecute(s);

        // 서버에 음악 정보를 보내고 각 정보를 서버 DB에 담은 후 inserted/synced 메시지를 받는다.
        // 메시지 받은 후 동기화 버튼이 사라지고 음악 추천 목록을 받아온다.

        if (s.equalsIgnoreCase("Your data is properly inserted") || s.equalsIgnoreCase("Your data is already synced"))  {
            Log.e(TAG, "asyncTask로 데이터 넘기고 난 다음에 받는 메시지: " + s);


            Random random = new Random();
            int num = random.nextInt(150);

            // volley 를 사용해서 무작위로 1곡을 골라 해당 곡 정보를 보낸다.
            DBHelper dbHelper = new DBHelper(context, "MusicDescriptorTable.db", null, 1);
            dbHelper.getObject(num);
            Log.e(TAG, "show getObject " + num + ": " + dbHelper.getObject(num));
            JSONObject jsonObject = dbHelper.getObject(num);
            Log.e(TAG, "jsonObject: "+jsonObject);
            uploadSamples(jsonObject);

            layoutPaidUserFirst.setVisibility(View.INVISIBLE);
        }

    }

}

    /*
    @Override
    public void onItemClick(RecommendedLists item) {
        // 추천 목록 눌렀을 때 각 곡 재생하거나 pause 하거나 하도록.
        //Toast.makeText(getApplicationContext(), item.title + "is clicked", Toast.LENGTH_SHORT).show();
        //String url = "http://ec2-54-180-82-222.ap-northeast-2.compute.amazonaws.com/Music/Corinne Bailey Rae-1-Like A Star.mp3"; // your URL here // 기존 서버
        //String url = "http://ec2-54-180-85-211.ap-northeast-2.compute.amazonaws.com/Music/Corinne Bailey Rae-1-Like A Star.mp3"; // your URL here // 백업 서버

        boolean isPlaying = false;
        boolean isPrepared = false;
        MediaPlayer mediaPlayer = new MediaPlayer();

        if (!isPrepared) {

            String title = item.title;
            // String url = "http://ec2-54-180-82-222.ap-northeast-2.compute.amazonaws.com/Music/서교동의 밤-1-럭키스타 (Lucky Star) (Feat. 다원).mp3"; // your URL here // 기존 서버
            // String url = "http://ec2-54-180-85-211.ap-northeast-2.compute.amazonaws.com/Music/서교동의 밤-1-럭키스타 (Lucky Star) (Feat. 다원).mp3"; // your URL here // 백업 서버
            // String url = "http://ec2-13-125-241-11.ap-northeast-2.compute.amazonaws.com/Music/서교동의 밤-1-럭키스타 (Lucky Star) (Feat. 다원).mp3"; // 수정 서버(190718)

            String url = "http://ec2-13-125-241-11.ap-northeast-2.compute.amazonaws.com/Music/"; // 수정 서버(190718)
            String ext = ".jpg";

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {
                mediaPlayer.setDataSource(url+title+ext);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mediaPlayer.prepare(); // might take long! (for buffering, etc)
                //isPrepared = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            isPrepared = true;
        }
        if (!isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
        } else {
            mediaPlayer.pause();
            isPlaying = false;
        }

    } */

    @Override
    public void onItemClick(int position) {
        // 스트리밍 목록 눌렀을 때 각 스트리밍 재생 페이지로 넘어간다

        String userId = arrayListStreamer.get(position).getUserId();

        if (userId.equals("iamrae112")) {
            Intent toPlayer = new Intent(StreamingMainActivity.this, StreamerActivity.class);
            startActivity(toPlayer);
        } else {
            Intent toPlayer = new Intent(StreamingMainActivity.this, StreamingPlayerActivity.class);
            startActivity(toPlayer);
        }

    }

    // volley 사용해서 추천받을 샘플곡의 json 보내기 (https://stackoverflow.com/questions/46422727/how-to-send-json-array-as-post-request-in-volley 참고)
    public void uploadSamples(JSONObject jsonObject){
        RequestQueue queue = Volley.newRequestQueue(context);
        // String url = "http://ec2-54-180-82-222.ap-northeast-2.compute.amazonaws.com/Server/php_playlist.php"; // 기존 서버
        // String url = "http://ec2-54-180-85-211.ap-northeast-2.compute.amazonaws.com/Server/php_playlist.php"; // 백업 서버
        String url = "http://ec2-13-125-241-11.ap-northeast-2.compute.amazonaws.com/Server/php_playlist.php"; // 수정 서버(190718)
        Log.e(TAG, "uploadJson called " + url + " | " + jsonObject);

        // Create a json array
        JSONArray array = new JSONArray();

        // Create json objects
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("averageLoudness", jsonObject.get("averageLoudness"));
            jsonBody.put("dynamicComplexity", jsonObject.get("dynamicComplexity"));
            jsonBody.put("bpm", jsonObject.get("bpm"));
            jsonBody.put("danceability", jsonObject.get("danceability"));
            jsonBody.put("chordsChangesRate", jsonObject.get("chordsChangesRate"));
            jsonBody.put("chordsNumbersRate", jsonObject.get("chordsNumbersRate"));
            jsonBody.put("chordsKey", jsonObject.get("chordsKey"));
            jsonBody.put("chordsScale", jsonObject.get("chordsScale"));
            jsonBody.put("title", jsonObject.get("title"));
            jsonBody.put("artist", jsonObject.get("artist"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        array.put(jsonBody);

        JsonArrayRequest request_json = new JsonArrayRequest(Request.Method.POST, url, array, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //Toast.makeText(getApplicationContext(), "String Response :" + response.toString(), Toast.LENGTH_SHORT).show();

                // 유료회원 리사이클러뷰 어댑터와 데이터 정의 및 연결
                adapterRecom = new RecRecyclerViewAdapter(StreamingMainActivity.this, arrayRecomList);
                Log.e(TAG, "after getting JSONObject: layoutAdapter : " + adapterRecom + " lists :");


                // 서비스에 곡 목록 얹기
                MoodyJ.getInstance().getServiceInterface().setPlayList(arrayRecomList);


                // 추천곡 리사이클러뷰 어댑터와 데이터 정의 및 연결 --> list 에 곡목록 받고나서 레이아웃매니저가 뿌려줘야 하는건가?
                GridLayoutManager gridLayoutManager = new GridLayoutManager(StreamingMainActivity.this, 2, GridLayoutManager.VERTICAL, false);
                recyclerViewPaidUser.setLayoutManager(gridLayoutManager);
                recyclerViewPaidUser.setAdapter(adapterRecom);

                // 뷰 사이 간격 지정 (via ItemDecoration)
                adapterRecom.addItemDecoration(new SpaceItemDecoration(StreamingMainActivity.this));

                adapterRecom.setOnItemClickListener(new RecRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {

                        MoodyJ.getInstance().getServiceInterface().play(position);
                        Intent onclick = new Intent(StreamingMainActivity.this, RecomPlayerActivity.class);
                        onclick.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        onclick.putExtra("position", position);
                        startActivity(onclick);

                    }

                    @Override
                    public void onItemClick(RecommendedLists item) {

                        /* 클릭하자마자 바로 재생
                        boolean isPlaying = false;
                        boolean isPrepared = false;
                        MediaPlayer mediaPlayer = new MediaPlayer();

                        if (!isPrepared) {

                            String fileName = item.getFileName();
                            // String url = "http://ec2-54-180-82-222.ap-northeast-2.compute.amazonaws.com/Music/서교동의 밤-1-럭키스타 (Lucky Star) (Feat. 다원).mp3"; // your URL here // 기존 서버
                            // String url = "http://ec2-54-180-85-211.ap-northeast-2.compute.amazonaws.com/Music/서교동의 밤-1-럭키스타 (Lucky Star) (Feat. 다원).mp3"; // your URL here // 백업 서버
                            // String url = "http://ec2-13-125-241-11.ap-northeast-2.compute.amazonaws.com/Music/서교동의 밤-1-럭키스타 (Lucky Star) (Feat. 다원).mp3"; // 수정 서버(190718)

                            String url = "http://ec2-13-125-241-11.ap-northeast-2.compute.amazonaws.com/Music/"; // 수정 서버(190718)
                            String ext = ".mp3";

                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                            try {
                                mediaPlayer.setDataSource(url+fileName+ext);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                mediaPlayer.prepare(); // might take long! (for buffering, etc)
                                //isPrepared = true;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            isPrepared = true;
                        }
                        if (!isPlaying) {
                            mediaPlayer.start();
                            isPlaying = true;
                        } else {
                            mediaPlayer.pause();
                            isPlaying = false;
                        }*/

                    }

                });

                recyclerViewPaidUser.setVisibility(View.VISIBLE);



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), "Error getting response" + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting response" + error.getMessage());
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<String, String>();
            // Add headers
                headers.put("Content-Type", "application/json; charset=utf-8");
            return headers;
            }

        // Convert response to JSON Array Again
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                String responseString;
                JSONArray responseArray = new JSONArray();
                if (responseArray != null) {
                    try {
                        responseString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        JSONObject obj = new JSONObject(responseString);
                        (responseArray).put(obj);

                        // json으로 받아온 곡 목록(sent as "report" from php)을 리사이클러뷰 arraylist에 담기
                        recSongs = obj.getJSONArray("report");
                        Log.e(TAG, "recSongs.length() :" + recSongs.length());

                        for(int i = 0; i < recSongs.length(); i++){

                            // title, artist, fileName 받아와서 저장
                            JSONObject c = recSongs.getJSONObject(i);
                            String title = c.getString("title");
                            String artist = c.getString("artist");
                            String fileName = c.getString("fileName");

                            createRecommendedList(title, artist, fileName);

                        }


                    } catch(Exception ex) {

                    }
                }
                return Response.success(responseArray, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
            queue.add(request_json);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}