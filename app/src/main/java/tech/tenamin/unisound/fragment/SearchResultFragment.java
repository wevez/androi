package tech.tenamin.unisound.fragment;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import tech.tenamin.unisound.R;
import tech.tenamin.unisound.adapter.SearchResultListAdapter;
import tech.tenamin.unisound.core.api.APIAdapter;
import tech.tenamin.unisound.core.api.MusicSnippet;
import tech.tenamin.unisound.core.api.PlayingAdapter;
import tech.tenamin.unisound.core.api.impl.SoundCloud;
import tech.tenamin.unisound.core.api.impl.YouTube;

/**
 * Fragment to display music search results.
 *
 * @author tenamen
 * @since 2023/08/17.
 */
public class SearchResultFragment extends Fragment {

    /** MainActivity to control views of it. */
    private final AppCompatActivity APP_VIEW;

    /** Data instance referenced when displaying search results. */
    private final SearchResultListAdapter RESULT_LIST;

    /** This appears when the search is not yet finished. */
    private ProgressBar progressBar;

    public SearchResultFragment(final AppCompatActivity appView, final String KEYWORD) {

        this.APP_VIEW = appView;

        // Crate a bunch of search result api and execute it.
        RESULT_LIST = new SearchResultListAdapter(this.APP_VIEW, R.layout.search_result_item)
                .addAllApi(
                        new SoundCloud.SoundCloudSearchAPIAdapter(this.APP_VIEW),
                        new YouTube.YouTubeSearchAPIAdapter(this.APP_VIEW)
                )
                .searchAllFromFirst(KEYWORD);

        // Threading an operation that waits until the search process is finished before controlling it.
        new Thread(() -> {

            // Wait until all search api of bundle is finished.
            while (RESULT_LIST.getBUNDLE().stream().anyMatch(APIAdapter::isConnecting));

            // Views can only be controlled from the main thread.
            new Handler(Looper.getMainLooper()).post(() -> {

                // The search process is finished, so the progress bar is invisible.
                progressBar.setVisibility(View.INVISIBLE);

                // Add fetched search result to the list for displaying.
                RESULT_LIST.getBUNDLE().forEach(b -> {
                    b.getSearchResult().forEach(RESULT_LIST::add);
                });
            });
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Create a view instance for search result fragment.
        final View view = inflater.inflate(R.layout.fragment_search_result, container, false);

        // Bind progress bar and make it visible.
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // Set adapter to search result list view.
        final ListView listView = view.findViewById(R.id.search_result_list);
        listView.setAdapter(RESULT_LIST);

        // Define the process when the list of search results is tapped.
        listView.setOnItemClickListener((adapterView, view1, i, l) -> {
            final MusicSnippet selectedSnippet = this.RESULT_LIST.getItem(i);

            // Create a playing adapter instance for the snippet to be played.
            final PlayingAdapter<?> playingAdapter = PlayingAdapter.adapterOf(APP_VIEW, selectedSnippet);

            // Threading because fetching music source from snippet can take some time.
            new Thread(playingAdapter::play).start();

            // Set playback card listeners.
            playingAdapter.setPlaybackCardAppearance();

        });

        return view;
    }
}