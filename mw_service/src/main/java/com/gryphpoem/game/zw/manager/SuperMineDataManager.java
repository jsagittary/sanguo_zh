package com.gryphpoem.game.zw.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.world.SuperGuard;

/**
 * @ClassName SuperMineDataManager.java
 * @Description 超级矿点处理
 * @author QiuKun
 * @date 2018年7月25日
 */
@Component
public class SuperMineDataManager {

    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;

    /**
     * 赋值玩家对象中的army
     */
    public void loadSuperMineArmy() {
        worldDataManager.getSuperMineCampMap().values().stream().flatMap(List::stream).forEach(sm -> {
            List<SuperGuard> collectArmy = sm.getCollectArmy();
            Iterator<SuperGuard> collectIt = collectArmy.iterator();
            while (collectIt.hasNext()) {
                SuperGuard sg = collectIt.next();
                Army oldArmy = sg.getArmy();
                Player player = playerDataManager.getPlayer(oldArmy.getLordId());
                if (player == null) {
                    collectIt.remove();
                    continue;
                }
                Army army = player.armys.get(oldArmy.getKeyId());
                if (army == null) {
                    collectIt.remove();
                    continue;
                }
                sg.setArmy(army);// 赋值新的army
            }
            List<Army> helpArmyList = new ArrayList<>();
            for (Army oldArmy : sm.getHelpArmy()) {
                Player player = playerDataManager.getPlayer(oldArmy.getLordId());
                if (player == null) {
                    continue;
                }
                Army army = player.armys.get(oldArmy.getKeyId());
                if (army == null) {
                    continue;
                }
                helpArmyList.add(army);
            }
            sm.getHelpArmy().clear();
            sm.getHelpArmy().addAll(helpArmyList);
        });
    }

}
