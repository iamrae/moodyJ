package com.example.raelee.moodyj;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class RecRecyclerViewAdapter extends RecyclerView.Adapter<RecRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecRecyclerViewAdapter"; // 각 로그 앞에 RecRecyclerViewAdapter (리사이클러뷰 어댑터) 태그 붙여두기

    ArrayList<RecommendedLists> mLists;
    Context mContext;
    private RecRecyclerViewAdapter.OnItemClickListener mListener;


    public RecRecyclerViewAdapter(Context context, ArrayList<RecommendedLists> lists) {

        mLists = lists;
        mContext = context;

    }

    public void addItemDecoration(SpaceItemDecoration spaceItemDecoration) {

    }


    public interface OnItemClickListener {
        void onItemClick(RecommendedLists item);
        void onItemClick(int position);
    }

    public void setOnItemClickListener(RecRecyclerViewAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;
        public ImageView imageView;
        public RelativeLayout relativeLayout;
        RecommendedLists item;

        public TextView title;
        public TextView artist;
        public TextView playButton;
        public ImageView albumArt;

        public ViewHolder(View v, final OnItemClickListener listener) {

            super(v);

            imageView = (ImageView) v.findViewById(R.id.cardAlbum);
            relativeLayout = (RelativeLayout) v.findViewById(R.id.cardRelative);
            title = (TextView) v.findViewById(R.id.cardTitle);
            artist = (TextView) v.findViewById(R.id.cardArtist);



            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {

                            item = mLists.get(position);
                            listener.onItemClick(item);
                            listener.onItemClick(position);

                        }
                    }
                }
            });

        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_streaming_main_recom, parent, false);
        ViewHolder holder = new ViewHolder(view, mListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        RecommendedLists recommendedLists = mLists.get(position);
        String url = "http://ec2-13-125-241-11.ap-northeast-2.compute.amazonaws.com/Music/"; // 수정 서버(190718)
        String ext = ".jpg";

        holder.title.setText(recommendedLists.getTitle());
        holder.artist.setText(recommendedLists.getArtist());
        Glide.with(mContext)
                .load(url + recommendedLists.getFileName() + ext).into(holder.imageView);
    }

    @Override
    public int getItemCount() {

        return mLists.size();

    }


}
