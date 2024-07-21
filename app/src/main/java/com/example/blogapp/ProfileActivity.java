package com.example.blogapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference userRef;
    private EditText nametxt;

    private List<Blog> blogList;
    private RecyclerView recyclerView;
    private YourBlogsAdapter blogAdapter;
    private ImageButton backbtn;

    private String name;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        firebaseAuth = FirebaseAuth.getInstance();
        nametxt = findViewById(R.id.fullname_edit_text);
        recyclerView = findViewById(R.id.recyler_profile);
        backbtn=findViewById(R.id.backbtn_aboutus);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        blogList = new ArrayList<>();
        blogAdapter = new YourBlogsAdapter(this, blogList,this);
        recyclerView.setAdapter(blogAdapter);

        user = firebaseAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());


        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(ProfileActivity.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        });
        loadUserProfile();
        loadBlogs();
    }

    public void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    name = snapshot.child("firstName").getValue(String.class);
                    if (name != null && !name.isEmpty()) {
                        nametxt.setText(name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load user name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBlogs() {
        DatabaseReference blogsRef = FirebaseDatabase.getInstance().getReference("blogs");
        blogsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                blogList.clear();
                List<Blog> tempList = new ArrayList<>();
                List<Blog> sortedBlogList = new ArrayList<>();
                int count = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String username = snapshot.child("userName").getValue(String.class);

                    if (username != null && username.equals(name)) {
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                        String category = snapshot.child("category").getValue(String.class);
                        String title = snapshot.child("title").getValue(String.class);
                        Long viewsLong = snapshot.child("views").getValue(Long.class); // Long object to handle null
                        String userName = snapshot.child("userName").getValue(String.class);
                        String description = snapshot.child("description").getValue(String.class);
                        Long timestampLong = snapshot.child("timeadded").getValue(Long.class);
                        String blogId = snapshot.child("blogId").getValue(String.class); // Assuming you have a blogId field

                        // Handle null values
                        long views = viewsLong != null ? viewsLong : 0; // default to 0 if null
                        long timestamp = timestampLong != null ? timestampLong : System.currentTimeMillis(); // default to current time if null

                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                        String formattedDate = sdf.format(new Date(timestamp));

                        Blog blog = new Blog(blogId, imageUrl, category, title, formattedDate, views, timestampLong, userName, description);

                        sortedBlogList.add(blog);
                        count++;
                        if (count == 200) {
                            break;
                        }
                    }
                }
                tempList.addAll(sortedBlogList);
                blogList.addAll(tempList);

                blogAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Failed to load blogs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteBlog(String blogId) {

        DatabaseReference blogRef = FirebaseDatabase.getInstance().getReference("blogs").child(blogId);
        blogRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ProfileActivity.this, "Blog deleted", Toast.LENGTH_SHORT).show();
                // Reload the blogs after deletion
                loadBlogs();
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to delete blog", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
