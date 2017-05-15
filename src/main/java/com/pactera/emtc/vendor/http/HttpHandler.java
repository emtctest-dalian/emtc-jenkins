package com.pactera.emtc.vendor.http;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.*;

public class HttpHandler {

    protected HttpClient client;

    public HttpHandler(String host){
        client = new HttpClient();
        client.getHostConfiguration().setHost(host,80,"http");
    }

    public String post(String url, NameValuePair[] pairs){
        PostMethod post = new PostMethod(url);
        post.setRequestBody(pairs);
        try {
            int status  =  client.executeMethod(post);
            return post.getResponseBodyAsString();
        } catch (HttpException e) {
            e.printStackTrace();
            return "{'status':0,'err':'"+e.getMessage()+"'}";
        } catch (IOException e)  {
            e.printStackTrace();
            return "{'status':0,'err':'"+e.getMessage()+"'}";
        }finally{
            post.releaseConnection();
        }
    }

}
