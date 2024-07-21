package com.example.blogapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class YourBlogsAdapter extends RecyclerView.Adapter<YourBlogsAdapter.ViewHolder> {
    private List<Blog> blogList;
    private Context context;
    private ProfileActivity profileActivity;

    public YourBlogsAdapter(Context context, List<Blog> blogList, ProfileActivity profileActivity) {
        this.context = context;
        this.blogList = blogList;
        this.profileActivity = profileActivity;
    }

    @NonNull
    @Override
    public YourBlogsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewblog_item, parent, false);
        return new YourBlogsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YourBlogsAdapter.ViewHolder holder, int position) {
        Blog blog = blogList.get(position);
        holder.bind(blog);
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView blogImage;
        TextView categoryText, titleText, dateText, viewsText;
        Button readmorebtn, editbtn, deletebtn;

        View cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            blogImage = itemView.findViewById(R.id.blog_image);
            categoryText = itemView.findViewById(R.id.blogCategory_txt);
            titleText = itemView.findViewById(R.id.blogTitle_txt);
            dateText = itemView.findViewById(R.id.blog_UploadDate);
            viewsText = itemView.findViewById(R.id.blog_views);
            cardView = itemView.findViewById(R.id.cardview_blogpost);
            readmorebtn = itemView.findViewById(R.id.readmore_btn);
            editbtn = itemView.findViewById(R.id.edit_btn);
            editbtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey)));
            deletebtn = itemView.findViewById(R.id.delete_btn);
            deletebtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.redbox)));



            deletebtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Blog blog = blogList.get(position);
                    showDeleteConfirmationDialog(blog);
                }
            });
        }

        public void bind(Blog blog) {
            Glide.with(context).load(blog.getImageUrl()).into(blogImage);
            categoryText.setText(blog.getCategory());
            titleText.setText(blog.getTitle());
            dateText.setText(blog.getDate());
            String st = String.valueOf(blog.getViews());
            st = st + "  Views";
            viewsText.setText(st);
        }

        private void showDeleteConfirmationDialog(Blog blog) {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Blog")
                    .setMessage("Are you sure you want to delete this blog?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        profileActivity.deleteBlog(blog.getBlogid());
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }
}
