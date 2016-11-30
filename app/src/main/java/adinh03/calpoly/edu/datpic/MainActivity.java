package adinh03.calpoly.edu.datpic;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
      GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
      LocationListener
{

   private FirebaseAuth mFirebaseAuth;
   private FirebaseUser mFirebaseUser;
   private RecyclerView mRecyclerView;
   private MyAdapter mAdapter;
   private ArrayList<Entry> mEntryList;
   private DatabaseReference mDBRef;
   private FirebaseStorage mStorage;
   private FirebaseDatabase mDataBase;
   private Button mTestUpload, mCameraUpload;
   private Button mViewComment;
   private Button mGlobalImages, mHotButton, mNewButton;
   private User mUser;
   private static final int TEST_UPLOAD_INTENT = 2;
   private static final int USER_SETTING_INTENT = 3;
   private static final int REQUEST_IMAGE_CAPTURE = 4;


   private boolean mRequestLocationUpdates = false;
   private GoogleApiClient mGoogleApiClient = null;
   private Location userLocation = null;
   private LocationRequest mLocationRequest;

   private static int UPDATE_INTERVAL = 10000;
   private static int FASTEST_INTERVAL = 5000;
   private static int DISPLACEMENT = 10;
   private static double MaxRange = 0.3;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      // Initialize Firebase Auth
      mFirebaseAuth = FirebaseAuth.getInstance();
      mFirebaseUser = mFirebaseAuth.getCurrentUser();

      if (mFirebaseUser == null)
      {
         // Not logged in, launch the Log In activity
         loadLogInView();
      }

      // Initializing current User
      if (mFirebaseUser != null)
         mUser = new User(mFirebaseUser.getEmail(), mFirebaseUser.getUid());

      // Initializing Entry Objects
      mEntryList = (ArrayList<Entry>) getLastCustomNonConfigurationInstance();
      if (mEntryList == null)
      {
         mEntryList = new ArrayList<>();
      }
      StaticEntryList.getInstance().setEntry(mEntryList);

      mAdapter = new MyAdapter(mEntryList, mUser);

      // Initializing Recycler View
      mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
      mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
            true));
      //these lines of code fixed stuttering when scrolling images
      mRecyclerView.setHasFixedSize(true);
      mRecyclerView.setItemViewCacheSize(20);
      mRecyclerView.setDrawingCacheEnabled(true);
      mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

      mRecyclerView.setAdapter(mAdapter);

      // Initialize Storage
      mStorage = FirebaseStorage.getInstance();

      if (mFirebaseUser != null)
         mStorage.getReference().child("Photos").child(mFirebaseUser.getUid()).child
               ("ProfilePicture")
               .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
         {
            @Override
            public void onSuccess(Uri uri)
            {
               mUser.setProfilePicture(uri.toString());
            }
         }).addOnFailureListener(new OnFailureListener()
         {
            @Override
            public void onFailure(@NonNull Exception e)
            {
            }
         });

      //Test for uploading
      mTestUpload = (Button) findViewById(R.id.testUpload);
      mTestUpload.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
            imageIntent.setType("image/*");
            startActivityForResult(imageIntent, TEST_UPLOAD_INTENT);

         }
      });

      mCameraUpload = (Button) findViewById(R.id.cameraUpload);
      mCameraUpload.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (imageIntent.resolveActivity(getPackageManager()) != null)
            {
               startActivityForResult(imageIntent, REQUEST_IMAGE_CAPTURE);
            }
         }
      });

      mGlobalImages = (Button) findViewById(R.id.globalImages);
      mGlobalImages.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View view)
         {
            populateAllImages();
         }
      });

      mHotButton = (Button) findViewById(R.id.hotButton);
      mHotButton.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            mRecyclerView.getLayoutManager().scrollToPosition(mEntryList.size() - 1);
            populateHotImages();
         }
      });
      mNewButton = (Button) findViewById(R.id.newButton);
      mNewButton.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            mRecyclerView.getLayoutManager().scrollToPosition(mEntryList.size() - 1);
            populateAllImages();
         }
      });


      //setup for location permission
      //asks for permission
      //permissionRequest();
      //build GoogleApiClient
      buildGoogleAPI();

      //DatabaseReference dbRef = mDataBase.getReference();
      //Test for downloading all images (only for Vertical Prototype)
