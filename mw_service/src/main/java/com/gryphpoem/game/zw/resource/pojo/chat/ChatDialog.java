package com.gryphpoem.game.zw.resource.pojo.chat;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Chat;

/**
 * @ClassName ChatDialog.java
 * @Description 聊天对话
 * @author QiuKun
 * @date 2017年9月16日
 */
public class ChatDialog {
    private long targetId;// 会话目标玩家id
    private int state; // 对话状态 0 已读, 1 未读
    private CommonPb.Chat chat; // 最近的一条会话
    private int isCampChatDia; // 是否是阵营邮件会话 0否 1是

    public ChatDialog(long targetId, int state) {
        this.targetId = targetId;
        this.state = state;
    }

    public ChatDialog(long targetId, int state, Chat chat) {
        super();
        this.targetId = targetId;
        this.state = state;
        this.chat = chat;
    }

    public long getTargetId() {
        return targetId;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public CommonPb.Chat getChat() {
        return chat;
    }

    public void setChat(CommonPb.Chat chat) {
        this.chat = chat;
    }

    public int getIsCampChatDia() {
		return isCampChatDia;
	}

	public void setIsCampChatDia(int isCampChatDia) {
		this.isCampChatDia = isCampChatDia;
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (targetId ^ (targetId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ChatDialog other = (ChatDialog) obj;
        if (targetId != other.targetId) return false;
        return true;
    }

    @Override
    public String toString() {
        return "ChatDialog [targetId=" + targetId + ", state=" + state + "]";
    }

}
