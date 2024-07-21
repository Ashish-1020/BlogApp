package com.example.blogapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private Button continueButton;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        categoryList = new ArrayList<>();
        categoryList.add(new Category(R.drawable.education, "Education"));
        categoryList.add(new Category(R.drawable.gardening, "Gardening"));
        categoryList.add(new Category(R.drawable.healthylifestyle, "Lifestyle"));
        categoryList.add(new Category(R.drawable.man_hiking, "Adventure"));
        categoryList.add(new Category(R.drawable.product_manager, "Management"));
        categoryList.add(new Category(R.drawable.technology, "Technology"));
        categoryList.add(new Category(R.drawable.yoga, "Yoga"));
        categoryList.add(new Category(R.drawable.geopolitics, "Geopolitics"));
        categoryList.add(new Category(R.drawable.sport, "Sports"));




        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(this, categoryList);
        recyclerView.setAdapter(adapter);

        continueButton = findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<Category> followingCategories = adapter.getFollowingCategories();
                uploadToDatabase(followingCategories);
                startActivity(new Intent(CategoryActivity.this, MainActivity.class));
                finish();
            }
        });


    }



    private void uploadToDatabase(List<Category> followingCategories) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("followedCategories");

        Map<String, Object> categoriesMap = new HashMap<>();
        for (Category category : followingCategories) {
            String key = databaseReference.push().getKey();
            if (key != null) {
                Map<String, Object> categoryMap = new HashMap<>();
                categoryMap.put("categoryName", category.getCategoryName());
                categoryMap.put("categoryImage", category.getCategoryImage());
                categoriesMap.put(key, categoryMap);
            }
        }
        databaseReference.setValue(categoriesMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(CategoryActivity.this, "Categories uploaded successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(CategoryActivity.this, "Failed to upload categories", Toast.LENGTH_SHORT).show());
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                       CategoryActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

}