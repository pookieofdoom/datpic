package adinh03.calpoly.edu.datpic;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
      GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
      LocationListener {

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
   private Uri mImageUri;
   private String mPictureImagePath;
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
   /**
    * ATTENTION: This was auto-generated to implement the App Indexing API.
    * See https://g.co/AppIndexing/AndroidStudio for more information.
    */
   private GoogleApiClient client;

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


      Bundle bundle = getIntent().getExtras();
      System.out.println(bundle);
      if(bundle != null) {
         if(bundle.containsKey("nickname")) {
            DatabaseReference newEntry = mDataBase.getInstance().getReference("users").child(mFirebaseUser.getUid());
            String nickname = getIntent().getStringExtra("nickname");
            newEntry.child("nickname").setValue(nickname);
            mUser.setmNickname(nickname);
         }
      }

      //loadUserLikes();

      // Initializing Entry Objects
      mEntryList = (ArrayList<Entry>) getLastCustomNonConfigurationInstance();
      if (mEntryList == null) {
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

      mCameraUpload = (Button) findViewById(R.id.cameraUpload);
      mCameraUpload.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String path = Environment.getExternalStorageDirectory() + File.separator + "test.jpg";
            File file = new File(path);
            Uri outputFileUri = Uri.fromFile(file);
            if (imageIntent.resolveActivity(getPackageManager()) != null) {

               File photo = null;
               try {
                  // place where to store camera taken picture
                  photo = createTemporaryFile("picture", ".jpg");
                  photo.delete();
               } catch (Exception e) {
                  Toast.makeText(getApplicationContext(), "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG);
               }
               mImageUri = Uri.fromFile(photo);
               imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
               startActivityForResult(imageIntent, REQUEST_IMAGE_CAPTURE);
            }
         }
      });

      mGlobalImages = (Button) findViewById(R.id.globalImages);
      mGlobalImages.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            populateAllImages();
         }
      });

      mHotButton = (Button) findViewById(R.id.hotButton);
      mHotButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            mRecyclerView.getLayoutManager().scrollToPosition(mEntryList.size() - 1);
            populateHotImages();
         }
      });
      mNewButton = (Button) findViewById(R.id.newButton);
      mNewButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            mRecyclerView.getLayoutManager().scrollToPosition(mEntryList.size() - 1);
            populateAllImages();
         }
      });


      //setup for location permission
      //asks for permission
      permissionRequest();
      //build GoogleApiClient
      buildGoogleAPI();

      //DatabaseReference dbRef = mDataBase.getReference();
      //Test for downloading all images (only for Vertical Prototype)
