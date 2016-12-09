package adinh03.calpoly.edu.datpic;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
      GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
      LocationListener
{
   private FirebaseAuth mFirebaseAuth;
   private FirebaseUser mFirebaseUser;
   private RecyclerView mRecyclerView;
   private MyAdapter mAdapter;
   private ArrayList<Entry> mEntryList;
   private ArrayList<Entry> mFilterList;
   private FirebaseStorage mStorage;
   private FirebaseDatabase mDataBase;
   private Button mGlobalImages, mHotButton, mNewButton;
   private User mUser;
   private Uri mImageUri;
   private static final int TEST_UPLOAD_INTENT = 2;
   private static final int USER_SETTING_INTENT = 3;
   private static final int REQUEST_IMAGE_CAPTURE = 4;
   private static final int CHOOSE_PICTURE = 5;
   private GoogleApiClient mGoogleApiClient = null;
   private Location userLocation = null;
   private String userCity = "";
   private LocationRequest mLocationRequest;
   private char mFilterMode;
   private static int UPDATE_INTERVAL = 10000;
   private static int FASTEST_INTERVAL = 5000;
   private static int DISPLACEMENT = 10;
   private static double MaxRange = 0.3;
   private int cols;

   private Geocoder gcd;
   private Comparator globalComparator = new GlobalComparator();
   private Comparator hotComparator = new HotComparator();
   /**
    * ATTENTION: This was auto-generated to implement the App Indexing API.
    * See https://g.co/AppIndexing/AndroidStudio for more information.
    */

   /* TODO:
      1. Like's are not generated on sign in (putting it after populate images works but now
      won't generate on start up
      2. Image uploads' location is not generated after immediately posting
      3. Redo populate all images since now its showing the oldest at the top and not the newest
      (requires date stamp)
    */
   private GoogleApiClient client;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      // Initialize Firebase Auth

      mFirebaseAuth = FirebaseAuth.getInstance();
      mFirebaseUser = mFirebaseAuth.getCurrentUser();
      gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
      if (mFirebaseUser == null)
      {
         // Not logged in, launch the Log In activity
         loadLogInView();
      } else {
         locationPermissionRequest();
      }

      // Initializing current User
      if (mFirebaseUser != null)
      {
         mUser = new User(mFirebaseUser.getEmail(), mFirebaseUser.getUid());
         //loadUserLikes();
      }


      Bundle bundle = getIntent().getExtras();
      if (bundle != null)
      {
         if (bundle.containsKey("nickname"))
         {
            DatabaseReference newEntry = mDataBase.getInstance().getReference("users").child
                  (mFirebaseUser.getUid());
            String nickname = getIntent().getStringExtra("nickname");
            newEntry.child("nickname").setValue(nickname);
            mUser.setmNickname(nickname);
         }
      }


      // Initializing Entry Objects
      //modify this because multiple things need to be saved on landscape change including which
      // button at top is clicked
      // and the master entry list
      mEntryList = (ArrayList<Entry>) getLastCustomNonConfigurationInstance();
      if (mEntryList == null)
      {
         mEntryList = new ArrayList<>();
      }
      if(mFilterList == null) {
         mFilterList = new ArrayList<>();
      }
      StaticEntryList.getInstance().setEntry(mEntryList);
      mFilterList.addAll(mEntryList);

      mAdapter = new MyAdapter(mFilterList, mUser);

      // Initializing Recycler View
      if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
         if (!getResources().getBoolean(R.bool.isTablet)) {
            cols = 1;
         } else {
            cols = 1;
         }
      } else {
         if (!getResources().getBoolean(R.bool.isTablet)) {
            cols = 2;
         } else {
            cols = 2;
         }
      }
      GridLayoutManager gridlayout = new GridLayoutManager(this, cols);
      mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
      mRecyclerView.setLayoutManager(gridlayout);
      //these lines of code fixed stuttering when scrolling images
      mRecyclerView.setHasFixedSize(true);
      mRecyclerView.setItemViewCacheSize(20);
      mRecyclerView.setDrawingCacheEnabled(true);
      mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

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
               Log.d("DEBUG", "onFailure: ");
            }
         });



      //load user likes not working properly after change
      mGlobalImages = (Button) findViewById(R.id.globalButton);
      mGlobalImages.setSelected(true);
      mGlobalImages.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            if (mFilterList.size() != mEntryList.size())
            {
               Log.d("DEBUG", "onClick: size diff when hitting Global");
               mFilterList.clear();
               mFilterList.addAll(mEntryList);
            }
            v.setSelected(true);
            mFilterMode = 'G';
            mHotButton.setSelected(false);
            mNewButton.setSelected(false);
            Collections.sort(mFilterList, globalComparator);
            mAdapter.notifyDataSetChanged();
            Log.d("FilterList", "FilterList: Global: " + mFilterList);
            //loadUserLikes();
         }
      });

      mHotButton = (Button) findViewById(R.id.hotButton);
      mHotButton.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            if (mFilterList.size() != mEntryList.size())
            {
               Log.d("DEBUG", "onClick: size diff when hitting Hot");
               mFilterList.clear();
               mFilterList.addAll(mEntryList);
            }
            v.setSelected(true);
            mFilterMode = 'H';
            mGlobalImages.setSelected(false);
            mNewButton.setSelected(false);
            Collections.sort(mFilterList, hotComparator);
            Log.d("FilterList", "FilterList: Hot: " + mFilterList);
            mAdapter.notifyDataSetChanged();
            //loadUserLikes()
         }
      });
      mNewButton = (Button) findViewById(R.id.newButton);
      mNewButton.setEnabled(false);
      mNewButton.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {

            if(userLocation != null) {
               mFilterList.clear();
               for (Entry entry : mEntryList) {
                  Log.d("DEBUG", "NewButton: " + entry.getLocation() + " " + userLocation + " " + entry.getUrl());
                  if (entry.getLocation().equals(userCity)) {
                     mFilterList.add(entry);
                  }
               }

               v.setSelected(true);
               mFilterMode = 'N';
               mHotButton.setSelected(false);
               mGlobalImages.setSelected(false);
               Collections.sort(mFilterList, globalComparator);
               mAdapter.notifyDataSetChanged();
               Log.d("DEBUG", "userLocation : " + userLocation.getLongitude() + ", " + userLocation
                       .getLatitude() + " city: " + userCity);
            }
            //populateLocalImages(); //this needs to implement new comparator
         }
      });

      //build GoogleApiClient
      // ATTENTION: This was auto-generated to implement the App Indexing API.
      // See https://g.co/AppIndexing/AndroidStudio for more information.
      client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
      buildGoogleAPI();


      //populate everything in beginning then the buttons just sort what the user current can see
      populateAllImages();
      mFilterMode = 'G';

   }

   private File createTemporaryFile(String part, String ext) throws Exception
   {
      File tempDir = Environment.getExternalStorageDirectory();
      tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
      if (!tempDir.exists())
      {
         tempDir.mkdirs();
      }
      return File.createTempFile(part, ext, tempDir);
   }

   @Override
   protected void onStart()
   {

      super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
      client.connect();
      if (mGoogleApiClient != null)
      {
         mGoogleApiClient.connect();
      }
      // ATTENTION: This was auto-generated to implement the App Indexing API.
      // See https://g.co/AppIndexing/AndroidStudio for more information.
      AppIndex.AppIndexApi.start(client, getIndexApiAction());
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
      //this methods needs to update individual entry fields or u must restart whole app
      DatabaseReference ref = FirebaseDatabase.getInstance().getReference("images");
      ref.addValueEventListener(new ValueEventListener()
      {
         double latitude = 0;
         double longitude = 0;
         int likeCount = 0;
         int dislikeCount = 0;
         long uploadTime = 0;
         List<Address> addresses;

         @Override
         public void onDataChange(DataSnapshot dataSnapshot)
         {
            Log.d("DEBUG", "onDataChange: no way");
            for (DataSnapshot imageShots : dataSnapshot.getChildren())
            {
               for (DataSnapshot urlShots : imageShots.getChildren())
               {
                  if (urlShots.getKey().equals("latitude"))
                  {
                     latitude = Double.valueOf(urlShots.getValue().toString());
                  }
                  else if (urlShots.getKey().equals("longitude"))
                  {
                     longitude = Double.valueOf(urlShots.getValue().toString());
                  }
                  else if (urlShots.getKey().equals("LikeCount"))
                  {
                     likeCount = urlShots.getValue(Integer.class);
                  }
                  else if (urlShots.getKey().equals("DislikeCount"))
                  {
                     dislikeCount = urlShots.getValue(Integer.class);
                  }
                  else if (urlShots.getKey().equals("UploadTime"))
                  {
                     uploadTime = urlShots.getValue(Long.class);
                  }

                  if (urlShots.getKey().equals("url"))
                  {
                     Uri data = Uri.parse(urlShots.getValue().toString());
                     Entry insert = new Entry(likeCount, dislikeCount, urlShots.getValue()
                           .toString(), imageShots.getKey(),
                           "");
                     insert.setUploadTime(uploadTime);
                     if (!mEntryList.contains(insert))
                     {
                        insert.setLocation(getCity(latitude, longitude));

                        mEntryList.add(insert);
                        addToFilter(insert);
                        StaticEntryList.getInstance().setMap(data.toString(), imageShots.getKey());
                     }
                     else
                     {
                        int index = mEntryList.indexOf(insert);
                        //if that entry already exist, check if uploadTime needs to be updated
                           if (mEntryList.get(index).getUploadTime() != uploadTime) {
                              mEntryList.get(index).setUploadTime(uploadTime);
                           }
                           if (mEntryList.get(index).getLikeCount() != likeCount) {
                              Log.d("DEBUG", "LikeCount : " + mEntryList.get(index).getLikeCount() +
                                      " to " + likeCount);
                              mEntryList.get(index).setLikeCount(likeCount);
                           }
                           if (mEntryList.get(index).getDislikeCount() != dislikeCount) {
                              Log.d("DEBUG", "DislikeCount : " + mEntryList.get(index)
                                      .getDislikeCount() + " to " + dislikeCount);
                              mEntryList.get(index).setDislikeCount(dislikeCount);
                           }

//                        int index2 = mFilterList.indexOf(mEntryList.get(index));
//                        if (index2 != -1)
//                        {
//                           mAdapter.notifyItemInserted(index2);
//                           //mAdapter.notifyDataSetChanged();
//                        }

                     }
                  }
               }

            }
            Log.d("DEBUG", "loaduserlikes: how many times am i getting called?");
            loadUserLikes();
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

               //System.out.println("url issss: " + url);
               Uri data = Uri.parse(url);
               Entry insert = new Entry(likeCount, 0, url, "", "");
               pictureLocation.setLatitude(latitude);
               pictureLocation.setLongitude(longitude);

               //Log.d("DEBUG", "this is picture location :" + pictureLocation.getLatitude());
               //Log.d("DEBUG", "this is user location : (" + userLocation.getLatitude() + ", " +
               //      userLocation.getLongitude());
               if (!mEntryList.contains(insert) && withinRange(pictureLocation, userLocation))
               {
                  mEntryList.add(insert);
                  mAdapter.notifyItemInserted(mEntryList.size() - 1);
                  StaticEntryList.getInstance().setMap(data.toString(), imageSnapshot.getKey());
               }


            }

         }

         @Override
         public void onCancelled(DatabaseError databaseError)
         {

         }
      });
   }

   private void loadUserLikes()
   {
      DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + mUser.getId
            () + "/Like");
      Log.d("DEBUG", "loadUserLikes: ");
      //careful since this only gets called once... if we ever have to do something with likes
      //then we need to apply non single event listener
      ref.addListenerForSingleValueEvent(new ValueEventListener()
      {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot)
         {
            for (DataSnapshot likeShots : dataSnapshot.getChildren())
            {
               //store all the liked photos
               Log.d("DEBUG", "loaduserlikes: " + likeShots.getKey());
               Log.d("DEBUG", "loaduserlikes: " + "DU MAY1");
               if (likeShots.getValue(Boolean.class) == true)
               {
                  Log.d("DEBUG", "loaduserlikes: " + "DU MAY2");
                  if (!mUser.getLikedPhotos().containsKey(likeShots.getKey()))
                  {
                     mUser.getLikedPhotos().put(likeShots.getKey(), true);

                     Log.d("DEBUG", "loaduserlikes: " + "DU MAY3 " + mEntryList.size());

                     for (int i = 0; i < mEntryList.size(); i++)
                     {
                        Log.d("DEBUG", "loaduserlikes: " + "DU MAY4");
                        if (mEntryList.get(i).getImageKey().equals(likeShots.getKey()))
                        {
                           Log.d("DEBUG", "loaduserlikes: " + mEntryList.get(i).getImageKey());
                           int index = mFilterList.indexOf(mEntryList.get(i));
                           if (index != -1)
                              mAdapter.notifyItemChanged(i);
                        }
                     }
                  }
               }
               else if (likeShots.getValue(Boolean.class) == false)
               {
                  if (!mUser.getDislikedPhotos().containsKey(likeShots.getKey()))
                  {
                     mUser.getDislikedPhotos().put(likeShots.getKey(), true);

                     for (int i = 0; i < mEntryList.size(); i++)
                     {
                        if (mEntryList.get(i).getImageKey().equals(likeShots.getKey()))
                        {
                           int index = mFilterList.indexOf(mEntryList.get(i));
                           if (index != -1)
                              mAdapter.notifyItemChanged(i);
                        }
                     }
                  }
               }

            }
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
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(intent);
   }

   @Override
   public Object onRetainCustomNonConfigurationInstance()
   {
      return mEntryList;
   }

   public Bitmap grabImage()
   {
      this.getContentResolver().notifyChange(mImageUri, null);
      ContentResolver cr = this.getContentResolver();
      Bitmap bitmap = null;
      try
      {
         bitmap = MediaStore.Images.Media.getBitmap(cr, mImageUri);
      } catch (Exception e)
      {
         Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
      }

      return bitmap;
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == TEST_UPLOAD_INTENT && resultCode == RESULT_OK) {
         final Uri uri = data.getData();
         String[] filePathColumn = {MediaStore.Images.Media.DATA};

         Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
         cursor.moveToFirst();

         int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
         String filePath = cursor.getString(columnIndex);
         cursor.close();
         Bitmap selectedImageBitmap = BitmapFactory.decodeFile(filePath);
         File photo = null;
         try
         {
            // place where to store camera taken picture
            photo = createTemporaryFile("picture", ".jpg");
            photo.delete();
            FileOutputStream fOS = new FileOutputStream(photo);
            selectedImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOS);

         } catch (Exception e)
         {
            Toast.makeText(getApplicationContext(), "Please check SD card! Image shot is " +
                  "impossible!", Toast.LENGTH_LONG);
         }
         scaleImageAndStoreToFirebase(Uri.fromFile(photo));
         

      }
      else if (requestCode == USER_SETTING_INTENT && resultCode == RESULT_OK)
      {
         mUser = (User) data.getSerializableExtra("UserIntent");
      }
      else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
      {
         scaleImageAndStoreToFirebase(mImageUri);
         mImageUri = null;
      }
      else if (requestCode == CHOOSE_PICTURE && resultCode == RESULT_OK)
      {

      }
   }

   private void scaleImageAndStoreToFirebase(Uri uri)
   {
      File imageFile = new File(uri.getPath());
      Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getPath());
      System.out.println("image path: " + imageFile.getPath());

      final int maxSize = 700;
      int outWidth;
      int outHeight;
      int inWidth = imageBitmap.getWidth();
      int inHeight = imageBitmap.getHeight();
      if (inWidth > inHeight)
      {
         outWidth = maxSize;
         outHeight = (inHeight * maxSize) / inWidth;
      }
      else
      {
         outHeight = maxSize;
         outWidth = (inWidth * maxSize) / inHeight;
      }

      Bitmap out = Bitmap.createScaledBitmap(imageBitmap, outWidth, outHeight, false);
      FileOutputStream fOut;
      try
      {
         fOut = new FileOutputStream(imageFile);
         out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
         fOut.flush();
         fOut.close();
         imageBitmap.recycle();
         out.recycle();
      } catch (Exception e)
      {

      }

      saveImageToFirebase(uri);
   }

   public void saveImageToFirebase(Uri uri)
   {
      final Long currentTime = System.currentTimeMillis();
      StorageReference filePath = mStorage.getReference().child("Photos").child(mFirebaseUser
            .getUid()).child(Long.toString(currentTime));
      filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
      {
         @Override
         public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
         {
            Toast.makeText(MainActivity.this, "Upload Done", Toast.LENGTH_LONG).show();
            Uri downloadUri = taskSnapshot.getDownloadUrl();
            storeImageToUser(downloadUri, currentTime);
         }
      });
   }

   private void storeImageToUser(Uri uri, Long currentTime)
   {
      String userId = mFirebaseUser.getUid();
      DatabaseReference newEntry = mDataBase.getInstance().getReference("users").child(userId);
      DatabaseReference newImage = mDataBase.getInstance().getReference("images").push();
      String imageKey = newImage.getKey();
      if (userLocation != null)
      {
         newImage.child("latitude").setValue(String.valueOf(userLocation.getLatitude()));
         newImage.child("longitude").setValue(String.valueOf(userLocation.getLongitude()));
      }

      newImage.child("LikeCount").setValue(0);
      newImage.child("DislikeCount").setValue(0);
      newImage.child("UploadTime").setValue(currentTime);
      newEntry.child("Images").child(imageKey).setValue(uri.toString());
      newImage.child("url").setValue(uri.toString());
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
         case R.id.menu_camera:
            storagePermissionRequest();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choose Image Source");
            builder.setItems(new CharSequence[]{"Gallery", "Camera"},
                  new DialogInterface.OnClickListener()
                  {

                     @Override
                     public void onClick(DialogInterface dialog, int which)
                     {
                        switch (which)
                        {
                           case 0:
                              // GET IMAGE FROM THE GALLERY
                              Intent intent = new Intent(Intent.ACTION_PICK, android.provider
                                    .MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                              intent.setType("image/*");

                              Intent chooser = Intent.createChooser(intent, "Choose a Picture");
                              startActivityForResult(chooser, TEST_UPLOAD_INTENT);
                              break;
                           case 1:
                              Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                              String path = Environment.getExternalStorageDirectory() + File
                                    .separator + "test.jpg";
                              if (imageIntent.resolveActivity(getPackageManager()) != null)
                              {
                                 File photo = null;
                                 try
                                 {
                                    // place where to store camera taken picture
                                    photo = createTemporaryFile("picture", ".jpg");
                                    photo.delete();
                                 } catch (Exception e)
                                 {
                                    Toast.makeText(getApplicationContext(), "Please check SD " +
                                          "card! Image shot is " +
                                          "impossible!", Toast.LENGTH_LONG);
                                 }
                                 mImageUri = Uri.fromFile(photo);
                                 imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                                 startActivityForResult(imageIntent, REQUEST_IMAGE_CAPTURE);
                              }
                              break;
                           default:
                              break;
                        }
                     }
                  });
            builder.show();
         default:
            break;
      }
      return false;

   }

   private void locationPermissionRequest()
   {
      if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_DENIED)
      {
         System.out.println("location is not granted");
         ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
               .ACCESS_FINE_LOCATION}, 1);
      }
   }


   //Location Permission
   private void storagePermissionRequest()
   {
      System.out.println("here in permission request");
      if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_DENIED)
      {
         System.out.println("needs permission request");
         ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
               .READ_EXTERNAL_STORAGE,
               Manifest.permission.WRITE_EXTERNAL_STORAGE
         }, 1);
      }
   }

