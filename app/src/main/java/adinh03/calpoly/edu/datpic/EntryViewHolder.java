package adinh03.calpoly.edu.datpic;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Anthony on 10/26/2016.
 */

//Viewholder only looks at image for Vertical prototype. Will hold more elements later.

public class EntryViewHolder extends RecyclerView.ViewHolder {

   private ImageView mImage;
   private Button mLike, mDislike, mComment, mFlag;

   public EntryViewHolder(final View itemView) {
      super(itemView);
      mImage = (ImageView) itemView.findViewById(R.id.image);
      //mLike = (Button) itemView.findViewById(R.id.like_button);
      //mDislike = (Button) itemView.findViewById(R.id.dislike_button);
      mComment = (Button) itemView.findViewById(R.id.comment_button);
//      mFlag = (Button) itemView.findViewById(R.id.flag_button);

      //mImage on click listener will go to bigger image

      mComment.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            viewThisComment(view);
         }
      });
   }

   public void bind(Entry entry) {
      //load image here
      Picasso.with(mImage.getContext()).load(entry.getUri()).into(mImage);

   }

   public void viewThisComment(View view) {
      Context viewHolderContext = view.getContext();
      Intent intent = new Intent(viewHolderContext, AddCommentActivity.class);
      System.out.println("position: " + getAdapterPosition());
      intent.putExtra("clickedImageIndex", getAdapterPosition());
      viewHolderContext.startActivity(intent);

   }




}
