package com.devhch.mirai.moviesapp_stage1;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.devhch.mirai.moviesapp_stage1.adapter.MoviesAdapter;
import com.devhch.mirai.moviesapp_stage1.api.Client;
import com.devhch.mirai.moviesapp_stage1.api.Service;
import com.devhch.mirai.moviesapp_stage1.model.Movie;
import com.devhch.mirai.moviesapp_stage1.model.MoviesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // Get YOur API KEY from themoviedb.org
    private final String KEY_API = "";

    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private SwipeRefreshLayout swipeContainer;
    private ProgressDialog progressDialog;

    private static String LIST_STATE = "list_state";
    private Parcelable savedRecyclerLayoutState;
    private static final String BUNDLE_RECYCLER_LAYOUT = "recycler_layout";
    private ArrayList<Movie> moviesInstance = new ArrayList<>();

    private TextView mEmptyStateTextView;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relativeLayout = findViewById(R.id.relative_layout);
        mEmptyStateTextView = findViewById(R.id.empty_view);
        mEmptyStateTextView.append("\nMake sure that you are connected to the Internet and click here again.");

        swipeContainer = findViewById(R.id.swipe_refresh_layout);
        swipeContainer.setColorSchemeResources(android.R.color.holo_orange_dark);

        if (isNetworkAvailable()) {
            mEmptyStateTextView.setVisibility(View.GONE);
            if (savedInstanceState != null) {
                moviesInstance = savedInstanceState.getParcelableArrayList(LIST_STATE);
                savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
                displayData();
            } else {
                initViews();
            }
        } else {
            relativeLayout.setBackgroundColor( getResources().getColor(R.color.colorPrimary));
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            swipeContainer.setVisibility(View.GONE);
            mEmptyStateTextView.setOnClickListener(v -> {
                if ( isNetworkAvailable()) {
                    swipeContainer.setVisibility(View.VISIBLE);
                    relativeLayout.setBackgroundColor( getResources().getColor(R.color.gray));
                    initViews();
                    mEmptyStateTextView.setVisibility(View.GONE);
                }else {
                    Toast.makeText(MainActivity.this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                }
            });
        }

        swipeContainer.setOnRefreshListener(() -> {
            if ( isNetworkAvailable()) {
                initViews();
                Toast.makeText(MainActivity.this, "Movies Refreshed", Toast.LENGTH_SHORT).show();
            }else {
                mEmptyStateTextView.setVisibility(View.VISIBLE);
                relativeLayout.setBackgroundColor( getResources().getColor(R.color.colorPrimary));
                swipeContainer.setVisibility(View.GONE);
                mEmptyStateTextView.setOnClickListener(v -> {
                    if ( isNetworkAvailable()) {
                        swipeContainer.setVisibility(View.VISIBLE);
                        relativeLayout.setBackgroundColor( getResources().getColor(R.color.gray));
                        initViews();
                        mEmptyStateTextView.setVisibility(View.GONE);
                    }else {
                        Toast.makeText(MainActivity.this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void displayData() {
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new MoviesAdapter(this, moviesInstance);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 6));
        }
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        restoreLayoutManagerPosition();
        adapter.notifyDataSetChanged();
    }

    private void restoreLayoutManagerPosition() {
        if (savedRecyclerLayoutState != null) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            assert layoutManager != null;
            layoutManager.onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    private void initViews() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching movies...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        recyclerView = findViewById(R.id.recycler_view);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 6));
        }

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        loadJSON();
    }

    private void loadJSON() {
        String sortOrder = checkSortOrder();

        if (sortOrder.equals(this.getString(R.string.pref_most_popular))) {
            try {
                if (KEY_API.isEmpty()) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Please obtain API Key firstly from themoviedb.org",
                            Toast.LENGTH_SHORT
                    ).show();
                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                    progressDialog.dismiss();
                    return;
                }

                Client mClient = new Client();
                Service apiService =
                        mClient.getClient().create(Service.class);
                Call<MoviesResponse> call = apiService.getPopularMovies(KEY_API);
                call.enqueue(new Callback<MoviesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MoviesResponse> call, @NonNull Response<MoviesResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                List<Movie> movies = response.body().getResults();
                                moviesInstance.clear();
                                moviesInstance.addAll(movies);
                                recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                                recyclerView.smoothScrollToPosition(0);
                                if (swipeContainer.isRefreshing()) {
                                    swipeContainer.setRefreshing(false);
                                }
                                progressDialog.dismiss();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MoviesResponse> call, @NonNull Throwable t) {
                        Log.d("Error", Objects.requireNonNull(t.getMessage()));
                        Toast.makeText(MainActivity.this, "Error Fetching Data!", Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (Exception e) {
                Log.d("Error", Objects.requireNonNull(e.getMessage()));
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                if (KEY_API.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please obtain API Key firstly from themoviedb.org", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                    return;
                }

                Client mClient = new Client();
                Service apiService =
                        mClient.getClient().create(Service.class);
                Call<MoviesResponse> call = apiService.getTopRatedMovies(KEY_API);
                call.enqueue(new Callback<MoviesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MoviesResponse> call, @NonNull Response<MoviesResponse> response) {
                        assert response.body() != null;
                        List<Movie> movies = response.body().getResults();
                        moviesInstance.clear();
                        moviesInstance.addAll(movies);
                        recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                        recyclerView.smoothScrollToPosition(0);
                        if (swipeContainer.isRefreshing()) {
                            swipeContainer.setRefreshing(false);
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull Call<MoviesResponse> call, @NonNull Throwable t) {
                        Log.d("Error", Objects.requireNonNull(t.getMessage()));
                        Toast.makeText(MainActivity.this, "Error Fetching Data!", Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (Exception e) {
                Log.d("Error", Objects.requireNonNull(e.getMessage()));
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String checkSortOrder() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString(
                this.getString(R.string.pref_sort_order_key),
                this.getString(R.string.pref_most_popular)
        );
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (isNetworkAvailable()) {
            savedInstanceState.putParcelableArrayList(LIST_STATE, moviesInstance);
            savedInstanceState.putParcelable(
                    BUNDLE_RECYCLER_LAYOUT,
                    Objects.requireNonNull(recyclerView.getLayoutManager())
                            .onSaveInstanceState());
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if (isNetworkAvailable()) {
            moviesInstance = savedInstanceState.getParcelableArrayList(LIST_STATE);
            savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if ( isNetworkAvailable()) {
            initViews();
        }
    }
}
