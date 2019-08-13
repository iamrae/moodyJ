package com.example.raelee.moodyj;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class StrRecyclerViewAdapter extends RecyclerView.Adapter<StrRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "StrRecyclerViewAdapter"; // 각 로그 앞에 StrRecyclerViewAdapter (리사이클러뷰 어댑터) 태그 붙여두기

    private ArrayList<StreamerList> mStreamerList;
    private Context mContext;
    protected ItemListener mListener;

    public StrRecyclerViewAdapter(Context context, ArrayList<StreamerList> list, StreamingMainActivity itemListener) {
        mStreamerList = list;
        mContext = context;
        mListener = itemListener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView thumbnail;
        public TextView userId;
        public LinearLayout parentLayout;
        StreamerList item;

        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(this);
            thumbnail = view.findViewById(R.id.profile);
            userId = view.findViewById(R.id.username);
            parentLayout = view.findViewById(R.id.userLinear);

        }

        public void setData(StreamerList item) {
            this.item = item;

            Bitmap bitmap = getThumbnail(mContext, item.thumbnail);
            userId.setText(item.userId);
            thumbnail.setImageBitmap(bitmap);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                mListener.onItemClick(position);
            }
        }
    }

    @NonNull
    @Override
    public StrRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // list of streamers
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_streaming_main_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StrRecyclerViewAdapter.ViewHolder holder, final int position) {

       /* Bitmap bitmap = getThumbnail(mContext, Integer.parseInt(mStreamerList.get(position).getThumbnail()), 200);
        StreamerList streamerList = mStreamerList.get(position); // 스트리밍중인 목록 받아오기
        holder.userId.setText(streamerList.getUserId()); // username 받아오기
        holder.thumbnail.setImageBitmap(bitmap);  // 비트맵으로 받아온 이미지를 thumbnail 에 얹기*/

        holder.setData(mStreamerList.get(position));// 스트리밍중인 목록 받아오기
//


    }



    private Bitmap getThumbnail(Context context, String fileName){
        // raw folder 에서 이미지 비트맵으로 불러오기

        AssetManager assetManager = mContext.getAssets();
        InputStream inputStream = null;

        try{
            inputStream = assetManager.open(fileName);
        } catch (IOException e){
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        return bitmap;
    }

    @Override
    public int getItemCount() {
        // RecyclerView 에 몇개의 아이템을 띄우는 지 확인
        return mStreamerList.size();
    }

    public interface ItemListener {
        void onItemClick(int position);
    }
}
