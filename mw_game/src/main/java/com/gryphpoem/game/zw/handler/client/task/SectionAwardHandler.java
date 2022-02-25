package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.SectionAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.SectionAwardRs;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * @ClassName SectionAwardHandler.java
 * @Description 剧情章节领取奖励
 * @author QiuKun
 * @date 2017年10月26日
 */
public class SectionAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        SectionAwardRq req = msg.getExtension(SectionAwardRq.ext);
        TaskService taskService = getService(TaskService.class);
        SectionAwardRs resp = taskService.sectionAward(getRoleId(), req.getSectionId());
        if (resp != null) {
            sendMsgToPlayer(SectionAwardRs.EXT_FIELD_NUMBER, SectionAwardRs.ext, resp);
        }
    }

}
