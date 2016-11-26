package adinh03.calpoly.edu.datpic;

import java.io.Serializable;

/**
 * Created by pooki on 11/25/2016.
 */

public class User implements Serializable
{
   private String mEmail, mId;

   private String mProfilePicture;

   public User(String email, String id)
   {
      mEmail = email;
      mId = id;
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
}
