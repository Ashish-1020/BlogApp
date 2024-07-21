package com.example.blogapp;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {


    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Blog");


    private DatabaseReference userReference;


    private RecyclerView recyclerView;
    private BlogAdapter blogAdapter;
    private List<Blog> blogList;

    private FloatingActionButton addBlogButton;
    private CircleImageView profilePic;
    private String profileImageUrl;
    private ImageButton menubtn;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchblog;
    private List<String> followedCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        blogList = new ArrayList<>();
        blogAdapter = new BlogAdapter(this,blogList);
        recyclerView.setAdapter(blogAdapter);

        addBlogButton = findViewById(R.id.floatingActionButton);
        profilePic = findViewById(R.id.profileImage_blogger);


        userReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        menubtn=findViewById(R.id.menu_mainActivitybtn);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        searchblog=findViewById(R.id.searchView);
        searchblog.setFocusable(true);
        searchblog.setFocusableInTouchMode(true);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainActivity.this,ProfileActivity.class);
                startActivity(i);
            }
        });


        addBlogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddBlogActivity.class);
                startActivity(intent);
            }
        });

        loadUserProfileImage();
        setupNavigationDrawer();
        swipeRefreshLayout.setOnRefreshListener(this::loadBlogs);
        setupSearchBlog();


    }

    private void setupSearchBlog() {
        searchblog.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterBlogs(newText);
                return false;
            }
        });

        searchblog.setOnTouchListener((v, event) -> {
            searchblog.setIconified(false);
            return false;
        });
    }

    private void filterBlogs(String text) {
        List<Blog> filteredList = new ArrayList<>();
        for (Blog blog : blogList) {
            if (blog.getTitle().toLowerCase().contains(text.toLowerCase()) ||
                    blog.getDescription().toLowerCase().contains(text.toLowerCase()) ||
                    blog.getUserName().toLowerCase().contains(text.toLowerCase()) ||
                    blog.getCategory().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(blog);
            }
        }
        blogAdapter.filterList(filteredList);
    }
    private void setupNavigationDrawer() {
        menubtn.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
        });

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if(id==R.id.home){
                    drawerLayout.closeDrawer(navigationView);
                }else if(id==R.id.profile){
                    Intent i=new Intent(MainActivity.this,ProfileActivity.class);
                    startActivity(i);
                    finish();
                }else if(id==R.id.savedposts){
                    Intent i=new Intent(MainActivity.this,SavedblogsActivity.class);
                    startActivity(i);
                    finish();
                }else if(id==R.id.category){
                    Intent i=new Intent(MainActivity.this,CategoryActivity.class);
                    startActivity(i);
                    finish();
                }else if(id==R.id.aboutus){
                    Intent i=new Intent(MainActivity.this,AboutusActivity.class);
                    startActivity(i);
                    finish();
                }else if(id==R.id.logout){

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Warning");
                    builder.setMessage("Are sure you want to logout.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
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
                        Glide.with(MainActivity.this).load(profileImageUrl).into(profilePic);
                    }
                    for (DataSnapshot categorySnapshot : snapshot.child("followedCategories").getChildren()) {
                        String categoryName = categorySnapshot.child("categoryName").getValue(String.class);
                        followedCategories.add(categoryName);
                    }
                    loadBlogs();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
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
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    String category = snapshot.child("category").getValue(String.class);
                    String title = snapshot.child("title").getValue(String.class);
                    Long viewsLong = snapshot.child("views").getValue(Long.class);
                    String userName = snapshot.child("userName").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    Long timestampLong = snapshot.child("timeadded").getValue(Long.class);
                    String blogId = snapshot.child("blogId").getValue(String.class);
                    long views = viewsLong != null ? viewsLong : 0;
                    long timestamp = timestampLong != null ? timestampLong : System.currentTimeMillis(); // default to current time if null

                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    String formattedDate = sdf.format(new Date(timestamp));

                    Blog blog = new Blog(blogId, imageUrl, category, title, formattedDate, views,timestampLong, userName, description);

                    if (followedCategories.contains(category)){
                        sortedBlogList.add(blog);
                        count++;}
                    if (count == 200) {
                        break;
                    }
                }
                Collections.sort(sortedBlogList, new Comparator<Blog>() {
                    @Override
                    public int compare(Blog b1, Blog b2) {
                        return Long.compare(b2.getTimestamp(), b1.getTimestamp());
                    }
                });
                for (Blog blog : sortedBlogList) {
                    int randomIndex = (int) (Math.random() * (tempList.size() + 1));
                    tempList.add(randomIndex, blog);
                }
                blogList.addAll(tempList);
                blogAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load blogs", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }



}