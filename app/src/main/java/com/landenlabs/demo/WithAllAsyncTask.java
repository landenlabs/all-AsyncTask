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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.landenlabs.AllAsyncTask;

public class WithAllAsyncTask
        extends androidx.fragment.app.Fragment
        implements View.OnClickListener {

    private TextView resultsTv;
    private NumPicker jobCounterNp;
    private CheckBox verboseCb;
    private StringBuilder sb = new StringBuilder();
    private SharedPreferences pref;

    private static final String PREF_MSG = "Msg";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        pref = requireContext().getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        sb.append(pref.getString(PREF_MSG, ""));
        return inflater.inflate(R.layout.fragment_with_async_task, container, false);
    }

    @Override
    public void onPause() {
        pref.edit().putString(PREF_MSG, sb.toString()).apply();
        super.onPause();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.testBtn1).setOnClickListener(this);
        view.findViewById(R.id.testBtn2).setOnClickListener(this);
        view.findViewById(R.id.testBtn3).setOnClickListener(this);
        view.findViewById(R.id.clearBtn).setOnClickListener(this);
        resultsTv = view.findViewById(R.id.textResult);

        if (sb.length() == 0) {
            printLn("Press tasks buttons to start a background job.");
            sb.delete(0, sb.length());
        } else {
            resultsTv.setText(sb);
        }

        verboseCb = view.findViewById(R.id.verboseCb);
        jobCounterNp = view.findViewById(R.id.jobCount);
    }


    void printLn(String msg) {
        sb.append(msg).append("\n");
        resultsTv.setText(sb);
    }

    void clear() {
        sb.delete(0, sb.length());
        resultsTv.setText("");
    }

    @Override
    public void onClick(View view) {
        JobSpec jobSpec = new JobSpec(verboseCb.isChecked(), jobCounterNp.getValue());

        int id = view.getId();
        if (id == R.id.testBtn1) {
            jobSpec.setPeriod(5).setName(" TEST . ");
        } else if (id == R.id.testBtn2) {
            jobSpec.setPeriod(8).setName(" TEST2 .. ");
        } else if (id == R.id.testBtn3) {
            jobSpec.setPeriod(10).setName(" TEST3 ... ");
        } else if (id == R.id.clearBtn) {
            clear();
            return;
        }
        example(jobSpec);
    }

    // ---------------------------------------------------------------------------------------------
    private void example(JobSpec runParams) {

        long statNano = System.nanoTime();
        // RunParams runParams = new RunParams(period, name);
        InitParams initParams = new InitParams();

        AllAsyncTask<InitParams, JobSpec, WorkResult> bgTask = new AllAsyncTask<>();

        bgTask.init(initParams, task -> { // Optional perform initialization work on main thread
                return true;    // Returning false will skip execution and jump to onError.
            })
            .onThread(runParams, task -> { // Do work on background thread
                long beginWorkNano = System.nanoTime();
                try {
                    task.postToMain(task.id + String.format(" deltaMilli=%,d", (beginWorkNano - statNano)/1000));
                    JobSpec params = task.runParams;

                    if (params.verbose)
                        task.postToMain(task.id + params.name + " Begin sleeping " + params.period + " secs");

                    // Example work to perform on background worker thread.
                    for (int idx = 0; idx < params.period; idx++) {
                        Thread.sleep(1000);
                        if (params.verbose)
                            task.postToMain(task.id + "  " + params.name + " sleeping " + idx);
                    }
                    if (Math.random() < 0.3) {
                        throw new IllegalStateException("Random failure");
                    }

                    if (params.verbose)
                        task.postToMain(task.id + params.name + " Sleep completed");

                } catch (Exception ex) {
                    task.postToMain(ex.getMessage());
                }

                return new WorkResult();    // Return result(Nullable).
            })
            .onMessage(msg -> {    // Optionally handle messages on UI thread
                printLn(msg.obj.toString());
            })
            .onFinish(task -> {    // Optionally handle task finish on UI thread
                JobSpec params = task.runParams;
                long deltaMilli = (System.nanoTime() - statNano)/1000;
                if (task.outResult != null) {
                    task.postToMain(task.id + params.name + "[Task done] " + String.format("%,d", deltaMilli));
                } else {
                    task.postToMain(task.id + params.name + "[Task failed] " + String.format("%,d", deltaMilli));
                }
            })
            .onError(task -> {     // Optionally handle task exception on UI thread
                task.postToMain(task.id + " Task failed " + task.error.getMessage());
            })
            .start()    // Start task, required to get things going.
        ;
    }

    static class InitParams {
        InitParams() {
        }
    }

    static class RunParams {
        int period;
        String msg;

        RunParams(int period, String msg) {
            this.period = period;
            this.msg = msg;
        }
    }

    static class WorkResult {
        WorkResult() {
        }
    }

}
