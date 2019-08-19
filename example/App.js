import React, { useEffect } from 'react';
import { View, Text } from 'react-native';
import Permissions from 'react-native-permissions';
import * as MediaLibrary from 'react-native-media-library';

function App() {
  async function fetchAssets() {
    try {
      // const assets = await MediaLibrary.getAssetsAsync({
      //   first: 10
      // });
      // console.log('assets', assets);

      const albums = await MediaLibrary.getAlbumsAsync();
      console.log('albums', albums);
    } catch (err) {
      console.log(err);
    }
  }

  useEffect(() => {
    Permissions.request('photo').then(response => {
      // console.log(response);
    });
  }, []);

  useEffect(() => {
    fetchAssets();
  }, []);

  return (
    <View>
      <Text>Hello</Text>
    </View>
  );
}

export default App;
