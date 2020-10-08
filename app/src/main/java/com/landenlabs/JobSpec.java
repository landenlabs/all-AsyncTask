package com.landenlabs;

public class JobSpec {
    public boolean verbose;
    public int jobCount;
    public int period;
    public String name;

    public JobSpec(boolean verbose, int jobCount) {
        this.verbose = verbose;
        this.jobCount = jobCount;
    }

    public JobSpec setPeriod(int period) {
        this.period = period;
        return this;
    }
    public JobSpec setName(String name) {
        this.name = name;
        return this;
    }
}