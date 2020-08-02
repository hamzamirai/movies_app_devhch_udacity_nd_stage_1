package com.devhch.mirai.moviesapp_stage1.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.devhch.mirai.moviesapp_stage1.DetailActivity;
import com.devhch.mirai.moviesapp_stage1.R;
import com.devhch.mirai.moviesapp_stage1.model.Movie;

import java.util.List;


/**
 * Created By Hamza Chaouki [MiraiDev].
 * On 7/5/2020
 */
public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {

    private static Context mContext;
    private static List<Movie> movieList;

    public MoviesAdapter(Context mContext, List<Movie> movieList) {
        this.mContext = mContext;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public MoviesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_card, parent, false);
        return new MyViewHolder(v);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull MoviesAdapter.MyViewHolder holder, int position) {
        holder.title.setText(movieList.get(position).getOriginalTitle());

        String vote = Double.toString(movieList.get(position).getVoteAverage());
        holder.userRating.setText(vote);

        Glide.with(mContext)
                .load(movieList.get(position).getPosterPath())
                .placeholder(R.drawable.loading)
                .into(holder.thumbnail);

    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView title, userRating;
        public ImageView thumbnail;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            userRating = itemView.findViewById(R.id.user_rating);
            thumbnail = itemView.findViewById(R.id.thumbnail);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if ( pos != RecyclerView.NO_POSITION) {
                    Movie clickedDataItem = movieList.get(pos);
                    Intent intent = new Intent(mContext, DetailActivity.class);
                    intent.putExtra("original_title", movieList.get(pos).getOriginalTitle());
                    intent.putExtra("path_poster", movieList.get(pos).getPosterPath());
                    intent.putExtra("overview", movieList.get(pos).getOverview());
                    intent.putExtra("vote_average", Double.toString(movieList.get(pos).getVoteAverage()));
                    intent.putExtra("release_date", movieList.get(pos).getReleaseDate());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    //Toast.makeText(v.getContext(), "You clicked " + clickedDataItem.getOriginalTitle(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

