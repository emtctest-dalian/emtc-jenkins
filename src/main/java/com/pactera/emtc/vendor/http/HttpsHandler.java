package com.pactera.emtc.vendor.http;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

public class HttpsHandler extends HttpHandler{

    protected Protocol myhttps ;

    public HttpsHandler(String host){
        super(host);
        this.setSSL(host,443);
    }

    public HttpsHandler(String host,int port){
        super(host);
        this.setSSL(host,port);
    }

    public void setSSL(String host,int port){
        myhttps = new Protocol("https", (ProtocolSocketFactory)new EasySSLProtocolSocketFactory(), port);
        super.client.getHostConfiguration().setHost(host, port, myhttps);
        Protocol.registerProtocol("https", myhttps);
    }
}
