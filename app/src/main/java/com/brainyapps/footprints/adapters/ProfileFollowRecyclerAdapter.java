package com.brainyapps.footprints.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.brainyapps.footprints.R;
import com.brainyapps.footprints.models.Follower;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by SuperMan on 4/23/2018.
 */

public class ProfileFollowRecyclerAdapter extends RecyclerView.Adapter<ProfileFollowRecyclerAdapter.ViewHolder>{
    public List<Follower> followList = new ArrayList<>();

    public ProfileFollowRecyclerAdapter(ArrayList<Follower> searchList){
        super();
        this.followList = searchList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rightItemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_profile_follow_item, parent, false);
        return new ViewHolder(rightItemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Follower follow_user = followList.get(position);
        final ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.tvName.setVisibility(View.VISIBLE);
        viewHolder.tvName.setText(follow_user.getName());
        viewHolder.avatar.setVisibility(View.VISIBLE);
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

        viewHolder.parentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onSelectProfile(position, follow_user.userId);
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
        return followList.size();
    }

    public void setData(ArrayList<Follower> data) {
        followList.clear();
        followList.addAll(data);
    }

    public void clear() {
        followList.clear();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View parentContainer;
        public CircleImageView avatar;
        public TextView tvName;

        public ViewHolder(View convertView) {
            super(convertView);

            parentContainer = (View) convertView.findViewById(R.id.list_profile_follow_container);
            avatar = (CircleImageView) convertView.findViewById(R.id.list_profile_follow_image);
            tvName = (TextView) convertView.findViewById(R.id.list_profile_follow_name);
        }
    }
}
