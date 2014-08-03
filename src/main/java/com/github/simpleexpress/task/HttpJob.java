package com.github.simpleexpress.task;

import com.github.simpleexpress.page.PageParser;
import com.github.simpleexpress.run.Runner;
import com.github.simpleexpress.util.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class HttpJob
{
    private int taskCounter;
    private Runner runner;
    private JobController jc;
    private List<HttpTask> failures;
    private HttpClientContext context;
    private List<TaskInjector> injectors;

    private final Log log = LogFactory.getLog(getClass());


    public HttpJob(String jobName, int maxRetry, HttpUriRequest entry, Runner runner)
    {
        this(jobName, maxRetry, entry, runner, null);
    }

    public HttpJob(String jobName, int maxRetry, HttpUriRequest entry,
                   Runner runner, List<TaskInjector> injectors)
    {
        this.jc = new JobController(jobName, maxRetry, entry);
        this.injectors = injectors;
        failures = new ArrayList<>();
        context = new HttpClientContext();
        this.runner = runner;
    }

    public HttpJob addTask(String taskName, PageParser parser)
    {
        jc.addTask(taskName, parser);
        return this;
    }

    public PageParser getParserByTaskName(String name) throws NoSuchTaskException
    {
        return jc.getParserByTaskName(name);
    }

   /*
    * The execution follows the steps below:
    * 1. Get the entry of the task and request that entry
    * 2. Get the PageParser based on the task name, and parse the response.
    *    The parsed result will contain the sub task requests if applicable.
    * 3. Put the sub requests into the execution queue
    * 4. Pull the results from the queue and process them.
    * 5. Repeat step 2 - 4
    */
    public void run()
    {
        HttpTask task = new HttpTask(jc.getTaskEntryName(), jc.getMaxRetry(),
                jc.getTaskEntry(), context, runner.getHttpClient());
        task.setMessage(jc.getJobName());
        runner.push(task);
        ++taskCounter;

        processExecutedTask();
        processFailure();
        runner.shutdown();
    }

    void processExecutedTask()
    {
        while (taskCounter > 0)
        {
            HttpTask executed = null;
            HttpResult result = null;
            try
            {
                executed = runner.pull();
                --taskCounter;

                if (injectors != null)
                {
                    for (TaskInjector injector: injectors)
                    {
                        injector.run(executed, this);
                    }
                }

                PageParser parser = jc.getParserByTaskName(executed.getTaskName());
                result = parser.handleResponse(executed.getResponse());
                final String message = makePostMessage(executed.getMessage(), result.getMessage());

                if (! result.isSuccess())
                {
                    if (! executed.reachMaxRetry())
                    {
                        // Keep response connection for retrying so don't close it.
                        executed.incRetryCount();
                        runner.push(executed);
                        log.warn("will retry task: " + executed.getMessage());
                        ++taskCounter;
                    }
                    else
                    {
                        failures.add(executed);
                        executed.closeResponse();
                        logResult(false, message);
                    }
                    continue;
                }

                if (result.getNamedRequestList() == null)
                {
                    executed.closeResponse();
                    logResult(result.isSuccess(), message);
                    continue;
                }

                final String taskName = jc.getNextTaskName(executed.getTaskName());
                result.getNamedRequestList().forEach(pair ->
                {
                    String newMessage = pair.getLeft();
                    HttpUriRequest request = pair.getRight();

                    HttpTask subTask = new HttpTask(taskName, jc.getMaxRetry(),
                            request, context, runner.getHttpClient());
                    subTask.setMessage(makePreMessage(message, newMessage));
                    runner.push(subTask);
                    ++taskCounter;
                });
                executed.closeResponse();
            }
            catch (InterruptedException e)
            {
                log.error("running was interrupted, shutdown now.", e);
                runner.shutdown();
                System.exit(-1);
            }
            catch (ExecutionException e)
            {
                logResult(false, "unknown task failed...");
                log.warn("execution exception occurred while retrieving executed task", e);
            }
            catch (NoSuchTaskException e)
            {
                log.warn("invalid task name found, sub tasks are ignored.\n" +
                        "dump request information for the task:\n" +
                        executed.getRequest().getURI() +
                        "\ndump sub requests information for the task:\n" +
                        StringUtil.toString(result), e);
                executed.closeResponse();
            }
        }
    }

    void processFailure()
    {
        // Do what you want to do here
    }

    private void logResult(boolean success, String message)
    {
        if (success)
        {
            log.info("success~" + message);
        }
        else
        {
            log.warn("fail~" + message);
        }
    }

    // Simply compose the message
    static String makePreMessage(String parent, String preMessage)
    {
        return parent.trim() + "~" + preMessage.trim();
    }

    static String makePostMessage(String parent, String postMessage)
    {
        String message = parent.trim();
        String newer = postMessage.trim();

        return newer.equals("")
                ? message
                : message + "::" + newer;
    }
}
