package com.example.raelee.moodyj;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import static com.example.raelee.moodyj.MainActivity.list;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelper";

    ArrayList<SortedMusicList> sortedMusicList = new ArrayList<>();
    MusicLibrary musicLibrary;

    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블 생성
        /* 이름은 MUSICDESCRIPTORTABLE이고, 자동으로 값이 증가하는 _id 정수형 기본키 컬럼과
        averageLoudness, dynamicComplexity, bpm, danceability,
        chordsChangesRate, chordsNumbersRate 더블 컬럼과
        chordsKey, chordsScale, title, artist 문자열 컬럼으로 구성된 테이블을 생성. */

        db.execSQL("CREATE TABLE MUSICDESCRIPTORTABLE (_id INTEGER PRIMARY KEY AUTOINCREMENT, averageLoudness DOUBLE, dynamicComplexity DOUBLE, " +
                "bpm DOUBLE, danceability DOUBLE, chordsChangesRate DOUBLE, chordsNumbersRate DOUBLE," +
                "chordsKey TEXT, chordsScale TEXT, title TEXT, artist TEXT);");


    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(double averageLoudness, double dynamicComplexity, double bpm, double danceability,
                       double chordsChangesRate, double chordsNumbersRate,
                       String chordsKey, String chordsScale, String title, String artist) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO MUSICDESCRIPTORTABLE VALUES(null, '" + averageLoudness + "', '" + dynamicComplexity + "', '" + bpm + "', '"
                + danceability + "', '" + chordsChangesRate + "', '" + chordsNumbersRate + "','" + chordsKey + "', '" + chordsScale + "', '"
                + title + "', '" + artist + "');");
        db.close();
    }

    public void delete(String title) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행 삭제
        db.execSQL("DELETE FROM MUSICDESCRIPTORTABLE WHERE title='" + title + "';");
        db.close();

    }

    // DB에 넣은 값을 json으로 변환해서 서버에 보내기 위한 메소드
    public JSONArray getResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        JSONArray result = new JSONArray();

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM MUSICDESCRIPTORTABLE", null);

        //
        while (cursor.moveToNext()) {
            JSONObject rowObject = new JSONObject();

            try {
                rowObject.put(cursor.getColumnName(0), cursor.getInt(0));
                rowObject.put(cursor.getColumnName(1), cursor.getDouble(1));
                rowObject.put(cursor.getColumnName(2), cursor.getDouble(2));
                rowObject.put(cursor.getColumnName(3), cursor.getDouble(3));
                rowObject.put(cursor.getColumnName(4), cursor.getDouble(4));
                rowObject.put(cursor.getColumnName(5), cursor.getDouble(5));
                rowObject.put(cursor.getColumnName(6), cursor.getDouble(6));
                rowObject.put(cursor.getColumnName(7), cursor.getString(7));
                rowObject.put(cursor.getColumnName(8), cursor.getString(8));
                rowObject.put(cursor.getColumnName(9), cursor.getString(9));
                rowObject.put(cursor.getColumnName(10), cursor.getString(10));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            result.put(rowObject);
        }

        cursor.close();
        db.close();
        return result;
    }

    // DB에 넣은 값을 json Object 로 변환해서 서버에 보내기 위한 메소드
    public JSONObject getObject(int random) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        int select = random;

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM MUSICDESCRIPTORTABLE WHERE _id='" + select + "';", null);

        JSONObject rowObject = new JSONObject();
        //
        while (cursor.moveToNext()) {

            for (int i = 1; i <= 10; i++){

                try {
                    if (i < 7) {
                        rowObject.put(cursor.getColumnName(i), cursor.getDouble(i));
                    } else {
                        rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();
        db.close();
        return rowObject;
    }


    // averageLoudness 최대값 구하기
    public double getALMax() {
        SQLiteDatabase db = getReadableDatabase();
        double max = 0;

        // Cursor로 맥스값 추출할 데이터 출력
        String sql = "SELECT max(averageLoudness) FROM MUSICDESCRIPTORTABLE";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null) {
            cursor.moveToNext();
            Log.e(TAG, "cursor: " + cursor);
            max = cursor.getDouble(0);
        }

        cursor.close();
        db.close();
        return max;
    }

    // averageLoudness 최소값 구하기
    public double getALMin() {
        SQLiteDatabase db = getReadableDatabase();
        double min = 0;

        // Cursor로 미니멈값 추출할 데이터 출력
        String sql = "SELECT min(averageLoudness) FROM MUSICDESCRIPTORTABLE";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null) {
            cursor.moveToNext();
            min = cursor.getDouble(0);
        }

        db.close();
        return min;
    }

    // dynamicComplexity 최대값 구하기
    public double getDCMax() {
        SQLiteDatabase db = getReadableDatabase();
        double max = 0;

        // Cursor로 맥스값 추출할 데이터 출력
        String sql = "SELECT max(dynamicComplexity) FROM MUSICDESCRIPTORTABLE";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null) {
            cursor.moveToNext();
            Log.e(TAG, "cursor: " + cursor);
            max = cursor.getDouble(0);
        }

        cursor.close();
        db.close();
        return max;
    }

    // dynamicComplexity 최소값 구하기
    public double getDCMin() {
        SQLiteDatabase db = getReadableDatabase();
        double min = 0;

        // Cursor로 미니멈값 추출할 데이터 출력
        String sql = "SELECT min(dynamicComplexity) FROM MUSICDESCRIPTORTABLE";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null) {
            cursor.moveToNext();
            min = cursor.getDouble(0);
        }

        db.close();
        return min;
    }

    // bpm 최대값 구하기
    public double getBpmMax() {
        SQLiteDatabase db = getReadableDatabase();
        double max = 0;

        // Cursor로 맥스값 추출할 데이터 출력
        String sql = "SELECT max(bpm) FROM MUSICDESCRIPTORTABLE";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null) {
            cursor.moveToNext();
            max = cursor.getDouble(0);
        }

        db.close();
        return max;
    }

    // bpm 최소값 구하기
    public double getBpmMin() {
        SQLiteDatabase db = getReadableDatabase();
        double min = 0;

        // Cursor로 미니멈값 추출할 데이터 출력
        String sql = "SELECT min(bpm) FROM MUSICDESCRIPTORTABLE";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null) {
            cursor.moveToNext();
            min = cursor.getDouble(0);
        }

        db.close();
        return min;
    }

    // Danceability 최대값 구하기
    public double getDanMax() {
        SQLiteDatabase db = getReadableDatabase();
        double max = 0;

        // Cursor로 맥스값 추출할 데이터 출력
        String sql = "SELECT max(danceability) FROM MUSICDESCRIPTORTABLE";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null) {
            cursor.moveToNext();
            max = cursor.getDouble(0);
        }

        db.close();
        return max;
    }

    // Danceability 최소값 구하기
    public double getDanMin() {
        SQLiteDatabase db = getReadableDatabase();
        double min = 0;

        // Cursor로 미니멈값 추출할 데이터 출력
        String sql = "SELECT min(danceability) FROM MUSICDESCRIPTORTABLE";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null) {
            cursor.moveToNext();
            min = cursor.getDouble(0);
        }

        db.close();
        return min;
    }

    // chordsChangesRate 최대값 구하기
    public double getCcrMax() {
        SQLiteDatabase db = getReadableDatabase();
        double max = 0;

        // Cursor로 맥스값 추출할 데이터 출력
        String sql = "SELECT max(chordsChangesRate) FROM MUSICDESCRIPTORTABLE";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null) {
            cursor.moveToNext();
            max = cursor.getDouble(0);
        }

        db.close();
        return max;
    }

    // chordsChangesRate 최소값 구하기
    public double getCcrMin() {
        SQLiteDatabase db = getReadableDatabase();
        double min = 0;

        // Cursor로 미니멈값 추출할 데이터 출력
        String sql = "SELECT min(chordsChangesRate) FROM MUSICDESCRIPTORTABLE";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null) {
            cursor.moveToNext();
            min = cursor.getDouble(0);
        }

        db.close();
        return min;
    }

    // chordsNumbersRate 최대값 구하기
    public double getCnrMax() {
        SQLiteDatabase db = getReadableDatabase();
        double max = 0;

        // Cursor로 맥스값 추출할 데이터 출력
        String sql = "SELECT max(chordsNumbersRate) FROM MUSICDESCRIPTORTABLE";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null) {
            cursor.moveToNext();
            max = cursor.getDouble(0);
        }

        db.close();
        return max;
    }

    // chordsNumbersRate 최소값 구하기
    public double getCnrMin() {
        SQLiteDatabase db = getReadableDatabase();
        double min = 0;

        // Cursor로 미니멈값 추출할 데이터 출력
        String sql = "SELECT min(chordsNumbersRate) FROM MUSICDESCRIPTORTABLE";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null) {
            cursor.moveToNext();
            min = cursor.getDouble(0);
        }

        db.close();
        return min;
    }

    // normalize 계산을 위해 average Loudness 값 내보내기
    public double getAverageLoudness(int id) {
        SQLiteDatabase db = getWritableDatabase();

        double averageLoudness = 0;

        Cursor cursor = db.rawQuery("SELECT averageLoudness FROM MUSICDESCRIPTORTABLE WHERE _id='" + id + "';", null);

        if (cursor != null) {
            cursor.moveToNext();
            averageLoudness = cursor.getDouble(0);
        }
        db.close();
        return averageLoudness;
    }

    // normalize 계산을 위해 dynamicComplexity 값 내보내기
    public double getDynamicComplexity(int id) {
        SQLiteDatabase db = getWritableDatabase();

        double dynamicComplexity = 0;

        Cursor cursor = db.rawQuery("SELECT dynamicComplexity FROM MUSICDESCRIPTORTABLE WHERE _id='" + id + "';", null);

        if (cursor != null) {
            cursor.moveToNext();
            dynamicComplexity = cursor.getDouble(0);
        }
        db.close();
        return dynamicComplexity;
    }

    // normalize 계산을 위해 bpm 값 내보내기
    public double getBpm(int id) {
        SQLiteDatabase db = getWritableDatabase();

        double bpm = 0;

        Cursor cursor = db.rawQuery("SELECT bpm FROM MUSICDESCRIPTORTABLE WHERE _id='" + id + "';", null);

        if (cursor != null) {
            cursor.moveToNext();
            bpm = cursor.getDouble(0);
        }
        db.close();
        return bpm;
    }

    // normalize 계산을 위해 danceability 값 내보내기
    public double getDanceability(int id) {
        SQLiteDatabase db = getWritableDatabase();

        double danceability = 0;

        Cursor cursor = db.rawQuery("SELECT danceability FROM MUSICDESCRIPTORTABLE WHERE _id='" + id + "';", null);

        if (cursor != null) {
            cursor.moveToNext();
            danceability = cursor.getDouble(0);
        }
        db.close();
        return danceability;
    }

    // normalize 계산을 위해 chordsChangesRate 값 내보내기
    public double getChordsChangesRate(int id) {
        SQLiteDatabase db = getWritableDatabase();

        double chordsChangesRate = 0;

        Cursor cursor = db.rawQuery("SELECT chordsChangesRate FROM MUSICDESCRIPTORTABLE WHERE _id='" + id + "';", null);

        if (cursor != null) {
            cursor.moveToNext();
            chordsChangesRate = cursor.getDouble(0);
        }
        db.close();
        return chordsChangesRate;
    }

    // normalize 계산을 위해 chordsNumbersRate 값 내보내기
    public double getChordsNumbersRate(int id) {
        SQLiteDatabase db = getWritableDatabase();

        double chordsNumbersRate = 0;

        Cursor cursor = db.rawQuery("SELECT chordsNumbersRate FROM MUSICDESCRIPTORTABLE WHERE _id='" + id + "';", null);

        if (cursor != null) {
            cursor.moveToNext();
            chordsNumbersRate = cursor.getDouble(0);
        }
        db.close();
        return chordsNumbersRate;
    }

    // String 값 (key) 내보내기
    public String getKey(int id) {
        SQLiteDatabase db = getWritableDatabase();

        String key = "";

        Cursor cursor = db.rawQuery("SELECT chordsKey FROM MUSICDESCRIPTORTABLE WHERE _id='" + id + "';", null);

        if (cursor != null) {
            cursor.moveToNext();
            key = cursor.getString(0);
        }
        db.close();
        return key;
    }

    // String 값 (scale) 내보내기
    public String getScale(int id) {
        SQLiteDatabase db = getWritableDatabase();

        String scale = "";

        Cursor cursor = db.rawQuery("SELECT chordsScale FROM MUSICDESCRIPTORTABLE WHERE _id='" + id + "';", null);

        if (cursor != null) {
            cursor.moveToNext();
            scale = cursor.getString(0);
        }
        db.close();
        return scale;
    }

    // String 값 (title) 내보내기
    public String getTitle(int id) {
        SQLiteDatabase db = getWritableDatabase();

        String title = "";

        Cursor cursor = db.rawQuery("SELECT title FROM MUSICDESCRIPTORTABLE WHERE _id='" + id + "';", null);

        if (cursor != null) {
            cursor.moveToNext();
            title = cursor.getString(0);
        }
        db.close();
        return title;
    }

    // String 값 (artist) 내보내기
    public String getArtist(int id) {
        SQLiteDatabase db = getWritableDatabase();

        String artist = "";

        Cursor cursor = db.rawQuery("SELECT artist FROM MUSICDESCRIPTORTABLE WHERE _id='" + id + "';", null);

        if (cursor != null) {
            cursor.moveToNext();
            artist = cursor.getString(0);
        }
        db.close();
        return artist;
    }

    // 사용자가 seekBar를 조정하고 sort 버튼을 누르면 해당 메소드가 실행된다
    public void sortByKeyScale(Context context, long happy, long sensual, long tempo) {

        Log.e(TAG, "sortByKeyScale: started " + happy);

        // seekBar에서 받아온 값 저장하기
        long happyR = happy;
        long sensualR = sensual;
        long tempoR = tempo;
        Context c = context;

        // Key/Scale 로 값 걸러내기 위한 조건
        String scale, key1, key2, key3, key4, key5, key6;
        if (happyR > 50) {
            // happy 의 값이 50보다 클 때 : 우울한 >> 단조 곡만 추린다
            scale = "'minor'";

            if (sensualR > 50) {
                // sensual 의 값이 50보다 클 때 : 거친 >> 조성이 복잡하지 않는 곡만 추리기
                // key = "E or B or F# or C# or D or A";
                key1 = "'E'";
                key2 = "'B'";
                key3 = "'F#'";
                key4 = "'C#'";
                key5 = "'A#'";
                key6 = "'F'";
            } else {
                // sensual 의 값이 50보다 작거나 같을 때 : 부드러운 >> 조성이 복잡한 곡 추리기
                // key = "G# or D# or A# or F or C or G";
                key1 = "'G#'";
                key2 = "'D#'";
                key3 = "'A#'";
                key4 = "'F'";
                key5 = "'C'";
                key6 = "'G'";
            }
        } else {
            // happy 의 값이 50보다 작거나 같을 때 : 신나는 >> 장조 곡만 추린다
            scale = "'major'";

            if (sensualR > 50) {
                // sensual 의 값이 50보다 클 때 : 거친 >> 조성이 복잡하지 않는 곡만 추리기
                // key = "C or G or D or A or A# or F";
                key1 = "'C'";
                key2 = "'G'";
                key3 = "'D'";
                key4 = "'A'";
                key5 = "'A#'";
                key6 = "'F'";
            } else {
                // sensual 의 값이 50보다 작거나 같을 때 : 부드러운 >> 조성이 복잡한 곡 추리기
                //  key = "E or B or F# or C# or G# or D#";
                key1 = "'E'";
                key2 = "'B'";
                key3 = "'F#'";
                key4 = "'C#'";
                key5 = "'G#'";
                key6 = "'D#'";
            }
        }


        // Key/Scale 로 db에서 값 추려내기
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM MUSICDESCRIPTORTABLE WHERE chordsScale= " + scale + " AND chordsKey= " + key1 + " OR chordsKey= " + key2 +
                " OR chordsKey= " + key3 + " OR chordsKey= " + key4 + " OR chordsKey= " + key5 + " OR chordsKey= " + key6 + ";";

        ArrayList<SortedList> sortedList = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {

            //Log.e(TAG, "sortByKeyScale: while cursor.moveToNext");
            SortedList mList = new SortedList();

            mList.setAverageLoudness(cursor.getDouble(1));
            mList.setDynamicComplexity(cursor.getDouble(2));
            mList.setBpm(cursor.getDouble(3));
            mList.setDanceability(cursor.getDouble(4));
            mList.setChordsChangesRate(cursor.getDouble(5));
            mList.setChordsNumbersRate(cursor.getDouble(6));
            //String chordsKey = cursor.getString(7);
            //String chordsScale = cursor.getString(8);
            mList.setTitle(cursor.getString(9));
            mList.setArtist(cursor.getString(10));

            sortedList.add(mList);
            Log.e(TAG, "sortByKeyScale: sortedList" + mList.getArtist());
        }

        cursor.close();

        // sorted 곡의 세부 항목을 arrayList에 담기

        ArrayList alArray = new ArrayList();
        ArrayList dcArray = new ArrayList();
        ArrayList bpmArray = new ArrayList();
        ArrayList danArray = new ArrayList();
        ArrayList ccrArray = new ArrayList();
        ArrayList cnrArray = new ArrayList();

        for (int i = 0; i < sortedList.size(); i++) {
            alArray.add(sortedList.get(i).getAverageLoudness());
            dcArray.add(sortedList.get(i).getDynamicComplexity());
            bpmArray.add(sortedList.get(i).getBpm());
            danArray.add(sortedList.get(i).getDanceability());
            ccrArray.add(sortedList.get(i).getChordsChangesRate());
            cnrArray.add(sortedList.get(i).getChordsNumbersRate());
            //Log.e(TAG, "alArray " + alArray);

        }
/*
        // 각 항목의 max/min 값을 구하는 방법
        Object max = Collections.max(alArray);
        Object min = Collections.min(alArray);
        Log.e(TAG, "max and min of alArray " + max + " / " + min);*/


        // max/min 값 리스트에 담기
        SortedMaxValues maxValues = new SortedMaxValues();
        maxValues.setAlMax((double) Collections.max(alArray));
        maxValues.setAlMin((double) Collections.min(alArray));
        maxValues.setDcMax((double) Collections.max(dcArray));
        maxValues.setDcMin((double) Collections.min(dcArray));
        maxValues.setBpmMax((double) Collections.max(bpmArray));
        maxValues.setBpmMin((double) Collections.min(bpmArray));
        maxValues.setDanMax((double) Collections.max(danArray));
        maxValues.setDanMin((double) Collections.min(danArray));
        maxValues.setCcrMax((double) Collections.max(ccrArray));
        maxValues.setCcrMin((double) Collections.min(ccrArray));
        maxValues.setCnrMax((double) Collections.max(cnrArray));
        maxValues.setCnrMin((double) Collections.min(cnrArray));

        //Log.e(TAG, "getAlMax2 " + maxValues.getAlMax());


        // 정규화된 값을 담는 arrayList
        ArrayList<NormalizedList> normalizedLists = new ArrayList<>();

        double rawAL, rawDC, rawBPM, rawDan, rawCcr, rawCnr;
        double normAL, normDC, normBPM, normDan, normCcr, normCnr;

        for (int i = 0; i < sortedList.size(); i++) {

            NormalizedList normList = new NormalizedList();

            rawAL = sortedList.get(i).averageLoudness; // 대입되어야 할 average Loudness 값
            normAL = ((rawAL - maxValues.getAlMin()) / (maxValues.getAlMax() - maxValues.getAlMin()));
            normList.setNormAL(normAL);

            rawDC = sortedList.get(i).dynamicComplexity;
            normDC = ((rawDC - maxValues.getDcMin()) / (maxValues.getDcMax() - maxValues.getDcMin()));
            normList.setNormDC(normDC);

            rawBPM = sortedList.get(i).bpm;
            normBPM = ((rawBPM - maxValues.getBpmMin()) / (maxValues.getBpmMax() - maxValues.getBpmMin()));
            normList.setNormBPM(normBPM);

            rawDan = sortedList.get(i).danceability;
            normDan = ((rawDan - maxValues.getDanMin()) / (maxValues.getDanMax() - maxValues.getDanMin()));
            normList.setNormDan(normDan);

            rawCcr = sortedList.get(i).chordsChangesRate;
            normCcr = ((rawCcr - maxValues.getCcrMin()) / (maxValues.getCcrMax() - maxValues.getCcrMin()));
            normList.setNormCcr(normCcr);

            rawCnr = sortedList.get(i).chordsNumbersRate;
            normCnr = ((rawCnr - maxValues.getCnrMin()) / (maxValues.getCnrMax() - maxValues.getCnrMin()));
            normList.setNormCnr(normCnr);
            //Log.e(TAG, " " + normAL);

            normalizedLists.add(normList);

        }

        // 정규화된 값을 다 담고 나서 이제 각 받아온 값을 seekBar 에서 받아 수치랑 비교해준다.
        // seekBar 에서 받아온 수치를 담을 변수 지정
        double seekAL, seekDC, seekBPM, seekDan, seekCcr, seekCnr;
        seekAL = (double) (1 - (happyR / 100.0));
        seekDan = (double) (1 - (happyR / 100.0));
        seekBPM = (double) ((tempoR / 100.0));
        seekDC = (double) (1 - (sensualR / 100.0));
        seekCcr = (double) (1 - (sensualR / 100.0));
        seekCnr = (double) (1 - (sensualR / 100.0));

        Log.e(TAG, "seekBar에서 받아온 값 : " + happyR + " | " + sensualR + " | " + tempoR);
        Log.e(TAG, "seekBar에서 받아서 계산한 값 : " + seekAL + " | " + seekDC + " | " + seekBPM + " | " + seekDan + " | " + seekCcr + " | " + seekCnr);


/*
        // 거리값 계산할 벡터 생성
        double[] seekArray = {seekAL, seekDC, seekBPM, seekDan, seekCcr, seekCnr};
        double[] sortArray = {normalizedLists.get(i).getNormAL(), normalizedLists.get(i).getNormDC(), normalizedLists.get(i).getNormBPM(),
                                normalizedLists.get(i).getNormDan(), normalizedLists.get(i).getNormCcr(), normalizedLists.get(i).getNormCnr()};
*/

        // 거리값 계산하기
        ArrayList<DistanceList> distanceLists = new ArrayList<>();

        for (int i = 0; i < sortedList.size(); i++) {

            DistanceList distList = new DistanceList();

            double dist = Math.pow(seekAL - normalizedLists.get(i).getNormAL(), 2) + Math.pow(seekDC - normalizedLists.get(i).getNormDC(), 2) +
                    Math.pow(seekBPM - normalizedLists.get(i).getNormBPM(), 2) + Math.pow(seekDan - normalizedLists.get(i).getNormDan(), 2) +
                    Math.pow(seekCcr - normalizedLists.get(i).getNormCcr(), 2) + Math.pow(seekCnr - normalizedLists.get(i).getNormCnr(), 2);
            double result = Math.sqrt(dist);

            //Log.e(TAG, "거리값 계산하기 : " + result);

            distList.setDistance(result);
            distList.setTitle(sortedList.get(i).getTitle());
            distList.setArtist(sortedList.get(i).getArtist());
            distanceLists.add(distList);
        }

        Log.e(TAG, "distanceLists에 값이 들어갔는지 확인: " + distanceLists.get(2).getTitle() + " | " + distanceLists.get(3).getTitle());

        // 거리값이 담긴 arrayList 크기 순으로 정렬하기
        Collections.sort(distanceLists, Collections.reverseOrder());

        /*
        // 모든 목록 다 담는 코드
        for (DistanceList distCheck : distanceLists){
            //Log.e(TAG, "is it sorted?? " + distCheck.getDistance() + " | " + distCheck.getTitle() + " | " + distCheck.getArtist());
            //String [] titleArray = {distCheck.getTitle()};
            sortMusicList(c, distCheck.getTitle());
        }
        */

        for (int i = 0; i < 10; i++) {
            sortMusicList(c, distanceLists.get(i).getTitle());
            //sortMusicLibrary(c, distanceLists.get(i).getTitle());
        }

        Log.e(TAG, "상위 10곡의 제목이 담겼는지 확인 "+distanceLists.get(5).getTitle() );

    }

    // 분류된 곡을 담아 RecyclerView 에 띄우는 리스트 생성하기
    public void sortMusicList(Context context, String title){

        String searchTitle = title;
        Context c = context;
        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME};

        //String selection = MediaStore.Audio.Media.IS_MUSIC;
        String selection = MediaStore.Audio.Media.TITLE + " = '"+ searchTitle + "'";

        Cursor cursor = c.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);

        while (cursor.moveToNext()) {

            SortedMusicList mList = new SortedMusicList();
            // 단말기에 있는 음악 목록이 담기는 공간
            mList.setSongId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));// 곡의 아이디값
            //Log.e(TAG, "getting music from MediaStore."); // "로그 : mediaStore 에서 음악 가져오기"
            mList.setAlbumId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))); // 곡이 들어있는 앨범의 아이디값
            mList.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))); // 곡 제목
            mList.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))); // 아티스트
            mList.setDuration(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))); // 곡의 길이
            mList.setData(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            mList.setDisplayName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));

            //데이터를 담는다.
            sortedMusicList.add(mList);
        }
        cursor.close();
        //Log.e(TAG, "SortedMusic from MediaStore is as follows:" + sortedMusicList);
    }

    // 분류된 곡을 담아서 MediaController의 queueItem에 담기
    public void sortMusicLibrary(Context context, String title){

        String searchTitle = title;
        Context c = context;
        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Albums._ID};


        String selection = MediaStore.Audio.Media.TITLE + " = '"+ searchTitle + "'";
        Cursor cursor = c.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);

        while (cursor.moveToNext()) {

            String mediaId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String mediaTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            long duration = Long.parseLong(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
            TimeUnit durationUnit = TimeUnit.SECONDS;
            String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            String albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums._ID));

            musicLibrary.createMediaMetadataCompat(mediaId, mediaTitle, artist, album, duration, durationUnit, fileName, data, albumId);


        }

        cursor.close();
    }

    // Key/Scale 로 걸러낸 곡을 1차로 담아낼 ArrayList
    public class SortedList implements Serializable {
        private double averageLoudness;
        private double dynamicComplexity;
        private double bpm;
        private double danceability;
        private double chordsChangesRate;
        private double chordsNumbersRate;
        private String title;
        private String artist;

        public double getAverageLoudness() {
            return averageLoudness;
        }

        public void setAverageLoudness(double averageLoudness) {
            this.averageLoudness = averageLoudness;
        }

        public double getDynamicComplexity() {
            return dynamicComplexity;
        }

        public void setDynamicComplexity(double dynamicComplexity) {
            this.dynamicComplexity = dynamicComplexity;
        }

        public double getBpm() {
            return bpm;
        }

        public void setBpm(double bpm) {
            this.bpm = bpm;
        }

        public double getDanceability() {
            return danceability;
        }

        public void setDanceability(double danceability) {
            this.danceability = danceability;
        }

        public double getChordsChangesRate() {
            return chordsChangesRate;
        }

        public void setChordsChangesRate(double chordsChangesRate) {
            this.chordsChangesRate = chordsChangesRate;
        }

        public double getChordsNumbersRate() {
            return chordsNumbersRate;
        }

        public void setChordsNumbersRate(double chordsNumbersRate) {
            this.chordsNumbersRate = chordsNumbersRate;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getArtist() {
            return artist;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

    }

    // Key/Scale 로 걸러낸 곡 들의 max/min 값을 담을 ArrayList
    public class SortedMaxValues implements Serializable {
        private double alMax, alMin, dcMax, dcMin, bpmMax, bpmMin, danMax,
                danMin, ccrMax, ccrMin, cnrMax, cnrMin;

        public double getAlMax() {
            return alMax;
        }

        public void setAlMax(double alMax) {
            this.alMax = alMax;
        }

        public double getAlMin() {
            return alMin;
        }

        public void setAlMin(double alMin) {
            this.alMin = alMin;
        }

        public double getDcMax() {
            return dcMax;
        }

        public void setDcMax(double dcMax) {
            this.dcMax = dcMax;
        }

        public double getDcMin() {
            return dcMin;
        }

        public void setDcMin(double dcMin) {
            this.dcMin = dcMin;
        }

        public double getBpmMax() {
            return bpmMax;
        }

        public void setBpmMax(double bpmMax) {
            this.bpmMax = bpmMax;
        }

        public double getBpmMin() {
            return bpmMin;
        }

        public void setBpmMin(double bpmMin) {
            this.bpmMin = bpmMin;
        }

        public double getDanMax() {
            return danMax;
        }

        public void setDanMax(double danMax) {
            this.danMax = danMax;
        }

        public double getDanMin() {
            return danMin;
        }

        public void setDanMin(double danMin) {
            this.danMin = danMin;
        }

        public double getCcrMax() {
            return ccrMax;
        }

        public void setCcrMax(double ccrMax) {
            this.ccrMax = ccrMax;
        }

        public double getCcrMin() {
            return ccrMin;
        }

        public void setCcrMin(double ccrMin) {
            this.ccrMin = ccrMin;
        }

        public double getCnrMax() {
            return cnrMax;
        }

        public void setCnrMax(double cnrMax) {
            this.cnrMax = cnrMax;
        }

        public double getCnrMin() {
            return cnrMin;
        }

        public void setCnrMin(double cnrMin) {
            this.cnrMin = cnrMin;
        }

    }

    // max/min 으로 계산한 정규화 값을 담을 ArrayList
    public class NormalizedList implements Serializable {
        private double normAL, normDC, normBPM, normDan, normCcr, normCnr;

        public double getNormAL() {
            return normAL;
        }

        public void setNormAL(double normAL) {
            this.normAL = normAL;
        }

        public double getNormDC() {
            return normDC;
        }

        public void setNormDC(double normDC) {
            this.normDC = normDC;
        }

        public double getNormBPM() {
            return normBPM;
        }

        public void setNormBPM(double normBPM) {
            this.normBPM = normBPM;
        }

        public double getNormDan() {
            return normDan;
        }

        public void setNormDan(double normDan) {
            this.normDan = normDan;
        }

        public double getNormCcr() {
            return normCcr;
        }

        public void setNormCcr(double normCcr) {
            this.normCcr = normCcr;
        }

        public double getNormCnr() {
            return normCnr;
        }

        public void setNormCnr(double normCnr) {
            this.normCnr = normCnr;
        }
    }

    // 거리값 계산 후 distance, 노래제목, 아티스트명을 담을 ArrayList
    public class DistanceList implements Serializable, Comparable<DistanceList> {
        private double distance;
        private String title, artist;

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getArtist() {
            return artist;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }


        @Override
        public int compareTo(DistanceList distList) {
            if (this.distance < distList.getDistance()){
                return -1;
            } else if(this.distance > distList.getDistance()){
                return 1;
            }
            return 0;
        }
    }

}
