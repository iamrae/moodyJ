package com.example.raelee.moodyj;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<MusicList> mList;
    private RecyclerViewAdapter.OnItemClickListener mListener;
    private Context mContext;




   // 기존에는 arraylist 에서 받아온 곡정보들을 리사이클러뷰 어댑터에 연결해서 띄워줬음
    public RecyclerViewAdapter(Context context, ArrayList<MusicList> list){

        this.mList = list;
        this.mContext = context;

        Log.e(TAG, "RecyclerViewAdapter: context check" + context.getContentResolver());
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(RecyclerViewAdapter.OnItemClickListener listener) {
        mListener = listener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView albumArt;
        TextView title;
        TextView artist;
        LinearLayout parentLayout;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            albumArt = itemView.findViewById(R.id.view_albumArt);
            title = itemView.findViewById(R.id.view_title);
            artist = itemView.findViewById(R.id.view_artist);
            parentLayout = itemView.findViewById(R.id.parent_layout);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) { // just in case
                            listener.onItemClick(position);
                        }
                    }
                }
            });

        }
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 음악 리스트: 아이템 레이아웃을 등록해주기
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_my_music_list, parent, false);
        RecyclerViewAdapter.ViewHolder holder = new RecyclerViewAdapter.ViewHolder(view, mListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, final int position) {
        // 뷰홀더가 어댑터와 연결되었을 때 아이템 내 세부 항목들을 등록시켜준다.

        //Bitmap bitmap = getAlbumImage(mContext, Integer.parseInt((mList.get(position)).getAlbumId()), 200);
        MusicList musicList = mList.get(position); // 각 위치에 해당하는 아이템 ?
        holder.title.setText(musicList.getTitle()); // 아이템 제목을 클래스에서 불러와서 저장
        holder.artist.setText(musicList.getArtist()); // 아이템 내용을 클래스에서 불러와서 저장
        Bitmap bitmap = getAlbumImage(mContext, Integer.parseInt((musicList.getAlbumId())), 200);
        holder.albumArt.setImageBitmap(bitmap);


    }


    @Override
    public int getItemCount() {
        // RecyclerView 에 몇개의 아이템을 띄우는 지 확인
        return mList.size();
    }


    // 앨범아트 가져와서 비트맵으로 보여주기
    public static final BitmapFactory.Options options = new BitmapFactory.Options();

    public static Bitmap getAlbumImage(Context context, int album_id, int MAX_IMAGE_SIZE){
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://media/external/audio/albumart/" + album_id); // 앨범아트 저장소 절대경로

        if(uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = resolver.openFileDescriptor(uri, "r");

                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, options);

                int scale = 0;
                if (options.outHeight > MAX_IMAGE_SIZE || options.outWidth > MAX_IMAGE_SIZE) {
                    scale = (int) Math.pow(2, (int)Math.round(Math.log(MAX_IMAGE_SIZE / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
                }
                options.inJustDecodeBounds = false;
                options.inSampleSize = scale;

                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, options);

                if (b != null) {
                    // 실제로 원하는 사이즈로 비율 조정하는 부분
                    if (options.outHeight != MAX_IMAGE_SIZE || options.outWidth != MAX_IMAGE_SIZE) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE, true);
                        b.recycle();
                        b = tmp;
                    }
                }
                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }

        return null;
    }
}
