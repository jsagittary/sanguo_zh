package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticCiaDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticVipDataMgr;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.ResAdd;
import com.gryphpoem.game.zw.pb.GamePb1.SynBuildRs;
import com.gryphpoem.game.zw.pb.GamePb1.SynGainResRs;
import com.gryphpoem.game.zw.pb.GamePb1.SyncRoleRebuildRs;
import com.gryphpoem.game.zw.pb.GamePb4.SyncAutoBuildRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.dao.impl.p.BuildingDao;
import com.gryphpoem.game.zw.resource.dao.impl.p.ResourceDao;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinWar;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;

@Component
public class BuildingDataManager {
    @Autowired
    private ResourceDao resourceDao;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private BuildingDao buildingDao;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private TechDataManager techDataManager;
    @Autowired
    private SolarTermsDataManager solarTermsDataManager;
    @Autowired
    private HonorDailyDataManager honorDailyDataManager;
    @Autowired
    private SeasonTalentService seasonTalentService;

    public boolean inited = false;

    public void createBuilding(Player player) {
        Building building = new Building();
        building.setLordId(player.roleId);
        Map<Integer, StaticBuildingInit> initBuildingMap = StaticBuildingDataMgr.getBuildingInitMap();
        for (StaticBuildingInit buildingInit : initBuildingMap.values()) {
            if (isResType(buildingInit.getBuildingType())) {
                // Map<Integer, Mill> millMap = new HashMap<>();
                // millMap.put(buildingInit.getBuildingType(),
                // new Mill(buildingInit.getBuildingId(), buildingInit.getBuildingType(),
                // buildingInit.getInitLv()));
                player.mills.put(buildingInit.getBuildingId(), new Mill(buildingInit.getBuildingId(),
                        buildingInit.getBuildingType(), buildingInit.getInitLv(), 0));
            } else {
                switch (buildingInit.getBuildingType()) {
                    case BuildingType.COMMAND:
                        building.setCommand(buildingInit.getInitLv());
                        break;
                    case BuildingType.FACTORY_1:
                        building.setFactory1(buildingInit.getInitLv());
                        break;
                    case BuildingType.FACTORY_2:
                        building.setFactory2(buildingInit.getInitLv());
                        break;
                    case BuildingType.FACTORY_3:
                        building.setFactory3(buildingInit.getInitLv());
                        break;
                    case BuildingType.TECH:
                        building.setTech(buildingInit.getInitLv());
                        break;
                    case BuildingType.WAR_FACTORY:
                        building.setWar(buildingInit.getInitLv());
                        break;
                    case BuildingType.STOREHOUSE:
                        building.setWare(buildingInit.getInitLv());
                        break;
                    case BuildingType.WAR_COLLEGE:
                        building.setCollege(buildingInit.getInitLv());
                        break;
                    case BuildingType.REMAKE:
                        building.setRefit(buildingInit.getInitLv());
                        break;
                    case BuildingType.ORDNANCE_FACTORY:
                        building.setMunition(buildingInit.getInitLv());
                        break;
                    case BuildingType.CHEMICAL_PLANT:
                        building.setChemical(buildingInit.getInitLv());
                        break;
                    case BuildingType.WALL:
                        building.setWall(buildingInit.getInitLv());
                        break;
                    case BuildingType.TRADE_CENTRE:
                        building.setTrade(buildingInit.getInitLv());
                        break;
                    case BuildingType.CLUB:
                        building.setClub(buildingInit.getInitLv());
                        break;
                    case BuildingType.TRAIN_FACTORY_1:
                        building.setTrain(buildingInit.getInitLv());
                        break;
                    case BuildingType.TRAIN_FACTORY_2:
                        building.setTrain2(buildingInit.getInitLv());
                        break;
                    case BuildingType.AIR_BASE:
                        building.setAir(buildingInit.getInitLv());
                        break;
                    default:
                        break;
                }
            }
        }
        buildingDao.insertBuilding(building);
        player.building = building;

        // ????????????????????????
        updateBuildingLockState(player);
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param buildingId
     */
    public void ReInitBuilding(Player player, int buildingId) {
        Building building = player.building;
        StaticBuildingInit buildingInit = StaticBuildingDataMgr.getBuildingInitMapById(buildingId);
        if (isResType(buildingInit.getBuildingType())) {
            player.mills.put(buildingInit.getBuildingId(), new Mill(buildingInit.getBuildingId(),
                    buildingInit.getBuildingType(), buildingInit.getInitLv(), 0));
        } else {
            switch (buildingInit.getBuildingType()) {
                case BuildingType.COMMAND:
                    building.setCommand(buildingInit.getInitLv());
                    break;
                case BuildingType.FACTORY_1:
                    building.setFactory1(buildingInit.getInitLv());
                    break;
                case BuildingType.FACTORY_2:
                    building.setFactory2(buildingInit.getInitLv());
                    break;
                case BuildingType.FACTORY_3:
                    building.setFactory3(buildingInit.getInitLv());
                    break;
                case BuildingType.TECH:
                    building.setTech(buildingInit.getInitLv());
                    break;
                case BuildingType.WAR_FACTORY:
                    building.setWar(buildingInit.getInitLv());
                    break;
                case BuildingType.STOREHOUSE:
                    building.setWare(buildingInit.getInitLv());
                    break;
                case BuildingType.WAR_COLLEGE:
                    building.setCollege(buildingInit.getInitLv());
                    break;
                case BuildingType.REMAKE:
                    building.setRefit(buildingInit.getInitLv());
                    break;
                case BuildingType.ORDNANCE_FACTORY:
                    building.setMunition(buildingInit.getInitLv());
                    break;
                case BuildingType.CHEMICAL_PLANT:
                    building.setChemical(buildingInit.getInitLv());
                    break;
                case BuildingType.WALL:
                    building.setWall(buildingInit.getInitLv());
                    break;
                case BuildingType.TRADE_CENTRE:
                    building.setTrade(buildingInit.getInitLv());
                    break;
                case BuildingType.CLUB:
                    building.setClub(buildingInit.getInitLv());
                    break;
                case BuildingType.TRAIN_FACTORY_1:
                    building.setTrain(buildingInit.getInitLv());
                    break;
                case BuildingType.TRAIN_FACTORY_2:
                    building.setTrain2(buildingInit.getInitLv());
                    break;
                case BuildingType.AIR_BASE:
                    building.setAir(buildingInit.getInitLv());
                    break;
                default:
                    break;
            }
        }
    }

    public void createResource(Player player, StaticIniLord staticIniLord) {
        Resource resource = new Resource();
        resource.setLordId(player.roleId);
        resource.setOil(staticIniLord.getOil());
        resource.setElec(staticIniLord.getElec());
        resource.setOre(staticIniLord.getOre());
        resource.setFood(staticIniLord.getFood());
        resource.setStoreTime(TimeHelper.getCurrentSecond());
        resource.setHumanTime(TimeHelper.getCurrentSecond());

        // ???????????? ???????????????????????????
        // Iterator<Mill> it = player.mills.values().iterator();
        // while (it.hasNext()) {
        // Mill mill = it.next();
        // addResourceOutAndMax(mill.getType(), mill.getLv(), resource);
        // }

        // StaticBuildingLv staticCommand = StaticBuildingDataMgr.getStaticBuildingLevel(BuildingId.COMMAND, 1);
        // resource.setStoneOut(staticCommand.getStoneOut());
        // resource.setIronOut(staticCommand.getIronOut());
        // resource.setOilOut(staticCommand.getOilOut());
        // resource.setCopperOut(staticCommand.getCopperOut());
        // resource.setSiliconOut(staticCommand.getSiliconOut());
        //
        // resource.setStoneMax(staticCommand.getStoneMax());
        // resource.setIronMax(staticCommand.getIronMax());
        // resource.setOilMax(staticCommand.getOilMax());
        // resource.setCopperMax(staticCommand.getCopperMax());
        // resource.setSiliconMax(staticCommand.getSiliconMax());
        resourceDao.insertResource(resource);
        player.resource = resource;

    }

    public void updateBuilding(Building building) {
        buildingDao.updateBuilding(building);
    }

    private static int getBuildingLv(int type, int pos, Player player) {
        Building building = player.building;
        if (CheckNull.isNull(building)) {
            return 0;
        }
        if (isResType(type)) {
            return getBuildingLv4Res(pos, player);
        } else {
            return getBuildingLv(type, building);
        }

    }

    /**
     * ??????????????????
     *
     * @param buildingId
     * @param player
     * @return
     */
    public static int getBuildingLv(int buildingId, Player player) {
        StaticBuildingInit sBuildIng = StaticBuildingDataMgr.getBuildingInitMapById(buildingId);
        if (sBuildIng != null) {
            return getBuildingLv(sBuildIng.getBuildingType(), buildingId, player);
        }
        return 0;
    }

    /**
     * ??????????????????????????????
     *
     * @param buildingId
     * @param player
     * @throws MwException
     */
    public void checkBuildingIsCreate(int buildingId, Player player) throws MwException {
        if (getBuildingLv(buildingId, player) < 1) {
            throw new MwException(GameError.BUILDING_NOT_CREATE.getCode(), "??????????????????, roleId:", player.roleId,
                    ", buildingId", buildingId);
        }
    }

    private static int getBuildingLv(int type, Building building) {
        switch (type) {
            case BuildingType.COMMAND:
                return building.getCommand();
            case BuildingType.TECH:
                return building.getTech();
            case BuildingType.WAR_FACTORY:
                return building.getWar();
            case BuildingType.STOREHOUSE:
                return building.getWare();
            case BuildingType.REMAKE:
                return building.getRefit();
            case BuildingType.ORDNANCE_FACTORY:
                return building.getMunition();
            case BuildingType.CHEMICAL_PLANT:
                return building.getChemical();
            case BuildingType.WAR_COLLEGE:
                return building.getCollege();
            case BuildingType.WALL:
                return building.getWall();
            case BuildingType.TRAIN_FACTORY_1:
                return building.getTrain();
            case BuildingType.TRAIN_FACTORY_2:
                return building.getTrain2();
            case BuildingType.TRADE_CENTRE:
                return building.getTrade();
            case BuildingType.FACTORY_1:
                return building.getFactory1();
            case BuildingType.FACTORY_2:
                return building.getFactory2();
            case BuildingType.FACTORY_3:
                return building.getFactory3();
            case BuildingType.AIR_BASE:
                return building.getAir();
            default:
                return 0;
        }
    }

    public static int getBuildingLv4Res(int pos, Player player) {
        return player.mills.get(pos) != null ? player.mills.get(pos).getLv() : 0;
        // Map<Integer, Mill> map = player.mills.get(pos);
        // if (map != null) {
        // return map.get(pos).getLv();
        // }
        // return 0;
    }

    public void synBuildToPlayer(Player target, BuildQue buildQue, int state) {
        if (target != null && target.isLogin) {
            SynBuildRs.Builder builder = SynBuildRs.newBuilder();
            builder.setQueue(PbHelper.createBuildQuePb(buildQue));
            builder.setState(state);
            builder.setRebuild(target.common.getReBuild());
            builder.addAllResAdd(listResAdd(target));

            Base.Builder msg = PbHelper.createSynBase(SynBuildRs.EXT_FIELD_NUMBER, SynBuildRs.ext, builder.build());
            MsgDataManager.getIns().add(new Msg(target.ctx, msg.build(), target.roleId));
            // MsgDataManager.getIns().add(new Msg(target.ctx, msg.build(),
            // target.roleId));
            // GameServer.getInstance().synMsgToPlayer(target.ctx, msg);
        }
    }

    /**
     * true ????????????????????????
     *
     * @param player
     * @param needBuildingLv
     * @return
     */
    public boolean checkBuildingLevel(Player player, List<List<Integer>> needBuildingLv) {
        Building building = player.building;
        if (needBuildingLv != null && needBuildingLv.size() > 0) {
            for (List<Integer> needBuiding : needBuildingLv) {
                switch (needBuiding.get(0).intValue()) {
                    case BuildingType.COMMAND:
                        return building.getCommand() < needBuiding.get(2).intValue();
                }
            }
        }
        return false;
    }

    /**
     * true ????????????????????????
     *
     * @param player
     * @param needBuildingLv
     * @return
     */
    public boolean checkBuildingLv(Player player, List<Integer> needBuildingLv) {
        Building building = player.building;
        if (needBuildingLv != null && needBuildingLv.size() > 0) {
            switch (needBuildingLv.get(0).intValue()) {
                case BuildingType.COMMAND:
                    return building.getCommand() < needBuildingLv.get(2).intValue();
                case BuildingType.TECH:
                    return building.getTech() < needBuildingLv.get(2).intValue();
                case BuildingType.WAR_FACTORY:
                    return building.getWar() < needBuildingLv.get(2).intValue();
                case BuildingType.STOREHOUSE:
                    return building.getWare() < needBuildingLv.get(2).intValue();
                case BuildingType.WAR_COLLEGE:
                    return building.getCollege() < needBuildingLv.get(2).intValue();
                case BuildingType.REMAKE:
                    return building.getRefit() < needBuildingLv.get(2).intValue();
                case BuildingType.ORDNANCE_FACTORY:
                    return building.getMunition() < needBuildingLv.get(2).intValue();
                case BuildingType.CHEMICAL_PLANT:
                    return building.getChemical() < needBuildingLv.get(2).intValue();
                case BuildingType.WALL:
                    return building.getWall() < needBuildingLv.get(2).intValue();
                case BuildingType.FACTORY_1:
                    return building.getFactory1() < needBuildingLv.get(2).intValue();
                case BuildingType.FACTORY_2:
                    return building.getFactory2() < needBuildingLv.get(2).intValue();
                case BuildingType.FACTORY_3:
                    return building.getFactory3() < needBuildingLv.get(2).intValue();
                case BuildingType.TRAIN_FACTORY_1:
                    return building.getTrain() < needBuildingLv.get(2).intValue();
                case BuildingType.TRAIN_FACTORY_2:
                    return building.getTrain2() < needBuildingLv.get(2).intValue();
                case BuildingType.CLUB:
                    return building.getClub() < needBuildingLv.get(2).intValue();
                case BuildingType.AIR_BASE:
                    return building.getAir() < needBuildingLv.get(2).intValue();
                default:
                    break;
            }
        }
        return false;
    }

    public static boolean isResType(int buildingType) {
        return Arrays.binarySearch(BuildingType.RES_ARRAY, buildingType) >= 0;
    }

    public static boolean isTrainType(int buildingType) {
        return Arrays.binarySearch(BuildingType.TRAIN_ARRAY, buildingType) >= 0;
    }

    public static boolean isFactoryId(int buildingId) {
        return Arrays.binarySearch(BuildingType.FACTORY_ARRAY, buildingId) >= 0;
    }

    public boolean addResourceOutAndMax(int buildingType, int buildingLv, Resource resource) {
        if (buildingLv == 0) {
            return false;
        }
        StaticBuildingLv staticBuildingLevel = StaticBuildingDataMgr.getStaticBuildingLevel(buildingType, buildingLv);
        if (staticBuildingLevel == null) {
            LogUtil.error("BuildingLv config error,type=" + buildingType + ",lv=" + buildingLv);
            return false;
        }

        List<Integer> resOuts = staticBuildingLevel.getResourceOut();
        if (resOuts == null) {
            LogUtil.error("BuildingLv resourceOut error,type=" + buildingType + ",lv=" + buildingLv);
            return false;
        }
        if (resOuts.get(0) != AwardType.RESOURCE) {
            LogUtil.error("BuildingLv resourceOut error,type=" + buildingType + ",lv=" + buildingLv);
            return false;
        }
        switch (resOuts.get(1).intValue()) {
            case AwardType.Resource.ELE:
                resource.setElecOut(resource.getElecOut() + resOuts.get(2));
                break;
            case AwardType.Resource.FOOD:
                resource.setFoodOut(resource.getFoodOut() + resOuts.get(2));
                break;
            case AwardType.Resource.OIL:
                resource.setOilOut(resource.getOilOut() + resOuts.get(2));
                break;
            case AwardType.Resource.ORE:
                resource.setOreOut(resource.getOreOut() + resOuts.get(2));
                break;
        }
        return true;
    }

    public void getResourceOut4SeasonTalent(Player player, ResourceMult resMult) {
        Gains gains = player.gains.get(BuildingType.COMMAND);
        if (!ObjectUtils.isEmpty(gains)) {
            StaticCommandMult staticCommandMult = StaticBuildingDataMgr.getCommandMult(gains.getId());
            if (Objects.nonNull(staticCommandMult)) {
                //?????????????????????????????????
                resMult.setOilSeasonTalent(seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_406));
                resMult.setElecSeasonTalent(seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_406));
                resMult.setFoodSeasonTalent(seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_406));

                //??????????????????
                if (staticCommandMult.getQuality() == 6) {
                    resMult.setOreSeasonTalent(seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_406));
                }
            }
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param resMult
     */
    public void getResourceOut4SolarTerms(Player player, ResourceMult resMult) {
        resMult.setOilSeason(solarTermsDataManager.getLevyBoundByType(SolarTermsConstant.SOLAR_TERMS_TYPE_OIL));
        resMult.setElecSeason(solarTermsDataManager.getLevyBoundByType(SolarTermsConstant.SOLAR_TERMS_TYPE_ELE));
        resMult.setFoodSeason(solarTermsDataManager.getLevyBoundByType(SolarTermsConstant.SOLAR_TERMS_TYPE_FOOD));
        resMult.setOreSeason(solarTermsDataManager.getLevyBoundByType(SolarTermsConstant.SOLAR_TERMS_TYPE_ORE));
    }

    /**
     * ????????????
     *
     * @param player
     * @param resMult
     */
    public void getResourceOut4Agent(Player player, ResourceMult resMult) {
        Cia cia = player.getCia();
        if (cia != null) {
            cia.getFemaleAngets().values().stream().filter(fa -> fa.getQuality() > 0).forEach(fa -> {
                StaticAgent sAgent = StaticCiaDataMgr.getAgentConfByAgent(fa);
                int skillVal = fa.getSkillVal();
                if (sAgent != null) {
                    switch (sAgent.getSkillId()) {
                        case CiaConstant.SKILL_OIL_ADD:
                            resMult.setOilAgent(resMult.getOilAgent() + skillVal);
                            break;
                        case CiaConstant.SKILL_ELEC_ADD:
                            resMult.setElecAgent(resMult.getElecAgent() + skillVal);
                            break;
                        case CiaConstant.SKILL_FOOD_ADD:
                            resMult.setFoodAgent(resMult.getFoodAgent() + skillVal);
                            break;
                        case CiaConstant.SKILL_ORE_ADD:
                            resMult.setOreAgent(resMult.getOreAgent() + skillVal);
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param resMult
     */
    public void getResourceOut4BerlinJob(Player player, ResourceMult resMult) {
        resMult.setOilBerlinJob(BerlinWar.getBerlinBuffVal(player.roleId, BerlinWarConstant.BUFF_TYPE_OIL));
        resMult.setElecBerlinJob(BerlinWar.getBerlinBuffVal(player.roleId, BerlinWarConstant.BUFF_TYPE_ELE));
        resMult.setFoodBerlinJob(BerlinWar.getBerlinBuffVal(player.roleId, BerlinWarConstant.BUFF_TYPE_FOOD));
        resMult.setOreBerlinJob(BerlinWar.getBerlinBuffVal(player.roleId, BerlinWarConstant.BUFF_TYPE_ORE));
    }

    /**
     * ?????????????????????
     *
     * @param player
     * @param resMult
     */
    public void getResourceOut4Command(Player player, ResourceMult resMult) {
        Gains commandMult = player.gains.get(BuildingType.COMMAND);
        if (commandMult != null) {
            StaticCommandMult staticCommandMult = StaticBuildingDataMgr.getCommandMult(commandMult.getId());
            List<List<Integer>> resOuts = staticCommandMult.getResMult();
            if (resOuts == null) return;
            // VIP??????, ?????????????????????
            int addCoef = Math.max(vipDataManager.getNum(player.lord.getVip(), VipConstant.RESOURCE_GAIN), 0);
            for (List<Integer> resOut : resOuts) {
                if (resOut.get(0) != AwardType.RESOURCE) {
                    continue;
                }
                switch (resOut.get(1)) {
                    case AwardType.Resource.ELE:
                        resMult.setElecWeath(resMult.getElecWeath() + resOut.get(2) + addCoef);
                        break;
                    case AwardType.Resource.FOOD:
                        resMult.setFoodWeath(resMult.getFoodWeath() + resOut.get(2) + addCoef);
                        break;
                    case AwardType.Resource.OIL:
                        resMult.setOilWeath(resMult.getOilWeath() + resOut.get(2) + addCoef);
                        break;
                    case AwardType.Resource.ORE:
                        resMult.setOreWeath(resMult.getOreWeath() + resOut.get(2) + addCoef);
                        break;
                }
            }
        }
    }

    private int getRes(long base, int... mult) {
        int num = (int) base;
        for (int i : mult) {
            num += (int) (base * (i / Constant.TEN_THROUSAND));
        }
        return num;
    }

    /**
     * ??????????????????
     *
     * @param player
     * @return
     */
    public ResourceMult getResourceMult(Player player) {
        ResourceMult resourceMult = new ResourceMult();
        Resource resource = player.resource;
        getResourceOut4Command(player, resourceMult);
        getResourceOut4SolarTerms(player, resourceMult);
        techDataManager.getResourceOut4Tech(player, resourceMult);
        getResourceOut4Agent(player, resourceMult);// ????????????
        getResourceOut4BerlinJob(player, resourceMult);// ??????????????????
        getResourceOut4SeasonTalent(player, resourceMult);//??????????????????

        resourceMult.setElec(getRes(resource.getElecOut(), resource.getElecOutF(), resourceMult.getElecWeath(),
                resourceMult.getElecSeason(), resourceMult.getElecTech(), resourceMult.getElecActive(),
                resourceMult.getElecAgent(), resourceMult.getElecBerlinJob(), resourceMult.getElecSeasonTalent()));

        resourceMult.setFood(getRes(resource.getFoodOut(), resource.getFoodOutF(), resourceMult.getFoodWeath(),
                resourceMult.getFoodSeason(), resourceMult.getFoodTech(), resourceMult.getFoodActive(),
                resourceMult.getFoodAgent(), resourceMult.getFoodBerlinJob(), resourceMult.getFoodSeasonTalent()));

        resourceMult.setOil(getRes(resource.getOilOut(), resource.getOilOutF(), resourceMult.getOilWeath(),
                resourceMult.getOilSeason(), resourceMult.getOilTech(), resourceMult.getOilActive(),
                resourceMult.getOilAgent(), resourceMult.getOilBerlinJob(), resourceMult.getOilSeasonTalent()));

        resourceMult.setOre(getRes(resource.getOreOut(), resource.getOreOutF(), resourceMult.getOreWeath(),
                resourceMult.getOreSeason(), resourceMult.getOreTech(), resourceMult.getOreActive(),
                resourceMult.getOreAgent(), resourceMult.getOreBerlinJob(), resourceMult.getOreSeasonTalent()));
        return resourceMult;
    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @param buildintType
     * @return
     */
    public int getResourceMult(Player player, int buildintType, int resOut) {
        ResourceMult resourceMult = new ResourceMult();
        Resource resource = player.resource;
        getResourceOut4Command(player, resourceMult);// ?????????????????????
        getResourceOut4SolarTerms(player, resourceMult);// ??????????????????
        techDataManager.getResourceOut4Tech(player, resourceMult);// ????????????
        getResourceOut4Agent(player, resourceMult);// ????????????
        getResourceOut4BerlinJob(player, resourceMult);// ??????????????????
        getResourceOut4SeasonTalent(player, resourceMult);//??????????????????

        switch (buildintType) {
            case BuildingType.RES_ELE:
                resourceMult.setElec(getRes(resOut, resource.getElecOutF(), resourceMult.getElecWeath(),
                        resourceMult.getElecSeason(), resourceMult.getElecTech(), resourceMult.getElecActive(),
                        resourceMult.getElecAgent(), resourceMult.getElecBerlinJob(), resourceMult.getElecSeasonTalent()));
                return resourceMult.getElec();
            case BuildingType.RES_FOOD:
                resourceMult.setFood(getRes(resOut, resource.getFoodOutF(), resourceMult.getFoodWeath(),
                        resourceMult.getFoodSeason(), resourceMult.getFoodTech(), resourceMult.getFoodActive(),
                        resourceMult.getFoodAgent(), resourceMult.getFoodBerlinJob(), resourceMult.getFoodSeasonTalent()));
                return resourceMult.getFood();
            case BuildingType.RES_OIL:
                resourceMult.setOil(getRes(resOut, resource.getOilOutF(), resourceMult.getOilWeath(),
                        resourceMult.getOilSeason(), resourceMult.getOilTech(), resourceMult.getOilActive(),
                        resourceMult.getOilAgent(), resourceMult.getOilBerlinJob(), resourceMult.getOilSeasonTalent()));
                return resourceMult.getOil();
            case BuildingType.RES_ORE:
                resourceMult.setOre(getRes(resOut, resource.getOreOutF(), resourceMult.getOreWeath(),
                        resourceMult.getOreSeason(), resourceMult.getOreTech(), resourceMult.getOreActive(),
                        resourceMult.getOreAgent(), resourceMult.getOreBerlinJob(), resourceMult.getOreSeasonTalent()));
                return resourceMult.getOre();
        }

        return 0;
    }

    // public boolean addResourceOutAndMax(Player player) {
    // Iterator<Gains> it = player.gains.values().iterator();
    // while (it.hasNext()) {
    // Gains gains = it.next();
    // if (TimeHelper.getCurrentSecond() > gains.getEndTime()) {
    // continue;
    // }
    // gains.getId()
    // addResourceOutAndMax(mill.getType(), mill.getLv(), resource);
    // }
    // }

    public int getBuilding4LvCnt(Player player, int type, int lv) {
        int cnt = 0;
        for (Entry<Integer, Mill> kv : player.mills.entrySet()) {
            if (kv.getValue() != null && kv.getValue().getType() == type && kv.getValue().getLv() >= lv) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param type
     * @return
     */
    public static int getBuildingTopLv(Player player, int type) {
        Building building = player.building;
        if (isResType(type)) {
            return getMillTopLv(player, type);
        } else {
            return getBuildingLv(type, building);
        }
    }

    public static int getMillTopLv(Player player, int millId) {
        int lv = 0;
        Iterator<Mill> it = player.mills.values().iterator();
        while (it.hasNext()) {
            Mill mill = it.next();
            if (mill.getType() == millId && mill.getLv() > lv) {
                lv = mill.getLv();
            }
        }
        return lv;
    }

    public int getMillCount(Player player, int millId, int lv) {
        int count = 0;
        Iterator<Mill> it = player.mills.values().iterator();
        while (it.hasNext()) {
            Mill mill = it.next();
            if (mill.getType() == millId && mill.getLv() >= lv) {
                count++;
            }
        }
        return count;
    }

    /**
     * ????????????
     *
     * @param player
     */
    public void addHumanPerSecond(Player player) {
        if (checkBuildingLock(player, BuildingType.CHEMICAL_PLANT)) {// ????????????????????????
            Resource resource = player.resource;
            StaticBuildingLv buildingLv = StaticBuildingDataMgr.getStaticBuildingLevel(BuildingType.COMMAND,
                    player.building.getCommand());
            int min = buildingLv != null && buildingLv.getCapacity() != null && buildingLv.getCapacity().size() > 0
                    ? buildingLv.getCapacity().get(0).get(0) : 0;
            int max = buildingLv != null && buildingLv.getCapacity() != null && buildingLv.getCapacity().size() > 0
                    ? buildingLv.getCapacity().get(0).get(1) : 0;
            int now = TimeHelper.getCurrentSecond();
            // ??????
            if (min > resource.getHuman()) {
                int human = (int) (min * 0.005f * (now - resource.getHumanTime()) / TimeHelper.MINUTE);
                if (human > 0) {
                    resource.setHumanTime(now);
                    if (resource.getHuman() + human > min) {
                        resource.setHuman(min);
                    } else {
                        resource.setHuman(resource.getHuman() + human);
                    }
                }
            }
            // ??????
            if (max > 0 && resource.getHuman() > max) {
                int human = (int) ((resource.getHuman() - max)
                        * (1 - Math.pow(0.9995, (now - resource.getHumanTime()) / TimeHelper.MINUTE)));
                if (human > 0) {
                    resource.setHumanTime(now);
                    if (resource.getHuman() - human < max) {
                        resource.setHuman(max);
                    } else {
                        resource.setHuman(resource.getHuman() - human);
                    }
                }
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param atkPlayer
     */
    public void SyncRebuild(Player player, Player atkPlayer) {
        Common common = player.common;
        SyncRoleRebuildRs.Builder builder = SyncRoleRebuildRs.newBuilder();
        List<Award> awards = player.awards.computeIfAbsent(Constant.AwardType.TYPE_1, k -> new ArrayList<>());
        builder.addAllAward(awards);
        if (common != null) {
            builder.setRebuild(common.getReBuild());
        }
        Effect effect = player.getEffect().get(EffectConstant.PROTECT);
        if (effect != null) {
            builder.setProtectTime(effect.getEndTime());
        }
        // ??????????????????????????????????????????
        builder.setIsAdvance(player.isAdvanceAward());

        // ???????????????????????????
        builder.setAtkCamp(atkPlayer.lord.getCamp());
        builder.setAtkName(atkPlayer.lord.getNick());
        builder.setQuickBuyArmyCnt(player.getMixtureDataById(PlayerConstant.DAILY_QUICK_BUY_ARMY));
        builder.setBattleTime(TimeHelper.getCurrentSecond());
        Base.Builder msg = PbHelper.createRsBase(SyncRoleRebuildRs.EXT_FIELD_NUMBER, SyncRoleRebuildRs.ext,
                builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        LogUtil.debug("??????????????????" + player.roleId);
    }

    /**
     * ????????????????????????
     *
     * @param player
     */
    public void SyncCrossRebuild(Player player) {
        Common common = player.common;
        SyncRoleRebuildRs.Builder builder = SyncRoleRebuildRs.newBuilder();
        List<Award> awards = player.awards.computeIfAbsent(Constant.AwardType.TYPE_1, k -> new ArrayList<>());
        builder.addAllAward(awards);
        if (common != null) {
            builder.setRebuild(common.getReBuild());
        }
        Effect effect = player.getEffect().get(EffectConstant.PROTECT);
        if (effect != null) {
            builder.setProtectTime(effect.getEndTime());
        }
        // ??????????????????????????????????????????
        builder.setIsAdvance(player.isAdvanceAward());

        // ???????????????????????????
//        builder.setAtkCamp(atkPlayer.lord.getCamp());
//        builder.setAtkName(atkPlayer.lord.getNick());
        builder.setQuickBuyArmyCnt(player.getMixtureDataById(PlayerConstant.DAILY_QUICK_BUY_ARMY));
        builder.setBattleTime(TimeHelper.getCurrentSecond());
        Base.Builder msg = PbHelper.createRsBase(SyncRoleRebuildRs.EXT_FIELD_NUMBER, SyncRoleRebuildRs.ext,
                builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        LogUtil.debug("????????????????????????: " + player.roleId);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param def
     * @return
     */
    public long[] getProtectRes(Player def) {
        Resource defRes = def.resource;
        int lv = getBuildingTopLv(def, BuildingType.STOREHOUSE);
        if (lv == 0) {
            return new long[] { defRes.getOil(), defRes.getFood(), defRes.getElec() };
        }
        StaticBuildingLv sBuildingLv = StaticBuildingDataMgr.getStaticBuildingLevel(BuildingType.STOREHOUSE, lv);
        // ??????????????????
        long protectOil = 0;
        long protectFood = 0;
        long protectEle = 0;
        if (sBuildingLv != null) {
            List<List<Integer>> capactiy = sBuildingLv.getCapacity();
            if (capactiy != null && !capactiy.isEmpty()) {
                for (List<Integer> kv : capactiy) {
                    if (kv != null && !kv.isEmpty() && kv.size() > 1) {
                        if (kv.get(0) == AwardType.Resource.OIL) {
                            protectOil = kv.get(1);
                        } else if (kv.get(0) == AwardType.Resource.FOOD) {
                            protectFood = kv.get(1);
                        } else if (kv.get(0) == AwardType.Resource.ELE) {
                            protectEle = kv.get(1);
                        }
                    }
                }
            }
        }

        // ????????????
        int techEffect = techDataManager.getTechEffect4SingleVal(def, TechConstant.TYPE_18);
        int seasonTalentEffect = seasonTalentService.getSeasonTalentEffectValue(def, SeasonConst.TALENT_EFFECT_502);
        int protectMult = techEffect + seasonTalentEffect;
        if (protectMult > 0) {
            protectOil = (long) (protectOil * (1 + protectMult * 1.0f / Constant.TEN_THROUSAND));
            protectFood = (long) (protectFood * (1 + protectMult * 1.0f / Constant.TEN_THROUSAND));
            protectEle = (long) (protectEle * (1 + protectMult * 1.0f / Constant.TEN_THROUSAND));
        }

        long loseOil = Math.max(((defRes.getOil() - protectOil)), 0);
        long loseFood = Math.max(((defRes.getFood() - protectFood)), 0);
        long loseEle = Math.max(((defRes.getElec() - protectEle)), 0);
        return new long[] { loseOil, loseFood, loseEle };
    }

    /**
     * ??????????????????
     *
     * @param def ?????????
     * @return
     */
    public Map<Integer, Integer> canPlunderScout(Player def, Player atk, long[] proRes) {
        // ?????????????????????=ROUNDDOWN(??????LV/10,0)*300000+500000
        // int maxLose = def.lord.getLevel() / 10 * 300000 + 500000;
        // float pro = 3;
        // int loseOil = Math.min((int) (proRes[0] / pro), maxLose);
        // int loseFood = Math.min((int) (proRes[1] / pro), maxLose);
        // int loseEle = Math.min((int) (proRes[2] / pro), maxLose);
        // float gainPro = 3.0f / 4;
        //
        // int obtainOil = (int) (gainPro * loseOil);
        // int obtainFood = (int) (gainPro * loseFood);
        // int obtainEle = (int) (gainPro * loseEle);

        // ?????????????????????=ROUNDDOWN(??????LV/10,0)*300000+500000
        int maxLose = def.lord.getLevel() / 10 * 300000 + 500000;
        int defStorehouseLv = getBuildingTopLv(def, BuildingType.STOREHOUSE);
        float lostPro = 0.35f - defStorehouseLv * 0.005f;
        int loseOil = Math.min((int) Math.ceil(proRes[0] * lostPro), maxLose);
        int loseFood = Math.min((int) Math.ceil(proRes[1] * lostPro), maxLose);
        int loseEle = Math.min((int) Math.ceil(proRes[2] * lostPro), maxLose);

        int atkStorehouseLv = getBuildingTopLv(atk, BuildingType.STOREHOUSE);
        float gainPro = 0.65f + atkStorehouseLv * 0.005f;

        long human = def.resource.getHuman();
        int obtainHuman = (int) (human / 3);
        Map<Integer, Integer> canPlunder = new HashMap<>();
        canPlunder.put(AwardType.Resource.OIL, (int) Math.ceil(gainPro * loseOil));
        canPlunder.put(AwardType.Resource.FOOD, (int) Math.ceil(gainPro * loseFood));
        canPlunder.put(AwardType.Resource.ELE, (int) Math.ceil(gainPro * loseEle));
        // canPlunder.put(AwardType.Resource.ORE, 0);
        canPlunder.put(AwardType.Resource.HUMAN, obtainHuman);
        return canPlunder;

    }

    /**
     * ??????????????????
     *
     * @param def
     * @param atkStorehouseLv
     * @param proRes
     * @return
     */
    public Map<Integer, Integer> canPlunderCrossScout(Player def, int atkStorehouseLv, long[] proRes) {
        // ?????????????????????=ROUNDDOWN(??????LV/10,0)*300000+500000
        // int maxLose = def.lord.getLevel() / 10 * 300000 + 500000;
        // float pro = 3;
        // int loseOil = Math.min((int) (proRes[0] / pro), maxLose);
        // int loseFood = Math.min((int) (proRes[1] / pro), maxLose);
        // int loseEle = Math.min((int) (proRes[2] / pro), maxLose);
        // float gainPro = 3.0f / 4;
        //
        // int obtainOil = (int) (gainPro * loseOil);
        // int obtainFood = (int) (gainPro * loseFood);
        // int obtainEle = (int) (gainPro * loseEle);

        // ?????????????????????=ROUNDDOWN(??????LV/10,0)*300000+500000
        int maxLose = def.lord.getLevel() / 10 * 300000 + 500000;
        int defStorehouseLv = getBuildingTopLv(def, BuildingType.STOREHOUSE);
        float lostPro = 0.35f - defStorehouseLv * 0.005f;
        int loseOil = Math.min((int) Math.ceil(proRes[0] * lostPro), maxLose);
        int loseFood = Math.min((int) Math.ceil(proRes[1] * lostPro), maxLose);
        int loseEle = Math.min((int) Math.ceil(proRes[2] * lostPro), maxLose);

        float gainPro = 0.65f + atkStorehouseLv * 0.005f;

        long human = def.resource.getHuman();
        int obtainHuman = (int) (human / 3);
        Map<Integer, Integer> canPlunder = new HashMap<>();
        canPlunder.put(AwardType.Resource.OIL, (int) Math.ceil(gainPro * loseOil));
        canPlunder.put(AwardType.Resource.FOOD, (int) Math.ceil(gainPro * loseFood));
        canPlunder.put(AwardType.Resource.ELE, (int) Math.ceil(gainPro * loseEle));
        // canPlunder.put(AwardType.Resource.ORE, 0);
        canPlunder.put(AwardType.Resource.HUMAN, obtainHuman);
        return canPlunder;
    }

    /**
     * ?????????????????? ??? ?????????, ????????????????????????,??????????????????????????????
     *
     * @param atk
     * @param def
     * @param loseList
     * @param protect ?????????????????????
     * @return ???????????????NULL
     */
    public List<CommonPb.Award> dropList4War(Player atk, Player def, List<CommonPb.Award> loseList, boolean protect) throws MwException {
        List<CommonPb.Award> list = new ArrayList<>();
        // ???????????????????????????????????????(?????????????????? = ?????????????????? = ??????????????? - ???????????????????????????)
        if (atk == null || def == null) {
            return list;
        }
        // ??????????????????
        Resource defRes = def.resource;
        // ????????? ??????:??????????????????????????????????????????0??????????????????????????????????????????????????????????????????
        // int lv = getBuildingTopLv(def, BuildingType.STOREHOUSE);
        // if (lv == 0) {
        // return list;
        // }

        long[] proRes = getProtectRes(def);

        // ?????????????????????=ROUNDDOWN(??????LV/10,0)*300000+500000
        int maxLose = def.lord.getLevel() / 10 * 300000 + 500000;

        // ?????????????????? = ?????????????????? * ???????????? (???????????? = 0.2 * ???????????????????????? / ???????????????????????????????????????)
        // float pro = (Constant.FIGHT_LOSE_PRO * getBuildingTopLv(atk, BuildingType.STOREHOUSE) / lv);
        // pro = pro > 1 ? 1 : pro;

        int defStorehouseLv = getBuildingTopLv(def, BuildingType.STOREHOUSE);
        float lostPro = 0.35f - defStorehouseLv * 0.005f;
        int loseOil = Math.min((int) Math.ceil(proRes[0] * lostPro), maxLose);
        int loseFood = Math.min((int) Math.ceil(proRes[1] * lostPro), maxLose);
        int loseEle = Math.min((int) Math.ceil(proRes[2] * lostPro), maxLose);

        int atkStorehouseLv = getBuildingTopLv(atk, BuildingType.STOREHOUSE);
        float gainPro = 0.65f + atkStorehouseLv * 0.005f;

        ChangeInfo change = ChangeInfo.newIns();
        if (loseOil > 0) {
            rewardDataManager.subResource(def, AwardType.Resource.OIL, loseOil, AwardFrom.FIGHT_DEF);// , "?????????"
            loseList.add(Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.OIL).setCount(loseOil)
                    .build());
            int gain = (int) Math.ceil(gainPro * loseOil);
            if (gain > 0) {
                list.add(Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.OIL).setCount(gain)
                        .build()); // Constant.FIGHT_GAIN_PRO
            }
            // ??????????????????????????????????????????
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.OIL);
        }
        if (loseEle > 0) {
            rewardDataManager.subResource(def, AwardType.Resource.ELE, loseEle, AwardFrom.FIGHT_DEF);// , "?????????"
            loseList.add(Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.ELE).setCount(loseEle)
                    .build());
            int gain = (int) Math.ceil(gainPro * loseEle);
            if (gain > 0) {
                list.add(Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.ELE).setCount(gain)
                        .build());
            }
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.ELE);
        }
        if (loseFood > 0) {
            rewardDataManager.subResource(def, AwardType.Resource.FOOD, loseFood, AwardFrom.FIGHT_DEF);// , "?????????"
            loseList.add(Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.FOOD)
                    .setCount(loseFood).build());
            int gain = (int) Math.ceil(gainPro * loseFood);
            if (gain > 0) {
                list.add(Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.FOOD).setCount(gain)
                        .build());
            }
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);
        }
        // ???????????????????????????????????????????????????????????????
        long human = defRes.getHuman();
        int loseHuman = (int) (human / 2);
        if (loseHuman > 0 && checkBuildingLock(def, BuildingType.CHEMICAL_PLANT)) {// ???????????????????????????????????????
            rewardDataManager.subResource(def, AwardType.Resource.HUMAN, loseHuman, AwardFrom.FIGHT_DEF);// , "?????????"
            loseList.add(Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.HUMAN)
                    .setCount(loseHuman).build());
            int gain = (int) (human / 3);
            if (gain > 0 && checkBuildingLock(atk, BuildingType.CHEMICAL_PLANT)) {// ????????????????????????????????????
                list.add(Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.HUMAN).setCount(gain)
                        .build());
            }
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.HUMAN);
        }
        LogUtil.debug(def.roleId + ",??????????????????oil=" + loseOil + ",food=" + loseFood + ",ele=" + loseEle + ",loseHuman="
                + loseHuman + ",human=" + def.resource.getHuman());
        // ????????????????????????????????????
        rewardDataManager.syncRoleResChanged(def, change);
        processRebuild(def, loseOil, loseFood, loseEle, protect);
        // ????????????????????????
        processHonorDaily(atk, def, loseOil, loseFood, loseEle);
        return list;
    }

    /**
     * ??????????????????????????????
     *
     * @param atkStoreHouseLv
     * @param unlockChemicalPlant
     * @param def
     * @return
     * @throws MwException
     */
    public List<byte[]> dropCrossBattleAward(int atkStoreHouseLv, boolean unlockChemicalPlant, Player def) throws MwException {
        List<byte[]> list = new ArrayList<>();
        // ???????????????????????????????????????(?????????????????? = ?????????????????? = ??????????????? - ???????????????????????????)
        if (def == null) {
            return list;
        }
        // ??????????????????
        Resource defRes = def.resource;
        // ????????? ??????:??????????????????????????????????????????0??????????????????????????????????????????????????????????????????
        // int lv = getBuildingTopLv(def, BuildingType.STOREHOUSE);
        // if (lv == 0) {
        // return list;
        // }

        long[] proRes = getProtectRes(def);

        // ?????????????????????=ROUNDDOWN(??????LV/10,0)*300000+500000
        int maxLose = def.lord.getLevel() / 10 * 300000 + 500000;

        // ?????????????????? = ?????????????????? * ???????????? (???????????? = 0.2 * ???????????????????????? / ???????????????????????????????????????)
        // float pro = (Constant.FIGHT_LOSE_PRO * getBuildingTopLv(atk, BuildingType.STOREHOUSE) / lv);
        // pro = pro > 1 ? 1 : pro;

        int defStorehouseLv = getBuildingTopLv(def, BuildingType.STOREHOUSE);
        float lostPro = 0.35f - defStorehouseLv * 0.005f;
        int loseOil = Math.min((int) Math.ceil(proRes[0] * lostPro), maxLose);
        int loseFood = Math.min((int) Math.ceil(proRes[1] * lostPro), maxLose);
        int loseEle = Math.min((int) Math.ceil(proRes[2] * lostPro), maxLose);

        float gainPro = 0.65f + atkStoreHouseLv * 0.005f;

        ChangeInfo change = ChangeInfo.newIns();
        if (loseOil > 0) {
            rewardDataManager.subResource(def, AwardType.Resource.OIL, loseOil, AwardFrom.FIGHT_DEF);// , "?????????"
            int gain = (int) Math.ceil(gainPro * loseOil);
            if (gain > 0) {
                list.add(Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.OIL).setCount(gain)
                        .build().toByteArray()); // Constant.FIGHT_GAIN_PRO
            }
            // ??????????????????????????????????????????
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.OIL);
        }
        if (loseEle > 0) {
            rewardDataManager.subResource(def, AwardType.Resource.ELE, loseEle, AwardFrom.FIGHT_DEF);// , "?????????"
            int gain = (int) Math.ceil(gainPro * loseEle);
            if (gain > 0) {
                list.add(Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.ELE).setCount(gain)
                        .build().toByteArray());
            }
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.ELE);
        }
        if (loseFood > 0) {
            rewardDataManager.subResource(def, AwardType.Resource.FOOD, loseFood, AwardFrom.FIGHT_DEF);// , "?????????"
            int gain = (int) Math.ceil(gainPro * loseFood);
            if (gain > 0) {
                list.add(Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.FOOD).setCount(gain)
                        .build().toByteArray());
            }
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);
        }
        // ???????????????????????????????????????????????????????????????
        long human = defRes.getHuman();
        int loseHuman = (int) (human / 2);
        if (loseHuman > 0 && checkBuildingLock(def, BuildingType.CHEMICAL_PLANT)) {// ???????????????????????????????????????
            rewardDataManager.subResource(def, AwardType.Resource.HUMAN, loseHuman, AwardFrom.FIGHT_DEF);// , "?????????"
            int gain = (int) (human / 3);
            if (gain > 0 && unlockChemicalPlant) {// ????????????????????????????????????
                list.add(Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.HUMAN).setCount(gain)
                        .build().toByteArray());
            }
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.HUMAN);
        }
        LogUtil.debug(def.roleId + ",??????????????????oil=" + loseOil + ",food=" + loseFood + ",ele=" + loseEle + ",loseHuman="
                + loseHuman + ",human=" + def.resource.getHuman());
        // ????????????????????????????????????
        rewardDataManager.syncRoleResChanged(def, change);
        return list;
    }

    /**
         * ?????????????????? ??? ?????????, ????????????????????????,??????????????????????????????
         *
         * @param atk
         * @param def
         * @return ???????????????NULL
         */
    public List<CommonPb.Award> dropList4War(Player atk, Player def, List<CommonPb.Award> loseList) throws MwException {
        return dropList4War(atk, def, loseList, true);
    }

    /**
     * ????????????????????????
     *
     * @param atk
     * @param def
     * @param loseOil
     * @param loseFood
     * @param loseEle
     */
    private void processHonorDaily(Player atk, Player def, int loseOil, int loseFood, int loseEle) {
        int sum = 0;
        sum += loseOil > 0 ? loseOil : 0;
        sum += loseFood > 0 ? loseFood : 0;
        sum += loseEle > 0 ? loseEle : 0;
        honorDailyDataManager.addAndCheckHonorReport2s(atk, HonorDailyConstant.COND_ID_13, sum);
        honorDailyDataManager.addAndCheckHonorReport2s(def, HonorDailyConstant.COND_ID_12, sum, atk.lord.getNick());
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param loseOil
     * @param loseFood
     * @param loseEle
     * @param protect ?????????????????????
     */
    private void processRebuild(Player player, int loseOil, int loseFood, int loseEle, boolean protect) {
        Common common = player.common;
        List<Award> award = player.awards.get(Constant.AwardType.TYPE_1);
        if (award == null) {
            award = new ArrayList<>();
            player.awards.put(Constant.AwardType.TYPE_1, award);
        }
        int baseNum = Constant.REBUILD_AWARD_NORMAL; // ?????????????????????
        if (common != null) {
            float pro = Constant.FIGHT_LOSE_PRO;
            int protectTime = Constant.REBUILD_PROTECT_NORMAL;
            if (common.getReBuild() > 0) {
                // ??????????????????
                common.setReBuild(common.getReBuild() - 1);
                player.setAdvanceAward(true);
                pro = Constant.FIGHT_GAIN_PRO;
                protectTime = Constant.REBUILD_PROTECT_HIGHT;
                baseNum = Constant.REBUILD_AWARD_HIGHT;
                loseFood = loseFood * 3 / 4;
                loseEle = loseEle * 3 / 4;
                loseOil = loseOil * 3 / 4;
            } else {
                loseFood = loseFood / 4;
                loseEle = loseEle / 4;
                loseOil = loseOil / 4;
            }
            Effect effect = player.getEffect().get(EffectConstant.PROTECT);
            if (effect == null && protect) {
                player.getEffect().put(EffectConstant.PROTECT,
                        new Effect(EffectConstant.PROTECT, 0, TimeHelper.getCurrentSecond() + protectTime));
            }
            LogUtil.debug("????????????????????????,??????=" + pro);
            // ????????????
            List<Award> nAward = new ArrayList<>();
            int oil = 0;
            int food = 0;
            int ele = 0;
            if (!award.isEmpty()) {
                for (Award a : award) {
                    if (a.getType() == AwardType.RESOURCE && a.getId() == AwardType.Resource.FOOD) {
                        food += a.getCount();
                    } else if (a.getType() == AwardType.RESOURCE && a.getId() == AwardType.Resource.ELE) {
                        ele += a.getCount();
                    } else if (a.getType() == AwardType.RESOURCE && a.getId() == AwardType.Resource.OIL) {
                        oil += a.getCount();
                    } else {
                        nAward.add(PbHelper.createAwardPb(a.getType(), a.getId(), a.getCount()));
                    }
                }
            }

            nAward.add(PbHelper.createAwardPb(AwardType.RESOURCE, AwardType.Resource.OIL, baseNum + oil + loseOil));
            nAward.add(PbHelper.createAwardPb(AwardType.RESOURCE, AwardType.Resource.ELE, baseNum + ele + loseEle));
            nAward.add(PbHelper.createAwardPb(AwardType.RESOURCE, AwardType.Resource.FOOD, baseNum + food + loseFood));

            LogUtil.debug("????????????????????????,??????=" + pro + ",nAward=" + nAward);
            player.awards.put(Constant.AwardType.TYPE_1, nAward);
        }
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param player
     * @param buildingId
     * @return
     */
    private boolean checkBuildingLockCalc(Player player, int buildingId) {
        return StaticFunctionDataMgr.funcitonIsOpen(player, buildingId);
    }

    /**
     * ???????????????????????????
     *
     * @param player
     */
    public void updateBuildingLockState(Player player) {
        StaticBuildingDataMgr.getBuildingInitMap().forEach((k, v) -> {
            checkBuildingLock(player, k);
        });
    }

    /**
     * ???????????????,?????????????????? mill.setUnlock(true)
     *
     * @param player
     * @param mill
     * @param unlock
     */
    private void unLockMill(Player player, Mill mill, boolean unlock) {
        if (unlock && !mill.isUnlock()) {
            mill.setUnlock(unlock);
            mill.setResTime(TimeHelper.getCurrentSecond());
            mill.setResCnt(1);
            synGainResRs(player, mill);
        }
    }

    public void synGainResRs(Player player, Mill... mills) {
        if (player != null && player.isLogin && player.ctx != null && mills != null && mills.length > 0) {
            SynGainResRs.Builder builder = SynGainResRs.newBuilder();
            for (Mill mill : mills) {
                builder.addMills(PbHelper.createMillPb(mill));
            }
            Base.Builder msg = PbHelper.createSynBase(SynGainResRs.EXT_FIELD_NUMBER, SynGainResRs.ext, builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @param buildingId
     * @return true ????????????????????????
     */
    public boolean checkBuildingLock(Player player, int buildingId) {
        StaticBuildingInit sBuildingInit = StaticBuildingDataMgr.getBuildingInitMapById(buildingId);
        if (sBuildingInit == null) {
            LogUtil.debug("?????????????????????   buildingId:", buildingId);
            return false;
        }
        if (isResType(sBuildingInit.getBuildingType())) {// ?????????????????????
            Mill mill = player.mills.get(buildingId);
            if (mill == null) {
                // LogUtil.debug("???????????????,???????????????????????? :", buildingId);
                return checkBuildingLockCalc(player, buildingId);
            }
            if (mill.isUnlock()) { // ????????????????????????
                return true;
            } else {
                // ????????????????????????
                boolean unlock = checkBuildingLockCalc(player, buildingId);
                unLockMill(player, mill, unlock);
                return unlock;
            }
        } else if (isTrainType(sBuildingInit.getBuildingType())) {// ?????????????????????
            BuildingExt buildingExt = player.buildingExts.get(buildingId);
            if (buildingExt == null) {
                buildingExt = new BuildingExt(sBuildingInit.getBuildingId());
                player.buildingExts.put(sBuildingInit.getBuildingId(), buildingExt);
            }
            if (buildingExt.isUnlock()) { // ????????????????????????
                return true;
            } else {
                boolean unlock = checkBuildingLockCalc(player, buildingId);
                if (unlock) { // ????????????
                    buildingExt.setType(0);
                    buildingExt.setUnLockTime(TimeHelper.getCurrentSecond());
                }
                buildingExt.setUnlock(unlock);
                return unlock;
            }
        } else {
            BuildingExt buildingExt = player.buildingExts.get(buildingId);
            if (buildingExt == null) {
                buildingExt = new BuildingExt(sBuildingInit.getBuildingId(), sBuildingInit.getBuildingType());
                player.buildingExts.put(sBuildingInit.getBuildingId(), buildingExt);
            }
            if (buildingExt.isUnlock()) { // ????????????????????????
                return true;
            } else {
                boolean unlock = checkBuildingLockCalc(player, buildingId);
                buildingExt.setUnlock(unlock);
                if (unlock) { // ????????????
                    buildingExt.setUnLockTime(TimeHelper.getCurrentSecond());
                }
                return unlock;
            }
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param player
     * @param mill
     * @return
     */
    public boolean checkMillCanGain(Player player, Mill mill) {
        return checkBuildingLock(player, mill.getPos()) && mill.getLv() > 0;
    }

    /**
     * ?????????????????????????????????
     *
     * @param player
     * @return ???????????????????????????
     */
    public int resetAutoBuildCnt(Player player) {
        int vipLv = player.lord.getVip();
        StaticVip staticVip = StaticVipDataMgr.getVipMap(vipLv);
        int cnt = staticVip.getAutoBuild();
        player.common.setAutoBuildCnt(cnt);
        return player.common.getAutoBuildCnt();
    }

    /**
     * ????????????????????????
     *
     * @param player
     */
    public void syncAutoBuildInfo(Player player) {
        SyncAutoBuildRs.Builder builder = SyncAutoBuildRs.newBuilder();
        builder.setAutoBuildCnt(player.common.getAutoBuildCnt());
        builder.setAutoBuildOnOff(player.common.getAutoBuildOnOff());
        Base.Builder msg = PbHelper.createSynBase(SyncAutoBuildRs.EXT_FIELD_NUMBER, SyncAutoBuildRs.ext,
                builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    /**
     * ??????????????????????????????
     *
     * @param player
     */
    public void refreshSourceData(Player player) {
        player.resource.setElecOut(0);
        player.resource.setOilOut(0);
        player.resource.setOreOut(0);
        player.resource.setFoodOut(0);
        Iterator<Mill> it = player.mills.values().iterator();
        while (it.hasNext()) {
            Mill mill = it.next();
            if (checkMillCanGain(player, mill)) {
                addResourceOutAndMax(mill.getType(), mill.getLv(), player.resource);
            }
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param player
     * @param buildId
     * @return ?????? true ????????????????????????
     */
    public boolean checkBuildIsUpping(Player player, int buildId) {
        for (BuildQue build : player.buildQue.values()) {
            if (build.getPos() == buildId) {
                return true;
            }
        }
        return false;
    }

    public List<ResAdd> listResAdd(Player player) {
        List<ResAdd> list = new ArrayList<>();
        ResAdd.Builder res = ResAdd.newBuilder();
        ResourceMult resourceMult = new ResourceMult();
        Resource resource = player.resource;
        getResourceOut4Command(player, resourceMult);
        getResourceOut4SolarTerms(player, resourceMult);
        techDataManager.getResourceOut4Tech(player, resourceMult);
        getResourceOut4Agent(player, resourceMult);// ????????????
        getResourceOut4SeasonTalent(player, resourceMult);//??????????????????

        res.setResId(AwardType.Resource.OIL).setBaseAdd((int) resource.getOilOut())
                .setWeatherAdd((int) (resource.getOilOut() * (resourceMult.getOilWeath() / Constant.TEN_THROUSAND)))
                .setTechAdd((int) (resource.getOilOut() * (resourceMult.getOilTech() / Constant.TEN_THROUSAND)))
                .setSeasonAdd((int) (resource.getOilOut() * (resourceMult.getOilSeason() / Constant.TEN_THROUSAND)))
                .setAgentAdd((int) (resource.getOilOut() * (resourceMult.getOilAgent() / Constant.TEN_THROUSAND)))
                .setSeasonTalent((int) (resource.getOilOut() * (resourceMult.getOilSeasonTalent() / Constant.TEN_THROUSAND)));
        list.add(res.build());

        res = ResAdd.newBuilder();
        res.setResId(AwardType.Resource.ELE).setBaseAdd((int) resource.getElecOut())
                .setWeatherAdd((int) (resource.getElecOut() * (resourceMult.getElecWeath() / Constant.TEN_THROUSAND)))
                .setTechAdd((int) (resource.getElecOut() * (resourceMult.getElecTech() / Constant.TEN_THROUSAND)))
                .setSeasonAdd((int) (resource.getElecOut() * (resourceMult.getElecSeason() / Constant.TEN_THROUSAND)))
                .setAgentAdd((int) (resource.getElecOut() * (resourceMult.getElecAgent() / Constant.TEN_THROUSAND)))
                .setSeasonTalent((int) (resource.getElecOut() * (resourceMult.getElecSeasonTalent() / Constant.TEN_THROUSAND)));
        list.add(res.build());

        res = ResAdd.newBuilder();
        res.setResId(AwardType.Resource.FOOD).setBaseAdd((int) resource.getFoodOut())
                .setWeatherAdd((int) (resource.getFoodOut() * (resourceMult.getFoodWeath() / Constant.TEN_THROUSAND)))
                .setTechAdd((int) (resource.getFoodOut() * (resourceMult.getFoodTech() / Constant.TEN_THROUSAND)))
                .setSeasonAdd((int) (resource.getFoodOut() * (resourceMult.getFoodSeason() / Constant.TEN_THROUSAND)))
                .setAgentAdd((int) (resource.getFoodOut() * (resourceMult.getFoodAgent() / Constant.TEN_THROUSAND)))
                .setSeasonTalent((int) (resource.getFoodOut() * (resourceMult.getFoodSeasonTalent() / Constant.TEN_THROUSAND)));
        list.add(res.build());

        res = ResAdd.newBuilder();
        res.setResId(AwardType.Resource.ORE).setBaseAdd((int) resource.getOreOut())
                .setWeatherAdd((int) (resource.getOreOut() * (resourceMult.getOreWeath() / Constant.TEN_THROUSAND)))
                .setTechAdd((int) (resource.getOreOut() * (resourceMult.getOreTech() / Constant.TEN_THROUSAND)))
                .setSeasonAdd((int) (resource.getOreOut() * (resourceMult.getOreSeason() / Constant.TEN_THROUSAND)))
                .setAgentAdd((int) (resource.getOreOut() * (resourceMult.getOreAgent() / Constant.TEN_THROUSAND)))
                .setSeasonTalent((int) (resource.getOreOut() * (resourceMult.getOreSeasonTalent() / Constant.TEN_THROUSAND)));
        list.add(res.build());
        LogUtil.channel(player.roleId + resourceMult.toString());
        return list;
    }
}
