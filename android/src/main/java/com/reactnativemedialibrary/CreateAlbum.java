package com.reactnativemedialibrary;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.facebook.react.bridge.Promise;

import static com.reactnativemedialibrary.MediaLibraryConstants.ERROR_NO_ALBUM;
import static com.reactnativemedialibrary.MediaLibraryConstants.ERROR_UNABLE_TO_LOAD;
import static com.reactnativemedialibrary.MediaLibraryConstants.ERROR_UNABLE_TO_LOAD_PERMISSION;
import static com.reactnativemedialibrary.MediaLibraryConstants.ERROR_UNABLE_TO_SAVE;
import static com.reactnativemedialibrary.MediaLibraryUtils.FileStrategy;
import static com.reactnativemedialibrary.MediaLibraryUtils.copyStrategy;
import static com.reactnativemedialibrary.MediaLibraryUtils.getAssetsById;
import static com.reactnativemedialibrary.MediaLibraryUtils.moveStrategy;
import static com.reactnativemedialibrary.MediaLibraryUtils.queryAlbum;

class CreateAlbum extends AsyncTask<Void, Void, Void> {
  private final Context mContext;
  private final Promise mPromise;
  private final String mAlbumName;
  private final String mAssetId;
  private final FileStrategy mStrategy;

  public CreateAlbum(Context context, String albumName, String assetId, boolean copyAsset, Promise promise) {
    mContext = context;
    mAlbumName = albumName;
    mAssetId = assetId;
    mPromise = promise;
    mStrategy = copyAsset ? copyStrategy : moveStrategy;
  }

  private File createAlbum() {
    File album = new File(Environment.getExternalStorageDirectory().getPath(), mAlbumName);

    if (!album.exists() && !album.mkdirs()) {
      mPromise.reject(ERROR_NO_ALBUM, "Could not create album directory.");
      return null;
    }
    return album;
  }

  @Override
  protected Void doInBackground(Void... params) {
    try {
      File album = createAlbum();
      if (album == null) {
        return null;
      }
      List<File> files = getAssetsById(mContext, mPromise, mAssetId);
      if (files == null) {
        return null;
      }
      File albumCreator = getAssetsById(mContext, mPromise, mAssetId).get(0);
      File newFile = mStrategy.apply(albumCreator, album, mContext);

      MediaScannerConnection.scanFile(
          mContext,
          new String[]{newFile.getPath()},
          null,

          new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
              if (uri == null) {
                mPromise.reject(ERROR_UNABLE_TO_SAVE, "Could not add image to album.");
                return;
              }
              final String selection = MediaStore.Images.Media.DATA + "=?";
              final String[] args = {path};
              queryAlbum(mContext, selection, args, mPromise);
            }
          });
    } catch (SecurityException e) {
      mPromise.reject(ERROR_UNABLE_TO_LOAD_PERMISSION,
          "Could not create album: need WRITE_EXTERNAL_STORAGE permission.", e);
    } catch (IOException e) {
      mPromise.reject(ERROR_UNABLE_TO_LOAD, "Could not read file or parse EXIF tags", e);
    }
    return null;
  }
}