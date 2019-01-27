package me.matoosh.nerve.android;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BlankFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    //Swipe variables.
    private boolean isSwipingDown = false;
    private GestureDetector gestureDetector;

    public BlankFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BlankFragment.
     */
    public static BlankFragment newInstance() {
        BlankFragment fragment = new BlankFragment();
        return fragment;
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
        View v = inflater.inflate(R.layout.fragment_blank, container, false);
        v.setOnTouchListener((view, motionEvent) -> {
            mListener.onBlankFragmentTouchEvent(motionEvent);

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
        return v;
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
        boolean onBlankFragmentTouchEvent(MotionEvent event);
        void onDownSwipeStart();
        void onDownSwipeProgress(int distanceX, int distanceY, int posX, int posY);
        void onDownSwipeStop();
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

            mListener.onDownSwipeProgress((int)distanceX, (int)distanceY, (int)e2.getX(), (int)e2.getY());
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }
}
