package adinh03.calpoly.edu.datpic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pooki on 11/25/2016.
 */

public class User implements Serializable
{
   private String mEmail, mId;

   private String mProfilePicture;

   private ArrayList<String> mLikedPhotos;

   public User(String email, String id)
   {
      mEmail = email;
      mId = id;
      mLikedPhotos = new ArrayList<>();
   }
   public User(String email, String id, ArrayList<String> likedPhotos)
   {
      mEmail = email;
      mId = id;
      mLikedPhotos = likedPhotos;
   }

   public User(String email, String id, String profilePicture)
   {
      mEmail = email;
      mId = id;
      mProfilePicture = profilePicture;
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

   public ArrayList<String> getLikedPhotos()
   {
      return mLikedPhotos;
   }

   public void setLikedPhotos(ArrayList<String> mLikedPhotos)
   {
      this.mLikedPhotos = mLikedPhotos;
   }
}
