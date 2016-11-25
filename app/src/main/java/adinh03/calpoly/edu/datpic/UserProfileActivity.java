package adinh03.calpoly.edu.datpic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * Created by pooki on 11/25/2016.
 */

public class UserProfileActivity extends AppCompatActivity
{
   private ImageView mProfilePic;
   private TextView mUserEmail;
   private User mUser;
   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_user_profile);
      mUser = (User) getIntent().getExtras().get("UserInfo");
      mProfilePic = (ImageView) findViewById(R.id.user_image);
      mUserEmail = (TextView) findViewById(R.id.setting_email);
      mUserEmail.setText(mUser.getEmail());
      Picasso
            .with(getApplicationContext())
            .load(mUser.getProfilePicture())
            .placeholder(R.mipmap.ic_launcher)
            .into(mProfilePic);

      mProfilePic.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
            imageIntent.setType("image/*");
            startActivityForResult(imageIntent, 2);
         }
      });

   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      super.onActivityResult(requestCode, resultCode, data);
      if (resultCode == RESULT_OK && requestCode == 2)
      {
         Uri uri = data.getData();
         mUser.setProfilePicture(uri);
         Picasso
               .with(getApplicationContext())
               .load(mUser.getProfilePicture())
               .placeholder(R.mipmap.ic_launcher)
               .into(mProfilePic);
      }
      DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
      ref.addValueEventListener(new ValueEventListener()
      {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot)
         {
            for (DataSnapshot userSnapshot : dataSnapshot.getChildren())
            {
               Log.d("DEBUG", "User is: " + userSnapshot.getKey());
               if (userSnapshot.getKey().equals(mUser.getId()))
               {
                  //will store profile picture here
               }

            }
         }

         @Override
         public void onCancelled(DatabaseError databaseError)
         {

         }
      });
   }
}
