package adinh03.calpoly.edu.datpic;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Comment;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Dylan on 10/30/16.
 */

public class AddCommentActivity extends AppCompatActivity {
    EditText inputComment;
    Button submitCommentButton;
    ArrayList<CommentEntry> commentList;
    RecyclerView commentRecyclerView;
    private CommentAdapter adapter;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
    private FirebaseDatabase mDataBase;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.comment_activity);

        commentList = (ArrayList<CommentEntry>) getLastCustomNonConfigurationInstance();
        if (commentList == null) {
            commentList = new ArrayList<>();
        }

        inputComment = (EditText) findViewById(R.id.InputCommentTextBox);
        submitCommentButton = (Button) findViewById(R.id.CommentSubmitButton);
        commentRecyclerView = (RecyclerView) findViewById(R.id.CommentRecylerView);

        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        final int clickedImageIndex = getIntent().getIntExtra("clickedImageIndex", -1);



        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Log.d("DEBUG", "User is: " + userSnapshot.getKey());
                    for (DataSnapshot imageShots : userSnapshot.getChildren()) {
                        Log.d("DEBUG", "Image is: " + imageShots.getKey());

                        if (imageShots.getKey().toString().equals(StaticEntryList.getInstance().getMap().get(StaticEntryList
                              .getInstance().getEntry(clickedImageIndex).getUri().toString()))) {


                            for (DataSnapshot commentShots : imageShots.child("comments").getChildren()) {
                                CommentEntry insert = new CommentEntry();
                                insert.setText(commentShots.getValue().toString());
//                                commentList.clear();
                                if (!commentList.contains(insert)) {
                                    commentList.add(insert);
                                }
                                adapter.notifyDataSetChanged();

                            }
                            populateAllComments();
                        }
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //after update the list
        adapter = new CommentAdapter(commentList);
        System.out.println("adapter: " + adapter.mList.size());
        commentRecyclerView.setAdapter(adapter);


        inputComment.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    System.out.println("Before" + inputComment.getText().toString());

                    if ((keyCode == KeyEvent.KEYCODE_ENTER) && isEmptyString(inputComment)) {
                        System.out.println(inputComment.getText().toString());
                        addComment(inputComment, clickedImageIndex);
                        //adds comment in database for photo
                        inputComment.setText(null);
                    }
                }
                return false;
            }
        });

        submitCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEmptyString(inputComment)) {
                    addComment(inputComment, clickedImageIndex);
                    inputComment.setText(null);
                }
            }
        });

    }

    protected void populateAllComments() {

    }

    protected void addComment(EditText text, int clickedImageIndex) {

        String path = StaticEntryList.getInstance().getMap().get(StaticEntryList.getInstance().getEntry(clickedImageIndex).getUri().toString());
        mDataBase.getInstance().getReference("users").child(mFirebaseUser.getUid()).child(path).child("comments").push().setValue(text.getText().toString());
        CommentEntry newEntry = new CommentEntry();
        newEntry.setText(text.getText().toString());
        commentList.add(newEntry);
        adapter.notifyDataSetChanged();
    }

    protected boolean isEmptyString(EditText text) {
        String input = text.getText().toString();
        return input.trim().length() != 0 && !TextUtils.isEmpty(input);
    }



    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
    }

    public class CommentAdapter extends RecyclerView.Adapter<CommentHolder> {
        private ArrayList<CommentEntry> mList;
        public CommentAdapter(ArrayList<CommentEntry> list) {
            mList = list;
        }

        @Override
        public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CommentHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));

        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.comment_entry;
        }

        @Override
        public void onBindViewHolder(CommentHolder holder, int position) {
            holder.bind(mList.get(position));
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }

    public static class CommentHolder extends RecyclerView.ViewHolder {
        private TextView commentTextView;
        public CommentHolder(View view) {
            super(view);
            commentTextView = (TextView) view.findViewById(R.id.commentView);
        }

        public void bind(final CommentEntry commentEntry) {
            commentTextView.setText(commentEntry.getText());
        }


    }
}
