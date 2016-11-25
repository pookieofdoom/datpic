package adinh03.calpoly.edu.datpic;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by pooki on 11/25/2016.
 */

public class User implements Serializable
{
   private String mEmail, mId;



   private Uri mProfilePicture;

   public User(String email, String id)
   {
      mEmail = email;
      mId = id;
   }

   public User(String email, String id, Uri profilePicture)
   {
      mEmail = email;
      mId = id;
      mProfilePicture = profilePicture;
   }

   public Uri getProfilePicture()
   {
      return mProfilePicture;
   }

   public void setProfilePicture(Uri mProfilePicture)
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
}
