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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        if(watchFragment != null) {
            watchFragment.reveal(-distanceY, posX, posY);
        }
    }

    /**
     * Called when the user stops swiping down on the camera view.
     */
    @Override
    public void onDownSwipeStop() {
        if(watchFragment != null) {
            if(watchFragment.isRevealed) {
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

    }

    @Override
    public void onWatchSectionHiding() {
        //Making this section visible.
        mViewPager.setVisibility(View.VISIBLE);
        hideSystemUI();
    }

    @Override
    public boolean onBlankFragmentTouchEvent(MotionEvent event) {
        return cameraFragment.getView().dispatchTouchEvent(event);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_overview, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
        }
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
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}
