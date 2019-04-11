package com.brainyapps.footprints.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.brainyapps.footprints.R;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.models.Comment;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.utils.Utils;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by SuperMan on 4/24/2018.
 */

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder> {

    public List<Comment> commentList = new ArrayList<>();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(DBInfo.TBL_USER);

    public CommentRecyclerAdapter(ArrayList<Comment> commentList){
        super();
        this.commentList = commentList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rightItemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_comment_item, parent, false);
        return new ViewHolder(rightItemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Comment commenter = commentList.get(position);
        final ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.setUserInfo(commenter.userId);
        viewHolder.tvContent.setText(commenter.comment);
        viewHolder.tvTime.setText(this.getTime(commenter.commentTime));

        viewHolder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onSelectProfile(position, commenter.userId);
                }
            }
        });
    }

    private OnClickItemListener mListener;

    public void setOnClickItemListener(OnClickItemListener listener) {
        this.mListener = listener;
    }

    public interface OnClickItemListener {
        void onSelectProfile(int index, String userId);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void setData(ArrayList<Comment> data) {
        commentList.clear();
        commentList.addAll(data);
    }

    public void clear() {
        commentList.clear();
    }

    public String getTime(String time){
        return Utils.converteTimestamp(time);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View parentContainer;
        public CircleImageView avatar;
        public TextView tvName;
        public TextView tvTime;
        public TextView tvContent;

        public Map<String,String> infoList = new HashMap<>();

        public ViewHolder(View convertView) {
            super(convertView);

            parentContainer = (View) convertView.findViewById(R.id.list_comment_container);
            avatar = (CircleImageView) convertView.findViewById(R.id.list_comment_image);
            tvName = (TextView) convertView.findViewById(R.id.list_comment_name);
            tvTime = (TextView) convertView.findViewById(R.id.list_comment_time);
            tvContent = (TextView) convertView.findViewById(R.id.list_comment_content);
        }

        public void setUserInfo(String userId) {
            getInfo(userId);
        }

        public void getInfo(final String userId){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(DBInfo.TBL_USER).child(userId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    tvName.setText(user.firstName+" "+user.lastName);
                    if(user.photoUrl.equals("")){
                        char first_letter = user.firstName.charAt(0);
                        char last_letter = user.lastName.charAt(0);
                        TextDrawable drawable = TextDrawable.builder()
                                .beginConfig()
                                .fontSize(30)
                                .bold()
                                .width(100)  // width in px
                                .height(100) // height in px
                                .endConfig()
                                .buildRect(new StringBuilder().append(first_letter).append(last_letter).toString(), Color.rgb(10,127,181));
                        avatar.setImageDrawable(drawable);
                    }else {
                        Glide.with(getApplicationContext()).load(user.photoUrl).into(avatar);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}