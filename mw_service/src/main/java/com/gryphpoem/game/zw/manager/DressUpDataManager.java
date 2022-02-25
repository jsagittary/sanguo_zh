package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.gameplay.local.util.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCastleSkin;
import com.gryphpoem.game.zw.resource.domain.s.StaticChatBubble;
import com.gryphpoem.game.zw.resource.domain.s.StaticPortrait;
import com.gryphpoem.game.zw.resource.domain.s.StaticTitle;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.dressup.*;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.rpc.DubboRpcService;
import com.gryphpoem.game.zw.service.TitleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/3/4 16:26
 */
@Component
public class DressUpDataManager implements DelayInvokeEnvironment {

    /**
     * 创建事件
     */
    public static final int CREATE_EVENT = 1;
    /**
     * 更新事件
     */
    public static final int UPDATE_EVENT = 2;
    /**
     * 删除事件
     */
    public static final int DELETE_EVENT = 3;

    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private TitleService titleService;

    @Autowired
    private MailDataManager mailDataManager;

    /**
     * 装扮的延迟队列
     */
    private DelayQueue<RemoveDressUpDelayRun> delayBattleQueue = new DelayQueue<>(this);


    @Override
    public DelayQueue getDelayQueue() {
        return delayBattleQueue;
    }


    /**
     * 根据奖励类型获取装扮数据
     *
     * @param player 玩家对象
     * @param type   奖励类型
     * @return 装扮数据
     */
    public Map<Integer, BaseDressUpEntity> getDressUpByType(Player player, int type) {
        if (Objects.isNull(player) && !AwardType.DRESS_UP_TYPE.contains(type)) {
            return null;
        }
        DressUp dressUp = player.getDressUp();
        if (Objects.nonNull(dressUp)) {
            Map<Integer, BaseDressUpEntity> dressUpMap = dressUp.getDressUpEntityMapByType(type);
            if (dressUpMap.isEmpty()) {
                // 转换历史数据
                convertOldData(player, type, dressUpMap);
            }
            // 检测装扮解锁和默认装扮
            checkDressUpUnlock(player, type, dressUpMap);
            return dressUpMap;
        }
        return null;
    }

