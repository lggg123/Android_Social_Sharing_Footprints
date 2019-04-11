package com.brainyapps.footprints.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.brainyapps.footprints.R;
import com.brainyapps.footprints.models.Notification;
import com.brainyapps.footprints.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SuperMan on 5/4/2018.
 */

public class RegularNotificationRecyclerAdapter extends RecyclerView.Adapter<RegularNotificationRecyclerAdapter.ViewHolder>{

    public List<Notification> notificationList = new ArrayList<>();

    public RegularNotificationRecyclerAdapter(ArrayList<Notification> notificationList){
        super();
        this.notificationList = notificationList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rightItemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_notification_regular_item, parent, false);
        return new ViewHolder(rightItemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Notification notification = notificationList.get(position);
        final ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.tvContent.setText(notification.content);
        viewHolder.tvTime.setText(Utils.converteTimestamp(notification.time));
        if(notification.type.equals(Notification.NotifyType.LIKEED)){
            viewHolder.note_image.setImageResource(R.drawable.ico_notify_like);
        }else if(notification.type.equals(Notification.NotifyType.COMMENT)){
            viewHolder.note_image.setImageResource(R.drawable.ico_notify_post);
        }else if(notification.type.equals(Notification.NotifyType.FOLLOWED)){
            viewHolder.note_image.setImageResource(R.drawable.ico_notify_accept);
        }else if(notification.type.equals(Notification.NotifyType.ACCEPTED)){
            viewHolder.note_image.setImageResource(R.drawable.ico_notify_accept);
        }else if(notification.type.equals(Notification.NotifyType.DECLINED)){
            viewHolder.note_image.setImageResource(R.drawable.ico_notify_accept);
        }

        viewHolder.parentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onSelectProfile(position, notification.type, notification.notifyFrom, notification.notifyTo);
                }
            }
        });
    }

    private OnClickItemListener mListener;

    public void setOnClickItemListener(OnClickItemListener listener) {
        this.mListener = listener;
    }

    public interface OnClickItemListener {
        void onSelectProfile(int index, String notification_type, String notification_from, String notification_to);
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
        public ImageView note_image;
        public TextView tvContent;
        public TextView tvTime;

        public ViewHolder(View convertView) {
            super(convertView);

            parentContainer = (View) convertView.findViewById(R.id.notification_regular_tab);
            note_image = (ImageView) convertView.findViewById(R.id.notification_regular_avatar);
            tvContent = (TextView) convertView.findViewById(R.id.notification_regular_content);
            tvTime = (TextView) convertView.findViewById(R.id.notification_regular_time);
        }
    }
}
