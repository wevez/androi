package tech.tenamin.unisound.core.api;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * Adapter for downloading music files from data of music snippet.
 *
 * @author tenamen
 * @since 2023/08/17.
 * @param <T> the type of music snippet
 */
public abstract class DownloadingAdapter<T extends MusicSnippet> extends APIAdapter {

    /** Music snippet which is going to be downloaded. */
    private final T SNIPPET;

    /** The destination file which the music file is going to be downloaded. b*/
    private final File DESTINATION;

    public DownloadingAdapter(@NonNull final Context context, @NonNull final T snippet, @NonNull final File destination) {
        super(context);
        this.SNIPPET = snippet;
        this.DESTINATION = destination;
    }

    @NonNull
    public final T getSNIPPET() {
        return this.SNIPPET;
    }

    @NonNull
    public final File getDESTINATION() {
        return this.DESTINATION;
    }

    /**
     * Execute
     */
    public abstract void downloadMusic();
}
