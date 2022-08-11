package com.gryphpoem.game.zw.handler.client.chapterTask;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.chapterTask.ChapterTaskService;

import java.util.Optional;

/**
 * desc: TODO
 * author: huangxm
 * date: 2022/5/26 10:21
 **/
public class GetChapterTaskHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb5.GetChapterTaskRs.Builder chapterTask = getService(ChapterTaskService.class).getChapterTask(getRoleId());
        Optional.ofNullable(chapterTask).ifPresent(e -> {
            sendMsgToPlayer(GamePb5.GetChapterTaskRs.EXT_FIELD_NUMBER, GamePb5.GetChapterTaskRs.ext, e.build());
        });
    }
}
