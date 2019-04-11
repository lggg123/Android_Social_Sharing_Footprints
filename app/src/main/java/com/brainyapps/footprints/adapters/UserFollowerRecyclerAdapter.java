package com.brainyapps.footprints.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.brainyapps.footprints.R;
import com.brainyapps.footprints.models.Follower;
import com.brainyapps.footprints.models.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by SuperMan on 4/26/2018.
 */

public class UserFollowerRecyclerAdapter extends RecyclerView.Adapter<UserFollowerRecyclerAdapter.ViewHolder> {
    final String my_user_id =  FirebaseAuth.getInstance().getCurrentUser().getUid();
    public List<User> followerList = new ArrayList<>();

    public UserFollowerRecyclerAdapter(ArrayList<User> followerList){
        super();
        this.followerList = followerList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rightItemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_follower_item, parent, false);
        return new ViewHolder(rightItemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final User follow_user = followerList.get(position);

        viewHolder.tvName.setText(follow_user.getName());
        if(follow_user.photoUrl.equals("")){
            char first_letter = follow_user.firstName.charAt(0);
            char last_letter = follow_user.lastName.charAt(0);
            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    .fontSize(30)
                    .bold()
                    .width(100)  // width in px
                    .height(100) // height in px
                    .endConfig()
                    .buildRect(new StringBuilder().append(first_letter).append(last_letter).toString(), Color.rgb(10,127,181));
            viewHolder.avatar.setImageDrawable(drawable);
        }else {
            Glide.with(getApplicationContext()).load(follow_user.photoUrl).into(viewHolder.avatar);
        }

        viewHolder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onSelectProfile(position, follow_user.userId);
                }
            }
        });
        viewHolder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.btn_following.setVisibility(View.VISIBLE);
                viewHolder.btn_follow.setVisibility(View.GONE);
                if (mListener != null) {
                    mListener.onFollowUser(position, follow_user.userId);
                }
            }
        });
        if(follow_user.followers.isEmpty()){
           viewHolder.btn_following.setVisibility(View.GONE);
           viewHolder.btn_follow.setVisibility(View.VISIBLE);
        }else {
            for(Map.Entry<String,String> single: follow_user.followers.entrySet()){
                if(single.getKey().toString().equals(my_user_id)){
                    viewHolder.btn_following.setVisibility(View.VISIBLE);
                    viewHolder.btn_follow.setVisibility(View.GONE);
                }
            }
        }
    }

    private OnClickItemListener mListener;

    public void setOnClickItemListener(OnClickItemListener listener) {
        this.mListener = listener;
    }

    public interface OnClickItemListener {
        void onSelectProfile(int index, String userId);
        void onFollowUser(int index, String userId);
    }

    @Override
    public int getItemCount() {
        return followerList.size();
    }

    public void setData(ArrayList<User> data) {
        followerList.clear();
        followerList.addAll(data);
    }

    public void clear() {
        followerList.clear();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View parentContainer;
        public CircleImageView avatar;
        public TextView tvName;
        public RelativeLayout btn_following;
        public RelativeLayout btn_follow;

        public ViewHolder(View convertView) {
            super(convertView);

            parentContainer = (View) convertView.findViewById(R.id.list_follower_container);
            avatar = (CircleImageView) convertView.findViewById(R.id.list_follower_image);
            tvName = (TextView) convertView.findViewById(R.id.list_follower_name);
            btn_following = (RelativeLayout)convertView.findViewById(R.id.list_follower_following_btn);
            btn_follow = (RelativeLayout)convertView.findViewById(R.id.list_follower_follow_btn);
        }
    }
}
