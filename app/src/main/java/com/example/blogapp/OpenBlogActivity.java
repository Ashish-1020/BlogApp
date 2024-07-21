package com.example.blogapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class OpenBlogActivity extends AppCompatActivity {

    private DatabaseReference userRef;
    private DatabaseReference savedBlogsRef;

    private ImageButton menu, saveArticle, backbtn;
    private TextView title, description_blog, bloggerName, date;

    private ImageView blogimage;
    private CircleImageView bloggerpic;
    private FirebaseAuth mAuth;
    private boolean isSaved = false;
    String blogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_blog);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("users");

        menu = findViewById(R.id.menu_openblog_btn);
        saveArticle = findViewById(R.id.saveBlog_btn);
        backbtn = findViewById(R.id.gotomain_btn);

        title = findViewById(R.id.blog_title);
        description_blog = findViewById(R.id.content_blog);
        blogimage = findViewById(R.id.image_blog);
        bloggerName = findViewById(R.id.name_blogger);
        date = findViewById(R.id.dateUploaded_blog);
        bloggerpic = findViewById(R.id.profileImage_blogger);

        backbtn.setOnClickListener(v -> {
            Intent i = new Intent(OpenBlogActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        });

        saveArticle.setOnClickListener(v -> {
            if (isSaved) {
                removeFromSaved();
            } else {
                saveToSaved();
            }
        });

        String titleText = getIntent().getStringExtra("title");
        String categoryText = getIntent().getStringExtra("category");
        String dateText = getIntent().getStringExtra("date");
        String description = getIntent().getStringExtra("description");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String username = getIntent().getStringExtra("userName");
         blogId = getIntent().getStringExtra("blogId");

        if (blogId == null || blogId.isEmpty()) {
            // Handle case where blogId is null or empty
            Toast.makeText(this, "Invalid blog", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            title.setText(titleText);
            date.setText(dateText);
            bloggerName.setText(username);
            description_blog.setText(description);
            Glide.with(this).load(imageUrl).into(blogimage);

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                savedBlogsRef = userRef.child(currentUser.getUid()).child("savedblogs");

                savedBlogsRef.child(blogId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            isSaved = true;
                            saveArticle.setImageResource(R.drawable.bookmark_filled);
                        } else {
                            isSaved = false;
                            saveArticle.setImageResource(R.drawable.bookmark_outline);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("OpenBlogActivity", "Failed to read savedblogs data", error.toException());
                    }
                });

                Query userQuery = userRef.orderByChild("firstName").equalTo(username);
                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String.class);
                                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                    Glide.with(OpenBlogActivity.this).load(profileImageUrl).into(bloggerpic);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("DetailActivity", "Failed to read user data", error.toException());
                    }
                });
            }
        }
    }

    private void saveToSaved() {
        savedBlogsRef.child(blogId).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    isSaved = true;
                    saveArticle.setImageResource(R.drawable.bookmark_filled);
                    Toast.makeText(OpenBlogActivity.this, "Blog saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OpenBlogActivity.this, "Failed to save blog", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeFromSaved() {
        savedBlogsRef.child(blogId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    isSaved = false;
                    saveArticle.setImageResource(R.drawable.bookmark_outline);
                    Toast.makeText(OpenBlogActivity.this, "Blog removed from saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OpenBlogActivity.this, "Failed to remove blog from saved", Toast.LENGTH_SHORT).show();
                });
    }
}
