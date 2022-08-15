package com.gryphpoem.game.zw.resource.domain.p;

import java.io.Serializable;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-08-12 17:15
 */
public class DbPlayerHero implements Serializable {
    private long lordId;
    private byte[] heroBiography;

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public byte[] getHeroBiography() {
        return heroBiography;
    }

    public void setHeroBiography(byte[] heroBiography) {
        this.heroBiography = heroBiography;
    }
}
