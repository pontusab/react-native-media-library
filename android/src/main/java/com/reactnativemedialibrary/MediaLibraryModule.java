package com.reactnativemedialibrary;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Promise;
 
// import org.unimodules.core.ExportedModule;
// import org.unimodules.core.ModuleRegistry;
// import org.unimodules.core.Promise;
// import org.unimodules.core.interfaces.services.EventEmitter;
// import org.unimodules.interfaces.permissions.Permissions;

// import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
// import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
// import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import static com.reactnativemedialibrary.MediaLibraryConstants.ERROR_NO_PERMISSIONS;
import static com.reactnativemedialibrary.MediaLibraryConstants.ERROR_NO_PERMISSIONS_MESSAGE;
import static com.reactnativemedialibrary.MediaLibraryConstants.ERROR_NO_PERMISSIONS_MODULE;
import static com.reactnativemedialibrary.MediaLibraryConstants.ERROR_NO_PERMISSIONS_MODULE_MESSAGE;
import static com.reactnativemedialibrary.MediaLibraryConstants.EXTERNAL_CONTENT;
import static com.reactnativemedialibrary.MediaLibraryConstants.LIBRARY_DID_CHANGE_EVENT;
import static com.reactnativemedialibrary.MediaLibraryConstants.MEDIA_TYPE_ALL;
import static com.reactnativemedialibrary.MediaLibraryConstants.MEDIA_TYPE_AUDIO;
import static com.reactnativemedialibrary.MediaLibraryConstants.MEDIA_TYPE_PHOTO;
import static com.reactnativemedialibrary.MediaLibraryConstants.MEDIA_TYPE_UNKNOWN;
import static com.reactnativemedialibrary.MediaLibraryConstants.MEDIA_TYPE_VIDEO;
import static com.reactnativemedialibrary.MediaLibraryConstants.SORT_BY_CREATION_TIME;
import static com.reactnativemedialibrary.MediaLibraryConstants.SORT_BY_DEFAULT;
import static com.reactnativemedialibrary.MediaLibraryConstants.SORT_BY_DURATION;
import static com.reactnativemedialibrary.MediaLibraryConstants.SORT_BY_HEIGHT;
import static com.reactnativemedialibrary.MediaLibraryConstants.SORT_BY_MEDIA_TYPE;
import static com.reactnativemedialibrary.MediaLibraryConstants.SORT_BY_MODIFICATION_TIME;
import static com.reactnativemedialibrary.MediaLibraryConstants.SORT_BY_WIDTH;


public class MediaLibraryModule extends ReactContextBaseJavaModule {
  // private MediaStoreContentObserver mImagesObserver = null;
  // private MediaStoreContentObserver mVideosObserver = null;
  private Context mContext;
  // private ModuleRegistry mModuleRegistry;

