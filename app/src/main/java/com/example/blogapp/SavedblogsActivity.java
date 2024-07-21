package com.example.blogapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SavedblogsActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference userRef, blogsRef,userReference;
    private List<Blog> blogList;
    private RecyclerView recyclerView;
    private SavedBlogAdapter blogAdapter;
    private List<String> savedBlogIds;
    private String profileImageUrl;
    private CircleImageView profilePic;
    private ImageButton backbtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savedblogs);

        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recyclerview_savedblogs);
        profilePic=findViewById(R.id.profileImage_blogger);
        backbtn=findViewById(R.id.backbtn_aboutus);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        user = firebaseAuth.getCurrentUser();

        userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("savedblogs");
        blogsRef = FirebaseDatabase.getInstance().getReference("blogs");
        userReference=FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        blogList = new ArrayList<>();
        savedBlogIds = new ArrayList<>();
        blogAdapter = new SavedBlogAdapter(this, blogList); // Pass ProfileActivity if needed
        recyclerView.setAdapter(blogAdapter);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(SavedblogsActivity.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        loadSavedBlogIds();
        loadUserProfileImage();
    }

    private void loadSavedBlogIds() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot blogSnapshot : snapshot.getChildren()) {
                        savedBlogIds.add(blogSnapshot.getKey());
                    }
                    loadBlogs();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SavedblogsActivity.this, "Failed to load saved blogs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProfileImage() {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(SavedblogsActivity.this).load(profileImageUrl).into(profilePic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SavedblogsActivity.this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadBlogs() {
        for (String blogId : savedBlogIds) {
            blogsRef.child(blogId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Blog blog = snapshot.getValue(Blog.class);
                        if (blog != null) {
                            blogList.add(blog);
                            blogAdapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SavedblogsActivity.this, "Failed to load blog details", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
