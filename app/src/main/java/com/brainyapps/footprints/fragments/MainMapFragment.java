package com.brainyapps.footprints.fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.brainyapps.footprints.EditpostActivity;
import com.brainyapps.footprints.PostViewActivity;
import com.brainyapps.footprints.R;
import com.brainyapps.footprints.SearchActivity;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.models.Post;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.utils.Utils;
import com.brainyapps.footprints.views.AlertFactory;
import com.brainyapps.footprints.views.DirectionsJSONParser;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.walnutlabs.android.ProgressHUD;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import static android.content.Context.LOCATION_SERVICE;

public class MainMapFragment extends android.app.Fragment implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, LocationListener, GoogleMap.OnMyLocationChangeListener {
    private final static int MY_PERMISSIONS_REQUEST_LOCATION = 3234;

    private MapView mapView;
    private GoogleMap gmap;

    private Marker currentMarker;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private EditText search_edit;

    public static final String TAG = MainMapFragment.class.getSimpleName();

    public static final String FRAGMENT_TAG = "com_mobile_main_map_fragment_tag";

    private static Context mContext;

    private Location myLocation;
    private LatLng currentMyPos;
    private LatLng currentSelectedPos;

    private HashMap<Marker, String> mapData;

    private ArrayList<Post> currentPosts;
    private boolean isMyLocationChanged;

    private LinearLayout popupInfo;
    private LinearLayout popupDist;
    private CircleImageView post_avatar;
    private TextView post_user_name;

    private Polyline polyline;
    private View myContentsView;

    private List<User> userList = new ArrayList<>();
    private static String my_id;

    private Circle circle;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    private ProgressHUD mProgressDialog;

    private void showProgressHUD(String text) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        mProgressDialog = ProgressHUD.show(getActivity(), text, true);
        mProgressDialog.show();
    }

    private void hideProgressHUD() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    BitmapDescriptor icon_position;
    BitmapDescriptor icon_foot;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.map_gotomyposition:
                gotoMyPosition();
                break;
            default:
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            myLocation = location;
            getLocationNearyBy(true);
            checkNearybyPost(myLocation);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Utils.checkConnection(getActivity());
            int currentAPIVersion = Build.VERSION.SDK_INT;
            if (currentAPIVersion >= Build.VERSION_CODES.M) {
                Log.e("Test", "Call callback function");
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return;
        }

        if (s.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
            myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


        } else if (s.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
            myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        }
        getLocationNearyBy(true);
    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onMyLocationChange(Location location) {
        if (location != null) {
            myLocation = location;
            getLocationNearyBy(true);
            checkNearybyPost(myLocation);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (checkInRadius(marker)) {
            if ((Integer) marker.getTag() >= 1) {
                if ((Integer) marker.getTag() == 1) {
                    showPost(mapData.get(marker));
                }else if((Integer) marker.getTag() == 2){
                    editPost(mapData.get(marker));
                }
            } else {
                if (polyline != null) {
                    polyline.remove();
                }
                popupDist.setVisibility(View.GONE);
                String url = getDirectionsUrl(currentMyPos, marker.getPosition());
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
            }
        } else {
            if ((Integer) marker.getTag() == 1) {
                showPost(mapData.get(marker));
            } else if((Integer) marker.getTag() == 2){
                editPost(mapData.get(marker));
            }
            return;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getTitle().equals("My Position")) {
            return true;
        }
        if ((Integer) marker.getTag() >= 1) {
            User selected_user = getUserFromId(marker.getTitle().toString());
            if (selected_user != null) {
                post_user_name.setText(selected_user.getName());
                if (TextUtils.isEmpty(selected_user.photoUrl)) {
                    char first_letter = selected_user.firstName.toString().charAt(0);
                    char last_letter = selected_user.lastName.toString().charAt(0);
                    TextDrawable drawable = TextDrawable.builder()
                            .beginConfig()
                            .fontSize(30)
                            .bold()
                            .width(100)  // width in px
                            .height(100) // height in px
                            .endConfig()
                            .buildRect(new StringBuilder().append(first_letter).append(last_letter).toString(), Color.rgb(10, 127, 181));
                    post_avatar.setImageDrawable(drawable);
                } else {
                    Glide.with(mContext).load(selected_user.photoUrl).into(post_avatar);
//                                Glide.with(mContext).load(selected_user.photoUrl).downloadOnly(50,50);
//                                Glide.with(mContext).load(selected_user.photoUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(post_avatar);
                }
            }
        }
        popupDist.setVisibility(View.VISIBLE);
        return false;
    }

    public boolean checkInRadius(Marker marker) {
        float[] distance = new float[2];
        Location.distanceBetween(marker.getPosition().latitude, marker.getPosition().longitude,
                circle.getCenter().latitude, circle.getCenter().longitude, distance);

        if (distance[0] > circle.getRadius()) {
            return false;
        } else {
            return true;
        }
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        MyInfoWindowAdapter() {
            if (myContentsView == null) {
                myContentsView = getActivity().getLayoutInflater().inflate(R.layout.main_popup, null);
            }
        }

        @Override
        public View getInfoWindow(Marker marker) {
            if ((Integer) marker.getTag() >= 1) {
                popupInfo.setVisibility(View.VISIBLE);
                popupDist.setVisibility(View.GONE);

            } else {
                popupInfo.setVisibility(View.GONE);
                if (!checkInRadius(marker)) {
                    popupDist.setVisibility(View.GONE);
                } else {
                    popupDist.setVisibility(View.VISIBLE);
                }
            }
            return myContentsView;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    public static android.app.Fragment newInstance(Context context, String user_id) {
        mContext = context;
        my_id = user_id;
        android.app.Fragment f = new MainMapFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        icon_position = BitmapDescriptorFactory.fromResource(R.drawable.position);
        icon_foot = BitmapDescriptorFactory.fromResource(R.drawable.footprint);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(DBInfo.TBL_USER);
        showProgressHUD("");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userInfo : dataSnapshot.getChildren()) {
                    User user = userInfo.getValue(User.class);
                    userList.add(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.

        MapsInitializer.initialize(mContext);
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main_map, container, false);
        View vGotoMyPosition = (View) rootView.findViewById(R.id.map_gotomyposition);
        vGotoMyPosition.setOnClickListener(this);

        myContentsView = getActivity().getLayoutInflater().inflate(R.layout.main_popup, null);
        popupInfo = ((LinearLayout) myContentsView.findViewById(R.id.main_popup_user_info));
        popupInfo.setVisibility(View.GONE);
        popupDist = ((LinearLayout) myContentsView.findViewById(R.id.main_popup_user_dist));
        popupDist.setVisibility(View.GONE);
        post_avatar = (CircleImageView) myContentsView.findViewById(R.id.main_popup_avatar);
        post_user_name = (TextView) myContentsView.findViewById(R.id.main_popup_name);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        if (myLocation == null) {
            isMyLocationChanged = false;
        } else {
            isMyLocationChanged = true;
        }

        if (currentPosts == null) {
            currentPosts = new ArrayList<>();
        }

        mapView = (MapView) rootView.findViewById(R.id.mapview);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        search_edit = (EditText) rootView.findViewById(R.id.main_search_edit);

        search_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
                    searchAction(search_edit.getText().toString());
                    return true;
                } else {
                    return false;
                }
            }
        });
