package adinh03.calpoly.edu.datpic;

import android.net.Uri;

/**
 * Created by Anthony on 10/26/2016.
 */

//This entry class is only used for Uri to get images for Vertical Prototype. Not final version.

public class Entry {

   private int mLikeCount, mDislikeCount;

   private Uri mUri;

   public Entry() {

   }

   public Entry(int likeCount, int dislikeCount, Uri uri) {
      mLikeCount = likeCount;
      mDislikeCount = dislikeCount;
      mUri = uri;
   }

   public int getLikeCount() {
      return mLikeCount;
   }

   public void setLikeCount(int likeCount) {
      this.mLikeCount = likeCount;
   }

   public int getDislikeCount() {
      return mDislikeCount;
   }

   public Uri getUri() {
      return mUri;
   }

   public void setUri(Uri uri) {
      this.mUri = uri;
   }
}
