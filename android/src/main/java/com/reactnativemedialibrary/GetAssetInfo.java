package com.reactnativemedialibrary;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.MediaStore.Images.Media;

import com.facebook.react.bridge.Promise;

import static com.reactnativemedialibrary.MediaLibraryUtils.queryAssetInfo;

class GetAssetInfo extends AsyncTask<Void, Void, Void> {
  private final Context mContext;
  private final String mAssetId;
  private final Promise mPromise;

  public GetAssetInfo(Context context, String assetId, Promise promise) {
    mContext = context;
    mAssetId = assetId;
    mPromise = promise;
  }

  @Override
  protected Void doInBackground(Void... params) {

    final String selection = Media._ID + "=?";
    final String[] selectionArgs = {mAssetId};
    queryAssetInfo(mContext, selection, selectionArgs, true, mPromise);
    return null;
  }
}
