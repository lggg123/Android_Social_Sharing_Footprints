package com.brainyapps.footprints.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.brainyapps.footprints.R;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.utils.Utils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by SuperMan on 4/28/2018.
 */

public class AdminUserRecyclerAdaper extends RecyclerView.Adapter<AdminUserRecyclerAdaper.ViewHolder>{

    public List<User> adminUserviewList = new ArrayList<>();

    public AdminUserRecyclerAdaper(ArrayList<User> adminUserviewList){
        super();
        this.adminUserviewList = adminUserviewList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rightItemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_search_item, parent, false);
        return new ViewHolder(rightItemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final User selected_user = adminUserviewList.get(position);
        final ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.tvName.setText(selected_user.getName());
        if(selected_user.photoUrl.equals("")){
            char first_letter = selected_user.firstName.charAt(0);
            char last_letter = selected_user.lastName.charAt(0);
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
            Glide.with(getApplicationContext()).load(selected_user.photoUrl).into(viewHolder.avatar);
        }

        viewHolder.parentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onSelectProfile(position, selected_user.userId);
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
        return adminUserviewList.size();
    }

    public void setData(ArrayList<User> data) {
        adminUserviewList.clear();
        adminUserviewList.addAll(data);
    }

    public void clear() {
        adminUserviewList.clear();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View parentContainer;
        public CircleImageView avatar;
        public TextView tvName;

        public ViewHolder(View convertView) {
            super(convertView);

            parentContainer = (View) convertView.findViewById(R.id.searched_user_container);
            avatar = (CircleImageView) convertView.findViewById(R.id.searched_user_image);
            tvName = (TextView) convertView.findViewById(R.id.searched_user_name);
        }
    }
}