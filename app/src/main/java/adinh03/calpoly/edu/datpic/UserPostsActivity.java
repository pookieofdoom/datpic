package adinh03.calpoly.edu.datpic;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by tjyung on 11/26/16.
 */

public class UserPostsActivity extends AppCompatActivity
{
   private FirebaseAuth mFirebaseAuth;
   private FirebaseUser mFirebaseUser;
   private RecyclerView mRecyclerView;
   private MyAdapter mAdapter;
   private ArrayList<Entry> mEntryList;
   private User mUser;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.user_post_activity);

      // Initialize Firebase Auth
      mFirebaseAuth = FirebaseAuth.getInstance();
      mFirebaseUser = mFirebaseAuth.getCurrentUser();


      // Initializing Entry Objects
      mEntryList = (ArrayList<Entry>) getLastCustomNonConfigurationInstance();
      if (mEntryList == null)
      {
         mEntryList = new ArrayList<>();
      }
      StaticEntryList.getInstance().setEntry(mEntryList);
      if (mFirebaseUser != null)
         mUser = new User(mFirebaseUser.getEmail(), mFirebaseUser.getUid());
      mAdapter = new MyAdapter(mEntryList, mUser);

      // Initializing Recycler View
      mRecyclerView = (RecyclerView) findViewById(R.id.userPostsRecyclerView);
      mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
            false));
      //these lines of code fixed stuttering when scrolling images
      mRecyclerView.setHasFixedSize(true);
      mRecyclerView.setItemViewCacheSize(20);
      mRecyclerView.setDrawingCacheEnabled(true);
      mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

      mRecyclerView.setAdapter(mAdapter);

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
               if (userSnapshot.getKey().equals(mFirebaseUser.getUid()))
               {
                  for (DataSnapshot imageShots : userSnapshot.getChildren())
                  {
                     if (imageShots.getKey().equals("Images"))
                     {
                        for (DataSnapshot urlShots : imageShots.getChildren())
                        {
                           Uri data = Uri.parse(urlShots.getValue().toString());
                           Entry insert = new Entry(0, 0, data, "");
                           if (!mEntryList.contains(insert))
                           {
                              mEntryList.add(insert);
                              flag = true;
                              StaticEntryList.getInstance().setMap(data.toString(), urlShots
                                    .getKey());
                           }
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
}
