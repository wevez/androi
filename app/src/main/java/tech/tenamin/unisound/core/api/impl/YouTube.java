package tech.tenamin.unisound.core.api.impl;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import tech.tenamin.unisound.MainActivity;
import tech.tenamin.unisound.core.api.MusicSnippet;
import tech.tenamin.unisound.core.api.PlayingAdapter;
import tech.tenamin.unisound.core.api.SearchAPIAdapter;
import tech.tenamin.unisound.core.api.util.CustomStringRequest;
import tech.tenamin.unisound.core.api.util.JSONUtil;
import tech.tenamin.unisound.core.api.util.StringUtil;

/**
 * A package of YouTube API.
 *
 * @author tenamen
 * @since 2023/08/17.
 */
public class YouTube {

    /** Tag for Android logging system */
    private static final String TAG = "YouTube API Adapter";

    /** Video tag for filtering search result */
    private static final String VIDEO_TAG = "EgIQAQ%3D%3D";

    /** The host URL of YouTube */
    private static final String HOST_URL = "https://www.youtube.com";

    /** Do not let make instance of YouTube */
    private YouTube() { }

    /**
     * Playing adapter for YouTube
     */
    public static class YouTubePlayingAdapter extends PlayingAdapter <YouTubeSnippet> {

        public YouTubePlayingAdapter(final AppCompatActivity context, YouTube.YouTubeSnippet data) {
            super(context, data);
        }

        @Override
        public void play() {
            player.stop();
            player.reset();

            // Create a request for Youtube DL API.
            final YoutubeDLRequest request = new YoutubeDLRequest(
                    String.format(
                            "https://www.youtube.com/watch?v=%s",
                            this.SNIPPET.getVIDEO_ID()
                    )
            );

            // Add audio options to request.
            request.addOption("--extract-audio");
            request.addOption("--audio-format", "mp3");
            request.addOption("--extract-audio");

            player.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {

                // Fetch video info through YouTube DL API and set player's data source.
                final VideoInfo info = YoutubeDL.getInstance().getInfo(request);
                player.setDataSource(info.getUrl());
                System.out.println(info.getUrl());
            } catch (YoutubeDLException e) {
                Log.w(String.format("%s YoutubeDLException", TAG), e);
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                Log.w(String.format("%s InterruptedException", TAG), e);
                throw new RuntimeException(e);
            } catch (YoutubeDL.CanceledException e) {
                Log.w(String.format("%s YouTubeDL.CanceledException", TAG), e);
                throw new RuntimeException(e);
            } catch (IOException e) {
                Log.w(String.format("%s IOException", TAG), e);
                throw new RuntimeException(e);
            }

            player.prepareAsync();
            super.play();
        }
    }

    /**
     * Music snippet for YouTube
     */
    public static class YouTubeSnippet extends MusicSnippet {

        /** This is an id which is needed to play music. */
        private final String VIDEO_ID;

        public YouTubeSnippet(String TITLE, String PUBLISHER, Date PUBLISHED_AT, final String VIDEO_ID, final String THUMBNAIL) {
            super(TITLE, PUBLISHER, PUBLISHED_AT, THUMBNAIL);
            this.VIDEO_ID = VIDEO_ID;
        }

        public String getVIDEO_ID() {
            return this.VIDEO_ID;
        }
    }

    /**
     * Search API adapter for YouTube
     */
    public static class YouTubeSearchAPIAdapter extends SearchAPIAdapter<YouTubeSnippet> {

        private String countToken;
        private JSONObject context;
        private String apiToken;

        public YouTubeSearchAPIAdapter(Context context) {
            super(context);
        }

