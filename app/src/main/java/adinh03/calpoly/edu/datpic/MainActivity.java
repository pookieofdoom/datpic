package adinh03.calpoly.edu.datpic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity{
   private FirebaseAuth mFirebaseAuth;
   private FirebaseUser mFirebaseUser;


    @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

       // Initialize Firebase Auth
       mFirebaseAuth = FirebaseAuth.getInstance();
       mFirebaseUser = mFirebaseAuth.getCurrentUser();

       if (mFirebaseUser == null) {
           // Not logged in, launch the Log In activity
           loadLogInView();
       }
   }

   private void loadLogInView() {
      Intent intent = new Intent(this, LogInActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(intent);
   }
}
