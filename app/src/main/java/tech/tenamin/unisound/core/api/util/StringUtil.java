package tech.tenamin.unisound.core.api.util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Utility class for string manipulation.
 *
 * @author tenamen
 * @since 2023/08/17.
 */
public class StringUtil {

    /**
     * Extracts a substring from the given target string between the first occurrence of the 'first' string
     * (inclusive) and the first occurrence of the 'last' string (exclusive).
     * (Ex: clip("@#Example@#", "@#", "@#") -> "Example")
     *
     * @param target the target string from which to extract the substring
     * @param first the starting string to search for
     * @param last the ending string to search for
     * @return the extracted substring, or an empty string if either 'first' or 'last' is not found in 'target'
     */
    public static String clip(String target, String first, String last) {
        final int startIndex = target.indexOf(first) + first.length();
        return target.substring(startIndex, target.indexOf(last, startIndex));
    }

    /**
     * Encode string to one for URL.
     *
     * @param RAW raw string
     * @return URL encoded string
     */
    public static String encodeKeywordToURL(final String RAW) {
        try {
            return URLEncoder.encode(
                    RAW,
                    "UTF-8"
            );
        } catch (UnsupportedEncodingException e) {
            Log.w("UnsupportedEncodingException", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Decode URL encoded string to decoded one.
     *
     * @param RAW URL encoded string
     * @return decoded string
     */
    public static String decodeKeyword(final String RAW) {
        try {
            return URLDecoder.decode(
                    RAW.replaceAll("%20", " "),
                    "UTF-8"
            );
        } catch (UnsupportedEncodingException e) {
            Log.w("UnsupportedEncodingException", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the given number of seconds into a time format of minutes and seconds (MM:SS).
     *
     * @param SECONDS The number of seconds to be converted.
     * @return The formatted time string in the MM:SS format.
     */
    @SuppressLint("DefaultLocale")
    public static String secondsToMMSS(final int SECONDS) {
        final String A = String.valueOf(SECONDS % 60);
        return String.format(
                "%d:%s",
                SECONDS / 60,
                A.length() == 1 ? A.concat("0") : A
        );
    }
}
