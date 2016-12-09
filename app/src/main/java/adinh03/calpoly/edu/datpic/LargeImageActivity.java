package adinh03.calpoly.edu.datpic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.R.attr.targetActivity;

/**
 * Created by tjyung on 12/7/16.
 */

public class LargeImageActivity extends AppCompatActivity {

    String uri;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        //postponeEnterTransition();
        super.onCreate(savedInstanceState);
        Bundle bund = this.getIntent().getExtras();
        uri = bund.getString("pic");


        setContentView(R.layout.comment_post_image);

        image = (ImageView)findViewById(R.id.profilePic);
        Picasso.with(LargeImageActivity.this).load(Uri.parse(uri)).memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE).fit().into(image, new Callback() {
            @Override
            public void onSuccess() {
                System.out.println("yay");
                //scheduleStartPostponedTransition(image);
            }

            @Override
            public void onError() {

            }
        });
        image.setTransitionName(getString(R.string.transition_string));


    }

    private void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                });
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        // Postpone the shared element return transition.
        postponeEnterTransition();


        Picasso.with(LargeImageActivity.this).load(Uri.parse(uri)).into(image, new Callback() {
            @Override
            public void onSuccess() {
                System.out.println("yay");
                //scheduleStartPostponedTransition(image);
            }

            @Override
            public void onError() {

            }
        });
    }

}
