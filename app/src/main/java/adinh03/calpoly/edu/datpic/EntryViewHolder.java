package adinh03.calpoly.edu.datpic;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

/**
 * Created by Anthony on 10/26/2016.
 */

//Viewholder only looks at image for Vertical prototype. Will hold more elements later.

public class EntryViewHolder extends RecyclerView.ViewHolder
{
   
   public ImageView mImage;
   public ImageView mLike, mDislike, mComment, mFlag;
   private TextView mLocation;
   private FirebaseDatabase mDataBase;

   public EntryViewHolder(View itemView, View.OnClickListener
         mCommentOnClickListener, View.OnClickListener mLikeOnClickListener, View.OnClickListener
         mDislikeOnClickListener)
   {
      super(itemView);
      mLocation = (TextView) itemView.findViewById(R.id.imageLocation);
      mImage = (ImageView) itemView.findViewById(R.id.image);
      mLike = (ImageView) itemView.findViewById(R.id.like_button);
      mDislike = (ImageView) itemView.findViewById(R.id.dislike_button);
      mComment = (ImageView) itemView.findViewById(R.id.comment_button);

      //mImage on click listener will go to bigger image
      mComment.setTag(R.string.viewHolder, this);
      mComment.setOnClickListener(mCommentOnClickListener);
      mLike.setTag(R.string.viewHolder, this);
      mLike.setOnClickListener(mLikeOnClickListener);
      mDislike.setTag(R.string.viewHolder, this);
      mDislike.setOnClickListener(mDislikeOnClickListener);
   }


   public void bind(Entry entry, User user)
   {
      //load image here
      Picasso.with(mImage.getContext()).load(entry.getUrl()).into(mImage);

      mLike.setSelected(user.getLikedPhotos().containsKey(StaticEntryList.getInstance().getMap().get(entry.getUrl())));
      mDislike.setSelected(user.getDislikedPhotos().containsKey(StaticEntryList.getInstance().getMap().get(entry.getUrl())));
      mLocation.setText("Posted from: " + entry.getLocation());
   }


}
