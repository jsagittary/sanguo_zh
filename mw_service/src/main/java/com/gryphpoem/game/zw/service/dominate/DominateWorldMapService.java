package com.gryphpoem.game.zw.service.dominate;

import com.gryphpoem.game.zw.core.eventbus.Subscribe;
import com.gryphpoem.game.zw.core.eventbus.ThreadMode;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideCity;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideGovernor;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.SiLiDominateWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.StateDominateWorldMap;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.GamePb8;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.EventRegisterService;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.List;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-22 21:30
 */
@Component
public class DominateWorldMapService implements EventRegisterService, GmCmdService {
    @Autowired
    private List<IDominateWorldMapService> serviceList;
    @Autowired
    private WorldDataManager worldDataManager;

    private volatile SerializePb.SerDominateData ser;

    private IDominateWorldMapService getService(int functionId) {
        if (CheckNull.isEmpty(serviceList)) return null;
        return serviceList.stream().filter(service -> service.getWorldMapFunction() == functionId).findFirst().orElse(null);
    }

    /**
     * 获取地体活动信息
     *
     * @param roleId
     * @param req
     * @return
     */
    public GamePb8.GetDominateWorldMapInfoRs getDominateWorldMapInfo(long roleId, GamePb8.GetDominateWorldMapInfoRq req) {
        IDominateWorldMapService service = getService(req.getWorldFunction());
        if (CheckNull.isNull(service)) {
            throw new MwException(GameError.PARAM_ERROR, "未找到处理类, req: ", req.getWorldFunction());
        }
        return service.getDominateWorldMapInfo(roleId, req);
    }

    /**
     * 攻击某个雄踞一方城池
     *
     * @param roleId
     * @param req
     * @return
     */
    public GamePb8.AttackDominateCityRs attackDominateCity(long roleId, GamePb8.AttackDominateCityRq req) {
        IDominateWorldMapService service = getService(req.getWorldFunction());
        if (CheckNull.isNull(service)) {
            throw new MwException(GameError.PARAM_ERROR, "未找到处理类, req: ", req.getWorldFunction());
        }
        return service.attackDominateCity(roleId, req);
    }

    /**
     * 获取城池详情
     *
     * @param roleId
     * @param req
     * @return
     */
    public GamePb8.GetDominateDetailRs getDominateDetail(long roleId, GamePb8.GetDominateDetailRq req) {
        IDominateWorldMapService service = getService(req.getWorldFunction());
        if (CheckNull.isNull(service)) {
            throw new MwException(GameError.PARAM_ERROR, "未找到处理类, req: ", req.getWorldFunction());
        }
        return service.getDominateDetail(roleId, req);
    }

    /**
     * 获取城池历任都督
     *
     * @param roleId
     * @param req
     * @return
     */
    public GamePb8.GetDominateGovernorListRs getDominateGovernorList(long roleId, GamePb8.GetDominateGovernorListRq req) {
        IDominateWorldMapService service = getService(req.getWorldFunction());
        if (CheckNull.isNull(service)) {
            throw new MwException(GameError.PARAM_ERROR, "未找到处理类, req: ", req.getWorldFunction());
        }
        return service.getDominateGovernorList(roleId, req);
    }

    /**
     * 获取雄踞一方排行榜
     *
     * @param roleId
     * @param req
     * @return
     */
    public GamePb8.GetDominateRankRs getDominateRank(long roleId, GamePb8.GetDominateRankRq req) {
        IDominateWorldMapService service = getService(req.getWorldFunction());
        if (CheckNull.isNull(service)) {
            throw new MwException(GameError.PARAM_ERROR, "未找到处理类, req: ", req.getWorldFunction());
        }
        return service.getDominateRank(roleId, req);
    }

    public void initSchedule() {
        StateDominateWorldMap.getInstance().initSchedule();
        SiLiDominateWorldMap.getInstance().initSchedule();
    }

    public void handleOnData() throws ParseException {
        deserialize();
        this.ser = null;
        StateDominateWorldMap.getInstance().handleOnStartup();
        SiLiDominateWorldMap.getInstance().handleOnStartup();
    }

    public void marchEnd(Player player, Army army, int nowSec) {
        IDominateWorldMapService service = getService(getWorldFunctionByArmyType(army));
        if (CheckNull.isNull(service)) {
            throw new MwException(GameError.PARAM_ERROR, "未找到处理类, army: ", army.getType());
        }
        service.marchEnd(player, army, nowSec);
    }

