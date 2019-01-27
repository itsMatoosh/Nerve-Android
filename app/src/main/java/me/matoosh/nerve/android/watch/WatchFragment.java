package me.matoosh.nerve.android.watch;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import me.matoosh.nerve.android.R;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;


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
    public boolean isRevealed = false;
    private GestureDetector gestureDetector;


    public WatchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        gestureDetector = new GestureDetector(getContext(), new GestureHandler());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_watch, container, false);
        mOutlineProvider = new RevealOutlineProvider();
        v.setOutlineProvider(mOutlineProvider);
        return v;
    }

    @Override
    public void onResume() {
        onHide();
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
     * Checks whether the reveal animation should snap to full view.
     * @return
     */
    public boolean shouldRevealSnap() {
        if(this.getView().getHeight() == 0) return false;

        if(!isRevealed)
            return 2*this.mOutlineProvider.getRadius() / getDiagonal() > 0.25f;
        else
            return 2*this.mOutlineProvider.getRadius() / getDiagonal() < 0.7f;
    }

    public RevealOutlineProvider getOutlineProvider() {
        return mOutlineProvider;
    }

    /**
     * Animates the full reveal of the view.
     */
    public void revealFull() {
        if(mListener != null) {
            mListener.onWatchSectionRevealing();
        }

        ValueAnimator animator = ValueAnimator.ofInt((int)(Math.abs(getOutlineProvider().getRadius()) * 2.7f));
        int mDuration = 2000;
        animator.setDuration(mDuration);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if(mOutlineProvider.radius > getDiagonal()/2) {
                    animator.cancel();
                }
                reveal((int)animation.getAnimatedValue(), WatchFragment.this.getView().getWidth()/2, WatchFragment.this.getView().getHeight()/2);
            }
        });
        animator.start();
    }

    /**
     * Animates the full reveal of the view.
     */
    public void revealHide() {
        if(mListener != null) {
            mListener.onWatchSectionHiding();
        }

        ValueAnimator animator = ValueAnimator.ofInt(400);
        int mDuration = 2000;
        animator.setDuration(mDuration);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if(mOutlineProvider.radius < 0) {
                    animator.cancel();
                }
                reveal(-(int)animation.getAnimatedValue(), WatchFragment.this.getView().getWidth()/2, WatchFragment.this.getView().getHeight()/2);
            }
        });
        animator.start();
    }

    /**
     * Reveals the fragment with the given radius parameters.
     * @param rad
     */
    public void reveal(int rad, int posX, int posY) {
        if(!this.isVisible()) this.getView().setVisibility(View.VISIBLE);

        if(rad > 0 && mOutlineProvider.radius >= getDiagonal()/2 && getView().getClipToOutline()) {
            //reached full reveal
            if(!isRevealed) {
                doMediumVibration();
                onReveal();
            }
            vibrateIn = false;
        } else if(rad < 0 && mOutlineProvider.radius <= 0) {
            //reached full hide
            if(isRevealed) {
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
        this.getView().setVisibility(View.GONE);
        this.getView().setClipToOutline(true);
        mOutlineProvider.resetRadius();
        isRevealed = false;
        getView().setOnKeyListener(null);

        //call callback
        if(mListener != null) {
            mListener.onWatchSectionHidden();
        }
    }

    /**
     * Called once the reveal animation finishes.
     */
    public void onReveal() {
        this.getView().setVisibility(View.VISIBLE);
        this.getView().setClipToOutline(false);
        isRevealed = true;

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
        getView().setOnTouchListener((view, motionEvent) -> {
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
     * Returns the diagonal of the view.
     * @return
     */
    public double getDiagonal() {
        if(getView().getHeight() == 0) return 1;
        return Math.sqrt(Math.pow(getView().getHeight(),2) + Math.pow(getView().getWidth(),2));
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
