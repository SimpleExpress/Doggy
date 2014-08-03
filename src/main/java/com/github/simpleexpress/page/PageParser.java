package com.github.simpleexpress.page;

import com.github.simpleexpress.task.HttpResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;


@FunctionalInterface
public interface PageParser extends ResponseHandler<HttpResult>
{
    public HttpResult handleResponse(HttpResponse response);
}
