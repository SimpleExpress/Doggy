package com.github.simpleexpress.page;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;


public class LiteResponseValidator
{
    private static final Log log = LogFactory.getLog(LiteResponseValidator.class);

    public static boolean pageContainsWhenSuccess(String expected, HttpResponse response)
    {
        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() < 300)
        {
            try
            {
                HttpEntity entity = response.getEntity();
                String html = EntityUtils.toString(entity);
                return html.contains(expected);
            }
            catch (Exception e)
            {
                log.warn("Error in processing the response", e);
                return false;
            }
        }

        log.warn("Not a success return code: " + status.getStatusCode());
        return false;
    }
}
