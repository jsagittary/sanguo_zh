package com.gryphpoem.game.zw.rpc.callback;

import com.gryphpoem.cross.common.CrossResponse;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.Objects;

public class CrossErrorCallback {

    public void checkPlayerExist(int cmd, long roleId, Player player, BasePb.Base.Builder base) throws MwException {
        if (Objects.nonNull(player))
            return;

        base.setCmd(cmd);
        base.setCode(GameError.PLAYER_NOT_EXIST.getCode());
        LogUtil.error(" player not exist: ", roleId, ", cmd: ", cmd);
        throw new MwException();
    }

    public void checkCrossResponse(int cmd, CrossResponse crossResponse, BasePb.Base.Builder base) throws MwException {
        if (Objects.nonNull(crossResponse) && crossResponse.getCode() == GameError.OK.getCode())
            return;

        int code = CheckNull.isNull(crossResponse) ? GameError.CROSS_DATA_NULL.getCode() : crossResponse.getCode();
        LogUtil.error(code, ", message: ", crossResponse.getMessage());
        base.setCmd(cmd);
        base.setCode(code);
        throw new MwException();
    }
}
