package tech.tenamin.unisound;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;

import tech.tenamin.unisound.core.api.MusicSnippet;
import tech.tenamin.unisound.databinding.ActivityMainBinding;
import tech.tenamin.unisound.fragment.ArtistFragment;
import tech.tenamin.unisound.fragment.HistoryFragment;
import tech.tenamin.unisound.fragment.HomeFragment;
import tech.tenamin.unisound.fragment.LikeFragment;
import tech.tenamin.unisound.fragment.PlayingFragment;
import tech.tenamin.unisound.fragment.PlaylistFragment;
import tech.tenamin.unisound.fragment.SearchResultFragment;

/**
 * @author tenamen
 * @since 2023/08/17.
 */
public class MainActivity extends AppCompatActivity {

    public static MusicSnippet currentMusic = null;

    // Used to load the 'unisound' library on application startup.
    static {
        System.loadLibrary("unisound");
    }

    private SearchResultFragment searchResultFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set view of MainActivity.
        super.onCreate(savedInstanceState);
        tech.tenamin.unisound.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize YouTubeDL library.
        try {
            YoutubeDL.getInstance().init(this);
        } catch (YoutubeDLException e) {
            throw new RuntimeException(e);
        }

        // Set search action behavior in toolbar on top.
        final ActionMenuItemView searchAction = findViewById(R.id.search);
        searchAction.setOnClickListener(view -> {
            final EditText searchBox = findViewById(R.id.search_box);
            final TextView appNameView = findViewById(R.id.app_name_text);
            appNameView.setText("←");
            appNameView.setClickable(true);
            appNameView.setOnClickListener(p -> {
                appNameView.setText(R.string.app_name);
                appNameView.setClickable(false);
                final HomeFragment homeFragment = new HomeFragment();
                this.selectedFragment(homeFragment);
                this.searchResultFragment = null;
                searchBox.clearFocus();
                unfocusKeyboard(view);
                searchBox.setVisibility(View.INVISIBLE);
            });
            searchBox.setVisibility(View.VISIBLE);
            if (searchBox.isFocused()) {
                searchBox.clearFocus();
                unfocusKeyboard(view);
                searchBox.setVisibility(View.INVISIBLE);
                appNameView.setText(R.string.app_name);
                appNameView.setClickable(false);
                final HomeFragment homeFragment = new HomeFragment();
                this.selectedFragment(homeFragment);
                this.searchResultFragment = null;
            } else {
                searchBox.requestFocus();
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // Set search box behavior.
        final EditText searchBox = findViewById(R.id.search_box);
        searchBox.setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (((EditText) view).getText().toString().isEmpty()) {
                    searchBox.clearFocus();
                    unfocusKeyboard(view);
                    searchBox.setVisibility(View.INVISIBLE);
                } else {
                    this.searchResultFragment = new SearchResultFragment(this, ((EditText) view).getText().toString());
                    selectedFragment(this.searchResultFragment);
                }
                return true;
            }
            return false;
        });

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            final int itemId = item.getItemId();
            if (itemId == R.id.artist) {
                ArtistFragment artistFragment = new ArtistFragment();
                selectedFragment(artistFragment);
            } else if (itemId == R.id.history) {
                HistoryFragment historyFragment = new HistoryFragment();
                selectedFragment(historyFragment);
            } else if (itemId == R.id.home) {
                if (this.searchResultFragment == null) {
                    HomeFragment homeFragment = new HomeFragment();
                    selectedFragment(homeFragment);
                } else {
                    selectedFragment(this.searchResultFragment);
                }
            } else if (itemId == R.id.like) {
                LikeFragment likeFragment = new LikeFragment();
                selectedFragment(likeFragment);
            } else if (itemId == R.id.playlist) {
                PlaylistFragment playlistFragment = new PlaylistFragment();
                selectedFragment(playlistFragment);
            }
            return true;
        });
        bottomNavigationView.setSelectedItemId(R.id.home);

        final ImageView plabackCardBackground = findViewById(R.id.card_background);
        plabackCardBackground.setOnClickListener(view -> {
            final Toolbar toolbar = findViewById(R.id.toolbar_main);
            toolbar.setVisibility(View.INVISIBLE);
            plabackCardBackground.setVisibility(View.INVISIBLE);
            // TODO makePlaybackCardInvisible()

            final PlayingFragment playingFragment = new PlayingFragment(currentMusic, MainActivity.this);
            this.selectedFragment(playingFragment);
        });
    }

    /**
     * Replaces the current fragment in the frame layout with the specified fragment.
     *
     * @param fragment The fragment to be displayed.
     */
    private void selectedFragment(final Fragment fragment) {
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    /**
     * A native method that is implemented by the 'unisound' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    /**
     * Hides the soft input keyboard from the specified window view.
     *
     * @param view The view associated with the window.
     */
    private void unfocusKeyboard(final View view) {
        final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}