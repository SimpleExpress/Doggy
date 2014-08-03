package com.github.simpleexpress.run;


import com.github.simpleexpress.task.HttpTask;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.concurrent.*;

public class Runner
{
    private CloseableHttpClient httpClient;
    private ExecutorService pool;
    private CompletionService<HttpTask> executor;


    public Runner(int threadNum)
    {
        this(CustomizedHttpClient.createDefaultHttpClient(), threadNum);
    }

    public Runner(CloseableHttpClient httpClient, int threadNum)
    {
        this.httpClient = httpClient;
        pool = Executors.newFixedThreadPool(threadNum);
        this.executor = new ExecutorCompletionService<>(pool);
    }

    public CloseableHttpClient getHttpClient()
    {
        return httpClient;
    }

    public void push(HttpTask task)
    {
        executor.submit(task);
    }

    public HttpTask pull() throws InterruptedException, ExecutionException
    {
        return executor.take().get();
    }

    public void shutdown()
    {
        pool.shutdown();
    }
}
