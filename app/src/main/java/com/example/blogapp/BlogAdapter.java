package com.example.blogapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.ViewHolder> {

    private List<Blog> blogList;
    private Context context;
    private DatabaseReference viewsRef;

    public BlogAdapter(Context context, List<Blog> blogList) {
        this.context = context;
        this.blogList = blogList;
        this.viewsRef = FirebaseDatabase.getInstance().getReference().child("blogs");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Blog blog = blogList.get(position);
        holder.bind(blog);



        holder.cardView.setOnClickListener(v -> {
            increaseViews(blog,position);
            Intent intent = new Intent(context, OpenBlogActivity.class);
            intent.putExtra("title", blog.getTitle());
            intent.putExtra("category", blog.getCategory());
            intent.putExtra("date", blog.getDate());
            intent.putExtra("views", blog.getViews());
            intent.putExtra("imageUrl", blog.getImageUrl());
            intent.putExtra("userName",blog.getUserName());
            intent.putExtra("description",blog.getDescription());
            intent.putExtra("blogId",blog.getBlogid());
            context.startActivity(intent);
        });
    }
    private void increaseViews(Blog blog, int position) {
        long currentViews = blog.getViews();
        long newViews = currentViews + 1;

        String blogId = blog.getBlogid();
        if (blogId != null) {
            viewsRef.child(blogId).child("views").setValue(newViews)
                    .addOnSuccessListener(aVoid -> {
                        blogList.get(position).setViews(newViews);
                        notifyItemChanged(position);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update views count", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "Blog ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public void filterList(List<Blog> filteredList) {
        this.blogList = filteredList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView blogImage;
        TextView categoryText, titleText, dateText, viewsText;


        View cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            blogImage = itemView.findViewById(R.id.blog_image);
            categoryText = itemView.findViewById(R.id.blogCategory_txt);
            titleText = itemView.findViewById(R.id.blogTitle_txt);
            dateText = itemView.findViewById(R.id.blog_UploadDate);
            viewsText = itemView.findViewById(R.id.blog_views);
            cardView=itemView.findViewById(R.id.cardview_blogpost);

        }

        public void bind(Blog blog) {
            Glide.with(context).load(blog.getImageUrl()).into(blogImage);
            categoryText.setText(blog.getCategory());
            titleText.setText(blog.getTitle());
            dateText.setText(blog.getDate());
            String st=String.valueOf(blog.getViews());
            st=st+"  Views";
            viewsText.setText(st);

        }
    }
}
