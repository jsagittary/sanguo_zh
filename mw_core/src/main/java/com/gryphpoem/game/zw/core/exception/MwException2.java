package com.gryphpoem.game.zw.core.exception;

/**
 * @ClassName MwException.java
 * @Description 《现代战争》自定义异常类
 * @author TanDonghai
 * @date 创建时间：2017年3月4日 下午2:59:31
 *
 */
public class MwException2 extends Exception {
	private static final long serialVersionUID = 1L;

	private int code; // 错误码

	public MwException2() {
		super();
	}

	public MwException2(String message) {
		super(message);
	}

	public MwException2(int code, Object... message) {
		super(ExceptionMessage.spliceMessage(message));
		this.code = code;
	}

	public MwException2(String message, Throwable t) {
		super(message, t);
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "MwException [code=" + code + ", message=" + getMessage() + "]";
	}
}
