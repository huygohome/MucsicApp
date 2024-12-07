package com.example.mucsicapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SongList {
    private static List<Song> songList = new ArrayList<>();

    public static List<Song> getSongs() {
        return songList;
    }

    public static void loadMusicFromDevice(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };

        Cursor cursor = contentResolver.query(musicUri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                long durationInMs = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String duration = formatDuration(durationInMs);
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                songList.add(new Song(
                        R.drawable.music_logo, // Placeholder hình ảnh
                        name,
                        artist,
                        duration,
                        filePath
                ));
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Toast.makeText(context, "Không tìm thấy nhạc trong thiết bị", Toast.LENGTH_SHORT).show();
        }
    }

    private static String formatDuration(long durationInMs) {
        int minutes = (int) (durationInMs / 1000 / 60);
        int seconds = (int) (durationInMs / 1000 % 60);
        return String.format("%d:%02d", minutes, seconds);
    }
}

