package dev.conn.overlay;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Overlay";

    private int mLastTouchPos;
    private View mContents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        WindowManager.LayoutParams params = getWindow().getAttributes();

        // Set up the Activity to be at the bottom, full width.
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // "transparent" is a transparent view above the handle that expands when the Activity
        // becomes full screen.
        View transparent = findViewById(R.id.transparent);

        // This will be the content of the overlay Activity (everything except the handle).
        mContents = findViewById(R.id.hello);

        findViewById(R.id.handle).setOnTouchListener((view, motionEvent) -> {
            view.performClick();

            View decorView = getWindow().getDecorView();
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) decorView.getLayoutParams();

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                // When the user starts to drag the handle:

                // - Make the Activity full height:
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                getWindowManager().updateViewLayout(decorView, lp);

                // - Make "transparent" take up space:
                transparent.setVisibility(View.INVISIBLE);

                // - Remember where the click was:
                mLastTouchPos = (int) motionEvent.getRawY();

                // If we don't return true on an ACTION_DOWN event we'll don't get an ACTION_UP.
                return true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                // When the user has finished dragging the handle:

                // - Make the Activity WRAP_CONTENTS:
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                getWindowManager().updateViewLayout(decorView, lp);

                // - Make "transparent" go away:
                transparent.setVisibility(View.GONE);

                // - Update the size of contents:
                updateContentsSize((int) motionEvent.getRawY());

                return true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                // When we're dragging the handle, update the size of contents.
                updateContentsSize((int) motionEvent.getRawY());

                return true;
            }

            return false;
        });
    }

    /**
     * Looks at the given y, and updates the size of the "content" view by how much y has changed
     * since the last call.
     */
    private void updateContentsSize(int y) {
        int diff = y - mLastTouchPos;
        mLastTouchPos = y;

        // - Update the height of the contents by that much:
        ViewGroup.LayoutParams viewParams = mContents.getLayoutParams();
        viewParams.height -= diff;
        mContents.setLayoutParams(viewParams);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Copied this from Ted's example, I'm not quite sure if it does anything...

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