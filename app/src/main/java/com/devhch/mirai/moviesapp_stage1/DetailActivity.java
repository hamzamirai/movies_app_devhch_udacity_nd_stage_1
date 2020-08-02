package com.devhch.mirai.moviesapp_stage1;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {
    TextView nameOfMovie, plotSynopsis, userRating, releaseDate;
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setElevation(0);
        actionBar.setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.thumbnail_image_header);
        nameOfMovie = findViewById(R.id.movie_title);
        plotSynopsis = findViewById(R.id.plotsynopsis);
        userRating = findViewById(R.id.user_rating);
        releaseDate = findViewById(R.id.release_date);

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.getExtras() != null) {
            Bundle bundle =  intentThatStartedThisActivity.getExtras();
            String thumbnail = bundle.getString("path_poster");
            String movieName = bundle.getString("original_title");
            String synopsis = bundle.getString("overview");
            String rating = bundle.getString("vote_average");
            String dateOfRelease = bundle.getString("release_date");

            Glide.with(this)
                    .load(thumbnail)
                    .placeholder(R.drawable.loading)
                    .into(imageView);

            nameOfMovie.append(" " + movieName);
            plotSynopsis.append(" " +synopsis);
            userRating.append(" " +rating);
            releaseDate.append(" " +dateOfRelease);
        } else {
            Toast.makeText(this, "No API Data", Toast.LENGTH_SHORT).show();
        }
    }
}
