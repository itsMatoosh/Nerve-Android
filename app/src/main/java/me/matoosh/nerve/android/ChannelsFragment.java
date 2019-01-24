package me.matoosh.nerve.android;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChannelsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChannelsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChannelsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private RevealOutlineProvider mOutlineProvider;
    private boolean vibrateIn = false;
    private int ticks = 0;

    public ChannelsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChannelsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChannelsFragment newInstance(String param1, String param2) {
        ChannelsFragment fragment = new ChannelsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_channels, container, false);
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
        void onSectionRevealed();
        void onSectionRevealing();
        void onSectionHidden();
        void onSectionHiding();
    }

    /**
     * Checks whether the reveal animation should snap to full view.
     * @return
     */
    public boolean shouldRevealSnap() {
        if(this.getView().getHeight() == 0) return false;
        return this.mOutlineProvider.getRadius() / this.getView().getHeight() > 0.17f;
    }

    public RevealOutlineProvider getOutlineProvider() {
        return mOutlineProvider;
    }

    /**
     * Animates the full reveal of the view.
     */
    public void revealFull() {
        if(mListener != null) {
            mListener.onSectionRevealing();
        }

        ValueAnimator animator = ValueAnimator.ofInt((int)(getOutlineProvider().getRadius() * 2.7f));
        int mDuration = 2000;
        animator.setDuration(mDuration);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if(mOutlineProvider.radius > getDiagonal()/2) {
                    animator.cancel();
                }
                reveal((int)animation.getAnimatedValue(), ChannelsFragment.this.getView().getWidth()/2, ChannelsFragment.this.getView().getHeight()/2);
            }
        });
        animator.start();
    }

    /**
     * Animates the full reveal of the view.
     */
    public void revealHide() {
        if(mListener != null) {
            mListener.onSectionHiding();
        }

        this.getView().setClipToOutline(true);
        ValueAnimator animator = ValueAnimator.ofInt(400);
        int mDuration = 2000;
        animator.setDuration(mDuration);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if(mOutlineProvider.radius < 0) {
                    animator.cancel();
                }
                reveal(-(int)animation.getAnimatedValue(), ChannelsFragment.this.getView().getWidth()/2, ChannelsFragment.this.getView().getHeight()/2);
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

            onReveal();
            vibrateIn = false;
        } else if(rad < 0 && mOutlineProvider.radius <= 0) {
            //reached full hide
            onHide();
            vibrateIn = false;
        }
        else {
            if (this.mOutlineProvider.getRadius() < 10) {
                this.mOutlineProvider.setPosition(posX, posY);
            } else {
                if (shouldRevealSnap() && !vibrateIn) {
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
                    vibrateIn = true;
                } else if (!shouldRevealSnap() && vibrateIn) {
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
                    vibrateIn = false;
                }
            }

            this.mOutlineProvider.addRadius(rad);

            this.getView().invalidateOutline();
        }
    }
    public void onHide() {
        this.getView().setVisibility(View.GONE);
        this.getView().setClipToOutline(true);
        mOutlineProvider.resetRadius();
        getView().setOnKeyListener(null);

        //call callback
        if(mListener != null) {
            mListener.onSectionHidden();
        }
    }
    public void onReveal() {
        this.getView().setVisibility(View.VISIBLE);
        this.getView().setClipToOutline(false);

        //set back button
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK )
                {
                    revealHide();
                    return true;
                }
                return false;
            }
        } );

        //call callback
        if(mListener != null) {
            mListener.onSectionRevealed();
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
}
