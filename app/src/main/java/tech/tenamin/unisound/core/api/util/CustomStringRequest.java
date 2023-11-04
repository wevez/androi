package tech.tenamin.unisound.core.api.util;

import androidx.annotation.Nullable;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * StringRequest class which has a particular user agent.
 *
 * @author tenamen
 * @since 2023/08/17.
 */
public class CustomStringRequest extends StringRequest {

    /** This is a user agent to fetch a client id. */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36 OPR/91.0.4516.72 (Edition GX-CN)";

    public CustomStringRequest(String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    // To use a custom user agent, we need to override getHeaders() and add user agent to it.
    @Override
    public Map<String, String> getHeaders() {

        // Create a request and add user agent to it.
        Map<String, String> headers = new HashMap<>();
        headers.put("User-agent", USER_AGENT);

        return headers;
    }
}
