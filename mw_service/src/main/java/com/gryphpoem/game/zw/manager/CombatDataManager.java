package com.gryphpoem.game.zw.manager;

import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.resource.domain.Player;

@Component
public class CombatDataManager {

    /**
     * 是否通关某关卡
     * 
     * @param player
     * @param combatId
     * @return
     */
    public int getCombatId(Player player, int combatId) {
        if (player.combats.containsKey(combatId) && player.combats.get(combatId).getStar() > 0) {
            return 1;
        }
        return 0;
    }
}
