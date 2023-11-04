package tech.tenamin.unisound.fragment;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import jp.wasabeef.glide.transformations.BlurTransformation;
import tech.tenamin.unisound.R;
import tech.tenamin.unisound.core.api.MusicSnippet;

public class PlayingFragment extends Fragment {

    private MusicSnippet CURRENT;
    private final AppCompatActivity APP_VIEW;

    public PlayingFragment(final MusicSnippet CURRENT, final AppCompatActivity APP_VIEW) {
        this.CURRENT = CURRENT;
        this.APP_VIEW = APP_VIEW;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_playing, container, false);

        final ImageView playingBackground = view.findViewById(R.id.playing_background);
        Glide.with(view).load(CURRENT.getTHUMBNAIL())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(100, 3)))
                .into(playingBackground);

        final TextView backText = view.findViewById(R.id.playing_back_button);
        backText.setOnClickListener(v -> {
            final Toolbar toolbar = this.APP_VIEW.findViewById(R.id.toolbar_main);
            toolbar.setVisibility(View.INVISIBLE);
            final View playbackCard = this.APP_VIEW.findViewById(R.id.card_background);
            playbackCard.setVisibility(View.INVISIBLE);
        });

        return view;
    }
}