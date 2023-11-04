package tech.tenamin.unisound.core.api.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Utility class for handling json as a stream.
 *
 * @author tenamen
 * @since 2023/08/17.
 */
public class JSONUtil {

    /**
     * Creates a stream of JSONObject from the given JSONArray.
     *
     * @param array the JSONArray to create the stream from
     * @return a stream of JSONObject from the JSONArray
     */
    public static Stream<JSONObject> streamOf(final JSONArray array) {
        return IntStream.range(0, array.length())
                .mapToObj(i -> {
                    try {
                        return array.getJSONObject(i);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Constructs a function that retrieves a JSONObject with the specified name from a given JSONObject.
     *
     * @param name the name of the JSONObject to retrieve
     * @return a function that retrieves the specified JSONObject from another JSONObject
     */
    public static Function<JSONObject, JSONObject> getObject(final String name) {
        return i -> {
            try {
                return i.getJSONObject(name);
            } catch (JSONException e) {
                Log.w(String.format("%s JSONException"), e);
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Constructs a predicate that checks if a given JSONObject contains a specific field with the specified name.
     *
     * @param name the name of the field to check for existence in the JSONObject
     * @return a predicate that evaluates to true if the JSONObject contains the specified field, false otherwise
     */
    public static Predicate<JSONObject> hasFilter(final String name) {
        return o -> o.has(name);
    }

    /**
     * Constructs a function that retrieves a String value associated with the specified name from a given JSONObject.
     *
     * @param name name the name of the String value to retrieve
     * @return a function that retrieves the specified String value from the JSONObject
     */
    public static Function<JSONObject, String> getString(final String name) {
        return o -> {
            try {
                return o.getString(name);
            } catch (JSONException e) {
                Log.w(String.format("%s JSONException"), e);
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Constructs a function that retrieves a JSONArray associated with the specified name from a given JSONObject.
     *
     * @param name name the name of the JSONArray to retrieve
     * @return a function that retrieves the specified JSONArray from the JSONObject
     */
    public static Function<JSONObject, JSONArray> getArray(final String name) {
        return o -> {
            try {
                return o.getJSONArray(name);
            } catch (JSONException e) {
                Log.w(String.format("%s JSONException"), e);
                throw new RuntimeException(e);
            }
        };
    }
}
