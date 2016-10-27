package adinh03.calpoly.edu.datpic;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by Anthony on 10/26/2016.
 */

public class EntryViewHolder extends RecyclerView.ViewHolder {

   private ImageView mImage;
   private Button mLike, mDislike, mComment, mFlag;

   public EntryViewHolder(View itemView) {
      super(itemView);
      mImage = (ImageView) itemView.findViewById(R.id.image);
      mLike = (Button) itemView.findViewById(R.id.like_button);
      mDislike = (Button) itemView.findViewById(R.id.dislike_button);
      mComment = (Button) itemView.findViewById(R.id.comment_button);
      mFlag = (Button) itemView.findViewById(R.id.flag_button);

      //mImage on click listener will go to bigger image

   }


}
