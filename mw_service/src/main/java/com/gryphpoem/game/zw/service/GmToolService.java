package com.gryphpoem.game.zw.service;

import com.alibaba.fastjson.JSONArray;
import com.google.protobuf.GeneratedMessage;
import com.googlecode.protobuf.format.JsonFormat;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.ReflectUtil;
import com.gryphpoem.game.zw.core.util.StrUtils;
import com.gryphpoem.game.zw.dataMgr.StaticVipDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.PlayerInfo;
import com.gryphpoem.game.zw.pb.HttpPb;
import com.gryphpoem.game.zw.pb.HttpPb.BackLordBaseRq;
import com.gryphpoem.game.zw.pb.PbJsonFormat;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.DefultJob;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.p.Tech;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GmToolService {
    @Autowired
    private GmService gmService;

    @Autowired
    private MailDataManager mailDataManager;

    @Autowired
    private ChatService chatService;
    @Autowired
    private SmallIdManager smallIdManager;

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private MailService mailService;

    /**
     * ??????GM??????
     * 
     * @param making
     * @param type
     * @param channelNo
     * @param online
     * @param moldId
     * @param title
     * @param content
     * @param award
     * @param to
     * @param alv
     * @param blv
     * @param avip
     * @param bvip
     * @param partys
     * @return
     */
    public boolean sendMailLogic(String making, int type, String channelNo, String childNo, int online, int moldId, String title,
            String content, String award, String to, int alv, int blv, int avip, int bvip, String partys,int camp) {
        // ????????????
        Object[] params = null;
        if (!title.equals("") && !content.equals("")) {
            params = new String[] { title, content };
        } else if (title.equals("") && !content.equals("")) {
            params = new String[] { content };
        } else if (!title.equals("") && content.equals("")) {
            params = new String[] { title };
        }
        int now = TimeHelper.getCurrentSecond();

        // ????????????
        List<Award> awardList = new ArrayList<Award>();
        if (!CheckNull.isNullTrim(award)) {// ???????????????????????????????????????
            // ????????????|??????id|1|1,2&
            List<List<Integer>> awards = getListList(award);
            // String[] awards = award.split("&");
            for (List<Integer> item : awards) {
                // String[] item = itemStr.split("\\|");
                int item_type = item.get(0);
                int item_id = item.get(1);
                int item_num = item.get(2);

                Award en = PbHelper.createAwardPb(item_type, item_id, item_num);
                awardList.add(en);
            }
        }
        // ??????,?????????
        List<String> channelNoList = new ArrayList<>();
        String[] channelNos = channelNo.split("\\|");
        for (int i = 0; i < channelNos.length; i++) {
            channelNoList.add(channelNos[i]);
        }
        // ?????????
//        List<Integer> childNoList = new ArrayList<>();
//        String[] childNos = childNo.split("\\|");
//        for (int i = 0; i < childNos.length; i++) {
//            childNoList.add(Integer.valueOf(childNos[i]));
//        }

        // ????????????
        if (type == 1) {// ???????????????
            String[] names = to.split("\\|");
            if (!CheckNull.isEmpty(names)) {
                Player player;
                for (String nick : names) {
                    player = playerDataManager.getPlayer(nick);
                    if (null != player) {
                        if(awardList.size()>0) {
                            mailDataManager.sendAttachMail(player, awardList, moldId, AwardFrom.GM_SEND, now, params);
                        }else {
                            mailDataManager.sendNormalMail(player,moldId,now,params);
                        }
                    }
                }
            } else {
                LogUtil.error("??????????????????GM?????????????????????, to:", to);
            }
        } else if ((type == 2) || (type == 3)) { // 2 ?????????????????????, 3 ???????????????
            Iterator<Player> it;
            if (type == 2) {// ????????????
                it = playerDataManager.getPlayers().values().iterator();
            } else {// ????????????
                List<Player> partyList = new ArrayList<>();
                ConcurrentHashMap<Long, Player> playerByCamp = playerDataManager.getPlayerByCamp(camp);
                if (CheckNull.isEmpty(playerByCamp)) {
                    return false;
                }
                partyList.addAll(playerByCamp.values());
                it = partyList.iterator();
            }
            while (it.hasNext()) {
                Player player = (Player) it.next();
                if (player == null || player.account == null || !player.isActive() || player.lord == null) {
                    continue;
                }
                if ((channelNo.equals("0")) || (channelNoList.contains(player.account.getPlatNo()+","+player.account.getChildNo()))) {
                    
                    Lord lord = player.lord;
                    if (alv != 0 && lord.getLevel() < alv) {
                        continue;
                    }
                    if (blv != 0 && lord.getLevel() > blv) {
                        continue;
                    }
                    if (avip != 0 && lord.getVip() < avip) {
                        continue;
                    }
                    if (bvip != 0 && lord.getVip() > bvip) {
                        continue;
                    }
                    if (awardList.size() == 0) {
                        if (online == 0) {// ????????????
                            mailDataManager.sendNormalMail(player, moldId, TimeHelper.getCurrentSecond(), params);
                        } else if (online == 1 && (player.ctx != null)) {// ????????????
                            mailDataManager.sendNormalMail(player, moldId, TimeHelper.getCurrentSecond(), params);
                        }
                    } else {
                        if (online == 0) {// ????????????
                            mailDataManager.sendAttachMail(player, awardList, moldId, AwardFrom.GM_SEND, now, params);
                        } else if (online == 1 && player.ctx != null) {// ????????????
                            mailDataManager.sendAttachMail(player, awardList, moldId, AwardFrom.GM_SEND, now, params);
                        }
                    }
                }
            }
            return true;
        } else if (type == 4) {// ??????????????? ??????id
            String[] ids = to.split("\\|");
            if (!CheckNull.isEmpty(ids)) {
                Player player;
                for (String id : ids) {
                    player = playerDataManager.getPlayer(Long.valueOf(id));
                    if (null != player) {
                        if(awardList.size()>0) {
                            mailDataManager.sendAttachMail(player, awardList, moldId, AwardFrom.GM_SEND, now, params);
                        }else {
                            mailDataManager.sendNormalMail(player,moldId,now,params);
                        }
                    }
                }
            } else {
                LogUtil.error("?????????id??????GM?????????????????????, to:", to);
            }
        }
        return true;
    }

    /**
     * ??????????????????
     * 
     * @param
     * @return
     */
    public boolean sendNoticeLogic(HttpPb.NoticeRq req) {
        Date now = new Date();
        
        Date beginTime = DateHelper.parseDate(req.getBeginTime());
        ScheduleManager scheduleManager = ScheduleManager.getInstance();
        if (DateHelper.isAfterTime(now, beginTime)) {
            scheduleManager.addOrModifyDefultJob(DefultJob.createDefult("gmChatJob"),
                    (job) -> chatService.sendSysChatOnWorld(req.getChatId(),req.getCamp(), req.getContent()), beginTime, req.getSeconds(), req.getCount());
        }
//        chatService.sendSysChatOnWorld(Integer.parseInt(chatIdStr),campStr, content);
        return true;
    }

    public boolean forbiddenLogic(int forbiddenId, String nick, int time) {
        if (forbiddenId == 1) {
            gmService.gmSilence(nick, time);
        } else if (forbiddenId == 2) {
            gmService.gmSilence(nick, 0);
        } else if (forbiddenId == 3) {
            gmService.gmForbidden(nick, time);
            gmService.gmKick(nick);
        } else if (forbiddenId == 4) {
            gmService.gmForbidden(nick, 0);
        } else if (forbiddenId == 5) {
            gmService.gmKick(nick);
        }
        return true;
    }

    public boolean forbiddenLogic(int forbiddenId, long lordId, int time) {
        if (forbiddenId == 1) {
            Player player = playerDataManager.getPlayer(lordId);
            if (player != null && player.account.getIsGm() == 0) {
                player.lord.setSilence(time);
                int now = TimeHelper.getCurrentSecond();
                // ????????????????????????
                rewardDataManager.syncRoleResChanged(player, rewardDataManager.createChangeInfoPb(-1, time >= now || time == 1 ? 1 : 0, time));
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_SILENCE, TimeHelper.getCurrentSecond());
            }
        } else if (forbiddenId == 2) {
            Player player = playerDataManager.getPlayer(lordId);
            if (player != null && player.account.getIsGm() == 0) {
                player.lord.setSilence(0);
                // ????????????????????????
                rewardDataManager.syncRoleResChanged(player, rewardDataManager.createChangeInfoPb(-1,0, 0));
            }
        } else if (forbiddenId == 3) {
            Player player = playerDataManager.getPlayer(lordId);
            if (player != null && player.account.getIsGm() == 0) {
                player.account.setForbid(time);
                // TODO: 2020/10/14 ???????????????????????????
                // player.account.setForbidDesc("");
                if (player.isLogin && player.account.getIsGm() == 0 && player.ctx != null) {
                    player.ctx.close();
                }
                chatService.deleteRoleChat(player);
                mailService.deleteCampMail(player.roleId, MailConstant.MOLD_CAMP_MAIL);
            }
        } else if (forbiddenId == 4) {
            Player player = playerDataManager.getPlayer(lordId);
            if (player != null && player.account.getIsGm() == 0) {
                player.account.setForbid(0);
            }
        } else if (forbiddenId == 5) {
            Player player = playerDataManager.getPlayer(lordId);
            if (player != null && player.account.getIsGm() == 0) {
                if (player.isLogin && player.account.getIsGm() == 0 && player.ctx != null) {
                    player.ctx.close();
                }
            }
        }
        return true;
    }

    // public void getLordBase(final GetLordBaseRq req, final ServerHandler handler) {
    // GameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
    // @Override
    // public void action() {
    // String marking = req.getMarking();
    // long lordId = req.getLordId();
    // int type = req.getType();
    // backLordBaseLogic(marking, lordId, type);
    // }
    // }, DealType.MAIN);
    //
    // }

    /**
     * ??????????????????
     * 
     * @param markging
     * @param lordId
     * @param type 1.???????????? 2.???????????? 3.?????? 4.???????????? 5.???????????? 6.??????
     * @return
     */
    public Base.Builder backLordBaseLogic(String markging, long lordId, int type, List<String> params) {
        Player player = playerDataManager.getPlayer(lordId);
        BackLordBaseRq.Builder builder = BackLordBaseRq.newBuilder();
        builder.setMarking(markging);
        builder.setType(type);
        if(type == 99){ ////??????????????????????????????
            builder.setCode(200);
            PlayerInfo.Builder playerInfoBuilder = PlayerInfo.newBuilder();
            fillPlayerInfoData(player, playerInfoBuilder, type, params);
            builder.setPlayInfo(playerInfoBuilder.build());
            Base.Builder baseBuilder = PbHelper.createRqBase(BackLordBaseRq.EXT_FIELD_NUMBER, null, BackLordBaseRq.ext,
                    builder.build());
            return baseBuilder;
        }
        if (player == null) {
            builder.setCode(1);
            Base.Builder baseBuilder = PbHelper.createRqBase(BackLordBaseRq.EXT_FIELD_NUMBER, null, BackLordBaseRq.ext,
                    builder.build());
            return baseBuilder;
        } else {
            builder.setCode(200);
            PlayerInfo.Builder playerInfoBuilder = PlayerInfo.newBuilder();
            fillPlayerInfoData(player, playerInfoBuilder, type, params);
            builder.setPlayInfo(playerInfoBuilder.build());
        }
        Base.Builder baseBuilder = PbHelper.createRqBase(BackLordBaseRq.EXT_FIELD_NUMBER, null, BackLordBaseRq.ext,
                builder.build());
        return baseBuilder;
    }

    /**
     * ??????playerInfo??????
     * 
     * @param builder
     * @param type 1.???????????? 2.???????????? 3.?????? 4.???????????? 5.???????????? 6.??????
     */
    private void fillPlayerInfoData(Player player, PlayerInfo.Builder builder, int type, List<String> params) {
        switch (type) {
            case 1:// ????????????
                player.props.forEach((k, v) -> {
                    CommonPb.Prop prop = PbHelper.createPropPb(v);
                    builder.addProp(prop);
                });
                break;
            case 2:// ??????
                player.equips.forEach((k, v) -> {
                    CommonPb.Equip equip = PbHelper.createEquipPb(v);
                    builder.addEquip(equip);
                });
                break;
            case 3:// ??????
                player.heros.forEach((k, v) -> {
                    CommonPb.Hero hero = PbHelper.createHeroPb(v, player);
                    builder.addHero(hero);
                });
                break;
            case 4:// ????????????
                player.buildingExts.values().forEach(buildExt -> {// ????????????
                    builder.addBuild(PbHelper.createBuildingBaseByExtPb(buildExt));
                });
                player.mills.values().forEach(mill -> {// ????????????
                    builder.addMill(PbHelper.createMillPb(mill));
                });
                break;
            case 5:// ????????????
                player.supEquips.forEach((k, v) -> {
                    builder.addSuperEquip(PbHelper.createSuperEquipPb(v));
                });
                break;
            case 6:// ??????
                Tech tech = player.tech;
                if (tech != null) {
                    tech.getTechLv().forEach((k, techLv) -> {
                        builder.addTech(PbHelper.createTechLv(techLv));
                    });
                }
            case 7: // JSON????????????
                String ext = getJsonByParam(player, params);
                builder.setExt(ext);
                break;
            case 99: //??????????????????????????????
                Map<Integer, Integer> rebelNextOpenMap = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.REBEL_NEXT_OPEN_TIME);
                int rebelNextTime = rebelNextOpenMap.getOrDefault(0, 0);
                Map<Integer, Integer> airshipNextOpenMap = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.AIR_SHIP_NEXT_OPEN_TIME);
                int airshipNextTime = airshipNextOpenMap.getOrDefault(0, 0);
                Map<Integer, Integer> counterAtkNextOpenMap = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.COUNTER_ATK_NEXT_OPEN_TIME);
                int counterAtkNextTime = counterAtkNextOpenMap.getOrDefault(0, 0);
                String nextTime = "{rebel:"+rebelNextTime+",airship:"+airshipNextTime+",counterAtk:"+counterAtkNextTime+"}";
                builder.setExt(nextTime);
                break;
            default:
                break;
        }
    }

    private static final String SER_PACK_NAME = "com.gryphpoem.game.zw.pb.SerializePb$";
    private static final String PARESE_METHOD_NAME = "parseFrom";
    private static final String NULL_JSON = "{}";

    private String getJsonByParam(Player player, List<String> params) {
        if (CheckNull.isEmpty(params)) {
            return NULL_JSON;
        }
        try {
            String model = params.get(0);
            if(model.equals("lord")){
                return player.lord.getSilence()+","+player.account.getForbid();
            }
            Method method = ReflectionUtils.findMethod(Player.class, StrUtils.firstLowerCase(model));
            method.setAccessible(true);
            byte[] data = (byte[]) ReflectionUtils.invokeMethod(method, player);
            String clzzName = SER_PACK_NAME + StrUtils.firstUpperCase(model);
            LogUtil.debug("??????????????? clzzName :", clzzName);
            Class<?> serClass = Class.forName(clzzName);
            GeneratedMessage gMsg = (GeneratedMessage) ReflectUtil.invokeStaticMethod(serClass, PARESE_METHOD_NAME,
                    data);
            if (params.size() == 1) {
                return JsonFormat.printToString(gMsg);
            } else {
                String[] array = new String[params.size()];
                params.toArray(array);
                String[] dest = new String[array.length - 1];
                System.arraycopy(array, 1, dest, 0, dest.length);
                return PbJsonFormat.printToString(gMsg, dest);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("????????????????????????JSON??????,????????????:", e);
        }
        return NULL_JSON;
    }

    private List<List<Integer>> getListList(String columnValue) {
        List<List<Integer>> listList = new ArrayList<List<Integer>>();
        if (columnValue == null || columnValue.isEmpty()) {
            return listList;
        }

        // JSONArray arrays = JSONArray.fromObject(columnValue);
        try {
            JSONArray arrays = JSONArray.parseArray(columnValue);
            for (int i = 0; i < arrays.size(); i++) {
                List<Integer> list = new ArrayList<Integer>();
                JSONArray array = arrays.getJSONArray(i);
                for (int j = 0; j < array.size(); j++) {
                    list.add(array.getInteger(j));
                }

                // if (!list.isEmpty()) {
                listList.add(list);
                // }
            }
        } catch (Exception e) {
            // System.out.println("ListListTypeHandler parse:" + columnValue);
            LogUtil.error("???????????? columnValue:", columnValue);
        }

        return listList;
    }

    public boolean modVipLogic(long lordId, int type, int value) {
        Player player = playerDataManager.getPlayer(lordId);
        System.out.println("lordId" + lordId + "type" + type + "value" + value);
        if (player == null) {
            LogUtil.error("??????????????? " + "lordId:" + lordId + ", type:" + type + ", value:" + value);
            return false;
        }
        if (type == 1) {// ??????VIP
            if (value >= 0 && value <= StaticVipDataMgr.getMaxVipLv()) {
                player.lord.setVipExp(StaticVipDataMgr.getVipMap(value).getExp());
//                player.lord.setVip(value);
                vipDataManager.setVip(player,value);
            }
        } else if (type == 2) {// ????????????
            if (value >= 0) {
                player.lord.setVipExp(value);
//                player.lord.setVip(StaticVipDataMgr.calcVip(player.lord.getVipExp()));
                vipDataManager.setVip(player,StaticVipDataMgr.calcVip(player.lord.getVipExp()));
            }
        }
        // ??????????????????
        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(AwardType.MONEY, AwardType.Money.VIP_EXP);
        rewardDataManager.syncRoleResChanged(player, change);
        return true;
    }

    public boolean modLordLogic(long lordId, int type, String keyId, String value, String value2) {
        Player player = playerDataManager.getPlayer(lordId);
        if (player == null) {
            return true;
        }
        switch (type) {
            // ???????????????????????????
            case ModLordConstant.MOD_LORD_SIGNATURE:
                if (!CheckNull.isNullTrim(value)) {
                    player.lord.setSignature(value);
                }
                break;
            default:
                break;
        }
        return true;
    }

    public boolean modPropLogic(long lordId, int type, String props) {
        // Player player = playerDataManager.getPlayer(lordId);
        List<List<Integer>> listList = new ArrayList<List<Integer>>();
        if (props == null || props.isEmpty()) {
            LogUtil.error("modPropLogic error  props:" + props);
            return false;
        }
        try {
            JSONArray arrays = JSONArray.parseArray(props);
            for (int i = 0; i < arrays.size(); i++) {
                List<Integer> list = new ArrayList<Integer>();
                JSONArray array = arrays.getJSONArray(i);
                for (int j = 0; j < array.size(); j++) {
                    list.add(array.getInteger(j));
                }
                listList.add(list);
            }
        } catch (Exception e) {
            LogUtil.error("modPropLogic error  props:" + props);
            throw e;
        }
        // if (type == -1) { // ????????????
        // for (List<Integer> list : listList) {
        // if (!rewardDataManager.checkPropIsEnough(player, list.get(1), list.get(2), "");) {
        // LogUtil.error("modPropLogic prope not enougth :"
        // + list.get(0) + "_" + list.get(1) + "_"
        // + list.get(2));
        // return false;
        // }
        // }
        // for (List<Integer> list : listList) {
        // rewardDataManager.subProp(player, list.get(0), list.get(1),
        // list.get(2), AwardFrom.INNER_MOD_PROPS);
        // }
        // } else if (type == 1) { // ????????????
        // rewardDataManager.addAwardList(player, listList,
        // AwardFrom.INNER_MOD_PROPS);
        // }
        // rewardDataManager.synInnerModPropsToPlayer(player, type, listList);
        return true;
    }

    public boolean modNameLogic(long lordId, String name) {
        Player player = playerDataManager.getPlayer(lordId);
        if (name == null || name.isEmpty() || name.length() >= 12) {
            LogUtil.error("modNameLogic error  name:" + name);
            return false;
        }

        if (EmojiHelper.containsEmoji(name)) {
            LogUtil.error("modNameLogic error invalid char");
            return false;
        }

        if (!playerDataManager.takeNick(name)) {
            LogUtil.error("modNameLogic error same name");
            return false;
        }
        playerDataManager.rename(player, name);
        return true;
    }

    /**
     * ??????????????????
     * 
     * @param lordId ????????????
     * @param lv ???????????????
     */
    public void processSmallIdLogic(long lordId, int lv) {
        Iterator<Player> it = playerDataManager.getPlayers().values().iterator();
        int nowDay = TimeHelper.getCurrentDay();
        if (lordId > 0) {
            Player player = playerDataManager.getPlayer(lordId);
            smallIdManager.addSmallIdOnline(player);
        } else {
            while (it.hasNext()) {
                Player player = (Player) it.next();
                if (player == null || player.account == null || !player.isActive() || player.lord == null) {
                    continue;
                }
                Lord lord = player.lord;
                if (lord == null) {
                    continue;
                }
                if (lord.getTopup() > 0 || lord.getLevel() > lv) {
                    continue;
                }
                int offTime = TimeHelper.getDay(lord.getOffTime());
                if (lord.getOffTime() > 0 && nowDay - offTime <= Constant.SMALL_LOGIN_DAY) {
                    continue;
                }
                smallIdManager.addSmallIdOnline(player);
            }
        }
    }

    /**
     * ??????????????????????????????????????? ?????????????????????????????? global
     */
    public void resetAllPlayerWorldData() {
        worldDataManager.initNewWorldData();
        Iterator<Player> it = playerDataManager.getPlayers().values().iterator();
        worldDataManager.clearAllPlayerData();
        while (it.hasNext()) {
            Player player = (Player) it.next();
            if (player == null || player.account == null || player.lord == null || player.lord.getPos() < 0) {
                continue;
            }
            try {
                playerDataManager.removeAreaPlayer(player.lord.getPos(), player);
                player.armys.clear();
                player.battleMap.clear();
                player.acquisiteQue.clear();
                for (Hero h : player.heros.values()) {
                    h.setState(HeroConstant.HERO_STATE_IDLE);
                    h.setStatus(HeroConstant.HERO_STATUS_IDLE);
                }
                for (int heroId : player.heroBattle) {
                    Hero h = player.heros.get(heroId);
                    if (null != h) {
                        h.setState(HeroConstant.HERO_STATE_IDLE);
                        h.setStatus(HeroConstant.HERO_STATUS_BATTLE);
                    }
                }
                for (int heroId : player.heroWall) {
                    Hero h = player.heros.get(heroId);
                    if (null != h) {
                        h.setState(HeroConstant.HERO_STATE_IDLE);
                        h.setStatus(HeroConstant.HERO_STATUS_WALL_BATTLE);
                    }
                }
                for (int heroId : player.heroAcq) {
                    Hero h = player.heros.get(heroId);
                    if (null != h) {
                        h.setState(HeroConstant.HERO_STATE_IDLE);
                        h.setStatus(HeroConstant.HERO_STATUS_COLLECT);
                    }
                }
                player.lord.setArea(-1);
                player.lord.setPos(-1);
                worldDataManager.addNewPlayer(player, false);
                playerDataManager.addPlayer(player);
            } catch (Exception e) {
                LogUtil.error("resetAllPlayerWorldData lord=" + player.lord.getLordId(), e);
            }

        }
    }
}
