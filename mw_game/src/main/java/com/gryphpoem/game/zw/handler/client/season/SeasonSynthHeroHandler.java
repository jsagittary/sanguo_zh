package com.gryphpoem.game.zw.handler.client.season;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.session.SeasonHeroService;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-04-19 17:03
 */
public class SeasonSynthHeroHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        GamePb4.SynthSeasonHeroRq req = msg.getExtension(GamePb4.SynthSeasonHeroRq.ext);
        SeasonHeroService service = getService(SeasonHeroService.class);
        GamePb4.SynthSeasonHeroRs resp = service.synthSessionHero(player, req);
        if (resp != null){
            sendMsgToPlayer(GamePb4.SynthSeasonHeroRs.EXT_FIELD_NUMBER, GamePb4.SynthSeasonHeroRs.ext, resp);
        }
    }
}
