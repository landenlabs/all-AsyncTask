/*
 * Copyright (c) 2020 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Dennis Lang
 * @see http://LanDenLabs.com/
 */

package com.landenlabs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;

import java.util.Locale;

/**
 * A simple layout group that provides a numeric text area with two buttons to
 * increment or decrement the value in the text area. Holding either button
 * will auto increment the value up or down appropriately.
 *
 * @author Jeffrey F. Cole
 */
@SuppressWarnings({"FieldCanBeLocal", "UnusedAssignment"})
public class NumPicker extends LinearLayout {

    private final long REPEAT_DELAY = 50;

    private final int ELEMENT_HEIGHT = LayoutParams.MATCH_PARENT;
    private final int ELEMENT_WIDTH = LayoutParams.WRAP_CONTENT;

    private final int DEF_VALUE = 10;
    private final int DEF_MIN_VALUE = 1;
    private final int DEF_MAX_VALUE = 40;

    public Integer value = DEF_VALUE;
    public int min_value = DEF_MIN_VALUE;
    public int max_value = DEF_MAX_VALUE;

    public EditText valueText;
    ImageView decrement;
    ImageView increment;
    private final Handler repeatUpdateHandler = new Handler();

    private boolean autoIncrement = false;
    private boolean autoDecrement = false;

    public NumPicker(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        // this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        LayoutParams sidesParams = new LayoutParams(150, ELEMENT_HEIGHT);
        LayoutParams centerParams = new LayoutParams(150, ELEMENT_HEIGHT);
        centerParams.bottomMargin = -18;    // TODO use dp

        int defStyleAttr = 0;
        int defStyleRes = 0;
        TypedArray a = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.NumPicker, defStyleAttr, defStyleRes);
        try {
            value = a.getInt(R.styleable.NumPicker_value, DEF_VALUE);
            max_value = a.getInt(R.styleable.NumPicker_max_value, DEF_MAX_VALUE);
            min_value = a.getInt(R.styleable.NumPicker_min_value, DEF_MIN_VALUE);
        } finally {
            a.recycle();
        }

        // init the individual elements
        initDecrementButton(context);
        initValueEditText(context);
        initIncrementButton(context);

        // Can be configured to be vertical or horizontal
        // Thanks for the help, LinearLayout!
        if (getOrientation() == VERTICAL) {
            addView(increment, sidesParams);
            addView(valueText, centerParams);
            addView(decrement, sidesParams);
        } else {
            addView(decrement, sidesParams);
            addView(valueText, centerParams);
            addView(increment, sidesParams);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initIncrementButton(Context context) {
        increment = new ImageView(context);
        increment.setImageResource(R.drawable.ic_increase);
        increment.setForeground(
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.transparent_ripple, context.getTheme()));
        // increment.setTextSize(25);
        // increment.setText("+");

        // Increment once for a click
        increment.setOnClickListener(v -> increment());

        // Auto increment for a long click
        increment.setOnLongClickListener(
                arg0 -> {
                    autoIncrement = true;
                    repeatUpdateHandler.post(new RepetetiveUpdater());
                    return false;
                }
        );

        // When the button is released, if we're auto incrementing, stop
        increment.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && autoIncrement) {
                autoIncrement = false;
            }
            return false;
        });
    }

    private void initValueEditText(Context context) {

        // value = new Integer(0);

        valueText = new EditText(context);
        // valueText.setTextSize(25);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(valueText,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(valueText,
                8, 42, 2, TypedValue.COMPLEX_UNIT_DIP);

        // Since we're a number that gets affected by the button, we need to be
        // ready to change the numeric value with a simple ++/--, so whenever
        // the value is changed with a keyboard, convert that text value to a
        // number. We can set the text area to only allow numeric input, but
        // even so, a carriage return can get hacked through. To prevent this
        // little quirk from causing a crash, store the value of the internal
        // number before attempting to parse the changed value in the text area
        // so we can revert to that in case the text change causes an invalid
        // number
        valueText.setOnKeyListener((v, arg1, event) -> {
            int backupValue = value;
            try {
                value = Integer.parseInt(((EditText) v).getText().toString());
            } catch (NumberFormatException nfe) {
                value = backupValue;
            }
            return false;
        });

        // Highlight the number when we get focus
        valueText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ((EditText) v).selectAll();
            }
        });
        valueText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        valueText.setText(valueStr());
        valueText.setInputType(InputType.TYPE_CLASS_NUMBER);
        valueText.setAllCaps(true);
        valueText.setTypeface(Typeface.MONOSPACE);
        valueText.setIncludeFontPadding(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // valueText.setFirstBaselineToTopHeight(0);
        }
        valueText.setLineSpacing(0, 1.0f);
        valueText.setPadding(0,0,0,0);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initDecrementButton(Context context) {
        decrement = new ImageView(context);
        // decrement = new Button(context);
        // decrement.setTextSize(25);
        // decrement.setText("-");
        decrement.setImageResource(R.drawable.ic_decrease);
        decrement.setForeground(
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.transparent_ripple, context.getTheme()));

        // Decrement once for a click
        decrement.setOnClickListener(v -> decrement());


        // Auto Decrement for a long click
        decrement.setOnLongClickListener(
                arg0 -> {
                    autoDecrement = true;
                    repeatUpdateHandler.post(new RepetetiveUpdater());
                    return false;
                }
        );

        // When the button is released, if we're auto decrementing, stop
        decrement.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && autoDecrement) {
                autoDecrement = false;
            }
            return false;
        });
    }

    public void increment() {
        if (value < max_value) {
            value = value + 1;
            valueText.setText(valueStr());
        }
    }

    public void decrement() {
        if (value > min_value) {
            value = value - 1;
            valueText.setText(valueStr());
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        if (value > max_value) value = max_value;
        if (value < min_value) value = min_value;
        if (value >= 0) {
            this.value = value;
            valueText.setText(valueStr());
        }
    }

    public void setRange(int minValue, int maxValue) {
        min_value = minValue;
        max_value = maxValue;
        setValue(value);
    }

    private String valueStr() {
        return String.format(Locale.getDefault(), "%d", value);
    }

    // =============================================================================================

    /**
     * This little guy handles the auto part of the auto incrementing feature.
     * In doing so it instantiates itself. There has to be a pattern name for
     * that...
     */
    class RepetetiveUpdater implements Runnable {
        public void run() {
            if (autoIncrement) {
                increment();
                repeatUpdateHandler.postDelayed(new RepetetiveUpdater(), REPEAT_DELAY);
            } else if (autoDecrement) {
                decrement();
                repeatUpdateHandler.postDelayed(new RepetetiveUpdater(), REPEAT_DELAY);
            }
        }
    }

}
