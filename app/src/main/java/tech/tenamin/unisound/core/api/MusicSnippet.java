package tech.tenamin.unisound.core.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

/**
 * The snippet object contains basic information about the music (title, description, etc.).
 * This object is only for storing information, not for processing.
 *
 * @author tenamen
 * @since 2023/08/17.
 */
public abstract class MusicSnippet {

    /** The title of music. */
    protected final String TITLE;

    /** The publisher of music. */
    protected final String PUBLISHER;

    /** The date when the music was posted. */
    protected final Date PUBLISHED_AT;

    /** The URL of thumbnail for music. */
    protected String THUMBNAIL;

    public MusicSnippet(
            @NonNull String TITLE,
            @NonNull String PUBLISHER,
            @Nullable Date PUBLISHED_AT,
            @NonNull String THUMBNAIL
    ) {
        this.TITLE = TITLE;
        this.PUBLISHER = PUBLISHER;
        this.PUBLISHED_AT = PUBLISHED_AT;
        this.THUMBNAIL = THUMBNAIL;
    }

    public String getTITLE() {
        return TITLE;
    }

    public String getPUBLISHER() {
        return PUBLISHER;
    }

    public Date getPUBLISHED_AT() {
        return PUBLISHED_AT;
    }

    public String getTHUMBNAIL() {
        return THUMBNAIL;
    }


    @NonNull
    @Override
    public String toString() {
        return "MusicData{" +
                "TITLE='" + TITLE + '\'' +
                ", PUBLISHER='" + PUBLISHER + '\'' +
                ", PUBLISHED_AT=" + PUBLISHED_AT +
                ", THUMBNAIL=" + THUMBNAIL +
                '}';
    }
}
