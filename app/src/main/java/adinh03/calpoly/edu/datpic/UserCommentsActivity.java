package adinh03.calpoly.edu.datpic;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by tjyung on 11/26/16.
 */

public class UserCommentsActivity extends AppCompatActivity {
    ArrayList<CommentEntry> commentList;
    RecyclerView commentRecyclerView;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users");
    private CommentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_comment_activity);

        commentList = (ArrayList<CommentEntry>) getLastCustomNonConfigurationInstance();
        if (commentList == null)
        {
            commentList = new ArrayList<>();
        }
        commentRecyclerView = (RecyclerView) findViewById(R.id.userCommentsRecylerView);

        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager
                .VERTICAL, false));

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mRef.addListenerForSingleValueEvent(populateCommentListener());

        adapter = new CommentAdapter(commentList);

        commentRecyclerView.setAdapter(adapter);

    }

    private ValueEventListener populateCommentListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    System.out.println("Data snapshot" + userSnapshot.getKey());
                    System.out.println("UID" + mFirebaseUser.getUid());
                    if(userSnapshot.getKey().equals(mFirebaseUser.getUid())) {
                        System.out.println("UID" + mFirebaseUser.getUid());
                        for (DataSnapshot entrySnapShot : userSnapshot.getChildren()) {

                            if (entrySnapShot.getKey().equals("Entry")) {
                                for (DataSnapshot commentSnapShot : entrySnapShot.getChildren()) {
                                    System.out.println("comments" + commentSnapShot.child("comment").getValue(String.class));
                                    if (commentSnapShot.child("imageData").child("imageLoc").getValue() != null) {

                                        CommentEntry insert = new CommentEntry();
                                        insert.setNickname(commentSnapShot.child("comment").getValue(String.class));
                                        System.out.println("Pic " + commentSnapShot.child("imageLoc").getValue(String.class));
                                        insert.setProfilePic(Uri.parse(commentSnapShot.child("imageData").child("imageLoc").getValue(String.class)));
                                        if (!commentList.contains(insert)) {
                                            commentList.add(insert);
                                            adapter.notifyDataSetChanged();
                                        }

                                    }
                                }
                            }
                        }
                    }

                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };
    }

    public class CommentAdapter extends RecyclerView.Adapter<AddCommentActivity.CommentHolder>
    {
        private ArrayList<CommentEntry> mList;

        public CommentAdapter(ArrayList<CommentEntry> list)
        {
            mList = list;
        }

        @Override
        public AddCommentActivity.CommentHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new AddCommentActivity.CommentHolder(LayoutInflater.from(parent.getContext()).inflate(viewType,
                    parent, false));

        }

        @Override
        public int getItemViewType(int position)
        {
            return R.layout.comment_entry;
        }

        @Override
        public void onBindViewHolder(AddCommentActivity.CommentHolder holder, int position)
        {
            holder.bind(mList.get(position));
            holder.setListener(mList.get(position),UserCommentsActivity.this);
        }


        @Override
        public int getItemCount()
        {
            return mList.size();
        }
    }


}
