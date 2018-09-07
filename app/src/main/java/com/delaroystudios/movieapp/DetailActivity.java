package com.delaroystudios.movieapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.delaroystudios.movieapp.ViewModel.AppExecutors;
import com.delaroystudios.movieapp.adapter.ReviewAdapter;
import com.delaroystudios.movieapp.adapter.TrailerAdapter;
import com.delaroystudios.movieapp.api.Client;
import com.delaroystudios.movieapp.api.Service;
import com.delaroystudios.movieapp.data.FavoriteContract;
import com.delaroystudios.movieapp.data.FavoriteDbHelper;
import com.delaroystudios.movieapp.database.AppDatabase;
import com.delaroystudios.movieapp.database.FavoriteEntry;
import com.delaroystudios.movieapp.model.Movie;
import com.delaroystudios.movieapp.model.Review;
import com.delaroystudios.movieapp.model.ReviewResult;
import com.delaroystudios.movieapp.model.Trailer;
import com.delaroystudios.movieapp.model.TrailerResponse;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView;

import java.io.File;
import java.io.FileOutputStream;
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
    private AppDatabase mDb;
    List<FavoriteEntry> entries = new ArrayList<>();
    boolean exists;

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
        mDb = AppDatabase.getInstance(getApplicationContext());

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

        checkStatus(movieName);
        initViews();

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
        loadReview();
    }

    private void loadJSON(){
        try{
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()){
                Toast.makeText(getApplicationContext(), "Please get your API Key", Toast.LENGTH_SHORT).show();
                return;
            }else {
                Client Client = new Client();
                Service apiService = Client.getClient().create(Service.class);
                Call<TrailerResponse> call = apiService.getMovieTrailer(movie_id, BuildConfig.THE_MOVIE_DB_API_TOKEN);
                call.enqueue(new Callback<TrailerResponse>() {
                    @Override
                    public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                List<Trailer> trailer = response.body().getResults();
                                MultiSnapRecyclerView recyclerView = (MultiSnapRecyclerView) findViewById(R.id.recycler_view1);
                                LinearLayoutManager firstManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
                                recyclerView.setLayoutManager(firstManager);
                                recyclerView.setAdapter(new TrailerAdapter(getApplicationContext(), trailer));
                                recyclerView.smoothScrollToPosition(0);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<TrailerResponse> call, Throwable t) {
                        Log.d("Error", t.getMessage());
                        Toast.makeText(DetailActivity.this, "Error fetching trailer", Toast.LENGTH_SHORT).show();

                    }
                });
            }

        } catch(Exception e){
            Log.d("Error", e.getMessage());
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
        }
    }


    //TODO
    private void loadReview(){
        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please get your API Key", Toast.LENGTH_SHORT).show();
                return;
            } else {
                Client Client = new Client();
                Service apiService = Client.getClient().create(Service.class);
                Call<Review> call = apiService.getReview(movie_id, BuildConfig.THE_MOVIE_DB_API_TOKEN);

                call.enqueue(new Callback<Review>() {
                    @Override
                    public void onResponse(Call<Review> call, Response<Review> response) {
                        if (response.isSuccessful()){
                            if (response.body() != null){
                                List<ReviewResult> reviewResults = response.body().getResults();
                                MultiSnapRecyclerView recyclerView2 = (MultiSnapRecyclerView) findViewById(R.id.review_recyclerview);
                                LinearLayoutManager firstManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
                                recyclerView2.setLayoutManager(firstManager);
                                recyclerView2.setAdapter(new ReviewAdapter(getApplicationContext(), reviewResults));
                                recyclerView2.smoothScrollToPosition(0);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Review> call, Throwable t) {

                    }
                });
            }

        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, "unable to fetch data",Toast.LENGTH_SHORT).show();
        }

    }

    public void saveFavorite(){
        Double rate = movie.getVoteAverage();
        final FavoriteEntry favoriteEntry = new FavoriteEntry(movie_id, movieName, rate, thumbnail, synopsis);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.favoriteDao().insertFavorite(favoriteEntry);
            }
        });
    }

    private void deleteFavorite(final int movie_id){
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.favoriteDao().deleteFavoriteWithId(movie_id);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.share:
                shareContent();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareContent(){

        Bitmap bitmap =getBitmapFromView(imageView);
        try {
            File file = new File(this.getExternalCacheDir(),"logicchip.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_TEXT, movieName);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, "Share image via"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            bgDrawable.draw(canvas);
        }   else{
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    @SuppressLint("StaticFieldLeak")
    private void checkStatus(final String movieName){
        final MaterialFavoriteButton materialFavoriteButton = (MaterialFavoriteButton) findViewById(R.id.favorite_button);
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params){
                entries.clear();
                entries = mDb.favoriteDao().loadAll(movieName);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid){
                super.onPostExecute(aVoid);
                if (entries.size() > 0){
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
                                        deleteFavorite(movie_id);
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
                                        deleteFavorite(movie_id);
                                        Snackbar.make(buttonView, "Removed from Favorite",
                                                Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        }.execute();
    }
}
