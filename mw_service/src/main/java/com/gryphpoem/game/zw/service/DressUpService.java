package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.manager.DressUpDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.dressup.BaseDressUpEntity;
import com.gryphpoem.game.zw.resource.pojo.dressup.DressUp;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinWar;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.Turple;
import com.gryphpoem.game.zw.rpc.DubboRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 皮肤的service
 *
 * @description:
 * @author: zhou jie
 * @time: 2021/3/4 11:38
 */
@Service
public class DressUpService implements LoginService, GmCmdService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private DressUpDataManager dressUpDataManager;
    @Autowired
    private DubboRpcService dubboRpcService;

    /**
     * 获取装扮数据
     *
     * @param roleId 玩家id
     * @param type   装扮类型
     * @return 装扮数据
     * @throws MwException 自定义数据
     */
    public GamePb4.GetDressUpDataRs getDressUp(long roleId, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 装扮数据
        final Map<Integer, BaseDressUpEntity> dressUpMap = dressUpDataManager.getDressUpByType(player, type);
        GamePb4.GetDressUpDataRs.Builder builder = GamePb4.GetDressUpDataRs.newBuilder();
        if (!CheckNull.isEmpty(dressUpMap)) {
            builder.addAllData(dressUpMap.values().stream().map(BaseDressUpEntity::toData).map(CommonPb.DressUpEntity.Builder::build).collect(Collectors.toList()));
        }
        return builder.build();
    }

    /**
     * 修改装扮数据
     *
     * @param roleId 玩家id
     * @param req    请求参数
     * @return 修改后数据
     * @throws MwException 自定义异常
     */
    public GamePb4.ChangeDressUpRs changeDressUp(long roleId, GamePb4.ChangeDressUpRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int type = req.getType();
        if (!AwardType.DRESS_UP_TYPE.contains(type)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, ", 没有该类型的装扮 type:", type);
        }
        Map<Integer, BaseDressUpEntity> dressUpMap = dressUpDataManager.getDressUpByType(player, type);
        int id = req.getId();
        DressUp dressUp = player.getDressUp();
        if (type == AwardType.PORTRAIT) {
            dressUpDataManager.checkPlayerHasDress(player, type, id, dressUpMap);
            player.lord.setPortrait(id);
            dubboRpcService.updatePlayerLord2CrossPlayerServer(player);
        } else {
            if (CheckNull.isEmpty(dressUpMap)) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ", type:", type, ", 未拥有该装扮:", id);
            }
            BaseDressUpEntity dressUpEntity = dressUpMap.get(id);
            if (Objects.isNull(dressUpEntity)&&type != AwardType.TITLE) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ", type:", type, ", 未拥有该装扮:", id);
            }
            if (type == AwardType.CASTLE_SKIN) {
                StaticCastleSkin staticCastleSkin = StaticLordDataMgr.getCastleSkinMapById(id);
                if (staticCastleSkin == null) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, ", 城堡皮肤未配置 skinId:", id);
                }
                player.setCurCastleSkin(id);
                dressUpDataManager.pushPlayerCastleSkinChange(player);
                CalculateUtil.reCalcBattleHeroAttr(player);
            } else if (type == AwardType.PORTRAIT_FRAME) {
                StaticPortraitFrame staticPortraitFrame = StaticLordDataMgr.getPortraitFrame(id);
                if (Objects.isNull(staticPortraitFrame)) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, ", 头像框未配置 skinId:", id);
                }
                dressUp.setCurPortraitFrame(id);
            } else if (type == AwardType.NAMEPLATE) {
                StaticNameplate staticNameplate = StaticLordDataMgr.getNameplate(id);
                if (Objects.isNull(staticNameplate)) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, ", 铭牌未配置 skinId:", id);
                }
                dressUp.setCurNamePlate(id);
                dressUpDataManager.pushPlayerCastleSkinChange(player);
            } else if (type == AwardType.MARCH_SPECIAL_EFFECTS) {
                StaticMarchLine sMarchLine = StaticLordDataMgr.getMarchLine(id);
                if (Objects.isNull(sMarchLine)) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, ", 行军特效未配置 skinId:", id);
                }
                dressUp.setCurMarchEffect(id);
            } else if(type == AwardType.CHAT_BUBBLE){
                dressUp.setCurrChatBubble(id);
            }else if (type == AwardType.TITLE){
                if(id==-1){
                    dressUp.setCurTitle(0);
                }else{
                    StaticTitle title = StaticLordDataMgr.getTitleMapById(id);
                    if (Objects.isNull(title)) {
                        throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, ", 称号未配置 skinId:", id);
                    }
                    if (!dressUpEntity.isPermanentHas()&&dressUpEntity.getDuration()<=0){
                        throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ", 称号未解锁 skinId:", id);
                    }
                    dressUp.setCurTitle(id);
                }
                dressUpDataManager.pushPlayerCastleSkinChange(player);
            }
        }

        return GamePb4.ChangeDressUpRs.newBuilder().build();
    }

    /**
     * 获取玩家所拥有特殊头像
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.GetPortraitRs getPortrait(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GamePb4.GetPortraitRs.Builder builder = GamePb4.GetPortraitRs.newBuilder();
        // 检测玩家解锁获取的头像
        dressUpDataManager.oldCheckPlayerUnlockPortrait(player);
        player.portraits.forEach(builder::addPortraitIds);
        return builder.build();
    }


    /**
     * 修改头像
     *
     * @param roleId
     * @param portraitId
     * @return
     * @throws MwException
     */
    public GamePb4.ChangePortraitRs changePortrait(long roleId, int portraitId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        dressUpDataManager.oldCheckPlayerHasPortrait(player, portraitId);
        player.lord.setPortrait(portraitId);
        dubboRpcService.updatePlayerLord2CrossPlayerServer(player);
        GamePb4.ChangePortraitRs.Builder builder = GamePb4.ChangePortraitRs.newBuilder();
        builder.setPortraitId(portraitId);
        return builder.build();
    }


    /**
     * 获取拥有的形象
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.GetBodyImageRs getBodyImage(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (player.getCurBodyImage() == 0) {// 没有设置形象时设置头像对应的形象
            StaticBodyImage sBodyImage = StaticLordDataMgr.getBodyImageByPortraitId(player.lord.getPortrait());
            player.setCurBodyImage(sBodyImage == null ? 1 : sBodyImage.getId());
        }
        // 换形象
        if (StaticLordDataMgr.getBerlinWinnerBodyImage().contains(player.getCurBodyImage())
                && !BerlinWar.isCurWinner(roleId)) {
            player.setCurBodyImage(1);
        }
        GamePb4.GetBodyImageRs.Builder builder = GamePb4.GetBodyImageRs.newBuilder();
        builder.setCurBodyImage(player.getCurBodyImage());
        builder.addAllBodyImageIds(player.getBodyImages());
        return builder.build();
    }

    /**
     * 修改形象
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb4.ChangeBodyImageRs changeBodyImage(long roleId, GamePb4.ChangeBodyImageRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int bodyImageId = req.getBodyImageId();
        StaticBodyImage sBodyImage = StaticLordDataMgr.getBodyImageById(bodyImageId);
        if (sBodyImage == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "形象未配置 : bodyImageId", bodyImageId, ", roleId:",
                    roleId);
        }
        if (sBodyImage.getType() == StaticBodyImage.TYPE_FREE) {
        } else if (sBodyImage.getType() == StaticBodyImage.TYPE_PORTRAIT) {
            int portraitId = sBodyImage.getParam();
            dressUpDataManager.oldCheckPlayerHasPortrait(player, portraitId);
        } else {
            throw new MwException(GameError.NO_CONFIG.getCode(), "形象未配置 : bodyImageId", bodyImageId, ", roleId:",
                    roleId);
        }
        player.setCurBodyImage(bodyImageId);
        GamePb4.ChangeBodyImageRs.Builder builder = GamePb4.ChangeBodyImageRs.newBuilder();
        builder.setBodyImageId(bodyImageId);
        return builder.build();
    }


    /**
     * 获取聊天气泡
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.GetChatBubbleRs getChatBubble(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GamePb4.GetChatBubbleRs.Builder builder = GamePb4.GetChatBubbleRs.newBuilder();
        builder.setCurBubbleId(player.getCurChatBubble());
        builder.addAllBubbleIds(player.getChatBubbles());
        return builder.build();
    }

    @Override
    public void afterLogin(Player player){
        try {
            dressUpDataManager.getDressUpByType(player,AwardType.CHAT_BUBBLE);
            if(player.getCurChatBubble() > 0 && player.getDressUp().getCurrChatBubble() <= 0){
                player.getDressUp().setCurrChatBubble(player.getCurChatBubble());
                player.setCurChatBubble(0);
            }
            dressUpDataManager.checkVipChatBubble(player);
            //检查当前是否是霸主
            Turple<Integer, Long> perWinner = BerlinWar.getCurWinner();
            if(perWinner == null || (perWinner != null && !perWinner.getB().equals(player.roleId))){
                Map<Integer, BaseDressUpEntity> map = dressUpDataManager.getDressUpByType(player,AwardType.PORTRAIT);
                if(map.containsKey(11)){
                    dressUpDataManager.subDressUp(player,AwardType.PORTRAIT,11,0, AwardFrom.DO_SOME);
                }
            }
        }catch (Exception e) {
            LogUtil.error(e);
        }
    }


    public void reloadTable() {
        playerDataManager.getAllPlayer().values().forEach(player -> {
            StaticCastleSkin staticCastleSkin = StaticLordDataMgr.getCastleSkinMapById(player.getCurCastleSkin());
            if (!CheckNull.isNull(staticCastleSkin))
                return;
            player.setCurCastleSkin(StaticCastleSkin.DEFAULT_SKIN_ID);
        });
    }

    @GmCmd("dressUpService")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        String cmd = params[0];
        if ("reload".equalsIgnoreCase(cmd)) {
            reloadTable();
        }
    }
}
