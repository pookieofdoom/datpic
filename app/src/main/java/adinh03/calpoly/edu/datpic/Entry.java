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
   private String mLocation;
   private long mUploadTime;

   public Entry() {

   }

   public Entry(int likeCount, int dislikeCount, String url, String imageKey, String location) {
      mLikeCount = likeCount;
      mDislikeCount = dislikeCount;
      mUri = url;
      mImageKey = imageKey;
      mLocation = location;
      mUploadTime = 0;
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


   public long getUploadTime()
   {
      return mUploadTime;
   }

   public void setUploadTime(long uploadTime)
   {
      mUploadTime = uploadTime;
   }

   @Override
   public String toString() {
      return mUri + " Likes: " + mLikeCount + " dislikes: " + mDislikeCount + " Location: " + mLocation + " uploadTime: " + mUploadTime;
   }
}
