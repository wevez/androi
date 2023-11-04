package tech.tenamin.unisound.core.api;

import android.media.MediaPlayer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import jp.wasabeef.glide.transformations.BlurTransformation;
import tech.tenamin.unisound.MainActivity;
import tech.tenamin.unisound.R;
import tech.tenamin.unisound.core.api.impl.SoundCloud;
import tech.tenamin.unisound.core.api.impl.YouTube;
import tech.tenamin.unisound.core.api.util.StringUtil;

/**
 * The snippet object contains information about music playback and methods to manipulate it (play(), stop(), etc..).
 *
 * @author tenamen
 * @since 2023/08/17.
 * @param <T> Type of MusicSnippet
 */
public abstract class PlayingAdapter <T extends MusicSnippet> {

    /** MainActivity instance used to control views of it. */
    protected final AppCompatActivity APP_VIEW;

    /** Request queue used to fetch the information necessary to play music. */
    protected final RequestQueue QUEUE;

    /** The data that forms the basis of the music to be played. */
    protected final T SNIPPET;

    /** The MediaPlayer used to play music. */
    protected static final MediaPlayer player = new MediaPlayer();

    protected PlayingAdapter(@NonNull final AppCompatActivity appView, @NonNull final T data) {
        this.APP_VIEW = appView;
        this.QUEUE = Volley.newRequestQueue(appView);
        this.SNIPPET = data;
    }

    /**
     * Handles the visual and interactive aspects of a playback card.
     * (Including setting the title, publisher, thumbnail, and seek bar behavior)
     */
    public final void setPlaybackCardAppearance() {

        // Get playback card view and appears it.
        final View playbackCard = APP_VIEW.findViewById(R.id.playback_card);
        playbackCard.bringToFront();
        playbackCard.setVisibility(View.VISIBLE);

        // Set title text of playback card.
        final TextView titleView = playbackCard.findViewById(R.id.textView_song_title);
        titleView.setText(SNIPPET.getTITLE());

        final TextView publisherView = playbackCard.findViewById(R.id.textView_artist);
        publisherView.setText(SNIPPET.getPUBLISHER());

        // Blur thumbnail and set it to the background of playback card.
        final ImageView thumbnailView = playbackCard.findViewById(R.id.card_background);
        Glide.with(APP_VIEW).load(SNIPPET.getTHUMBNAIL())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(100, 3)))
                .into(thumbnailView);

        // Seek bar position synchronizes with playback time after each second.
        final TextView currentPosition = this.APP_VIEW.findViewById(R.id.playing_position);
        final SeekBar seekBar = this.APP_VIEW.findViewById(R.id.play_current_time);
        this.APP_VIEW.runOnUiThread(new Runnable() {

            private final Handler handler = new Handler();

            @Override
            public void run() {
                final int seconds = player.getCurrentPosition() / 1000;
                currentPosition.setText(
                        String.format("%s/%s",
                                StringUtil.secondsToMMSS(seconds),
                                StringUtil.secondsToMMSS(player.getDuration() / 1000)
                        )
                );
                seekBar.setProgress(seconds);
                handler.postDelayed(this, 1000);
            }
        });

        // Allow users to return to the playback position by operating the seek bar.
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.start();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                player.pause();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                if (fromUser) player.seekTo(progress * 1000);
            }
        });
    }

    public final T getSNIPPET() {
        return this.SNIPPET;
    }

    /** Plays music from the beginning. */
    public void play() {
        MainActivity.currentMusic = this.SNIPPET;

        player.setOnPreparedListener((v) -> {
            final SeekBar seekBar = this.APP_VIEW.findViewById(R.id.play_current_time);
            seekBar.setMax(player.getDuration() / 1000);
            v.start();
        });

        final ImageView imagePlayButton = this.APP_VIEW.findViewById(R.id.play_button);
        imagePlayButton.setImageResource(R.drawable.ic_pause_24);
        imagePlayButton.setOnClickListener(view -> {
            this.pause();
        });
    }

    /** Stops the currently playing music. */
    public void pause() {
        if (!player.isPlaying()) return;
        player.pause();

        final ImageView imagePlayButton = this.APP_VIEW.findViewById(R.id.play_button);
        imagePlayButton.setImageResource(R.drawable.ic_play_24);
        imagePlayButton.setOnClickListener(view -> {
            player.start();
            imagePlayButton.setImageResource(R.drawable.ic_pause_24);
            imagePlayButton.setOnClickListener(v -> {
                this.pause();
            });
        });
    }

    /** Stops the currently playing music and plays it again from the beginning. */
    public void replay() {
        if (player.isPlaying()) player.stop();
        player.start();
    }

    /**
     * Creates a PlayingAdapter based on the given MusicSnippet and Context.
     *
     * @param context The Context used to create the PlayingAdapter.
     * @param snippet The MusicSnippet used to determine the type of PlayingAdapter to create.
     * @return A PlayingAdapter based on the given MusicSnippet and Context.
     */
    public static PlayingAdapter<?> adapterOf(final AppCompatActivity context, final MusicSnippet snippet) {
        if (snippet instanceof YouTube.YouTubeSnippet) {
            return new YouTube.YouTubePlayingAdapter(context, (YouTube.YouTubeSnippet) snippet);
        } else if (snippet instanceof SoundCloud.SoundCloudSnippet) {
            return new SoundCloud.SoundCloudPlayingAdapter(context, (SoundCloud.SoundCloudSnippet) snippet);
        } else {
            throw new RuntimeException(new Exception(String.format("PlayingAdapter.adapterOf: Unsupported snippet %s", snippet)));
        }
    }
}
