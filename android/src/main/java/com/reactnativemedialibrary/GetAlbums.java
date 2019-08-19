package com.reactnativemedialibrary;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;

// import java.util.ArrayList;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import java.util.List;

import com.facebook.react.bridge.Promise;

import static com.reactnativemedialibrary.MediaLibraryConstants.ERROR_UNABLE_TO_LOAD;
import static com.reactnativemedialibrary.MediaLibraryConstants.ERROR_UNABLE_TO_LOAD_PERMISSION;
import static com.reactnativemedialibrary.MediaLibraryConstants.EXTERNAL_CONTENT;

class GetAlbums extends AsyncTask<Void, Void, Void> {
  private final Context mContext;
  private final Promise mPromise;

  public GetAlbums(Context context, Promise promise) {
    mContext = context;
    mPromise = promise;
  }

  @Override
  protected Void doInBackground(Void... params) {
    WritableNativeArray result = new WritableNativeArray();
    final String countColumn = "COUNT(*)";
    final String[] projection = {MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, countColumn};
    final String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " != " + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE + ") /*";

    try (Cursor albums = mContext.getContentResolver().query(
        EXTERNAL_CONTENT,
        projection,
        selection,
        null,
        "*/ GROUP BY " + MediaStore.Images.Media.BUCKET_ID +
            " ORDER BY " + MediaStore.Images.Media.BUCKET_DISPLAY_NAME)) {
              
       if (albums == null) {
        mPromise.reject(ERROR_UNABLE_TO_LOAD, "Could not get albums. Query returns null.");
      } else {
        final int bucketIdIndex = albums.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
        final int bucketDisplayNameIndex = albums.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        final int numOfItemsIndex = albums.getColumnIndex(countColumn);

        while (albums.moveToNext()) {
          WritableNativeMap album = new WritableNativeMap();
          album.putString("id", albums.getString(bucketIdIndex));
          album.putString("title", albums.getString(bucketDisplayNameIndex));
          // album.putParcelable("type", null);
          album.putInt("assetCount", albums.getInt(numOfItemsIndex));
          result.pushMap(album);
        }
        mPromise.resolve(result);
      }
    } catch (SecurityException e) {
      mPromise.reject(ERROR_UNABLE_TO_LOAD_PERMISSION,
          "Could not get albums: need READ_EXTERNAL_STORAGE permission.", e);
    }
    return null;
  }
}