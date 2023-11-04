package tech.tenamin.unisound.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tech.tenamin.unisound.R;
import tech.tenamin.unisound.core.api.MusicSnippet;
import tech.tenamin.unisound.core.api.SearchAPIAdapter;
import tech.tenamin.unisound.core.api.util.ImageDownloadTask;

/**
 * ArrayAdapter for SearchResult.
 *
 * @author tenamen
 * @since 2023/08/17.
 */
public class SearchResultListAdapter extends ArrayAdapter<MusicSnippet> {

    /** Bundle of SearchAPI. */
    private final List<SearchAPIAdapter<?>> BUNDLE = new ArrayList<>();

    private final LayoutInflater mInflater;
    private final int mResource;

    public SearchResultListAdapter(@NonNull Context context, int resource) {
        super(context, resource);

        this.mResource = resource;
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Adds a single SearchAPIAdapter to the bundle.
     *
     * @param apiAdapter The SearchAPIAdapter to be added
     * @return The updated SearchResultListAdapter object with the added API adapter
     */
    public SearchResultListAdapter addAPI(final SearchAPIAdapter<?> apiAdapter) {
        this.BUNDLE.add(apiAdapter);
        return this;
    }

    /**
     * Adds multiple SearchAPIAdapters to the bundle.
     *
     * @param apiAdapters SearchAPIAdapters to be added
     * @return The updated SearchResultListAdapter object with the added API adapters
     */
    public SearchResultListAdapter addAllApi(final SearchAPIAdapter<?>... apiAdapters) {
        this.BUNDLE.addAll(Arrays.asList(apiAdapters));
        return this;
    }

    /**
     * Search all bundles for the specified keyword.
     * The search results are stored in the original SearchResultListAdapter object.
     *
     * @param KEYWORD keyword used for search
     * @return SearchResultListAdapter object containing search results
     */
    public SearchResultListAdapter searchAllFromFirst(final String KEYWORD) {
        this.BUNDLE.forEach(b -> b.searchFromStart(KEYWORD));
        return this;
    }

    /**
     * Searches for the next results using all the SearchAPIAdapters in the bundle.
     *
     * @return The updated SearchResultListAdapter object
     */
    public SearchResultListAdapter searchAllNext() {
        this.BUNDLE.forEach(SearchAPIAdapter::searchNext);
        return this;
    }

    public List<SearchAPIAdapter<?>> getBUNDLE() {
        return BUNDLE;
    }

    /**
     * Creates and populates a view for each item in the list.
     *
     * @param position The position of the item in the data set
     * @param convertView The old view to reuse, if possible
     * @param parent The parent view group
     * @return The view for the specified position
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final View view = convertView == null ? mInflater.inflate(mResource, null) : convertView;

        int index = 0;
        for (SearchAPIAdapter<?> searchAPIAdapter : this.BUNDLE) {
            if (searchAPIAdapter.isConnecting()) continue;
            for (MusicSnippet musicSnippet : searchAPIAdapter.getSearchResult()) {
                if (index++ == position) {
                    final TextView titleView = view.findViewById(R.id.title);
                    titleView.setText(musicSnippet.getTITLE());

                    final TextView publisherView = view.findViewById(R.id.publisher);
                    publisherView.setText(musicSnippet.getPUBLISHER());

                    new ImageDownloadTask(view.findViewById(R.id.thumbnail))
                            .execute(musicSnippet.getTHUMBNAIL());

                    break;
                }
            }
        }

        return view;
    }
}
