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

package com.landenlabs.demo;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;

import com.landenlabs.demo.R;


public class MainActivity extends FragmentActivity
        implements View.OnClickListener {

    View fragHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(  R.layout.activity_main);

        findViewById(R.id.withAllAsyncTask).setOnClickListener(this);
        findViewById(R.id.withRxJava).setOnClickListener(this);
        fragHolder = findViewById(R.id.fragHolder);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.withAllAsyncTask) {
            WithAllAsyncTask withAllAsyncTask = new WithAllAsyncTask();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragHolder, withAllAsyncTask)
                    .setTransition(TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.withRxJava) {
            WithRxJava withRxJava = new WithRxJava();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragHolder, withRxJava)
                    .setTransition(TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(null)
                    .commit();
        }
    }

}
