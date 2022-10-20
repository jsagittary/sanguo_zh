package com.hundredcent.game.zw.cross.world.warfile;

import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-01-20 20:29
 */

@SpringBootTest
public class WarFireTest {

    private ClassPathXmlApplicationContext ac;

    public WarFireTest(){
        ac = new ClassPathXmlApplicationContext("application.xml");
    }

    @Test
    public void testOnSecond(){
        CrossWorldMapDataManager mgr = ac.getBean(CrossWorldMapDataManager.class);

        CrossWorldMapDataManager crossWorldMapDataManager = ac.getBean(CrossWorldMapDataManager.class);
        CrossWorldMap crossWorldMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);

        System.out.println("++++++++++++");
    }


}
