package adinh03.calpoly.edu.datpic;

/**
 * Created by Anthony on 10/26/2016.
 */

public class Entry {

   private int mLikeCount, mDislikeCount;

   public Entry() {

   }

   public Entry(int likeCount, int dislikeCount) {
      mLikeCount = likeCount;
      mDislikeCount = dislikeCount;
   }

   public int getmLikeCount() {
      return mLikeCount;
   }

   public void setmLikeCount(int mLikeCount) {
      this.mLikeCount = mLikeCount;
   }

   public int getmDislikeCount() {
      return mDislikeCount;
   }
}
