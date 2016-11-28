package adinh03.calpoly.edu.datpic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

/**
 * Created by pooki on 11/25/2016.
 */

public class UserProfileActivity extends AppCompatActivity
{
   private ImageView mProfilePic;
   private TextView mUserEmail;
   private User mUser;
   private FirebaseStorage mStorage;
   private Button postButton;
   private Button commentButton;
   private Button editProfileButton;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_user_profile);
      mUser = (User) getIntent().getSerializableExtra("UserIntent");
      mProfilePic = (ImageView) findViewById(R.id.user_image);
      mUserEmail = (TextView) findViewById(R.id.setting_email);

      mStorage = FirebaseStorage.getInstance();
      postButton = (Button) findViewById(R.id.postsButton);
      commentButton = (Button) findViewById(R.id.commentsButton);
      editProfileButton = (Button) findViewById(R.id.editProfileButton);


      mUserEmail.setText(mUser.getEmail());
      Picasso
              .with(getApplicationContext())
              .load(mUser.getProfilePicture())
              .placeholder(R.mipmap.ic_launcher)
              .fit()
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

      postButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent i = new Intent(UserProfileActivity.this, UserPostsActivity.class);
            startActivity(i);
         }
      });

      commentButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent i = new Intent(UserProfileActivity.this, UserCommentsActivity.class);
            startActivity(i);
         }
      });

      editProfileButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent i = new Intent(UserProfileActivity.this, EditProfileActivity.class);
            startActivity(i);
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
         mUser.setProfilePicture(uri.toString());
         Picasso
               .with(getApplicationContext())
               .load(mUser.getProfilePicture())
               .placeholder(R.mipmap.ic_launcher)
               .fit()
               .into(mProfilePic);
         StorageReference filePath = mStorage.getReference().child("Photos").child(mUser.getId())
               .child("ProfilePicture");
         filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
         {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
               Toast.makeText(UserProfileActivity.this, "Profile Pic set", Toast.LENGTH_SHORT).show();
            }
         });
      }
   }

   @Override
   public void onBackPressed()
   {
      //super.onBackPressed();
      Intent i = new Intent();
      Bundle userBundle = new Bundle();
      userBundle.putSerializable("UserIntent", mUser);
      i.putExtras(userBundle);
      setResult(RESULT_OK, i);
      finish();
   }
}
