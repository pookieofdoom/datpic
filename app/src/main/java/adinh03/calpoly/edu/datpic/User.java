package adinh03.calpoly.edu.datpic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pooki on 11/25/2016.
 */

public class User implements Serializable
{
   private String mEmail, mId, mNickname;

   private String mProfilePicture;

   private HashMap<String, Boolean> mLikedPhotos;

   private HashMap<String, Boolean> mDislikedPhotos;

   public User(String email, String id)
   {
      mEmail = email;
      mId = id;
      mLikedPhotos = new HashMap<String, Boolean>();
      mDislikedPhotos = new HashMap<String, Boolean>();
   }

   public User(String email, String id, HashMap<String, Boolean> likedPhotos, HashMap<String, Boolean>
         dislikePhotos)
   {
      mEmail = email;
      mId = id;
      mLikedPhotos = likedPhotos;
      mDislikedPhotos = dislikePhotos;
   }

   public User(String email, String id, String profilePicture)
   {
      mEmail = email;
      mId = id;
      mProfilePicture = profilePicture;
   }

   public void setmNickname(String nickname)
   {
      mNickname = nickname;
   }

   public String getmNickname()
   {
      return mNickname;
   }

   public String getProfilePicture()
   {
      return mProfilePicture;
   }

   public void setProfilePicture(String mProfilePicture)
   {
      this.mProfilePicture = mProfilePicture;
   }

   public String getEmail()
   {
      return mEmail;
   }

   public void setEmail(String mEmail)
   {
      this.mEmail = mEmail;
   }

   public String getId()
   {
      return mId;
   }

   public void setId(String mId)
   {
      this.mId = mId;
   }

   public HashMap<String, Boolean> getLikedPhotos()
   {
      return mLikedPhotos;
   }

   public void setLikedPhotos(HashMap<String, Boolean> mLikedPhotos)
   {
      this.mLikedPhotos = mLikedPhotos;
   }

   public HashMap<String, Boolean> getDislikedPhotos(){ return mDislikedPhotos; }
}
