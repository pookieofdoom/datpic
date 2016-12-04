package adinh03.calpoly.edu.datpic;

import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.ImageView;

import java.io.Serializable;

/**
 * Created by Dylan on 10/30/16.
 */

public class CommentEntry implements Serializable{
   public void CommentEntry() {

   }
   String text;
   private String nickname;
   private Uri profilePic;

   public void setText(String s) {
      text = s;
   }
   public String getText() {
      return text;
   }

   public void setNickname(String s) {nickname = s;}
   public String getNickname() {return nickname;}

   public void setProfilePic(Uri imageView) {profilePic = imageView;}
   public Uri getProfilePic() {return profilePic;}
}
