package tech.tenamin.unisound.core.api.impl;

import static java.lang.String.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tech.tenamin.unisound.MainActivity;
import tech.tenamin.unisound.core.api.MusicSnippet;
import tech.tenamin.unisound.core.api.PlayingAdapter;
import tech.tenamin.unisound.core.api.SearchAPIAdapter;
import tech.tenamin.unisound.core.api.util.CustomStringRequest;
import tech.tenamin.unisound.core.api.util.JSONUtil;
import tech.tenamin.unisound.core.api.util.StringUtil;

/**
 * A package of SoundCloud API.
 *
 * @author tenamen
 * @since 2023/08/17.
 */
public class SoundCloud {

    /** Tag for Android logging system */
    private static final String TAG = "SoundCloud API Adapter";

    /** This is like a key to communicate with SoundCloud API */
    private static volatile String clientID = null;

    /** Host URL of SoundCloud API */
    private static final String API_HOST = "https://api-v2.soundcloud.com";

    /** This is a data format used in SoundCloud API */
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /** Data formatter instance based on a pattern on SoundCloud API */
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);

    /** The limit number to search result amount. */
    private static final int SEARCH_LIMIT = 20;

    /** Do not let make instance of SoundCloud */
    private SoundCloud() { }

    /** Playing adapter for SoundCloud API */
    public static class SoundCloudPlayingAdapter extends PlayingAdapter <SoundCloudSnippet> {

        public SoundCloudPlayingAdapter(final AppCompatActivity context, SoundCloudSnippet data) {
            super(context, data);
        }

        private void onResponse(final String response) {

            try {

                // Fetch url of the music source from response.
                final JSONObject object = new JSONObject(response);
                final String url = object.getString("url");

                player.setAudioStreamType(AudioManager.STREAM_MUSIC);

                try {
                    player.setDataSource(url);
                } catch (IOException e) {
                    Log.w(String.format("%s IOException", TAG), e);
                    throw new RuntimeException(e);
                }

                player.prepareAsync();
            } catch (JSONException e) {
                Log.w(String.format("%s JSONException", TAG), e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public void play() {
            player.stop();
            player.reset();

            ensureClientId(QUEUE, () -> {

                // Create a request to fetch music download link.
                final Request<?> request = new StringRequest(
                        String.format(
                                "%s?client_id=%s&track_authorization=%s",
                                this.SNIPPET.getTRACK_ID(),
                                clientID,
                                this.SNIPPET.getTRACK_AUTH()
                        ),
                        this::onResponse,
                        null
                );

                this.QUEUE.add(request);
            });
            super.play();
        }
    }

    /**
     * Music snippet for SoundCloud
     */
    public static class SoundCloudSnippet extends MusicSnippet {

        /** These are like keys needed to get audio data on SoundCloud */
        private final String TRACK_ID, TRACK_AUTH;

        public SoundCloudSnippet(
                String TITLE,
                String PUBLISHER,
                Date PUBLISHED_AT,
                final String TRACK_ID,
                final String TRACK_AUTH,
                final String THUMBNAIL
        ) {
            super(TITLE, PUBLISHER, PUBLISHED_AT, THUMBNAIL);
            this.TRACK_AUTH = TRACK_AUTH;
            this.TRACK_ID = TRACK_ID;
        }

        @NonNull
        @Override
        public String toString() {
            return format(
                    "title:%s,publisher:%s,published_at:%s,track_id:%s,track_auth:%s",
                    this.getTITLE(),
                    this.getPUBLISHER(),
                    this.getPUBLISHED_AT().toString(),
                    this.TRACK_ID,
                    this.TRACK_AUTH
            );
        }

        public String getTRACK_ID() {
            return this.TRACK_ID;
        }

        public String getTRACK_AUTH() {
            return this.TRACK_AUTH;
        }
    }

    /**
     * Search API adapter for SoundCloud
     */
    public static class SoundCloudSearchAPIAdapter extends SearchAPIAdapter<SoundCloudSnippet> {

        public SoundCloudSearchAPIAdapter(Context context) {
            super(context);
        }

        @Override
        public void search(@NonNull String KEYWORD, int OFFSET, final boolean FROM_FIRST) {

            // Create a request and add it to queue using client id.
            ensureClientId(this.QUEUE, () -> {
                this.QUEUE.add(
                        this.createRequest(KEYWORD, OFFSET, SEARCH_LIMIT)
                );
            });
        }

        /**
         * Make a Volley request to fetch search items.
         *
         * @param KEYWORD search keyword
         * @param OFFSET search offset of start position
         * @param LIMIT search limit
         * @return request instance to fetch search items
         */
        private Request<?> createRequest(@NonNull String KEYWORD, int OFFSET, int LIMIT) {

            // Create a request based on arguments.
            @SuppressLint("DefaultLocale")
            final StringRequest request = new StringRequest(
                    format(
                        "%s/search?q=%s&client_id=%s&limit=%d&offset=%d",
                        API_HOST,
                        StringUtil.encodeKeywordToURL(KEYWORD),
                        clientID,
                        LIMIT,
                        OFFSET
                    ),
                    this::onResponse,
                    null
            );
            return request;
        }

        /**
         * Parse search response and add search result to adapter's list.
         *
         * @param response search response
         */
        private void onResponse(final String response) {
            // JSONObject creation and data formatting can make exceptions.
            try {

                // Convert response to JSONArray.
                final JSONObject json = new JSONObject(response);
                final JSONArray array = json.getJSONArray("collection");

                // Parse item data from JSONArray.
                JSONUtil.streamOf(array)
                        .filter(JSONUtil.hasFilter("media"))
                        .forEach(o -> {
                            try {
                                // Parse item data from JSONObject.
                                final String title = o.getString("title");
                                final String publisher = o.getJSONObject("user").getString("username");
                                final Date publishedDate = DATE_FORMAT.parse(o.getString("created_at"));
                                final String trackId = o
                                        .getJSONObject("media")
                                        .getJSONArray("transcodings")
                                        .getJSONObject(1)
                                        .getString("url");
                                final String trackAuth = o.getString("track_authorization");
                                final String thumbnail = o.getString("artwork_url");

                                this.SEARCH_RESULT.add(
                                        new SoundCloudSnippet(
                                                StringUtil.decodeKeyword(title),
                                                publisher,
                                                publishedDate,
                                                trackId,
                                                trackAuth,
                                                thumbnail
                                        )
                                );
                            } catch (JSONException e) {
                                Log.w(String.format("%s JSONException", TAG), e);
                                throw new RuntimeException(e);
                            } catch (final ParseException e) {
                                Log.w(format("%s %s", TAG, "Date ParseException"), e);
                                throw new RuntimeException(e);
                            }
                        });
            } catch (final JSONException e) {
                Log.w(format("%s %s", TAG, "JSONException"), e);
                throw new RuntimeException(e);
            }

            // Count up the search offset.
            this.searchOffset += SEARCH_LIMIT;

            this.connecting = false;
        }
    };

    /**
     * We sometimes need a client id to communicate with SoundCloud API.
     * Executing task with this method, you can do it with client id.
     *
     * @param queue queue to be added a request for fetching client id
     * @param runnable Runnable object contains a method using client id
     */
    public static void ensureClientId(final RequestQueue queue, @NonNull final Runnable runnable) {

        // If client id is already fetched, we run as usual.
        if (clientID != null) {
            runnable.run();
            return;
        }

        // Create a request to fetch client id.
        final Request<?> request = new CustomStringRequest(
                "https://soundcloud.com",
                response -> {
                    // HACK
                    final String a = response.split("<script crossorigin src=\\\"https://a-v2.sndcdn.com/assets/")[5];
                    final StringRequest nextRequest = new StringRequest(
                            String.format("https://a-v2.sndcdn.com/assets/%s", a.substring(0, a.indexOf("\""))),
                            r -> {
                                clientID = StringUtil.clip(r, "client_id=", "\"");

                                runnable.run();
                            },
                            null
                    );
                    queue.add(nextRequest);
                },
                null
        );

        queue.add(request);
    }
}
