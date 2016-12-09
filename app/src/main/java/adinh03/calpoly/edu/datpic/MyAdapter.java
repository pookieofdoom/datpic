package adinh03.calpoly.edu.datpic;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import static adinh03.calpoly.edu.datpic.R.layout.entry;

/**
 * Created by Anthony on 10/26/2016.
 */

public class MyAdapter extends RecyclerView.Adapter<EntryViewHolder>
{
   private ArrayList<Entry> mEntry;
   private User mCurrentUser;
   private FirebaseDatabase mDataBase;
   private FirebaseStorage mStorage;


   private View.OnClickListener likeClickListener = new View.OnClickListener()
   {
      @Override
      public void onClick(View v)
      {
         EntryViewHolder holder = (EntryViewHolder) v.getTag(R.string.viewHolder);
         holder.mDislike.setSelected(false);
         boolean liked = !v.isSelected();
         v.setSelected(liked);
         setLike(holder.getAdapterPosition(), liked);
      }
   };

   private View.OnClickListener commentClickListener = new View.OnClickListener()
   {
      @Override
      public void onClick(View view)
      {
         viewThisComment(view);
      }
   };

   private View.OnClickListener dislikeClickListener = new View.OnClickListener()
   {
      @Override
      public void onClick(View v)
      {
         EntryViewHolder holder = (EntryViewHolder) v.getTag(R.string.viewHolder);
         holder.mLike.setSelected(false);
         boolean disliked = !v.isSelected();
         v.setSelected(disliked);
         setDislike(holder.getAdapterPosition(), disliked);
      }
   };

   private void viewThisComment(View view)
   {
      Context viewHolderContext = view.getContext();
      EntryViewHolder holder = (EntryViewHolder) view.getTag(R.string.viewHolder);
      Intent intent = new Intent(viewHolderContext, AddCommentActivity.class);
      intent.putExtra("clickedImageIndex", holder.getAdapterPosition());

      intent.putExtra("user", mCurrentUser);
      viewHolderContext.startActivity(intent);


   }

   private void setLike(int position, boolean isLike)
   {
      String path = StaticEntryList.getInstance().getMap().get(mEntry.get(position).getUrl());
      DatabaseReference setLike = mDataBase.getInstance().getReference("users")
            .child(mCurrentUser.getId());
      DatabaseReference counterLike = mDataBase.getInstance().getReference("images").child(path)
            .child("LikeCount");


      if (isLike)
      {
         //if it was already disliked, remove it
         if (mCurrentUser.getDislikedPhotos().containsKey(path))
         {
            //removes from array
            mCurrentUser.getDislikedPhotos().remove(path);

            //decrements dislike counter
            DatabaseReference dislikeCounter = mDataBase.getInstance().getReference("images")
                  .child(path).child("DislikeCount");
            dislikeCounter.addListenerForSingleValueEvent(decrementCounter);
            //unselects the dislike button (need tag for this?)

         }
         setLike.child("Like").child(path).setValue(true);
         mCurrentUser.getLikedPhotos().put(path, true);
         counterLike.addListenerForSingleValueEvent(incrementCounter);
      }
      else if (!isLike)
      {
         setLike.child("Like").child(path).removeValue();
         mCurrentUser.getLikedPhotos().remove(path);
         counterLike.addListenerForSingleValueEvent(decrementCounter);
      }

   }

   private void setDislike(int position, boolean isDisliked)
   {
      String path = StaticEntryList.getInstance().getMap().get(mEntry.get(position).getUrl());
      DatabaseReference setLike = mDataBase.getInstance().getReference("users")
            .child(mCurrentUser.getId());
      DatabaseReference counterDislike = mDataBase.getInstance().getReference("images").child(path)
            .child("DislikeCount");


      if (isDisliked)
      {
         //if it was already liked, remove it
         if (mCurrentUser.getLikedPhotos().containsKey(path))
         {
            //removes from array
            mCurrentUser.getLikedPhotos().remove(path);
            //decrements dislike counter
            DatabaseReference counterLike = mDataBase.getInstance().getReference("images")
                  .child(path).child("LikeCount");
            counterLike.addListenerForSingleValueEvent(decrementCounter);
            //unselects the dislike button (need tag for this?)

         }
         setLike.child("Like").child(path).setValue(false);
         mCurrentUser.getDislikedPhotos().put(path, true);

         counterDislike.addListenerForSingleValueEvent(incrementCounter);
      }
      else if (!isDisliked)
      {
         setLike.child("Like").child(path).removeValue();
         mCurrentUser.getDislikedPhotos().remove(path);
         counterDislike.addListenerForSingleValueEvent(decrementCounter);
      }

   }

   private ValueEventListener incrementCounter = new ValueEventListener()
   {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot)
      {
         long value = (long) dataSnapshot.getValue();
         dataSnapshot.getRef().setValue(++value);
      }

      @Override
      public void onCancelled(DatabaseError databaseError)
      {

      }
   };

   private ValueEventListener decrementCounter = new ValueEventListener()
   {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot)
      {
         long value = (long) dataSnapshot.getValue();
         dataSnapshot.getRef().setValue(--value);
      }

      @Override
      public void onCancelled(DatabaseError databaseError)
      {

      }
   };


   public MyAdapter(ArrayList<Entry> entry, User currentUser)
   {
      mEntry = entry;
      mCurrentUser = currentUser;
      mStorage = FirebaseStorage.getInstance();

      if (mCurrentUser != null)
      {
         DatabaseReference getNick = mDataBase.getInstance().getReference("users")
               .child(mCurrentUser.getId()).child("nickname");
         getNick.addValueEventListener(new ValueEventListener()
         {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
               System.out.println("WTF  " + (String) dataSnapshot.getValue());
               mCurrentUser.setmNickname((String) dataSnapshot.getValue());

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
         });

         mStorage.getReference().child("Photos").child(mCurrentUser.getId()).child
               ("ProfilePicture")
               .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
         {
            @Override
            public void onSuccess(Uri uri)
            {
               mCurrentUser.setProfilePicture(uri.toString());
            }
         }).addOnFailureListener(new OnFailureListener()
         {
            @Override
            public void onFailure(@NonNull Exception e)
            {
            }
         });
      }
   }

   @Override
   public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
   {
      return new EntryViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType,
            parent, false), commentClickListener, likeClickListener,
            dislikeClickListener);
   }

   @Override
   public void onBindViewHolder(EntryViewHolder holder, int position)
   {
      Entry entry = mEntry.get(position);
      holder.bind(entry, mCurrentUser);
   }

   @Override
   public int getItemCount()
   {
      return mEntry.size();
   }

   @Override
   public int getItemViewType(int position)
   {
      return entry;
   }

}
