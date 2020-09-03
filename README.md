AllAsyncTask - AsyncTask replacement
===

AS of API 30 AsyncTask is deprecated. This simple class replaces AsyncTask.

## Usage

At your MainThread(UIThread), start a background thread just like this:

```java
        // Provided types of Initial, Run and Result parameters. 
        AllAsyncTask<InitParams, RunParams, WorkResult> bgTask = new AllAsyncTask<>();

        bgTask
                .init(initParams, task -> { // Optionall perform initialization work on main thread
                    return true;  // Returning false will skip execution and jump to onError.
                })
                .onThread(runParams, task -> { // Do work on background thread
                    try {
                        RunParams params = task.runParams;
                        task.postToMain(task.id + params.msg + " Begin sleeping " + params.period + " secs");

                        // Example doing work on background worker thread. 
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

                    return new WorkResult();    // result can be null.
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
```

## Reference

Based off work by 
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-SugarTask-green.svg?style=flat)](https://android-arsenal.com/details/1/2590)


## License

    Copyright 2020 Dennis Lang

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
