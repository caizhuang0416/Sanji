package com.caizhuang.stock.pull;

import com.google.common.base.Strings;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * Created by caizhuang on 16/4/28.
 */
public class HTTPPuller  {
    private static Logger LOG = LoggerFactory.getLogger(HTTPPuller.class);
    protected static final HttpClient CLIENT = HttpClients.custom()
            .setConnectionManager(new PoolingHttpClientConnectionManager() {{
                setMaxTotal(12);
                setDefaultMaxPerRoute(getMaxTotal());
            }})
            .build();


    HttpGet request = new HttpGet();
    public String pull(String url, int maxRetry) {
        String res = null;
        while (maxRetry-- > 0) {
            try {
                request.setURI(URI.create(url));
                HttpResponse response = CLIENT.execute(request);
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {

                    for (Header h : response.getAllHeaders()) {
                        System.out.println(h);
                    }
                    continue;
                }
                EntityUtils.toString(response.getEntity());
                res = IOUtils.toString(response.getEntity().getContent());
                break;
            } catch (IOException e) {
                LOG.warn("http error", e);
            } finally {
                if (request != null) request.releaseConnection();
            }
        }

        return res;
    }
    public static void main(String[] args) {
        HTTPPuller puller = new HTTPPuller();
        System.out.println(puller.pull("http://qt.gtimg.cn/q=sh600087", 1));
        System.out.println(puller.pull("http://qt.gtimg.cn/q=sz000859", 1));
    }
}
