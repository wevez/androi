package tech.tenamin.unisound.core.api.util;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * @author tenamen
 * @since 2023/08/17.
 */
public class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {

    /** The ImageView in which the image to be downloaded is set. */
    @SuppressLint("StaticFieldLeak")
    private final ImageView bmImage;

    /** The Runnable which is going to be called after the image is set to ImageView. */
    private final Runnable AFTER_EFFECT;

    public ImageDownloadTask(final ImageView bmImage, final Runnable AFTER_EFFECT) {
        this.bmImage = bmImage;
        this.AFTER_EFFECT = AFTER_EFFECT;
    }

    public ImageDownloadTask(final ImageView bmImage) {
        this(bmImage, () -> {});
    }

    /**
     * Executes downloading image for the given url.
     *
     * @param urls The URL for downloading image.
     * @return Downloaded bitmap(Image data).
     */
    protected Bitmap doInBackground(String... urls) {
        final String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            final InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
        return mIcon11;
    }

    /**
     * Set given Bitmap(Downloaded image) to the ImageView.
     * After that, the after effect is called.
     *
     * @param result The downloaded bitmap.
     */
    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
        this.AFTER_EFFECT.run();
    }
}
