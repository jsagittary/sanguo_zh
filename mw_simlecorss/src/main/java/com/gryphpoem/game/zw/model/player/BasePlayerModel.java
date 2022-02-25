package com.gryphpoem.game.zw.model.player;

import com.gryphpoem.game.zw.model.DataObject;

/**
 * @ClassName BasePlayerModel.java
 * @Description
 * @author QiuKun
 * @date 2019年5月11日
 */
public abstract class BasePlayerModel extends DataObject {

    protected long lordId;

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public abstract PlayerModelType getModelType();

}
