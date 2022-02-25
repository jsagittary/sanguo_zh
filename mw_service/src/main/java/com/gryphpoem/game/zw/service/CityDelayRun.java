package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayRun;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.pojo.world.City;

/**
 * @author xwind
 * @date 2021/9/2
 */
public class CityDelayRun implements DelayRun {

    private City city;
    private StaticCity staticCity;
    private Player player;

    public CityDelayRun(City city, StaticCity staticCity, Player player) {
        this.city = city;
        this.staticCity = staticCity;
        this.player = player;
    }

    @Override
    public int deadlineTime() {
        return city.getLeaveOver();
    }

    @Override
    public void deadRun(int runTime, DelayInvokeEnvironment env) {
        if(env instanceof CityService){
            CityService cityService = (CityService) env;
            cityService.doLeave(city,staticCity,player);
        }
    }
}
