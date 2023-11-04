package tech.tenamin.unisound.core.api;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * API adapter to search items.
 *
 * @author tenamen
 * @since 2023/08/17.
 * @param <E> Type of item to be fetched in a search
 */
public abstract class SearchAPIAdapter<E extends MusicSnippet> extends APIAdapter {

    /** Tag for Android logging system. */
    private static final String TAG = "Search API Adapter";

    /** Last Searched keyword. */
    private String lastKeyword;

    /**
     * The offset of search position.
     * This is counted up by the child class.
     */
    protected int searchOffset;

    /** List to store items of search result. */
    protected final List<E> SEARCH_RESULT = new ArrayList<>();

    public SearchAPIAdapter(Context context) {
        super(context);
    }

    public final List<E> getSearchResult() {
        return this.SEARCH_RESULT;
    }

    /**
     * Search from the first position.
     * Last keyword and search offset will be changed.
     *
     * @param KEYWORD search keyword
     */
    public final void searchFromStart(@NonNull final String KEYWORD) {

        if (this.connecting) {
            Log.w(TAG, "Already connecting!");
            return;
        }

        this.searchOffset = 0;
        this.lastKeyword = KEYWORD;
        this.connecting = true;

        this.search(KEYWORD, this.searchOffset, true);
    }

    /**
     * Search to next page.
     */
    public final void searchNext() {

        if (this.connecting) {
            Log.w(TAG, "Already connecting!");
            return;
        }

        this.connecting = true;
        this.search(this.lastKeyword, this.searchOffset, false);
    }

    /**
     * Create a request to search items and add it to queue.
     * The fetched date will be stored to SEARCH_RESULT.
     *
     * @param KEYWORD search keyword
     * @param OFFSET search offset of start position
     */
    protected abstract void search(@NonNull final String KEYWORD, final int OFFSET, final boolean FROM_START);
}
