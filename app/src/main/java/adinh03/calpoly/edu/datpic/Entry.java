package adinh03.calpoly.edu.datpic;

import android.net.Uri;

/**
 * Created by Anthony on 10/26/2016.
 */

//This entry class is only used for Uri to get images for Vertical Prototype. Not final version.

public class Entry {

   private int mLikeCount, mDislikeCount;
   private String mUri;
   private String mImageKey;
   private boolean mUserLiked, mUserDisliked;
   private String mLocation;

   public Entry() {

   }

   public Entry(int likeCount, int dislikeCount, String url, String imageKey, String location) {
      mLikeCount = likeCount;
      mDislikeCount = dislikeCount;
      mUri = url;
      mImageKey = imageKey;
      mUserLiked = false;
      mUserDisliked = true;
      mLocation = location;
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

   public void setDislikeCount(int dislikeCount)
   {
      mDislikeCount = dislikeCount;
   }

   public String getUrl() {
      return mUri;
   }

   public void setUri(String uri) {
      this.mUri = uri;
   }

   public String getImageKey()
   {
      return mImageKey;
   }

   public void setImageKey(String imageKey)
   {
      this.mImageKey = imageKey;
   }

   public void setLocation(String location) {
      this.mLocation = location;
   }
   public String getLocation() {
      return mLocation;
   }

   @Override
   public boolean equals(Object o)
   {
      if(!(o instanceof Entry)) {
         return false;
      }
      if(!mUri.equals(((Entry)o).mUri)) {
         return false;
      }
      return true;
   }

   public boolean getUserLiked()
   {
      return mUserLiked;
   }

   public void setUserLiked(boolean userLiked)
   {
      mUserLiked = userLiked;
   }

   public boolean getUserDisliked() { return mUserDisliked; }

   public void setUserDisliked(boolean userDisliked) {mUserDisliked = userDisliked;}
}
