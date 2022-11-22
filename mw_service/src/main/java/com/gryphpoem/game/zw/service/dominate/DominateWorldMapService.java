package com.gryphpoem.game.zw.service.dominate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-22 21:30
 */
@Component
public class DominateWorldMapService {
    @Autowired
    private List<IDominateWorldMapService> serviceList;


}
