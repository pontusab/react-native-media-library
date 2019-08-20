import React, { useCallback, useEffect, useState } from 'react';
import { View, Text, Image, FlatList } from 'react-native';
import Permissions from 'react-native-permissions';
import * as MediaLibrary from 'react-native-media-library';

function App() {
  const [assets, setAssets] = useState([]);
  const [albums, setAlbums] = useState([]);
  const [selectedAlbum, setSelectedAlbum] = useState(null);
  const [hasNextPage, setHasNextPage] = useState(true);
  const [endCursor, setEndCursor] = useState();

  const fetchAssets = useCallback(
    async after => {
      if (!hasNextPage) {
        return;
      }

      try {
        const result = await MediaLibrary.getAssetsAsync({
          after,
          // album: '-2075821635',
          first: 10
        });

        setAssets(p => p.concat(result.assets));
        setHasNextPage(result.hasNextPage);
        setEndCursor(result.endCursor);
      } catch (err) {
        logError(err);
      }
    },
    [selectedAlbum]
  );

  const fetchAlbums = useCallback(async () => {
    const albums = await MediaLibrary.getAlbumsAsync();
    setAlbums(albums);
  }, []);

  useEffect(() => {
    Permissions.request('photo').then(response => {
      if (response === 'authorized') {
        fetchAlbums();
        fetchAssets();
      }
    });
  }, []);

  const onEndReached = useCallback(() => {
    if (hasNextPage) {
      fetchAssets(endCursor);
    }
  }, [hasNextPage, endCursor, fetchAssets]);

  return (
    <View>
      {albums.map(album => (
        <Text key={album.id} onPress={() => console.log(album.id)}>
          {album.title}
        </Text>
      ))}
      <FlatList
        data={assets}
        renderItem={({ item }) => <Image source={item} />}
        keyExtractor={({ id }) => id}
        onEndReached={onEndReached}
      />
    </View>
  );
}

export default App;
