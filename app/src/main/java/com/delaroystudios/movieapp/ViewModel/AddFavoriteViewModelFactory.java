package com.delaroystudios.movieapp.ViewModel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.delaroystudios.movieapp.database.AppDatabase;

/**
 * Created by delaroy on 9/6/18.
 */

public class AddFavoriteViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final int mFavoriteId;

    public AddFavoriteViewModelFactory(AppDatabase database, int favoriteId) {
        mDb = database;
        mFavoriteId = favoriteId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AddFavoriteViewModel(mDb, mFavoriteId);
    }
}
