package me.matoosh.nerve.android;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import me.matoosh.nerve.android.dummy.DummyContent;
import me.matoosh.nerve.android.watch.WatchFragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * The main activity, facilitates most of the user interaction.
 */
public class Nerve extends AppCompatActivity implements CameraFragment.OnFragmentInteractionListener, NodesFragment.OnListFragmentInteractionListener, DashboardFragment.OnFragmentInteractionListener, WatchFragment.OnFragmentInteractionListener, BlankFragment.OnFragmentInteractionListener {

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    /**
     * The channels fragment.
     */
    private WatchFragment watchFragment;
    /**
     * The camera fragment.
     */
    private CameraFragment cameraFragment;

    //Pager page ids.
    public final int FRIENDS_PAGE = 0;
    public final int CAMERA_PAGE = 1;
    public final int DASHBOARD_PAGE = 2;

    public static final String TAG = "Main View";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nerve);
        //Set unlimited layout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Hiding the watch fragment.
                if(watchFragment.getView().getVisibility() != View.GONE) {
                    watchFragment.onHide();
                }
            }

            @Override
            public void onPageSelected(int position) {
                if(position == 1) {
                    hideSystemUI();
                } else {
                    showSystemUI();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onResume() {
        this.findViewById(R.id.channels_fragment).setVisibility(View.GONE);
        mViewPager.setVisibility(View.VISIBLE);
        mViewPager.setCurrentItem(CAMERA_PAGE, false);
        watchFragment = ((WatchFragment)getSupportFragmentManager().findFragmentById(R.id.channels_fragment));
        cameraFragment = ((CameraFragment)getSupportFragmentManager().findFragmentById(R.id.camera_fragment));

        super.onResume();
    }

    /**
     * Enables the fullscreen mode.
     */
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();

        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars onHide and show.
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    /**
     * Disables the fullscreen mode.
     */
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    //Fragment listeners.
    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }

    @Override
    public void onDownSwipeStart() {
    }

    /**
     * Called while the user is swiping down on the camera view.
     * @param distanceX
     * @param distanceY
     * @param posX
     * @param posY
     */
    @Override
    public void onDownSwipeProgress(int distanceX, int distanceY, int posX, int posY) {
        if(watchFragment != null && (watchFragment.getValueAnimator() == null || (watchFragment.getValueAnimator() != null && !watchFragment.getValueAnimator().isRunning()))) {
            watchFragment.reveal(-distanceY, posX, posY);
        }
    }

    /**
     * Called when the user stops swiping down on the camera view.
     */
    @Override
    public void onDownSwipeStop() {
        if(watchFragment != null) {
            if(watchFragment.getViewModel().isWatchModuleVisible().getValue()) {
                if(!watchFragment.shouldRevealSnap()) {
                    watchFragment.revealFull();
                } else {
                    watchFragment.revealHide();
                }
            } else {
                if(watchFragment.shouldRevealSnap()) {
                    watchFragment.revealFull();
                } else {
                    watchFragment.revealHide();
                }
            }
        }
    }

    /**
     * Called when the channels section has been revealed.
     */
    @Override
    public void onWatchSectionRevealed() {
        //Hiding this section.
        mViewPager.setVisibility(View.GONE);
        showSystemUI();
    }

    @Override
    public void onWatchSectionRevealing() {

    }

    /**
     * Called when the channels section has been hidden.
     */
    @Override
    public void onWatchSectionHidden() {
        mViewPager.setFocusableInTouchMode(true);
        mViewPager.requestFocus();
    }

    @Override
    public void onWatchSectionHiding() {
        if(mViewPager.getVisibility() == View.GONE) {
            mViewPager.setVisibility(View.VISIBLE);
            hideSystemUI();
        }
    }

    @Override
    public boolean onBlankFragmentTouchEvent(MotionEvent event) {
        return cameraFragment.getView().dispatchTouchEvent(event);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if(position == FRIENDS_PAGE) {
                return NodesFragment.newInstance(1);
            } else if(position == CAMERA_PAGE) {
                return BlankFragment.newInstance();
            } else if(position == DASHBOARD_PAGE) {
                return DashboardFragment.newInstance("a", "b");
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}
