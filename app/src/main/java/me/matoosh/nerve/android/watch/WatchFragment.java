package me.matoosh.nerve.android.watch;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import me.matoosh.nerve.android.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WatchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class WatchFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    //Swipe variables.
    private RevealOutlineProvider mOutlineProvider;
    private boolean vibrateIn = false;
    private boolean isSwipingDown = false;
    private GestureDetector gestureDetector;
    private ValueAnimator valueAnimator;

    public static final String TAG = "Watch View";

    public WatchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        gestureDetector = new GestureDetector(getContext(), new GestureHandler());

        getViewModel().getUserAddress(getContext()).observe(this, address -> {
            //Update the location label.
            TextView textView = WatchFragment.this.getActivity().findViewById(R.id.watch_city_name);
            textView.setText(address.getLocality() + ", " + address.getCountryCode());
        });

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_watch, container, false);
        mOutlineProvider = new RevealOutlineProvider();
        v.setOutlineProvider(mOutlineProvider);

        TextView iv = v.findViewById(R.id.watch_city_name);

        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                iv,
                PropertyValuesHolder.ofFloat("scaleX", 1.11f),
                PropertyValuesHolder.ofFloat("scaleY", 1.11f));
        scaleDown.setDuration(1300);

        scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);
        scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleDown.start();

        return v;
    }

    @Override
    public void onResume() {
        //Restore reveal
        restoreReveal();

        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Restores the revealed state of the view panel.
     */
    public void restoreReveal() {
        WatchViewModel model = ViewModelProviders.of(this).get(WatchViewModel.class);
        if(model.isWatchModuleVisible().getValue()) {
            onReveal();
        } else {
            onHide();
        }
    }

    /**
     * Checks whether the reveal animation should snap to full view.
     * @return
     */
    public boolean shouldRevealSnap() {
        if(this.getView().getHeight() == 0) return false;

        if(!getViewModel().isWatchModuleVisible().getValue())
            return 2*this.mOutlineProvider.getRadius() / getDiagonal() > 0.25f;
        else
            return 2*this.mOutlineProvider.getRadius() / getDiagonal() < 0.7f;
    }

    /**
     * Animates the full reveal of the view.
     */
    public void revealFull() {
        valueAnimator = ValueAnimator.ofInt((int)(Math.abs(getOutlineProvider().getRadius()) * 2.7f));
        int mDuration = 2000;
        valueAnimator.setDuration(mDuration);

        valueAnimator.addUpdateListener(animation -> {
            if(mOutlineProvider.radius > getDiagonal()/2 && valueAnimator != null) {
                valueAnimator.cancel();
            }
            reveal((int)animation.getAnimatedValue(), WatchFragment.this.getView().getWidth()/2, WatchFragment.this.getView().getHeight()/2);
        });
        valueAnimator.start();
    }

    /**
     * Animates the full reveal of the view.
     */
    public void revealHide() {
        valueAnimator = ValueAnimator.ofInt(400);
        int mDuration = 2000;
        valueAnimator.setDuration(mDuration);

        valueAnimator.addUpdateListener(animation -> {
            if(mOutlineProvider.radius < 0 && valueAnimator != null) {
                valueAnimator.cancel();

            }
            reveal(-(int)animation.getAnimatedValue(), WatchFragment.this.getView().getWidth()/2, WatchFragment.this.getView().getHeight()/2);
        });
        valueAnimator.start();
    }

    /**
     * Reveals the fragment with the given radius parameters.
     * @param rad
     */
    public void reveal(int rad, int posX, int posY) {
        if(!this.isVisible()) this.getView().setVisibility(View.VISIBLE);
        if(mListener != null) {
            if(rad > 0) {
                mListener.onWatchSectionRevealing();
            } else if(rad < 0) {
                mListener.onWatchSectionHiding();
            }
        }

        if(rad > 0 && mOutlineProvider.radius >= getDiagonal()/2 && getView().getClipToOutline()) {
            //reached full reveal
            if(!getViewModel().isWatchModuleVisible().getValue()) {
                doMediumVibration();
            }
            onReveal();
            vibrateIn = false;
        } else if(rad < 0 && mOutlineProvider.radius <= 0) {
            //reached full hide
            if(getViewModel().isWatchModuleVisible().getValue()) {
                doMediumVibration();
            }
            onHide();
            vibrateIn = false;
        }
        else {
            if(!this.getView().getClipToOutline()) this.getView().setClipToOutline(true);

            if (this.mOutlineProvider.getRadius() < 10) {
                this.mOutlineProvider.setPosition(posX, posY);
            } else {
                if (shouldRevealSnap() && !vibrateIn) {
                    doShortVibration();
                    vibrateIn = true;
                } else if (!shouldRevealSnap() && vibrateIn) {
                    doShortVibration();
                    vibrateIn = false;
                }
            }

            this.mOutlineProvider.addRadius(rad);

            this.getView().invalidateOutline();
        }
    }

    /**
     * Called once the hide animation finishes.
     */
    public void onHide() {
        Log.i(TAG, "Watch view hidden!");
        this.getView().setVisibility(View.GONE);
        this.getView().setClipToOutline(true);
        mOutlineProvider.resetRadius();
        getViewModel().isWatchModuleVisible().setValue(false);
        getView().setOnKeyListener(null);
        getView().setOnTouchListener(null);

        if(valueAnimator != null) {
            if(valueAnimator.isRunning()) {
                valueAnimator.cancel();
            }
            valueAnimator = null;
        }

        //call callback
        if(mListener != null) {
            mListener.onWatchSectionHidden();
        }
    }

    /**
     * Called once the reveal animation finishes.
     */
    public void onReveal() {
        Log.i(TAG, "Watch view revealed!");
        this.getView().setVisibility(View.VISIBLE);
        this.getView().setClipToOutline(false);
        getViewModel().isWatchModuleVisible().setValue(true);

        if(valueAnimator != null) {
            if(valueAnimator.isRunning()) {
                valueAnimator.cancel();
            }
            valueAnimator = null;
        }

        //set back button
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener((v, keyCode, event) -> {
            if( keyCode == KeyEvent.KEYCODE_BACK )
            {
                revealHide();
                return true;
            }
            return false;
        });
        getView().findViewById(R.id.watch_appbar_content).setOnTouchListener((view, motionEvent) -> {
            System.out.println("SSSS");
            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if(isSwipingDown) {
                    mListener.onDownSwipeStop();
                    isSwipingDown = false;
                }
            } else {
                gestureDetector.onTouchEvent(motionEvent);
            }
            return false;
        });

        //call callback
        if(mListener != null) {
            mListener.onWatchSectionRevealed();
        }
    }

    /**
     * Creates a short vibration.
     */
    private void doShortVibration() {
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(v.hasAmplitudeControl()) {
                v.vibrate(VibrationEffect.createOneShot(60, 30));
            } else {
                v.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        } else {
            v.vibrate(10);
        }
    }

    /**
     * Creates a medium vibration.
     */
    private void doMediumVibration() {
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(v.hasAmplitudeControl()) {
                v.vibrate(VibrationEffect.createOneShot(60, 50));
            } else {
                v.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        } else {
            v.vibrate(40);
        }
    }

    /**
     * Gets the viewmodel of the fragment.
     * @return
     */
    public WatchViewModel getViewModel() {
        return ViewModelProviders.of(this).get(WatchViewModel.class);
    }

    /**
     * Returns the diagonal of the view.
     * @return
     */
    public double getDiagonal() {
        if(getView().getHeight() == 0) return 1;
        return Math.sqrt(Math.pow(getView().getHeight(),2) + Math.pow(getView().getWidth(),2));
    }

    public RevealOutlineProvider getOutlineProvider() {
        return mOutlineProvider;
    }
    public ValueAnimator getValueAnimator() { return valueAnimator; }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onWatchSectionRevealed();
        void onWatchSectionRevealing();
        void onWatchSectionHidden();
        void onWatchSectionHiding();

        //Swiping callbacks.
        void onDownSwipeStart();
        void onDownSwipeProgress(int distanceX, int distanceY, int posX, int posY);
        void onDownSwipeStop();
    }

    /**
     * Provides the appropriate reveal outline provider.
     */
    public class RevealOutlineProvider extends ViewOutlineProvider {
        private float radius;
        private float posX;
        private float posY;

        @Override
        public void getOutline(View view, Outline outline) {
            if(view.getWidth() == 0) return;
            float centerX = view.getWidth()/2;
            float centerY = view.getHeight()/2;
            float diffPercent = 1-radius/(view.getWidth()/2);
            if(diffPercent < 0) diffPercent = 0;

            outline.setOval((int)(centerX +(posX - centerX)*diffPercent - radius), (int)(centerY + (posY - centerY)*diffPercent - radius), (int)(centerX + (posX - centerX)*diffPercent + radius), (int)(centerY + (posY - centerY)*diffPercent + radius));
        }

        public void resetRadius() {
            this.radius = -1;
        }

        public void addRadius(int rad) {
            this.radius += rad;
        }

        public float getRadius() {
            return radius;
        }

        public void setPosition(int x, int y) {
            posX = x;
            posY = y;
        }
    }

    /**
     * Detects and handles gestures.
     */
    public class GestureHandler extends android.view.GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(e2.getY() < e1.getY()) return false;
            if(!isSwipingDown) {
                mListener.onDownSwipeStart();
                isSwipingDown = true;
            }

            mListener.onDownSwipeProgress(-(int)distanceX, -(int)distanceY, (int)e2.getX(), (int)e2.getY());
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }
}
