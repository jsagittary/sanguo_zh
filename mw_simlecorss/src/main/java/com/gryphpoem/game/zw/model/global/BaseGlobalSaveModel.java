package com.gryphpoem.game.zw.model.global;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @ClassName IDbGlobal.java
 * @Description
 * @author QiuKun
 * @date 2019年6月19日
 */
public interface BaseGlobalSaveModel {

    /**
     * 获取数据
     * 
     * @return
     */
    byte[] getData();

    /**
     * 反序列化
     * 
     * @param data
     */
    void loadData(byte[] data) throws InvalidProtocolBufferException;

    /**
     * 获取类型
     * 
     * @return
     */
    GlobalModelType getModelType();

}
