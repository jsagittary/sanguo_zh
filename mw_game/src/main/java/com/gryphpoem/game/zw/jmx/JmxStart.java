package com.gryphpoem.game.zw.jmx;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.MBeanExporter;

public class JmxStart implements InitializingBean {
	private MBeanExporter exporter;
	private MyHttpAdaptor httpAdaptor;

	public MBeanExporter getExporter() {
		return exporter;
	}

	public void setExporter(MBeanExporter exporter) {
		this.exporter = exporter;
	}

	public MyHttpAdaptor getHttpAdaptor() {
		return httpAdaptor;
	}

	public void setHttpAdaptor(MyHttpAdaptor httpAdaptor) {
		this.httpAdaptor = httpAdaptor;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("----------------------------");
	}
}
