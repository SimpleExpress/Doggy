package com.github.simpleexpress;


import com.github.simpleexpress.run.CustomizedHttpClient;
import com.github.simpleexpress.run.Runner;
import com.github.simpleexpress.task.HttpJob;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;


public final class Doggy
{
    // Example
    public void runTask()
    {
        CloseableHttpClient httpClient = CustomizedHttpClient.createDefaultHttpClient();
        Runner runner = new Runner(httpClient, 5);
        HttpJob job = new HttpJob("Test", 3, new HttpGet("https://v2ex.com"), runner);
        // Implement specific PageParser to replace null value
        job.addTask("Task 1", null);
        job.addTask("Task 2", null);
        job.run();
    }

    public static void go()
    {
        Doggy doggy = new Doggy();
        doggy.runTask();
    }
}
