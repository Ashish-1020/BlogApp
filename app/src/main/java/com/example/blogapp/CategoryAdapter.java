package com.example.blogapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categoryList;
    private Context context;

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }
    @NonNull
    @Override
    public CategoryAdapter.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.categoryImage.setImageResource(category.getCategoryImage());
        holder.categoryName.setText(category.getCategoryName());
        holder.followButton.setText(category.isFollowing() ? "Following" : "Follow");

        holder.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFollowing = category.isFollowing();
                category.setFollowing(!isFollowing);
                holder.followButton.setText(category.isFollowing() ? "Following" : "Follow");
                holder.followButton.setBackgroundColor(ContextCompat.getColor(context, category.isFollowing() ? R.color.text_background : R.color.blue_btn));
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }


    public List<Category> getFollowingCategories() {
        List<Category> followingCategories = new ArrayList<>();
        for (Category category : categoryList) {
            if (category.isFollowing()) {
                followingCategories.add(category);
            }
        }
        return followingCategories;
    }
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImage;
        TextView categoryName;
        Button followButton;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.category_image);
            categoryName = itemView.findViewById(R.id.category_name);
            followButton = itemView.findViewById(R.id.follow_btn);
        }
    }
}