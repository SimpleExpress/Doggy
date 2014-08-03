package com.github.simpleexpress.task;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;

import java.util.List;


public class HttpResult
{
    private boolean success;
    private String message;  // The message will be logged for further analysis
    private List<Pair<String, ? extends HttpUriRequest>> namedRequestList = null;


    public HttpResult(boolean success,
                      List<Pair<String, ? extends HttpUriRequest>> requests)
    {
        this(success, "", requests);
    }

    public HttpResult(boolean success,
                      String message,
                      List<Pair<String, ? extends HttpUriRequest>> requests)
    {
        this.success = success;
        this.message = message;
        this.namedRequestList = requests;
    }

    public String getMessage()
    {
        return message;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public List<Pair<String, ? extends HttpUriRequest>> getNamedRequestList()
    {
        return namedRequestList;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(success ? " succeeded" : " failed");

        if (namedRequestList == null || namedRequestList.size() <= 0)
        {
            sb.append(" without sub http tasks. Message = ");
            sb.append(message);
        }
        else
        {
            sb.append(String.format(" with %d sub http tasks. Message = %s\n\n",
                    namedRequestList.size(), message));
            sb.append("==========================================" +
                    "======================================\n");

            namedRequestList.forEach(nameRequestPair ->
            {
                String name = nameRequestPair.getLeft();
                HttpUriRequest request = nameRequestPair.getRight();
                sb.append(name);
                sb.append("\n");
                sb.append(request.getURI());
                sb.append("\nparameters: ");
                try
                {
                    HttpEntity entity = ((HttpPost) request).getEntity();
                    List<NameValuePair> data = URLEncodedUtils.parse(entity);
                    data.forEach(pair ->
                    {
                        sb.append("\n");
                        sb.append(pair.getName());
                        sb.append(": ");
                        sb.append(pair.getValue());
                    });
                    sb.append("\n\n");
                }
                catch (Exception e)
                {
                    sb.append("not exist.\n\n");
                }
            });
        }

        return sb.toString();
    }
}
