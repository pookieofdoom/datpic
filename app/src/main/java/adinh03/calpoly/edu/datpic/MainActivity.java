package adinh03.calpoly.edu.datpic;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
      GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

   private FirebaseAuth mFirebaseAuth;
   private FirebaseUser mFirebaseUser;
   private RecyclerView mRecyclerView;
   private MyAdapter mAdapter;
   private ArrayList<Entry> mEntryList;
   private DatabaseReference mDBRef;
   private FirebaseStorage mStorage;
   private FirebaseDatabase mDataBase;
   private Button mTestUpload;
   private Button mViewComment;
   private User mUser;
   private static final int TEST_UPLOAD_INTENT = 2;
   private static final int USER_SETTING_INTENT = 3;


   private boolean mRequestLocationUpdates = false;
   private GoogleApiClient mGoogleApiClient = null;
   private Location userLocation = null;
   private LocationRequest mLocationRequest;

   private static int UPDATE_INTERVAL = 10000;
   private static int FASTEST_INTERVAL = 5000;
   private static int DISPLACEMENT = 10;
   private static int MaxRange = 10;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      // Initialize Firebase Auth
      mFirebaseAuth = FirebaseAuth.getInstance();
      mFirebaseUser = mFirebaseAuth.getCurrentUser();

      if (mFirebaseUser == null) {
         // Not logged in, launch the Log In activity
         loadLogInView();
      }

      // Initializing current User
      if (mFirebaseUser != null)
         mUser = new User(mFirebaseUser.getEmail(), mFirebaseUser.getUid());

      // Initializing Entry Objects
      mEntryList = (ArrayList<Entry>) getLastCustomNonConfigurationInstance();
      if (mEntryList == null) {
         mEntryList = new ArrayList<>();
      }
      StaticEntryList.getInstance().setEntry(mEntryList);

      mAdapter = new MyAdapter(mEntryList);

      // Initializing Recycler View
      mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
      mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
            false));
      //these lines of code fixed stuttering when scrolling images
      mRecyclerView.setHasFixedSize(true);
      mRecyclerView.setItemViewCacheSize(20);
      mRecyclerView.setDrawingCacheEnabled(true);
      mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

      mRecyclerView.setAdapter(mAdapter);

      // Initialize Storage
      mStorage = FirebaseStorage.getInstance();

      if (mFirebaseUser != null)
         mStorage.getReference().child("Photos").child(mFirebaseUser.getUid()).child("ProfilePicture")
               .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
         @Override
         public void onSuccess(Uri uri) {
            mUser.setProfilePicture(uri.toString());
         }
      }).addOnFailureListener(new OnFailureListener() {
         @Override
         public void onFailure(@NonNull Exception e) {
         }
      });

      //Test for uploading
      mTestUpload = (Button) findViewById(R.id.testUpload);
      mTestUpload.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
            imageIntent.setType("image/*");
            startActivityForResult(imageIntent, TEST_UPLOAD_INTENT);

         }
      });

      //setup for location permission
      //asks for permission
      permissionRequest();
      //build GoogleApiClient
      buildGoogleAPI();

      //DatabaseReference dbRef = mDataBase.getReference();
      //Test for downloading all images (only for Vertical Prototype)
      populateAllImages();


   }

   @Override
   protected void onStart() {

      super.onStart();
      if (mGoogleApiClient != null) {
         mGoogleApiClient.connect();
      }
   }

   @Override
   protected void onResume() {
      super.onResume();

   }

   @Override
   protected void onStop() {
      super.onStop();

      if (mGoogleApiClient.isConnected()) {
         mGoogleApiClient.disconnect();
      }
   }


   private void populateAllImages() {

      DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
      ref.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
            mEntryList.clear();
            boolean flag = false;

            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
               //Log.d("DEBUG", "User is: " + userSnapshot.getKey());
               for (DataSnapshot imageShots : userSnapshot.getChildren()) {
                  //Log.d("DEBUG", "Image is: " + imageShots.getKey());
                  if (imageShots.getKey().equals("Images")) {
                     for (DataSnapshot urlShots : imageShots.getChildren()) {
                        //Log.d("DEBUG", "Url is " + urlShots.getValue(String.class));
                        Uri data = Uri.parse(urlShots.getValue().toString());
                        Entry insert = new Entry(0, 0, data);
                        if (!mEntryList.contains(insert)) {
                           mEntryList.add(insert);
                           flag = true;
                           StaticEntryList.getInstance().setMap(data.toString(), urlShots.getKey());
                        }
                     }
                  }
               }

            }
            if (flag)
               mAdapter.notifyDataSetChanged();

         }

         @Override
         public void onCancelled(DatabaseError databaseError) {

         }
      });


   }

   private void loadLogInView() {
      Intent intent = new Intent(this, LogInActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(intent);
   }

   @Override
   public Object onRetainCustomNonConfigurationInstance() {
      return mEntryList;
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == TEST_UPLOAD_INTENT && resultCode == RESULT_OK) {
         Uri uri = data.getData();
         StorageReference filePath = mStorage.getReference().child("Photos").child(mFirebaseUser
               .getUid()).child(Long.toString(System.currentTimeMillis()));
         filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
               Toast.makeText(MainActivity.this, "Upload Done", Toast.LENGTH_LONG).show();
               Uri downloadUri = taskSnapshot.getDownloadUrl();
               storeImageToUser(downloadUri);
            }
         });
      } else if (requestCode == USER_SETTING_INTENT && resultCode == RESULT_OK) {
         mUser = (User) data.getSerializableExtra("UserIntent");

      }
   }

   private void storeImageToUser(Uri uri) {
      String userId = mFirebaseUser.getUid();
      DatabaseReference newEntry = mDataBase.getInstance().getReference("users").child(userId);
      DatabaseReference newImage = mDataBase.getInstance().getReference("images").push();
      String imageKey = newImage.getKey();
      newImage.child("url").setValue(uri.toString());
      newEntry.child("Images").child(imageKey).setValue(uri.toString());

   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      getMenuInflater().inflate(R.menu.main_menu, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      //return super.onOptionsItemSelected(item);
      switch (item.getItemId()) {
         case R.id.menu_settings:
            Intent intent = new Intent(this, UserProfileActivity.class);
            Bundle userBundle = new Bundle();
            userBundle.putSerializable("UserIntent", mUser);
            intent.putExtras(userBundle);
            startActivityForResult(intent, USER_SETTING_INTENT);
            return true;
         case R.id.sign_out:
            Toast.makeText(this, "signing out", Toast.LENGTH_SHORT).show();
            mFirebaseAuth.signOut();
            loadLogInView();
         default:
            break;
      }
      return false;

   }


   //Location Permission
   private void permissionRequest() {
      if (ActivityCompat.shouldShowRequestPermissionRationale(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION)) {
      } else {
         ActivityCompat.requestPermissions(this,
               new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
               1);
      }
   }

   @Override
   public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);

      if (requestCode == 1) {
         if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            //Displaying a toast
            System.out.println("Permission granted now you can access user location ");
         } else {
            //Displaying another toast if permission is not granted
            System.out.println("Oops you just denied the permission");
         }
      } else {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      }
   }


   private void buildGoogleAPI() {
      if (mGoogleApiClient == null) {
         mGoogleApiClient = new GoogleApiClient.Builder(this)
               .addConnectionCallbacks(this)
               .addOnConnectionFailedListener(this)
               .addApi(LocationServices.API)
               .build();
      }
   }


   @Override
   public void onLocationChanged(Location location) {

   }

   @Override
   public void onStatusChanged(String s, int i, Bundle bundle) {

   }

   @Override
   public void onProviderEnabled(String s) {

   }

   @Override
   public void onProviderDisabled(String s) {

   }

   @Override
   public void onConnected(@Nullable Bundle bundle) {
      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
         // TODO: Consider calling
         //    ActivityCompat#requestPermissions
         // here to request the missing permissions, and then overriding
         //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
         //                                          int[] grantResults)
         // to handle the case where the user grants the permission. See the documentation
         // for ActivityCompat#requestPermissions for more details.
         return;
      }
      userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
      if (userLocation != null) {
         System.out.println("latitude: " + String.valueOf(userLocation.getLatitude()));
         System.out.println("longitude: " +String.valueOf(userLocation.getLongitude()));
      }

   }

   @Override
   public void onConnectionSuspended(int i) {

   }

   @Override
   public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

   }

   //Determines the distance between where picture was uploaded and user
   private boolean withinRange(Location pictureLocation, Location userLocation) {

      int earthRadius = 6371;
      double latDistance = Math.toRadians(pictureLocation.getLatitude() - userLocation.getLatitude());
      double lngDistance = Math.toRadians(pictureLocation.getLongitude() - userLocation.getLongitude());

      double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(pictureLocation.getLatitude())) * Math.cos(Math.toRadians(userLocation.getLatitude()))
            * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      double distance = earthRadius * c * 5 / 8;
      System.out.println("distance: " + distance);
      return distance < MaxRange;
   }
}
