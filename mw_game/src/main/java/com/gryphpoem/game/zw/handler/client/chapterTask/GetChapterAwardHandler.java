package com.gryphpoem.game.zw.handler.client.chapterTask;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.chapterTask.ChapterTaskService;

import java.util.Optional;

/**
 * desc: TODO
 * author: huangxm
 * date: 2022/5/26 10:30
 **/
public class GetChapterAwardHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb5.GetChapterAwardRs.Builder builder = getService(ChapterTaskService.class).getChapterAward(getRoleId());
        Optional.ofNullable(builder).ifPresent(e -> {
            sendMsgToPlayer(GamePb5.GetChapterAwardRs.EXT_FIELD_NUMBER, GamePb5.GetChapterAwardRs.ext, e.build());
        });
    }
}
