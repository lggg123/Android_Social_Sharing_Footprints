package com.brainyapps.footprints;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.brainyapps.footprints.adapters.SearchRecyclerAdapter;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.models.Search;
import com.brainyapps.footprints.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.walnutlabs.android.ProgressHUD;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener, SearchRecyclerAdapter.OnClickItemListener{
    private ArrayList<Search> searchList = new ArrayList<>();
    private EditText search_box;
    private RecyclerView recyclerView;
    private SearchRecyclerAdapter searchRecyclerAdapter;
    private String searchKey;
    private int abc = 0;
    final String user_id =  FirebaseAuth.getInstance().getCurrentUser().getUid();

    private ProgressHUD mProgressDialog;
    private void showProgressHUD(String text) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        mProgressDialog = ProgressHUD.show(this, text, true);
        mProgressDialog.show();
    }
    private void hideProgressHUD() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        search_box = (EditText) findViewById(R.id.search_edit);

        searchRecyclerAdapter = new SearchRecyclerAdapter(searchList);
        recyclerView = (RecyclerView) findViewById(R.id.searched_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(searchRecyclerAdapter);
        searchRecyclerAdapter.setOnClickItemListener(this);
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String  search_key = bundle.getString(IntentExtra.SEARCH_STRING);
                search_box.setText(search_key);
                search_box.setSelection(search_box.getText().length());
                searchKey = search_box.getText().toString();
                searchAction();
            }
        }

        search_box.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || (event.getAction() == KeyEvent.ACTION_DOWN))) {
                    searchKey = search_box.getText().toString();
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    searchAction();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    public void searchAction(){
        showProgressHUD("");
        searchRecyclerAdapter.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(DBInfo.TBL_USER);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot userInfo : dataSnapshot.getChildren()) {
                        User user = userInfo.getValue(User.class);
                        if(user.banned == 0){
                            String userName = user.firstName+" "+user.lastName;
                            if(!TextUtils.isEmpty(searchKey)){
                                if(userName.toLowerCase().contains(searchKey.toLowerCase()) && !user.userId.toString().equals(user_id)){
                                    final Search new_result = new Search();
                                    new_result.userId = user.userId;
                                    new_result.firstName = user.firstName;
                                    new_result.lastName = user.lastName;
                                    new_result.photoUrl = user.photoUrl;
                                    searchList.add(new_result);
                                }
                            }
                        }
                    }
                    searchRecyclerAdapter.notifyDataSetChanged();
                }
                hideProgressHUD();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressHUD();
            }
        });
    }

    public void search_goto_backpage(View view){
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onSelectProfile(int index, String userId) {
        Intent other_user_page_intent = new Intent(this, OthersprofileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.USER_ID, userId);
        other_user_page_intent.putExtras(bundle);
        startActivity(other_user_page_intent);
    }
}
