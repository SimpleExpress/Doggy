package com.github.simpleexpress.task;

import com.github.simpleexpress.page.PageParser;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/*
 * A JobController controls the task execution flow.
 * Usually a HttpJob contains several HttpTask, it is called task chain in
 * the JobController, each node in the task chain is a TaskName-PageParser
 * pair. e.g. a simple job contains 2 tasks.
 *     Task 1 -> PageParser 1
 *     Task 2 -> PageParser 2
 * Note that the controller will determine the PageParser by the task name.
 * And the next task will be retrieved from the task chain.
 */
class JobController
{
    private int maxRetry;
    private String jobName;
    private List<String> tasks;
    private HttpUriRequest entry;
    private LinkedHashMap<String, PageParser> taskChain;


    public JobController(String jobName, int maxRetry, HttpUriRequest entry)
    {
        this.jobName = jobName;
        this.maxRetry = maxRetry;
        this.entry = entry;
        taskChain = new LinkedHashMap<>();
        tasks = new ArrayList<>();
    }

    private void reload()
    {
        tasks.clear();
        if (taskChain.size() > 0)
        {
            tasks.addAll(taskChain.keySet());
        }
    }

    public void addTask(String taskName, PageParser parser)
    {
        taskChain.put(taskName, parser);
        reload();
    }

    public String getJobName()
    {
        return jobName;
    }

    public String getTaskEntryName()
    {
        if (tasks.size() > 0)
        {
            return tasks.get(0);
        }
        throw new RuntimeException("No task has been assigned.");
    }

    public int getMaxRetry()
    {
        return maxRetry > 0 ? maxRetry : 0;
    }

    public String getNextTaskName(String taskName) throws NoSuchTaskException
    {
        if (! taskChain.containsKey(taskName))
        {
            throw new NoSuchTaskException(taskName);
        }

        int index = -1;
        for (int i = 0; i < tasks.size(); i++)
        {
            if (tasks.get(i).equals(taskName))
            {
                index = i;
                if (index == tasks.size() - 1)
                {
                    throw new NoSuchTaskException("no subsequent tasks for <" + taskName + ">");
                }
                break;
            }
        }

        return tasks.get(index + 1);
    }

    public HttpUriRequest getTaskEntry()
    {
        return entry;
    }

    public PageParser getParserByTaskName(String taskName) throws NoSuchTaskException
    {
        if (! taskChain.containsKey(taskName))
        {
            throw new NoSuchTaskException(taskName);
        }
        return taskChain.get(taskName);
    }
}
