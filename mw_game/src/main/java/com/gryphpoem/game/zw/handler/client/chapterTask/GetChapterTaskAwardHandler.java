package com.gryphpoem.game.zw.handler.client.chapterTask;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.chapterTask.ChapterTaskService;

import java.util.Optional;

/**
 * desc: TODO
 * author: huangxm
 * date: 2022/5/26 10:24
 **/
public class GetChapterTaskAwardHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb5.GetChapterTaskAwardRs.Builder builder = getService(ChapterTaskService.class).getChapterTaskAward(getRoleId(), msg.getExtension(GamePb5.GetChapterTaskAwardRq.ext).getTaskId());
        Optional.ofNullable(builder)
                .ifPresent(e -> {
                    sendMsgToPlayer(GamePb5.GetChapterTaskAwardRs.EXT_FIELD_NUMBER, GamePb5.GetChapterTaskAwardRs.ext, e.build());
                });
    }
}