//      populateLocalImages();
      populateAllImages();


      // ATTENTION: This was auto-generated to implement the App Indexing API.
      // See https://g.co/AppIndexing/AndroidStudio for more information.
      client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
   }

   private File createTemporaryFile(String part, String ext) throws Exception {
      File tempDir = Environment.getExternalStorageDirectory();
      tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
      if (!tempDir.exists()) {
         tempDir.mkdirs();
      }
      return File.createTempFile(part, ext, tempDir);
   }

   @Override
   protected void onStart() {

      super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
      client.connect();
      if (mGoogleApiClient != null) {
         mGoogleApiClient.connect();
      }
      // ATTENTION: This was auto-generated to implement the App Indexing API.
      // See https://g.co/AppIndexing/AndroidStudio for more information.
      AppIndex.AppIndexApi.start(client, getIndexApiAction());
   }


   @Override
   protected void onDestroy() {
      super.onDestroy();
      if (mGoogleApiClient.isConnected()) {
         mGoogleApiClient.disconnect();
      }
   }

   private void populateAllImages() {
      DatabaseReference ref = FirebaseDatabase.getInstance().getReference("images");
      ref.addValueEventListener(new ValueEventListener() {
         double latitude = 0;
         double longitude = 0;
         List<Address> addresses;

         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot imageShots : dataSnapshot.getChildren()) {
               for (DataSnapshot urlShots : imageShots.getChildren()) {
                  if (urlShots.getKey().equals("latitude")) {
                     latitude = Double.valueOf(urlShots.getValue().toString());
                  } else if (urlShots.getKey().equals("longitude")) {
                     longitude = Double.valueOf(urlShots.getValue().toString());
                  }

                  if (urlShots.getKey().equals("url")) {
                     Uri data = Uri.parse(urlShots.getValue().toString());
                     Entry insert = new Entry(0, 0, data, imageShots.getKey(), "");
                     if (!mEntryList.contains(insert)) {
                        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
                        try {
                           addresses = gcd.getFromLocation(latitude, longitude, 1);
                        } catch (Exception e) {
                        }
                        if (addresses.size() > 0) {
                           insert.setLocation(addresses.get(0).getLocality());

                        }
                        mEntryList.add(insert);
                        mAdapter.notifyItemInserted(mEntryList.size() - 1);
                        StaticEntryList.getInstance().setMap(data.toString(), imageShots.getKey());
                     }
                  }
               }

            }
         }

         @Override
         public void onCancelled(DatabaseError databaseError) {

         }
      });
   }


   private void populateLocalImages() {
      DatabaseReference ref = FirebaseDatabase.getInstance().getReference("images");
      ref.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
            double latitude = 0;
            double longitude = 0;
            String url = null;
            int likeCount = 0;
            Location pictureLocation = new Location("pictureLocation");

            for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
               for (DataSnapshot imageInfoSnapshot : imageSnapshot.getChildren()) {
                  if (imageInfoSnapshot.getKey().equals("latitude")) {
                     latitude = Double.valueOf(imageInfoSnapshot.getValue().toString());
                  } else if (imageInfoSnapshot.getKey().equals("longitude")) {
                     longitude = Double.valueOf(imageInfoSnapshot.getValue().toString());
                  } else if (imageInfoSnapshot.getKey().equals("url")) {
                     url = imageInfoSnapshot.getValue().toString();
                  } else if (imageInfoSnapshot.getKey().equals("LikeCount")) {
                     likeCount = imageInfoSnapshot.getValue(Integer.class);
                  }

               }

               //System.out.println("url issss: " + url);
               Uri data = Uri.parse(url);
               Entry insert = new Entry(likeCount, 0, data, "", "");
               pictureLocation.setLatitude(latitude);
               pictureLocation.setLongitude(longitude);

               //Log.d("DEBUG", "this is picture location :" + pictureLocation.getLatitude());
               //Log.d("DEBUG", "this is user location : (" + userLocation.getLatitude() + ", " +
               //      userLocation.getLongitude());
               if (!mEntryList.contains(insert) && withinRange(pictureLocation, userLocation)) {
                  mEntryList.add(insert);
                  mAdapter.notifyItemInserted(mEntryList.size() - 1);
                  StaticEntryList.getInstance().setMap(data.toString(), imageSnapshot.getKey());
               }


            }

         }

         @Override
         public void onCancelled(DatabaseError databaseError) {

         }
      });
   }

   private void populateHotImages() {
      DatabaseReference ref = FirebaseDatabase.getInstance().getReference("images");
      ref.orderByChild("LikeCount").addChildEventListener(new ChildEventListener() {
         @Override
         public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            for (DataSnapshot urlShots : dataSnapshot.getChildren()) {
               Entry insert = new Entry(0, 0, null, "", "");
               if (urlShots.getKey().equals("LikeCount")) {
                  insert.setLikeCount(urlShots.getValue(Integer.class));
               }
               if (urlShots.getKey().equals("url")) {
                  Uri data = Uri.parse(urlShots.getValue().toString());
                  insert.setUri(data);
                  if (!mEntryList.contains(insert)) {
                     mEntryList.add(insert);
                     mAdapter.notifyItemInserted(mEntryList.size() - 1);
                     StaticEntryList.getInstance().setMap(data.toString(), urlShots.getKey());
                  }
               }


            }


         }

         @Override
         public void onChildChanged(DataSnapshot dataSnapshot, String s) {

         }

         @Override
         public void onChildRemoved(DataSnapshot dataSnapshot) {

         }

         @Override
         public void onChildMoved(DataSnapshot dataSnapshot, String s) {

         }

         @Override
         public void onCancelled(DatabaseError databaseError) {

         }
      });


   }

   private void loadUserLikes() {
      DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + mUser.getId
            () + "/Like");
      //careful since this only gets called once... if we ever have to do something with likes
      //then we need to apply non single event listener
      ref.addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot likeShots : dataSnapshot.getChildren()) {
               //store all the liked photos
               if (!mUser.getLikedPhotos().contains(likeShots.getKey())) {
                  mUser.getLikedPhotos().add(likeShots.getKey());

                  for (int i = 0; i < mEntryList.size(); i++) {
                     if (mEntryList.get(i).getImageKey().equals(likeShots.getKey())) {
                        mEntryList.get(i).setUserLiked(true);
                        mAdapter.notifyItemChanged(i);
                     }
                  }
               }
            }
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

   public Bitmap grabImage() {
      this.getContentResolver().notifyChange(mImageUri, null);
      ContentResolver cr = this.getContentResolver();
      Bitmap bitmap = null;
      try {
         bitmap = MediaStore.Images.Media.getBitmap(cr, mImageUri);
      } catch (Exception e) {
         Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
      }

      return bitmap;
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == TEST_UPLOAD_INTENT && resultCode == RESULT_OK) {
         Uri uri = data.getData();
         System.out.println("uri data is " + uri.toString());


         saveImageToFirebase(uri);
      } else if (requestCode == USER_SETTING_INTENT && resultCode == RESULT_OK) {
         mUser = (User) data.getSerializableExtra("UserIntent");
      } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
         saveImageToFirebase(mImageUri);
         mImageUri = null;
      }
   }

   public void saveImageToFirebase(Uri uri) {
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
   }

   private void storeImageToUser(Uri uri) {
      String userId = mFirebaseUser.getUid();
      DatabaseReference newEntry = mDataBase.getInstance().getReference("users").child(userId);
      DatabaseReference newImage = mDataBase.getInstance().getReference("images").push();
      String imageKey = newImage.getKey();
      newImage.child("url").setValue(uri.toString());
      newImage.child("latitude").setValue(String.valueOf(userLocation.getLatitude()));
      newImage.child("longitude").setValue(String.valueOf(userLocation.getLongitude()));
      newImage.child("LikeCount").setValue(0);
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
            Manifest.permission.ACCESS_FINE_LOCATION)) {
      } else {
         ActivityCompat.requestPermissions(this,
               new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
               1);
      }
   }

   @Override
   public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
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
      Log.d("OnConnected", "OnConnected OnLocationChanged");
      userLocation = location;
   }

   @Override
   public void onConnected(@Nullable Bundle bundle) {
      Log.d("OnConnected", "OnConnected");
      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

      mLocationRequest = LocationRequest.create();
      mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
      mLocationRequest.setInterval(60000); //about 1 minute location checking
      LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
            mLocationRequest, this);
      userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

      if (userLocation != null) {
         System.out.println("latitude: " + String.valueOf(userLocation.getLatitude()));
         System.out.println("longitude: " + String.valueOf(userLocation.getLongitude()));
      } else {
         System.out.println("shit");
      }
      populateLocalImages();
   }

   @Override
   public void onConnectionSuspended(int i) {
      Log.d("OnConnected", "OnConnected Suspended");
   }

   @Override
   public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
      Log.d("OnConnected", "OnConnected Connection Failed");
   }

   //Determines the distance between where picture was uploaded and user
   private boolean withinRange(Location pictureLocation, Location userLocation) {
      if (pictureLocation.getLatitude() == userLocation.getLatitude() && pictureLocation
            .getLongitude() == userLocation.getLongitude()) {
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

   /**
    * ATTENTION: This was auto-generated to implement the App Indexing API.
    * See https://g.co/AppIndexing/AndroidStudio for more information.
    */
   public Action getIndexApiAction() {
      Thing object = new Thing.Builder()
            .setName("Main Page") // TODO: Define a title for the content shown.
            // TODO: Make sure this auto-generated URL is correct.
            .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
            .build();
      return new Action.Builder(Action.TYPE_VIEW)
            .setObject(object)
            .setActionStatus(Action.STATUS_TYPE_COMPLETED)
            .build();
   }

   @Override
   public void onStop() {
      super.onStop();

      // ATTENTION: This was auto-generated to implement the App Indexing API.
      // See https://g.co/AppIndexing/AndroidStudio for more information.
      AppIndex.AppIndexApi.end(client, getIndexApiAction());
      client.disconnect();
   }
}