//        initLocation();
        return rootView;
    }

    private void searchAction(String search_key) {
        search_edit.setText("");
        Intent search_page_intent = new Intent(getActivity(), SearchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.SEARCH_STRING, search_key);
        search_page_intent.putExtras(bundle);
        startActivity(search_page_intent);
    }

    private void showPost(String post_id) {
        Intent post_page_intent = new Intent(getActivity(), PostViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.POST_ID, post_id);
        post_page_intent.putExtras(bundle);
        startActivity(post_page_intent);
    }

    private void editPost(String post_id) {
        Intent edit_post_intent = new Intent(getActivity(), EditpostActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.POST_ID, post_id);
        edit_post_intent.putExtras(bundle);
        startActivity(edit_post_intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        checkGpsService();
        getPosts();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        if (checkGpsService()) {
            showProgressHUD("");
            initializeMap();
        }
//        initialMapTest();
    }

    public void initializeMap() {
        gmap.setOnInfoWindowClickListener(this);
        gmap.setOnMarkerClickListener(this);
        gmap.setInfoWindowAdapter(new MyInfoWindowAdapter());
//        gmap.setOnInfoWindowClickListener(new InfoClickWindowAdapter());
        initLocation();
    }

    public void gotoMyPosition() {
        getLocationNearyBy(true);
    }

    public boolean checkGpsService() {
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            AlertFactory.showAlert(mContext, "", "Check your Location service in Settings -> Location");
            return false;
        } else {
            return true;
        }
    }

    public void checkNearybyPost(Location myLocation) {
        if (!currentPosts.isEmpty()) {
            for (Post post : currentPosts) {
                Location post_location = new Location("");
                post_location.setLatitude(post.googlePosition.latitude);
                post_location.setLongitude(post.googlePosition.longitude);
                if (myLocation.distanceTo(post_location) < 10) {
                    mDatabase.child(DBInfo.TBL_POST).child(post.postId).child("isLocked").setValue(1);
                    if (post.isLocked == 0)
                        showPost(post.postId);
                }
            }
        }
    }

    public User getUserFromId(String userId) {
        for (User user : userList) {
            if (user.userId.equals(userId)) {
                return user;
            }
        }
        return null;
    }

    public void getLocationNearyBy(boolean goMyLocation) {
        if (myLocation != null && gmap != null) {
            if (currentMarker != null)
                currentMarker.remove();

            currentMyPos = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions().position(currentMyPos);
            markerOptions.icon(icon_position);
            markerOptions.title("My Position");
            currentMarker = gmap.addMarker(markerOptions);
            if (circle != null) {
                circle.remove();
            }
            circle = gmap.addCircle(new CircleOptions()
                    .center(currentMyPos)
                    .radius(2000)
                    .strokeColor(Color.RED));
            if (goMyLocation) {
                gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentMyPos, 14));
            }
            hideProgressHUD();
        }
    }

    public void getPosts() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query myFollwings = databaseReference.child(DBInfo.TBL_USER).child(my_id).child("followings");
        myFollwings.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> userList = new HashMap<>();
                if (dataSnapshot.exists()) {
                    userList = (Map<String, String>)dataSnapshot.getValue();
                }
                userList.put(my_id,"");
                getPostResults(userList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getPostResults(final Map<String, String> users) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.child(DBInfo.TBL_POST);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentPosts.clear();
                if (!dataSnapshot.exists()) {
                    updatePlaces(null, false);
                    return;
                }
                ArrayList<Post> userArrayList = new ArrayList<>();
                for (Map.Entry<String, String> entry : users.entrySet()) {
                    String singleUser_id = (String) entry.getKey();
                    for (DataSnapshot postItem : dataSnapshot.getChildren()) {
                        Post post = postItem.getValue(Post.class);
                        if (!TextUtils.isEmpty(post.userId)) {
                            if (post.userId.equals(singleUser_id)) {
                                userArrayList.add(post);
                            }
                        }
                    }
                }
                currentPosts.addAll(userArrayList);
                updatePlaces(currentPosts, false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updatePlaces(ArrayList<Post> arrays, boolean isSearch) {
        if (gmap != null)
            gmap.clear();
        getLocationNearyBy(false);
        if (arrays == null || arrays.size() == 0) {
            return;
        }

        if (mapData == null)
            mapData = new HashMap<>();

        mapData.clear();

        int index = 0;
        while (index < arrays.size()) {
            Post post = arrays.get(index);
            if (post.googlePosition != null && post.googlePosition.containLocation()) {
                if(post.userId.equals(my_id)){
                    setMark(new LatLng(post.googlePosition.getLatitude(), post.googlePosition.getLongitude()), 2, post.userId, post.postId);
                }else {
                    setMark(new LatLng(post.googlePosition.getLatitude(), post.googlePosition.getLongitude()), post.isLocked, post.userId, post.postId);
                }
            }
            index++;
        }
    }

    private void setMark(LatLng latLng, int isLocked, String userId, String postId) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(userId);
        markerOptions.icon(icon_foot);
        Marker marker = gmap.addMarker(markerOptions);
        marker.setTag(isLocked);
        mapData.put(marker, postId);
    }

    public void initLocation() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Utils.checkLocationPermission(getActivity());
            int currentAPIVersion = Build.VERSION.SDK_INT;
            if (currentAPIVersion >= Build.VERSION_CODES.M) {
                Log.e("Test", "Call callback function");
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return;
        }
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        getLocationNearyBy(true);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            getLocationNearyBy(true);
//            if (grantResults.length > 0
//                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getLocationNearyBy(true);
//                Log.e("","ACCEPTED!!!");
//            } else {
//                Log.e("","DENIED!!!");
//                // permission denied, boo! Disable the
//                // functionality that depends on this permission.
//            }
        }
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        //
        String str_mode = "mode=walking";
        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+str_mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters+"&key="+getString(R.string.google_maps_key);

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){

        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = new ArrayList<LatLng>();
            PolylineOptions lineOptions = new PolylineOptions();
                lineOptions.width(10);
                lineOptions.color(Color.parseColor("#ca2f22"));
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
//                points = new ArrayList<LatLng>();
//                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
            }

            // Drawing polyline in the Google Map for the i-th route
            polyline = gmap.addPolyline(lineOptions);
        }
    }
}
