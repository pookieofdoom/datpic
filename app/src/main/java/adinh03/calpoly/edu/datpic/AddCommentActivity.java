package adinh03.calpoly.edu.datpic;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Dylan on 10/30/16.
 */

public class AddCommentActivity extends AppCompatActivity
{
   EditText inputComment;
   Button submitCommentButton;
   ArrayList<CommentEntry> commentList;
   RecyclerView commentRecyclerView;
   ImageView commentImage;
   private CommentAdapter adapter;
   DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users");
   DatabaseReference imageRef = FirebaseDatabase.getInstance().getReference("images");
   private FirebaseDatabase mDataBase;
   private FirebaseUser mFirebaseUser;
   private FirebaseAuth mFirebaseAuth;
   private User mCurrentUser;
   private int clickedImageIndex;
   private FirebaseStorage mStorage;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.comment_activity);

      mCurrentUser = (User) getIntent().getSerializableExtra("user");

      mStorage = FirebaseStorage.getInstance();


      commentList = (ArrayList<CommentEntry>) getLastCustomNonConfigurationInstance();
      if (commentList == null)
      {
         commentList = new ArrayList<>();
      }
      clickedImageIndex = getIntent().getIntExtra("clickedImageIndex", -1);
      inputComment = (EditText) findViewById(R.id.InputCommentTextBox);
      submitCommentButton = (Button) findViewById(R.id.CommentSubmitButton);
      commentRecyclerView = (RecyclerView) findViewById(R.id.CommentRecylerView);
      commentImage = (ImageView) findViewById(R.id.ImageForCommentSection);

      commentRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager
            .VERTICAL, false));

      mFirebaseAuth = FirebaseAuth.getInstance();
      mFirebaseUser = mFirebaseAuth.getCurrentUser();

      mRef.addValueEventListener(populateCommentListener());

      //sets the image user that's trying to view
      Picasso.with(this).load(StaticEntryList.getInstance().getEntry(clickedImageIndex).getUrl()).into(commentImage);

      //after update the list
      adapter = new CommentAdapter(commentList);

      commentRecyclerView.setAdapter(adapter);
      inputComment.setHintTextColor(Color.LTGRAY);


      inputComment.setOnKeyListener(new View.OnKeyListener()
      {
         @Override
         public boolean onKey(View view, int keyCode, KeyEvent keyEvent)
         {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN)
            {
               System.out.println("Before" + inputComment.getText().toString());
               if ((keyCode == KeyEvent.KEYCODE_ENTER) && isEmptyString(inputComment))
               {
                  addComment(inputComment, clickedImageIndex);
                  //adds comment in database for photo
                  inputComment.setText(null);
               }
            }
            return false;
         }
      });

      submitCommentButton.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View view)
         {
            if (isEmptyString(inputComment))
            {
               addComment(inputComment, clickedImageIndex);
               inputComment.setText(null);

            }

         }
      });

   }


   protected void addComment(EditText text, int clickedImageIndex)
   {

      String path = StaticEntryList.getInstance().getMap().get(StaticEntryList.getInstance()
            .getEntry(clickedImageIndex).getUrl());
      DatabaseReference ref = mRef.child(mFirebaseUser.getUid()).child("Entry");
      final DatabaseReference finalRef = ref;
      final String commentId = ref.push().getKey();
      ref.child(commentId).child("comment").setValue(text.getText().toString());
      Log.d("DEBUG", "addComment: " + path);
      ref.child(commentId).child("imageData").child("imageId").setValue(path);
      imageRef.child(path).child("url").addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
            System.out.println("test: "  + dataSnapshot.getValue());
            finalRef.child(commentId).child("imageData").child("imageLoc").setValue(dataSnapshot.getValue());
         }

         @Override
         public void onCancelled(DatabaseError databaseError) {

         }
      });

      ref.child(commentId).child("nickname").setValue(mCurrentUser.getmNickname());
      CommentEntry newEntry = new CommentEntry();
      newEntry.setText(text.getText().toString());
      newEntry.setNickname(mCurrentUser.getmNickname());
      if(mCurrentUser.getProfilePicture() != null) {
         newEntry.setProfilePic(Uri.parse(mCurrentUser.getProfilePicture()));
      }

      commentList.add(newEntry);
      adapter.notifyDataSetChanged();
   }

   private ValueEventListener populateCommentListener()
   {
      return new ValueEventListener()
      {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot)
         {
            commentList.clear();
            for (DataSnapshot userSnapshot : dataSnapshot.getChildren())
            {
               Log.d("DEBUG2", "User is: " + userSnapshot.getKey());
               for (DataSnapshot entrySnapShot : userSnapshot.getChildren())
               {
                  if (entrySnapShot.getKey().equals("Entry"))
                  {
                     for (DataSnapshot commentSnapShot : entrySnapShot.getChildren())
                     {
                        Log.d("DEBUG2", "CommentId is " + commentSnapShot.getKey());
                        Log.d("DEBUG2", "IMAGE URI: " + StaticEntryList.getInstance()
                              .getMap().get(StaticEntryList
                                    .getInstance().getEntry(clickedImageIndex).getUrl()));

                        if (commentSnapShot.child("imageData").child("imageId").getValue() != null &&
                              commentSnapShot.child("imageData").child("imageLoc").getValue() != null &&
                              commentSnapShot.child("imageData").child("imageId").getValue(String.class).equals
                                    (StaticEntryList.getInstance()
                                          .getMap
                                                ().get(StaticEntryList
                                                .getInstance().getEntry(clickedImageIndex).getUrl())))
                        {

                           final CommentEntry insert = new CommentEntry();
                           insert.setText(commentSnapShot.child("comment").getValue(String.class));
                           insert.setNickname(commentSnapShot.child("nickname").getValue(String.class));
                           mStorage.getReference().child("Photos").child(userSnapshot.getKey()).child
                                   ("ProfilePicture")
                                   .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                           {
                              @Override
                              public void onSuccess(Uri uri)
                              {
                                 insert.setProfilePic(uri);
                              }
                           }).addOnFailureListener(new OnFailureListener()
                           {
                              @Override
                              public void onFailure(@NonNull Exception e)
                              {
                              }
                           });
                           if (!commentList.contains(insert))
                           {
                              commentList.add(insert);
                              adapter.notifyDataSetChanged();
                           }

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
      };
   }

   protected boolean isEmptyString(EditText text)
   {
      String input = text.getText().toString();
      return input.trim().length() != 0 && !TextUtils.isEmpty(input);
   }


   @Override
   protected void onRestoreInstanceState(Bundle savedInstanceState)
   {
      super.onRestoreInstanceState(savedInstanceState);
   }


   public class CommentAdapter extends RecyclerView.Adapter<CommentHolder>
   {
      private ArrayList<CommentEntry> mList;

      public CommentAdapter(ArrayList<CommentEntry> list)
      {
         mList = list;
      }

      @Override
      public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType)
      {
         return new CommentHolder(LayoutInflater.from(parent.getContext()).inflate(viewType,
               parent, false));

      }

      @Override
      public int getItemViewType(int position)
      {
         return R.layout.comment_entry;
      }

      @Override
      public void onBindViewHolder(CommentHolder holder, int position)
      {
         holder.bind(mList.get(position));
      }

      @Override
      public int getItemCount()
      {
         return mList.size();
      }
   }

   public static class CommentHolder extends RecyclerView.ViewHolder
   {
      private TextView commentTextView;
      private TextView nicknameTextView;
      private ImageView profilePic;

      public CommentHolder(View view)
      {
         super(view);
         commentTextView = (TextView) view.findViewById(R.id.commentView);
         nicknameTextView = (TextView) view.findViewById(R.id.nicknameTextView);
         profilePic = (ImageView) view.findViewById(R.id.profilePic);
      }

      public void bind(final CommentEntry commentEntry)
      {
         commentTextView.setText(commentEntry.getText());
         nicknameTextView.setText(commentEntry.getNickname());
         System.out.println(commentEntry.getProfilePic());
         Picasso.with(profilePic.getContext()).load(commentEntry.getProfilePic()).memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE).resize(200,200).into(profilePic);
      }

      public void setListener(final CommentEntry commentEntry, final Context context)
      {
         profilePic.setTransitionName(context.getString(R.string.transition_string));
         profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(context,LargeImageActivity.class);
               intent.putExtra("pic", commentEntry.getProfilePic().toString());
               ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((UserCommentsActivity)context,(View)profilePic, context.getString(R.string.transition_string));
               context.startActivity(intent,options.toBundle());
            }
         });
      }

   }
}