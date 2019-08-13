package com.example.raelee.moodyj;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

public class NormDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "NormDBHelper";

    // NormDBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public NormDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블 생성
        /* 이름은 MUSICDESCRIPTORNORM이고, 자동으로 값이 증가하는 _id 정수형 기본키 컬럼과
        averageLoudness, dynamicComplexity, bpm, danceability,
        chordsChangesRate, chordsNumbersRate 더블 컬럼과
        chordsKey, chordsScale, title, artist 문자열 컬럼으로 구성된 테이블을 생성. */
        db.execSQL("CREATE TABLE MUSICDESCRIPTORNORM (_id INTEGER PRIMARY KEY AUTOINCREMENT, normAL DOUBLE, normDC DOUBLE, " +
                "normBPM DOUBLE, normDan DOUBLE, normCcr DOUBLE, normCnr DOUBLE," +
                "chordsKey TEXT, chordsScale TEXT, title TEXT, artist TEXT);");

        db.close();

    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // DB에 값 집어넣기
    public void insert(double normAL, double normDC, double normBPM, double normDan,
                       double normCcr, double normCnr,
                       String chordsKey, String chordsScale, String title, String artist) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO MUSICDESCRIPTORNORM VALUES(null, '" + normAL + "', '" + normDC + "', '" + normBPM + "', '"
                + normDan + "', '" + normCcr + "', '" + normCnr + "','" + chordsKey + "', '" + chordsScale + "', '"
                + title + "', '" + artist + "');");
        db.close();
    }

    public String getResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM MUSICDESCRIPTORNORM", null);

        //
        while (cursor.moveToNext()) {
            result += cursor.getInt(0)
                    + " : "
                    + cursor.getDouble(1)
                    + " | "
                    + cursor.getDouble(2)
                    + " | "
                    + cursor.getDouble(3)
                    + " | "
                    + cursor.getDouble(4)
                    + " | "
                    + cursor.getDouble(5)
                    + " | "
                    + cursor.getDouble(6)
                    + " | "
                    + cursor.getString(7)
                    + " | "
                    + cursor.getString(8)
                    + " | "
                    + cursor.getString(9)
                    + " | "
                    + cursor.getString(10);
        }

        db.close();
        return result;
    }

    // 추가된 row 의 수를 확인하는 메소드
    public int getNumber(){
        SQLiteDatabase db = getReadableDatabase();
        int num = 0;
        Cursor cursor = db.rawQuery("SELECT * FROM MUSICDESCRIPTORNORM", null);
        num = cursor.getCount();
        db.close();
        return num;

    }

}

