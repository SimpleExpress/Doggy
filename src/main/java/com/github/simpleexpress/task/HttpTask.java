package com.github.simpleexpress.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.concurrent.Callable;


public class HttpTask implements Callable<HttpTask>
{
    // Statuses
    public static final String NOT_RUN = "not-run";
    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";

    private int tried;
    private int maxRetry;
    private String status;
    private String message;
    private HttpContext context;
    private HttpUriRequest request;
    private CloseableHttpResponse response;
    private CloseableHttpClient httpClient;

    private final String taskName;
    private final Log log = LogFactory.getLog(getClass());


    public HttpTask(String taskName,
                    int maxRetry,
                    HttpUriRequest request,
                    HttpContext context,
                    CloseableHttpClient httpClient)
    {
        tried = 0;
        this.maxRetry = maxRetry;
        message = "";
        status = NOT_RUN;
        this.request = request;
        this.taskName = taskName;
        this.context = context;
        this.httpClient = httpClient;
    }

    public boolean reachMaxRetry()
    {
        return tried >= maxRetry;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getTaskName()
    {
        return taskName;
    }

    public String getStatus()
    {
        return status;
    }

    public CloseableHttpResponse getResponse()
    {
        return response;
    }

    public HttpUriRequest getRequest()
    {
        return request;
    }

    public int getRetryCount()
    {
        return maxRetry;
    }

    public void incRetryCount()
    {
        ++maxRetry;
    }

    @Override
    public HttpTask call()
    {
        if (reachMaxRetry())
        {
            return this;
        }

        status = SUCCESS;
        ++tried;

        try
        {
            response = httpClient.execute(request, context);
        }
        catch (IOException e)
        {
            log.error("error in executing the request", e);
            status = FAILED;
        }

        return this;
    }

    public void closeResponse()
    {
        if (response == null)
        {
            return;
        }

        try
        {
            response.close();
        }
        catch (IOException e)
        {
            log.warn("error found in closing the response", e);
        }
    }
}