//   @Override
//   public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                          @NonNull int[] grantResults)
//   {
//      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//      if (requestCode == 1)
//      {
//         if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//         {
//
//            //Displaying a toast
//            System.out.println("Permission granted now you can access user location ");
//         }
//         else
//         {
//            //Displaying another toast if permission is not granted
//            System.out.println("Oops you just denied the permission");
//         }
//      }
//      else
//      {
//         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//      }
//   }


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
      if(userLocation != null) {
         userCity = getCity(location.getLatitude(), location.getLongitude());
         mNewButton.setEnabled(true);
      }
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

      mLocationRequest = LocationRequest.create();
      mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
      //mLocationRequest.setInterval(60000); //about 1 minute location checking
      mLocationRequest.setInterval(10000);
      LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
            mLocationRequest, this);
      userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
      if(userLocation != null) {
         userCity = getCity(userLocation.getLatitude(), userLocation.getLongitude());
         mNewButton.setEnabled(true);
      }
      if (userLocation != null)
      {
         System.out.println("latitude: " + String.valueOf(userLocation.getLatitude()));
         System.out.println("longitude: " + String.valueOf(userLocation.getLongitude()));
      }
      else
      {
         System.out.println("shit");
      }
      //populateLocalImages();
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

   /**
    * ATTENTION: This was auto-generated to implement the App Indexing API.
    * See https://g.co/AppIndexing/AndroidStudio for more information.
    */
   public Action getIndexApiAction()
   {
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
   public void onStop()
   {
      super.onStop();

      // ATTENTION: This was auto-generated to implement the App Indexing API.
      // See https://g.co/AppIndexing/AndroidStudio for more information.
      AppIndex.AppIndexApi.end(client, getIndexApiAction());
      client.disconnect();
   }

   public void addToFilter(Entry insert)
   {
      //this has a small bug there after the item is inserted, the references are messed up so u
      // have to hit refilter
      Log.d("DEBUG", "addToFilter: am i here? " + mFilterMode);
      if (mFilterMode == 'H')
      {
         Log.d("DEBUG", "addToFilter: H");
         mFilterList.add(insert);
         Collections.sort(mFilterList, hotComparator);

      }
      else if (mFilterMode == 'G')
      {
         Log.d("DEBUG", "addToFilter: G");
         mFilterList.add(insert);
         Collections.sort(mFilterList, globalComparator);
      }
      else if (mFilterMode == 'N')
      {
         Log.d("DEBUG", "addToFilter: N " + insert);
         if(insert.getLocation().equals(userCity)) {
            mFilterList.add(insert);
         }
         Collections.sort(mFilterList, globalComparator);
      }
      Log.d("AddToFilter", "AddToFilter: " + mFilterList);
      mAdapter.notifyItemInserted(mFilterList.indexOf(insert));
      //mAdapter.notifyDataSetChanged();
   }
   public String getCity(double latitude, double longitude) {

      List<Address> addresses = null;
      try
      {
         addresses = gcd.getFromLocation(latitude, longitude, 1);
      } catch (Exception e)
      {
      }
      if (addresses != null && addresses.size() > 0)
      {
        return addresses.get(0).getLocality();

      }
      return "";
   }
}

