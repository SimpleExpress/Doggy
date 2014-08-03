package com.github.simpleexpress.util;

import com.github.simpleexpress.page.FormNotFoundException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;



public class HandlerUtil
{
    public static FormElement
    getForm(Document document, String formAttrName, String formAttrValue)
            throws FormNotFoundException
    {
        Elements elements = document.select("form");
        List<FormElement> forms = elements.forms();

        for (FormElement form: forms)
        {
            if (form.attr(formAttrName).equals(formAttrValue))
            {
                return form;
            }
        }

        throw new FormNotFoundException(String.format(
                "Not found form with given criteria: [%s=%s]",
                formAttrName,
                formAttrValue));
    }

    public static List<NameValuePair> getFormData(FormElement form)
    {
        List<NameValuePair> params = new ArrayList<>();
        form.formData().forEach(kv -> params.add(new BasicNameValuePair(kv.key(), kv.value())));

        return params;
    }

    public static Document
    getDocument(final HttpResponse response, String baseUrl)
            throws HttpResponseException, IOException
    {
        final StatusLine statusLine = response.getStatusLine();
        final HttpEntity entity = response.getEntity();

        if (statusLine.getStatusCode() >= 300)
        {
            EntityUtils.consume(entity);
            throw new HttpResponseException(
                    statusLine.getStatusCode(),
                    statusLine.getReasonPhrase());
        }

        if (entity != null)
        {
            InputStream in = entity.getContent();
            Document doc = Jsoup.parse(in, null, baseUrl);
            in.close();
            return doc;
        }

        throw new IOException("Empty entity found");
    }

}
