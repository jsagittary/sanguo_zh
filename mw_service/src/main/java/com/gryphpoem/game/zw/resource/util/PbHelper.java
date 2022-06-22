package com.gryphpoem.game.zw.resource.util;

import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.rank.RankItem;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.manager.BattlePassDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.*;
import com.gryphpoem.game.zw.pb.GamePb2;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.pb.SerializePb.DbActivity;
import com.gryphpoem.game.zw.pb.SerializePb.DbDay7Act;
import com.gryphpoem.game.zw.pb.SerializePb.DbDay7ActStatus;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityAuctionParam;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.ArmQue;
import com.gryphpoem.game.zw.resource.domain.p.BuildQue;
import com.gryphpoem.game.zw.resource.domain.p.Combat;
import com.gryphpoem.game.zw.resource.domain.p.Day7Act;
import com.gryphpoem.game.zw.resource.domain.p.DbFriend;
import com.gryphpoem.game.zw.resource.domain.p.DbMasterApprentice;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.p.EquipQue;
import com.gryphpoem.game.zw.resource.domain.p.Factory;
import com.gryphpoem.game.zw.resource.domain.p.FemaleAgent;
import com.gryphpoem.game.zw.resource.domain.p.Gains;
import com.gryphpoem.game.zw.resource.domain.p.History;
import com.gryphpoem.game.zw.resource.domain.p.Mill;
import com.gryphpoem.game.zw.resource.domain.p.MultCombatTeam;
import com.gryphpoem.game.zw.resource.domain.p.RedPacket;
import com.gryphpoem.game.zw.resource.domain.p.RedPacketRole;
import com.gryphpoem.game.zw.resource.domain.p.Resource;
import com.gryphpoem.game.zw.resource.domain.p.StoneCombat;
import com.gryphpoem.game.zw.resource.domain.p.TechQue;
import com.gryphpoem.game.zw.resource.domain.p.WallNpc;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.ActRank;
import com.gryphpoem.game.zw.resource.pojo.Equip;
import com.gryphpoem.game.zw.resource.pojo.EquipJewel;
import com.gryphpoem.game.zw.resource.pojo.Mail;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.pojo.SuperEquip;
import com.gryphpoem.game.zw.resource.pojo.Task;
import com.gryphpoem.game.zw.resource.pojo.WarPlane;
import com.gryphpoem.game.zw.resource.pojo.*;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.daily.DailyReport;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.NpcForce;
import com.gryphpoem.game.zw.resource.pojo.global.ScheduleBoss;
import com.gryphpoem.game.zw.resource.pojo.hero.AwakenData;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.medal.Medal;
import com.gryphpoem.game.zw.resource.pojo.medal.RedMedal;
import com.gryphpoem.game.zw.resource.pojo.party.PartySuperSupply;
import com.gryphpoem.game.zw.resource.pojo.party.PartySupply;
import com.gryphpoem.game.zw.resource.pojo.season.SeasonTalent;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureWare;
import com.gryphpoem.game.zw.resource.pojo.world.Area;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinRoleInfo;
import com.gryphpoem.game.zw.resource.pojo.world.CabinetLead;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.pojo.world.Gestapo;
import com.gryphpoem.game.zw.resource.pojo.world.SuperGuard;
import com.gryphpoem.game.zw.resource.pojo.world.SuperMine;
import com.gryphpoem.game.zw.resource.pojo.world.*;
import com.gryphpoem.game.zw.resource.pojo.world.battlepass.BattlePassPersonInfo;
import com.gryphpoem.game.zw.resource.pojo.world.battlepass.GlobalBattlePass;
import com.gryphpoem.game.zw.resource.pojo.world.battlepass.battlePassTask;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PbHelper {

    public static byte[] putShort(short s) {
        byte[] b = new byte[2];
        b[0] = (byte) (s >> 8);
        b[1] = (byte) (s >> 0);
        return b;
    }

    static public short getShort(byte[] b, int index) {
        return (short) (((b[index + 1] & 0xff) | b[index + 0] << 8));
    }

    static public Base parseFromByte(byte[] result) throws InvalidProtocolBufferException {
        short len = PbHelper.getShort(result, 0);
        byte[] data = new byte[len];
        System.arraycopy(result, 2, data, 0, len);
        Base rs = Base.parseFrom(data, DataResource.getRegistry());
        return rs;
    }

    static public <T> Base.Builder createRsBase(int cmd, GeneratedExtension<Base, T> ext, T msg) {
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(cmd);
        baseBuilder.setCode(GameError.OK.getCode());
        if (msg != null) {
            baseBuilder.setExtension(ext, msg);
        }
        return baseBuilder;
    }

    static public <T> Base.Builder createRsBase(GameError gameError, int cmd, GeneratedExtension<Base, T> ext, T msg) {
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(cmd);
        baseBuilder.setCode(gameError.getCode());
        if (msg != null) {
            baseBuilder.setExtension(ext, msg);
        }
        return baseBuilder;
    }

    static public Base createRsBase(int cmd, int code) {
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(cmd);
        baseBuilder.setCode(code);
        return baseBuilder.build();
    }

    public static Base.Builder createErrorBase(int cmd, int code, long param) {
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(cmd);
        baseBuilder.setCode(code);
        if (param != 0) {
            baseBuilder.setParam(param);
        }
        return baseBuilder;
    }

    static public <T> Base.Builder createRqBase(int cmd, Long param, GeneratedExtension<Base, T> ext, T msg) {
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(cmd);
        if (param != null) {
            baseBuilder.setParam(param);
        }
        baseBuilder.setExtension(ext, msg);
        return baseBuilder;
    }

    static public <T> Base.Builder createSynBase(int cmd, GeneratedExtension<Base, T> ext, T msg) {
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(cmd);
        if (msg != null) {
            baseBuilder.setExtension(ext, msg);
        }

        return baseBuilder;
    }

    /**
     * Method: createAwardPb
     *
     * @Description: 无keyId的奖励 @param type @param id @param count @return @return CommonPb.Award @throws
     */
    static public CommonPb.Award createAwardPb(int type, int id, int count) {
        CommonPb.Award.Builder builder = CommonPb.Award.newBuilder();
        builder.setType(type);
        builder.setId(id);
        builder.setCount(count);
        return builder.build();
    }

    public static List<byte[]> createAwardByte(int type, int id, int count) {
        List<byte[]> list = new ArrayList<>();
        list.add(createAwardPb(type, id, count).toByteArray());
        return list;
    }

    public static List<Award> createAwards(List<AwardItem> items) {
        List<Award> list = new ArrayList<>();
        Optional.ofNullable(items).ifPresent(tmps -> items.forEach(obj -> list.add(createAward(obj))));
        return list;
    }

    public static Award createAward(AwardItem awardItem) {
        return createAwardPb(awardItem.getType(), awardItem.getId(), awardItem.getCount());
    }

    /**
     * Method: createAwardPb
     *
     * @Description: 有keyId的奖励 @param type @param id @param count @param keyId @return @return CommonPb.Award @throws
     */
    static public CommonPb.Award createAwardPb(int type, int id, int count, int keyId) {
        CommonPb.Award.Builder builder = CommonPb.Award.newBuilder();
        builder.setType(type);
        builder.setId(id);
        builder.setCount(count);
        if (keyId != 0) {
            builder.setKeyId(keyId);
        }

        return builder.build();
    }

    static public CommonPb.Award createAwardPbWithParam(int type, int id, int count, int keyId, int... param) {
        CommonPb.Award.Builder builder = CommonPb.Award.newBuilder();
        builder.setType(type);
        builder.setId(id);
        builder.setCount(count);
        if (keyId != 0) {
            builder.setKeyId(keyId);
        }
        for (int i = 0; i < param.length; i++) {
            builder.addParam(param[i]);
        }

        return builder.build();
    }

    static public CommonPb.Award createAwardPbWithParam(int type, int id, int count, int keyId, List<Integer> param) {
        CommonPb.Award.Builder builder = CommonPb.Award.newBuilder();
        builder.setType(type);
        builder.setId(id);
        builder.setCount(count);
        if (keyId != 0) {
            builder.setKeyId(keyId);
        }
        if (!ObjectUtils.isEmpty(param)) {
            builder.addAllParam(param);
        }

        return builder.build();
    }

    /**
     * 创建奖励pb
     *
     * @param awardList
     * @return
     */
    public static List<CommonPb.Award> createAwardsPb(List<List<Integer>> awardList) {
        List<CommonPb.Award> awards = new ArrayList<CommonPb.Award>();
        if (!CheckNull.isEmpty(awardList)) {
            int type = 0;
            int id = 0;
            int count = 0;
            for (List<Integer> award : awardList) {
                if (award.size() != 3) {
                    continue;
                }

                type = award.get(0);
                id = award.get(1);
                count = award.get(2);
                awards.add(createAwardPb(type, id, count));
            }
        }
        return awards;
    }

    /**
     * 获得某些类型的翻倍奖励
     *
     * @param awardList
     * @param multiple
     * @param awardTypes 需要翻倍的奖励类型
     * @return
     */
    public static List<CommonPb.Award> createMultipleAwardsPb(List<List<Integer>> awardList, double multiple, List<Integer> awardTypes) {
        List<CommonPb.Award> awards = new ArrayList<CommonPb.Award>();
        if (!CheckNull.isEmpty(awardList)) {
            int type = 0;
            int id = 0;
            Integer count;
            for (List<Integer> award : awardList) {
                if (award.size() != 3) {
                    continue;
                }

                type = award.get(0);
                id = award.get(1);
                count = new Integer(award.get(2));
                if (Objects.nonNull(awardTypes) && awardTypes.contains(type)) {
                    count = multiple != 0d ? (int) (count * multiple) : count;
                }
                awards.add(createAwardPb(type, id, count));
            }
        }
        return awards;
    }

    /**
     * 奖励列表翻倍
     * @param awardList
     * @param multiple
     * @return
     */
    public static List<CommonPb.Award> createMultipleAwardsPb(List<List<Integer>> awardList, double multiple) {
        List<CommonPb.Award> awards = new ArrayList<CommonPb.Award>();
        if (!CheckNull.isEmpty(awardList)) {
            int type = 0;
            int id = 0;
            Integer count;
            for (List<Integer> award : awardList) {
                if (award.size() != 3) {
                    continue;
                }

                type = award.get(0);
                id = award.get(1);
                count = new Integer(award.get(2));
                count = multiple != 0d ? (int) (count * multiple) : count;
                awards.add(createAwardPb(type, id, count));
            }
        }
        return awards;
    }

    public static Award createAward(List<Integer> list) {
        if (ListUtils.isBlank(list)) {
            return null;
        }
        return createAwardPb(list.get(0), list.get(1), list.get(2));
    }

    public static CommonPb.Task createTaskPb(Task task) {
        CommonPb.Task.Builder builder = CommonPb.Task.newBuilder();
        builder.setTaskId(task.getTaskId());
        builder.setSchedule(task.getSchedule());
        // builder.setAccept(task.getAccept());
        builder.setStatus(task.getStatus());
        return builder.build();
    }

    public static CommonPb.Task createTaskPb(Task task,Object...objs) {
        CommonPb.Task.Builder builder = CommonPb.Task.newBuilder();
        builder.setTaskId(task.getTaskId());
        builder.setSchedule(task.getSchedule());
        // builder.setAccept(task.getAccept());
        builder.setStatus(task.getStatus());
        if (objs.length > 0 && objs[0] instanceof Integer) {
            int taskType = Integer.parseInt(String.valueOf(objs[0]));
            builder.setTaskType(taskType);
        }
        return builder.build();
    }

    public static CommonPb.Task buildTask(int taskId, long progress, int state) {
        CommonPb.Task.Builder builder = CommonPb.Task.newBuilder();
        builder.setTaskId(taskId);
        builder.setSchedule(progress);
        builder.setStatus(state);
        return builder.build();
    }

    public static CommonPb.Area createAreaPb(Area area) {
        CommonPb.Area.Builder builder = CommonPb.Area.newBuilder();
        builder.setArea(area.getArea());
        builder.setStatus(area.getStatus());
        builder.addAllFirstKill(PbHelper.createFirstKill(area.getCityFirstKill()));
        return builder.build();
    }

    private static List<CityFirstKill> createFirstKill(Map<String, Map<String, List<Long>>> cityFirstKill) {
        List<CityFirstKill> cityFirstKillList = new ArrayList<>();
        for (Entry<String, Map<String, List<Long>>> entry : cityFirstKill.entrySet()) {
            CityFirstKill.Builder builder = CityFirstKill.newBuilder();
            builder.setCityInfo(entry.getKey());
            builder.setSponsor(createFirstKillInfo(entry.getValue(), WorldConstant.KILL_SPONSOR));
            builder.setAtklist(createFirstKillInfo(entry.getValue(), WorldConstant.KILL_ATKLIST));
            cityFirstKillList.add(builder.build());
        }
        return cityFirstKillList;
    }

    private static FirstKillInfo createFirstKillInfo(Map<String, List<Long>> killInfo, String killSponsor) {
        FirstKillInfo.Builder builder = FirstKillInfo.newBuilder();
        if (killInfo.containsKey(killSponsor)) {
            builder.setRole(killSponsor);
            builder.addAllRolesId(killInfo.get(killSponsor));
        }
        return builder.build();
    }

    public static CommonPb.City createCityPb(City city) {
        CommonPb.City.Builder builder = CommonPb.City.newBuilder();
        builder.setCityId(city.getCityId());
        builder.setCityLv(city.getCityLv());
        builder.setCamp(city.getCamp());
        builder.setStatus(city.getStatus());
        builder.setCloseTime(city.getCloseTime());
        builder.setAttackCamp(city.getAttackCamp());
        builder.setOwnerId(city.getOwnerId());
        builder.setBeginTime(city.getBeginTime());
        builder.setEndTime(city.getEndTime());
        builder.setProduced(city.getProduced());
        builder.setFinishTime(city.getFinishTime());
        builder.setCampaignTime(city.getCampaignTime());
        builder.setProtectTime(city.getProtectTime());
        if (city.getCampaignList() != null) {
            builder.addAllRole(city.getCampaignList());
        }
        builder.setExtraReward(city.getExtraReward());
        if (city.getFormList() != null) {
            for (CityHero hero : city.getFormList()) {
                builder.addForm(createTwoIntPb(hero.getNpcId(), hero.getCurArm()));
            }
        }
        if (!CheckNull.isNullTrim(city.getName())) {
            builder.setName(city.getName());
        }
        builder.setCityExp(city.getExp());
        builder.setAtkBeginTime(city.getAtkBeginTime());
        builder.setDevTime(city.getNextDevTime());
        if (!CheckNull.isEmpty(city.getAttackRoleId())) {
            builder.addAllAttackRoleId(city.getAttackRoleId());
        }
        builder.setBuildingExp(city.getBuildingExp());
        builder.setLeaveOver(city.getLeaveOver());
        return builder.build();
    }

    public static CommonPb.TwoInt createTwoIntPb(int v1, int v2) {
        CommonPb.TwoInt.Builder builder = CommonPb.TwoInt.newBuilder();
        builder.setV1(v1);
        builder.setV2(v2);
        return builder.build();
    }

    public static TwoStr createTwoStr(String v1,String v2){
        TwoStr.Builder builder = TwoStr.newBuilder();
        builder.setV1(v1);
        builder.setV2(v2);
        return builder.build();
    }

    public static LongInt createLongIntPb(long v1, int v2) {
        LongInt.Builder builder = LongInt.newBuilder();
        builder.setV1(v1);
        builder.setV2(v2);
        return builder.build();
    }

    public static IntLong createIntLongPc(int v1, long v2) {
        IntLong.Builder builder = IntLong.newBuilder();
        builder.setV1(v1);
        builder.setV2(v2);
        return builder.build();
    }

    public static CommonPb.StrInt createStrIntPb(String v1, int v2) {
        CommonPb.StrInt.Builder builder = CommonPb.StrInt.newBuilder();
        builder.setV1(v1);
        builder.setV2(v2);
        return builder.build();
    }

    private static int getAttrValue(List<TwoInt> list, int attrId) {
        if (Objects.isNull(list)) {
            return 0;
        }
        Optional<TwoInt> twoInt = list.stream().filter(attr -> (attr.getV1() == attrId)).findFirst();
        return twoInt.isPresent() ? twoInt.get().getV2() : 0;
    }

    public static CommonPb.Hero createHeroPb(Hero hero, Player player, List<TwoInt> list) {
        CommonPb.Hero.Builder builder = CommonPb.Hero.newBuilder();
        builder.setHeroId(hero.getHeroId());
        builder.setLevel(hero.getLevel());
        builder.setExp(hero.getExp());

        for (int i = HeroConstant.ATTR_ATTACK; i <= HeroConstant.ATTR_LEAD; i++) {
            int addAttrValue = getAttrValue(list, i);
            builder.addAttr(createTwoIntPb(i, hero.getAttr()[i] + addAttrValue));// 将领属性
        }
        for (Entry<Integer, Integer> entry : hero.getExtAttrs().entrySet()) {
            int addAttrValue = getAttrValue(list, entry.getKey());
            builder.addAttr(createTwoIntPb(entry.getKey(), entry.getValue() + addAttrValue));
        }
        int value;
//        for (int i = 0; i < hero.getWash().length; i++) {// 将领洗炼数据
//            value = hero.getWash()[i];
//            if (value > 0) {
//                builder.addWash(createTwoIntPb(i, value));
//            }
//        }
        for (int i = 0; i < hero.getEquip().length; i++) {// 将领装备信息
            value = hero.getEquip()[i];
            if (value > 0) {
                builder.addEquip(createTwoIntPb(i, value));
            }
        }
        builder.setStatus(hero.getStatus());
        if (hero.isOnBattle() || hero.isOnAcq() || hero.isOnWall() || hero.isCommando()) {
            builder.setCount(hero.getCount());
            builder.setPos(hero.getPos());
        }
        builder.setState(hero.getState());
        builder.setWallPos(hero.getWallPos());
        builder.setBreakExp(hero.getBreakExp());
        builder.setWallArmyTime(hero.getWallArmyTime());
        builder.setQuality(hero.getQuality());
        builder.setWashTotalFloorCount(hero.getWashTotalFloorCount());
        builder.setAcqPos(hero.getAcqPos());
        builder.setDefPos(hero.getDefPos());
        builder.setDecorated(hero.getDecorated());
        List<Integer> warPlanes = hero.getWarPlanes();
        if (!CheckNull.isEmpty(warPlanes)) {
            for (int planeId : warPlanes) {
                try {
                    WarPlane warPlane = player.checkWarPlaneIsExist(planeId);
                    if (!CheckNull.isNull(warPlane)) {
                        builder.addWarPlane(createTwoIntPb(planeId, warPlane.getLevel()));
                        builder.addPlanePos(createTwoIntPb(warPlane.getBattlePos(), planeId));
                    }
                } catch (MwException e) {
                    LogUtil.error(e);
                }
            }
        }
        Map<Integer, Integer> showFight = hero.getShowFight();
        if (!CheckNull.isEmpty(showFight)) {
            for (Entry<Integer, Integer> en : showFight.entrySet()) {
                builder.addShowFight(createTwoIntPb(en.getKey(), en.getValue()));
            }
        }
        builder.setComandoPos(hero.getCommandoPos());
        Map<Integer, AwakenData> awakenMap = hero.getAwaken();
        if (!CheckNull.isEmpty(awakenMap)) {
            CommonPb.AwakenData.Builder data = CommonPb.AwakenData.newBuilder();
            awakenMap.values().forEach(awakenData -> {
                if (CheckNull.isNull(awakenData))
                    return;
                data.setStatus(awakenData.getStatus());
                Map<Integer, Integer> evolutionGene = awakenData.getEvolutionGene();
                if (!CheckNull.isEmpty(evolutionGene)) {
                    for (Entry<Integer, Integer> en : evolutionGene.entrySet()) {
                        data.addEvolutionGene(PbHelper.createTwoIntPb(en.getKey(), en.getValue()));
                    }
                }
                builder.addAwakendata(data.build());
                data.clear();
            });
        }

        builder.setSandTableState(hero.getSandTableState());
        builder.setFightVal(hero.getFightVal());
        builder.setCgyStage(hero.getCgyStage());
        builder.setCgyLv(hero.getCgyLv());
        for (Entry<Integer, Integer> entry : hero.getSkillLevels().entrySet()) {
            builder.addSkillLevel(createTwoIntPb(entry.getKey(), entry.getValue()));
        }
        builder.setIsOnBaitTeam(hero.isOnBaitTeam() ? 1 : 0);
        Stream.iterate(1,i->i+1).limit(hero.getTotem().length-1).forEach(j -> {
            if(hero.getTotemKey(j) > 0){
                builder.addTotem(PbHelper.createTwoIntPb(j,hero.getTotemKey(j)));
            }
        });
        if (Objects.nonNull(hero.getTreasureWare())) {
            builder.setTreasureWare(hero.getTreasureWare());
        }
        builder.setGrade(hero.getGradeKeyId());
        return builder.build();
    }

    public static CommonPb.Hero createHeroPb(Hero hero, Player player) {
        CommonPb.Hero.Builder builder = CommonPb.Hero.newBuilder();
        builder.setHeroId(hero.getHeroId());
        builder.setLevel(hero.getLevel());
        builder.setExp(hero.getExp());
        for (int i = HeroConstant.ATTR_ATTACK; i <= HeroConstant.ATTR_LEAD; i++) {
            builder.addAttr(createTwoIntPb(i, hero.getAttr()[i]));// 将领属性
        }
        for (Entry<Integer, Integer> entry : hero.getExtAttrs().entrySet()) {
            builder.addAttr(createTwoIntPb(entry.getKey(), entry.getValue()));
        }
        int value;
//        for (int i = 0; i < hero.getWash().length; i++) {// 将领洗炼数据
//            value = hero.getWash()[i];
//            if (value > 0) {
//                builder.addWash(createTwoIntPb(i, value));
//            }
//        }
        for (int i = 0; i < hero.getEquip().length; i++) {// 将领装备信息
            value = hero.getEquip()[i];
            if (value > 0) {
                builder.addEquip(createTwoIntPb(i, value));
            }
        }
        builder.setStatus(hero.getStatus());
        if (hero.isOnBattle() || hero.isOnAcq() || hero.isOnWall() || hero.isCommando()) {
            builder.setCount(hero.getCount());
            builder.setPos(hero.getPos());
        }
        builder.setState(hero.getState());
        builder.setWallPos(hero.getWallPos());
        builder.setBreakExp(hero.getBreakExp());
        builder.setWallArmyTime(hero.getWallArmyTime());
        builder.setQuality(hero.getQuality());
        builder.setWashTotalFloorCount(hero.getWashTotalFloorCount());
        builder.setAcqPos(hero.getAcqPos());
        builder.setDefPos(hero.getDefPos());
        builder.setDecorated(hero.getDecorated());
        List<Integer> warPlanes = hero.getWarPlanes();
        if (!CheckNull.isEmpty(warPlanes)) {
            for (int planeId : warPlanes) {
                try {
                    WarPlane warPlane = player.checkWarPlaneIsExist(planeId);
                    if (!CheckNull.isNull(warPlane)) {
                        builder.addWarPlane(createTwoIntPb(planeId, warPlane.getLevel()));
                        builder.addPlanePos(createTwoIntPb(warPlane.getBattlePos(), planeId));
                    }
                } catch (MwException e) {
                    LogUtil.error(e);
                }
            }
        }
        Map<Integer, Integer> showFight = hero.getShowFight();
        if (!CheckNull.isEmpty(showFight)) {
            for (Entry<Integer, Integer> en : showFight.entrySet()) {
                builder.addShowFight(createTwoIntPb(en.getKey(), en.getValue()));
            }
        }
        builder.setComandoPos(hero.getCommandoPos());
        Map<Integer, AwakenData> awakenMap = hero.getAwaken();
        if (!CheckNull.isEmpty(awakenMap)) {
            CommonPb.AwakenData.Builder data = CommonPb.AwakenData.newBuilder();
            awakenMap.values().forEach(awakenData -> {
                if (CheckNull.isNull(awakenData))
                    return;
                data.setStatus(awakenData.getStatus());
                Map<Integer, Integer> evolutionGene = awakenData.getEvolutionGene();
                if (!CheckNull.isEmpty(evolutionGene)) {
                    for (Entry<Integer, Integer> en : evolutionGene.entrySet()) {
                        data.addEvolutionGene(PbHelper.createTwoIntPb(en.getKey(), en.getValue()));
                    }
                }
                builder.addAwakendata(data.build());
                data.clear();
            });
        }

        builder.setSandTableState(hero.getSandTableState());
        builder.setFightVal(hero.getFightVal());
        builder.setCgyStage(hero.getCgyStage());
        builder.setCgyLv(hero.getCgyLv());
        for (Entry<Integer, Integer> entry : hero.getSkillLevels().entrySet()) {
            builder.addSkillLevel(createTwoIntPb(entry.getKey(), entry.getValue()));
        }
        builder.setIsOnBaitTeam(hero.isOnBaitTeam() ? 1 : 0);
        Stream.iterate(1,i->i+1).limit(hero.getTotem().length-1).forEach(j -> {
            if(hero.getTotemKey(j) > 0){
                builder.addTotem(PbHelper.createTwoIntPb(j,hero.getTotemKey(j)));
            }
        });
        if (Objects.nonNull(hero.getTreasureWare())) {
            builder.setTreasureWare(hero.getTreasureWare());
        }
        builder.setGrade(hero.getGradeKeyId());
        return builder.build();
    }

    public static CommonPb.Equip createEquipPb(Equip equip) {
        CommonPb.Equip.Builder builder = CommonPb.Equip.newBuilder();
        builder.setKeyId(equip.getKeyId());
        builder.setEquipId(equip.getEquipId());
        for (int i = 0; i < equip.getAttrAndLv().size(); i++) {
            // 当前洗炼属性
            Turple<Integer, Integer> attrLv = equip.getAttrAndLv().get(i);
            builder.addAttr(createTwoIntPb(attrLv.getA(), attrLv.getB()));
        }
        if (equip instanceof Ring) {
            builder.setExta(createEquipExtrPb((Ring) equip));
        }
        if (equip.getEquipLocked() != 0) {
            // 如果装备上锁标识不为0则设置返回值
            builder.setEquipLocked(equip.getEquipLocked());
        }
        return builder.build();
    }

    public static CommonPb.EquipExtr createEquipExtrPb(Ring ring) {
        EquipExtr.Builder builder = EquipExtr.newBuilder();
        builder.setLv(ring.getLv());
        builder.setUpCnt(ring.getCount());
        builder.setUpProbability(ring.getUpProbability());
        List<Integer> jewels = ring.getJewels();
        if (!CheckNull.isEmpty(jewels)) {
            jewels.forEach(jewel -> builder.addRingStone(jewel));
        }
        Map<Integer, Integer> attr = ring.getExtAttr();
        if (!CheckNull.isEmpty(attr)) {
            attr.entrySet().forEach(en -> builder.addAttr(createTwoIntPb(en.getKey(), en.getValue())));
        }
        return builder.build();
    }

    /**
     * @param medal
     * @return CommonPb.Medal
     * @Title: createMedalPb
     * @Description: 给客户端的玩家勋章数据
     */
    public static CommonPb.Medal createMedalPb(Medal medal) {
        CommonPb.Medal.Builder builder = CommonPb.Medal.newBuilder();
        builder.setKeyId(medal.getKeyId());
        builder.setMedalId(medal.getMedalId());
        builder.setQuality(medal.getQuality());
        builder.setHeroId(medal.getHeroId());
        builder.setLevel(medal.getLevel());
        if (medal.getGeneralSkillId() != null && medal.getGeneralSkillId().size() > 0) {
            builder.addAllGeneralSkillId(medal.getGeneralSkillId());
        }
        if (medal.getSpecialSkillId() != null && medal.getSpecialSkillId() > 0) {
            builder.setSpecialSkillId(medal.getSpecialSkillId());
        }
        if (medal.hasAuraSkill()) {
            builder.setAuraSkillId(medal.getAuraSkillId());
        }
        if (medal.getInitAuraSkillId() != null && medal.getInitAuraSkillId() > 0) {
            builder.setInitAuraSkillId(medal.getInitAuraSkillId());
        }
        if (medal.getMedalAttr() != null) {
            Turple<Integer, Integer> medalAttr = medal.getMedalAttr();
            builder.setMedalAttr(createTwoIntPb(medalAttr.getA(), medalAttr.getB()));
        }
        if (medal.getIsLock() != null) {
            builder.setIsLock(medal.getIsLock());
        }

        return builder.build();
    }

    public static CommonPb.Equip createEquipSavePb(Equip equip) {
        CommonPb.Equip.Builder builder = CommonPb.Equip.newBuilder();
        builder.setKeyId(equip.getKeyId());
        builder.setEquipId(equip.getEquipId());
        // 当前洗炼属性
        for (int i = 0; i < equip.getAttrAndLv().size(); i++) {
            Turple<Integer, Integer> attrlv = equip.getAttrAndLv().get(i);
            builder.addAttr(createTwoIntPb(attrlv.getA(), attrlv.getB()));
        }
        builder.setHeroId(equip.getHeroId());
        builder.setNotUpLvCnt(equip.getNotUpLvCnt());
        if (equip instanceof Ring) {
            builder.setExta(createEquipExtrPb((Ring) equip));
        }
        builder.setEquipLocked(equip.getEquipLocked());
        return builder.build();
    }

    /**
     * @param medal
     * @return CommonPb.Medal
     * @Title: createMedalSavePb
     * @Description: 玩家勋章全部数据
     */
    public static CommonPb.Medal createMedalSavePb(Medal medal) {
        CommonPb.Medal.Builder builder = CommonPb.Medal.newBuilder();
        builder.setKeyId(medal.getKeyId());
        builder.setMedalId(medal.getMedalId());
        builder.setQuality(medal.getQuality());
        builder.setHeroId(medal.getHeroId());
        builder.setLevel(medal.getLevel());
        if (medal.getGeneralSkillId() != null && medal.getGeneralSkillId().size() > 0) {
            builder.addAllGeneralSkillId(medal.getGeneralSkillId());
        }
        if (medal.getSpecialSkillId() != null && medal.getSpecialSkillId() > 0) {
            builder.setSpecialSkillId(medal.getSpecialSkillId());
        }
        if (medal.hasAuraSkill()) {
            builder.setAuraSkillId(medal.getAuraSkillId());
        }
        if (medal.getInitAuraSkillId() != null && medal.getInitAuraSkillId() > 0) {
            builder.setInitAuraSkillId(medal.getInitAuraSkillId());
        }
        if (medal.getMedalAttr() != null) {
            Turple<Integer, Integer> medalAttr = medal.getMedalAttr();
            builder.setMedalAttr(createTwoIntPb(medalAttr.getA(), medalAttr.getB()));
        }
        if (medal.getIntensifyCnt() != null) {
            builder.setIntensifyCnt(medal.getIntensifyCnt());
        }
        if (medal.getGoldBarCnt() != null) {
            builder.setGoldBarCnt(medal.getGoldBarCnt());
        }
        if (medal.getIsLock() != null) {
            builder.setIsLock(medal.getIsLock());
        }
        if (medal instanceof RedMedal) {
            RedMedal redMedal = (RedMedal) medal;
            MedalExt.Builder ext = MedalExt.newBuilder();
            ext.setAuraUnLock(redMedal.isAuraUnLock());
            ext.setSpecialSkillUnLock(redMedal.isSpecialSkillUnLock());
            builder.setExt(ext.build());
        }
        return builder.build();
    }

    public static CommonPb.Prop createPropPb(Prop prop) {
        CommonPb.Prop.Builder builder = CommonPb.Prop.newBuilder();
        builder.setPropId(prop.getPropId());
        builder.setCount(prop.getCount());
        builder.setUseCount(prop.getUseCount());
        builder.setUseTime(prop.getUseTime());
        return builder.build();
    }

    public static CommonPb.BuildQue createBuildQuePb(BuildQue e) {
        CommonPb.BuildQue.Builder builder = CommonPb.BuildQue.newBuilder();
        builder.setKeyId(e.getKeyId());
        builder.setBuildingType(e.getBuildingType());
        builder.setId(e.getPos());
        builder.setPeriod(e.getPeriod());
        builder.setEndTime(e.getEndTime());
        builder.setIndex(e.getIndex());
        builder.setFromType(e.getFromType());
        builder.setNewType(e.getNewType());
        if (e.haveFreeSpeed()) {
            builder.setFree(e.getFree());
            builder.setParam(e.getParam());
        }
        return builder.build();
    }

    public static CommonPb.Mill createMillPb(Mill mill) {
        CommonPb.Mill.Builder builder = CommonPb.Mill.newBuilder();
        builder.setId(mill.getPos());
        builder.setType(mill.getType());
        builder.setLv(mill.getLv());
        builder.setGainCnt(mill.getResCnt());
        builder.setResTime(mill.getResTime());
        builder.setUnlock(mill.isUnlock());
        return builder.build();
    }

    public static CommonPb.BuildingBase createBuildingBaseByExtPb(BuildingExt ext) {
        BuildingBase.Builder builder = BuildingBase.newBuilder();
        builder.setId(ext.getId());
        builder.setType(ext.getType());
        builder.setUnlock(ext.isUnlock());
        builder.setUnLockTime(ext.getUnLockTime());
        return builder.build();
    }

    public static CommonPb.Gains createGainsPb(Gains gains) {
        CommonPb.Gains.Builder builder = CommonPb.Gains.newBuilder();
        builder.setId(gains.getId());
        builder.setType(gains.getType());
        builder.setEndTime(gains.getEndTime());
        return builder.build();
    }

    public static CommonPb.AreaForce createAreaForcePb(int pos, int camp, int lv, int command) {
        CommonPb.AreaForce.Builder builder = CommonPb.AreaForce.newBuilder();
        builder.setPos(pos);
        builder.setCamp(camp);
        builder.setLevel(lv);
        builder.setCommand(command);
        return builder.build();
    }

    public static CommonPb.AreaGestapo createAreaGestapoPb(int pos, long summoner, int endTime) {
        CommonPb.AreaGestapo.Builder builder = CommonPb.AreaGestapo.newBuilder();
        builder.setPos(pos);
        builder.setSummoner(summoner);
        builder.setEndTime(endTime);
        return builder.build();
    }

    public static CommonPb.Effect createEffectPb(Effect effect) {
        CommonPb.Effect.Builder builder = CommonPb.Effect.newBuilder();
        builder.setId(effect.getEffectType());
        builder.setEndTime(effect.getEndTime());
        builder.setVal(effect.getEffectVal());
        return builder.build();
    }

    public static CommonPb.AreaCity createAreaCityPb(Player player, City city, Player cityOwner) {
        CommonPb.AreaCity.Builder builder = CommonPb.AreaCity.newBuilder();
        builder.setCityId(city.getCityId());
        builder.setCamp(city.getCamp());
        builder.setProtectTime(city.getProtectTime());
        if (city.isProtect()) {
            builder.setFree(true);
        }
        if (city.getAttackCamp() > 0) {
            builder.setAttackCamp(city.getAttackCamp());
        }
        if (city.getProduced() > 0) {
            builder.setProduced(city.getProduced());
        }
        if (city.getFinishTime() > 0) {
            builder.setFinishTime(city.getFinishTime());
        }
        if (!CheckNull.isNull(cityOwner)) {
            builder.setOwnerId(cityOwner.roleId);
            builder.setOwner(cityOwner.lord.getNick());
            builder.setPortrait(cityOwner.lord.getPortrait());
        }
        if (!CheckNull.isNullTrim(city.getName())) {
            builder.setCityName(city.getName());
        }
        builder.setHasFirstKillReward(city.hasFirstKillReward(player.roleId));
        return builder.build();
    }

    public static CommonPb.Factory createFactoryPb(int id, Factory factory) {
        CommonPb.Factory.Builder builder = CommonPb.Factory.newBuilder();
        builder.setId(id);
        builder.setFctExpLv(factory.getFctExpLv());
        builder.setFctLv(factory.getFctLv());
        for (ArmQue armQue : factory.getAddList()) {
            builder.addArmQue(createArmQuePb(armQue));
        }
        return builder.build();
    }

    public static CommonPb.ArmQue createArmQuePb(ArmQue armQue) {
        CommonPb.ArmQue.Builder builder = CommonPb.ArmQue.newBuilder();
        builder.setKeyId(armQue.getKeyId());
        builder.setId(armQue.getBuildingId());
        builder.setAddArm(armQue.getAddArm());
        builder.setEndTime(armQue.getEndTime());
        builder.setTime(armQue.getTime());
        builder.setNeedFood(armQue.getNeedFood());
        if (armQue.haveFreeSpeed()) {
            builder.setFree(armQue.getFree());
            builder.setParam(armQue.getParam());
        }
        builder.setNeedOIL(armQue.getNeedOIL());
        builder.setIsNotExtendQue(armQue.isNotExtendQue());
        return builder.build();
    }

    public static CommonPb.EquipQue createEquipQuePb(EquipQue equipQue) {
        CommonPb.EquipQue.Builder builder = CommonPb.EquipQue.newBuilder();
        builder.setKeyId(equipQue.getKeyId());
        builder.setEquipId(equipQue.getEquipId());
        builder.setEndTime(equipQue.getEndTime());
        builder.setPeriod(equipQue.getPeriod());
        builder.setFreeCnt(equipQue.getFreeCnt());
        builder.setEmployeId(equipQue.getEmployeId());
        return builder.build();
    }

    public static CommonPb.History createHistoryPb(int type, History history) {
        CommonPb.History.Builder builder = CommonPb.History.newBuilder();
        builder.setType(type);
        builder.setId(history.getId());
        builder.setPraram(history.getParam());
        return builder.build();
    }

    public static CommonPb.Army createArmyPb(Army army, boolean isSave) {
        CommonPb.Army.Builder builder = CommonPb.Army.newBuilder();
        builder.setKeyId(army.getKeyId());
        builder.setType(army.getType());
        builder.setTarget(army.getTarget());
        builder.setState(army.getState());
        builder.setDuration(army.getDuration());
        builder.setEndTime(army.getEndTime());
        builder.addAllHero(army.getHero());
        builder.setId(army.getTargetId());
        builder.setBattleTime(army.getBattleTime());
        builder.setTarLordId(army.getTarLordId());
        builder.setLordId(army.getLordId());
        if (isSave && !CheckNull.isEmpty(army.getGrab())) {
            builder.addAllGrab(army.getGrab());
        }
        if (army.getBattleId() != null) {
            builder.setBattleId(army.getBattleId());
        }
        List<Award> marchConsume = army.getMarchConsume();
        if (!CheckNull.isEmpty(marchConsume)) {
            builder.addAllMarchConsume(marchConsume);
        }
        builder.setSubType(army.getSubType());
        // TODO: 2020/5/14  需求有变，客户端不显示行军时候勋章的光环效果了
        // if (!CheckNull.isEmpty(army.getHeroMedals())) {
        //     builder.addAllMedal(army.getHeroMedals());
        // }
        int marchLineId = army.getMarchLineId();
        if (marchLineId != 0) {
            builder.setMarchLine(marchLineId);
        }
        if (isSave && !ObjectUtils.isEmpty(army.getSeasonTalentAttr())) {
            builder.addAllSeasonTalentAttr(army.getSeasonTalentAttr());
        }
        return builder.build();
    }


    public static CommonPb.Collect.Builder createCollectBuilder(Guard guard) {
        CommonPb.Collect.Builder collect = CommonPb.Collect.newBuilder();
        collect.setCamp(guard.getCamp());
        collect.setLv(guard.getLv());
        collect.setRoleId(guard.getRoleId());
        collect.setName(guard.getNick());
        collect.setHeroLv(guard.getHeroLv());
        collect.setHeroId(guard.getHeroId());
        collect.setCount(guard.getArmCount());
        if (guard.getPlayer() != null) {
            Hero h = guard.getPlayer().heros.get(guard.getHeroId());
            if (h != null) {
                collect.setHeroDecorated(h.getDecorated());
            }
        }
        return collect;
    }

    public static CommonPb.MapForce createMapForce(int pos, int type, int param, String name, int camp,
                                                   CommonPb.Collect collect, int resource, boolean battle, int prot, int battleTime) {
        CommonPb.MapForce.Builder builder = CommonPb.MapForce.newBuilder();
        builder.setPos(pos);
        builder.setType(type);
        builder.setParam(param);
        if (null != name) {
            builder.setName(name);
        }
        if (camp >= 0) {
            builder.setCamp(camp);
        }
        if (null != collect) {
            builder.setCollect(collect);
        }
        if (resource > 0) {
            builder.setResource(resource);
        }
        builder.setBattle(battle);
        builder.setProt(prot);
        if (battleTime > 0) {
            builder.setBattleTime(battleTime);
        }
        return builder.build();
    }

    public static CommonPb.MapForce createMapForceByPlayer(Player p, boolean battle, int prot, boolean showSummon,
                                                           int protTime, int rebelRoundId) {
        CommonPb.MapForce.Builder builder = CommonPb.MapForce.newBuilder();
        builder.setPos(p.lord.getPos());
        builder.setType(WorldConstant.FORCE_TYPE_PLAYER);
        builder.setParam(p.building.getCommand());
        builder.setName(p.lord.getNick());
        builder.setCamp(p.lord.getCamp());
        builder.setBattle(battle);
        builder.setProt(prot);
        builder.setProtectTime(protTime);
        builder.setLordId(p.lord.getLordId());
        builder.setEcisiveBattle(p.getDecisiveInfo().isDecisive());// 判断玩家是否决战状态
        // 插白旗时间
        DecisiveInfo decisiveInfo = p.getDecisiveInfo();
        if (decisiveInfo != null) {
            int endTime = decisiveInfo.getFlyTime() + WorldConstant.DECISIVE_BATTLE_FINAL_TIME.get(0);
            int now = TimeHelper.getCurrentSecond();
            if (endTime > now) {
                builder.setWhiteFlagTime(endTime);
            }
        }
        if (showSummon && p.summon != null) {
            builder.setSummonCnt(p.summon.getRespondId().size());
            builder.setSummonSum(p.summon.getSum());
            builder.setSummonTime(p.summon.getLastTime() + Constant.SUMMON_KEEP_TIME);
        }
        builder.setRebelRoundId(rebelRoundId);
        builder.setSeqId(p.getCurCastleSkin());
        builder.setCurrSkinStar(p.getCastleSkinStarById(p.getCurCastleSkin()));
        Optional.ofNullable(p.getDressUp())
                .ifPresent(dressUp -> builder.setTitleId(dressUp.getCurTitle()));
        Optional.ofNullable(p.getDressUp())
                .ifPresent(dressUp -> builder.setCurNamePlate(dressUp.getCurNamePlate()));
        Optional.ofNullable(p.getDressUp())
                .ifPresent(dressUp -> builder.setTitleId(dressUp.getCurTitle()));
        return builder.build();
    }

    public static CommonPb.MapCity createMapCityPb(Player player, int pos, City city, StaticCity staticCity,
                                                   String owner, Set<Integer> atkCamp) {
        if (null == city) {
            return null;
        }

        CommonPb.MapCity.Builder builder = CommonPb.MapCity.newBuilder();
        builder.setPos(pos);
        builder.setCityId(city.getCityId());
        builder.setCamp(city.getCamp());
        builder.setProtectTime(city.getProtectTime());
        builder.setCurArm(city.getCurArm());
        builder.setMaxArm(city.getTotalArm());
        if (city.isProtect()) {
            builder.setFree(true);
        }
        if (city.getProduced() > 0) {
            builder.setProduced(city.getProduced());
        }
        if (city.getFinishTime() > 0) {
            builder.setFinishTime(city.getFinishTime());
        }
        if (city.getOwnerId() > 0) {
            builder.setOwnerId(city.getOwnerId());
            if (owner != null) {
                builder.setOwner(owner);
            }
        }
        if (city.isInCampagin()) {
            builder.setCampaignTime(city.getCampaignTime());
            builder.addAllRole(city.getCampaignList());
        }
        builder.setBattle(city.isInBattle());
        builder.setHasFirstKillReward(city.hasFirstKillReward(player.roleId));
        if (!CheckNull.isNullTrim(city.getName())) {
            builder.setCityName(city.getName());
        }
        if (!CheckNull.isEmpty(atkCamp)) {
            builder.addAllAtkCamp(atkCamp);
        }
        return builder.build();
    }

    public static MapLine createMapLinePb(March march, int battleType) {
        CommonPb.MapLine.Builder builder = CommonPb.MapLine.newBuilder();
        builder.setStartPos(march.getStartPos());
        builder.setEndPos(march.getTargetPos());
        builder.setNick(march.getPlayer().lord.getNick());
        builder.setStartTime(march.getStartTime());
        builder.setEndTime(march.getEndTime());
        builder.setCamp(march.getCamp());
        builder.setKeyId(march.getKeyId());
        builder.setArmyState(march.getArmy().getState());
        builder.setArmyType(march.getArmy().getType());
        if (!CheckNull.isEmpty(march.getArmy().getHero())) {
            builder.addAllHeroId(march.getArmy().getHero().stream().map(ti -> ti.getV1()).collect(Collectors.toList()));
        }
        builder.setBattleType(battleType);
        builder.setLordId(march.getPlayer().roleId);
        builder.setMarchLineId(march.getArmy().getMarchLineId());
        return builder.build();
    }

    public static CommonPb.RptMan createRptMan(int pos, String name, int vip, int lv) {
        CommonPb.RptMan.Builder builder = CommonPb.RptMan.newBuilder();
        builder.setPos(pos);
        builder.setName(name);
        builder.setVip(vip);
        builder.setLv(lv);
        return builder.build();
    }

    public static CommonPb.RptMan createRptMan(int pos, String name, int vip, int lv, long roleId) {
        CommonPb.RptMan.Builder builder = CommonPb.RptMan.newBuilder();
        builder.setPos(pos);
        builder.setName(name);
        builder.setVip(vip);
        builder.setLv(lv);
        builder.setRoleId(roleId);
        return builder.build();
    }

    public static CommonPb.RptBandit createRptBandit(int banditId, int pos) {
        CommonPb.RptBandit.Builder builder = CommonPb.RptBandit.newBuilder();
        builder.setBanditId(banditId);
        builder.setPos(pos);
        return builder.build();
    }

    public static CommonPb.RptCity createRptCityPb(int cityId, int pos) {
        CommonPb.RptCity.Builder builder = CommonPb.RptCity.newBuilder();
        builder.setCityId(cityId);
        builder.setPos(pos);
        return builder.build();
    }

    public static CommonPb.RptSummary createRptSummary(int total, int lost, int camp, String name, int portrait, int portraitFrame) {
        CommonPb.RptSummary.Builder builder = CommonPb.RptSummary.newBuilder();
        builder.setTotal(total);
        builder.setLost(lost);
        if (camp >= 0) {
            builder.setCamp(camp);
        }
        if (null != name) {
            builder.setName(name);
            builder.setPortrait(portrait);
            builder.setPortraitFrame(portraitFrame);
        }
        return builder.build();
    }

    public static CommonPb.RptOther createRptOtherPb(int type, int id, int pos, int camp, String extParam) {
        CommonPb.RptOther.Builder builder = CommonPb.RptOther.newBuilder();
        builder.setType(type);
        builder.setId(id);
        builder.setPos(pos);
        builder.setCamp(camp);
        if (extParam != null) {
            builder.setExtParam(extParam);
        }
        return builder.build();
    }

    public static CommonPb.RptHero createRptHero(int type, int kill, int award, int heroId, String owner, int lv,
                                                 int addExp, int lost, Hero hero) {
        CommonPb.RptHero.Builder builder = CommonPb.RptHero.newBuilder();
        builder.setType(type);
        builder.setKill(kill);
        builder.setLost(lost);
        builder.setAward(award);
        if (heroId > 0) {
            builder.setHeroId(heroId);
        }
        if (null != owner) {
            builder.setOwner(owner);
        }
        if (lv > 0) {
            builder.setLv(lv);
        }
        if (addExp > 0) {
            builder.setExp(addExp);
        }
        builder.setHeroDecorated(CheckNull.isNull(hero) ? 0 : hero.getDecorated());
        if (Objects.nonNull(hero)) {
            builder.setGradeKeyId(hero.getGradeKeyId());
        }
        return builder.build();
    }

    public static CommonPb.RptHero createRptHero(int type, int kill, int award, Object hero, String owner, int lv,
                                                 int addExp, int lost) {
        if (hero instanceof Integer) {
            Integer heroId = (Integer) hero;
            return createRptHero(type, kill, award, CheckNull.isNull(heroId) ? 0 : heroId, owner, lv, addExp, lost, null);
        } else {
            Hero hero_ = (Hero) hero;
            return createRptHero(type, kill, award, CheckNull.isNull(hero_) ? 0 : hero_.getHeroId(), owner, lv, addExp, lost, hero_);
        }
    }

    public static CommonPb.Mail saveMailPb(Mail mail, Player player) {
        CommonPb.Mail.Builder builder = CommonPb.Mail.newBuilder();
        builder.setKeyId(mail.getKeyId());
        builder.setType(mail.getType());
        builder.setState(mail.getState());
        builder.setTime(mail.getTime());
        builder.setMoldId(mail.getMoldId());
        builder.setLv(mail.getLv());
        builder.setVipLv(mail.getVipLv());

        if (mail.getTitle() != null) builder.setTitle(mail.getTitle());

        if (mail.getSendName() != null) builder.setSendName(mail.getSendName());

        if (mail.getContent() != null) builder.setContent(mail.getContent());

        if (mail.getToName() != null) builder.addAllToName(mail.getToName());

        if (mail.getRewardList() != null) builder.addAllAward(mail.getRewardList());

        if (mail.gettParam() != null) {
            builder.addAllTParam(mail.gettParam());
        }
        if (mail.getcParam() != null) {
            builder.addAllCParam(mail.getcParam());
        }
        if (mail.getDropList() != null) {
            builder.addAllDrop(mail.getDropList());
        }
        if (!CheckNull.isEmpty(mail.getRecoverList())) {
            builder.addAllRecover(mail.getRecoverList());
        }
        if (mail.getCollect() != null) builder.setCollect(mail.getCollect());
        if (mail.getScout() != null) builder.setScout(mail.getScout());
        if (mail.getEnrollMailInfo() != null) {
            builder.setEnrollInfo(mail.getEnrollMailInfo());
        }
        if (mail.getRoundOverMailInfo() != null) {
            builder.setRoundOverInfo(mail.getRoundOverMailInfo());
        }
        builder.setReportStatus(mail.getReportStatus());
        builder.setIsCross(mail.isCross());
        builder.setOriginator(mail.getOriginator());
        return builder.build();
    }

    public static CommonPb.Mail createMailPb(Mail mail, Player player) {
        CommonPb.Mail.Builder builder = CommonPb.Mail.newBuilder();
        builder.setKeyId(mail.getKeyId());
        builder.setType(mail.getType());
        builder.setState(mail.getState());
        builder.setTime(mail.getTime());
        builder.setMoldId(mail.getMoldId());
        builder.setLock(mail.getLock());
        builder.setLv(mail.getLv());
        builder.setVipLv(mail.getVipLv());

        if (mail.getTitle() != null) builder.setTitle(mail.getTitle());

        if (mail.getSendName() != null) builder.setSendName(mail.getSendName());

        if (mail.getContent() != null) builder.setContent(mail.getContent());

        if (mail.getToName() != null) builder.addAllToName(mail.getToName());

        if (mail.getRewardList() != null) builder.addAllAward(mail.getRewardList());

        Report mailReport = player.getMailReport(mail);
        if (Objects.nonNull(mailReport)) {
            builder.setReport(mailReport);
        }

        if (mail.gettParam() != null) {
            builder.addAllTParam(mail.gettParam());
        }
        if (mail.getcParam() != null) {
            builder.addAllCParam(mail.getcParam());
        }
        if (mail.getDropList() != null) {
            builder.addAllDrop(mail.getDropList());
        }
        if (!CheckNull.isEmpty(mail.getRecoverList())) {
            builder.addAllRecover(mail.getRecoverList());
        }
        if (mail.getCollect() != null) builder.setCollect(mail.getCollect());
        if (mail.getScout() != null) builder.setScout(mail.getScout());
        if (mail.getEnrollMailInfo() != null) {
            builder.setEnrollInfo(mail.getEnrollMailInfo());
        }
        if (mail.getRoundOverMailInfo() != null) {
            builder.setRoundOverInfo(mail.getRoundOverMailInfo());
        }
        builder.setReportStatus(mail.getReportStatus());
        builder.setIsCross(mail.isCross());
        builder.setOriginator(mail.getOriginator());
        return builder.build();
    }

    public static CommonPb.MailCollect createMailCollectPb(int time, Hero hero, int exp, List<Award> grab,
                                                           boolean effect) {
        CommonPb.MailCollect.Builder builder = CommonPb.MailCollect.newBuilder();
        builder.setTime(time);
        builder.setHeroId(hero.getHeroId());
        builder.setHeroLv(hero.getLevel());
        builder.setHeroDecorated(hero.getDecorated());
        builder.setExp(exp);
        builder.addAllGrab(grab);
        builder.setEffect(effect);
        return builder.build();
    }

    public static CommonPb.ScoutRes createScoutResPb(long food, long elec, long oil, long ore, long human,
                                                     List<TwoInt> canPlunderList) {
        CommonPb.ScoutRes.Builder builder = CommonPb.ScoutRes.newBuilder();
        builder.setFood(food);
        builder.setEle(elec);
        builder.setOil(oil);
        builder.setOre(ore);
        builder.setHuman(human);
        builder.addAllCanPlunder(canPlunderList);
        return builder.build();
    }

    public static CommonPb.ScoutCity createScoutCityPb(int wall, long fight, int arm1, int arm2, int arm3) {
        CommonPb.ScoutCity.Builder builder = CommonPb.ScoutCity.newBuilder();
        builder.setWall(wall);
        builder.setFight(fight);
        builder.setArm1(arm1);
        builder.setArm2(arm2);
        builder.setArm3(arm3);
        return builder.build();
    }

    public static CommonPb.ScoutHero createScoutHeroPb(Hero hero, int source, int state, Player player) {
        CommonPb.ScoutHero.Builder builder = CommonPb.ScoutHero.newBuilder();
        builder.setHeroId(hero.getHeroId());
        builder.setLevel(hero.getLevel());
        builder.setCount(hero.getCount());
        builder.setHeroDecorated(hero.getDecorated());
        builder.setSource(source);
        builder.setState(state);
        List<Integer> warPlanes = hero.getWarPlanes();
        if (!CheckNull.isEmpty(warPlanes)) {
            warPlanes.forEach(planeId -> {
                StaticPlaneUpgrade sPlaneUpgrade = StaticWarPlaneDataMgr.getPlaneUpgradeById(planeId);
                if (!CheckNull.isNull(sPlaneUpgrade)) {
                    WarPlane warPlane = player.warPlanes.get(sPlaneUpgrade.getPlaneType());
                    if (!CheckNull.isNull(warPlane)) {
                        builder.addPlanes(createTwoIntPb(warPlane.getPlaneId(), warPlane.getLevel()));
                        builder.addPlanePos(createTwoIntPb(warPlane.getBattlePos(), warPlane.getPlaneId()));
                    }
                }

            });
        }
        return builder.build();
    }

    public static CommonPb.MailScout createMailScoutPb(CommonPb.ScoutRes res, CommonPb.ScoutCity city,
                                                       List<CommonPb.ScoutHero> heros) {
        CommonPb.MailScout.Builder builder = CommonPb.MailScout.newBuilder();
        if (null != res) {
            builder.setRes(res);
        }
        if (null != city) {
            builder.setCity(city);
        }
        if (null != heros) {
            builder.addAllHero(heros);
        }
        return builder.build();
    }

    public static MailShow createMailShowPb(Mail mail) {
        CommonPb.MailShow.Builder builder = CommonPb.MailShow.newBuilder();
        builder.setKeyId(mail.getKeyId());
        builder.setType(mail.getType());
        builder.setState(mail.getState());
        builder.setTime(mail.getTime());
        builder.setMoldId(mail.getMoldId());
        builder.setLock(mail.getLock());
        if (!CheckNull.isEmpty(mail.getDropList())) {
            builder.addAllDrop(mail.getDropList());
        }
        if (!CheckNull.isEmpty(mail.getRecoverList())) {
            builder.addAllRecover(mail.getRecoverList());
        }
        if (mail.getTitle() != null) builder.setTitle(mail.getTitle());

        if (mail.getSendName() != null) builder.setSendName(mail.getSendName());

        if (mail.gettParam() != null) {
            builder.addAllParam(mail.gettParam());
        }
        if (mail.getcParam() != null) {
            builder.addAllParam(mail.getcParam());
        }
        builder.setIsCross(mail.isCross());
        builder.setOriginator(mail.getOriginator());
        return builder.build();
    }

    public static CommonPb.Combat createCombatPb(Combat combat) {
        CommonPb.Combat.Builder builder = CommonPb.Combat.newBuilder();
        builder.setCombatId(combat.getCombatId());
        builder.setStar(combat.getStar());
        return builder.build();
    }

    public static CommonPb.CombatFB createCombatFBPb(CombatFb combat) {
        CommonPb.CombatFB.Builder builder = CommonPb.CombatFB.newBuilder();
        builder.setCombatId(combat.getCombatId());
        builder.setCnt(combat.getCnt());
        builder.setEndTime(combat.getEndTime());
        builder.setGain(combat.getGain());
        builder.setStatus(combat.getStatus());
        builder.setBuyCnt(combat.getBuyCnt());
        return builder.build();
    }

    public static CommonPb.StoneCombat createStoneCombatPb(StoneCombat combat) {
        CommonPb.StoneCombat.Builder builder = CommonPb.StoneCombat.newBuilder();
        builder.setCombatId(combat.getCombatId());
        builder.setPassCnt(combat.getPassCnt());
        return builder.build();
    }

    public static CommonPb.Tech createTechLv(TechLv techLv) {
        CommonPb.Tech.Builder builder = CommonPb.Tech.newBuilder();
        builder.setId(techLv.getId());
        builder.setLv(techLv.getLv());
        builder.setExp(techLv.getStep());
        return builder.build();
    }

    public static CommonPb.TechQue createTechQue(TechQue techQue) {
        CommonPb.TechQue.Builder builder = CommonPb.TechQue.newBuilder();
        builder.setTechId(techQue.getId());
        builder.setTechEndTime(techQue.getEndTime());
        builder.setFreeSpeedCnt(techQue.getFreeCnt());
        builder.setFreeTime(techQue.getFreeTime());
        builder.setFreeOtherCnt(techQue.getFreeOtherCnt());
        builder.setParam(techQue.getParam());
        return builder.build();
    }

    public static CommonPb.Resource createCombatPb(Resource resource) {
        CommonPb.Resource.Builder builder = CommonPb.Resource.newBuilder();
        builder.setEle(resource.getElec());
        builder.setFood(resource.getFood());
        builder.setOil(resource.getOil());
        builder.setOre(resource.getOre());
        return builder.build();
    }

    public static CommonPb.Resource createResourcePb(Resource resource) {
        CommonPb.Resource.Builder builder = CommonPb.Resource.newBuilder();
        builder.setEle(resource.getElec());
        builder.setFood(resource.getFood());
        builder.setOil(resource.getOil());
        builder.setOre(resource.getOre());
        return builder.build();
    }

    public static CommonPb.Battle createBattlePb(Battle battle) {
        return createBattlePb(battle, battle.getDefArm());
    }

    public static CommonPb.Battle createBattlePb(Battle battle, int defArm, int atkHelpCnt, int defHelpCnt,
                                                 long defLordId) {
        CommonPb.Battle.Builder builder = CommonPb.Battle.newBuilder();
        builder.setBattleId(battle.getBattleId());
        builder.setAttack(battle.getAtkArm());
        builder.setDefend(defArm);
        builder.setBattleTime(battle.getBattleTime());
        builder.setAtkCamp(battle.getAtkCamp());
        builder.setDefCamp(battle.getDefCamp());
        builder.setAtkPos(battle.getAtkPos());
        builder.setDefPos(battle.getPos());
        builder.setAtkName(battle.getAtkName());
        builder.setBattleType(battle.getBattleType());
        builder.setAtkHelpChatCnt(atkHelpCnt);
        builder.setDefHelpChatCnt(defHelpCnt);
        builder.setAtkCityLv(battle.getAtkCityLv());
        builder.setDefCityLv(battle.getDefCityLv());
        builder.setAtkLordId(battle.getAtkLordId());
        builder.setDefLordId(defLordId);
        if (battle.getDefName() != null) {
            builder.setDefName(battle.getDefName());
        }
        builder.setType(battle.getType());
        builder.setAtkCastleSkin(battle.getAtkCastleSkin());
        builder.setDefCastleSkin(battle.getDefCastleSkin());
        builder.setAtkCastleSkinStar(battle.getAtkCastleSkinStar());
        builder.setDefCastleSkinStar(battle.getDefCastleSkinStar());
        builder.setAtkPortrait(battle.getAtkPortrait());
        builder.setDefPortrait(battle.getDefPortrait());
        builder.addAllInvites(battle.getInvites());
        builder.setBeginTime(battle.getBeginTime());
        builder.setAtkPortraitFrame(battle.getAtkPortraitFrame());
        builder.setDefPortraitFrame(battle.getDefPortraitFrame());
        return builder.build();
    }

    public static CommonPb.Battle createBattlePb(Battle battle, int defArm) {
        return createBattlePb(battle, defArm, battle.getAtkHelpChatCnt(), battle.getDefHelpChatCnt(),
                battle.getDefLordId());
    }

    public static CommonPb.CampBattle createCampBattlePb(Battle battle, int cityId) {
        CommonPb.CampBattle.Builder builder = CommonPb.CampBattle.newBuilder();
        builder.setCityId(cityId);
        builder.setAtkCamp(battle.getAtkCamp());
        builder.setDefCamp(battle.getDefCamp());
        builder.setBattleTime(battle.getBattleTime());
        return builder.build();
    }

    public static CommonPb.BattleRole createBattleRolePb(long roleId, int keyId, List<Integer> heroIdList) {
        CommonPb.BattleRole.Builder builder = CommonPb.BattleRole.newBuilder();
        builder.setRoleId(roleId);
        builder.setKeyId(keyId);
        if (null != heroIdList) {
            builder.addAllHeroId(heroIdList);
        }
        return builder.build();
    }

    public static CommonPb.BattlePO createBattlePOPb(Battle battle) {
        CommonPb.BattlePO.Builder builder = CommonPb.BattlePO.newBuilder();
        builder.setBattleId(battle.getBattleId());
        builder.setPos(battle.getPos());
        builder.setType(battle.getType());
        builder.setAtkCamp(battle.getAtkCamp());
        builder.setAtkCity(battle.getAtkCity());
        builder.setBattleTime(battle.getBattleTime());
        builder.setAtkPos(battle.getAtkPos());
        builder.setBattleType(battle.getBattleType());
        builder.setSponsorId(battle.getSponsor() != null ? battle.getSponsor().roleId : 0);
        builder.addAllAtkRoles(battle.getAtkRoles());
        builder.addAllDefRoles(battle.getDefRoles());
        if (null != battle.getDefencer()) {
            builder.setDefencerId(battle.getDefencer().roleId);
        }
        builder.addAllAtkRole(battle.getAtkList());
        builder.addAllDefRole(battle.getDefList());
        builder.setAtkArm(battle.getAtkArm());
        builder.setDefArm(battle.getDefArm());
        builder.addAllInvites(battle.getInvites());
        builder.setBeginTime(battle.getBeginTime());
        builder.setDefCamp(battle.getDefCamp());
        return builder.build();
    }

    public static CommonPb.SuperEquip createSuperEquipPb(SuperEquip equip) {
        CommonPb.SuperEquip.Builder builder = CommonPb.SuperEquip.newBuilder();
        builder.setType(equip.getType());
        builder.setLv(equip.getLv());
        builder.setStep(equip.getStep());
        builder.setBomb(equip.getBomb());
        builder.setGrowLv(equip.getGrowLv());
        return builder.build();
    }

    public static CommonPb.WallNpc createWallNpcPb(WallNpc wallNpc) {
        CommonPb.WallNpc.Builder builder = CommonPb.WallNpc.newBuilder();
        builder.setPos(wallNpc.getId());
        builder.setCount(wallNpc.getCount());
        builder.setExp(wallNpc.getExp());
        builder.setHeroId(wallNpc.getHeroNpcId());
        builder.setLevel(wallNpc.getLevel());
        builder.setAutoArmy(wallNpc.getAutoArmy());
        builder.setAddTime(wallNpc.getAddTime());
        return builder.build();
    }

    public static CommonPb.WallHero createWallHeroPb(Army army, int level, String name, int endTime, int armyCnt) {
        CommonPb.WallHero.Builder builder = CommonPb.WallHero.newBuilder();
        builder.setKeyId(army.getKeyId());
        builder.setLordId(army.getLordId());
        builder.setHeroId(army.getHero().get(0).getV1());
        builder.setLevel(level);
        builder.setCount(armyCnt);
        builder.setName(name);
        builder.setEndTime(endTime);
        return builder.build();
    }

    public static CommonPb.RankData createRankDataPb(String nick, int level, long fight, int area, int camp, int icon,
                                                     int rank, long roleId, int portraitFrame, String... strs) {
        CommonPb.RankData.Builder builder = CommonPb.RankData.newBuilder();
        builder.setName(nick);
        builder.setLv(level);
        builder.setValue(fight);
        builder.setArea(area);
        builder.setCamp(camp);
        builder.setIcon(icon);
        builder.setPortraitFrame(portraitFrame);
        builder.setRank(rank);
        builder.setRoleId(roleId);
        if (strs.length > 0) {
            for (String str : strs) {
                builder.addParam(str);
            }
        }
        return builder.build();
    }

    public static CommonPb.RankData createRankDataPb(Lord lord, int value, int award, int portraitFrame, String... strs) {
        CommonPb.RankData.Builder builder = CommonPb.RankData.newBuilder();
        builder.setName(lord.getNick());
        builder.setLv(lord.getLevel());
        builder.setValue(value);
        builder.setArea(award);
        builder.setCamp(lord.getCamp());
        builder.setIcon(lord.getPortrait());
        builder.setPortraitFrame(portraitFrame);
        builder.setRank(lord.getRanks());
        builder.setRoleId(lord.getLordId());
        if (strs.length > 0) {
            for (String str : strs) {
                builder.addParam(str);
            }
        }
        return builder.build();
    }

    public static TypeAwards createTypeAwardsPb(int key, List<Award> value) {
        CommonPb.TypeAwards.Builder builder = CommonPb.TypeAwards.newBuilder();
        builder.setType(key);
        builder.addAllReward(value);
        return builder.build();
    }

    static public CommonPb.Activity createActivityPb(ActivityBase activityBase, boolean open, int tips) {
        CommonPb.Activity.Builder builder = CommonPb.Activity.newBuilder();
        builder.setActivityId(activityBase.getActivityId());
        builder.setName(activityBase.getPlan().getName());
        builder.setBeginTime((int) (activityBase.getBeginTime().getTime() / 1000));
        builder.setEndTime((int) (activityBase.getEndTime().getTime() / 1000));
        if (activityBase.getDisplayTime() != null) {
            builder.setDisplayTime((int) (activityBase.getDisplayTime().getTime() / 1000));
        }
        if (activityBase.getAwardBeginTime() != null
                && StaticActivityDataMgr.isActTypeRank(activityBase.getActivityType())) {// 只有排行活动发此致
            builder.setAwardTime((int) (activityBase.getAwardBeginTime().getTime() / 1000));
        }
        builder.setOpen(open);
        builder.setTips(tips);
        builder.setType(activityBase.getActivityType());
        return builder.build();
    }

    public static ActivityCond createActivityCondPb(StaticPromotion promotion, int status, int state) {
        CommonPb.ActivityCond.Builder builder = CommonPb.ActivityCond.newBuilder();
        builder.setKeyId(promotion.getPromotionId());
        builder.setCond(promotion.getCount());
        builder.setStatus(status);
        builder.setState(state);
        List<List<Integer>> awardList = promotion.getList();
        for (List<Integer> e : awardList) {
            if (e.size() != 3) {
                continue;
            }
            int type = e.get(0);
            int id = e.get(1);
            int count = e.get(2);
            builder.addAward(PbHelper.createAwardPb(type, id, count));
        }
        return builder.build();
    }

    public static ActivityCond createActivityCondPb(StaticActHotProduct sahp, int status, int state) {
        CommonPb.ActivityCond.Builder builder = CommonPb.ActivityCond.newBuilder();
        builder.setKeyId(sahp.getKeyId());
        builder.setParam(String.valueOf(sahp.getTab()));
        if (sahp.getTab() == ActivityConst.ActHotProduct.STATUS_SPEND_SUM) {
            builder.setCond(sahp.getSpend());
        } else if (sahp.getTab() == ActivityConst.ActHotProduct.STATUS_BUY_COUNT) {
            builder.setCond(sahp.getTime());
        }
        builder.setStatus(status);
        builder.setState(state);
        sahp.getAwardList().forEach(sa -> {
            int type = sa.get(0);
            int id = sa.get(1);
            int count = sa.get(2);
            builder.addAward(PbHelper.createAwardPb(type, id, count));
        });
        return builder.build();
    }

    public static ActivityCond createActivityCondPb(StaticEasterAward sea, int status, int state) {
        CommonPb.ActivityCond.Builder builder = CommonPb.ActivityCond.newBuilder();
        builder.setKeyId(sea.getKeyId());
        builder.setCond(sea.getRecharge());
        builder.setStatus(status);
        builder.setState(state);
        Optional.ofNullable(sea.getParam())
                .ifPresent(params -> {
                    if (!CheckNull.isEmpty(params)) {
                        builder.setParam(String.valueOf(params.get(0)));
                    }
                });
        sea.getAwardList().forEach(sa -> {
            int type = sa.get(0);
            int id = sa.get(1);
            int count = sa.get(2);
            builder.addAward(PbHelper.createAwardPb(type, id, count));
        });
        return builder.build();
    }

    public static CommonPb.ActivityCond createActivityCondPb(StaticActAward actAward, int status, int state) {
        CommonPb.ActivityCond.Builder builder = CommonPb.ActivityCond.newBuilder();
        builder.setKeyId(actAward.getKeyId());
        builder.setCond(actAward.getCond());
        builder.setStatus(status);
        builder.setState(state);
        if (actAward.getParam() != null && !actAward.getParam().equals("")) {
            builder.setParam(Arrays.toString(actAward.getParam().toArray()));
        }
        List<List<Integer>> awardList = actAward.getAwardList();
        for (List<Integer> e : awardList) {
            if (e.size() != 3) {
                continue;
            }
            int type = e.get(0);
            int id = e.get(1);
            int count = e.get(2);
            builder.addAward(PbHelper.createAwardPb(type, id, count));
        }
        return builder.build();
    }

    public static CommonPb.ActivityCond createActivityCondPb(StaticActExchange actExchange, int status, int state, String... par) {
        CommonPb.ActivityCond.Builder builder = CommonPb.ActivityCond.newBuilder();
        builder.setKeyId(actExchange.getKeyId());
        List<Integer> prop = actExchange.getProp();
        if (!CheckNull.isEmpty(prop)) {
            builder.setCond(prop.get(2));
        }
        builder.setStatus(status);
        builder.setState(state);
        StringBuffer param = new StringBuffer();
        param.append(actExchange.getLvLimit()).append(",").append(actExchange.getNumberLimit());
        builder.setParam(param.toString());
        List<List<Integer>> awardList = actExchange.getAwardList();
        for (List<Integer> e : awardList) {
            if (e.size() != 3) {
                continue;
            }
            int type = e.get(0);
            int id = e.get(1);
            int count = e.get(2);
            builder.addAward(PbHelper.createAwardPb(type, id, count));
        }
        return builder.build();
    }

    public static CommonPb.ActivityCond createActivityCondPbByPayTurnplate(StaticActPayTurnplate sapt, int status) {
        CommonPb.ActivityCond.Builder builder = CommonPb.ActivityCond.newBuilder();
        builder.setKeyId(sapt.getId());
        builder.setStatus(status);
        builder.setParam(String.valueOf(sapt.getBetterAward()));
        List<Integer> award = sapt.getAward();
        int type = award.get(0);
        int id = award.get(1);
        int count = award.get(2);
        builder.addAward(PbHelper.createAwardPb(type, id, count));

        return builder.build();
    }

    /**
     * @Title: createActivityCondPbByOreTurnplate @Description: 矿石转盘 @param sapt @return 参数 CommonPb.ActivityCond
     * 返回类型 @throws
     */
    public static CommonPb.ActivityCond createActivityCondPbByOreTurnplate(StaticActOreTurnplate sapt) {
        CommonPb.ActivityCond.Builder builder = CommonPb.ActivityCond.newBuilder();
        builder.setKeyId(sapt.getId());
        builder.setParam(String.valueOf(sapt.getBetterAward()));
        List<Integer> award = sapt.getAward();
        int type = award.get(0);
        int id = award.get(1);
        int count = award.get(2);
        builder.addAward(PbHelper.createAwardPb(type, id, count));
        return builder.build();
    }

    /**
     * 创建好运道活动, 完成值pb对象
     *
     * @param saa         活动奖励
     * @param sActVoucher 代币转换
     * @param status      0
     * @param state       randomType
     * @return 活动完成值pb
     */
    public static ActivityCond createActGoodLuck(StaticActAward saa, StaticActVoucher sActVoucher, int status, int state) {
        CommonPb.ActivityCond.Builder builder = CommonPb.ActivityCond.newBuilder();
        builder.setKeyId(saa.getKeyId());
        if (!CheckNull.isEmpty(saa.getParam())) {
            builder.setParam(Arrays.toString(saa.getParam().toArray()));
        }
        builder.setCond(saa.getCond());
        builder.setStatus(status);
        builder.setState(state);
        List<List<Integer>> awardList = saa.getAwardList();
        for (List<Integer> e : awardList) {
            if (e.size() < 3) {
                continue;
            }
            int type = e.get(0);
            int id = e.get(1);
            int count = e.get(2);
            // 配置了才有
            int weight = e.size() == 4 ? e.get(3) : 0;
            builder.addAward(PbHelper.createAwardPbWithParam(type, id, count, 0, weight));
        }
        return builder.build();
    }

    public static CommonPb.Day7Act createDay7ActPb(int keyId, int status, int recved) {
        CommonPb.Day7Act.Builder builder = CommonPb.Day7Act.newBuilder();
        builder.setKeyId(keyId);
        builder.setStatus(status);
        builder.setRecved(recved);
        return builder.build();
    }

    public static CommonPb.DbFriend createDbFriendPb(DbFriend dbFriend) {
        CommonPb.DbFriend.Builder builder = CommonPb.DbFriend.newBuilder();
        builder.setAddTime(dbFriend.getAddTime());
        builder.setLordId(dbFriend.getLordId());
        builder.setState(dbFriend.getState());
        return builder.build();
    }

    public static CommonPb.Friend createFriendPb(Man man, DbFriend dbFriend) {
        CommonPb.Friend.Builder builder = CommonPb.Friend.newBuilder();
        builder.setMan(man);
        builder.setAddTime(dbFriend.getAddTime());
        builder.setState(dbFriend.getState());
        return builder.build();
    }

    public static CommonPb.Friend createFriendAndHeroPb(Man man, List<FriendHero> heros, DbFriend dbFriend) {
        CommonPb.Friend.Builder builder = CommonPb.Friend.newBuilder();
        builder.setMan(man);
        builder.setAddTime(dbFriend.getAddTime());
        builder.setState(dbFriend.getState());
        builder.addAllHero(heros);
        return builder.build();
    }

    public static CommonPb.FriendHero createFriendAndHeroPb(Hero hero, Player player) {
        CommonPb.FriendHero.Builder builder = CommonPb.FriendHero.newBuilder();
        builder.setHeroId(hero.getHeroId());
        builder.setLevel(hero.getLevel());
        builder.setExp(hero.getExp());
        if (hero.isOnBattle()) {
            builder.setCount(hero.getCount());
            builder.setPos(hero.getPos());
        }
        List<Integer> warPlanes = hero.getWarPlanes();
        if (!CheckNull.isEmpty(warPlanes)) {
            for (int planeId : warPlanes) {
                try {
                    WarPlane warPlane = player.checkWarPlaneIsExist(planeId);
                    if (!CheckNull.isNull(warPlane)) {
                        builder.addPlanes(createTwoIntPb(planeId, warPlane.getLevel()));
                        builder.addPlanePos(createTwoIntPb(warPlane.getBattlePos(), planeId));
                    }
                } catch (MwException e) {
                    LogUtil.error(e);
                }
            }
        }
        builder.setDecorated(hero.getDecorated());
        return builder.build();
    }

    public static CommonPb.DbMasterApprentice createDbMasterApprenticePb(DbMasterApprentice masterApprentice) {
        CommonPb.DbMasterApprentice.Builder builder = CommonPb.DbMasterApprentice.newBuilder();
        builder.setLordId(masterApprentice.getLordId());
        builder.setCreateTime(masterApprentice.getCreateTime());
        builder.setRelation(masterApprentice.getRelation());
        builder.setStaus(masterApprentice.getStaus());
        return builder.build();
    }

    public static CommonPb.MasterApprentice createMasterApprenticePb(Man man, DbMasterApprentice masterApprentice, Player player) {
        CommonPb.MasterApprentice.Builder builder = CommonPb.MasterApprentice.newBuilder();
        builder.setMan(man);
        builder.setCreateTime(masterApprentice.getCreateTime());
        builder.setRelation(masterApprentice.getRelation());
        builder.setStaus(masterApprentice.getStaus());
        if (!player.isLogin) {
            builder.setOfflineTime(player.lord.getOffTime());
        }
        return builder.build();
    }

    public static CommonPb.Man createManPbByLord(Player player) {
        Lord lord = player.lord;
        CommonPb.Man.Builder man = CommonPb.Man.newBuilder();
        man.setLordId(lord.getLordId());
        if (lord.getNick() != null) man.setNick(lord.getNick());
        man.setSex(lord.getSex());
        man.setIcon(lord.getPortrait());
        man.setPortraitFrame(player.getDressUp().getCurPortraitFrame());
        man.setLevel(lord.getLevel());
        man.setRanks(lord.getRanks());
        man.setFight(lord.getFight());
        man.setArea(lord.getArea());
        man.setCamp(lord.getCamp());
        man.setJob(lord.getJob());
        man.setFightNum(player.common.getKillNum());
        man.setVip(player.lord.getVip());
        if (!CheckNull.isNullTrim(lord.getSignature())) man.setSignature(lord.getSignature());
        Map<Integer, Integer> showFightInfo = player.getBattleHeroShowFightInfo();
        if (!CheckNull.isEmpty(showFightInfo)) {
            for (Entry<Integer, Integer> entry : showFightInfo.entrySet()) {
                Integer key = entry.getKey();
                key = CheckNull.isNull(key) ? 0 : key;
                Integer val = entry.getValue();
                val = CheckNull.isNull(val) ? 0 : val;
                man.addShowFight(PbHelper.createTwoIntPb(key, val));
            }
        }
        return man.build();
    }

    public static List<CommonPb.TwoInt> createTwoIntListByMap(Map<Integer, Integer> map) {
        List<CommonPb.TwoInt> list = new ArrayList<>();
        for (Entry<Integer, Integer> kv : map.entrySet()) {
            list.add(PbHelper.createTwoIntPb(kv.getKey(), kv.getValue()));
        }
        return list;
    }

    public static CommonPb.BlackhawkItem createBlackhawkItem(ActBlackhawkItem item) {
        CommonPb.BlackhawkItem.Builder builder = CommonPb.BlackhawkItem.newBuilder();
        builder.setKeyId(item.getKeyId());
        builder.setCond(item.getCond());
        builder.addAllAward(item.getAward());
        builder.setIsPurchased(item.isPurchased());
        builder.setPrice(item.getPrice());
        builder.setDiscount(item.getDiscount());
        builder.setDiscountPrice(item.getDiscountPrice());
        return builder.build();
    }

    public static Collection<CommonPb.BlackhawkItem> createBlackhawkItemList(Collection<ActBlackhawkItem> items) {
        List<CommonPb.BlackhawkItem> list = new ArrayList<>();
        for (ActBlackhawkItem item : items) {
            list.add(createBlackhawkItem(item));
        }
        return list;
    }

    public static CommonPb.CabinetLead createCabinetLeadPb(CabinetLead lead) {
        CommonPb.CabinetLead.Builder builder = CommonPb.CabinetLead.newBuilder();
        builder.setCabinetPlanId(lead.getCabinetPlanId());
        builder.setPos(lead.getPos());
        builder.setCamp(lead.getCamp());
        builder.setRoleId(lead.getRoleId());
        return builder.build();
    }

    public static CommonPb.Gestapo createGestapoPb(Gestapo gestapo) {
        CommonPb.Gestapo.Builder builder = CommonPb.Gestapo.newBuilder();
        builder.setGestapoId(gestapo.getGestapoId());
        builder.setPos(gestapo.getPos());
        builder.setEndTime(gestapo.getEndTime());
        builder.setRoleId(gestapo.getRoleId());
        builder.setStatus(gestapo.getStatus());
        return builder.build();
    }

    static public CommonPb.ActRank createActRank(ActRank actPlayerRank, String nick, int camp, int portrait, int portraitFrame) {
        return createActRank(actPlayerRank, -1, nick, camp, portrait, portraitFrame);
    }

    static public CommonPb.ActRank createActRank(ActRank actPlayerRank, int actType, String nick, int camp, int portrait, int portraitFrame) {
        CommonPb.ActRank.Builder builder = CommonPb.ActRank.newBuilder();
        builder.setLordId(actPlayerRank.getLordId());
        builder.setRankType(actType == -1 ? actPlayerRank.getRankType() : actType);
        builder.setRankValue(actPlayerRank.getRankValue());
        builder.setNick(nick);
        builder.setCamp(camp);
        builder.setRank(actPlayerRank.getRank());
        if (actPlayerRank.getParam() != null) {
            builder.setParam(actPlayerRank.getParam());
        }
        builder.setRankTime(actPlayerRank.getRankTime());
        builder.setPortrait(portrait);
        builder.setPortraitFrame(portraitFrame);
        return builder.build();
    }

    public static CommonPb.Summon createSummon(com.gryphpoem.game.zw.resource.domain.p.Summon summon) {
        CommonPb.Summon.Builder builder = CommonPb.Summon.newBuilder();
        builder.setCount(summon.getCount());
        builder.setLastTime(summon.getLastTime());
        builder.addAllRespondId(summon.getRespondId());
        builder.setStatus(summon.getStatus());
        builder.setSum(summon.getSum());
        return builder.build();
    }

    public static DbDay7Act createDbDay7ActPb(Day7Act day7Act) {
        DbDay7Act.Builder builder = DbDay7Act.newBuilder();
        builder.addAllRecvAwardIds(day7Act.getRecvAwardIds());
        for (Entry<Integer, Integer> e : day7Act.getStatus().entrySet()) {
            builder.addStatus(createTwoIntPb(e.getKey(), e.getValue()));
        }
        for (Entry<String, Integer> e : day7Act.getTankTypes().entrySet()) {
            builder.addTankTypes(createStrIntPb(e.getKey(), e.getValue()));
        }
        for (Entry<Integer, Map<Integer, Integer>> e : day7Act.getTypeCnt().entrySet()) {
            if (e.getValue() != null) {
                for (Entry<Integer, Integer> ee : e.getValue().entrySet()) {
                    builder.addTypeCnt(DbDay7ActStatus.newBuilder().setKey(e.getKey())
                            .addStatus(createTwoIntPb(ee.getKey(), ee.getValue())));
                }
            }
        }
        return builder.build();
    }

    static public DbActivity createDbActivityPb(Activity activity) {
        DbActivity.Builder builder = DbActivity.newBuilder();
        builder.setActivityId(activity.getActivityId());
        builder.setBeginTime(activity.getBeginTime());
        builder.setEndTime(activity.getEndTime());
        builder.setOpen(activity.getOpen());
        builder.setActivityType(activity.getActivityType());
        if (activity.getStatusCnt() != null) {
            for (Entry<Integer, Long> e : activity.getStatusCnt().entrySet())
                builder.addStatus(IntLong.newBuilder().setV1(e.getKey()).setV2(e.getValue()));
        }
        if (null != activity.getStatusMap()) {
            Iterator<Entry<Integer, Integer>> it = activity.getStatusMap().entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, Integer> next = it.next();
                int keyId = next.getKey();
                int value = next.getValue();
                builder.addTowInt(createTwoIntPb(keyId, value));
            }
        }
        if (null != activity.getPropMap()) {
            Iterator<Entry<Integer, Integer>> it = activity.getPropMap().entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, Integer> next = it.next();
                int keyId = next.getKey();
                int value = next.getValue();
                builder.addProp(createTwoIntPb(keyId, value));
            }
        }
        if (null != activity.getSaveMap()) {
            Iterator<Entry<Integer, Integer>> it = activity.getSaveMap().entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, Integer> next = it.next();
                int keyId = next.getKey();
                int value = next.getValue();
                builder.addSave(createTwoIntPb(keyId, value));
            }
        }
        if(!activity.getDataMap().isEmpty()){
            activity.getDataMap().entrySet().forEach(entry -> builder.addData(createTwoStr(entry.getKey(),entry.getValue())));
        }
        if (activity.getActivityType() == ActivityConst.ACT_LUCKY_TURNPLATE
                || activity.getActivityType() == ActivityConst.FAMOUS_GENERAL_TURNPLATE
                || activity.getActivityType() == ActivityConst.ACT_LUCKY_TURNPLATE_NEW
                || activity.getActivityType() == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR
                || activity.getActivityType() == ActivityConst.ACT_SEASON_TURNPLATE
                || activity.getActivityType() == ActivityConst.ACT_MAGIC_TREASURE_WARE) {
            ActTurnplat actTurnplat = (ActTurnplat) activity;
            builder.setTurnplat(actTurnplat.ser());
        } else if (activity.getActivityType() == ActivityConst.ACT_EQUIP_TURNPLATE) {
            EquipTurnplat actTurnplat = (EquipTurnplat) activity;
            builder.setEquipTurnplat(actTurnplat.ser());
        }

        if (CheckNull.nonEmpty(activity.getDayTasks())){
            activity.getDayTasks().forEach((key, value) -> builder.addSerDayTask(buildSerDayTask(key, value)));
        }
        if (CheckNull.nonEmpty(activity.getDayScore())){
            activity.getDayScore().forEach((key, value) -> builder.addSerDayScore(buildSerDayScore(key, value)));
        }

        //秋季拍卖活动
        if (activity.getActivityType() == ActivityConst.ACT_AUCTION) {
            Optional.ofNullable(activity.getActivityAuction()).
                    ifPresent(activityAuction -> builder.setActivityAuction(activityAuction.serialization()));
        }
        builder.setActKeyId(activity.getActivityKeyId());
        return builder.build();
    }

    private static SerializePb.SerDayScore buildSerDayScore(int day, Turple<Integer, Integer> turple) {
        SerializePb.SerDayScore.Builder builder = SerializePb.SerDayScore.newBuilder();
        builder.setDay(day);
        builder.setScore(createTwoIntPb(turple.getA() == null ? 0 : turple.getA(), turple.getB() == null ? 0 : turple.getB()));
        return builder.build();
    }

    private static SerializePb.SerDayTask buildSerDayTask(int day, List<ActivityTask> taskList) {
        SerializePb.SerDayTask.Builder builder = SerializePb.SerDayTask.newBuilder();
        builder.setDay(day);
        Optional.ofNullable(taskList).ifPresent(tmps -> tmps.forEach(o -> {
            SerializePb.SerActivityTask.Builder taskBuilder = SerializePb.SerActivityTask.newBuilder();
            taskBuilder.setTaskId(o.getTaskId());
            taskBuilder.setProgress(o.getProgress());
            taskBuilder.setCount(o.getCount());
            taskBuilder.setDrawCnt(o.getDrawCount());
            taskBuilder.setUid(o.getUid());
            builder.addTask(taskBuilder);
        }));
        return builder.build();
    }

    public static ChatDialog createChatDialogPb(Player my, Player target,
                                                com.gryphpoem.game.zw.resource.pojo.chat.ChatDialog d) {
        ChatDialog.Builder builder = ChatDialog.newBuilder();
        builder.setTargetId(target.roleId);
        builder.setPortrait(target.lord.getPortrait());
        builder.setPortraitFrame(target.getDressUp().getCurPortraitFrame());
        builder.setLv(target.lord.getLevel());
        builder.setVip(target.lord.getVip());
        builder.setJob(target.lord.getJob());
        builder.setArea(target.lord.getArea());
        builder.setNick(target.lord.getNick());
        builder.setCamp(target.lord.getCamp());
        if (d.getChat() != null) {
            builder.setIsSelf(d.getChat().getLordId() == my.roleId);
            builder.setMsg(d.getChat().getMsg());
            builder.setTime(d.getChat().getTime());
        }
        builder.setState(d.getState());
        return builder.build();
    }

    public static IntListInt createIntListInt(int v1, List<Integer> v2) {
        IntListInt.Builder builder = IntListInt.newBuilder();
        builder.setV1(v1);
        if (!CheckNull.isEmpty(v2)) {
            builder.addAllV2(v2);
        }
        return builder.build();
    }

    public static Collection<PayInfo> createPayInfo(List<StaticPay> staticPays) {
        List<PayInfo> list = new ArrayList<>();
        for (StaticPay staticPay : staticPays) {
            PayInfo.Builder builder = PayInfo.newBuilder();
            builder.setBanFlag(staticPay.getBanFlag());
            builder.setPayId(staticPay.getPayId());
            builder.setPrice(staticPay.getPrice());
            builder.setTopup(staticPay.getTopup());
            builder.setExtraGold(staticPay.getExtraGold());
            if (staticPay.getUsd() != 0.0) {
                builder.setUsd(staticPay.getUsd());
            }
            list.add(builder.build());
        }
        return list;
    }

    public static PayInfo createPayInfoBySpay(StaticPay staticPay) {
        PayInfo.Builder builder = PayInfo.newBuilder();
        builder.setBanFlag(staticPay.getBanFlag());
        builder.setPayId(staticPay.getPayId());
        builder.setPrice(staticPay.getPrice());
        builder.setTopup(staticPay.getTopup());
        builder.setExtraGold(staticPay.getExtraGold());
        if (staticPay.getUsd() != 0.0) {
            builder.setUsd(staticPay.getUsd());
        }
        return builder.build();
    }

    public static TriggerGiftInfo creteTriggerGiftsRs(StaticTriggerConf conf, TriggerGift triggerGift,
                                                      StaticActGiftpack triggerGiftConf, Player player) {
        TriggerGiftInfo.Builder builder = TriggerGiftInfo.newBuilder();
        builder.setTriggerId(conf.getTriggerId());
        builder.setGiftId(conf.getGiftId());
        builder.setCount(triggerGift.getCount());
        builder.setLastTime(triggerGift.getEndTime());
        builder.setRoleId(player.roleId);
        builder.setState(triggerGift.getState());
        StaticPay staticPay = StaticVipDataMgr.getStaticPayByPayId(triggerGift.getGiftId());
        if (staticPay != null) {
            builder.setPayinfo(createPayInfoBySpay(staticPay));
        }
        builder.setGold(triggerGiftConf.getGold());
        builder.setCut(triggerGiftConf.getCount());
        return builder.build();
    }

    public static List<SerializePb.DbTriggerGiftMap> createDbTriggerGiftMap(
            Map<Integer, Map<Integer, TriggerGift>> triggerGifts) {
        ArrayList<SerializePb.DbTriggerGiftMap> list = new ArrayList<>();
        for (Entry<Integer, Map<Integer, TriggerGift>> mapEntry : triggerGifts.entrySet()) {
            SerializePb.DbTriggerGiftMap.Builder builder = SerializePb.DbTriggerGiftMap.newBuilder();
            builder.setKey(mapEntry.getKey());
            List<TriggerGift> triggerGiftsList = new ArrayList<>(mapEntry.getValue().values());
            for (TriggerGift triggerGift : triggerGiftsList) {
                builder.addSerTriggerGift(PbHelper.createSerTriggerGift(triggerGift));
            }
            list.add(builder.build());
        }
        return list;
    }

    private static SerializePb.SerTriggerGift createSerTriggerGift(TriggerGift triggerGift) {
        SerializePb.SerTriggerGift.Builder builder = SerializePb.SerTriggerGift.newBuilder();
        builder.setKeyId(triggerGift.getKeyId());
        builder.setGiftId(triggerGift.getGiftId());
        builder.setBeginTime(triggerGift.getBeginTime());
        builder.setEndTime(triggerGift.getEndTime());
        builder.setCount(triggerGift.getCount());
        builder.setState(triggerGift.getState());
        builder.setStatus(triggerGift.getStatus());
        return builder.build();
    }

    public static GestapoBattle createGestapoBattlePb(Battle battle, Gestapo gestapo) {
        GestapoBattle.Builder builder = GestapoBattle.newBuilder();
        builder.setGestapo(gestapo.getGestapoId());
        builder.setAtkCamp(battle.getAtkCamp());
        builder.setBattleTime(battle.getBattleTime());
        return builder.build();
    }

    public static AtkCityActTask createAtkCityActTask(AtkCityAct cityAct, StaticAtkCityAct staticAtkCityAct,
                                                      int status) {
        AtkCityActTask.Builder builder = AtkCityActTask.newBuilder();
        builder.setKeyId(staticAtkCityAct.getKeyId());
        builder.setDay(staticAtkCityAct.getDay());
        builder.setCond(staticAtkCityAct.getNum());
        int state = cityAct.getStatusCnt().containsKey(staticAtkCityAct.getKeyId())
                ? cityAct.getStatusCnt().get(staticAtkCityAct.getKeyId()) : 0;
        builder.setState(state);
        builder.setActice(staticAtkCityAct.getPoint());
        builder.setParam(String.valueOf(status));
        return builder.build();
    }

    public static AtkCityActActive createAtkCityActActive(StaticActAward actAward, int recved) {
        AtkCityActActive.Builder builder = AtkCityActActive.newBuilder();
        builder.setKeyId(actAward.getKeyId());
        builder.setRecved(recved);
        List<List<Integer>> awardList = actAward.getAwardList();
        for (List<Integer> e : awardList) {
            if (e.size() != 3) {
                continue;
            }
            int type = e.get(0);
            int id = e.get(1);
            int count = e.get(2);
            builder.addAward(PbHelper.createAwardPb(type, id, count));
        }
        return builder.build();
    }

    public static SerializePb.DbAtkCityAct createAtkCityActDb(AtkCityAct atkCityAct) {
        SerializePb.DbAtkCityAct.Builder builder = SerializePb.DbAtkCityAct.newBuilder();
        for (Entry<String, Integer> e : atkCityAct.getTankTypes().entrySet()) {
            builder.addTankTypes(PbHelper.createStrIntPb(e.getKey(), e.getValue()));
        }
        for (Entry<Integer, Map<Integer, Integer>> e : atkCityAct.getTypeCnt().entrySet()) {
            if (e.getValue() != null) {
                for (Entry<Integer, Integer> ee : e.getValue().entrySet()) {
                    builder.addTypeCnt(DbDay7ActStatus.newBuilder().setKey(e.getKey())
                            .addStatus(createTwoIntPb(ee.getKey(), ee.getValue())));
                }
            }
        }
        for (Entry<Integer, Integer> e : atkCityAct.getStatus().entrySet()) {
            builder.addStatus(PbHelper.createTwoIntPb(e.getKey(), e.getValue()));
        }
        for (Entry<Integer, Integer> e : atkCityAct.getStatusCnt().entrySet()) {
            builder.addStatusCnt(PbHelper.createTwoIntPb(e.getKey(), e.getValue()));
        }
        builder.addAllCanRecvKeyId(atkCityAct.getCanRecvKeyId());
        return builder.build();
    }

    public static CommonPb.StoneHole createStoneHolePb(com.gryphpoem.game.zw.resource.pojo.StoneHole hole) {
        CommonPb.StoneHole.Builder bulider = CommonPb.StoneHole.newBuilder();
        bulider.setHoleIndex(hole.getHoleIndex());
        bulider.setStoneId(hole.getStoneId());
        bulider.setType(hole.getType());
        return bulider.build();
    }

    public static CommonPb.Stone createStonePb(com.gryphpoem.game.zw.resource.pojo.Stone s) {
        CommonPb.Stone.Builder bulider = CommonPb.Stone.newBuilder();
        bulider.setStoneId(s.getStoneId());
        bulider.setCnt(s.getCnt());
        return bulider.build();
    }

    public static SerializePb.SerLightningWarBoss createLightningWarBossPb(int area, LightningWarBoss boss) {
        SerializePb.SerLightningWarBoss.Builder builder = SerializePb.SerLightningWarBoss.newBuilder();
        builder.setId(area);
        builder.setCityId(boss.getId());
        builder.setPos(boss.getPos());
        if (!CheckNull.isNull(boss.getFighter())) {
            for (com.gryphpoem.game.zw.resource.pojo.fight.Force force : boss.getFighter().getForces()) {
                builder.addForce(
                        CommonPb.Force.newBuilder().setNpcId(force.id).setHp(force.hp).setCurLine(force.curLine));
            }
        }
        builder.setLastFightTime(boss.getLastFightTime());
        builder.setStatus(boss.getStatus());
        return builder.build();
    }

    public static GestapoCampRank createGestapoCampRankPb(int camp, int val, int time) {
        GestapoCampRank.Builder builder = GestapoCampRank.newBuilder();
        builder.setCamp(camp);
        builder.setVal(val);
        builder.setTime(time);
        return builder.build();
    }

    public static CommonPb.FemaleAgent createFemaleAgent(FemaleAgent fa) {
        CommonPb.FemaleAgent.Builder builder = CommonPb.FemaleAgent.newBuilder();
        builder.setId(fa.getId());
        builder.setQuality(fa.getQuality());
        builder.setAttrVal(fa.getAttrVal());
        builder.setSkillVal(fa.getSkillVal());
        builder.setExp(fa.getExp());
        builder.setStatus(fa.getStatus());
        builder.setAppointmentCnt(fa.getAppointmentCnt());
        builder.setStar(fa.getStar());
        builder.setDailyFree(fa.getDailyFree());
        return builder.build();
    }

    public static TurnplateInfo createTurnplateInfo(StaticTurnplateConf conf) {
        TurnplateInfo.Builder builder = TurnplateInfo.newBuilder();
        builder.setId(conf.getTurnplateId());
        builder.setCount(conf.getCount());
        builder.setPrice(conf.getPrice());
        builder.setPoint(conf.getPoint());
        builder.setTitle(conf.getBg());
        return builder.build();
    }

    /**
     * @param conf
     * @return EquipTurnplateInfo
     * @Title: createEquipTurnplateInfo
     * @Description: 装备转盘
     */
    public static EquipTurnplateInfo createEquipTurnplateInfo(StaticEquipTurnplateConf conf) {
        EquipTurnplateInfo.Builder builder = EquipTurnplateInfo.newBuilder();
        builder.setId(conf.getTurnplateId());
        builder.setCount(conf.getCount());
        builder.setPrice(conf.getPrice());
        builder.setPoint(conf.getPoint());
        builder.setTitle(conf.getBg());
        return builder.build();
    }

    public static DbSpecialProp createSpecialProp(Entry<Integer, Set<Integer>> kv) {
        DbSpecialProp.Builder builder = DbSpecialProp.newBuilder();
        builder.setType(kv.getKey());
        for (Integer id : kv.getValue()) {
            builder.addSpecialId(id);
        }
        return builder.build();

    }

    public static CommonPb.Barrage createBarragePb(int now, int barrageId, String... param) {
        Barrage.Builder barrageBuilder = Barrage.newBuilder();
        barrageBuilder.setTime(TimeHelper.getCurrentSecond());
        barrageBuilder.setBarrgeId(barrageId);
        if (param != null) {
            for (String p : param) {
                barrageBuilder.addParam(p);
            }
        }
        return barrageBuilder.build();
    }

    public static CommonPb.RedPacketRole createRedPacketRolePb(RedPacketRole rr, PlayerDataManager playerDataManager) {
        CommonPb.RedPacketRole.Builder builder = CommonPb.RedPacketRole.newBuilder();
        builder.setRoleId(rr.getRoleId());
        builder.setTime(rr.getTime());
        builder.setAwardId(rr.getAwardId());
        builder.setMsgId(rr.getMsgId());
        builder.setPortrait(rr.getPortrait());
        if (playerDataManager != null) {
            Player player = playerDataManager.getPlayer(rr.getRoleId());
            if (player != null) {
                builder.setNick(player.lord.getNick());
                builder.setCamp(player.lord.getCamp());
            }
        } else {
            builder.setNick(rr.getNick());
        }
        return builder.build();

    }

    public static CommonPb.RedPacket createRedPacketPb(RedPacket rp, PlayerDataManager playerDataManager) {
        CommonPb.RedPacket.Builder builder = CommonPb.RedPacket.newBuilder();
        builder.setId(rp.getId());
        builder.setSendTime(rp.getSendTime());
        builder.setExceedTime(rp.getExceedTime());
        builder.setRedPackId(rp.getRedPackId());
        builder.setChatId(rp.getChatId());
        for (RedPacketRole role : rp.getRole().values()) {
            builder.addRole(createRedPacketRolePb(role, playerDataManager));
        }
        builder.addAllRewarPond(rp.getRewarPond());
        builder.addAllParam(rp.getParam());
        builder.setRedType(rp.getRedType());
        return builder.build();
    }

    public static CommonPb.RedPacketShow createRedPacketShowPb(RedPacket rp, int time) {
        CommonPb.RedPacketShow.Builder builder = CommonPb.RedPacketShow.newBuilder();
        builder.setId(rp.getId());
        builder.setSendTime(rp.getSendTime());
        builder.setExceedTime(rp.getExceedTime());
        builder.setChatId(rp.getChatId());
        builder.addAllParam(rp.getParam());
        builder.setTime(time);
        return builder.build();
    }

    public static CommonPb.DayDiscounts createDayDiscountsPb(StaticActDaydiscounts sad, StaticActGiftpack sGift,
                                                             StaticPay payinfo, StaticPay payinfoIos, int count) {
        CommonPb.DayDiscounts.Builder builder = CommonPb.DayDiscounts.newBuilder();
        builder.setGiftId(sad.getActGiftId());
        builder.setCount(count);
        builder.setGrade(sad.getGrade());
        builder.addAllAward(PbHelper.createAwardsPb(sGift.getAward()));
        if (payinfo != null) {
            builder.setPayinfo(createPayInfoBySpay(payinfo));
        }
        if (payinfoIos != null) {
            builder.setPayinfoIos(createPayInfoBySpay(payinfoIos));
        }
        return builder.build();
    }

    public static CommonPb.MapForce createMapForcePb(SuperMine sm) {
        CommonPb.MapForce.Builder builder = CommonPb.MapForce.newBuilder();
        builder.setPos(sm.getPos());
        builder.setType(WorldConstant.FORCE_TYPE_SPUER_MINE);
        builder.setParam(sm.getConfigId());
        builder.setCamp(sm.getCamp());
        builder.setBattle(sm.getBattleIds().size() > 0); // 是否有战斗
        builder.setSeqId(sm.getSeqId());
        return builder.build();
    }

    public static CommonPb.SuperMine createSuperMinePbShowCity(SuperMine sm, int now, PlayerDataManager playerManager) {
        CommonPb.SuperMine.Builder builder = CommonPb.SuperMine.newBuilder();
        builder.setSeqId(sm.getSeqId());
        builder.setCamp(sm.getCamp());
        builder.setPos(sm.getPos());
        builder.setState(sm.getState());
        builder.setConfigId(sm.getConfigId());
        builder.setCapacity(sm.getCapacity());
        builder.setCityId(sm.getCityId());
        builder.setNextTime(sm.getNextTime());
        builder.setRemaining(sm.calcCollectRemaining(now));
        for (SuperGuard sg : sm.getCollectArmy()) { // 矿点采集将领
            Player player = playerManager.getPlayer(sg.getArmy().getLordId());
            if (player != null) {
                builder.addCollectList(PbHelper.createCollectPb(player, sg, now));
            }
        }
        return builder.build();
    }

    public static CommonPb.SuperMine createSuperMineDetailPb(SuperMine sm, int now, PlayerDataManager playerManager) {
        CommonPb.SuperMine.Builder builder = CommonPb.SuperMine.newBuilder();
        builder.setSeqId(sm.getSeqId());
        builder.setCamp(sm.getCamp());
        builder.setPos(sm.getPos());
        builder.setState(sm.getState());
        builder.setConfigId(sm.getConfigId());
        builder.setCapacity(sm.getCapacity());
        builder.setCityId(sm.getCityId());
        builder.setRemaining(sm.calcCollectRemaining(now));
        builder.setNextTime(sm.getNextTime());
        for (SuperGuard sg : sm.getCollectArmy()) { // 矿点采集将领
            Player player = playerManager.getPlayer(sg.getArmy().getLordId());
            if (player != null) {
                builder.addCollectList(PbHelper.createCollectPb(player, sg, now));
            }
        }
        for (Army army : sm.getHelpArmy()) { // 矿点驻防将领
            Player player = playerManager.getPlayer(army.getLordId());
            if (player != null) {
                builder.addAllHelpList(PbHelper.createWallHeroPbBySuperMine(player, army));
            }
        }
        return builder.build();
    }

    private static List<CommonPb.WallHero> createWallHeroPbBySuperMine(Player player, Army army) {
        List<CommonPb.WallHero> list = new ArrayList<>(army.getHero().size());
        for (TwoInt heroIdCnt : army.getHero()) {
            CommonPb.WallHero.Builder wallBuilder = CommonPb.WallHero.newBuilder();
            wallBuilder.setKeyId(army.getKeyId());
            wallBuilder.setLordId(army.getLordId());
            wallBuilder.setLevel(player.lord.getLevel());
            wallBuilder.setName(player.lord.getNick());
            wallBuilder.setHeroId(heroIdCnt.getV1());
            wallBuilder.setCount(heroIdCnt.getV2());
            list.add(wallBuilder.build());
        }
        return list;
    }

    private static CommonPb.Collect createCollectPb(Player player, SuperGuard sg, int now) {
        CommonPb.Collect.Builder builder = CommonPb.Collect.newBuilder();
        builder.setCamp(player.lord.getCamp());
        builder.setLv(player.lord.getLevel());
        builder.setRoleId(player.roleId);
        builder.setName(player.lord.getNick());
        int heroId = sg.getArmy().getHero().get(0).getV1();
        Hero hero = player.heros.get(heroId);
        builder.setHeroLv(hero.getLevel());
        builder.setHeroId(hero.getHeroId());
        builder.setCount(hero.getCount());
        builder.setCollectedTime(sg.calcCollectedTime(now));
        builder.setCanMaxCollectTime(sg.getCanMaxCollectTime());
        builder.setHeroTotalCnt(hero.getAttr()[3]);// 总兵力
        builder.setHeroDecorated(hero.getDecorated());
        return builder.build();
    }

    // 数据库保存
    public static CommonPb.SuperMine createSuperMineDbPb(SuperMine sm) {
        CommonPb.SuperMine.Builder builder = CommonPb.SuperMine.newBuilder();
        builder.setSeqId(sm.getSeqId());
        builder.setCamp(sm.getCamp());
        builder.setPos(sm.getPos());
        builder.setState(sm.getState());
        builder.setConfigId(sm.getConfigId());
        builder.setConvertRes(sm.getConvertRes());
        builder.setCapacity(sm.getCapacity());
        builder.setCityId(sm.getCityId());
        builder.addAllBattleIds(sm.getBattleIds());
        builder.setNextTime(sm.getNextTime());
        for (SuperGuard sg : sm.getCollectArmy()) {
            builder.addCollectArmy(createSuperGuard(sg));
        }
        for (Army army : sm.getHelpArmy()) {
            builder.addHelpArmy(PbHelper.createArmyPb(army, true));
        }
        return builder.build();
    }

    public static CommonPb.SuperGuard createSuperGuard(SuperGuard sg) {
        CommonPb.SuperGuard.Builder builder = CommonPb.SuperGuard.newBuilder();
        builder.setArmy(PbHelper.createArmyPb(sg.getArmy(), true));
        builder.setStartTime(sg.getStartTime());
        builder.setMaxCollectTime(sg.getMaxCollectTime());
        builder.setCollectTime(sg.getCollectTime());
        builder.setMaxCollectTime(sg.getMaxCollectTime());
        builder.setArmyArriveTime(sg.getArmyArriveTime());
        builder.setCanMaxCollectTime(sg.getCanMaxCollectTime());
        return builder.build();
    }

    public static CommonPb.BerlinJob createBerlinJobPb(Player player, int job) {
        return createBerlinJobPb(player, job, 0);
    }

    public static CommonPb.BerlinJob createBerlinJobPb(Player player, int job, int now) {
        CommonPb.BerlinJob.Builder builder = CommonPb.BerlinJob.newBuilder();
        builder.setRoleId(player.roleId);
        builder.setName(player.lord.getNick());
        builder.setJob(job);
        builder.setLv(player.lord.getLevel());
        builder.setCamp(player.lord.getCamp());
        builder.setArea(player.lord.getArea());
        builder.setPortrait(player.lord.getPortrait());
        builder.setRanks(player.lord.getRanks());
        if (now > 0) {
            builder.setWinnerTime(now);
        }
        builder.setPortraitFrame(player.getDressUp().getCurPortraitFrame());
        return builder.build();
    }

    public static List<CommonPb.HonorReport> createHonorReports(List<DailyReport> dailyReports) {
        ArrayList<CommonPb.HonorReport> honorReports = new ArrayList<>();
        dailyReports.forEach(report -> honorReports.add(report.ser()));
        return honorReports;
    }

    public static CityAtkFirstKill createKillInfo(Player cityOwner, BerlinWar berlinWar, StaticCity sCity) {
        CityAtkFirstKill.Builder builder = CityAtkFirstKill.newBuilder();
        if (CheckNull.isNull(cityOwner) || CheckNull.isNull(berlinWar) || CheckNull.isNull(sCity)) {
            return builder.build();
        }
        BerlinCityInfo cityInfo = berlinWar.getBerlinCityInfo();
        if (CheckNull.isNull(cityInfo)) {
            return builder.build();
        }
        builder.setArea(sCity.getArea());
        builder.setType(sCity.getType());
        builder.setCityId(sCity.getCityId());
        builder.setCamp(cityInfo.getCamp());
        builder.setSponsor(createAtkFirstKillInfo(cityOwner));
        return builder.build();
    }

    public static CommonPb.AtkFirstKillInfo createAtkFirstKillInfo(Player player) {
        CommonPb.AtkFirstKillInfo.Builder builder = CommonPb.AtkFirstKillInfo.newBuilder();
        builder.setLordId(player.roleId);
        builder.setPortrait(player.lord.getPortrait());
        builder.setNick(player.lord.getNick());
        builder.setParam(String.valueOf(player.lord.getCamp()));
        builder.setPortraitFrame(player.getDressUp().getCurPortraitFrame());
        return builder.build();
    }

    public static CommonPb.PartyCity createPartyCityPb(City city, PlayerDataManager playerDataManager) {
        PartyCity.Builder ser = PartyCity.newBuilder();
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
        ser.setPos(staticCity.getCityPos());
        ser.setCityId(city.getCityId());
        ser.setCityLv(city.getCityLv());
        ser.setCurArm(city.getCurArm());
        ser.setMaxArm(city.getTotalArm());
        ser.setEndTime(city.getEndTime());
        Player owner = playerDataManager.getPlayer(city.getOwnerId());
        if (null != owner) {
            ser.setOwner(owner.lord.getNick());
        }
        ser.addAllRole(city.getCampaignList());
        ser.setLeaveOver(city.getLeaveOver());
        ser.setCampaignTime(city.getCampaignTime());
        ser.setOwnerCamp(city.getCamp());
        return ser.build();
    }

    public static GamePb4.AttackBerlinWarRq createAttackBerlinWarRq(int cityId, int heroId, int atkType) {
        GamePb4.AttackBerlinWarRq.Builder builder = GamePb4.AttackBerlinWarRq.newBuilder();
        builder.setAtkType(atkType);
        builder.setHeroId(heroId);
        builder.setCityId(cityId);
        return builder.build();
    }

    public static CommonPb.BerlinRoleInfo createBerlinRoleInfo(BerlinRoleInfo roleInfo) {
        CommonPb.BerlinRoleInfo.Builder builder = CommonPb.BerlinRoleInfo.newBuilder();
        builder.setPressCnt(roleInfo.getStatus(BerlinWarConstant.RoleInfo.PRESS_CNT));
        builder.setResurrectionCnt(roleInfo.getStatus(BerlinWarConstant.RoleInfo.RESURRECTION_CNT));
        builder.setImmediatelyCnt(roleInfo.getStatus(BerlinWarConstant.RoleInfo.IMMEDIATELY_CNT));
        builder.setAtkCd(roleInfo.getAtkCD());
        return builder.build();
    }

    /**
     * 创建一个账号角色信息
     *
     * @param p
     * @return
     */
    public static AccountRoleInfo createAccountRoleInfo(Player p) {
        AccountRoleInfo.Builder ab = AccountRoleInfo.newBuilder();
        ab.setAccountKey(p.account.getAccountKey());
        ab.setCamp(p.lord.getCamp());
        ab.setDateRoleCreate(p.lord.getCtTime());
        ab.setLevel(p.lord.getLevel());
        ab.setRoleId(p.lord.getLordId());
        ab.setRoleName(p.lord.getNick());
        ab.setServerId(p.account.getServerId());
        ab.setFight(p.lord.getFight());
        ab.setVip(p.lord.getVip());
        ab.setCommandLv(p.building.getCommand());
        return ab.build();
    }

    public static CommonPb.MonopolyGrid createMonopolyGridPb(StaticActMonopoly sam) {
        CommonPb.MonopolyGrid.Builder builder = CommonPb.MonopolyGrid.newBuilder();
        builder.setRound(sam.getRound());
        builder.setGrid(sam.getGrid());
        List<List<Integer>> awardList = sam.getAward();
        if (awardList != null) {
            builder.addAllAward(createAwardsPb(awardList));
        }
        return builder.build();
    }

    public static BerlinBasicReport createBerlinBasicReport(boolean atkSuccess, Fighter attacker, Fighter defender,
                                                            Lord defLord, int heroId, int decorated) {
        CommonPb.BerlinBasicReport.Builder basicReport = CommonPb.BerlinBasicReport.newBuilder();
        basicReport.setIsWin(atkSuccess);
        basicReport.setAtkLost(attacker.lost);
        basicReport.setLost(defender.lost);
        basicReport.setRoleId(defLord.getLordId());
        basicReport.setHeroId(heroId);
        basicReport.setCamp(defLord.getCamp());
        basicReport.setNick(defLord.getNick());
        basicReport.setDecorated(decorated);
        return basicReport.build();
    }

    public static GamePb2.SyncAttackRoleRs createSyncAttackRoleRs(Lord atkLord, int atkTime, int status, int battleType,
                                                                  List<String> params) {
        GamePb2.SyncAttackRoleRs.Builder builder = GamePb2.SyncAttackRoleRs.newBuilder();
        if (!CheckNull.isNull(atkLord)) {
            builder.setAtkCamp(atkLord.getCamp());
            builder.setAtkName(atkLord.getNick());
            builder.setAtkPos(atkLord.getPos());
            builder.setActRoleId(atkLord.getLordId());
        }
        builder.setAtkTime(atkTime);
        builder.setStatus(status);
        builder.setBattleType(battleType);
        if (!CheckNull.isEmpty(params)) {
            builder.addAllParam(params);
        }
        return builder.build();
    }

    public static HitRoleInfo createHitRoleInfo(Player p) {
        HitRoleInfo.Builder builder = HitRoleInfo.newBuilder();
        builder.setRoleId(p.roleId);
        builder.setNick(p.lord.getNick());
        builder.setPos(p.lord.getPos());
        builder.setLv(p.lord.getLevel());
        builder.setCommand(p.building.getCommand());
        return builder.build();

    }

    public static TeamMember createTeamMemberPb(Player p, int attackCnt) {
        TeamMember.Builder builder = TeamMember.newBuilder();
        builder.setRoleId(p.roleId);
        builder.setNick(p.lord.getNick());
        builder.setFight(p.lord.getFight());
        if (attackCnt != 0) {
            // 进攻的兵力值, 复用fight字段
            builder.setFight(attackCnt);
        }
        builder.setLv(p.lord.getLevel());
        builder.setCamp(p.lord.getCamp());
        builder.setRanks(p.lord.getRanks());
        builder.setPortrait(p.lord.getPortrait());
        builder.setOnline(p.isLogin);
        builder.setPos(p.lord.getPos());
        builder.setPortraitFrame(p.getDressUp().getCurPortraitFrame());
        return builder.build();
    }

    /**
     * 创建MultCombatTeam的pb对象
     *
     * @param mct
     * @param pm
     * @return
     */
    public static CommonPb.MultCombatTeam createMultCombatTeamPb(MultCombatTeam mct, PlayerDataManager pm) {
        CommonPb.MultCombatTeam.Builder builder = CommonPb.MultCombatTeam.newBuilder();
        builder.setTeamId(mct.getTeamId());
        builder.setCaptainRoleId(mct.getCaptainRoleId());
        builder.setCombatId(mct.getCombatId());
        builder.setAutoJoin(mct.isAutoJoin());
        builder.setAutoStart(mct.isAutoStart());
        for (Long roleId : mct.getTeamMember()) {
            Player p = pm.getPlayer(roleId);
            if (p != null) {
                builder.addTm(PbHelper.createTeamMemberPb(p, 0));
            }
        }
        return builder.build();
    }

    public static SpecialAct createSpecialAct(StaticSpecialPlan plan) {
        SpecialAct.Builder builder = SpecialAct.newBuilder();
        builder.setKeyId(plan.getKeyId());
        builder.setType(plan.getActivityType());
        builder.setBeginTime(TimeHelper.dateToSecond(plan.getBeginTime()));
        builder.setEndTime(TimeHelper.dateToSecond(plan.getEndTime()));
        return builder.build();
    }

    public static CommonPb.MapForce createMapForcePb(AirshipWorldData aswd, PlayerDataManager pm) {
        CommonPb.MapForce.Builder builder = CommonPb.MapForce.newBuilder();
        builder.setPos(aswd.getPos());
        builder.setType(WorldConstant.FORCE_TYPE_AIRSHIP);
        builder.setParam(aswd.getId());
        builder.setCamp(aswd.getRemainHp());
        builder.setSeqId(aswd.getKeyId());
        if (aswd.getBelongRoleId() > 0) {
            Player belongP = pm.getPlayer(aswd.getBelongRoleId());
            if (belongP != null) {
                builder.setCamp(belongP.lord.getCamp());
            }
        }
        return builder.build();

    }

    public static MapForce createMapForcePb(StaticScheduleBoss ssb, ScheduleBoss boss) {
        CommonPb.MapForce.Builder builder = CommonPb.MapForce.newBuilder();
        builder.setPos(ssb.getPos());
        builder.setType(WorldConstant.FORCE_TYPE_SCHEDULE_BOSS);
        builder.setParam(boss.getRemainHp());
        return builder.build();
    }

    // 客户端显示的pb
    public static CommonPb.Airship createAirshipShowClientPb(AirshipWorldData aswd, final int myCamp,
                                                             boolean showJoinList, PlayerDataManager pm) {
        CommonPb.Airship.Builder builder = CommonPb.Airship.newBuilder();
        builder.setId(aswd.getId());
        builder.setKeyId(aswd.getKeyId());
        builder.setAreaId(aswd.getAreaId());
        builder.setPos(aswd.getPos());
        builder.setStatus(aswd.getStatus());
        builder.setTriggerTime(aswd.getTriggerTime());

        builder.setRemainHp(aswd.getRemainHp());
        // 所属玩家
        Player belongPlayer = pm.getPlayer(aswd.getBelongRoleId());
        if (belongPlayer != null) {
            builder.setBelongMember(PbHelper.createTeamMemberPb(belongPlayer, 0));
        }
        // 加入玩家
        List<BattleRole> joinRole = aswd.getJoinRoles().get(myCamp);
        if (!CheckNull.isEmpty(joinRole)) {
            if (showJoinList) {
                joinRole.stream()
                        .map(BattleRole::getRoleId)
                        .distinct()
                        .forEach(roleId -> {
                            Player joinP = pm.getPlayer(roleId);
                            if (joinP != null) {
                                int attackCnt = joinRole.stream()
                                        .filter(jr -> jr.getRoleId() == roleId)
                                        .map(BattleRole::getHeroIdList)
                                        // 获取进攻将领的上阵兵力
                                        .flatMapToInt(list ->
                                                list.stream()
                                                        .map(heroId -> joinP.heros.get(heroId))
                                                        .filter(Objects::nonNull)
                                                        .mapToInt(Hero::getCount))
                                        .sum();
                                builder.addJoinMember(PbHelper.createTeamMemberPb(joinP, attackCnt));
                            }
                        });
            }
            builder.setJoinCnt(joinRole.size());
        }
        builder.addAllInvites(aswd.getInvites());
        return builder.build();
    }

    public static InvitesRole crateInvitesRole(Player player, AirshipWorldData airship, Battle battle) {
        InvitesRole.Builder builder = InvitesRole.newBuilder();
        builder.setRoleId(player.roleId);
        builder.setLevel(player.lord.getLevel());
        builder.setPortrait(player.lord.getPortrait());
        builder.setNick(player.lord.getNick());
        builder.setFight(player.lord.getFight());
        builder.setPos(player.lord.getPos());
        int status = player.isLogin ? 1 : 0;
        if (Objects.nonNull(airship)) {
            HashSet<Long> invites = airship.getInvites();
            if (!CheckNull.isEmpty(invites) && invites.contains(player.roleId)) {
                status = 2;
            }
            List<BattleRole> battleRoles = airship.getJoinRoles().get(player.lord.getCamp());
            if (!CheckNull.isEmpty(battleRoles) && battleRoles.stream().anyMatch(br -> br.getRoleId() == player.roleId)) {
                status = 3;
            }
        }
        if (Objects.nonNull(battle)) {
            HashSet<Long> invites = battle.getInvites();
            if (!CheckNull.isEmpty(invites) && invites.contains(player.roleId)) {
                status = 2;
            }
            List<BattleRole> battleRoles = battle.getAtkList();
            if (!CheckNull.isEmpty(battleRoles) && battleRoles.stream().anyMatch(br -> br.getRoleId() == player.roleId)) {
                status = 3;
            }
        }
        builder.setPortraitFrame(player.getDressUp().getCurPortraitFrame());
        builder.setStatus(status);
        return builder.build();
    }

    // 数据库保存的db
    public static CommonPb.Airship createAirshipDBPb(AirshipWorldData aswd) {
        CommonPb.Airship.Builder builder = CommonPb.Airship.newBuilder();
        builder.setId(aswd.getId());
        builder.setKeyId(aswd.getKeyId());
        builder.setAreaId(aswd.getAreaId());
        builder.setPos(aswd.getPos());
        builder.setStatus(aswd.getStatus());
        builder.setTriggerTime(aswd.getTriggerTime());
        builder.setBelongRoleId(aswd.getBelongRoleId());
        List<NpcForce> npcList = aswd.getNpc();
        for (NpcForce npc : npcList) {
            builder.addNpc(createForcePb(npc));
        }
        Map<Integer, List<BattleRole>> joinRoles = aswd.getJoinRoles();
        for (Entry<Integer, List<BattleRole>> kv : joinRoles.entrySet()) {
            int camp = kv.getKey();
            if (camp == 1) {
                builder.addAllCamp1Army(kv.getValue());
            } else if (camp == 2) {
                builder.addAllCamp2Army(kv.getValue());
            } else if (camp == 3) {
                builder.addAllCamp3Army(kv.getValue());
            }
        }
        builder.addAllInvites(aswd.getInvites());
        return builder.build();
    }

    public static Force createForcePb(NpcForce npc) {
        Force.Builder builder = Force.newBuilder();
        builder.setNpcId(npc.getNpcId());
        builder.setHp(npc.getHp());
        builder.setCurLine(npc.getCurLine());
        return builder.build();
    }

    public static CommonPb.PartySupply createPartySupplyPb(PartySupply partySupply, boolean ser) {
        CommonPb.PartySupply.Builder builder = CommonPb.PartySupply.newBuilder();
        builder.setKey(partySupply.getKey());
        builder.setEndTime(partySupply.getEndTime());
        builder.setId(partySupply.getId());
        builder.setIncrementKey(PartySupply.INCREMENT_KEY);
        if (ser) {
            Map<Long, Integer> awardStatus = partySupply.getAwardStatus();
            for (Entry<Long, Integer> en : awardStatus.entrySet()) {
                builder.addStatus(createLongIntPb(en.getKey(), en.getValue()));
            }
        }

        return builder.build();
    }

    public static CommonPb.PartySuperSupply createSuperSupplyPb(PartySuperSupply supperSupply) {
        CommonPb.PartySuperSupply.Builder builder = CommonPb.PartySuperSupply.newBuilder();
        builder.setLv(supperSupply.getLv());
        builder.setEnergy(supperSupply.getEnergy());
        builder.setCostGold(supperSupply.getCostGold());
        return builder.build();
    }

    public static CommonPb.EquipJewel createJewelPb(EquipJewel jewel) {
        CommonPb.EquipJewel.Builder builder = CommonPb.EquipJewel.newBuilder();
        builder.setJewelId(jewel.getJewelId());
        builder.setCount(jewel.getCount());
        builder.setInlaid(jewel.getInlaid().get());
        return builder.build();
    }

    public static CommonPb.SignInfo createSignInfoPb(SiginInfo siginInfo) {
        SignInfo.Builder builder = SignInfo.newBuilder();
        builder.setTime(siginInfo.getTimes());
        builder.setSignIn(siginInfo.getSignIn());
        builder.setDoubleReward(siginInfo.getDoubleReward());
        builder.setLevel(siginInfo.getLevel());
        builder.setPage(siginInfo.getPage());
        return builder.build();
    }

    public static SignInInfo createSignInInfoPb(SiginInfo siginInfo) {
        CommonPb.SignInInfo.Builder builder = CommonPb.SignInInfo.newBuilder();
        builder.setDate(siginInfo.getDate());
        builder.setTimes(siginInfo.getTimes());
        builder.setLevel(siginInfo.getLevel());
        builder.setSignIn(siginInfo.getSignIn());
        builder.setDoubleReward(siginInfo.getDoubleReward());
        builder.setPage(siginInfo.getPage());
        if (siginInfo.getActivityId() != 0) {
            builder.setActivityId(siginInfo.getActivityId());
        }
        builder.setKeyId(siginInfo.getKeyId());
        return builder.build();
    }

    public static SignInInfo createSignInInfoPb(SiginInfo siginInfo, int actType) {
        CommonPb.SignInInfo.Builder builder = CommonPb.SignInInfo.newBuilder();
        builder.setDate(siginInfo.getDate());
        builder.setTimes(siginInfo.getTimes());
        builder.setLevel(siginInfo.getLevel());
        builder.setSignIn(siginInfo.getSignIn());
        builder.setDoubleReward(siginInfo.getDoubleReward());
        builder.setPage(siginInfo.getPage());
        if (siginInfo.getActivityId() != 0) {
            builder.setActivityId(siginInfo.getActivityId());
        }
        builder.setActType(actType);
        builder.setKeyId(siginInfo.getKeyId());
        return builder.build();
    }

    public static SerializePb.SerMixtureData createMixtureData(String key, Map<Integer, Integer> value) {
        SerializePb.SerMixtureData.Builder builder = SerializePb.SerMixtureData.newBuilder();
        builder.setKey(key);
        for (Entry<Integer, Integer> entry : value.entrySet()) {
            builder.addValue(createTwoIntPb(entry.getKey(), entry.getValue()));
        }
        return builder.build();
    }

    public static BattlePassPlanPb createBattlePassPlanPb(GlobalBattlePass globalBattlePass) {
        BattlePassPlanPb.Builder builder = BattlePassPlanPb.newBuilder();
        StaticBattlePassPlan sBattlePassPlan = StaticBattlePassDataMgr.getPlanById(globalBattlePass.getStaticKey());
        Optional.ofNullable(sBattlePassPlan).ifPresent(sbp -> {
            // 这里传战令的模板id
            builder.setKeyId(sbp.getPlanId());
            builder.setBeginTime(TimeHelper.dateToSecond(globalBattlePass.getBeginDate()));
            builder.setEndTime(TimeHelper.dateToSecond(globalBattlePass.getEndDate()));
            builder.addConf(PbHelper.createTwoIntPb(1, sbp.getBuyLevel()));
            builder.addConf(PbHelper.createTwoIntPb(2, sbp.getDailyExp()));
            builder.addConf(PbHelper.createTwoIntPb(3, sbp.getPayId()));
        });

        GlobalBattlePass.REFRESH_TIME_KEYS.forEach(key -> {
            builder.addLastFresh(createTwoIntPb(key, globalBattlePass.lastRefreshTimeByKey(key)));
        });
        return builder.build();
    }

    public static Iterable<? extends BattlePassTaskPb> createBattlePassTaskPb(BattlePassPersonInfo personInfo, BattlePassDataManager battlePassDataManager, Player player) {
        List<BattlePassTaskPb> passTaskPbs = new ArrayList<>();
        int staticKey = personInfo.getStaticKey();
        Map<Integer, battlePassTask> passTaskMap = personInfo.getPassTaskMap();
        Collection<StaticBattlePassTask> sPassTasks = StaticBattlePassDataMgr.getTasksByPlanKey(staticKey);
        if (!CheckNull.isEmpty(sPassTasks)) {
            for (StaticBattlePassTask sPassTask : sPassTasks) {
                int taskId = sPassTask.getId();
                battlePassTask passTask = passTaskMap.getOrDefault(taskId, new battlePassTask(taskId));
                // 刷新主动获取进度的task
                battlePassDataManager.refreshTaskSchedule(sPassTask, passTask, player);
                BattlePassTaskPb passTaskPb = createBattlePassTaskPb(passTask);
                if (!CheckNull.isNull(passTaskPb)) {
                    passTaskPbs.add(passTaskPb);
                }
            }
        }
        return passTaskPbs;
    }

    public static BattlePassTaskPb createBattlePassTaskPb(battlePassTask passTask) {
        if (!CheckNull.isNull(passTask)) {
            BattlePassTaskPb.Builder builder = BattlePassTaskPb.newBuilder();
            builder.setTaskId(passTask.getTaskId());
            builder.setSchedule(passTask.getSchedule());
            builder.setStatus(passTask.getStatus());
            return builder.build();
        }
        return null;
    }

    public static Iterable<? extends BattlePassAwardPb> createBattleAwardPb(BattlePassPersonInfo personInfo) {
        List<BattlePassAwardPb> passAwardPbs = new ArrayList<>();
        int staticKey = personInfo.getStaticKey();
        Map<Integer, Map<Integer, Integer>> passAwardStat = personInfo.getPassAwardStat();
        Collection<StaticBattlePassLv> awardLvs = StaticBattlePassDataMgr.getLvAwardByPlanKey(staticKey);
        if (!CheckNull.isEmpty(awardLvs)) {
            for (StaticBattlePassLv sPassLv : awardLvs) {
                BattlePassAwardPb.Builder builder = BattlePassAwardPb.newBuilder();
                int lv = sPassLv.getLv();
                builder.setKeyId(lv);
                Map<Integer, Integer> statusMap = passAwardStat.getOrDefault(lv, new HashMap<>());
                builder.setNormalStat(statusMap.getOrDefault(BattlePassPersonInfo.AWARD_NORMAL_KEY, 0));
                builder.setSpecialStat(statusMap.getOrDefault(BattlePassPersonInfo.AWARD_SPECIAL_KEY, 0));
                passAwardPbs.add(builder.build());
            }
        }
        return passAwardPbs;
    }

    public static BattlePassPersonInfoPb createBattlePassInfoPb(BattlePassPersonInfo personInfo) {
        BattlePassPersonInfoPb.Builder builder = BattlePassPersonInfoPb.newBuilder();
        builder.setExp(personInfo.getExp());
        builder.setLv(personInfo.getLv());
        builder.setAdvanced(personInfo.getAdvanced());
        builder.setDailyExp(personInfo.getDailyExp());
        return builder.build();
    }

    public static SmallAccountData createSmallAccountData(long roleId, long accountKey, int sid) {
        SmallAccountData.Builder builder = SmallAccountData.newBuilder();
        builder.setRoleId(roleId);
        builder.setAccountKey(accountKey);
        builder.setServerId(sid);
        return builder.build();
    }

    public static MergeBannerData createMergeBannerData(StaticMergeBanner smb) {
        MergeBannerData.Builder builder = MergeBannerData.newBuilder();
        builder.setServer(smb.getServer().toString());
        builder.setBloc(smb.getBloc());
        builder.setTimeBegin((int) (smb.getTimeBegin().getTime() / 1000));
        builder.setTimeEnd((int) (smb.getTimeEnd().getTime() / 1000));
        builder.setDesc1(smb.getDesc1());
        builder.setDesc2(smb.getDesc2());
        builder.setDesc3(smb.getDesc3());
        builder.setOpTime(smb.getOpTime());
        return builder.build();
    }

    public static CommonPb.IntegralRank createIntegralRank(Player player, int rank, RankItem<Integer> rit) {
        CommonPb.IntegralRank.Builder builder = CommonPb.IntegralRank.newBuilder();
        builder.setCamp(player.getCamp());
        builder.setLordId(player.getLordId());
        builder.setNick(player.lord.getNick());
        builder.setValue(rit.getRankValue());
        builder.setRanking(rank);
        return builder.build();
    }

    public static CommonPb.PbSeasonTalent createSeasonTalent(SeasonTalent talent, int resetCountWeek, int seasonTalentPlanId) {
        CommonPb.PbSeasonTalent.Builder pbt = CommonPb.PbSeasonTalent.newBuilder();
        pbt.setSeasonId(talent.getSeasonId());
        pbt.setClassifier(talent.getClassifier());
        pbt.setRemainStone(talent.getRemainStone());
        pbt.setCostStone(talent.getCostStone());
        pbt.setTotalStone(talent.getTotalStone());
        pbt.setResetClassifierCountWeek(resetCountWeek);
        pbt.setOpenTalent(talent.isOpenTalent());
        pbt.setOpenTalentProgress(talent.getOpenTalentProgress());
        if (seasonTalentPlanId != 0 && !talent.getLearns().isEmpty()) {
            pbt.addAllLearn(talent.getLearns());
        }
        return pbt.build();
    }

    public static GamePb4.GetActAuctionInfoRs createNoAuction(Activity activity, CommonPb.TwoInt.Builder currentRoundStatus, ActivityAuctionParam param) {
        GamePb4.GetActAuctionInfoRs.Builder builder = GamePb4.GetActAuctionInfoRs.newBuilder();
        builder.addAllConcernedItem(activity.getActivityAuction().getConcernedItem());
        builder.setActivityId(activity.getActivityId());
        builder.setStatus(currentRoundStatus == null ? 0 : currentRoundStatus.getV2());
        builder.setRound(currentRoundStatus == null ? 0 : currentRoundStatus.getV1());
        builder.setBeginTime(param == null ? 0 : (int) (param.getStartTime().getTime() / TimeHelper.SECOND_MS));
        builder.setEndTime(param == null ? 0 : (int) (param.getEndTime().getTime() / TimeHelper.SECOND_MS));
        return builder.build();
    }

    public static GamePb4.GetActAuctionRecordRs createNoAuctionRecord(Activity activity) {
        GamePb4.GetActAuctionRecordRs.Builder builder = GamePb4.GetActAuctionRecordRs.newBuilder();
        builder.setActivityId(activity.getActivityId());
        return builder.build();
    }

    public static GamePb4.GetMyAuctionRecordRs createNoMyAuctionRecord(Activity activity) {
        GamePb4.GetMyAuctionRecordRs.Builder builder = GamePb4.GetMyAuctionRecordRs.newBuilder();
        builder.setActivityId(activity.getActivityId());
        return builder.build();
    }

    public static CommonPb.MineDataPb createMineDataPb(int v1, MineData mineData) {
        CommonPb.MineDataPb.Builder builder = CommonPb.MineDataPb.newBuilder();
        builder.setPos(v1);
        builder.setMineId(mineData.getMineId());
        if (!ObjectUtils.isEmpty(mineData.getCollectTeam())) {
            builder.addAllCollectTeam(mineData.getCollectTeam());
        }

        return builder.build();
    }

    public static Collection<Award> mergeAwards(List<Award> awardList){
        if(ListUtils.isBlank(awardList)){
            return Collections.EMPTY_LIST;
        }
        Map<Integer,Award> map = new HashMap<>();
        awardList.forEach(o -> {
            Award award = map.get(o.getId());
            if(Objects.isNull(award)){
                map.put(o.getId(),o.toBuilder().build());
            }else {
                map.put(o.getId(),award.toBuilder().setCount(award.getCount() + o.getCount()).build());
            }
        });
        return map.values();
    }

    public static CommonPb.ActTask createActTaskPb(ActivityTask task) {
        CommonPb.ActTask.Builder builder = CommonPb.ActTask.newBuilder();
        builder.setUid(task.getUid());
        builder.setProgress(task.getProgress());
        builder.setFinishCnt(task.getCount());
        builder.setDrawCnt(task.getDrawCount());
        builder.setTaskType(task.getTaskId());
        return builder.build();
    }

    public static List<List<Integer>> convertTo(List<CommonPb.ExchangeItemConsume> subList) {
        List<List<Integer>> consumeList = new ArrayList<>(subList.size());
        for (CommonPb.ExchangeItemConsume tmp : subList) {
            List<Integer> list = new ArrayList<>(3);
            list.add(tmp.getType());
            list.add(tmp.getId());
            list.add(tmp.getCount());
            consumeList.add(list);
        }
        return consumeList;
    }
}