    public void checkDominateSideCity(StaticCity staticCity) {
        if (CheckNull.isNull(staticCity)) {
            throw new MwException(GameError.NO_CONFIG, "不能存在此城池");
        }
        if (staticCity.getType() == WorldConstant.CITY_TYPE_3 || staticCity.getType() == WorldConstant.CITY_TYPE_6) {
            throw new MwException(GameError.PARAM_ERROR, "不可通过其他方式攻击州郡城");
        }
        boolean isDominateCity = false;
        if (CheckNull.nonEmpty(Constant.SI_LI_DOMINATE_OPEN_CITY)) {
            for (List<Integer> list : Constant.SI_LI_DOMINATE_OPEN_CITY) {
                if (CheckNull.isEmpty(list)) continue;
                if (list.contains(staticCity.getCityId())) {
                    isDominateCity = true;
                    break;
                }
            }
        }

        if (isDominateCity) {
            throw new MwException(GameError.PARAM_ERROR, "不可通过其他方式攻击州郡城");
        }
    }

    private int getWorldFunctionByArmyType(Army army) {
        switch (army.getType()) {
            case ArmyConstant.ARMY_TYPE_STATE_DOMINATE_ATTACK:
                return WorldPb.WorldFunctionDefine.STATES_AND_COUNTIES_DOMINATE_VALUE;
            case ArmyConstant.ARMY_TYPE_SI_LI_DOMINATE_ATTACK:
                return WorldPb.WorldFunctionDefine.SI_LI_DOMINATE_SIDE_VALUE;
            default:
                return -1;
        }
    }

    public void addCityGovernor(City city, long roleId) {
        // 更新城池拥有者
        city.setOwner(roleId, TimeHelper.getCurrentSecond());
        if (city instanceof DominateSideCity) {
            DominateSideCity sideCity = (DominateSideCity) city;
            sideCity.getGovernorList().addFirst(new DominateSideGovernor(roleId, System.currentTimeMillis(), city.getCityId()));
        }
    }

    /**
     * 撤回部队
     *
     * @param player
     * @param army
     */
    public void retreatArmy(Player player, Army army) {
        City city = worldDataManager.getCityById(army.getTargetId());
        if (city instanceof DominateSideCity == false) {
            return;
        }
        DominateSideCity sideCity = (DominateSideCity) city;
        Turple<Long, Integer> turPle = sideCity.getDefendList().stream().filter(o -> o.getB() == army.getKeyId()).findFirst().orElse(null);
        if (Objects.nonNull(turPle)) {
            sideCity.getDefendList().remove(turPle);
            sideCity.removeHolder(player.roleId, army.getKeyId());
        }
        IDominateWorldMapService worldMapService = getService(getWorldFunctionByArmyType(army));
        if (Objects.nonNull(worldMapService)) {
            worldMapService.retreatArmy(player, army, null, sideCity, TimeHelper.getCurrentSecond(), true);
        }
    }

    public void setSer(SerializePb.SerDominateData ser) {
        this.ser = ser;
    }

    public void deserialize() {
        if (ser == null) return;
        if (ser.hasStateMap()) {
            StateDominateWorldMap.getInstance().deserialize(ser.getStateMap());
        }
        if (ser.hasSiLiMap()) {
            SiLiDominateWorldMap.getInstance().deserialize(ser.getSiLiMap());
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void syncDominateWorldMapInfo(Events.SyncDominateWorldMapChangeEvent event) {
        IDominateWorldMapService service = getService(event.worldFunction);
        if (CheckNull.isNull(service)) return;

        service.syncDominateWorldMapInfo(event.builder);
    }

    @GmCmd("dominate")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        String cmd = params[0];
        int function = Integer.parseInt(params[1]);
        if ("job".equalsIgnoreCase(cmd)) {
            int times = Integer.parseInt(params[2]);
            switch (function) {
                case WorldPb.WorldFunctionDefine.STATES_AND_COUNTIES_DOMINATE_VALUE:
                    switch (params[3]) {
                        case "preview":
                            StateDominateWorldMap.getInstance().onPreview(times);
                            break;
                    }
                case WorldPb.WorldFunctionDefine.SI_LI_DOMINATE_SIDE_VALUE:
                    switch (params[3]) {
                        case "preview":
                            SiLiDominateWorldMap.getInstance().onPreview();
                            break;
                    }
            }

        }
    }
}
