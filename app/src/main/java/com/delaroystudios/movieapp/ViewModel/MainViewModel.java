package com.delaroystudios.movieapp.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.delaroystudios.movieapp.database.AppDatabase;
import com.delaroystudios.movieapp.database.FavoriteEntry;

import java.util.List;

/**
 * Created by delaroy on 9/6/18.
 */

public class MainViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<FavoriteEntry>> favorite;

    public MainViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        favorite = database.favoriteDao().loadAllFavorite();
    }

    public LiveData<List<FavoriteEntry>> getFavorite() {
        return favorite;
    }
}
