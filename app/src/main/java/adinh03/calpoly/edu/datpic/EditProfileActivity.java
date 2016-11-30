package adinh03.calpoly.edu.datpic;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by tjyung on 11/26/16.
 */

public class EditProfileActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private Button submitButton;
    private EditText oldPass;
    private EditText newPass;
    private EditText confirmPass;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        submitButton = (Button)findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldPass = (EditText)findViewById(R.id.oldPass);
                newPass = (EditText)findViewById(R.id.newPass);
                confirmPass = (EditText)findViewById(R.id.confirmPass);

                if(oldPass.toString().equals("") && newPass.toString().equals("") && confirmPass.equals("")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                    builder.setTitle(R.string.empty_fields)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    System.out.println(oldPass.getText().toString().trim());
                    AuthCredential emailAuthProvider = EmailAuthProvider.getCredential(mFirebaseUser.getEmail().trim(), oldPass.getText().toString().trim());
                    mFirebaseUser.reauthenticate(emailAuthProvider).addOnCompleteListener(EditProfileActivity.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            System.out.println(task.isSuccessful());
                            if (task.isSuccessful()) {
                                if (newPass.getText().toString().equals(confirmPass.getText().toString())) {
                                    mFirebaseUser.updatePassword(newPass.getText().toString());
                                    finish();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                                    builder.setTitle(R.string.new_pass_no_match)
                                            .setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }

                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                                builder.setMessage(task.getException().getMessage())
                                        .setTitle(R.string.pass_no_match)
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });

                }

            }
        });


    }
}
