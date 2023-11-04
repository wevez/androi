package tech.tenamin.unisound.core.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Volley queue wrapper for API connection.
 *
 * @author tenamen
 * @since 2023/08/17.
 */
public abstract class APIAdapter {

    /** Tag for Android logging system. */
    private static final String TAG = "API Adapter";

    /** Whether to communicate with api or not. */
    protected volatile boolean connecting;

    /** A queue which contains requests to communicate with API */
    protected final RequestQueue QUEUE;

    public APIAdapter(final Context context) {

        // Create a response queue instance.
        this.QUEUE = Volley.newRequestQueue(context);
    }

    public final RequestQueue getQueue() {
        return this.QUEUE;
    }

    public final boolean isConnecting() {
        return this.connecting;
    }

    /**
     * Cancel all tasks in volley queue.
     */
    public final void cancel() {

        if (!this.connecting) {
            Log.w(TAG, "Not connecting now!");
            return;
        }

        this.connecting = false;
        this.QUEUE.cancelAll(e -> true);
    }
}
