package dev.conn.overlay;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {
    private int mActivityHeight = 400;

    private int mTouchStartPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        WindowManager.LayoutParams params = getWindow().getAttributes();

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        // params.height = mActivityHeight;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View transparent = findViewById(R.id.transparent);
        View hello = findViewById(R.id.hello);

        findViewById(R.id.handle).setOnTouchListener((view, motionEvent) -> {
            view.performClick();

            View decorView = getWindow().getDecorView();
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) decorView.getLayoutParams();

            boolean consumed = false;
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;

                mTouchStartPos = (int) motionEvent.getRawY();
                Log.d("Peter", "Start pos: " + mTouchStartPos);
                transparent.setVisibility(View.INVISIBLE);

                // Fix the height of the "hello contents".
                ViewGroup.LayoutParams viewParams = hello.getLayoutParams();
                viewParams.height = hello.getMeasuredHeight();
                hello.setLayoutParams(viewParams);

                consumed = true;
                Log.d("Peter", "Match");
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                transparent.setVisibility(View.GONE);


                int touchEndPos = (int) motionEvent.getRawY();
                Log.d("Peter", "End pos: " + touchEndPos);
                int diff = touchEndPos - mTouchStartPos;

                Log.d("Peter", "" + diff);

//                mActivityHeight -= diff;

                ViewGroup.LayoutParams viewParams = hello.getLayoutParams();
                viewParams.height -= diff;
                hello.setLayoutParams(viewParams);

//                params.height = mActivityHeight;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                consumed = true;
                Log.d("Peter", "Wrap");
            } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                int touchEndPos = (int) motionEvent.getRawY();
                int diff = touchEndPos - mTouchStartPos;
                mTouchStartPos = touchEndPos;
                ViewGroup.LayoutParams viewParams = hello.getLayoutParams();
                viewParams.height -= diff;
                hello.setLayoutParams(viewParams);


                consumed = true;
            }

//            decorView.requestLayout();
            getWindowManager().updateViewLayout(decorView, lp);
//            decorView.requestLayout();

            return consumed;
        });
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        View decorView = getWindow().getDecorView();
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) decorView.getLayoutParams();

        try {
            Field privateFlagsField = lp.getClass().getDeclaredField("privateFlags");
            int privateFlags = privateFlagsField.getInt(lp);
            Field noMoveFlag = lp.getClass().getField("PRIVATE_FLAG_NO_MOVE_ANIMATION");
            privateFlags |= noMoveFlag.getInt(null);
            privateFlagsField.setInt(lp, privateFlags);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        getWindowManager().updateViewLayout(decorView, lp);
    }
}