  public MediaLibraryModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mContext = reactContext;
  }

  @Override
  public String getName() {
    return "MediaLibrary";
  }

  @Override
  public Map<String, Object> getConstants() {
    return Collections.unmodifiableMap(new HashMap<String, Object>() {
      {
        put("MediaType", Collections.unmodifiableMap(new HashMap<String, Object>() {
          {
            put("audio", MEDIA_TYPE_AUDIO);
            put("photo", MEDIA_TYPE_PHOTO);
            put("video", MEDIA_TYPE_VIDEO);
            put("unknown", MEDIA_TYPE_UNKNOWN);
            put("all", MEDIA_TYPE_ALL);
          }
        }));
        put("SortBy", Collections.unmodifiableMap(new HashMap<String, Object>() {
          {
            put("default", SORT_BY_DEFAULT);
            put("creationTime", SORT_BY_CREATION_TIME);
            put("modificationTime", SORT_BY_MODIFICATION_TIME);
            put("mediaType", SORT_BY_MEDIA_TYPE);
            put("width", SORT_BY_WIDTH);
            put("height", SORT_BY_HEIGHT);
            put("duration", SORT_BY_DURATION);
          }
        }));
        put("CHANGE_LISTENER_NAME", LIBRARY_DID_CHANGE_EVENT);
      }
    });
  }

  // @Override
  // public void onCreate(ModuleRegistry moduleRegistry) {
  //   mModuleRegistry = moduleRegistry;
  // }

  // TODO(@tsapeta): refactor together with expo-permissions
  // @ReactMethod
  // public void requestPermissionsAsync(final Promise promise) {
  //   Permissions permissionsModule = mModuleRegistry.getModule(Permissions.class);

  //   if (permissionsModule == null) {
  //     promise.reject(ERROR_NO_PERMISSIONS_MODULE, ERROR_NO_PERMISSIONS_MODULE_MESSAGE);
  //     return;
  //   }
  //   String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

  //   permissionsModule.askForPermissions(permissions, new Permissions.PermissionsRequestListener() {
  //     @Override
  //     public void onPermissionsResult(int[] results) {
  //       boolean isGranted = results[0] == PackageManager.PERMISSION_GRANTED;
  //       Bundle response = new Bundle();

  //       response.putString("status", isGranted ? "granted" : "denied");
  //       response.putBoolean("granted", isGranted);
  //       promise.resolve(response);
  //     }
  //   });
  // }

  // TODO(@tsapeta): refactor together with expo-permissions
  // @ReactMethod
  // public void getPermissionsAsync(final Promise promise) {
  //   Permissions permissionsModule = mModuleRegistry.getModule(Permissions.class);

  //   if (permissionsModule == null) {
  //     promise.reject(ERROR_NO_PERMISSIONS_MODULE, ERROR_NO_PERMISSIONS_MODULE_MESSAGE);
  //     return;
  //   }
  //   boolean isGranted = !isMissingPermissions();
  //   Bundle response = new Bundle();

  //   response.putString("status", isGranted ? "granted" : "denied");
  //   response.putBoolean("granted", isGranted);
  //   promise.resolve(response);
  // }

  @ReactMethod
  public void createAssetAsync(String localUri, Promise promise) {
    if (isMissingPermissions()) {
      promise.reject(ERROR_NO_PERMISSIONS, ERROR_NO_PERMISSIONS_MESSAGE);
      return;
    }

    new CreateAsset(mContext, localUri, promise)
        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @ReactMethod
  public void addAssetsToAlbumAsync(List<String> assetsId, String albumId, boolean copyToAlbum, Promise promise) {
    if (isMissingPermissions()) {
      promise.reject(ERROR_NO_PERMISSIONS, ERROR_NO_PERMISSIONS_MESSAGE);
      return;
    }

    new AddAssetsToAlbum(mContext,
        assetsId.toArray(new String[0]), albumId, copyToAlbum, promise).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }


  @ReactMethod
  public void removeAssetsFromAlbumAsync(List<String> assetsId, String albumId, Promise promise) {
    if (isMissingPermissions()) {
      promise.reject(ERROR_NO_PERMISSIONS, ERROR_NO_PERMISSIONS_MESSAGE);
      return;
    }

    new RemoveAssetsFromAlbum(mContext,
        assetsId.toArray(new String[0]), albumId, promise).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @ReactMethod
  public void deleteAssetsAsync(List<String> assetsId, Promise promise) {
    if (isMissingPermissions()) {
      promise.reject(ERROR_NO_PERMISSIONS, ERROR_NO_PERMISSIONS_MESSAGE);
      return;
    }

    new DeleteAssets(mContext, assetsId.toArray(new String[0]), promise)
        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @ReactMethod
  public void getAssetInfoAsync(String assetId, Promise promise) {
    if (isMissingPermissions()) {
      promise.reject(ERROR_NO_PERMISSIONS, ERROR_NO_PERMISSIONS_MESSAGE);
      return;
    }

    new GetAssetInfo(mContext, assetId, promise).
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }


  @ReactMethod
  public void getAlbumsAsync(Map<String, Object> options /* unused on android atm */, Promise promise) {
    if (isMissingPermissions()) {
      promise.reject(ERROR_NO_PERMISSIONS, ERROR_NO_PERMISSIONS_MESSAGE);
      return;
    }

    new GetAlbums(mContext, promise).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }


  @ReactMethod
  public void getAlbumAsync(String albumName, Promise promise) {
    if (isMissingPermissions()) {
      promise.reject(ERROR_NO_PERMISSIONS, ERROR_NO_PERMISSIONS_MESSAGE);
      return;
    }

    new GetAlbum(mContext, albumName, promise)
        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @ReactMethod
  public void createAlbumAsync(String albumName, String assetId, boolean copyAsset, Promise promise) {
    if (isMissingPermissions()) {
      promise.reject(ERROR_NO_PERMISSIONS, ERROR_NO_PERMISSIONS_MESSAGE);
      return;
    }

    new CreateAlbum(mContext, albumName, assetId, copyAsset, promise)
        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @ReactMethod
  public void deleteAlbumsAsync(List<String> albumIds, Promise promise) {
    if (isMissingPermissions()) {
      promise.reject(ERROR_NO_PERMISSIONS, ERROR_NO_PERMISSIONS_MESSAGE);
      return;
    }

    new DeleteAlbums(mContext, albumIds, promise)
        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

  }

  @ReactMethod
  public void getAssetsAsync(ReadableMap assetOptions, Promise promise) {
    if (isMissingPermissions()) {
      promise.reject(ERROR_NO_PERMISSIONS, ERROR_NO_PERMISSIONS_MESSAGE);
      return;
    }

    new GetAssets(mContext, assetOptions, promise)
        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  // Library change observer

  // @ReactMethod
  // public void startObserving(Promise promise) {
  //   if (mImagesObserver != null) {
  //     promise.resolve(null);
  //     return;
  //   }

  //   // We need to register an observer for each type of assets,
  //   // because it seems that observing a parent directory (EXTERNAL_CONTENT) doesn't work well,
  //   // whereas observing directory of images or videos works fine.

  //   Handler handler = new Handler();
  //   mImagesObserver = new MediaStoreContentObserver(handler, Files.FileColumns.MEDIA_TYPE_IMAGE);
    // mVideosObserver = new MediaStoreContentObserver(handler, Files.FileColumns.MEDIA_TYPE_VIDEO);

  //   ContentResolver contentResolver = mContext.getContentResolver();

  //   contentResolver.registerContentObserver(
  //       Media.EXTERNAL_CONTENT_URI,
  //       true,
  //       mImagesObserver
  //   );
  //   contentResolver.registerContentObserver(
  //       MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
  //       true,
        // mVideosObserver
  //   );
  //   promise.resolve(null);
  // }

  // @ReactMethod
  // public void stopObserving(Promise promise) {
  //   if (mImagesObserver != null) {
  //     ContentResolver contentResolver = mContext.getContentResolver();

  //     contentResolver.unregisterContentObserver(mImagesObserver);
      // contentResolver.unregisterContentObserver(mVideosObserver);

  //     mImagesObserver = null;
      // mVideosObserver = null;
  //   }
  //   promise.resolve(null);
  // }

  private boolean isMissingPermissions() {
    return false;
    // Permissions permissionsManager = mModuleRegistry.getModule(Permissions.class);
    // if (permissionsManager == null) {
    //   return false;
    // }
    // int[] grantResults = permissionsManager.getPermissions(new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE});

    // return grantResults.equals(new int[]{PERMISSION_GRANTED, PERMISSION_GRANTED});
  }

  // private class MediaStoreContentObserver extends ContentObserver {
  //   private int mAssetsTotalCount;
  //   private int mMediaType;

  //   public MediaStoreContentObserver(Handler handler, int mediaType) {
  //     super(handler);
  //     mMediaType = mediaType;
  //     mAssetsTotalCount = getAssetsTotalCount(mMediaType);
  //   }

  //   @Override
  //   public void onChange(boolean selfChange) {
  //     this.onChange(selfChange, null);
  //   }

  //   @Override
  //   public void onChange(boolean selfChange, Uri uri) {
  //     int newTotalCount = getAssetsTotalCount(mMediaType);

  //     // Send event to JS only when assets count has been changed - to filter out some unnecessary events.
  //     // It's not perfect solution if someone adds and deletes the same number of assets in a short period of time, but I hope these events will not be batched.
  //     if (mAssetsTotalCount != newTotalCount) {
  //       mAssetsTotalCount = newTotalCount;
  //       mModuleRegistry.getModule(EventEmitter.class).emit(LIBRARY_DID_CHANGE_EVENT, new Bundle());
  //     }
  //   }

    private int getAssetsTotalCount(int mediaType) {
      Cursor countCursor = mContext.getContentResolver().query(
          EXTERNAL_CONTENT,
          new String[]{"count(*) AS count"},
          Files.FileColumns.MEDIA_TYPE + " == " + mediaType,
          null,
          null
      );

      countCursor.moveToFirst();

      return countCursor.getInt(0);
    }
  
}