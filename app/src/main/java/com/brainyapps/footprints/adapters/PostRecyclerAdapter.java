package com.brainyapps.footprints.adapters;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.brainyapps.footprints.R;
import com.brainyapps.footprints.models.Post;
import com.brainyapps.footprints.utils.Utils;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SuperMan on 5/2/2018.
 */

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder>{
    public List<Post> postList = new ArrayList<>();

    public PostRecyclerAdapter(ArrayList<Post> postList){
        super();
        this.postList = postList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rightItemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_post_item, parent, false);
        return new ViewHolder(rightItemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final Post myPost = postList.get(position);
        final ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.post_title.setVisibility(View.VISIBLE);
        viewHolder.post_title.setText(myPost.postTitle);
        viewHolder.post_description.setVisibility(View.VISIBLE);
        viewHolder.post_description.setText(myPost.postDescription);
        viewHolder.post_liked.setText(myPost.likes.size()+" Likes");
        viewHolder.post_comments.setText(myPost.comments.size()+" Comments");
        if(myPost.likes.toString().contains(myId)){
            viewHolder.ico_like.setImageResource(R.drawable.ico_like);
        }else {
            viewHolder.ico_like.setImageResource(R.drawable.ico_not_like);
        }
        if(myPost.mediaType.equals("Image")){
            viewHolder.media_image.setVisibility(View.VISIBLE);
            viewHolder.media_video_play_icon.setVisibility(View.GONE);
            viewHolder.media_video.setVisibility(View.GONE);
            Glide.with(viewHolder.media_image.getContext()).load(myPost.mediaUrl).into(viewHolder.media_image);
        }else {
            viewHolder.media_image.setVisibility(View.GONE);
            viewHolder.media_video_play_icon.setVisibility(View.VISIBLE);
            viewHolder.media_video.setVisibility(View.VISIBLE);
            MediaController mediaController = new MediaController(viewHolder.media_video.getContext());
            mediaController.setAnchorView(viewHolder.media_video);
            viewHolder.media_video.setMediaController(mediaController);
            viewHolder.media_video.setVideoURI(Uri.parse(myPost.mediaUrl));
        }
        viewHolder.post_time.setText(Utils.converteTimestamp(myPost.postedTime));

        viewHolder.ico_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isLiked = false;
                if (mListener != null) {
                    if(myPost.likes.toString().contains(myId)){
                        viewHolder.ico_like.setImageResource(R.drawable.ico_not_like);
                        isLiked = true;
                    }else {
                        viewHolder.ico_like.setImageResource(R.drawable.ico_like);
                        isLiked = false;
                    }
                    mListener.onLikeClick(position,isLiked, myPost.postId);
                }
            }
        });

        viewHolder.post_liked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onCheckLiked(position, myPost.postId);
                }
            }
        });

        viewHolder.post_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onCheckComments(position, myPost.postId);
                }
            }
        });
        viewHolder.media_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                viewHolder.media_video_play_icon.setVisibility(View.VISIBLE);
            }
        });

        viewHolder.media_video_play_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.media_video.start();
                viewHolder.media_video_play_icon.setVisibility(View.GONE);
            }
        });

        viewHolder.ico_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.popup_menu.setVisibility(View.VISIBLE);
            }
        });

        viewHolder.btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.downloadMedia(position, myPost.postId);
                }
                viewHolder.popup_menu.setVisibility(View.GONE);
            }
        });

        viewHolder.btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.showReportDlg(position, myPost.postId);
                }
                viewHolder.popup_menu.setVisibility(View.GONE);
            }
        });

        viewHolder.btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.popup_menu.setVisibility(View.GONE);
            }
        });
    }

    private OnClickItemListener mListener;

    public void setOnClickItemListener(OnClickItemListener listener) {
        this.mListener = listener;
    }

    public interface OnClickItemListener {
        void onLikeClick(int index,Boolean isLiked, String postId);
        void onCheckLiked(int index, String postId);
        void onCheckComments(int index, String postId);
        void media_video_play_icon(int index, String postId);
        void showReportDlg(int index,String postId);
        void downloadMedia(int index, String postId);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void setData(ArrayList<Post> data) {
        postList.clear();
        postList.addAll(data);
    }

    public void clear() {
        postList.clear();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView ico_edit;
        public ImageView ico_popup;
        public ImageView ico_like;
        public TextView post_title;
        public TextView post_description;
        public ImageView media_image;
        public ImageView media_video_play_icon;
        public VideoView media_video;
        public TextView post_time;
        public TextView post_comments;
        public TextView post_liked;

        public LinearLayout popup_menu;
        public TextView btn_download;
        public TextView btn_report;
        public TextView btn_cancel;

        public ViewHolder(View convertView) {
            super(convertView);

            ico_edit = (ImageView) convertView.findViewById(R.id.list_post_edit_ico);
            ico_edit.setVisibility(View.GONE);
            ico_popup = (ImageView) convertView.findViewById(R.id.list_post_menu);
            ico_popup.setVisibility(View.VISIBLE);
            ico_like = (ImageView) convertView.findViewById(R.id.list_post_add_like);
            post_title = (TextView) convertView.findViewById(R.id.list_post_title);
            post_description = (TextView) convertView.findViewById(R.id.list_post_description);
            post_time = (TextView) convertView.findViewById(R.id.list_post_time);
            media_image = (ImageView) convertView.findViewById(R.id.list_post_image);
            media_video = (VideoView) convertView.findViewById(R.id.list_post_video);
            media_video_play_icon = (ImageView) convertView.findViewById(R.id.list_post_video_play_icon);
            post_comments = (TextView) convertView.findViewById(R.id.list_post_comments);
            post_liked = (TextView) convertView.findViewById(R.id.list_post_liked);

            popup_menu = (LinearLayout) convertView.findViewById(R.id.list_post_popup);
            btn_download = (TextView) convertView.findViewById(R.id.list_post_popup_download);
            btn_report = (TextView) convertView.findViewById(R.id.list_post_popup_report);
            btn_cancel = (TextView) convertView.findViewById(R.id.list_post_popup_cancel);
        }
    }

}
