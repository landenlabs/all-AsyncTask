package com.landenlabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainFragment extends androidx.fragment.app.Fragment implements View.OnClickListener {

    TextView resultsTv;
    StringBuilder sb = new StringBuilder();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.testBtn1).setOnClickListener(this);
        view.findViewById(R.id.testBtn2).setOnClickListener(this);
        view.findViewById(R.id.testBtn3).setOnClickListener(this);
        view.findViewById(R.id.clearBtn).setOnClickListener(this);
        resultsTv = view.findViewById(R.id.textResult);

        printLn("Press tasks buttons to start a background job.");
        clear();
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
        int id = view.getId();
        switch (id) {
            case R.id.testBtn1:
                example(5, " TEST1 . ");
                break;
            case R.id.testBtn2:
                example(8, " TEST2 .. ");
                break;
            case R.id.testBtn3:
                example(10, " TEST3 ... ");
                break;
            case R.id.clearBtn:
                clear();
                break;
        }
    }

    // ---------------------------------------------------------------------------------------------
    private void example(int period, String name) {
        RunParams runParams = new RunParams(period, name);
        InitParams initParams = new InitParams();

        AllAsyncTask<InitParams, RunParams, WorkResult> bgTask = new AllAsyncTask<>();

        bgTask
                .init(initParams, task -> { // Optionall perform initialization work on main thread
                    return true;    // Returning false will skip execution and jump to onError.
                })
                .onThread(runParams, task -> { // Do work on background thread
                    try {
                        RunParams params = task.runParams;
                        task.postToMain(task.id + params.msg + " Begin sleeping " + params.period + " secs");

                        // Example work to perform on background worker thread.
                        for (int idx = 0; idx < params.period; idx++) {
                            Thread.sleep(1000);
                            task.postToMain(task.id + "  " + params.msg + " sleeping " + idx);
                        }
                        if (Math.random() < 0.3) {
                            throw new IllegalStateException("Random failure");
                        }
                        task.postToMain(task.id + params.msg + " Sleep completed");


                    } catch (Exception ex) {
                        task.postToMain(ex.getMessage());
                    }

                    return new WorkResult();    // Return result(Nullable).
                })
                .onMessage(msg -> {    // Optionally handle messages
                    printLn(msg.obj.toString());
                })
                .onFinish(task -> {    // Optionally handle task finish
                    RunParams params = task.runParams;
                    if (task.outResult != null) {
                        task.postToMain(task.id + params.msg + "[Task done]");
                    } else {
                        task.postToMain(task.id + params.msg + "[Task failed]");
                    }
                })
                .onError(task -> {     // Optionally handle task exeception
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
