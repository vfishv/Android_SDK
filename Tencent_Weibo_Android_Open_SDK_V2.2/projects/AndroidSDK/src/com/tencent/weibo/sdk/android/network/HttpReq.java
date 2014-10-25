package com.tencent.weibo.sdk.android.network;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;




import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import android.os.AsyncTask;
import android.util.Log;

/**
 * 处理网络请求的抽象类，异步发出网络请求，返回网络数据后并响应回调函数
 */
public abstract class HttpReq extends AsyncTask<Void, Integer, Object> {
	private final String GET = "GET";//Get请求方式
	private final String POST = "POST";//Post请求方式
	protected String mHost = null;//请求主机地址
	protected int mPort = 8088;//请求端口
	protected String mUrl = null;//网络请求地址
	protected String mMethod = null;//定义网络请求方式
	protected ReqParam mParam = new ReqParam();//参数封装类
	protected HttpCallback mCallBack = null;//回调函数
	private int mServiceTag = -1; 
	public void setServiceTag(int nTag) {
		mServiceTag = nTag;
	}

	public int getServiceTag() {
		return mServiceTag;
	}

	/**
	 * 返回回调方法
	 */
	protected HttpCallback getCallBack() {
		return mCallBack;
	}

	/**
	 * 设置网络请求方式的抽象函数
	 * @param method 网络请求方式 post或者get
	 * @throws Exception
	 */
	protected abstract void setReq(HttpRequestBase method) throws Exception;

	/**
	 * 响应网络请求得抽象函数
	 * @param response 网络请求返回的数据
	 * @return Object 返回的数据对象
	 * @throws Exception
	 */
	protected abstract Object processResponse(InputStream response)
			throws Exception;

	/**
	 * 设置网络请求参数封装类的函数
	 * @param param 网络请求参数封装类
	 */
	public void setParam(ReqParam param) {
		mParam = param;
	}

	/**
	 * 添加网络请求参数
	 * @param key 请求参数的名称
	 * @param value 请求参数的值，字符串类型
	 */
	public void addParam(String key, String value) {
		mParam.addParam(key, value);
	}
	
	/**
	 * 添加网络请求参数
	 * @param key 请求参数的名称
	 * @param value 请求参数的值，对象类型
	 */
	public void addParam(String key, Object value) {
		mParam.addParam(key, value);
	}

	/**
	 * 网络请求函数，该函数中通过判断网络请求的方式（post 或者 get）
	 * 发出网络请求，并返回响应的数据结果
	 * @return Object 返回的数据对象
	 * @throws Exception
	 */
	public Object runReq() throws Exception {

		HttpRequestBase method = null;
		int statusCode = -1;
		
		Log.e("QQ weibo", mUrl);

		// 区分POST,GET方法
		if (mMethod.equals(GET)) {
			mUrl += "?"
					+ mParam.toString().substring(0,
							mParam.toString().length() - 1);
			method = new HttpGet(mUrl);
		} else if (mMethod.equals(POST)) {
			if (mParam.getmParams().get("pic") != null) {
				return processResponse(picMethod());
			}

			method = new HttpPost(mUrl);
		} else {
			throw new Exception("unrecognized http method");
		}
		
		Object result = null;
		
		//===========================================================================
	    //HttpHost proxy = new HttpHost(mHost, mPort, "https");

	    //DefaultHttpClient client = new DefaultHttpClient();
	    DefaultHttpClient client = CustomerHttpClient.getHttpClient();
	    try {
	    	//client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

	    	//上边和下面代码取其一 或者都用?
	        //HttpHost target = new HttpHost(mHost, 443, "https");
	        //System.out.println("executing request to " + target + " via " + proxy);
	        //HttpResponse rsp = client.execute(target, method);
	    	setReq(method);
	        HttpResponse rsp = client.execute(method);
	        HttpEntity entity = rsp.getEntity();
	        statusCode = rsp.getStatusLine().getStatusCode();
	        //result = EntityUtils.toString(entity);
	        result = processResponse(entity.getContent());
	    } finally {
	    	//client.getConnectionManager().shutdown();
	    }
		//===========================================================================
		
	    //client.getHostConfiguration().setHost(mHost, mPort, "https");
		//method.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

		// 设置请求body部分
//		setReq(method);
//		statusCode = client.executeMethod(method);
//	    result = processResponse(method.getResponseBodyAsStream());
	    
		//Log.d("result", statusCode + "");
		
		if (statusCode != HttpStatus.SC_OK) {
			return null;
		}

		return result;
	}

