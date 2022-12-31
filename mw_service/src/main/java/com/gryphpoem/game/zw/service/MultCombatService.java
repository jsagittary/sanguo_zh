package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.Java8Utils.SendMsgToPlayerCallback;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticCombatDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticShopDataMgr;
import com.gryphpoem.game.zw.logic.FightSettleLogic;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.RptHero;
import com.gryphpoem.game.zw.pb.GamePb2.*;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.MultCombat;
import com.gryphpoem.game.zw.resource.domain.p.MultCombatTeam;
import com.gryphpoem.game.zw.resource.domain.s.StaticMultCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticMultcombatShop;
import com.gryphpoem.game.zw.resource.pojo.Trophy;
import com.gryphpoem.game.zw.resource.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName MultCombatService.java
 * @Description 多人副本
 * @date 2018年12月25日
 */
@Component
public class MultCombatService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private FightService fightService;
    @Autowired
    private FightSettleLogic fightSettleLogic;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    // 副本队伍 <teamId,MultCombatTeam>
    private Map<Integer, MultCombatTeam> combatTeams = new ConcurrentHashMap<>();

    /**
     * 创建副本队伍
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public CreateCombatTeamRs createCombatTeam(long roleId, CreateCombatTeamRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        MultCombat multCombat = player.getAndCreateMultCombat(); // 自己的关卡进度
        int combatId = req.getCombatId();

        // 副本问题检测
        checkCombatllegal(player, combatId);

        if (multCombat.getTeamId() != 0) {
            throw new MwException(GameError.MULTCOMBAT_IN_TEAM_STATUS.getCode(), "已经在队伍中, roleId:" + roleId);
        }
        MultCombatTeam team = createTeam(player, multCombat, combatId);

        CreateCombatTeamRs.Builder builder = CreateCombatTeamRs.newBuilder();
        builder.setTeam(PbHelper.createMultCombatTeamPb(team, playerDataManager));
        return builder.build();
    }

    /**
     * 创建队伍
     *
     * @param player
     * @param multCombat
     * @param combatId
     * @return
     */
    private MultCombatTeam createTeam(Player player, MultCombat multCombat, int combatId) {
        // 创建队伍
        MultCombatTeam team = MultCombatTeam.createMultTeam(player.roleId, combatId);
        combatTeams.put(team.getTeamId(), team);
        multCombat.setTeamId(team.getTeamId()); // 设置到玩家上
        int now = TimeHelper.getCurrentSecond();
        if (now - multCombat.getChatCd() > Constant.COMBAT_TEAM_CHAT_CD) { // cd判断
            // chatDataManager.sendSysChat(ChatConst.CHAT_COMBAT_TEAM, player.lord.getCamp(), 0, player.lord.getNick(),
            // combatId, team.getTeamId());
            multCombat.setChatCd(now);
        }
        return team;
    }

    /**
     * 修改副本队伍信息(解散队伍,修改状态)
     *
     * @param roleId
     * @param req    的command字段 1.解散队伍 2.修改自动开始状态 3.修改否允许自动加入状态
     * @return
     * @throws MwException
     */
    public ModifyCombatTeamRs modifyCombatTeam(long roleId, ModifyCombatTeamRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        final int command = req.getCommand();

        MultCombatTeam mcTeam = checkAndGetTeam(player); // 队伍信息
        // 检测是否是队长
        if (!checkIsCaptain(player)) {
            throw new MwException(GameError.YOU_ARE_NOT_CAPTAIN.getCode(), "不是队长没有权限, roleId:", roleId);
        }

        ModifyCombatTeamRs.Builder builder = ModifyCombatTeamRs.newBuilder();
        if (command == 1) {// 解散队伍
            breakupTeam(mcTeam);
            syncCombatTeam(1, mcTeam, roleId);
        } else if (command == 2) {// 修改自动开始状态
            mcTeam.setAutoStart(!mcTeam.isAutoStart());
            syncCombatTeam(mcTeam, roleId);
        } else if (command == 3) {// 修改否允许自动加入状态
            mcTeam.setAutoJoin(!mcTeam.isAutoJoin());
            syncCombatTeam(mcTeam, roleId);
        } else {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "command不合法, roleId:", roleId, ", command:",
                    command);
        }
        builder.setTeam(PbHelper.createMultCombatTeamPb(mcTeam, playerDataManager));
        return builder.build();
    }

    /**
     * 解散队伍
     *
     * @param mcTeam
     */
    private void breakupTeam(MultCombatTeam mcTeam) {
        mcTeam.getTeamMember().forEach(roleId -> {
            Player player = playerDataManager.getPlayer(roleId);
            if (player != null) {
                MultCombat multCombat = player.getAndCreateMultCombat();
                multCombat.setTeamId(0); // 个人状态解除team
            }
        });
        combatTeams.remove(mcTeam.getTeamId());
    }

    /**
     * 获取玩家所在的队伍
     *
     * @param player
     * @return
     * @throws MwException
     */
    private MultCombatTeam checkAndGetTeam(Player player) throws MwException {
        MultCombat multCombat = player.getAndCreateMultCombat();
        MultCombatTeam mcTeam = combatTeams.get(multCombat.getTeamId()); // 队伍信息
        if (mcTeam == null) {
            throw new MwException(GameError.TEAM_NOT_EXIST.getCode(), "队伍不存在");
        }
        return mcTeam;
    }

    /**
     * 队伍的推送
     *
     * @param command
     * @param mcTeam        1.队伍解散, 2.自己被踢掉了
     * @param processRoleId 当前操作者,没有为0
     */
    private void syncCombatTeam(int command, MultCombatTeam mcTeam, final long processRoleId) {
        mcTeam.getTeamMember().stream().filter(roleId -> roleId != processRoleId).forEach(roleId -> {
            Player player = playerDataManager.getPlayer(roleId);
            if (player != null) {
                syncCombatTeam(player, command, mcTeam);
            }
        });
    }

    /**
     * 队伍的推送
     *
     * @param mcTeam        1.队伍解散, 2.自己被踢掉了
     * @param processRoleId
     */
    private void syncCombatTeam(MultCombatTeam mcTeam, final long processRoleId) {
        syncCombatTeam(0, mcTeam, processRoleId);
    }

    /**
     * 队伍信息的推送
     *
     * @param player
     * @param command 1.队伍解散, 2.自己被踢掉了
     * @param mcTeam
     */
    private void syncCombatTeam(Player player, int command, MultCombatTeam mcTeam) {
        if (player != null && player.isLogin && player.ctx != null) {
            SyncCombatTeamRs.Builder builder = SyncCombatTeamRs.newBuilder();
            builder.setCommand(command);
            if (command != 1 || command != 2) {
                builder.setTeam(PbHelper.createMultCombatTeamPb(mcTeam, playerDataManager));
            }
            Base.Builder msg = PbHelper.createSynBase(SyncCombatTeamRs.EXT_FIELD_NUMBER, SyncCombatTeamRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 快速加入队伍(没有可加入就创建队伍);普通加入队伍
     *
     * @param roleId
     * @param req    teamId字段: 需要加入队伍的id; 填0表示快速加入
     * @return
     * @throws MwException
     */
    public void joinCombatTeam(long roleId, JoinCombatTeamRq req, SendMsgToPlayerCallback<JoinCombatTeamRs> callback)
            throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        MultCombat multCombat = player.getAndCreateMultCombat(); // 自己的关卡进度
        if (multCombat.getTeamId() != 0) {
            throw new MwException(GameError.MULTCOMBAT_IN_TEAM_STATUS.getCode(), "已经在队伍中, roleId:", roleId, ", teamId:",
                    multCombat.getTeamId());
        }
        int teamId = req.getTeamId();
        JoinCombatTeamRs.Builder builder = JoinCombatTeamRs.newBuilder();
        if (teamId <= 0) { // 快速加入
            final int combatId = req.getCombatId();
            checkCombatllegal(player, combatId);
            // 查找符合的队伍
            MultCombatTeam mcTeam = combatTeams.values().stream().filter(mct -> {
                if (mct.isAutoJoin() && mct.getTeamMember().size() < Constant.MEMBER_OF_COMBAT_TEAM
                        && mct.getCombatId() == combatId) {
                    Player tp = playerDataManager.getPlayer(mct.getCaptainRoleId());
                    if (tp != null) {
                        return tp.lord.getCamp() == player.lord.getCamp();
                    }
                }
                return false;
            }).findAny().orElse(null);
            if (mcTeam == null) {
                // 自己作为队长创建队伍
                mcTeam = createTeam(player, multCombat, combatId);
            } else {// 找到符合队伍加入
                mcTeam.getTeamMember().add(player.roleId);
                multCombat.setTeamId(mcTeam.getTeamId());
                syncCombatTeam(mcTeam, player.roleId);
            }
            teamId = mcTeam.getTeamId();
            builder.setTeam(PbHelper.createMultCombatTeamPb(mcTeam, playerDataManager));
        } else { // 指定队伍加入
            MultCombatTeam mcTeam = combatTeams.get(teamId);
            if (mcTeam == null) {
                throw new MwException(GameError.TEAM_NOT_EXIST.getCode(), "队伍已解散或不存在, roleId:", roleId, ", teamId:",
                        teamId);
            }
            if (mcTeam.getTeamMember().size() >= Constant.MEMBER_OF_COMBAT_TEAM) {// 队伍上限人数
                throw new MwException(GameError.TEAM_MEMBER_IS_FULL.getCode(), "队伍人员已满, roleId:", roleId, ", teamId:",
                        teamId);
            }
            Player tp = playerDataManager.getPlayer(mcTeam.getCaptainRoleId());
            if (tp.lord.getCamp() != player.lord.getCamp()) {
                throw new MwException(GameError.TEAM_NOT_EXIST.getCode(), "队伍阵营不正确, roleId:", roleId, ", teamId:",
                        teamId);
            }
            teamId = mcTeam.getTeamId();
            // 检测副本是否合法
            checkCombatllegal(player, mcTeam.getCombatId());
            // 加入队伍
            mcTeam.getTeamMember().add(player.roleId);
            multCombat.setTeamId(mcTeam.getTeamId());
            syncCombatTeam(mcTeam, player.roleId);
            builder.setTeam(PbHelper.createMultCombatTeamPb(mcTeam, playerDataManager));
        }
        if (callback != null)
            callback.sendMsgToPlayer(JoinCombatTeamRs.EXT_FIELD_NUMBER, JoinCombatTeamRs.ext, builder.build());
        checkAutoStart(teamId);
    }

    /**
     * 检测自动开启
     *
     * @param teamId
     */
    private void checkAutoStart(int teamId) {
        // 检测自动开启
        MultCombatTeam mcTeam = combatTeams.get(teamId);
        if (mcTeam != null && mcTeam.isAutoStart() && mcTeam.getTeamMember().size() >= Constant.MEMBER_OF_COMBAT_TEAM) {
            processMultCombatFight(mcTeam);
        }
    }

    /**
     * 检测副本是否合法
     *
     * @param player
     * @param combatId 目标副本
     * @throws MwException
     */
    private void checkCombatllegal(Player player, int combatId) throws MwException {
        long roleId = player.roleId;
        MultCombat multCombat = player.getAndCreateMultCombat();
        if (!checkMultCombatLock(player)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "多人副本功能未解锁 , roleId:" + roleId);
        }
        StaticMultCombat sMultCombat = StaticCombatDataMgr.getMultCombatById(combatId);
        if (sMultCombat == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "多人副本配置未找到 , roleId:" + roleId, ", combatId:",
                    combatId);
        }
        if (multCombat.getHighestCombatId() < sMultCombat.getPreId()) {
            throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), "前置关卡未通关, roleId:" + roleId);
        }

    }

    /**
     * 离开队伍
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public LeaveCombatTeamRs leaveCombatTeam(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        MultCombat multCombat = player.getAndCreateMultCombat(); // 自己的关卡进度
        if (multCombat.getTeamId() == 0) {
            throw new MwException(GameError.MULTCOMBAT_NOT_IN_TEAM_STATUS.getCode(), "自己不在队伍中, roleId:" + roleId);
        }
        MultCombatTeam mcTeam = combatTeams.get(multCombat.getTeamId());
        if (mcTeam == null) {
            multCombat.setTeamId(0); // 直接移除自身状态
        } else {
            if (!mcTeam.getTeamMember().contains(roleId)) {// 自己在一个存在的队伍,但队伍里面有没有自己
                multCombat.setTeamId(0);
                // 此处正常不应该不应该进来
                LogUtil.error("自己在一个存在的队伍,但队伍里面有没有自己 roleId:", roleId, ", teamId:", mcTeam.getTeamId());
            } else {
                if (roleId == mcTeam.getCaptainRoleId()) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), "队长不能离开队伍,只能解散 roleId:" + roleId);
                }
                multCombat.setTeamId(0);
                mcTeam.getTeamMember().remove(Long.valueOf(roleId));
                syncCombatTeam(mcTeam, roleId);
            }
        }
        LeaveCombatTeamRs.Builder builder = LeaveCombatTeamRs.newBuilder();
        return builder.build();
    }

    /**
     * 队长踢人
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public TickTeamMemberRs tickTeamMember(long roleId, TickTeamMemberRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        long tickRoleId = req.getRoleId();
        MultCombatTeam mcTeam = checkAndGetTeam(player);
        // 检测是否是队长
        if (!checkIsCaptain(player)) {
            throw new MwException(GameError.YOU_ARE_NOT_CAPTAIN.getCode(), "不是队长没有权限, roleId:", roleId);
        }
        if (mcTeam.getCaptainRoleId() == tickRoleId) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "不提自己, tickRoleId:", tickRoleId);
        }
        if (!mcTeam.getTeamMember().contains(tickRoleId)) {
            throw new MwException(GameError.TEAM_MEMBER_NOT_EXIST.getCode(), "队员不存在或离开, tickRoleId:", tickRoleId);
        }
        // 移除
        mcTeam.getTeamMember().remove(Long.valueOf(tickRoleId));
        Player tickPlayer = playerDataManager.getPlayer(tickRoleId);
        if (tickPlayer != null) {
            MultCombat tickMCombat = tickPlayer.getAndCreateMultCombat();
            tickMCombat.setTeamId(0);
            // 推送被踢的人
            syncCombatTeam(tickPlayer, 2, mcTeam);
        }
        TickTeamMemberRs.Builder builder = TickTeamMemberRs.newBuilder();
        builder.setTeam(PbHelper.createMultCombatTeamPb(mcTeam, playerDataManager));
        mcTeam.getTeamMember().stream().filter(rid -> rid.longValue() != roleId)
                .map(rid -> playerDataManager.getPlayer(rid)).forEach(p -> syncCombatTeam(p, 0, mcTeam));
        return builder.build();
    }

    /**
     * 获取多人副本信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetMultCombatRs getMultCombat(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        MultCombat multCombat = player.getAndCreateMultCombat();
        multCombat.refresh();
        GetMultCombatRs.Builder builder = GetMultCombatRs.newBuilder();
        builder.setCombat(multCombat.ser());
        int teamId = multCombat.getTeamId();
        if (teamId > 0) {
            MultCombatTeam mct = combatTeams.get(teamId);
            if (mct != null) {
                builder.setTeam(PbHelper.createMultCombatTeamPb(mct, playerDataManager));
            }
        }
        return builder.build();
    }

    /**
     * 获取可选队员列表
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetTeamMemberListRs getTeamMemberList(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        MultCombatTeam mcTeam = checkAndGetTeam(player);
        // 检测是否是队长
        if (!checkIsCaptain(player)) {
            throw new MwException(GameError.YOU_ARE_NOT_CAPTAIN.getCode(), "不是队长没有权限, roleId:", roleId);
        }
        int combatId = mcTeam.getCombatId();
        StaticMultCombat sMultCombat = StaticCombatDataMgr.getMultCombatById(combatId);
        if (sMultCombat == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "多人副本配置未找到, roleId:", roleId, ", combatId:", combatId);
        }
        // 获得列表 如果慢有优化空间
        List<Player> showList = playerDataManager.getPlayerByCamp(player.lord.getCamp()).values().stream()
                .filter(p -> roleId != p.roleId
                        && p.getAndCreateMultCombat().getHighestCombatId() >= sMultCombat.getPreId()
                        && checkMultCombatLock(p))
                .sorted((p1, p2) -> {
                    // 是否在线 > 战斗力
                    // 在线用1表示 ,不在线用0
                    int p1Online = p1.isLogin ? 1 : 0;
                    int p2Online = p2.isLogin ? 1 : 0;
                    // 都是从大到小进行排
                    if (p1Online > p2Online) {
                        return -1;
                    } else if (p1Online < p2Online) {
                        return 1;
                    } else {
                        long p1Fight = p1.lord.getFight();
                        long p2Fight = p2.lord.getFight();
                        if (p1Fight > p2Fight) {
                            return -1;
                        } else if (p1Fight < p2Fight) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                }).limit(30).collect(Collectors.toList());
        GetTeamMemberListRs.Builder builder = GetTeamMemberListRs.newBuilder();
        for (Player p : showList) {
            builder.addTm(PbHelper.createTeamMemberPb(p, 0));
        }
        return builder.build();
    }

    /**
     * 检测是否解锁
     *
     * @param p
     * @return
     */
    private boolean checkMultCombatLock(Player p) {
        // 解锁逻辑
        return StaticFunctionDataMgr.funcitonIsOpen(p, FunctionConstant.UNLOCK_TYPE_MULTCOMBAT);
    }

    /**
     * 发送邀请
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public SendInvitationRs sendInvitation(long roleId, SendInvitationRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        List<Long> roleIdList = req.getRoleIdList();

        MultCombatTeam mcTeam = checkAndGetTeam(player);
        // 检测是否是队长
        if (!checkIsCaptain(player)) {
            throw new MwException(GameError.YOU_ARE_NOT_CAPTAIN.getCode(), "不是队长没有权限, roleId:", roleId);
        }
        int combatId = mcTeam.getCombatId();
        StaticMultCombat sMultCombat = StaticCombatDataMgr.getMultCombatById(combatId);
        if (sMultCombat == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "多人副本配置未找到, roleId:", roleId, ", combatId:", combatId);
        }
        for (Long rid : roleIdList) {
            Player p = playerDataManager.getPlayer(rid);
            if (p != null && p.isLogin && p.getMultCombat() != null && checkMultCombatLock(p)
                    && p.getMultCombat().getHighestCombatId() >= sMultCombat.getPreId()) {
                syncInvitation(p, mcTeam);
            }
        }
        SendInvitationRs.Builder builder = SendInvitationRs.newBuilder();
        return builder.build();
    }

    /**
     * 邀请信息推送
     *
     * @param p
     * @param mcTeam
     */
    private void syncInvitation(Player p, MultCombatTeam mcTeam) {
        if (p != null && p.isLogin && p.ctx != null) {
            SyncInvitationRs.Builder builder = SyncInvitationRs.newBuilder();
            builder.setTeam(PbHelper.createMultCombatTeamPb(mcTeam, playerDataManager));
            Base.Builder msg = PbHelper.createSynBase(SyncInvitationRs.EXT_FIELD_NUMBER, SyncInvitationRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(p.ctx, msg.build(), p.roleId));
        }
    }

    /**
     * 开始多人副本
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public StartMultCombatRs startMultCombat(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        MultCombatTeam mcTeam = checkAndGetTeam(player);
        // 检测是否是队长
        if (!checkIsCaptain(player)) {
            throw new MwException(GameError.YOU_ARE_NOT_CAPTAIN.getCode(), "不是队长没有权限, roleId:", roleId);
        }
        int combatId = mcTeam.getCombatId();
        StaticMultCombat sMultCombat = StaticCombatDataMgr.getMultCombatById(combatId);
        if (sMultCombat == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "多人副本配置未找到, roleId:", roleId, ", combatId:", combatId);
        }
        // 战斗处理
        processMultCombatFight(mcTeam);
        StartMultCombatRs.Builder builder = StartMultCombatRs.newBuilder();
        return builder.build();
    }

    /**
     * 处理副本战斗
     *
     * @param mcTeam
     */
    private void processMultCombatFight(MultCombatTeam mcTeam) {
        StaticMultCombat sMultCombat = StaticCombatDataMgr.getMultCombatById(mcTeam.getCombatId());
        // 战斗力小的排在前面
        List<Player> players = mcTeam.getTeamMember().stream().map(rid -> playerDataManager.getPlayer(rid))
                .filter(p -> p != null).sorted((p1, p2) -> {
                    long p1Fight = p1.lord.getFight();
                    long p2Fight = p2.lord.getFight();
                    if (p1Fight > p2Fight) {
                        return 1;
                    } else if (p1Fight < p2Fight) {
                        return -1;
                    } else {
                        return 0;
                    }
                }).collect(Collectors.toList());

        Fighter attacker = fightService.createCombatPlayerFighter(players);
        Fighter defender = fightService.createNpcFighter(sMultCombat.getForm());
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.start();
        // 是否能有合作奖励
        // boolean hasTeamAward = hasTeamAward(players, sMultCombat.getCombatId());
        List<RptHero> rptHeroList = fightSettleLogic.combatCreateRptHeroNoAward(attacker.getForces());
        for (Player p : players) {
            List<List<Integer>> showAward = new ArrayList<>();
            MultCombat multCombat = p.getAndCreateMultCombat();
            if (fightLogic.getWinState() == 1) { // 胜利
                boolean hasTeamAward = mcTeam.getCaptainRoleId() != p.lord.getLordId();// 判断是否是房主,不是房主才能拿到
                multCombatAward(p, sMultCombat, hasTeamAward, showAward);
                multCombat.updateCombatId(sMultCombat.getCombatId()); // 更新进度
                LogLordHelper.commonLog("wipeMultCombat", AwardFrom.DO_MUlLCOMBAT_AWARD, p,
                        multCombat.getHighestCombatId());
            }
            SyncMultCombatReportRs.Builder dataBuilder = SyncMultCombatReportRs.newBuilder();
            dataBuilder.setResult(fightLogic.getWinState());
            if (!CheckNull.isEmpty(showAward)) {
                dataBuilder.addAllAward(PbHelper.createAwardsPb(showAward));
            }
            dataBuilder.setMc(multCombat.ser());
            dataBuilder.setRecord(fightLogic.generateRecord());
            dataBuilder.addAllAtkHero(rptHeroList);
            // dataBuilder.setTeam(PbHelper.createMultCombatTeamPb(mcTeam, playerDataManager));
            syncMultCombatReport(p, dataBuilder);// 推送结果
        }
        if (fightLogic.getWinState() == 1) {
            // 发送全服首次通关聊天
            sendPassChat(mcTeam);
        }
        // 解散队伍
        breakupTeam(mcTeam);
    }

    /**
     * 全服首通系统系统消息
     *
     * @param mcTeam
     */
    private void sendPassChat(MultCombatTeam mcTeam) {
        int combatId = mcTeam.getCombatId();
        Player player = playerDataManager.getPlayer(mcTeam.getCaptainRoleId());
        if (player == null) return;
        Trophy trophy = globalDataManager.getGameGlobal().getTrophy();
        Set<Integer> passMultCombat = trophy.getPassMultCombat();
        if (!passMultCombat.contains(combatId)) {
            chatDataManager.sendSysChat(ChatConst.CHAT_MULT_COMBAT_FIRSTPASS, 0, 0, player.lord.getCamp(),
                    player.lord.getNick(), combatId);
            passMultCombat.add(combatId);
        }
    }

    private void syncMultCombatReport(Player player, SyncMultCombatReportRs.Builder dataBuilder) {
        if (player != null && player.isLogin && player.ctx != null) {
            Base.Builder msg = PbHelper.createSynBase(SyncMultCombatReportRs.EXT_FIELD_NUMBER,
                    SyncMultCombatReportRs.ext, dataBuilder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 副本奖励
     *
     * @param p
     * @param sMultCombat
     * @param hasTeamAward
     * @param showAward
     */
    private void multCombatAward(Player p, StaticMultCombat sMultCombat, boolean hasTeamAward,
                                 List<List<Integer>> showAward) {
        MultCombat multCombat = p.getAndCreateMultCombat();
        int pointAward = 0; // 副本点数奖励
        // List<List<Integer>> awardRand = null; // 装备的奖励
        List<List<Integer>> otherAward = new ArrayList<>();// 其他奖励
        if (multCombat.getHighestCombatId() < sMultCombat.getCombatId()) { // 首通奖励
            pointAward = sMultCombat.getFirstPassPoint();
            // awardRand = sMultCombat.getFirstPassRand();
            otherAward.addAll(sMultCombat.getFirstPassAward());
        } else if (!multCombat.getTodayCombatId().contains(sMultCombat.getCombatId())) {// 每日通关
            pointAward = sMultCombat.getPassPoint();
            // 进行概率处理
            // awardRand = sMultCombat.getPassAward();
            List<List<Integer>> passRandAward = processPassRandAward(sMultCombat.getPassAward());
            if (passRandAward != null) {
                otherAward.addAll(passRandAward);
            }
        }

        if (hasTeamAward) {// 合作奖励
            if (multCombat.getTeamAwardCnt() < Constant.COMBAT_TEAM_AWARD_CNT) {
                otherAward.addAll(sMultCombat.getTeamAward());
                multCombat.setTeamAwardCnt(multCombat.getTeamAwardCnt() + 1);
            }
        }
        if (pointAward > 0) {
            multCombat.addCombatPoint(pointAward);
            if (showAward != null) {
                List<Integer> e = new ArrayList<>(3);
                e.add(AwardType.MULTCOMBAT_POINT);
                e.add(1);
                e.add(pointAward);
                showAward.add(e);
            }
        }
        // 飞机装备的奖励,不做
        // if (!CheckNull.isEmpty(awardRand)) {
        //
        // }
        // 其他奖励
        if (!CheckNull.isEmpty(otherAward)) {
            rewardDataManager.addAwardDelaySync(p, otherAward, null, AwardFrom.DO_MUlLCOMBAT_AWARD,
                    sMultCombat.getCombatId());
            if (showAward != null) {
                for (List<Integer> aw : otherAward) {
                    List<Integer> e = new ArrayList<>(aw);
                    showAward.add(e);
                }
            }
        }
    }

    private List<List<Integer>> processPassRandAward(List<List<Integer>> passRand) {
        if (!CheckNull.isEmpty(passRand)) {
            List<Integer> award = RandomUtil.getRandomByWeightAndRatio(passRand, 3, false,
                    (int) Constant.TEN_THROUSAND);
            if (!CheckNull.isEmpty(award)) {
                List<List<Integer>> awards = new ArrayList<>(1);
                awards.add(award);
                return awards;
            }
        }
        return null;
    }

    /**
     * 是否可以的到合作奖励
     *
     * @param players
     * @param combatId
     * @return
     */
    private boolean hasTeamAward(List<Player> players, int combatId) {
        int allPassCnt = 0; // 首通和今日通都完成
        for (Player p : players) {
            MultCombat multCombat = p.getAndCreateMultCombat();
            multCombat.refresh(); // 刷新
            if (multCombat.getHighestCombatId() >= combatId) {
                allPassCnt++;
            }
        }
        return allPassCnt != players.size();
    }

    /**
     * 多人副本扫荡
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public WipeMultCombatRs wipeMultCombat(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        MultCombat multCombat = player.getAndCreateMultCombat();
        multCombat.refresh();
        if (multCombat.getHighestCombatId() == 0) { // 没有可扫荡的关卡
            throw new MwException(GameError.PITCH_COMBAT_NOT_WIPE.getCode(), "关卡不能扫荡   highestCombatId:",
                    multCombat.getHighestCombatId(), ", roleId:", roleId);
        }
        if (multCombat.getWipeCnt() >= 1) {
            throw new MwException(GameError.PITCH_COMBAT_WIPE_CNT_NOT_ENOUGH.getCode(), "关卡扫荡点数不足    roleId:", roleId);
        }
        int highestCombatId = multCombat.getHighestCombatId();
        List<List<Integer>> showAward = new ArrayList<>();
        for (StaticMultCombat smc : StaticCombatDataMgr.getMultCombatMap().values()) {
            if (smc.getCombatId() <= highestCombatId && !multCombat.getTodayCombatId().contains(smc.getCombatId())) {
                multCombatAward(player, smc, false, showAward);
                multCombat.updateCombatId(smc.getCombatId());
            }
        }
        multCombat.setWipeCnt(multCombat.getWipeCnt() + 1);
        WipeMultCombatRs.Builder builder = WipeMultCombatRs.newBuilder();
        builder.addAllAward(PbHelper.createAwardsPb(showAward));
        builder.setMc(multCombat.ser());
        return builder.build();
    }

    /**
     * 多人副本商店购买
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public MultCombatShopBuyRs multCombatShopBuy(long roleId, MultCombatShopBuyRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        final int shopId = req.getShopId();
        StaticMultcombatShop sMultcombatShop = StaticShopDataMgr.getMultCombatShopMap().get(shopId);
        if (sMultcombatShop == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找到多人演练场商品id shopId:", shopId, ", roleId:", roleId);
        }
        MultCombat multCombat = player.getMultCombat();
        if (multCombat == null || multCombat.getCombatPoint() < sMultcombatShop.getCost()) {
            throw new MwException(GameError.BUY_MENTOR_SHOP_POINT_NOT_ENOUGH.getCode(), "购买积分不足 shopId:", shopId,
                    ", roleId:", roleId);
        }
        if (sMultcombatShop.isCombatIdCondEnable()) {
            if (multCombat.getHighestCombatId() < sMultcombatShop.getCombatIdCond()) {
                throw new MwException(GameError.BUY_MENTOR_COMBATID_NOT_COND.getCode(), "副本条件为达到 shopId:", shopId,
                        ", highestCombatId:", multCombat.getHighestCombatId(), ", roleId:", roleId);
            }
        }

        MultCombatShopBuyRs.Builder builder = MultCombatShopBuyRs.newBuilder();
        if (sMultcombatShop.isBuyCntEnable()) {
            int cnt = multCombat.getBuyCnt().getOrDefault(shopId, 0);
            if (cnt >= sMultcombatShop.getBuyCnt()) {
                throw new MwException(GameError.BUY_MENTOR_NOT_BUY_CNT.getCode(), "没有购买次数 shopId:", shopId, ", roleId:",
                        roleId);
            }
            int newCnt = cnt + 1;
            multCombat.getBuyCnt().put(shopId, newCnt);
            builder.setBuyCnt(PbHelper.createTwoIntPb(shopId, newCnt));
        }
        // 扣积分
        multCombat.subCombatPoint(sMultcombatShop.getCost());
        // 给奖励
        Award award = rewardDataManager.addAwardSignle(player, sMultcombatShop.getAward(), AwardFrom.BUY_MENTOR_SHOP,
                shopId);
        builder.addAward(award);
        builder.setPoints(multCombat.getCombatPoint());
        return builder.build();
    }

    /**
     * 检测是否是队长
     *
     * @param player
     * @return true是队长
     */
    private boolean checkIsCaptain(Player player) {
        MultCombat multCombat = player.getMultCombat();
        if (multCombat != null) {
            int teamId = multCombat.getTeamId();
            if (teamId > 0) {
                MultCombatTeam mcTeam = combatTeams.get(teamId);
                return mcTeam != null && mcTeam.getCaptainRoleId() == player.roleId;
            }
        }
        return false;
    }

}
