package com.delaroystudios.movieapp.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.delaroystudios.movieapp.database.AppDatabase;
import com.delaroystudios.movieapp.database.FavoriteEntry;

/**
 * Created by delaroy on 9/6/18.
 */

public class AddFavoriteViewModel extends ViewModel {

    private LiveData<FavoriteEntry> favorite;

    public AddFavoriteViewModel(AppDatabase database, int favoriteId) {
        favorite = database.favoriteDao().loadFavoriteById(favoriteId);
    }

    public LiveData<FavoriteEntry> getFavorite() {
        return favorite;
    }
}
