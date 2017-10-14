package com.delaroystudios.movieapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.delaroystudios.movieapp.adapter.TrailerAdapter;
import com.delaroystudios.movieapp.api.Client;
import com.delaroystudios.movieapp.api.Service;
import com.delaroystudios.movieapp.data.FavoriteContract;
import com.delaroystudios.movieapp.data.FavoriteDbHelper;
import com.delaroystudios.movieapp.model.Movie;
import com.delaroystudios.movieapp.model.Trailer;
import com.delaroystudios.movieapp.model.TrailerResponse;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.delaroystudios.movieapp.data.FavoriteContract.FavoriteEntry.COLUMN_TITLE;

/**
 * Created by delaroy on 5/18/17.
 */
public class DetailActivity extends AppCompatActivity {
    TextView nameOfMovie, plotSynopsis, userRating, releaseDate;
    ImageView imageView;
    private RecyclerView recyclerView;
    private TrailerAdapter adapter;
    private List<Trailer> trailerList;
    private FavoriteDbHelper favoriteDbHelper;
    private Movie favorite;
    private final AppCompatActivity activity = DetailActivity.this;
    private SQLiteDatabase mDb;

    Movie movie;
    String thumbnail, movieName, synopsis, rating, dateOfRelease;
    int movie_id;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //TODO
        FavoriteDbHelper dbHelper = new FavoriteDbHelper(this);
        mDb = dbHelper.getWritableDatabase();


        imageView = (ImageView) findViewById(R.id.thumbnail_image_header);
       // nameOfMovie = (TextView) findViewById(R.id.title);
        plotSynopsis = (TextView) findViewById(R.id.plotsynopsis);
        userRating = (TextView) findViewById(R.id.userrating);
        releaseDate = (TextView) findViewById(R.id.releasedate);

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra("movies")){

            movie = getIntent().getParcelableExtra("movies");

            thumbnail = movie.getPosterPath();
            movieName = movie.getOriginalTitle();
            synopsis = movie.getOverview();
            rating = Double.toString(movie.getVoteAverage());
            dateOfRelease = movie.getReleaseDate();
            movie_id = movie.getId();

            String poster = "https://image.tmdb.org/t/p/w500" + thumbnail;

            Glide.with(this)
                    .load(poster)
                    .placeholder(R.drawable.load)
                    .into(imageView);

          //  nameOfMovie.setText(movieName);
            plotSynopsis.setText(synopsis);
            userRating.setText(rating);
            releaseDate.setText(dateOfRelease);

       //TODO
            ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar)).setTitle(movieName);

        }else{
            Toast.makeText(this, "No API Data", Toast.LENGTH_SHORT).show();
        }

       MaterialFavoriteButton materialFavoriteButton = (MaterialFavoriteButton) findViewById(R.id.favorite_button);

       /*SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        materialFavoriteButton.setOnFavoriteChangeListener(
                new MaterialFavoriteButton.OnFavoriteChangeListener(){
                    @Override
                    public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite){
                        if (favorite){
                            SharedPreferences.Editor editor = getSharedPreferences("com.delaroystudios.movieapp.DetailActivity", MODE_PRIVATE).edit();
                            editor.putBoolean("Favorite Added", true);
                            editor.commit();
                            saveFavorite();
                            Snackbar.make(buttonView, "Added to Favorite",
                                    Snackbar.LENGTH_SHORT).show();
                        }else{
                            int movie_id = getIntent().getExtras().getInt("id");
                            favoriteDbHelper = new FavoriteDbHelper(DetailActivity.this);
                            favoriteDbHelper.deleteFavorite(movie_id);

                            SharedPreferences.Editor editor = getSharedPreferences("com.delaroystudios.movieapp.DetailActivity", MODE_PRIVATE).edit();
                            editor.putBoolean("Favorite Removed", true);
                            editor.commit();
                            Snackbar.make(buttonView, "Removed from Favorite",
                                    Snackbar.LENGTH_SHORT).show();
                        }

                    }
                }
        );
*/
        if (Exists(movieName)){
            materialFavoriteButton.setFavorite(true);
            materialFavoriteButton.setOnFavoriteChangeListener(
                    new MaterialFavoriteButton.OnFavoriteChangeListener() {
                        @Override
                        public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                            if (favorite == true) {
                                saveFavorite();
                                Snackbar.make(buttonView, "Added to Favorite",
                                        Snackbar.LENGTH_SHORT).show();
                            } else {
                                favoriteDbHelper = new FavoriteDbHelper(DetailActivity.this);
                                favoriteDbHelper.deleteFavorite(movie_id);
                                Snackbar.make(buttonView, "Removed from Favorite",
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });


        }else {
            materialFavoriteButton.setOnFavoriteChangeListener(
                    new MaterialFavoriteButton.OnFavoriteChangeListener() {
                        @Override
                        public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                            if (favorite == true) {
                                saveFavorite();
                                Snackbar.make(buttonView, "Added to Favorite",
                                        Snackbar.LENGTH_SHORT).show();
                            } else {
                                int movie_id = getIntent().getExtras().getInt("id");
                                favoriteDbHelper = new FavoriteDbHelper(DetailActivity.this);
                                favoriteDbHelper.deleteFavorite(movie_id);
                                Snackbar.make(buttonView, "Removed from Favorite",
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });


        }

        initViews();

    }

    public boolean Exists(String searchItem) {

        String[] projection = {
                FavoriteContract.FavoriteEntry._ID,
                FavoriteContract.FavoriteEntry.COLUMN_MOVIEID,
                FavoriteContract.FavoriteEntry.COLUMN_TITLE,
                FavoriteContract.FavoriteEntry.COLUMN_USERRATING,
                FavoriteContract.FavoriteEntry.COLUMN_POSTER_PATH,
                FavoriteContract.FavoriteEntry.COLUMN_PLOT_SYNOPSIS

        };
        String selection = FavoriteContract.FavoriteEntry.COLUMN_TITLE + " =?";
        String[] selectionArgs = { searchItem };
        String limit = "1";

        Cursor cursor = mDb.query(FavoriteContract.FavoriteEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }


    private void initViews(){
        trailerList = new ArrayList<>();
        adapter = new TrailerAdapter(this, trailerList);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view1);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        loadJSON();

    }

    private void loadJSON(){

        try{
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()){
                Toast.makeText(getApplicationContext(), "Please obtain your API Key from themoviedb.org", Toast.LENGTH_SHORT).show();
                return;
            }
            Client Client = new Client();
            Service apiService = Client.getClient().create(Service.class);
            Call<TrailerResponse> call = apiService.getMovieTrailer(movie_id, BuildConfig.THE_MOVIE_DB_API_TOKEN);
            call.enqueue(new Callback<TrailerResponse>() {
                @Override
                public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {
                    List<Trailer> trailer = response.body().getResults();
                    recyclerView.setAdapter(new TrailerAdapter(getApplicationContext(), trailer));
                    recyclerView.smoothScrollToPosition(0);
                }

                @Override
                public void onFailure(Call<TrailerResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(DetailActivity.this, "Error fetching trailer data", Toast.LENGTH_SHORT).show();

                }
            });

        }catch (Exception e){
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void saveFavorite(){
        favoriteDbHelper = new FavoriteDbHelper(activity);
        favorite = new Movie();

        Double rate = movie.getVoteAverage();


        favorite.setId(movie_id);
        favorite.setOriginalTitle(movieName);
        favorite.setPosterPath(thumbnail);
        favorite.setVoteAverage(rate);
        favorite.setOverview(synopsis);

        favoriteDbHelper.addFavorite(favorite);
    }


}
