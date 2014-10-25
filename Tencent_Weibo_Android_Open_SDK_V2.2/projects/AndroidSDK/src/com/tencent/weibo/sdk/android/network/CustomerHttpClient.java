package com.tencent.weibo.sdk.android.network;
/*
 * http请求公用的httpclient
 * 功能：http请求公用的httpclient，是线程安全的，并支持http和https两种协议
 */
import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

public class CustomerHttpClient {
    private static final String CHARSET = HTTP.UTF_8;
    private static DefaultHttpClient customerHttpClient;

    private CustomerHttpClient() {
    }
    
    public static final String USER_AGENT = "Mozilla/5.0(Linux;U;Android 5.0) AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1";

    public static synchronized DefaultHttpClient getHttpClient() {
        if (null == customerHttpClient) {
            HttpParams params = new BasicHttpParams();
            // 设置一些基本参数
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, CHARSET);
            HttpProtocolParams.setUseExpectContinue(params, true);
			HttpProtocolParams.setUserAgent(params, USER_AGENT);
            ConnManagerParams.setTimeout(params, 45000);
            HttpConnectionParams.setConnectionTimeout(params, 45000);
            HttpConnectionParams.setSoTimeout(params, 45000);
          
            // 设置我们的HttpClient支持HTTP和HTTPS两种模式
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
			customerHttpClient = new DefaultHttpClient(conMgr, params);
        }
        return customerHttpClient;
    }
}