        /**
         * Parse video info and add it to search list.
         *
         * @param array json array which contains video info.
         * @throws JSONException parsing info from json can make json exceptions.
         */
        private void parseAndAddContents(final JSONArray array) throws JSONException {

            // Parse count token from json.
            // The count token will be used to fetch next page data.
            final Optional<String> countTokenOptional = JSONUtil.streamOf(array)
                    .filter(JSONUtil.hasFilter("continuationItemRenderer"))
                    .map(JSONUtil.getObject("continuationItemRenderer"))
                    .map(JSONUtil.getObject("continuationEndpoint"))
                    .map(JSONUtil.getObject("continuationCommand"))
                    .map(JSONUtil.getString("token"))
                    .findAny();

            if (countTokenOptional.isPresent()) countToken = countTokenOptional.get();

            // Parse video info from json.
            JSONUtil.streamOf(array)
                    .filter(JSONUtil.hasFilter("itemSectionRenderer"))
                    .map(JSONUtil.getObject("itemSectionRenderer"))
                    .map(JSONUtil.getArray("contents"))
                    .forEach(contents -> JSONUtil.streamOf(contents)
                            .filter(JSONUtil.hasFilter("videoRenderer"))
                            .map(JSONUtil.getObject("videoRenderer"))
                            .filter(JSONUtil.hasFilter("videoId"))
                            .forEach(videoRenderer -> {
                                try {

                                    // Parse video info from video renderer.
                                    final String videoId = videoRenderer.getString("videoId");
                                    final String thumbnail = videoRenderer
                                            .getJSONObject("thumbnail")
                                            .getJSONArray("thumbnails")
                                            .getJSONObject(0)
                                            .getString("url");
                                    final String title = videoRenderer
                                            .getJSONObject("title")
                                            .getJSONArray("runs")
                                            .getJSONObject(0)
                                            .getString("text");
                                    String publisher = null;
                                    if (
                                            videoRenderer.has("ownerText") &&
                                                    videoRenderer.getJSONObject("ownerText").has("runs")
                                    ) {
                                        publisher = videoRenderer
                                                .getJSONObject("ownerText")
                                                .getJSONArray("runs")
                                                .getJSONObject(0)
                                                .getString("text");
                                    }

                                    // Create a snippet instance and add it to result list.
                                    this.SEARCH_RESULT.add(
                                            new YouTubeSnippet(
                                                    title,
                                                    publisher,
                                                    null,
                                                    videoId,
                                                    thumbnail
                                            )
                                    );

                                    this.searchOffset++;
                                } catch (JSONException e) {
                                    Log.w(String.format("%s JSONException"), e);
                                    throw new RuntimeException(e);
                                }
                            })
                    );

        }

        /**
         * Parse search response and add search result to adapter's list.
         * This is called at first search time of search.
         *
         * @param response search response
         */
        private void onResponseForStart(final String response) {

            final String[] ytInitData = response.split("var ytInitialData =");

            // No search result.
            if (ytInitData.length <= 1) {
                return;
            }

            final String s = ytInitData[1].split("</script>")[0];
            final String data = s.substring(0, s.length() - 1);

            // JSONObject creation and data formatting can make exceptions.
            try {

                final JSONObject initdata = new JSONObject(data);;

                // Parse API token which one is needed to search next page.
                this.apiToken = null;
                if (response.split("innertubeApiKey").length > 0) {
                    this.apiToken = response
                            .split("innertubeApiKey")[1]
                            .trim()
                            .split(",")[0]
                            .split("\"")[2];
                }

                // Parse context which one is needed to search next page.
                this.context = null;
                if (response.split("INNERTUBE_CONTEXT").length > 0) {
                    final String s2 = response
                            .split("INNERTUBE_CONTEXT")[1]
                            .trim();
                    this.context = new JSONObject(
                            s2.substring(2, s2.length() -2)
                    );
                }

                final JSONArray array = initdata
                        .getJSONObject("contents")
                        .getJSONObject("twoColumnSearchResultsRenderer")
                        .getJSONObject("primaryContents")
                        .getJSONObject("sectionListRenderer")
                        .getJSONArray("contents");

                // This token is needed to search next page.
                this.countToken = null;

                this.parseAndAddContents(array);
            } catch (JSONException e) {
                Log.w(String.format("%s JSONException", TAG), e);
                throw new RuntimeException(e);
            }

            this.connecting = false;
        }

        /**
         * Parse search response and add search result to adapter's list.
         * This is called to update search result.
         *
         * @param response search response
         */
        private void onResponseForNext(final JSONObject response) {

            try {
                final JSONObject item1 = response
                        .getJSONArray("onResponseReceivedCommands")
                        .getJSONObject(0)
                        .getJSONObject("appendContinuationItemsAction");

                final JSONArray continuationItems = item1.getJSONArray("continuationItems");
                this.parseAndAddContents(continuationItems);
            } catch (JSONException e) {
                Log.w(String.format("%s JSONException", TAG), e);
                throw new RuntimeException(e);
            }
            this.connecting = false;
        }

        /**
         * Make a volley request to fetch search items and add it to queue.
         *
         * @param KEYWORD search keyword
         * @param OFFSET search offset of start position
         * @return request instance to fetch search items
         */
        @Override
        public void search(@NonNull String KEYWORD, int OFFSET, final boolean FROM_FIRST) {

            // Crate a request instance.
            Request<?> request = null;
            if (FROM_FIRST) {
                request = new CustomStringRequest(
                        String.format(
                                "%s/results?search_query=%s&sp=%s",
                                HOST_URL,
                                StringUtil.encodeKeywordToURL(KEYWORD),
                                VIDEO_TAG
                        ),
                        this::onResponseForStart,
                        null
                );
            } else {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("context", this.context);
                    jsonObject.put("continuation", this.countToken);
                } catch (JSONException e) {
                    Log.w(String.format("%s JSONException"), e);
                    throw new RuntimeException(e);
                }
                request = new JsonObjectRequest(
                        Request.Method.POST,
                        String.format(
                                "%s/youtubei/v1/search?key=",
                                HOST_URL,
                                this.apiToken
                        ),
                        jsonObject,
                        this::onResponseForNext,
                        null
                );
            }
            this.QUEUE.add(request);
        }
    }
}
