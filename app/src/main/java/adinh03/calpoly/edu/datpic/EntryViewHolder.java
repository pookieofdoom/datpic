package adinh03.calpoly.edu.datpic;

import android.content.Context;
import android.content.Intent;
import android.graphics.LightingColorFilter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * Created by Anthony on 10/26/2016.
 */

//Viewholder only looks at image for Vertical prototype. Will hold more elements later.

public class EntryViewHolder extends RecyclerView.ViewHolder
{

   private ImageView mImage;
   private Button mLike, mDislike, mComment, mFlag;
   private FirebaseDatabase mDataBase;

   public EntryViewHolder(final View itemView, final User currentUser)
   {
      super(itemView);
      mImage = (ImageView) itemView.findViewById(R.id.image);
      mLike = (Button) itemView.findViewById(R.id.like_button);
      mDislike = (Button) itemView.findViewById(R.id.dislike_button);
      mComment = (Button) itemView.findViewById(R.id.comment_button);
//      mFlag = (Button) itemView.findViewById(R.id.flag_button);

      //mImage on click listener will go to bigger image

      mComment.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View view)
         {
            viewThisComment(view);
         }
      });

      mLike.setOnTouchListener(new View.OnTouchListener()
      {
         @Override
         public boolean onTouch(View v, MotionEvent event)
         {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
               if (!v.isSelected())
               {

                  v.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));
                  v.setSelected(true);
                  setLike(true, currentUser);
               }
               else if (v.isSelected())
               {
                  v.getBackground().clearColorFilter();
                  v.setSelected(false);
                  setLike(false, currentUser);
               }
            }

            return true;
         }
      });
   }


   public void bind(Entry entry)
   {
      //load image here
      Picasso.with(mImage.getContext()).load(entry.getUri()).into(mImage);

   }

   public void viewThisComment(View view)
   {
      Context viewHolderContext = view.getContext();
      Intent intent = new Intent(viewHolderContext, AddCommentActivity.class);
      System.out.println("position: " + getAdapterPosition());
      intent.putExtra("clickedImageIndex", getAdapterPosition());
      viewHolderContext.startActivity(intent);

   }

   public void setLike(boolean isLike, User currentUser)
   {
      String path = StaticEntryList.getInstance().getMap().get(StaticEntryList
            .getInstance()
            .getEntry(getAdapterPosition()).getUri().toString());
      DatabaseReference setLike = mDataBase.getInstance().getReference("users")
            .child(currentUser.getId());
      DatabaseReference counterLike = mDataBase.getInstance().getReference("images").child(path)
            .child("LikeCount");

      //like counter breaks when going to different views

      if (isLike)
      {
         setLike.child("Like").child(path).setValue(true);
         counterLike.addListenerForSingleValueEvent(new ValueEventListener()
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
         });
      }
      else if (!isLike)
      {
         setLike.child("Like").child(path).removeValue();
         counterLike.addListenerForSingleValueEvent(new ValueEventListener()
         {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
               Log.d("DEBUG69", "onDataChange: " + dataSnapshot.getKey() + " " + dataSnapshot.getValue());
               long value = (long) dataSnapshot.getValue();
               dataSnapshot.getRef().setValue(--value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
         });
      }
   }


}
