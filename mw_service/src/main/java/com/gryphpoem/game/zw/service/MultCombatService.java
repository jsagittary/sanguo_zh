package com.gryphpoem.game.zw.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.Java8Utils.SendMsgToPlayerCallback;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticCombatDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticShopDataMgr;
import com.gryphpoem.game.zw.logic.FightSettleLogic;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.RptHero;
import com.gryphpoem.game.zw.pb.GamePb2.CreateCombatTeamRq;
import com.gryphpoem.game.zw.pb.GamePb2.CreateCombatTeamRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetMultCombatRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetTeamMemberListRs;
import com.gryphpoem.game.zw.pb.GamePb2.JoinCombatTeamRq;
import com.gryphpoem.game.zw.pb.GamePb2.JoinCombatTeamRs;
import com.gryphpoem.game.zw.pb.GamePb2.LeaveCombatTeamRs;
import com.gryphpoem.game.zw.pb.GamePb2.ModifyCombatTeamRq;
import com.gryphpoem.game.zw.pb.GamePb2.ModifyCombatTeamRs;
import com.gryphpoem.game.zw.pb.GamePb2.MultCombatShopBuyRq;
import com.gryphpoem.game.zw.pb.GamePb2.MultCombatShopBuyRs;
import com.gryphpoem.game.zw.pb.GamePb2.SendInvitationRq;
import com.gryphpoem.game.zw.pb.GamePb2.SendInvitationRs;
import com.gryphpoem.game.zw.pb.GamePb2.StartMultCombatRs;
import com.gryphpoem.game.zw.pb.GamePb2.SyncCombatTeamRs;
import com.gryphpoem.game.zw.pb.GamePb2.SyncInvitationRs;
import com.gryphpoem.game.zw.pb.GamePb2.SyncMultCombatReportRs;
import com.gryphpoem.game.zw.pb.GamePb2.TickTeamMemberRq;
import com.gryphpoem.game.zw.pb.GamePb2.TickTeamMemberRs;
import com.gryphpoem.game.zw.pb.GamePb2.WipeMultCombatRs;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.ChatConst;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.FunctionConstant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.MultCombat;
import com.gryphpoem.game.zw.resource.domain.p.MultCombatTeam;
import com.gryphpoem.game.zw.resource.domain.s.StaticMultCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticMultcombatShop;
import com.gryphpoem.game.zw.resource.pojo.Trophy;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.RandomUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @ClassName MultCombatService.java
 * @Description ????????????
 * @author QiuKun
 * @date 2018???12???25???
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
    // ???????????? <teamId,MultCombatTeam>
    private Map<Integer, MultCombatTeam> combatTeams = new ConcurrentHashMap<>();

    /**
     * ??????????????????
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public CreateCombatTeamRs createCombatTeam(long roleId, CreateCombatTeamRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        MultCombat multCombat = player.getAndCreateMultCombat(); // ?????????????????????
        int combatId = req.getCombatId();

        // ??????????????????
        checkCombatllegal(player, combatId);

        if (multCombat.getTeamId() != 0) {
            throw new MwException(GameError.MULTCOMBAT_IN_TEAM_STATUS.getCode(), "??????????????????, roleId:" + roleId);
        }
        MultCombatTeam team = createTeam(player, multCombat, combatId);

        CreateCombatTeamRs.Builder builder = CreateCombatTeamRs.newBuilder();
        builder.setTeam(PbHelper.createMultCombatTeamPb(team, playerDataManager));
        return builder.build();
    }

    /**
     * ????????????
     * 
     * @param roleId
     * @param multCombat
     * @param combatId
     * @return
     */
    private MultCombatTeam createTeam(Player player, MultCombat multCombat, int combatId) {
        // ????????????
        MultCombatTeam team = MultCombatTeam.createMultTeam(player.roleId, combatId);
        combatTeams.put(team.getTeamId(), team);
        multCombat.setTeamId(team.getTeamId()); // ??????????????????
        int now = TimeHelper.getCurrentSecond();
        if (now - multCombat.getChatCd() > Constant.COMBAT_TEAM_CHAT_CD) { // cd??????
            // chatDataManager.sendSysChat(ChatConst.CHAT_COMBAT_TEAM, player.lord.getCamp(), 0, player.lord.getNick(),
            // combatId, team.getTeamId());
            multCombat.setChatCd(now);
        }
        return team;
    }

    /**
     * ????????????????????????(????????????,????????????)
     * 
     * @param roleId
     * @param req ???command?????? 1.???????????? 2.???????????????????????? 3.?????????????????????????????????
     * @return
     * @throws MwException
     */
    public ModifyCombatTeamRs modifyCombatTeam(long roleId, ModifyCombatTeamRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        final int command = req.getCommand();

        MultCombatTeam mcTeam = checkAndGetTeam(player); // ????????????
        // ?????????????????????
        if (!checkIsCaptain(player)) {
            throw new MwException(GameError.YOU_ARE_NOT_CAPTAIN.getCode(), "????????????????????????, roleId:", roleId);
        }

        ModifyCombatTeamRs.Builder builder = ModifyCombatTeamRs.newBuilder();
        if (command == 1) {// ????????????
            breakupTeam(mcTeam);
            syncCombatTeam(1, mcTeam, roleId);
        } else if (command == 2) {// ????????????????????????
            mcTeam.setAutoStart(!mcTeam.isAutoStart());
            syncCombatTeam(mcTeam, roleId);
        } else if (command == 3) {// ?????????????????????????????????
            mcTeam.setAutoJoin(!mcTeam.isAutoJoin());
            syncCombatTeam(mcTeam, roleId);
        } else {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "command?????????, roleId:", roleId, ", command:",
                    command);
        }
        builder.setTeam(PbHelper.createMultCombatTeamPb(mcTeam, playerDataManager));
        return builder.build();
    }

    /**
     * ????????????
     * 
     * @param mcTeam
     */
    private void breakupTeam(MultCombatTeam mcTeam) {
        mcTeam.getTeamMember().forEach(roleId -> {
            Player player = playerDataManager.getPlayer(roleId);
            if (player != null) {
                MultCombat multCombat = player.getAndCreateMultCombat();
                multCombat.setTeamId(0); // ??????????????????team
            }
        });
        combatTeams.remove(mcTeam.getTeamId());
    }

    /**
     * ???????????????????????????
     * 
     * @param player
     * @return
     * @throws MwException
     */
    private MultCombatTeam checkAndGetTeam(Player player) throws MwException {
        MultCombat multCombat = player.getAndCreateMultCombat();
        MultCombatTeam mcTeam = combatTeams.get(multCombat.getTeamId()); // ????????????
        if (mcTeam == null) {
            throw new MwException(GameError.TEAM_NOT_EXIST.getCode(), "???????????????");
        }
        return mcTeam;
    }

    /**
     * ???????????????
     * 
     * @param command
     * @param mcTeam 1.????????????, 2.??????????????????
     * @param processRoleId ???????????????,?????????0
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
     * ???????????????
     * 
     * @param mcTeam 1.????????????, 2.??????????????????
     * @param processRoleId
     */
    private void syncCombatTeam(MultCombatTeam mcTeam, final long processRoleId) {
        syncCombatTeam(0, mcTeam, processRoleId);
    }

    /**
     * ?????????????????????
     * 
     * @param player
     * @param command 1.????????????, 2.??????????????????
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
     * ??????????????????(??????????????????????????????);??????????????????
     * 
     * @param roleId
     * @param req teamId??????: ?????????????????????id; ???0??????????????????
     * @return
     * @throws MwException
     */
    public void joinCombatTeam(long roleId, JoinCombatTeamRq req, SendMsgToPlayerCallback<JoinCombatTeamRs> callback)
            throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        MultCombat multCombat = player.getAndCreateMultCombat(); // ?????????????????????
        if (multCombat.getTeamId() != 0) {
            throw new MwException(GameError.MULTCOMBAT_IN_TEAM_STATUS.getCode(), "??????????????????, roleId:", roleId, ", teamId:",
                    multCombat.getTeamId());
        }
        int teamId = req.getTeamId();
        JoinCombatTeamRs.Builder builder = JoinCombatTeamRs.newBuilder();
        if (teamId <= 0) { // ????????????
            final int combatId = req.getCombatId();
            checkCombatllegal(player, combatId);
            // ?????????????????????
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
                // ??????????????????????????????
                mcTeam = createTeam(player, multCombat, combatId);
            } else {// ????????????????????????
                mcTeam.getTeamMember().add(player.roleId);
                multCombat.setTeamId(mcTeam.getTeamId());
                syncCombatTeam(mcTeam, player.roleId);
            }
            teamId = mcTeam.getTeamId();
            builder.setTeam(PbHelper.createMultCombatTeamPb(mcTeam, playerDataManager));
        } else { // ??????????????????
            MultCombatTeam mcTeam = combatTeams.get(teamId);
            if (mcTeam == null) {
                throw new MwException(GameError.TEAM_NOT_EXIST.getCode(), "???????????????????????????, roleId:", roleId, ", teamId:",
                        teamId);
            }
            if (mcTeam.getTeamMember().size() >= Constant.MEMBER_OF_COMBAT_TEAM) {// ??????????????????
                throw new MwException(GameError.TEAM_MEMBER_IS_FULL.getCode(), "??????????????????, roleId:", roleId, ", teamId:",
                        teamId);
            }
            Player tp = playerDataManager.getPlayer(mcTeam.getCaptainRoleId());
            if (tp.lord.getCamp() != player.lord.getCamp()) {
                throw new MwException(GameError.TEAM_NOT_EXIST.getCode(), "?????????????????????, roleId:", roleId, ", teamId:",
                        teamId);
            }
            teamId = mcTeam.getTeamId();
            // ????????????????????????
            checkCombatllegal(player, mcTeam.getCombatId());
            // ????????????
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
     * ??????????????????
     * 
     * @param mcTeam
     */
    private void checkAutoStart(int teamId) {
        // ??????????????????
        MultCombatTeam mcTeam = combatTeams.get(teamId);
        if (mcTeam != null && mcTeam.isAutoStart() && mcTeam.getTeamMember().size() >= Constant.MEMBER_OF_COMBAT_TEAM) {
            processMultCombatFight(mcTeam);
        }
    }

    /**
     * ????????????????????????
     * 
     * @param player
     * @param combatId ????????????
     * @throws MwException
     */
    private void checkCombatllegal(Player player, int combatId) throws MwException {
        long roleId = player.roleId;
        MultCombat multCombat = player.getAndCreateMultCombat();
        if (!checkMultCombatLock(player)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "??????????????????????????? , roleId:" + roleId);
        }
        StaticMultCombat sMultCombat = StaticCombatDataMgr.getMultCombatById(combatId);
        if (sMultCombat == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "??????????????????????????? , roleId:" + roleId, ", combatId:",
                    combatId);
        }
        if (multCombat.getHighestCombatId() < sMultCombat.getPreId()) {
            throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), "?????????????????????, roleId:" + roleId);
        }

    }

    /**
     * ????????????
     * 
     * @param roleId
     * @return
     * @throws MwException
     */
    public LeaveCombatTeamRs leaveCombatTeam(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        MultCombat multCombat = player.getAndCreateMultCombat(); // ?????????????????????
        if (multCombat.getTeamId() == 0) {
            throw new MwException(GameError.MULTCOMBAT_NOT_IN_TEAM_STATUS.getCode(), "?????????????????????, roleId:" + roleId);
        }
        MultCombatTeam mcTeam = combatTeams.get(multCombat.getTeamId());
        if (mcTeam == null) {
            multCombat.setTeamId(0); // ????????????????????????
        } else {
            if (!mcTeam.getTeamMember().contains(roleId)) {// ??????????????????????????????,??????????????????????????????
                multCombat.setTeamId(0);
                // ????????????????????????????????????
                LogUtil.error("??????????????????????????????,?????????????????????????????? roleId:", roleId, ", teamId:", mcTeam.getTeamId());
            } else {
                if (roleId == mcTeam.getCaptainRoleId()) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), "????????????????????????,???????????? roleId:" + roleId);
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
     * ????????????
     * 
     * @param roleId
     * @return
     * @throws MwException
     */
    public TickTeamMemberRs tickTeamMember(long roleId, TickTeamMemberRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        long tickRoleId = req.getRoleId();
        MultCombatTeam mcTeam = checkAndGetTeam(player);
        // ?????????????????????
        if (!checkIsCaptain(player)) {
            throw new MwException(GameError.YOU_ARE_NOT_CAPTAIN.getCode(), "????????????????????????, roleId:", roleId);
        }
        if (mcTeam.getCaptainRoleId() == tickRoleId) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "????????????, tickRoleId:", tickRoleId);
        }
        if (!mcTeam.getTeamMember().contains(tickRoleId)) {
            throw new MwException(GameError.TEAM_MEMBER_NOT_EXIST.getCode(), "????????????????????????, tickRoleId:", tickRoleId);
        }
        // ??????
        mcTeam.getTeamMember().remove(Long.valueOf(tickRoleId));
        Player tickPlayer = playerDataManager.getPlayer(tickRoleId);
        if (tickPlayer != null) {
            MultCombat tickMCombat = tickPlayer.getAndCreateMultCombat();
            tickMCombat.setTeamId(0);
            // ??????????????????
            syncCombatTeam(tickPlayer, 2, mcTeam);
        }
        TickTeamMemberRs.Builder builder = TickTeamMemberRs.newBuilder();
        builder.setTeam(PbHelper.createMultCombatTeamPb(mcTeam, playerDataManager));
        mcTeam.getTeamMember().stream().filter(rid -> rid.longValue() != roleId)
                .map(rid -> playerDataManager.getPlayer(rid)).forEach(p -> syncCombatTeam(p, 0, mcTeam));
        return builder.build();
    }

    /**
     * ????????????????????????
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
     * ????????????????????????
     * 
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetTeamMemberListRs getTeamMemberList(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        MultCombatTeam mcTeam = checkAndGetTeam(player);
        // ?????????????????????
        if (!checkIsCaptain(player)) {
            throw new MwException(GameError.YOU_ARE_NOT_CAPTAIN.getCode(), "????????????????????????, roleId:", roleId);
        }
        int combatId = mcTeam.getCombatId();
        StaticMultCombat sMultCombat = StaticCombatDataMgr.getMultCombatById(combatId);
        if (sMultCombat == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????????????????, roleId:", roleId, ", combatId:", combatId);
        }
        // ???????????? ????????????????????????
        List<Player> showList = playerDataManager.getPlayerByCamp(player.lord.getCamp()).values().stream()
                .filter(p -> roleId != p.roleId
                        && p.getAndCreateMultCombat().getHighestCombatId() >= sMultCombat.getPreId()
                        && checkMultCombatLock(p))
                .sorted((p1, p2) -> {
                    // ???????????? > ?????????
                    // ?????????1?????? ,????????????0
                    int p1Online = p1.isLogin ? 1 : 0;
                    int p2Online = p2.isLogin ? 1 : 0;
                    // ???????????????????????????
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
     * ??????????????????
     * 
     * @param p
     * @return
     */
    private boolean checkMultCombatLock(Player p) {
        // ????????????
        return StaticFunctionDataMgr.funcitonIsOpen(p, FunctionConstant.UNLOCK_TYPE_MULTCOMBAT);
    }

    /**
     * ????????????
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
        // ?????????????????????
        if (!checkIsCaptain(player)) {
            throw new MwException(GameError.YOU_ARE_NOT_CAPTAIN.getCode(), "????????????????????????, roleId:", roleId);
        }
        int combatId = mcTeam.getCombatId();
        StaticMultCombat sMultCombat = StaticCombatDataMgr.getMultCombatById(combatId);
        if (sMultCombat == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????????????????, roleId:", roleId, ", combatId:", combatId);
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
     * ??????????????????
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
     * ??????????????????
     * 
     * @param roleId
     * @return
     * @throws MwException
     */
    public StartMultCombatRs startMultCombat(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        MultCombatTeam mcTeam = checkAndGetTeam(player);
        // ?????????????????????
        if (!checkIsCaptain(player)) {
            throw new MwException(GameError.YOU_ARE_NOT_CAPTAIN.getCode(), "????????????????????????, roleId:", roleId);
        }
        int combatId = mcTeam.getCombatId();
        StaticMultCombat sMultCombat = StaticCombatDataMgr.getMultCombatById(combatId);
        if (sMultCombat == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????????????????, roleId:", roleId, ", combatId:", combatId);
        }
        // ????????????
        processMultCombatFight(mcTeam);
        StartMultCombatRs.Builder builder = StartMultCombatRs.newBuilder();
        return builder.build();
    }

    /**
     * ??????????????????
     * 
     * @param mcTeam
     */
    private void processMultCombatFight(MultCombatTeam mcTeam) {
        StaticMultCombat sMultCombat = StaticCombatDataMgr.getMultCombatById(mcTeam.getCombatId());
        // ???????????????????????????
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
        fightLogic.fight();
        // ????????????????????????
        // boolean hasTeamAward = hasTeamAward(players, sMultCombat.getCombatId());
        List<RptHero> rptHeroList = fightSettleLogic.combatCreateRptHeroNoAward(attacker.getForces());
        for (Player p : players) {
            List<List<Integer>> showAward = new ArrayList<>();
            MultCombat multCombat = p.getAndCreateMultCombat();
            if (fightLogic.getWinState() == 1) { // ??????
                boolean hasTeamAward = mcTeam.getCaptainRoleId() != p.lord.getLordId();// ?????????????????????,????????????????????????
                multCombatAward(p, sMultCombat, hasTeamAward, showAward);
                multCombat.updateCombatId(sMultCombat.getCombatId()); // ????????????
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
            syncMultCombatReport(p, dataBuilder);// ????????????
        }
        if (fightLogic.getWinState() == 1) {
            // ??????????????????????????????
            sendPassChat(mcTeam);
        }
        // ????????????
        breakupTeam(mcTeam);
    }

    /**
     * ??????????????????????????????
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
     * ????????????
     * 
     * @param p
     * @param sMultCombat
     * @param hasTeamAward
     * @param showAward
     */
    private void multCombatAward(Player p, StaticMultCombat sMultCombat, boolean hasTeamAward,
            List<List<Integer>> showAward) {
        MultCombat multCombat = p.getAndCreateMultCombat();
        int pointAward = 0; // ??????????????????
        // List<List<Integer>> awardRand = null; // ???????????????
        List<List<Integer>> otherAward = new ArrayList<>();// ????????????
        if (multCombat.getHighestCombatId() < sMultCombat.getCombatId()) { // ????????????
            pointAward = sMultCombat.getFirstPassPoint();
            // awardRand = sMultCombat.getFirstPassRand();
            otherAward.addAll(sMultCombat.getFirstPassAward());
        } else if (!multCombat.getTodayCombatId().contains(sMultCombat.getCombatId())) {// ????????????
            pointAward = sMultCombat.getPassPoint();
            // ??????????????????
            // awardRand = sMultCombat.getPassAward();
            List<List<Integer>> passRandAward = processPassRandAward(sMultCombat.getPassAward());
            if (passRandAward != null) {
                otherAward.addAll(passRandAward);
            }
        }

        if (hasTeamAward) {// ????????????
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
        // ?????????????????????,??????
        // if (!CheckNull.isEmpty(awardRand)) {
        //
        // }
        // ????????????
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
     * ??????????????????????????????
     * 
     * @param players
     * @param combatId
     * @return
     */
    private boolean hasTeamAward(List<Player> players, int combatId) {
        int allPassCnt = 0; // ???????????????????????????
        for (Player p : players) {
            MultCombat multCombat = p.getAndCreateMultCombat();
            multCombat.refresh(); // ??????
            if (multCombat.getHighestCombatId() >= combatId) {
                allPassCnt++;
            }
        }
        return allPassCnt != players.size();
    }

    /**
     * ??????????????????
     * 
     * @param roleId
     * @return
     * @throws MwException
     */
    public WipeMultCombatRs wipeMultCombat(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        MultCombat multCombat = player.getAndCreateMultCombat();
        multCombat.refresh();
        if (multCombat.getHighestCombatId() == 0) { // ????????????????????????
            throw new MwException(GameError.PITCH_COMBAT_NOT_WIPE.getCode(), "??????????????????   highestCombatId:",
                    multCombat.getHighestCombatId(), ", roleId:", roleId);
        }
        if (multCombat.getWipeCnt() >= 1) {
            throw new MwException(GameError.PITCH_COMBAT_WIPE_CNT_NOT_ENOUGH.getCode(), "????????????????????????    roleId:", roleId);
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
     * ????????????????????????
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
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????????????????id shopId:", shopId, ", roleId:", roleId);
        }
        MultCombat multCombat = player.getMultCombat();
        if (multCombat == null || multCombat.getCombatPoint() < sMultcombatShop.getCost()) {
            throw new MwException(GameError.BUY_MENTOR_SHOP_POINT_NOT_ENOUGH.getCode(), "?????????????????? shopId:", shopId,
                    ", roleId:", roleId);
        }
        if (sMultcombatShop.isCombatIdCondEnable()) {
            if (multCombat.getHighestCombatId() < sMultcombatShop.getCombatIdCond()) {
                throw new MwException(GameError.BUY_MENTOR_COMBATID_NOT_COND.getCode(), "????????????????????? shopId:", shopId,
                        ", highestCombatId:", multCombat.getHighestCombatId(), ", roleId:", roleId);
            }
        }

        MultCombatShopBuyRs.Builder builder = MultCombatShopBuyRs.newBuilder();
        if (sMultcombatShop.isBuyCntEnable()) {
            int cnt = multCombat.getBuyCnt().getOrDefault(shopId, 0);
            if (cnt >= sMultcombatShop.getBuyCnt()) {
                throw new MwException(GameError.BUY_MENTOR_NOT_BUY_CNT.getCode(), "?????????????????? shopId:", shopId, ", roleId:",
                        roleId);
            }
            int newCnt = cnt + 1;
            multCombat.getBuyCnt().put(shopId, newCnt);
            builder.setBuyCnt(PbHelper.createTwoIntPb(shopId, newCnt));
        }
        // ?????????
        multCombat.subCombatPoint(sMultcombatShop.getCost());
        // ?????????
        Award award = rewardDataManager.addAwardSignle(player, sMultcombatShop.getAward(), AwardFrom.BUY_MENTOR_SHOP,
                shopId);
        builder.addAward(award);
        builder.setPoints(multCombat.getCombatPoint());
        return builder.build();
    }

    /**
     * ?????????????????????
     * 
     * @param player
     * @return true?????????
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
