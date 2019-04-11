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
import com.brainyapps.footprints.models.Report;
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
 * Created by SuperMan on 4/28/2018.
 */

public class AdminReportyRecyclerAdapter extends RecyclerView.Adapter<AdminReportyRecyclerAdapter.ViewHolder>{

    public List<Report> reportList = new ArrayList<>();

    public AdminReportyRecyclerAdapter(ArrayList<Report> reportList){
        super();
        this.reportList = reportList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rightItemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_search_item, parent, false);
        return new ViewHolder(rightItemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Report reported_user = reportList.get(position);
        final ViewHolder viewHolder = (ViewHolder) holder;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(DBInfo.TBL_USER).child(reported_user.reportedId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                viewHolder.tvName.setVisibility(View.VISIBLE);
                viewHolder.tvName.setText(user.getName());
                viewHolder.avatar.setVisibility(View.VISIBLE);
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
                    viewHolder.avatar.setImageDrawable(drawable);
                }else {
                    Glide.with(getApplicationContext()).load(user.photoUrl).into(viewHolder.avatar);
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
                    mListener.onSelectProfile(position, reported_user.reportId);
                }
            }
        });
    }

    private OnClickItemListener mListener;

    public void setOnClickItemListener(OnClickItemListener listener) {
        this.mListener = listener;
    }

    public interface OnClickItemListener {
        void onSelectProfile(int index, String reportId);
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public void setData(ArrayList<Report> data) {
        reportList.clear();
        reportList.addAll(data);
    }

    public void clear() {
        reportList.clear();
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