	/**
	 * 处理发送图片的函数
	 * 
	 * @return InputStream 发送图片后，网络返回的结果
	 */
//	private InputStream picMethod() {
//		return null;
//	}
	
	private InputStream picMethod() {
		
		DefaultHttpClient client = new DefaultHttpClient();

		InputStream result = null;
		HttpPost method = new HttpPost(mUrl);
		String strparams = mParam.toString();
		try {
			//StringEntity strEnt = null;
			//FileEntity picEnt = null;
			//ArrayList<AbstractHttpEntity> partList = new ArrayList<AbstractHttpEntity>();
			MultipartEntity entity = new MultipartEntity();
//			StringPart strPart = null;
//			FilePart picPart = null;
//			ArrayList<Part> partList = new ArrayList<Part>();
			if (strparams != null && !strparams.equals("")) {
				// 分割文本类型参数字符串
				String[] params = strparams.split("&");
				for (String str : params) {
					if (str != null && !str.equals("")) {
						if (str.indexOf("=") > -1) {
							String[] p = str.split("=");
							// 获取参数值
							String value = (p.length == 2 ? decode(p[1]) : "");
							//strPart = new StringPart(p[0], value, "utf-8");
							//partList.add(strPart);
							entity.addPart(p[0], new StringBody(value, Charset.forName("UTF-8")));
						}
					}
				}
				char[] pics = mParam.getmParams().get("pic").toCharArray();
				byte pic[] = new byte[pics.length];
				for (int i = 0; i < pics.length; i++) {
					pic[i] = (byte) pics[i];
				}
				//picPart = new FilePart("pic", new ByteArrayPartSource("123456.jpg", pic), "image/jpeg", "utf-8");
				//partList.add(picPart);
				ByteArrayBody picBody = new ByteArrayBody(pic, "image/jpeg", "123456.jpg");
				entity.addPart("pic", picBody);
				//entity.addPart("pic", new FileBody(new File("123456.jpg")));
				//picEnt = new FileEntity("pic", new ByteArrayPartSource("123456.jpg", pic), "image/jpeg", "utf-8");

			}
			//Part[] parts = new Part[partList.size()];
			//parts = partList.toArray(parts);
			//MultipartRequestEntity mrp = new MultipartRequestEntity(parts,method.getParams());
			//method.setRequestEntity(mrp);
			method.setEntity(entity);
			HttpResponse httpResponse = client.execute(method);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			//String strResult = EntityUtils.toString(httpResponse.getEntity());
			//result = new StringBufferInputStream(strResult);
			
			result = httpResponse.getEntity().getContent();
			
			//int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				return null;
			}
			//result = method.getResponseBodyAsStream();

		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 转换编码格式
	 * @param s 需要转换的字符串
	 * @return 转换编码后的字符串
	 */
	public static String decode(String s) {
		if (s == null) {
			return "";
		}
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 获取HTTP响应函数
	 * @param response 发出网络请求后返回的HttpResponse
	 * @return 返回的数据流
	 */
	private static InputStream readHttpResponse(HttpResponse response) {
		HttpEntity entity = response.getEntity();
		InputStream inputStream = null;
		try {
			inputStream = entity.getContent();
			Header header = response.getFirstHeader("Content-Encoding");
			if (header != null
					&& header.getValue().toLowerCase().indexOf("gzip") > -1) {
				inputStream = new GZIPInputStream(inputStream);
			}

			return inputStream;
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}
		return inputStream;
	}

	/**
	 * 继承PostMethod类，重写返回RequestCharSet为UTF-8
	 */
	public static class UTF8HttpPost extends HttpPost {
		public UTF8HttpPost(String url) {
			super(url);
		}

//		@Override
//		public String getRequestCharSet() {
//			return "UTF-8";
//		}
	}

	/**
	 *  AsyncTask方法重载
	 *  预执行
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	/**
	 *  AsyncTask方法重载
	 *  后台异步执行
	 */
	@Override
	protected Object doInBackground(Void... params) {
		try {
			Object result = this.runReq();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *  AsyncTask方法重载
	 *  在任务执行成功时回调
	 */
	@Override
	protected void onPostExecute(Object result) {
		if (mCallBack != null) {
			mCallBack.onResult(result);
		}
		HttpService.getInstance().onReqFinish(this);
	}

	/**
	 *  AsyncTask方法重载
	 *  在任务成功取消时回调
	 */
	@Override
	protected void onCancelled() {
		if (mCallBack != null) {
			mCallBack.onResult(null);
		}
		HttpService.getInstance().onReqFinish(this);
	}
	
}
