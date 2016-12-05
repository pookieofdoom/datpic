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
import android.widget.TextView;

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
   public ImageView mLike, mDislike, mComment;
   private User mCurrentUser;
   private TextView mLocation;
   private FirebaseDatabase mDataBase;

   public EntryViewHolder(View itemView, User currentUser, View.OnClickListener
         mCommentOnClickListener, View.OnClickListener mLikeOnClickListener, View.OnClickListener
         mDislikeOnClickListener)
   {
      super(itemView);
      mCurrentUser = currentUser;
      mLocation = (TextView) itemView.findViewById(R.id.imageLocation);
      mImage = (ImageView) itemView.findViewById(R.id.image);
      mLike = (ImageView) itemView.findViewById(R.id.like_button);
      mDislike = (ImageView) itemView.findViewById(R.id.dislike_button);
      mComment = (ImageView) itemView.findViewById(R.id.comment_button);

      //mImage on click listener will go to bigger image
      mComment.setTag(R.string.viewHolder, this);
      mComment.setOnClickListener(mCommentOnClickListener);
      mLike.setTag(R.string.viewHolder, this);
      mLike.setTag(R.string.currentUser, currentUser);
      mLike.setTag(R.string.dislikeButton, mDislike);
      mLike.setOnClickListener(mLikeOnClickListener);
      mDislike.setTag(R.string.viewHolder, this);
      mDislike.setTag(R.string.currentUser, currentUser);
      mDislike.setTag(R.string.likeButton, mLike);
      mDislike.setOnClickListener(mDislikeOnClickListener);
   }


   public void bind(Entry entry)
   {
      //load image here
      Picasso.with(mImage.getContext()).load(entry.getUri()).into(mImage);
      mLike.setSelected(entry.getUserLiked());
      mDislike.setSelected(!entry.getUserDisliked());
      mLocation.setText("Posted from: " + entry.getLocation());
   }


}