    /**
     * 推送装扮数据变动
     *
     * @param player        玩家对象
     * @param dressUpEntity 装扮数据
     * @param curEvent      变动事件
     */
    public void syncDressUp(Player player, BaseDressUpEntity dressUpEntity, int curEvent) {
        if (player.ctx != null && player.isLogin) {
            GamePb4.SyncDressUpRs.Builder builder = GamePb4.SyncDressUpRs.newBuilder();
            builder.setCurdEvent(curEvent);
            int type = dressUpEntity.getType();
            builder.setCurdType(type);
            builder.setData(dressUpEntity.toData());
            builder.setCurId(curDressUpId(player, type));
            BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb4.SyncDressUpRs.EXT_FIELD_NUMBER, GamePb4.SyncDressUpRs.ext, builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 获取执行装扮类型当前使用装扮
     *
     * @param player 玩家对象
     * @param type   装扮类型
     * @return 当前使用装扮
     */
    public int curDressUpId(Player player, int type) {
        int curId = 0;
        if (Objects.isNull(player)) {
            return curId;
        }
        DressUp dressUp = player.getDressUp();
        if (type == AwardType.PORTRAIT) {
            curId = player.lord.getPortrait();
        } else if (type == AwardType.CASTLE_SKIN) {
            curId = player.getCurCastleSkin();
        } else if (type == AwardType.PORTRAIT_FRAME) {
            curId = dressUp.getCurPortraitFrame();
        } else if (type == AwardType.NAMEPLATE) {
            curId = dressUp.getCurNamePlate();
        } else if (type == AwardType.MARCH_SPECIAL_EFFECTS) {
            curId = dressUp.getCurMarchEffect();
        } else if(type == AwardType.CHAT_BUBBLE){
            curId = dressUp.getCurrChatBubble();
        } else if(type == AwardType.TITLE){
            curId = dressUp.getCurTitle();
        }
        return curId;
    }

    /**
     * 购买气泡 没有或者是时效的 就可以购买
     * @param player
     * @param type
     * @param id
     * @return
     */
    public boolean checkCanBuy(Player player,int type,int id) {
        Map<Integer, BaseDressUpEntity> entityMap = getDressUpByType(player, type);
        if (Objects.nonNull(entityMap)) {
            BaseDressUpEntity dressUpEntity = entityMap.get(id);
            if (Objects.nonNull(dressUpEntity) && dressUpEntity.isPermanentHas()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 添加装扮和拥有时间
     *
     * @param player   玩家对象
     * @param type     奖励类型
     * @param id       奖励id
     * @param duration 增加的拥有时间, 1 设置为永久拥有, 大于1为拥有秒数
     * @param from     来源
     * @param param    参数
     */
    public void addDressUp(Player player, int type, int id, long duration, List<CommonPb.Award> convert, AwardFrom from, Object... param) {
        int addTime = (int) duration;
        if (Objects.isNull(player) || addTime < 1) {
            LogUtil.error("限时时间配置出错，跳过奖励, roleId:", player.roleId, ", type:", type, ", id:", id, ", addTime:", addTime, ", from:", from.getCode());
            return;
        }
        // 获取装扮类型数据
        Map<Integer, BaseDressUpEntity> entityMap = getDressUpByType(player, type);
        if (Objects.isNull(entityMap)) {
            return;
        }
        // 装扮对象
        BaseDressUpEntity dressUpEntity = entityMap.get(id);

        // 已经拥有装扮
        if (Objects.nonNull(dressUpEntity)) {
            if (dressUpEntity.isPermanentHas()) {
                // 转换道具
                List<List<Integer>> convertProps = dressUpEntity.convertProps();
                if (!CheckNull.isEmpty(convertProps)) {
                    // 转换后的奖励
                    convert.addAll(rewardDataManager.sendReward(player, convertProps, AwardFrom.DRESS_UP_CONVERT_PROPS));
                }
            } else {
                // 设置拥有时间
                if (type == AwardType.TITLE) {
                    StaticTitle staticTitle = StaticLordDataMgr.getTitleMapById(id);
                    try {
                        if(staticTitle.getDuration()>0){
                            dressUpEntity.addDuration(Math.toIntExact(staticTitle.getDuration()));
                        }else {
                            dressUpEntity.setPermanentHas(true);
                            dressUpEntity.setDuration(0);
                        }
                    }catch (Exception e){
                        LogUtil.error("称号解锁出错, roleId:", player.roleId, ", type:", type, ", id:", id, ", addTime:", addTime, ", from:", from.getCode());
                        return;
                    }
                }else{
                    dressUpEntity.addDuration(addTime);
                }
                // 同步变动
                syncDressUp(player, dressUpEntity, UPDATE_EVENT);
                //获得新装扮时刷新战力
                CalculateUtil.reCalcBattleHeroAttr(player);
            }
            // 日志埋点
            if(type == AwardType.TITLE){
                StaticTitle staticTitle = StaticLordDataMgr.getTitleMapById(id);
                if(staticTitle.getDuration()>0){
                    LogLordHelper.dressUp(player, from, dressUpEntity, "UpdateTitle", addTime);
                }else {
                    LogLordHelper.dressUp(player, from, dressUpEntity, "CreateTitle", addTime, param);
                }
            }else {
                LogLordHelper.dressUp(player, from, dressUpEntity, "UpdateDressUp", addTime);
            }
        } else {
            if (type == AwardType.PORTRAIT) {
                dressUpEntity = new PortraitEntity(id, addTime);
//                LogLordHelper.portrait(from, player.account, player.lord, id, addTime, param);
            } else if (type == AwardType.CASTLE_SKIN) {
                dressUpEntity = new CastleSkinEntity(id, addTime);
                // 设置初始星级
                StaticCastleSkin skin_ = StaticLordDataMgr.getCastleSkinMapById(id);
                CastleSkinEntity castleSkinEntity = (CastleSkinEntity) dressUpEntity;
                castleSkinEntity.setStar(skin_.getStar());
                //数数上报
//                LogLordHelper.commonLog("castleSkin", from, player.account, player.lord, id, addTime, param);
            } else if (type == AwardType.PORTRAIT_FRAME) {
                dressUpEntity = new PortraitFrameEntity(id, addTime);
                entityMap.put(id, dressUpEntity);
            } else if (type == AwardType.NAMEPLATE) {
                dressUpEntity = new NameplateEntity(id, addTime);
            } else if (type == AwardType.MARCH_SPECIAL_EFFECTS) {
                dressUpEntity = new MarchSpecialEffectEntity(id, addTime);
            } else if(type == AwardType.CHAT_BUBBLE){
                dressUpEntity = new ChatBubbleEntity(id,addTime);
            } else if(type == AwardType.TITLE){
                dressUpEntity = new TitleEntity(id,false);
                if(addTime>1){
                    dressUpEntity.setDuration(TimeHelper.getCurrentSecond()+addTime);
                }
            }
            if (Objects.isNull(dressUpEntity)) {
                return;
            }
            // 存储装扮数据
            entityMap.put(id, dressUpEntity);
            // 日志埋点
            if(type == AwardType.TITLE){
                LogLordHelper.dressUp(player, from, dressUpEntity, "CreateTitle", addTime, param);
            }else {
                LogLordHelper.dressUp(player, from, dressUpEntity, "CreateDressUp", addTime, param);
            }
            // 同步变动
            syncDressUp(player, dressUpEntity, CREATE_EVENT);
            if (type == AwardType.CASTLE_SKIN) {
                //获得装扮-刷新主城皮肤任务进度
                titleService.processTask(player, ETask.CASTLE_SKIN_NUM);
            }
            //获得新装扮时刷新战力
            CalculateUtil.reCalcBattleHeroAttr(player);
        }
        // 延时移除
        if (addTime != 1 && !dressUpEntity.isPermanentHas()) {
            delayBattleQueue.add(new RemoveDressUpDelayRun(player, dressUpEntity));
        }
    }

    /**
     * 减少装扮和拥有时间
     *
     * @param player  玩家对象
     * @param type    奖励类型
     * @param id      奖励id
     * @param subTime 增加的拥有时间, 1 设置为永久拥有, 大于1为拥有秒数
     * @param from    来源
     * @param param   参数
     */
    public void subDressUp(Player player, int type, int id, long subTime, AwardFrom from, Object... param) {
        if (Objects.isNull(player) || subTime < 0) {
            LogUtil.error("限时时间配置出错，跳过奖励, roleId:", player.roleId, ", type:", type, ", id:", id, ", addTime:", subTime, ", from:", from.getCode());
            return;
        }
        Map<Integer, BaseDressUpEntity> entityMap = getDressUpByType(player, type);
        if (Objects.isNull(entityMap)) {
            return;
        }

        // 装扮对象
        BaseDressUpEntity dressUpEntity = entityMap.get(id);
        if (Objects.isNull(dressUpEntity)) {
            return;
        }

        // 外观
        DressUp dressUp = player.getDressUp();

        // 修改拥有状态
        if(type==AwardType.TITLE){
            if (dressUpEntity.isPermanentHas()) {
                dressUpEntity.setPermanentHas(false);
            }
            if(subTime>0){
                dressUpEntity.subDuration((int) subTime);
            }else{
                //如果传入的时间不大于0，说明是不限时的称号去除。
                dressUpEntity.setDuration(0);
            }
        }else {
            if (dressUpEntity.isPermanentHas()) {
                dressUpEntity.setPermanentHas(false);
            } else {
                dressUpEntity.subDuration((int) subTime);
            }
        }

        // 删除标记
        boolean delete = false;
        // 如果既不是永久拥有, 限时时间又到达了
        if (!dressUpEntity.isPermanentHas() && dressUpEntity.getDuration() <= 0) {
            // 恢复默认装扮
            if (type == AwardType.PORTRAIT) {
                if (player.lord.getPortrait() == id) {
                    player.lord.setPortrait(1);
                }
                if(player.portraits.contains(id)){
                    player.portraits.remove(id);
                }
            } else if (type == AwardType.CASTLE_SKIN) {
                if (player.getCurCastleSkin() == id) {
                    player.setCurCastleSkin(StaticCastleSkin.DEFAULT_SKIN_ID);
                }
            } else if (type == AwardType.PORTRAIT_FRAME) {
                if (dressUp.getCurPortraitFrame() == id) {
                    dressUp.setCurPortraitFrame(Constant.DEFAULT_PORTRAIT_FRAME_ID);
                }
            } else if (type == AwardType.NAMEPLATE) {
                if (dressUp.getCurNamePlate() == id) {
                    dressUp.setCurNamePlate(Constant.DEFAULT_NAME_PLATE_ID);
                }
            } else if(type == AwardType.CHAT_BUBBLE){
                if(dressUp.getCurrChatBubble() == id){
                    dressUp.setCurrChatBubble(0);
                }
            } else if (type == AwardType.MARCH_SPECIAL_EFFECTS) {
                if (dressUp.getCurMarchEffect() == id) {
                    dressUp.setCurMarchEffect(Constant.DEFAULT_MARCH_LINE_ID);
                }
            } else if(type == AwardType.TITLE){
                if(dressUp.getCurTitle() == id){
                    dressUp.setCurTitle(0);
                }
            }
            int entityId = dressUpEntity.getId();
            // 删除装扮
            entityMap.remove(dressUpEntity.getId());
            if(type==AwardType.TITLE){
                //如果是称号,需要将一个新的未解锁的添加进去
                if (null == entityMap.get(entityId)) {
                    TitleEntity titleEntity = new TitleEntity(entityId, false);
                    entityMap.put(entityId, titleEntity);
                }
                StaticTitle title = StaticLordDataMgr.getTitleMapById(id);
                if(title.getDuration()>0){
                    mailDataManager.sendNormalMail(player,MailConstant.TITLE_TIME_IS_EXPIRED, TimeHelper.getCurrentSecond(),title.getId());
                }
            }
            delete = true;
            if (player.isLogin) {
                // 登录 推送皮肤更换
                pushPlayerCastleSkinChange(player);
            }
            if(type == AwardType.CASTLE_SKIN){
                //清除装扮-刷新主城皮肤任务进度
                titleService.processTask(player, ETask.CASTLE_SKIN_NUM);
            }
            // 日志埋点
            if(type == AwardType.TITLE){
                LogLordHelper.dressUp(player, from, dressUpEntity, "clearTitle", subTime, param);
            }else {
                LogLordHelper.dressUp(player, from, dressUpEntity, "clearDressUp", subTime, param);
            }
            // 重新计算战力
            CalculateUtil.reCalcAllHeroAttr(player);
            EventDataUp.prop(from, player.account, player.lord, id, type, 1, Constant.ACTION_SUB,id);
            DubboRpcService dubboRpcService = DataResource.ac.getBean(DubboRpcService.class);
            dubboRpcService.updatePlayerLord2CrossPlayerServer(player);
        }
        // 同步变动
        syncDressUp(player, dressUpEntity, delete ? DELETE_EVENT : UPDATE_EVENT);
    }

    /**
     * 玩家皮肤状态更改
     *
     * @param player 玩家对象
     */
    public void pushPlayerCastleSkinChange(Player player) {
        int area = player.lord.getArea();
        int pos = player.lord.getPos();
        if (area > WorldConstant.AREA_MAX_ID) {
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(area);
            if (cMap != null) {
                cMap.publishMapEvent(MapEvent.mapEntity(pos, MapCurdEvent.UPDATE));
            }
        } else {
            EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(Collections.singletonList(pos), Events.AreaChangeNoticeEvent.MAP_TYPE));
        }
    }


    /**
     * 主动检测装扮是否解锁
     *
     * @param player     玩家对象
     * @param type       装扮类型
     * @param dressUpMap 装扮数据
     */
    private void checkDressUpUnlock(Player player, int type, Map<Integer, BaseDressUpEntity> dressUpMap) {
        if (type == AwardType.PORTRAIT) {
            // 检测将领解锁的头像
            StaticLordDataMgr.getPortraitByUnlock(StaticPortrait.UNLOCK_HERO)
                    .stream()
                    .filter(sp -> {
                        try {
                            if (!dressUpMap.containsKey(sp.getId())) {
                                checkPlayerHasDress(player, type, sp.getId(), dressUpMap);
                                return true;
                            }
                        } catch (MwException e) {
                            return false;
                        }
                        return false;
                        // 永久解锁头像
                    }).forEach(sp -> dressUpMap.put(sp.getId(), new PortraitEntity(sp.getId(), true)));
            // 检测默认头像
            // 默认解锁的头像, 服务器不做初始化
//            if (!dressUpMap.containsKey(1)) {
//                dressUpMap.put(1, new PortraitEntity(1, true));
//            }
        } else if (type == AwardType.CASTLE_SKIN) {
            // 检测默认城池皮肤
            if (!dressUpMap.containsKey(StaticCastleSkin.DEFAULT_SKIN_ID)) {
                dressUpMap.put(StaticCastleSkin.DEFAULT_SKIN_ID, new CastleSkinEntity(StaticCastleSkin.DEFAULT_SKIN_ID, true));
            }
        } else if (type == AwardType.PORTRAIT_FRAME) {
            // 检测默认头像框
            if (!dressUpMap.containsKey(Constant.DEFAULT_PORTRAIT_FRAME_ID)) {
                dressUpMap.put(Constant.DEFAULT_PORTRAIT_FRAME_ID, new PortraitFrameEntity(Constant.DEFAULT_PORTRAIT_FRAME_ID, true));
            }
        } else if (type == AwardType.NAMEPLATE) {
            // 检测默认铭牌
            if (!dressUpMap.containsKey(Constant.DEFAULT_NAME_PLATE_ID)) {
                dressUpMap.put(Constant.DEFAULT_NAME_PLATE_ID, new NameplateEntity(Constant.DEFAULT_NAME_PLATE_ID, true));
            }
        } else if (type == AwardType.MARCH_SPECIAL_EFFECTS) {
            // 检测默认行军特效
            if (!dressUpMap.containsKey(Constant.DEFAULT_NAME_PLATE_ID)) {
                dressUpMap.put(Constant.DEFAULT_MARCH_LINE_ID, new MarchSpecialEffectEntity(Constant.DEFAULT_MARCH_LINE_ID, true));
            }
        } else if(type == AwardType.CHAT_BUBBLE){
            // 添加默认气泡
            if(!dressUpMap.containsKey(0)){
                dressUpMap.put(0,new ChatBubbleEntity(0,true));
            }
        }
    }


    /**
     * 检测装扮
     *
     * @param player     玩家对象
     * @param type       装扮类型
     * @param id         装扮配置id
     * @param dressUpMap 装扮数据
     * @throws MwException 自定义异常
     */
    public void checkPlayerHasDress(Player player, int type, int id, Map<Integer, BaseDressUpEntity> dressUpMap) throws MwException {
        if (type == AwardType.PORTRAIT) {
            StaticPortrait sPortrait = StaticLordDataMgr.getPortrait(id);
            if (sPortrait == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), String.format("未配置此头像, roleId: %s, portraitId: %s", player.roleId, id));
            }
            if (sPortrait.getUnlock() == StaticPortrait.UNLOCK_FREE) { // 免费头像
                // 非初始头像,需要检测是否获取
            } else if (sPortrait.getUnlock() == StaticPortrait.UNLOCK_VIP_GIFT_BUY
                    || sPortrait.getUnlock() == StaticPortrait.UNLOCK_TYPE_WINNER
                    || sPortrait.getUnlock() == StaticPortrait.UNLOCK_CROSS_WAR_FIRE) { // 需要拥有的
                if (!dressUpMap.containsKey(id)) {
                    throw new MwException(GameError.NOT_HAS_PORTRAIT.getCode(), String.format("未拥有此头像, roleId: %s, portraitId: %s", player.roleId, id));
                }
            } else commonCheckPortrait(player, id, sPortrait);
        }else if (type == AwardType.TITLE){
            StaticTitle staticTitle = StaticLordDataMgr.getTitleMapById(id);
            if(null==staticTitle){
                throw new MwException(GameError.NO_CONFIG.getCode(), String.format("未配置此称号, roleId: %s, portraitId: %s", player.roleId, id));
            }
            if(staticTitle.getTaskId()>0){
                //检查任务完成数量
            }else{
                //活动或者奖励获得
            }

        }

    }

    /**
     * 服务器起服处理
     */
    public void processPlayerTimerDressUp() {
        Optional.ofNullable(playerDataManager.getAllPlayer())
                .ifPresent(ps ->
                        ps.values().forEach(player -> {
                            Optional.ofNullable(player.getDressUp())
                                    .ifPresent(dressUp -> {
                                        List<BaseDressUpEntity> needDelayRun = dressUp.getAllDressUpEntity()
                                                .values()
                                                .stream()
                                                .filter(map -> !CheckNull.isEmpty(map))
                                                .flatMap(map -> map.values().stream())
                                                .filter(entity -> !entity.isPermanentHas() && entity.getDuration() > 0)
                                                .collect(Collectors.toList());
                                        for (BaseDressUpEntity entity : needDelayRun) {
                                            // 把所有临时装扮加入延时执行, 如果已经过了执行时间, 也同样会执行移除操作, 这里偷懒没有先移除过期的装扮
                                            delayBattleQueue.add(new RemoveDressUpDelayRun(player, entity));
                                        }
                                    });
                        })
                );
    }

    /**
     * 转换历史数据
     *
     * @param player                 玩家数据
     * @param type                   类型
     * @param dressUpEntityMapByType 新的数据存放
     */
    private void convertOldData(Player player, int type, Map<Integer, BaseDressUpEntity> dressUpEntityMapByType) {
        if (dressUpEntityMapByType.isEmpty()) {
            if (type == AwardType.PORTRAIT) {
                // 老的检测解锁的头像
                oldCheckPlayerUnlockPortrait(player);
                // 所拥有的头像
                player.portraits.stream()
                        .map(portrait -> {
                            // 之前拥有的头像, 都是永久存在的
                            return new PortraitEntity(portrait, true);
                        }).forEach(entity -> dressUpEntityMapByType.put(entity.getId(), entity));
            } else if (type == AwardType.CASTLE_SKIN) {
                // 所拥有的城堡皮肤
                player.getOwnCastleSkin().stream()
                        .map(castleSkin -> {
                            CastleSkinEntity castleSkinEntity = new CastleSkinEntity(castleSkin);
                            // 之前有星级
                            castleSkinEntity.setStar(player.getOwnCastleSkinStar().getOrDefault(castleSkin, 0));
                            int time = player.getOwnCastleSkinTime().getOrDefault(castleSkin, 0);
                            if (time > 0) {
                                // 之前有限时皮肤
                                castleSkinEntity.setDuration(time);
                            } else {
                                castleSkinEntity.setPermanentHas(true);
                            }
                            return castleSkinEntity;
                        }).forEach(entity -> dressUpEntityMapByType.put(entity.getId(), entity));

            } else if(type == AwardType.CHAT_BUBBLE){
                player.getChatBubbles().stream().map(id -> new ChatBubbleEntity(id,true)).forEach(entity -> dressUpEntityMapByType.put(entity.getId(),entity));
            }
        }
    }

    /**
     * 检测玩家解锁获取的头像
     *
     * @param player
     */
    @Deprecated
    public void oldCheckPlayerUnlockPortrait(Player player) {
        long roleId = player.roleId;
        // 检测将领解锁的头像
        StaticLordDataMgr.getPortraitByUnlock(StaticPortrait.UNLOCK_HERO)
                .stream()
                .filter(sp -> {
                    try {
                        if (!player.portraits.contains(sp.getId())) {
                            // 检测玩家是否拥有该头像
                            oldCheckPlayerHasPortrait(player, sp.getId());
                            return true;
                        }
                    } catch (MwException e) {
                        return false;
                    }
                    return false;
                }).forEach(sp -> {
            player.portraits.add(sp.getId());
        });
        // 获得霸主头像
//        Set<Integer> winerPortrait = StaticLordDataMgr.getPortraitByUnlock(StaticPortrait.UNLOCK_TYPE_WINNER).stream().map(StaticPortrait::getId).collect(Collectors.toSet());
//        if (!CheckNull.isEmpty(winerPortrait)) {
//            // 霸主判断
//            if (BerlinWar.isCurWinner(roleId)) {
//                player.portraits.addAll(winerPortrait);
//            } else {
//                player.portraits.removeAll(winerPortrait); // 移除旧霸主头像
//            }
//        }
    }

    /**
     * 检测玩家是否拥有该头像
     *
     * @param player     玩家
     * @param portraitId 头像
     * @throws MwException
     */
    @Deprecated
    public void oldCheckPlayerHasPortrait(Player player, int portraitId) throws MwException {
        StaticPortrait portrait = StaticLordDataMgr.getPortrait(portraitId);
        if (portrait == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "未配置此头像 portraitId:", portraitId);
        }
        if (portrait.getUnlock() == StaticPortrait.UNLOCK_FREE) { // 免费头像
            // 非初始头像,需要检测是否获取
        } else if (portrait.getUnlock() == StaticPortrait.UNLOCK_VIP_GIFT_BUY
                || portrait.getUnlock() == StaticPortrait.UNLOCK_TYPE_WINNER) { // 需要拥有的
            if (!player.portraits.contains(portraitId)) {
                throw new MwException(GameError.NOT_HAS_PORTRAIT.getCode(), "未拥有此头像 portraitId:", portraitId);
            }
        } else {
            commonCheckPortrait(player, portraitId, portrait);
        }
    }

    public void checkVipChatBubble(Player player){
        try {
            StaticLordDataMgr.getChatBubbleMap().values().stream().filter(tmp -> tmp.getType() == StaticChatBubble.TYPE_VIP_LV).forEach(tmp -> {
                Map<Integer, BaseDressUpEntity> map = player.getDressUp().getDressUpEntityMapByType(AwardType.CHAT_BUBBLE);
                if (Objects.isNull(map.get(tmp.getId())) && player.lord.getVip() >= tmp.getParam()) {
                    map.put(tmp.getId(), new ChatBubbleEntity(tmp.getId(), 1));
                }
            });
        }catch (Exception e) {
            LogUtil.error(e);
        }
    }

    /**
     * 公共检测玩家头像
     *
     * @param player     玩家对象
     * @param portraitId 头像id
     * @param portrait   头像配置
     * @throws MwException 自定义异常
     */
    private void commonCheckPortrait(Player player, int portraitId, StaticPortrait portrait) throws MwException {
        if (portrait.getUnlock() == StaticPortrait.UNLOCK_VIP_LV) {// 检测vip等级
            int needVipLv = portrait.getParam().get(0);
            int vipLv = player.lord.getVip();
            if (vipLv < needVipLv) {
                throw new MwException(GameError.NOT_HAS_PORTRAIT.getCode(), "vip等级不足 portraitId:", portraitId);
            }
        } else if (portrait.getUnlock() == StaticPortrait.UNLOCK_HERO) {
            int heroId = portrait.getParam().get(0);
            if (!player.heros.containsKey(heroId)) {
                throw new MwException(GameError.NOT_HAS_PORTRAIT.getCode(), "未拥有此头像 portraitId:", portraitId);
            }
        } else {
            throw new MwException(GameError.NOT_HAS_PORTRAIT.getCode(), "未拥有此头像 portraitId:", portraitId);
        }
    }


    /**
     * 延时执行移除玩家限时装扮
     */
    class RemoveDressUpDelayRun implements DelayRun {

        protected final Player player;

        final BaseDressUpEntity dressUpEntity;

        RemoveDressUpDelayRun(Player player, BaseDressUpEntity dressUpEntity) {
            this.player = player;
            this.dressUpEntity = dressUpEntity;
        }

        @Override
        public int deadlineTime() {
            return this.dressUpEntity.getDuration();
        }

        /**
         * 执行移除
         *
         * @param runTime 当前运行的时间
         * @param env     延时执行的环境
         */
        @Override
        public void deadRun(int runTime, DelayInvokeEnvironment env) {
            if (env instanceof DressUpDataManager) {
                if (Objects.isNull(player) && Objects.isNull(dressUpEntity)) {
                    return;
                }
                LogUtil.common("执行移除 玩家装扮 roleId: ", player.roleId, ", 移除的装扮:", dressUpEntity, " executeTime: ", dressUpEntity.getDuration(), ", runTime:", runTime);
                if (runTime >= dressUpEntity.getDuration() && !dressUpEntity.isPermanentHas()) {
                    // 移除玩家的限时装扮
                    subDressUp(player, dressUpEntity.getType(), dressUpEntity.getId(), dressUpEntity.getDuration(), AwardFrom.COMMON);
                }
            }
        }
    }

}