//      populateLocalImages();


   }

   @Override
   protected void onStart()
   {

      super.onStart();
      if (mGoogleApiClient != null)
      {
         mGoogleApiClient.connect();
      }
   }



   @Override
   protected void onDestroy()
   {
      super.onDestroy();
      if (mGoogleApiClient.isConnected())
      {
         mGoogleApiClient.disconnect();
      }
   }




   private void populateAllImages()
   {

      DatabaseReference ref = FirebaseDatabase.getInstance().getReference("images");
      ref.addValueEventListener(new ValueEventListener()
      {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot)
         {
//            mEntryList.clear();
            boolean flag = false;


            for (DataSnapshot imageShots : dataSnapshot.getChildren())
            {
               for (DataSnapshot urlShots : imageShots.getChildren())
               {
                  if (urlShots.getKey().equals("url"))
                  {
                     Log.d("DEBUG69", "Url is " + urlShots.getValue(String.class));
                     Log.d("DEBUG69", "Url is " + imageShots.getKey());
                     Uri data = Uri.parse(urlShots.getValue().toString());
                     Entry insert = new Entry(0, 0, data, "");
                     if (!mEntryList.contains(insert))
                     {
                        mEntryList.add(insert);
                        mAdapter.notifyItemInserted(mEntryList.size()-1);
                        flag = true;
                        Log.d("DEBUG69", "onDataChange: " + data.toString() + " imageShots: " + imageShots.getKey());
                        StaticEntryList.getInstance().setMap(data.toString(), imageShots.getKey());
                     }
                  }
               }

//               if (flag)
//               {
//                  Log.d("NotifyData", "NotifyData1");
//                  mAdapter.notifyDataSetChanged();
//               }
            }
         }

         @Override
         public void onCancelled(DatabaseError databaseError)
         {

         }
      });
   }


   private void populateLocalImages()
   {

      DatabaseReference ref = FirebaseDatabase.getInstance().getReference("images");
      ref.addValueEventListener(new ValueEventListener()
      {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot)
         {
//            mEntryList.clear();
            boolean flag = true;
            double latitude = 0;
            double longitude = 0;
            String url = null;
            int likeCount = 0;
            Location pictureLocation = new Location("pictureLocation");

            for (DataSnapshot imageSnapshot : dataSnapshot.getChildren())
            {
               for (DataSnapshot imageInfoSnapshot : imageSnapshot.getChildren())
               {
                  if (imageInfoSnapshot.getKey().equals("latitude"))
                  {
                     latitude = Double.valueOf(imageInfoSnapshot.getValue().toString());
                  }
                  else if (imageInfoSnapshot.getKey().equals("longitude"))
                  {
                     longitude = Double.valueOf(imageInfoSnapshot.getValue().toString());
                  }
                  else if (imageInfoSnapshot.getKey().equals("url"))
                  {
                     url = imageInfoSnapshot.getValue().toString();
                  }
                  else if (imageInfoSnapshot.getKey().equals("LikeCount"))
                  {
                     likeCount = imageInfoSnapshot.getValue(Integer.class);
                  }

               }

               System.out.println("url issss: " + url);
               Uri data = Uri.parse(url);
               Entry insert = new Entry(likeCount, 0, data, "");
               pictureLocation.setLatitude(latitude);
               pictureLocation.setLongitude(longitude);

               Log.d("DEBUG", "this is picture location :" + pictureLocation.getLatitude());
               Log.d("DEBUG", "this is user location :" + userLocation.getLatitude());
               Log.d("DEBUG", "this is user location long: " +userLocation.getLongitude());
               if (!mEntryList.contains(insert) && withinRange(pictureLocation, userLocation))
               {
                  mEntryList.add(insert);
                  mAdapter.notifyItemInserted(mEntryList.size()-1);
                  flag = true;
                  StaticEntryList.getInstance().setMap(data.toString(), imageSnapshot.getKey());
               }

            }
//            if (flag)
//            {
//               Log.d("NotifyData", "NotifyData2");
//               mAdapter.notifyDataSetChanged();
//            }

         }

         @Override
         public void onCancelled(DatabaseError databaseError)
         {

         }
      });
   }

   private void populateHotImages()
   {

      DatabaseReference ref = FirebaseDatabase.getInstance().getReference("images");
      ref.orderByChild("LikeCount").addChildEventListener(new ChildEventListener()
      {
         @Override
         public void onChildAdded(DataSnapshot dataSnapshot, String s)
         {
            boolean flag = false;
            for (DataSnapshot urlShots : dataSnapshot.getChildren())
            {
               Entry insert = new Entry(0, 0, null, "");
               if (urlShots.getKey().equals("LikeCount"))
               {
                  insert.setLikeCount(urlShots.getValue(Integer.class));
               }
               if (urlShots.getKey().equals("url"))
               {
                  Uri data = Uri.parse(urlShots.getValue().toString());
                  insert.setUri(data);
                  if (!mEntryList.contains(insert))
                  {
                     mEntryList.add(insert);
                     mAdapter.notifyItemInserted(mEntryList.size()-1);
                     flag = true;
                     StaticEntryList.getInstance().setMap(data.toString(), urlShots.getKey());
                  }
               }


            }


         }

         @Override
         public void onChildChanged(DataSnapshot dataSnapshot, String s)
         {

         }

         @Override
         public void onChildRemoved(DataSnapshot dataSnapshot)
         {

         }

         @Override
         public void onChildMoved(DataSnapshot dataSnapshot, String s)
         {

         }

         @Override
         public void onCancelled(DatabaseError databaseError)
         {

         }
      });


   }

   private void loadLogInView()
   {
      Intent intent = new Intent(this, LogInActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(intent);
   }

   @Override
   public Object onRetainCustomNonConfigurationInstance()
   {
      return mEntryList;
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == TEST_UPLOAD_INTENT && resultCode == RESULT_OK)
      {
         Uri uri = data.getData();
         System.out.println("uri data is " + uri.toString());
         StorageReference filePath = mStorage.getReference().child("Photos").child(mFirebaseUser
               .getUid()).child(Long.toString(System.currentTimeMillis()));
         filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
         {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
               Toast.makeText(MainActivity.this, "Upload Done", Toast.LENGTH_LONG).show();
               Uri downloadUri = taskSnapshot.getDownloadUrl();
               storeImageToUser(downloadUri);
            }
         });
      }
      else if (requestCode == USER_SETTING_INTENT && resultCode == RESULT_OK)
      {
         mUser = (User) data.getSerializableExtra("UserIntent");
      }
      else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
      {

      }
   }

   private void storeImageToUser(Uri uri)
   {
      String userId = mFirebaseUser.getUid();
      DatabaseReference newEntry = mDataBase.getInstance().getReference("users").child(userId);
      DatabaseReference newImage = mDataBase.getInstance().getReference("images").push();
      String imageKey = newImage.getKey();
      newImage.child("url").setValue(uri.toString());
//      newImage.child("latitude").setValue(String.valueOf(userLocation.getLatitude()));
//      newImage.child("longitude").setValue(String.valueOf(userLocation.getLongitude()));
      newImage.child("LikeCount").setValue(0);
      newEntry.child("Images").child(imageKey).setValue(uri.toString());

   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
      super.onCreateOptionsMenu(menu);
      getMenuInflater().inflate(R.menu.main_menu, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      //return super.onOptionsItemSelected(item);
      switch (item.getItemId())
      {
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
   private void permissionRequest()
   {
      if (ActivityCompat.shouldShowRequestPermissionRationale(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION))
      {
      }
      else
      {
         ActivityCompat.requestPermissions(this,
               new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
               1);
      }
   }

   @Override
   public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults)
   {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);

      if (requestCode == 1)
      {
         if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
         {

            //Displaying a toast
            System.out.println("Permission granted now you can access user location ");
         }
         else
         {
            //Displaying another toast if permission is not granted
            System.out.println("Oops you just denied the permission");
         }
      }
      else
      {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      }
   }


   private void buildGoogleAPI()
   {
      if (mGoogleApiClient == null)
      {
         mGoogleApiClient = new GoogleApiClient.Builder(this)
               .addConnectionCallbacks(this)
               .addOnConnectionFailedListener(this)
               .addApi(LocationServices.API)
               .build();
      }
   }


   @Override
   public void onLocationChanged(Location location)
   {
      Log.d("OnConnected", "OnConnected OnLocationChanged");
      userLocation = location;
   }

   @Override
   public void onConnected(@Nullable Bundle bundle)
   {
      Log.d("OnConnected", "OnConnected");
      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
      {
         Log.d("OnConnected", "OnConnected Failed");
         // TODO: Consider calling
         //    ActivityCompat#requestPermissions
         // here to request the missing permissions, and then overriding
         //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
         //                                          int[] grantResults)
         // to handle the case where the user grants the permission. See the documentation
         // for ActivityCompat#requestPermissions for more details.
         return;
      }

      Log.d("OnConnected", "OnConnected Passed");
      mLocationRequest = LocationRequest.create();
      mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
      mLocationRequest.setInterval(100); // Update location every second
      LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

      userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
      System.out.println("hello?");
      if (userLocation != null)
      {
         System.out.println("latitude: " + String.valueOf(userLocation.getLatitude()));
         System.out.println("longitude: " + String.valueOf(userLocation.getLongitude()));
      }
      else
      {
         System.out.println("shit");
      }
      populateLocalImages();
   }

   @Override
   public void onConnectionSuspended(int i)
   {
      Log.d("OnConnected", "OnConnected Suspended");
   }

   @Override
   public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
   {
      Log.d("OnConnected", "OnConnected Connection Failed");
   }

   //Determines the distance between where picture was uploaded and user
   private boolean withinRange(Location pictureLocation, Location userLocation)
   {
      if (pictureLocation.getLatitude() == userLocation.getLatitude() && pictureLocation
            .getLongitude() == userLocation.getLongitude())
      {
         return true;
      }
      int earthRadius = 6371;
      double latDistance = Math.toRadians(pictureLocation.getLatitude() - userLocation
            .getLatitude());
      double lngDistance = Math.toRadians(pictureLocation.getLongitude() - userLocation
            .getLongitude());

      double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(pictureLocation.getLatitude())) * Math.cos(Math.toRadians
            (userLocation.getLatitude()))
            * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      double distance = earthRadius * c * 5 / 8;
      System.out.println("distance: " + distance);
      return distance < MaxRange;
   }
}
