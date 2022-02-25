package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.CompareNotesRq;
import com.gryphpoem.game.zw.pb.GamePb4.CompareNotesRs;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2020-11-05 11:44
 */
public class CompareNotesHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        CompareNotesRq req = msg.getExtension(CompareNotesRq.ext);
        PlayerService playerService = getService(PlayerService.class);
        CompareNotesRs rs = playerService.compareNotes(getRoleId(), req.getTarget(), req.getHeroIdList());
        if (rs != null) {
            sendMsgToPlayer(CompareNotesRs.ext, rs);
        }
    }
}