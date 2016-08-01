package com.example.shiyu.client;

import android.graphics.ColorMatrixColorFilter;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by shiyu on 15/11/20.
 */
public class UIOptimization {

    public final static float[] BT_SELECTED=new float[] {
            2, 0, 0, 0, 2,
            0, 2, 0, 0, 2,
            0, 0, 2, 0, 2,
            0, 0, 0, 1, 0 };


    public final static float[] BT_NOT_SELECTED=new float[] {
            1, 0, 0, 0, 0,
            0, 1, 0, 0, 0,
            0, 0, 1, 0, 0,
            0, 0, 0, 1, 0 };


    public final static View.OnFocusChangeListener buttonOnFocusChangeListener=new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_SELECTED));
                v.setBackgroundDrawable(v.getBackground());
            }
            else
            {
                v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_NOT_SELECTED));
                v.setBackgroundDrawable(v.getBackground());
            }
        }
    };


    public final static View.OnTouchListener buttonOnTouchListener=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_SELECTED));
                v.setBackgroundDrawable(v.getBackground());
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_NOT_SELECTED));
                v.setBackgroundDrawable(v.getBackground());
            }
            return false;
        }
    };


    public final static void setButtonFocusChanged(View inView)
    {
        inView.setOnTouchListener(buttonOnTouchListener);
    }
}
