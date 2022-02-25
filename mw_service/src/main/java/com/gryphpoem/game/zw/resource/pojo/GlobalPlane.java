package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.resource.pojo.chat.Chat;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-16 15:51
 * @description: 战机公共数据, 目前只有最近一次跑马灯
 * @modified By:
 */
public class GlobalPlane {

    // 空军基地最近一次跑马灯
    private Chat lastChat;

    public Chat getLastChat() {
        return lastChat;
    }

    public void setLastChat(Chat lastChat) {
        this.lastChat = lastChat;
    }

}
