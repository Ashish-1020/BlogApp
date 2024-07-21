package com.example.blogapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SavedBlogAdapter extends RecyclerView.Adapter<SavedBlogAdapter.ViewHolder1> {

    private List<Blog> blogList;
    private Context context;
    private DatabaseReference viewsRef;

    public SavedBlogAdapter(Context context, List<Blog> blogList) {
        this.context = context;
        this.blogList = blogList;
        this.viewsRef = FirebaseDatabase.getInstance().getReference().child("blogs");
    }

    @NonNull
    @Override
    public ViewHolder1 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_item, parent, false);
        return new ViewHolder1(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder1 holder, int position) {
        Blog blog = blogList.get(position);
        holder.bind(blog);




    }




    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public class ViewHolder1 extends RecyclerView.ViewHolder {
        ImageView blogImage;
        TextView categoryText, titleText, dateText, viewsText;

        View cardView;

        public ViewHolder1(@NonNull View itemView) {
            super(itemView);
            blogImage = itemView.findViewById(R.id.blog_image);
            categoryText = itemView.findViewById(R.id.blogCategory_txt);
            titleText = itemView.findViewById(R.id.blogTitle_txt);
            dateText = itemView.findViewById(R.id.blog_UploadDate);
            viewsText = itemView.findViewById(R.id.blog_views);
            cardView = itemView.findViewById(R.id.cardview_blogpost);

        }

        public void bind(Blog blog) {
            Glide.with(context).load(blog.getImageUrl()).into(blogImage);
            categoryText.setText(blog.getCategory());
            titleText.setText(blog.getTitle());
            dateText.setText(blog.getDate());
            String viewsString = String.valueOf(blog.getViews()) + "  Views";
            viewsText.setText(viewsString);
        }
    }
}
