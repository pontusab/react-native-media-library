package com.reactnativemedialibrary;

import android.provider.MediaStore;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.facebook.react.bridge.ReadableMap;

import static com.reactnativemedialibrary.MediaLibraryConstants.MEDIA_TYPE_ALL;
import static com.reactnativemedialibrary.MediaLibraryUtils.convertMediaType;
import static com.reactnativemedialibrary.MediaLibraryUtils.mapOrderDescriptor;

class GetQueryInfo {
  private ReadableMap mInput;
  private int mLimit;
  private StringBuilder mSelection;
  private StringBuilder mOrder;
  private int mOffset;

  GetQueryInfo(ReadableMap input) {
    mInput = input;
  }

  int getLimit() {
    return mLimit;
  }

  int getOffset() {
    return mOffset;
  }

  String getSelection() {
    return mSelection.toString();
  }

  String getOrder() {
    return mOrder.toString();
  }

    public GetQueryInfo invoke() {
    mLimit = mInput.hasKey("first") ? mInput.getInt("first") : 20;

    mSelection = new StringBuilder();
    if (mInput.hasKey("album")) {
      mSelection.append(MediaStore.Images.Media.BUCKET_ID).append(" = ").append(mInput.getString("album"));
      mSelection.append(" AND ");
    }

    List<Object> mediaType = null;
    // List<Object> mediaType = mInput.hasKey("mediaType") ? (List<Object>) mInput.getMap("mediaType") : null;

    if (mediaType != null && !mediaType.contains(MEDIA_TYPE_ALL)) {
      List<Integer> mediaTypeInts = new ArrayList<Integer>();

      for (Object mediaTypeStr : mediaType) {
        mediaTypeInts.add(convertMediaType(mediaTypeStr.toString()));
      }
      mSelection.append(MediaStore.Files.FileColumns.MEDIA_TYPE).append(" IN (").append(TextUtils.join(",", mediaTypeInts)).append(")");
    } else {
      mSelection.append(MediaStore.Files.FileColumns.MEDIA_TYPE).append(" != ").append(MediaStore.Files.FileColumns.MEDIA_TYPE_NONE);
    }

    Double createdAfter = mInput.hasKey("createdAfter") ? (Double) mInput.getDouble("createdAfter") : null;
    Double createdBefore = mInput.hasKey("createdBefore") ? (Double) mInput.getDouble("createdBefore"): null;

    if (createdAfter != null) {
      mSelection
              .append(" AND ")
              .append(MediaStore.Images.Media.DATE_TAKEN)
              .append(" > ")
              .append(createdAfter.longValue());
    }

    if (createdBefore != null) {
      mSelection
              .append(" AND ")
              .append(MediaStore.Images.Media.DATE_TAKEN)
              .append(" < ")
              .append(createdBefore.longValue());
    }

    mOrder = new StringBuilder();
    // if (mInput.hasKey("sortBy") && ((List) mInput.getMap("sortBy")).size() > 0) {
      // mOrder.append(mapOrderDescriptor((List) mInput.getMap("sortBy")));
    // } else {
      mOrder.append(MediaStore.Images.Media.DEFAULT_SORT_ORDER);
    // }

    // to maintain compatibility with IOS field after is in string object
    mOffset = mInput.hasKey("after") ?
        Integer.parseInt((String) mInput.getString("after")) : 0;

    return this;
  }
}