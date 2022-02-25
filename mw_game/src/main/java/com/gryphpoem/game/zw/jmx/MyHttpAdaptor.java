package com.gryphpoem.game.zw.jmx;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.SmartInitializingSingleton;

import mx4j.tools.adaptor.http.HttpAdaptor;
import mx4j.tools.adaptor.http.XSLTProcessor;

/**
 * 继承HttpAdaptor类,实现自己的IP和端口
 * 
 * @author ericSong
 *
 */
public class MyHttpAdaptor extends HttpAdaptor implements SmartInitializingSingleton {
	private Logger logger = Logger.getLogger(this.getClass());;
	private String ip;
	private int port;
	private String user;
	private String pw;
	private String check;

	public MyHttpAdaptor(XSLTProcessor xsltProcessor,int port) {
		try {
			InetAddress addr = InetAddress.getLocalHost();
//			ip = addr.getHostAddress().toString(); // 获取本机ip
			ip = "0.0.0.0";
		} catch (Exception e) {
			logger.error("获取本地IP", e);
		}
		this.port = port;
		this.user = "eric";
		this.pw = "";
		this.check = "none";
		super.setHost(ip);
		super.setPort(port);
		super.setProcessor(xsltProcessor);
	}

	/**
	 * Kick off bean registration automatically after the regular singleton
	 * instantiation phase.
	 * 
	 * @see #registerBeans()
	 */
	@Override
	public void afterSingletonsInstantiated() {
		try {
			logger.info("JMX监听地址------------------------------------http://" + ip + ":" + port);
			this.addAuthorization(user, pw);
			this.setAuthenticationMethod(check);
			this.start();
		} catch (IOException e) {
			logger.error("JMX注册失败", e);
		}
	}
}
