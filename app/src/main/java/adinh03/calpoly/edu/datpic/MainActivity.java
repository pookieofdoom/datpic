package adinh03.calpoly.edu.datpic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity
{
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
      mUser = new User(mFirebaseUser.getEmail(), mFirebaseUser.getUid());

      // Initializing Entry Objects
      mEntryList = (ArrayList<Entry>) getLastCustomNonConfigurationInstance();
      if (mEntryList == null)
      {
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

      //DatabaseReference dbRef = mDataBase.getReference();
      //Test for downloading all images (only for Vertical Prototype)
      populateAllImages();


   }

   private void populateAllImages()
   {

      DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
      ref.addValueEventListener(new ValueEventListener()
      {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot)
         {
            mEntryList.clear();
            boolean flag = false;

            for (DataSnapshot userSnapshot : dataSnapshot.getChildren())
            {
               Log.d("DEBUG", "User is: " + userSnapshot.getKey());
               for (DataSnapshot imageShots : userSnapshot.getChildren())
               {
                  Log.d("DEBUG", "Image is: " + imageShots.getKey());
                  if (imageShots.getKey().equals("Images"))
                  {
                     for (DataSnapshot urlShots : imageShots.getChildren())
                     {
                        Log.d("DEBUG", "Url is " + urlShots.getValue(String.class));
                        Uri data = Uri.parse(urlShots.getValue().toString());
                        Entry insert = new Entry(0, 0, data);
                        if (!mEntryList.contains(insert))
                        {
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
   }

   private void storeImageToUser(Uri uri)
   {
      String userId = mFirebaseUser.getUid();
      DatabaseReference newEntry = mDataBase.getInstance().getReference("users").child(userId);
      DatabaseReference newImage = mDataBase.getInstance().getReference("images").push();
      String imageKey = newImage.getKey();
      newImage.child("url").setValue(uri.toString());
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
            userBundle.putSerializable("UserInfo", mUser);
            intent.putExtras(userBundle);
            startActivity(intent);
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
}
