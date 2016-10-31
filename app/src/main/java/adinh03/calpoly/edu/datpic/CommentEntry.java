package adinh03.calpoly.edu.datpic;

import java.io.Serializable;

/**
 * Created by Dylan on 10/30/16.
 */

public class CommentEntry implements Serializable{
   public void CommentEntry() {

   }
   String text;

   public void setText(String s) {
      text = s;
   }
   public String getText() {
      return text;
   }
}
