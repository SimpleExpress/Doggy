package com.github.simpleexpress.task;


@FunctionalInterface
public interface TaskInjector
{
    // Dynamically control or affect the execution
    public void run(HttpTask task, HttpJob job);
}
