package com.brainyapps.footprints.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.brainyapps.footprints.R;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.models.Notification;
import com.brainyapps.footprints.models.User;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by HappyBear on 7/2/2018.
 */

public class RequestNotificationRecyclerAdapter extends RecyclerView.Adapter<RequestNotificationRecyclerAdapter.ViewHolder>{

    public List<Notification> notificationList = new ArrayList<>();

    public RequestNotificationRecyclerAdapter(ArrayList<Notification> notificationList){
        super();
        this.notificationList = notificationList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rightItemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_notification_friend_request_item, parent, false);
        return new ViewHolder(rightItemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Notification notification = notificationList.get(position);
        final ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.tvContent.setText(notification.content);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(DBInfo.TBL_USER).child(notification.notifyFrom);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user.photoUrl.equals("")){
                    char first_letter = user.firstName.charAt(0);
                    char last_letter = user.lastName.charAt(0);
                    TextDrawable drawable = TextDrawable.builder()
                            .beginConfig()
                            .fontSize(20)
                            .bold()
                            .width(40)  // width in px
                            .height(40) // height in px
                            .endConfig()
                            .buildRect(new StringBuilder().append(first_letter).append(last_letter).toString(), Color.rgb(10,127,181));
                    viewHolder.avatar_image.setImageDrawable(drawable);
                }else {
                    Glide.with(getApplicationContext()).load(user.photoUrl).into(viewHolder.avatar_image);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        viewHolder.parentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onSelectProfile(position, notification.notifyFrom);
                }
            }
        });

        viewHolder.btn_accept.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(mListener != null) {
                    mListener.onClickAccept(position, notification.notifyFrom);
                }
            }
        });
        viewHolder.btn_decline.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(mListener != null) {
                    mListener.onClickDecline(position, notification.notifyFrom);
                }
            }
        });
    }

    private OnClickItemListener mListener;

    public void setOnClickItemListener(OnClickItemListener listener) {
        this.mListener = listener;
    }

    public interface OnClickItemListener {
        void onSelectProfile(int index, String notification_from);
        void onClickAccept(int index, String notification_from);
        void onClickDecline(int index, String notification_from);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void setData(ArrayList<Notification> data) {
        notificationList.clear();
        notificationList.addAll(data);
    }

    public void clear() {
        notificationList.clear();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View parentContainer;
        public CircleImageView avatar_image;
        public TextView tvContent;
        public RelativeLayout btn_accept;
        public RelativeLayout btn_decline;


        public ViewHolder(View convertView) {
            super(convertView);

            parentContainer = (View) convertView.findViewById(R.id.notification_friend_request_tab);
            avatar_image = (CircleImageView) convertView.findViewById(R.id.notification_friend_request_avatar);
            tvContent = (TextView) convertView.findViewById(R.id.notification_friend_request_content);
            btn_accept = (RelativeLayout) convertView.findViewById(R.id.notification_friend_request_accept);
            btn_decline = (RelativeLayout) convertView.findViewById(R.id.notification_friend_request_decline);
        }
    }
}
