package com.example.blogapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private CircleImageView profilePic;
    private ImageButton imageselector, back_btn;
    private EditText editTextRegisterFullName, editTextEmail, editTextPassword;

    private Uri imageUri;
    private TextView redirectTo_login;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        back_btn = findViewById(R.id.gotomain_btn);
        redirectTo_login = findViewById(R.id.login_page_redirect);
        profilePic = findViewById(R.id.profile_pic);
        imageselector = findViewById(R.id.imageselector_btn);
        editTextRegisterFullName = findViewById(R.id.fullname_edit_text);
        editTextEmail = findViewById(R.id.email_edit_text);
        editTextPassword = findViewById(R.id.password_edit_text);
        mAuth = FirebaseAuth.getInstance();

        // State change in user
        authStateListener = firebaseAuth -> {
            currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {

            } else {

            }
        };

        redirectTo_login.setOnClickListener(v -> {
            Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        });

        imageselector.setOnClickListener(v -> openFileChooser());

        Button buttonRegister = findViewById(R.id.continue_button);
        buttonRegister.setOnClickListener(v -> {
            String textfullname = editTextRegisterFullName.getText().toString();
            String textemail = editTextEmail.getText().toString();
            String textpassword = editTextPassword.getText().toString();

            if (TextUtils.isEmpty(textfullname)) {
                Toast.makeText(RegisterActivity.this, "Please enter the full name", Toast.LENGTH_LONG).show();
                editTextRegisterFullName.setError("Full name is required");
                editTextRegisterFullName.requestFocus();
            } else if (TextUtils.isEmpty(textemail)) {
                Toast.makeText(RegisterActivity.this, "Please enter the email", Toast.LENGTH_LONG).show();
                editTextEmail.setError("Email is required");
                editTextEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(textemail).matches()) {
                Toast.makeText(RegisterActivity.this, "Please re-enter the email", Toast.LENGTH_LONG).show();
                editTextEmail.setError("Valid Email is required");
                editTextEmail.requestFocus();
            } else if (TextUtils.isEmpty(textpassword)) {
                Toast.makeText(RegisterActivity.this, "Please enter the password", Toast.LENGTH_LONG).show();
                editTextPassword.setError("Password is required");
                editTextPassword.requestFocus();
            } else if (textpassword.length() < 8) {
                Toast.makeText(RegisterActivity.this, "Password should be at least 8 digits", Toast.LENGTH_LONG).show();
                editTextPassword.setError("Password is too weak");
                editTextPassword.requestFocus();
            } else {
                signUpUser();
            }
        });

        back_btn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
            startActivity(intent);
        });
    }

    private void signUpUser() {
        String firstName = editTextRegisterFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (firstName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        uploadImageToFirebase(user, firstName, email);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profilePic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebase(FirebaseUser user, String firstName, String email) {
        if (imageUri != null && user != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_images/" + user.getUid() + ".jpg");

            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        saveUserToDatabase(user.getUid(), firstName, email, downloadUrl);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(RegisterActivity.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(RegisterActivity.this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(RegisterActivity.this, "Image URI or User is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserToDatabase(String userId, String firstName, String email, String imageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", firstName);
        userMap.put("email", email);
        userMap.put("profileImageUrl", imageUrl);

        databaseReference.setValue(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, CategoryActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
