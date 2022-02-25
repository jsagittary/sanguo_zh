package com.gryphpoem.game.zw.handler.client.cross.worldwar;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonShopService;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarSeasonShopGoodsRq;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarSeasonShopGoodsRs;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * Created by pengshuo on 2019/4/4 17:30
 * <br>Description: 玩家世界争霸-世界阵营-商店物品获取
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class GetWorldWarSeasonShopGoodsHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        WorldWarSeasonShopGoodsRq req = msg.getExtension(WorldWarSeasonShopGoodsRq.ext);
        WorldWarSeasonShopService service = getService(WorldWarSeasonShopService.class);
        WorldWarSeasonShopGoodsRs rs = service.buySeasonShopEquip(getRoleId(),req.getKeyId());
        if (!CheckNull.isNull(rs)) {
            sendMsgToPlayer(WorldWarSeasonShopGoodsRs.ext,rs);
        }
    }
}
