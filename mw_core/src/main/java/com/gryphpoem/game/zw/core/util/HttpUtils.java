package com.gryphpoem.game.zw.core.util;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HttpUtils {

	private static final int TIME_OUT = 5;

	/**
	 * 通过HTTP GET 发送参数
	 * 
	 * @param httpUrl
	 * @param parameter
	 */
	public static String sendGet(String httpUrl, Map<String, String> parameter) {
		if (parameter == null || httpUrl == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		Iterator<Map.Entry<String, String>> iterator = parameter.entrySet().iterator();
		while (iterator.hasNext()) {
			if (sb.length() > 0) {
				sb.append('&');
			}
			Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			String value;
			try {
				value = URLEncoder.encode(entry.getValue(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				value = "";
			}
			sb.append(key).append('=').append(value);
		}
		String urlStr = null;
		if (httpUrl.lastIndexOf('?') != -1) {
			urlStr = httpUrl + '&' + sb.toString();
		} else {
			urlStr = httpUrl + '?' + sb.toString();
		}

		HttpURLConnection httpCon = null;
		String responseBody = null;
		try {
			URL url = new URL(urlStr);
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("GET");
			httpCon.setConnectTimeout(TIME_OUT * 1000);
			httpCon.setReadTimeout(TIME_OUT * 1000);
			// 开始读取返回的内容
			InputStream in = httpCon.getInputStream();
			byte[] readByte = new byte[1024];
			// 读取返回的内容
			int readCount = in.read(readByte, 0, 1024);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (readCount != -1) {
				baos.write(readByte, 0, readCount);
				readCount = in.read(readByte, 0, 1024);
			}
			responseBody = new String(baos.toByteArray(), "UTF-8");
			baos.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("sendGet exception");
		} finally {
			if (httpCon != null)
				httpCon.disconnect();
		}
		return responseBody;
	}

	/**
	 * 使用HTTP POST 发送文本
	 * 
	 * @param httpUrl
	 *            发送的地址
	 * @param postBody
	 *            发送的内容
	 * @return 返回HTTP SERVER的处理结果,如果返回null,发送失败
	 */
	public static String sentPost(String httpUrl, String postBody) {
		return sendPost(httpUrl, postBody, "UTF-8", null,1000);
	}

	/**
	 * 使用HTTP POST 发送文本
	 * 
	 * @param httpUrl
	 *            发送的地址
	 * @param postBody
	 *            发送的内容
	 * @return 返回HTTP SERVER的处理结果,如果返回null,发送失败
	 */
	public static String sentPost(String httpUrl, String postBody, String encoding) {
		return sendPost(httpUrl, postBody, encoding, null,1000);
	}

	/**
	 * 使用HTTP POST 发送文本
	 * 
	 * @param httpUrl
	 *            目的地址
	 * @param postBody
	 *            post的包体
	 * @param headerMap
	 *            增加的Http头信息
	 * @return
	 */
	public static String sentPost(String httpUrl, String postBody, Map<String, String> headerMap) {
		return sendPost(httpUrl, postBody, "UTF-8", headerMap,1000);
	}

	/**
	 * 使用HTTP POST 发送文本
	 * @param httpUrl 发送的地址
	 * @param paramMap 发送的内容
	 * @param encoding 发送的内容的编码
	 * @param headerMap 增加的Http头信息
	 * @param waitTime 增加的Http头信息
	 * @return 返回HTTP SERVER的处理结果,如果返回null,发送失败
	 */
	public static String sendPost(String httpUrl, Map<String, String> paramMap,  String encoding, Map<String, String> headerMap,int waitTime) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> entry : paramMap.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		String postBody = sb.toString();
		postBody = postBody.substring(0, postBody.length() - 1);
		return sendPost(httpUrl, postBody, encoding, headerMap, waitTime);
	}

	/**
	 * 使用HTTP POST 发送文本
	 * 
	 * @param httpUrl
	 *            发送的地址
	 * @param postBody
	 *            发送的内容
	 * @param encoding
	 *            发送的内容的编码
	 * @param headerMap
	 *            增加的Http头信息
	 * @return 返回HTTP SERVER的处理结果,如果返回null,发送失败
	 */
	public static String sendPost(String httpUrl, String postBody, String encoding, Map<String, String> headerMap,int waitTime) {
		HttpURLConnection httpCon = null;
		String responseBody = null;
		URL url = null;
		try {
			url = new URL(httpUrl);
		} catch (MalformedURLException e1) {
			System.out.println("URL null");
			e1.printStackTrace();
			return null;
		}
		try {
			httpCon = (HttpURLConnection) url.openConnection();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("openConnection exception");
			return null;
		}
		if (httpCon == null) {
			System.out.println("openConnection null");
			return null;
		}
		httpCon.setDoOutput(true);
		httpCon.setConnectTimeout(TIME_OUT * waitTime);
		httpCon.setReadTimeout(TIME_OUT * waitTime);
		httpCon.setDoOutput(true);
		httpCon.setUseCaches(false);
		try {
			httpCon.setRequestMethod("POST");
		} catch (ProtocolException e1) {
			e1.printStackTrace();
			return null;
		}
		if (headerMap != null) {
			Iterator<Entry<String, String>> iterator = headerMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> entry = iterator.next();
				httpCon.addRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		OutputStream output;
		try {
			output = httpCon.getOutputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		try {
			if (postBody != null) {
				output.write(postBody.getBytes(encoding));
			}

		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		try {
			output.flush();
			output.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

		// 开始读取返回的内容
		InputStream in;
		try {
			in = httpCon.getInputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		/**
		 * 这个方法可以在读写操作前先得知数据流里有多少个字节可以读取。 需要注意的是，如果这个方法用在从本地文件读取数据时，一般不会遇到问题，
		 * 但如果是用于网络操作，就经常会遇到一些麻烦。
		 * 比如，Socket通讯时，对方明明发来了1000个字节，但是自己的程序调用available()方法却只得到900，或者100，甚至是0，
		 * 感觉有点莫名其妙，怎么也找不到原因。 其实，这是因为网络通讯往往是间断性的，一串字节往往分几批进行发送。
		 * 本地程序调用available()方法有时得到0，这可能是对方还没有响应，也可能是对方已经响应了，但是数据还没有送达本地。
		 * 对方发送了1000个字节给你，也许分成3批到达，这你就要调用3次available()方法才能将数据总数全部得到。
		 * 
		 * 经常出现size为0的情况，导致下面readCount为0使之死循环(while (readCount != -1)
		 * {xxxx})，出现死机问题
		 */
		int size = 0;
		try {
			size = in.available();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		if (size == 0) {
			size = 1024;
		}
		byte[] readByte = new byte[size];
		// 读取返回的内容
		int readCount = -1;
		try {
			readCount = in.read(readByte, 0, size);
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (readCount != -1) {
			baos.write(readByte, 0, readCount);
			try {
				readCount = in.read(readByte, 0, size);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		try {
			responseBody = new String(baos.toByteArray(), encoding);
		} catch (UnsupportedEncodingException e) {
			return null;
		} finally {
			if (httpCon != null) {
				httpCon.disconnect();
			}
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return responseBody;
	}

	public static byte[] sendPbByte(String httpUrl, byte[] body) {
		HttpURLConnection httpCon = null;
		byte[] responseBody = null;
		URL url = null;
		try {
			url = new URL(httpUrl);
		} catch (MalformedURLException e1) {
			System.out.println("URL null");
			e1.printStackTrace();
			return null;
		}

		try {
			httpCon = (HttpURLConnection) url.openConnection();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("openConnection exception");
			return null;
		}

		if (httpCon == null) {
			System.out.println("openConnection null");
			return null;
		}

		httpCon.setDoOutput(true);
		httpCon.setConnectTimeout(TIME_OUT * 1000);
		httpCon.setReadTimeout(TIME_OUT * 1000);
		httpCon.setDoOutput(true);
		httpCon.setUseCaches(false);

		try {
			httpCon.setRequestMethod("POST");
		} catch (ProtocolException e1) {
			e1.printStackTrace();
			return null;
		}

		// if (headerMap != null) {
		// Iterator<Entry<String, String>> iterator =
		// headerMap.entrySet().iterator();
		// while (iterator.hasNext()) {
		// Entry<String, String> entry = iterator.next();
		// httpCon.addRequestProperty(entry.getKey(), entry.getValue());
		// }
		// }

		httpCon.addRequestProperty("Content-Type", "application/octet-stream");

		OutputStream output;
		try {
			output = httpCon.getOutputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

		try {
			if (body != null) {
				// System.out.println("send byte lenth:" + body.length);
				output.write(MessageUtil.putShort((short) body.length));

				// System.out.println("head:" +
				// Arrays.toString(PbHelper.putShort((short) body.length)));
				output.write(body);
				// System.out.println("body:" + Arrays.toString(body));
			}
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

		try {
			output.flush();
			output.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

		// 开始读取返回的内容
		InputStream in;
		try {
			in = httpCon.getInputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		/**
		 * 这个方法可以在读写操作前先得知数据流里有多少个字节可以读取。 需要注意的是，如果这个方法用在从本地文件读取数据时，一般不会遇到问题，
		 * 但如果是用于网络操作，就经常会遇到一些麻烦。
		 * 比如，Socket通讯时，对方明明发来了1000个字节，但是自己的程序调用available()方法却只得到900，或者100，甚至是0，
		 * 感觉有点莫名其妙，怎么也找不到原因。 其实，这是因为网络通讯往往是间断性的，一串字节往往分几批进行发送。
		 * 本地程序调用available()方法有时得到0，这可能是对方还没有响应，也可能是对方已经响应了，但是数据还没有送达本地。
		 * 对方发送了1000个字节给你，也许分成3批到达，这你就要调用3次available()方法才能将数据总数全部得到。
		 * 
		 * 经常出现size为0的情况，导致下面readCount为0使之死循环(while (readCount != -1)
		 * {xxxx})，出现死机问题
		 */
		int size = 0;
		try {
			size = in.available();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

		System.out.println("back stream len:" + size);
		if (size == 0) {
			size = 1024;
		}
		byte[] readByte = new byte[size];
		// 读取返回的内容
		int readCount = -1;
		try {
			readCount = in.read(readByte, 0, size);
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (readCount != -1) {
			baos.write(readByte, 0, readCount);
			try {
				readCount = in.read(readByte, 0, size);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		try {
			responseBody = baos.toByteArray();
		} finally {
			if (httpCon != null) {
				httpCon.disconnect();
			}
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return responseBody;
	}

	/**
	 * 向指定 URL 发送POST方法的请求
	 *
	 * @param url
	 *            发送请求的 URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @param type
	 *              1.content-Type 为application/x-www-form-urlencoded
	 *              2.content-Type 为application/json
	 * @return 所代表远程资源的响应结果
	 */
	public static String sendPost(String url, String param,int type) {
		OutputStreamWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			HttpURLConnection conn = null;
			conn = (HttpURLConnection) realUrl.openConnection();
			// 打开和URL之间的连接

			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");    // POST方法


			// 设置通用的请求属性

			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			conn.setConnectTimeout(TIME_OUT * 1000);
			switch (type){
				case 1:
					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					break;
				case 2:
					conn.setRequestProperty("Accept","application/json");
					conn.setRequestProperty("Content-Type", "application/json");
					break;
			}
			conn.setRequestProperty("Accept-Charset", "utf-8");
			conn.connect();

			// 获取URLConnection对象对应的输出流



			out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
			// 发送请求参数
			out.write(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应


			System.out.print("\n getOutputStream :::::"+conn.getInputStream());
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream(),"UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			LogUtil.error("发送 POST 请求出现异常！", e);
			e.printStackTrace();
		}
		//使用finally块来关闭输出流、输入流
		finally{
			try{
				if(out!=null){
					out.close();
				}
				if(in!=null){
					in.close();
				}
			}
			catch(IOException ex){
				ex.printStackTrace();
			}
		}

		return result;
	}
	
}
