package com.reactnativemedialibrary;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;

import static com.reactnativemedialibrary.MediaLibraryConstants.ASSET_PROJECTION;
import static com.reactnativemedialibrary.MediaLibraryConstants.ERROR_UNABLE_TO_LOAD;
import static com.reactnativemedialibrary.MediaLibraryConstants.ERROR_UNABLE_TO_LOAD_PERMISSION;
import static com.reactnativemedialibrary.MediaLibraryConstants.EXTERNAL_CONTENT;
import static com.reactnativemedialibrary.MediaLibraryUtils.putAssetsInfo;

class GetAssets extends AsyncTask<Void, Void, Void> {
  private final Context mContext;
  private final Promise mPromise;
  private final ReadableMap mAssetOptions;

  public GetAssets(Context context, ReadableMap assetOptions, Promise promise) {
    mContext = context;
    mAssetOptions = assetOptions;
    mPromise = promise;
  }

  @Override
  protected Void doInBackground(Void... params) {
    final Bundle response = new Bundle();
    GetQueryInfo getQueryInfo = new GetQueryInfo(mAssetOptions).invoke();
    final String selection = getQueryInfo.getSelection();
    final String order = getQueryInfo.getOrder();
    final int limit = getQueryInfo.getLimit();
    final int offset = getQueryInfo.getOffset();
    try (Cursor assets = mContext.getContentResolver().query(
        EXTERNAL_CONTENT,
        ASSET_PROJECTION,
        selection,
        null,
        order)) {
      if (assets == null) {
        mPromise.reject(ERROR_UNABLE_TO_LOAD, "Could not get assets. Query returns null.");
      } else {
        ArrayList<Bundle> assetsInfo = new ArrayList<>();
        putAssetsInfo(assets, assetsInfo, limit, offset, false);
        response.putParcelableArrayList("assets", assetsInfo);
        response.putBoolean("hasNextPage", !assets.isAfterLast());
        response.putString("endCursor", Integer.toString(assets.getPosition()));
        response.putInt("totalCount", assets.getCount());
        mPromise.resolve(response);
      }
    } catch (SecurityException e) {
      mPromise.reject(ERROR_UNABLE_TO_LOAD_PERMISSION,
          "Could not get asset: need READ_EXTERNAL_STORAGE permission.", e);
    } catch (IOException e) {
      Log.e(ERROR_UNABLE_TO_LOAD, "Could not read file or parse EXIF tags", e);
    }
    return null;
  }
}