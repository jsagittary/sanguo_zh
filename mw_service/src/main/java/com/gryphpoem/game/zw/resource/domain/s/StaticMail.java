package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticMail.java
 * @Description 邮件模版
 * @author TanDonghai
 * @date 创建时间：2017年4月5日 上午10:53:15
 *
 */
public class StaticMail {
	private int mailId;
	private int type;
	private int chatId;

	public int getChatId() {
		return chatId;
	}

	public void setChatId(int chatId) {
		this.chatId = chatId;
	}

	public int getMailId() {
		return mailId;
	}

	public void setMailId(int mailId) {
		this.mailId = mailId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "StaticMail [mailId=" + mailId + ", type=" + type + "]";
	}

}
