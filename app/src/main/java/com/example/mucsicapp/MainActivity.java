package com.example.mucsicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button btnMode, btnPlayPause, btnNext, btnPrev;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvDuration, tvSongTitle;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private ImageView btnFavorite;
    private SharedPreferences sharedPreferences;
    private int loopMode = 0; // 0 - Lặp danh sách, 1 - Lặp bài hiện tại, 2 - Phát ngẫu nhiên

    int[] musicFiles = {
            R.raw.hollidays,
            R.raw.beautifuldream,
            R.raw.averyhappychristmas
    };
    int currentSongIndex = 0;

    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize UI components
        btnMode = findViewById(R.id.btnMode);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        seekBar = findViewById(R.id.seekBar);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvDuration = findViewById(R.id.tvDuration);
        tvSongTitle = findViewById(R.id.tvSongTitle);
        btnFavorite = findViewById(R.id.btnFavorite);

        // Khai báo và khởi tạo SharedPreferences ở đây
        sharedPreferences = getSharedPreferences("MusicAppPrefs", MODE_PRIVATE);

        // Lấy chế độ lặp từ SharedPreferences
        loopMode = sharedPreferences.getInt("loopMode", 0); // Mặc định là 0 (Lặp danh sách)
        updateLoopButton(); // Cập nhật nút lặp theo chế độ đã lưu

        // Load trạng thái yêu thích từ SharedPreferences
        isFavorite = loadFavoriteState(currentSongIndex);
        updateFavoriteButton(isFavorite);

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.averyhappychristmas);

        // Set Duration
        tvDuration.setText(formatTime(mediaPlayer.getDuration()));

        btnMode.setOnClickListener(v -> {
            loopMode = (loopMode + 1) % 3; // Chuyển qua chế độ tiếp theo (0 -> 1 -> 2 -> 0)
            updateLoopButton(); // Cập nhật hình ảnh nút lặp

            // Lưu trạng thái chế độ lặp vào SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("loopMode", loopMode);
            editor.apply();
        });

        // Xử lý sự kiện click cho nút tim
        btnFavorite.setOnClickListener(v -> {
            // Đảo ngược trạng thái yêu thích
            isFavorite = !isFavorite;
            // Cập nhật nút tim
            updateFavoriteButton(isFavorite);
            // Lưu trạng thái yêu thích vào SharedPreferences
            saveFavoriteState(currentSongIndex, isFavorite);
        });

        // Gọi playSong() để phát nhạc
        playSong();

        // Play/Pause button
        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlayPause.setText("⏯");
            } else {
                mediaPlayer.start();
                btnPlayPause.setText("⏸");
                updateSeekBar();
            }
        });

        // Next button
        btnNext.setOnClickListener(v -> {
            currentSongIndex = (currentSongIndex + 1) % musicFiles.length;
            playSong();
        });

        // Previous button
        btnPrev.setOnClickListener(v -> {
            currentSongIndex = (currentSongIndex - 1 + musicFiles.length) % musicFiles.length;
            playSong();
        });

        // SeekBar
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
                tvCurrentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }

    private void updateLoopButton() {
        if (loopMode == 0) {
            btnMode.setText("🔁"); // Lặp danh sách
        } else if (loopMode == 1) {
            btnMode.setText("🔂"); // Lặp bài hiện tại
        } else if (loopMode == 2) {
            btnMode.setText("🔀"); // Phát ngẫu nhiên
        }
    }


    private void playSong() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, musicFiles[currentSongIndex]);

        String[] songTitles = {"Hollidays", "Beautiful Dream", "A Very Happy Christmas"};
        String[] songAuthors = {"John Doe", "Jane Smith", "Michael Brown"};
//        String[] songTitles = {"1", "2", "3"};
        tvSongTitle.setText(songTitles[currentSongIndex] + " - " + songAuthors[currentSongIndex]);

        // Đọc lại trạng thái yêu thích cho bài hát hiện tại
        isFavorite = loadFavoriteState(currentSongIndex);
        updateFavoriteButton(isFavorite);

        mediaPlayer.setOnPreparedListener(mp -> {
            tvDuration.setText(formatTime(mediaPlayer.getDuration()));
            seekBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.start();
            btnPlayPause.setText("⏸");
            updateSeekBar();
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            if (loopMode == 0) { // Lặp danh sách
                currentSongIndex = (currentSongIndex + 1) % musicFiles.length;
                playSong();
            } else if (loopMode == 1) { // Lặp bài hiện tại
                playSong();
            } else if (loopMode == 2) { // Phát ngẫu nhiên
                int previousIndex = currentSongIndex;
                do {
                    currentSongIndex = (int) (Math.random() * musicFiles.length);
                } while (currentSongIndex == previousIndex); // Kiểm tra để không bị trùng
                playSong();
            }
        });
    }

    private void saveFavoriteState(int songIndex, boolean isFavorite) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("favorite_" + songIndex, isFavorite);
        editor.apply();
    }

    private boolean loadFavoriteState(int songIndex) {
        return sharedPreferences.getBoolean("favorite_" + songIndex, false);
    }

    private void updateFavoriteButton(boolean isFavorite) {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.heart_filled512x512);
        } else {
            btnFavorite.setImageResource(R.drawable.heart_outline512x512);
        }
    }

    private void updateSeekBar() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        if (mediaPlayer.isPlaying()) {
            handler.postDelayed(this::updateSeekBar, 500);
        }
    }

    private String formatTime(int ms) {
        int minutes = ms / 1000 / 60;
        int seconds = (ms / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
