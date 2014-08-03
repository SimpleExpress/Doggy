package com.github.simpleexpress.run;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;


// TODO add more customizations
public class CustomizedHttpClient
{
    public static CloseableHttpClient createDefaultHttpClient()
    {
        return create(null, "Doggy GO");
    }

    public static CloseableHttpClient
    create(CredentialsProvider provider, String userAgent)
    {
        // Setup for multiple connections support, and connection limits as well.
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(200);

        /* SSL Note:
         * 1) Trusting self-signed certificate for testing purpose
         * 2) If step 1) doesn't work, then we need to import its CA
         * 2.1) Open the url with browser, and then export the root CA
         * 2.2) Import the certificate into Java trusted key store (default password is changeit):
         *   jdk/jre/bin/keytool -import -alias root_ca_alias -keystore jdk/jre/lib/security/cacerts -file \
         *   path/to/root-certificate
         */
        SSLContextBuilder sslBuilder = new SSLContextBuilder();
        SSLConnectionSocketFactory sslsf = null;
        try
        {
            sslBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            sslsf = new SSLConnectionSocketFactory(sslBuilder.build());
        }
        catch (Exception e)
        {
            System.out.println("failed to load trusting self signed strategy");
            e.printStackTrace();
        }

        HttpClientBuilder builder = HttpClients
                .custom()
                .setUserAgent(userAgent)
                .setConnectionManager(cm)
                .setDefaultRequestConfig(RequestConfig
                        .custom()
                        .setSocketTimeout(60000)
                        .setConnectTimeout(60000)
                        .setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
                        .build()
                );

        if (provider != null)
        {
            builder.setDefaultCredentialsProvider(provider);
        }

        if (sslsf != null)
        {
            builder.setSSLSocketFactory(sslsf);
        }

        return builder.build();
    }
}
