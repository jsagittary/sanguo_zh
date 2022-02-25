package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticChatDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFriendDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.Friend;
import com.gryphpoem.game.zw.pb.CommonPb.FriendHero;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb4.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.DbFriend;
import com.gryphpoem.game.zw.resource.domain.p.DbMasterApprentice;
import com.gryphpoem.game.zw.resource.domain.s.StaticCreditShop;
import com.gryphpoem.game.zw.resource.domain.s.StaticMasterReaward;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.chat.RoleChat;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.RandomUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName FriendService.java
 * @Description 好友师徒相关
 * @date 2017年6月27日
 */
@Service
public class FriendService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatDataManager chatDataManager;

    /**
     * 获取玩家好友数量
     *
     * @param player
     * @return
     */
    private int getFrinedCntByPlayer(Player player) {
        return (int) player.friends.values().stream().filter(f -> f.getState() >= 2).count();
    }

    /**
     * 获取玩家请求列表
     *
     * @param player
     * @return
     */
    private int frinedReqCntByPlayer(Player player) {
        return (int) player.friends.values().stream().filter(f -> f.getState() == DbFriend.STATE_WAIT_FRIEND_APPROVAL)
                .count();
    }

    /**
     * 自己待验证的好友列表
     *
     * @param player
     * @return
     */
    private int frinedRecCntByPlayer(Player player) {
        return (int) player.friends.values().stream().filter(f -> f.getState() == DbFriend.STATE_WAIT_SELF_APPROVAL)
                .count();
    }

    /**
     * 自己待验证列表中最早的
     *
     * @param player
     * @return
     */
    private long findRecRoleid(Player player) {
        DbFriend dbFriend = player.friends.values().stream()
                .filter(f -> f.getState() == DbFriend.STATE_WAIT_SELF_APPROVAL)
                .sorted(Comparator.comparingInt(DbFriend::getAddTime)).findFirst().orElse(null);
        if (dbFriend == null) {
            return 0L;
        } else {
            return dbFriend.getLordId();
        }
    }

    /**
     * 移除无效的好友
     *
     * @param player
     */
    public static void rmInvalidFriend(Player player) {
        if (player == null) return;
        for (Iterator<Entry<Long, DbFriend>> it = player.friends.entrySet().iterator(); it.hasNext(); ) {
            Entry<Long, DbFriend> kv = it.next();
            DbFriend df = kv.getValue();
            if (df.getState() == DbFriend.STATE_WAIT_FRIEND_APPROVAL
                    || df.getState() == DbFriend.STATE_WAIT_SELF_APPROVAL) {
                it.remove();
            }
        }
    }

    /**
     * 添加好友
     *
     * @param roleId
     * @param friendid
     * @return
     * @throws MwException
     */
    public AddFriendRs addFriend(long roleId, long friendid) throws MwException {
        if (roleId == friendid) {
            // 自己添加自己情况
            throw new MwException(GameError.FRIEND_HAD.getCode(), "不能添加自己为好友");
        }

        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Player friendPlayer = playerDataManager.checkPlayerIsExist(friendid);
        Map<Long, DbFriend> friendsMap = player.friends;
        // 好友已存在的情况
        if (player.friends.containsKey(friendid)) {
            DbFriend f = friendsMap.get(friendid);
            if (DbFriend.STATE_WAIT_FRIEND_APPROVAL == f.getState()) {
                throw new MwException(GameError.FRIEND_HAD.getCode(), "好友已存在 ,正在等待对方同意");
            } else if (DbFriend.STATE_WAIT_SELF_APPROVAL == f.getState()) {
                throw new MwException(GameError.FRIEND_HAD.getCode(), "好友已存在");
            } else {
                throw new MwException(GameError.FRIEND_HAD.getCode(), "好友已存在");
            }
        }
        // 好友已达到上限
        if (getFrinedCntByPlayer(player) >= Constant.MAX_FRIEND_COUNT) {
            throw new MwException(GameError.FRIEND_MAX_COUNT.getCode(), "你好友数量已达到上限");
        }
        // 今日请求好友列表达到上限
        if (frinedReqCntByPlayer(player) >= Constant.MAX_FRIEND_COUNT) {
            throw new MwException(GameError.FRIEND_MAX_COUNT.getCode(), "你请求好友列表数量已达到上限");
        }
        // 对方待认证列表超上限处理
        if (frinedRecCntByPlayer(friendPlayer) >= Constant.MAX_FRIEND_COUNT) {
            long oldReqRole = findRecRoleid(friendPlayer);
            friendPlayer.friends.remove(oldReqRole); // 移除对方待验证好友最老好友申请
            Player oldPlayer = playerDataManager.getPlayer(oldReqRole);
            if (!CheckNull.isNull(oldPlayer)) {
                // 删除老好友那边的请求
                oldPlayer.friends.remove(friendPlayer.roleId);
            }
        }
        AddFriendRs.Builder builder = AddFriendRs.newBuilder();
        if (getFrinedCntByPlayer(friendPlayer) >= Constant.MAX_FRIEND_COUNT) {
            throw new MwException(GameError.TARGET_FRIEND_MAX_COUNT.getCode(), "对方好友数量已达到上限");
            // builder.addAllFriends(getFriendListByPlayer(player));
            // return builder.build();
        }
        DbFriend myFriend = null;
        // 对方已有自己好友的情况->直接添加
        if (friendPlayer.friends.containsKey(roleId)) {
            int now = TimeHelper.getCurrentSecond();
            // 把好友添加到自己的好友列表中
            myFriend = new DbFriend(friendid, now, DbFriend.STATE_FRIEND);
            player.friends.put(friendid, myFriend);
        } else {
            int now = TimeHelper.getCurrentSecond();
            // 把好友添加到自己的好友列表中
            myFriend = new DbFriend(friendid, now, DbFriend.STATE_WAIT_FRIEND_APPROVAL);
            player.friends.put(friendid, myFriend);

            // 把自己添加对方的好友列表中
            DbFriend fFriend = new DbFriend(roleId, now, DbFriend.STATE_WAIT_SELF_APPROVAL);
            friendPlayer.friends.put(roleId, fFriend);
            syncFriendState(friendPlayer, 1);
        }
        if (myFriend != null) {
            Friend fpb = createFriendPb(myFriend);
            if (fpb != null) {
                builder.addFriends(fpb);
            }
        }
        return builder.build();
    }

    /**
     * 删除好友
     *
     * @param roleId
     * @param friendid
     * @return
     * @throws MwException
     */
    public DelFriendRs delFriend(long roleId, long friendid) throws MwException {
        Player friendPlayer = playerDataManager.checkPlayerIsExist(friendid);
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if (!player.friends.containsKey(friendid)) {
            throw new MwException(GameError.FRIEND_NOT_EXIST.getCode(), "好友不存在");
        }

        // 自己这边删除
        player.friends.remove(friendid);
        // 对方好友那边进行删除
        friendPlayer.friends.remove(roleId);

        DelFriendRs.Builder builder = DelFriendRs.newBuilder();
        return builder.build();
    }

    /**
     * 获取好友列表
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetFriendsRs getFriends(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        List<DbFriend> dbFriends = new ArrayList<>();
        dbFriends.addAll(player.friends.values());

        // 排序 先按状态排序 (状态为1在最前,2在中间,0在最后)如果状态相等就按照时间早晚排序
        // Collections.sort(dbFriends, new Comparator<DbFriend>() {
        //
        // @Override
        // public int compare(DbFriend o1, DbFriend o2) {
        // if (o1.getState() == o2.getState()) {
        // // 时间升序
        // if (o1.getAddTime() > o2.getAddTime()) {
        // return 1;
        // } else if (o1.getAddTime() < o2.getAddTime()) {
        // return -1;
        // } else {
        // return 0;
        // }
        // } else if (DbFriend.STATE_WAIT_SELF_APPROVAL == o1.getState()) {
        // // o1 要在 o2 之前(降序)
        // return -1;
        // } else if (DbFriend.STATE_WAIT_SELF_APPROVAL == o2.getState()) {
        // // o2 要在 o1 之前(降序)
        // return 1;
        // } else if (DbFriend.STATE_WAIT_FRIEND_APPROVAL == o1.getState()) {
        // // o1 要在 o2 之后
        // return 1;
        // } else if (DbFriend.STATE_WAIT_FRIEND_APPROVAL == o2.getState()) {
        // // o2 要在 o1 之后
        // return -1;
        // } else {
        // return 0;
        // }
        // }
        // });
        Set<Long> rmIds = new HashSet<>();
        GetFriendsRs.Builder builder = GetFriendsRs.newBuilder();
        for (DbFriend dbf : dbFriends) {
            long lordId = dbf.getLordId();
            Player fPlayer = playerDataManager.getPlayer(lordId);
            if (null == fPlayer) {
                // 记录无效的角色的好友
                rmIds.add(lordId);
                continue;
            }
            CommonPb.Man man = PbHelper.createManPbByLord(fPlayer);
            CommonPb.Friend friend = PbHelper.createFriendPb(man, dbf);
            builder.addFriends(friend);
        }
        builder.setMaxCount(Constant.MAX_FRIEND_COUNT);
        if (!rmIds.isEmpty()) {
            // 删除无效好友
            for (Long id : rmIds) {
                player.friends.remove(id);
            }
        }

        return builder.build();
    }

    /**
     * 同意或拒绝好友
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public AgreeRejectRs agreeReject(long roleId, AgreeRejectRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        boolean agree = req.getAgree();
        List<Player> awaitAgreeList;
        if (req.getIsAll() && !req.hasFriendId()) {
            awaitAgreeList = player.friends.values().stream()
                    .filter(f -> f.getState() == DbFriend.STATE_WAIT_SELF_APPROVAL)
                    .sorted(Comparator.comparingInt(DbFriend::getAddTime))
                    .map(f -> playerDataManager.getPlayer(f.getLordId()))
                    .filter(p -> {
                        if (CheckNull.isNull(p)) {
                            return false;
                        }
                        // 主要全部同意时候, 才过滤对方的好友已满的情况
                        if (agree && getFrinedCntByPlayer(p) >= Constant.MAX_FRIEND_COUNT) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        } else {
            awaitAgreeList = Collections.singletonList(playerDataManager.checkPlayerIsExist(req.getFriendId()));
        }

        for (Player friendPlayer : awaitAgreeList) {
            Long friendId = friendPlayer.roleId;
            if (agree) {
                // 同意,只需要修改状态
                DbFriend myFriend = player.friends.get(friendId);
                DbFriend fFriend = friendPlayer.friends.get(roleId);
                if (null == fFriend && null != myFriend) { //对方不是自己好友，可认定是系统发送的请求
                    // 已达到上限
                    if (getFrinedCntByPlayer(player) >= Constant.MAX_FRIEND_COUNT) {
                        syncFriendState(player, 1);
                        throw new MwException(GameError.FRIEND_MAX_COUNT.getCode(), "你好友数量已达到上限");
                    }
                    //修改状态
                    myFriend.setState(DbFriend.STATE_TMP);
                    //发送私聊
                    String content = StaticChatDataMgr.getFriendMessage();
                    RoleChat chat = (RoleChat) chatService.createRoleChat(player, content);
                    chat.setChannel(ChatConst.CHANNEL_PRIVATE);
                    chat.setSystem(true);
                    CommonPb.Chat b = chatDataManager.createPrivateChat(chat, roleId, friendId);

                    GamePb3.SyncChatRs.Builder chatBuilder = GamePb3.SyncChatRs.newBuilder();
                    chatBuilder.setChat(b);
                    Base.Builder builder = PbHelper.createSynBase(GamePb3.SyncChatRs.EXT_FIELD_NUMBER, GamePb3.SyncChatRs.ext,
                            chatBuilder.build());
                    MsgDataManager.getIns().add(new Msg(friendPlayer.ctx, builder.build(), friendId));

                } else {
                    if (null == myFriend) {
                        throw new MwException(GameError.FRIEND_NOT_EXIST.getCode(), "好友不在存在");
                    }
                    // 已达到上限
                    if (getFrinedCntByPlayer(player) >= Constant.MAX_FRIEND_COUNT) {
                        syncFriendState(player, 1);
                        throw new MwException(GameError.FRIEND_MAX_COUNT.getCode(), "你好友数量已达到上限");
                    }
                    if (getFrinedCntByPlayer(friendPlayer) >= Constant.MAX_FRIEND_COUNT) {
                        syncFriendState(player, 1);
                        throw new MwException(GameError.TARGET_FRIEND_MAX_COUNT.getCode(), "对方好友已经数量已达到上限");
                    }
                    // 双方变成已经是好友状态
                    myFriend.setState(DbFriend.STATE_FRIEND);
                    fFriend.setState(DbFriend.STATE_FRIEND);

                    // 检测之前是否是师徒关系
                    // 1.检测对方是否是自己的师傅
                    if (player.master != null && player.master.getLordId() == friendId) {
                        myFriend.setState(DbFriend.STATE_MASTER);
                        fFriend.setState(DbFriend.STATE_APPRENTICE);
                    }
                    // 2.检测对方是否是自己的徒弟
                    DbMasterApprentice apprentice = player.apprentices.get(friendId);
                    if (apprentice != null && apprentice.getStaus() == DbMasterApprentice.STATE_AGREE) {
                        myFriend.setState(DbFriend.STATE_APPRENTICE);
                        fFriend.setState(DbFriend.STATE_MASTER);
                    }
                }

                syncFriendState(friendPlayer, 1);
            } else {
                // 拒绝
                // 自己这边删除
                player.friends.remove(friendId);
                // 对方好友那边进行删除
                friendPlayer.friends.remove(roleId);
            }
        }

        AgreeRejectRs.Builder builder = AgreeRejectRs.newBuilder();
        builder.addAllFriends(getFriendListByPlayer(player));
        return builder.build();
    }


    private List<CommonPb.Friend> getFriendListByPlayer(Player player) {
        List<CommonPb.Friend> friends = new ArrayList<>();
        for (DbFriend dbf : player.friends.values()) {
            long lordId = dbf.getLordId();
            Player fPlayer = playerDataManager.getPlayer(lordId);
            if (null == fPlayer) {
                continue;
            }
            CommonPb.Man man = PbHelper.createManPbByLord(fPlayer);
            CommonPb.Friend friend = PbHelper.createFriendPb(man, dbf);
            friends.add(friend);
        }
        return friends;
    }

    private CommonPb.Friend createFriendPb(DbFriend dbf) {
        Player fPlayer = playerDataManager.getPlayer(dbf.getLordId());
        if (fPlayer != null) {
            CommonPb.Man man = PbHelper.createManPbByLord(fPlayer);
            CommonPb.Friend friend = PbHelper.createFriendPb(man, dbf);
            return friend;
        }
        return null;
    }

    /**
     * 获取好友详情(也可以用于查看玩家信息)
     *
     * @param roleId
     * @param friendid
     * @return
     * @throws MwException
     */
    public CheckFirendRs getFriendDetail(long roleId, long friendid) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Player fPlayer = playerDataManager.checkPlayerIsExist(friendid);

        CommonPb.Man man = PbHelper.createManPbByLord(fPlayer);

        CheckFirendRs.Builder builder = CheckFirendRs.newBuilder();

        List<FriendHero> heros = new ArrayList<>();
        for (Hero h : fPlayer.getAllOnBattleHeros()) {
            heros.add(PbHelper.createFriendAndHeroPb(h, fPlayer));
        }
        CommonPb.Friend friend = null;
        if (player.friends.containsKey(friendid)) {
            DbFriend dbf = player.friends.get(friendid);
            friend = PbHelper.createFriendAndHeroPb(man, heros, dbf);
        } else {
            // 不是好友的情况
            CommonPb.Friend.Builder fb = CommonPb.Friend.newBuilder();
            fb.setMan(man);
            fb.addAllHero(heros);
            friend = fb.build();
        }

        builder.setFriend(friend);
        return builder.build();
    }

    /**
     * 获取师徒信息
     *
     * @param roleId
     * @return
     */
    public GetMasterApprenticeRs getMasterApprentice(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        List<DbMasterApprentice> apprentices = new ArrayList<>();
        apprentices.addAll(player.apprentices.values());

        GetMasterApprenticeRs.Builder builder = GetMasterApprenticeRs.newBuilder();
        // 添加师傅
        if (player.master != null) {
            Player masterPlayer = playerDataManager.getPlayer(player.master.getLordId());
            if (null != masterPlayer) {
                CommonPb.Man masterMan = PbHelper.createManPbByLord(masterPlayer);
                CommonPb.MasterApprentice masterPb = PbHelper.createMasterApprenticePb(masterMan, player.master, masterPlayer);
                builder.setMaster(masterPb);
            }
        }
        Set<Long> rmIds = new HashSet<>();
        // 添加徒弟
        for (DbMasterApprentice apprentice : apprentices) {
            long lordId = apprentice.getLordId();
            Player aPlayer = playerDataManager.getPlayer(lordId);
            if (null == aPlayer) {
                rmIds.add(lordId);
                continue;
            }
            CommonPb.Man apprenticeMan = PbHelper.createManPbByLord(aPlayer);
            CommonPb.MasterApprentice apprenticePb = PbHelper.createMasterApprenticePb(apprenticeMan, apprentice, aPlayer);
            builder.addApprentices(apprenticePb);
        }
        if (!rmIds.isEmpty()) {
            // 删除无效徒弟
            for (Long id : rmIds) {
                player.apprentices.remove(id);
            }
        }

        builder.setMaxCount(Constant.MAX_APPRENTICE_COUNT);
        builder.setCredit(player.lord.getCredit());
        builder.addAllAwardedIds(PbHelper.createTwoIntListByMap(player.awardedIds));
        builder.addAllBoughtIds(player.boughtIds);
        return builder.build();
    }

    /**
     * 拜师
     *
     * @param roleId
     * @param masterId 师傅的角色id
     * @return
     * @throws MwException
     */
    public AddMasterRs addMasterRs(long roleId, long masterId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if (roleId == masterId) {
            throw new MwException(GameError.CAN_ADD_MASTER_SELF.getCode(), "不能拜自己为师, roleId:", roleId);
        }
        // 检测自己成为徒弟条件是否满足
        if (player.master != null) {
            throw new MwException(GameError.MASTER_HAD.getCode(), "师傅已存在");
        }
        // 不能拜自己的徒弟为师傅
        if (player.apprentices.containsKey(masterId)) {
            throw new MwException(GameError.APPRENTICE_NOT_BECOME_MASTER.getCode(), "不能拜自己的徒弟为师傅");
        }
        // 成为徒弟等级条件
        if (player.lord.getLevel() > Constant.MAX_APPRENTICE_LV) {
            throw new MwException(GameError.APPRENTICE_LV_ERR.getCode(), "当前等级过高,无法拜师");
        }

        // 最近解除师傅的时间
        int delMasterTime = player.getMixtureDataById(PlayerConstant.DEL_MASTER_TIME);
        int nowSecond = TimeHelper.getCurrentSecond();
        if (delMasterTime > 0 && nowSecond - delMasterTime < (Constant.ADD_MASTER_AGAIN_CD_TIME * TimeHelper.HOUR_S)) {
            throw new MwException(GameError.ADD_MASTER_CD_TIME_NOT_YET.getCode(), "解除师傅后, 拜师的CD时间未到, now: ", nowSecond, ", delMasterTime: ", delMasterTime);
        }

        // 检测对方是否满足成为师傅的条件
        Player mPlayer = playerDataManager.checkPlayerIsExist(masterId);

        if (mPlayer.lord.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NO_CAMP_NOT_BECOME_MASTER.getCode(), "非同一阵营不能拜师, roleId:", roleId,
                    ", friendId:", mPlayer.lord);
        }
        if (mPlayer.lord.getLevel() < Constant.MIN_MASTER_LV) {
            throw new MwException(GameError.MASTER_LV_ERR.getCode(), "当前好友等级不足,无法拜师 ");
        }
        if (getApprenticeCnt(mPlayer) >= Constant.MAX_APPRENTICE_COUNT) {
            throw new MwException(GameError.APPRENTICE_MAX_COUNT.getCode(), "当前好友徒弟人数已满，无法拜师");
        }

        // 过滤无效的师徒请求
        if (mPlayer.apprentices.containsKey(player.roleId)) {
            DbMasterApprentice masterApprentice = mPlayer.apprentices.get(player.roleId);
            if (DbMasterApprentice.STATE_WAIT_APPROVAL == masterApprentice.getStaus()) {
                throw new MwException(GameError.REPEATED_ADD_MASTER.getCode(), "拜师请求已存在, 正在等待对方同意");
            } else {
                throw new MwException(GameError.MASTER_HAD.getCode(), "已经是对方的徒弟了");
            }
        }

        // 给师傅发送拜师请求
        mPlayer.apprentices.put(player.roleId, new DbMasterApprentice(player.roleId, nowSecond, DbMasterApprentice.RELATION_APPRENTICE));
        syncFriendState(mPlayer, 2);

        AddMasterRs.Builder builder = AddMasterRs.newBuilder();
        return builder.build();
    }

    /**
     * 获取徒弟的数量
     *
     * @param player
     * @return
     */
    public int getApprenticeCnt(Player player) {
        return (int) player.apprentices.values().stream().filter(m -> m.getStaus() == DbMasterApprentice.STATE_AGREE).count();
    }


    /**
     * 同意或拒绝收徒
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public AgreeRejectMasterRs agreeRejectMaster(long roleId, AgreeRejectMasterRq req) throws MwException {
        // 师傅
        Player mPlayer = playerDataManager.checkPlayerIsExist(roleId);

        // 是否同意
        boolean agree = req.getAgree();

        List<Player> apprentices;
        if (req.getIsAll() && !req.hasRoleId()) {
            apprentices = mPlayer.apprentices.values().stream()
                    // 过滤状态为 未同意的
                    .filter(m -> m.getStaus() == DbMasterApprentice.STATE_WAIT_APPROVAL)
                    // 根据时间来排序
                    .sorted(Comparator.comparingInt(DbMasterApprentice::getCreateTime))
                    .map(f -> playerDataManager.getPlayer(f.getLordId()))
                    .filter(p -> {
                        if (CheckNull.isNull(p)) {
                            return false;
                        }
                        // 全部同意的时候才过滤玩家师傅
                        if (agree && p.master != null) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        } else {
            apprentices = Collections.singletonList(playerDataManager.checkPlayerIsExist(req.getRoleId()));
        }

        // 现在的时间
        int now = TimeHelper.getCurrentSecond();

        for (Player aPlayer : apprentices) {
            if (agree) {
                // 同意收徒
                if (getApprenticeCnt(mPlayer) >= Constant.MAX_APPRENTICE_COUNT) {
                    syncFriendState(mPlayer, 2);
                    throw new MwException(GameError.APPRENTICE_MAX_COUNT.getCode(), "当前好友徒弟人数已满，无法拜师");
                }
                if (aPlayer.master != null) {
                    throw new MwException(GameError.MASTER_HAD.getCode(), "师傅已存在");
                }

                // 收徒
                DbMasterApprentice dbApprentice = mPlayer.apprentices.get(aPlayer.roleId);
                dbApprentice.setStaus(DbMasterApprentice.STATE_AGREE);
                mPlayer.friends.computeIfPresent(aPlayer.roleId, (k, v) -> {
                    v.setState(DbFriend.STATE_APPRENTICE);
                    return v;
                });
                activityDataManager.updDay7ActSchedule(mPlayer, ActivityConst.ACT_TASK_APPRENTICES);

                // 拜师
                aPlayer.master = new DbMasterApprentice(mPlayer.roleId, now, DbMasterApprentice.RELATION_MASTER, DbMasterApprentice.STATE_AGREE);
                aPlayer.friends.computeIfPresent(mPlayer.roleId, (k, v) -> {
                    v.setState(DbFriend.STATE_MASTER);
                    return v;
                });
                activityDataManager.updDay7ActSchedule(aPlayer, ActivityConst.ACT_TASK_MASTER);


                // 给师傅发收徒邮件
                mailDataManager.sendNormalMail(mPlayer, MailConstant.MOLD_ADD_APPRENTICE, now, aPlayer.lord.getNick(),
                        aPlayer.lord.getNick());

                // 师徒奖励
                boolean award = aPlayer.getMixtureDataById(PlayerConstant.DEL_MASTER_TIME) == 0;

                // 给徒弟发奖励邮件
                List<Award> awards = null;
                if (award) {
                    if (!CheckNull.isEmpty(ActParamConstant.ACT_TUTOR_RANK_CONF)) {
                        // 徒弟的等级大于需要的等级, 就可以获取积分
                        int integral = ActParamConstant.ACT_TUTOR_RANK_CONF.stream().filter(conf -> aPlayer.lord.getLevel() >= conf.get(0)).mapToInt(conf -> conf.get(1)).sum();
                        if (integral > 0) {
                            // 给导师加积分
                            activityDataManager.updRankActivity(mPlayer, ActivityConst.ACT_TUTOR_RANK, integral);
                        }
                    }
                    awards = PbHelper.createAwardsPb(Constant.ADD_MASTER_REWARD);
                }

                mailDataManager.sendAttachMail(aPlayer, awards, MailConstant.MOLD_ADD_MASTER_REWARD, AwardFrom.ADD_MASTER_REWARD,
                        now, mPlayer.lord.getNick(), mPlayer.lord.getNick());

                syncFriendState(aPlayer, 2);
            } else {
                // 拒绝，直接删除拜师请求
                mPlayer.apprentices.remove(aPlayer.roleId);
            }
        }

        AgreeRejectMasterRs.Builder builder = AgreeRejectMasterRs.newBuilder();
        for (DbMasterApprentice apprentice : mPlayer.apprentices.values()) {
            long lordId = apprentice.getLordId();
            Player aPlayer = playerDataManager.getPlayer(lordId);
            if (null == aPlayer) {
                continue;
            }
            CommonPb.Man apprenticeMan = PbHelper.createManPbByLord(aPlayer);
            CommonPb.MasterApprentice apprenticePb = PbHelper.createMasterApprenticePb(apprenticeMan, apprentice, aPlayer);
            builder.addApprentice(apprenticePb);
        }
        return builder.build();
    }



/*    *//**
     * 拜师
     *
     * @param roleId
     * @param masterId 师傅的角色id
     * @return
     * @throws MwException
     *//*
    public AddMasterRs addMasterRs(long roleId, long masterId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检测自己成为徒弟条件是否满足
        if (player.master != null) {
            throw new MwException(GameError.MASTER_HAD.getCode(), "师傅已存在");
        }
        // 不能拜自己的徒弟为师傅
        if (player.apprentices.containsKey(masterId)) {
            throw new MwException(GameError.APPRENTICE_NOT_BECOME_MASTER.getCode(), "不能拜自己的徒弟为师傅");
        }
        // 是否为自己好友
        if (!player.friends.containsKey(masterId)) {
            throw new MwException(GameError.FRIEND_NOT_EXIST.getCode(), "非好友状态,不能拜师");
        }
        if (null == player.friends.get(masterId) || player.friends.get(masterId).getState() != DbFriend.STATE_FRIEND) {
            throw new MwException(GameError.FRIEND_NOT_EXIST.getCode(), "非好友状态,不能拜师");
        }
        // 成为徒弟等级条件
        if (player.lord.getLevel() > Constant.MAX_APPRENTICE_LV) {
            throw new MwException(GameError.APPRENTICE_LV_ERR.getCode(), "当前等级过高,无法拜师");
        }

        // 检测对方是否满足成为师傅的条件
        Player mPlayer = playerDataManager.checkPlayerIsExist(masterId);

        if (!mPlayer.friends.containsKey(roleId)) {
            throw new MwException(GameError.FRIEND_NOT_EXIST.getCode(), "非好友状态,不能拜师");
        }
        if (mPlayer.lord.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NO_CAMP_NOT_BECOME_MASTER.getCode(), "非同一阵营不能拜师, roleId:", roleId,
                    ", friendId:", mPlayer.lord);
        }
        if (null == mPlayer.friends.get(roleId) || mPlayer.friends.get(roleId).getState() != DbFriend.STATE_FRIEND) {
            throw new MwException(GameError.FRIEND_NOT_EXIST.getCode(), "非好友状态,不能拜师");
        }

        if (mPlayer.lord.getLevel() < Constant.MIN_MASTER_LV) {
            throw new MwException(GameError.MASTER_LV_ERR.getCode(), "当前好友等级不足,无法拜师 ");
        }
        if (mPlayer.apprentices.size() >= Constant.MAX_APPRENTICE_COUNT) {
            throw new MwException(GameError.APPRENTICE_MAX_COUNT.getCode(), "当前好友徒弟人数已满，无法拜师");
        }

        int now = TimeHelper.getCurrentSecond();
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_MASTER);
        // 拜师
        player.master = new DbMasterApprentice(mPlayer.roleId, now, DbMasterApprentice.RELATION_MASTER);
        player.friends.get(mPlayer.roleId).setState(DbFriend.STATE_MASTER); // 修改状态

        activityDataManager.updDay7ActSchedule(mPlayer, ActivityConst.ACT_TASK_APPRENTICES);
        // 收徒儿啦
        mPlayer.apprentices.put(player.roleId,
                new DbMasterApprentice(player.roleId, now, DbMasterApprentice.RELATION_APPRENTICE));
        mPlayer.friends.get(player.roleId).setState(DbFriend.STATE_APPRENTICE);
        // 给师傅发收徒邮件
        mailDataManager.sendNormalMail(mPlayer, MailConstant.MOLD_ADD_APPRENTICE, now, player.lord.getNick(),
                player.lord.getNick());

        if (!CheckNull.isEmpty(ActParamConstant.ACT_TUTOR_RANK_CONF)) {
            // 徒弟的等级大于需要的等级, 就可以获取积分
            int integral = ActParamConstant.ACT_TUTOR_RANK_CONF.stream().filter(conf -> player.lord.getLevel() >= conf.get(0)).mapToInt(conf -> conf.get(1)).sum();
            if (integral > 0) {
                // 给导师加积分
                activityDataManager.updRankActivity(mPlayer, ActivityConst.ACT_TUTOR_RANK, integral);
            }
        }

        // 给徒弟发奖励邮件
        List<Award> awards = PbHelper.createAwardsPb(Constant.ADD_MASTER_REWARD);
        mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ADD_MASTER_REWARD, AwardFrom.ADD_MASTER_REWARD,
                now, mPlayer.lord.getNick(), mPlayer.lord.getNick());

        AddMasterRs.Builder builder = AddMasterRs.newBuilder();
        return builder.build();
    }*/


    /**
     * 积分兑换
     *
     * @param roleId
     * @param productId 积分商店商品id
     * @return
     * @throws MwException
     */
    public CreditExchangeRs creditExchange(long roleId, int productId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检测积分商品是否存在
        StaticCreditShop product = StaticFriendDataMgr.getCreditProduct(productId);
        if (null == product) {
            throw new MwException(GameError.CREDIT_PRODUCT_NOT_EXIST.getCode(), "积分商品不存在");
        }
        // 检测只能单次兑换的物品是否兑换过
        if (1 == product.getIsRepeat() && player.boughtIds.contains(productId)) {
            throw new MwException(GameError.CREDIT_PRODUCT_IS_BOUGHT.getCode(), "该商品已经购买过");
        }

        // 检测玩家等级是否满足
        int pLv = player.lord.getLevel();
        if (pLv < product.getNeedlv()) {
            throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "玩家等级不够");
        }

        int needPrice = product.getPrice();
        if (needPrice < 0) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "积分兑换时,读取配置错误 , roleId:" + roleId + " productId:" + productId);
        }
        // // 检测积分是否满足
        // rewardDataManager.checkMoneyIsEnough(player.lord, AwardType.Money.CREDIT, needPrice, "积分兑换商品");
        // // 扣积分
        // rewardDataManager.subMoney(player, AwardType.Money.CREDIT, needPrice, AwardFrom.CREDIT_EXCHANGE, "积分兑换商品");
        List<List<Integer>> reaward = new ArrayList<>();
        reaward.add(product.getAward());
        rewardDataManager.checkBag(player, reaward); // 背包检查
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.CREDIT, needPrice,
                AwardFrom.CREDIT_EXCHANGE);
        // 获得物品
        Award award = rewardDataManager.addAwardSignle(player, product.getAward(), 1, AwardFrom.CREDIT_EXCHANGE);
        if (null == award) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "积分兑换时,读取配置错误 , roleId:" + roleId + " productId:" + productId);
        }
        // 记录已兑换商品
        if (1 == product.getIsRepeat()) { // 非重复购买的商品才需要记录
            player.boughtIds.add(productId);
        }

        CreditExchangeRs.Builder builder = CreditExchangeRs.newBuilder();
        builder.setCredit(player.lord.getCredit());
        builder.setAward(award);
        builder.addAllBoughtIds(player.boughtIds);
        return builder.build();
    }

    /**
     * 领取师徒奖励
     *
     * @param roleId
     * @param rewardId 师徒奖励的id
     * @return
     * @throws MwException
     */
    public MasterRewardRs masterReward(long roleId, int rewardId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticMasterReaward reaward = StaticFriendDataMgr.getReaward(rewardId);
        // 检测奖励是否存在
        if (null == reaward) {
            throw new MwException(GameError.MASTER_REWARD_NOT_EXIST.getCode(), "师徒奖励不存在");
        }

        // 检测条件条件是否满足
        List<Integer> condition = reaward.getCondition();

        if (CheckNull.isEmpty(condition) || condition.size() != 2) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "领取师徒奖励时,读取配置错误 , roleId:" + roleId + " rewardId:" + rewardId);
        }

        int needLv = condition.get(0); // 徒弟需要的等级
        int maxCount = condition.get(1);// 最多可领取的次数

        int curCount = 0; // 当前有多少人满足等级条件
        for (Entry<Long, DbMasterApprentice> en : player.apprentices.entrySet()) {
            Long pId = en.getKey();
            Player aPlayer = playerDataManager.getPlayer(pId);
            if (null == aPlayer) {
                continue;
            }
            DbMasterApprentice apprentice = en.getValue();
            if (apprentice.getStaus() == DbMasterApprentice.STATE_WAIT_APPROVAL) {
                continue;
            }
            if (aPlayer.lord.getLevel() >= needLv) {
                curCount++;
            }
        }

        Integer awardCount = player.awardedIds.get(rewardId);// 当前已经领取的个数
        if (null == awardCount) {
            awardCount = 0;
        }
        if (awardCount >= maxCount) {
            throw new MwException(GameError.MASTER_REWARD_IS_GET.getCode(), "领取条件奖励已经超过上限 roleId:", roleId, ", 当前已领取:",
                    awardCount, ", 领取上限个数:", maxCount);
        }
        if (awardCount >= curCount) {
            throw new MwException(GameError.GET_MASTER_REWARD_ERR.getCode(), "领取师徒奖励条件不满足");
        }

        // 发放奖品
        ChangeInfo change = ChangeInfo.newIns();
        List<Award> addAward = rewardDataManager.addAwardDelaySync(player, reaward.getAward(), change,
                AwardFrom.GET_MASTER_REWARD);

        // 记录已领取
        player.awardedIds.put(rewardId, awardCount + 1);

        MasterRewardRs.Builder builder = MasterRewardRs.newBuilder();
        builder.addAllAwardedIds(PbHelper.createTwoIntListByMap(player.awardedIds));
        builder.addAllAward(addAward);
        return builder.build();
    }

    /**
     * 解除师徒关系
     * @param roleId    发起者id
     * @param targetId  需要解除者id
     * @return 徒弟列表
     */
    public DelMasterApprenticeRs delMasterApprentice(long roleId, long targetId) throws MwException {

        Player mPlayer = playerDataManager.checkPlayerIsExist(roleId);
        Player tPlayer = playerDataManager.checkPlayerIsExist(targetId);

        DbMasterApprentice master = mPlayer.master;
        if (Objects.nonNull(master) && master.getLordId() == targetId && master.getStaus() == DbMasterApprentice.STATE_AGREE) {
            if (!checkCanDel(tPlayer, Constant.DEL_MASTER_NEED_OFFLINE_DAY)) {
                throw new MwException(GameError.DEL_MASTER_APPRENTICE_TIME_NOT_YET.getCode(), "解除师徒关系, 离线时间未到, target:", targetId);
            }
            // 解除发起者师傅
            mPlayer.master = null;
            // 解除解除者徒弟
            tPlayer.apprentices.remove(roleId);
            // 记录解除的时间
            mPlayer.setMixtureData(PlayerConstant.DEL_MASTER_TIME, TimeHelper.getCurrentSecond());
        } else if (mPlayer.apprentices.containsKey(targetId)) {
            if (!checkCanDel(tPlayer, Constant.DEL_APPRENTICE_NEED_OFFLINE_DAY)) {
                throw new MwException(GameError.DEL_MASTER_APPRENTICE_TIME_NOT_YET.getCode(), "解除师徒关系, 徒弟离线时间未到, target:", targetId);
            }
            // 解除发起者徒弟
            DbMasterApprentice apprentice = mPlayer.apprentices.get(targetId);
            if (DbMasterApprentice.STATE_AGREE == apprentice.getStaus()) {
                mPlayer.apprentices.remove(targetId);
                // 记录解除的时间
                tPlayer.setMixtureData(PlayerConstant.DEL_MASTER_TIME, TimeHelper.getCurrentSecond());
            }
            // 解除解除者师傅
            tPlayer.master = null;
        } else {
            // 没有师徒关系
            throw new MwException(GameError.DEL_MASTER_APPRENTICE_NOT_FOUND.getCode(), "解除师徒关系, 未找到对应解除者, target: ", targetId);
        }

        // 给解除者发送邮件通知
        mailDataManager.sendNormalMail(tPlayer, MailConstant.MOLD_DEL_MASTER_APPRENTICE, TimeHelper.getCurrentSecond(), mPlayer.lord.getNick());

        DelMasterApprenticeRs.Builder builder = DelMasterApprenticeRs.newBuilder();
        for (DbMasterApprentice apprentice : mPlayer.apprentices.values()) {
            long lordId = apprentice.getLordId();
            Player aPlayer = playerDataManager.getPlayer(lordId);
            if (null == aPlayer) {
                continue;
            }
            CommonPb.Man apprenticeMan = PbHelper.createManPbByLord(aPlayer);
            CommonPb.MasterApprentice apprenticePb = PbHelper.createMasterApprenticePb(apprenticeMan, apprentice, aPlayer);
            builder.addApprentice(apprenticePb);
        }
        return builder.build();

    }

    /**
     * 检测玩家是否可以解除师徒关系
     * @param player        检测的玩家
     * @param offlineDay    离线的天数
     * @return true         可以解除
     */
    private boolean checkCanDel(Player player, int offlineDay) {
        // 离线时间
        int offTime = player.lord.getOffTime();
        // 现在的second
        int nowSecond = TimeHelper.getCurrentSecond();
        // 最近多少天不登录
        int duTime = TimeHelper.DAY_S * offlineDay;
        return offTime > 0 && nowSecond - offTime >= duTime;
    }

    /**
     * 推送消息
     *
     * @param target
     * @param state
     */
    private void syncFriendState(Player target, int state) {
        if (target != null && target.ctx != null && target.isLogin) {
            SyncFriendStateRs.Builder builder = SyncFriendStateRs.newBuilder();
            builder.setState(state);
            Base.Builder msg = PbHelper.createSynBase(SyncFriendStateRs.EXT_FIELD_NUMBER, SyncFriendStateRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(target.ctx, msg.build(), target.roleId));
        }
    }

    /**
     * 获取推荐玩家列表
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetRecommendLordRs getRecommendLord(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int area = player.lord.getArea();
        int now = TimeHelper.getCurrentSecond();

        // 优先推荐本区域玩家, 离线时间不超过30分钟
        List<Player> tmpPlayers = playerDataManager.getPlayerByArea(area).values().stream()
                .filter(p -> p.roleId != roleId && !player.friends.containsKey(p.roleId) && (now - player.lord.getOffTime() < Constant.RECOMMEND_PLAYER_OFF_TIME)).collect(Collectors.toList());
        // 没有本区域没找到去其他区域
        if (tmpPlayers.size() < Constant.RECOMMEND_PLAYER_CNT) {
            tmpPlayers = playerDataManager.getPlayers().values().stream()
                    .filter(p -> p.roleId != roleId && !player.friends.containsKey(p.roleId))
                    .collect(Collectors.toList());
        }
        GetRecommendLordRs.Builder builder = GetRecommendLordRs.newBuilder();
        List<Integer> pIndexs = RandomUtil.getRandomNums(Constant.RECOMMEND_PLAYER_CNT, tmpPlayers.size());
        for (Integer index : pIndexs) {
            builder.addMan(PbHelper.createManPbByLord(tmpPlayers.get(index)));
        }

        return builder.build();
    }

    /**
     * 获取黑名单
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetBlacklistRs getBlacklist(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GetBlacklistRs.Builder builder = GetBlacklistRs.newBuilder();
        List<Long> blacklist = player.getBlacklist();
        if (!CheckNull.isEmpty(blacklist)) {
            for (Long rId : blacklist) {
                Player blackP = playerDataManager.getPlayer(rId);
                if (blackP != null) {
                    builder.addMan(PbHelper.createManPbByLord(blackP));
                }
            }
        }
        return builder.build();
    }

    /**
     * 添加黑名单
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public AddBlackListRs addBlackList(long roleId, AddBlackListRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        final long blacklistId = req.getRoleId();
        if (blacklistId == roleId) {
            throw new MwException(GameError.BLACKLIST_NOT_ADD_SELF.getCode(), "黑名单不能添加自己 roleId:", roleId,
                    ", blacklistId:", blacklistId);
        }
        Player blacklistPlayer = playerDataManager.checkPlayerIsExist(blacklistId);
        List<Long> blacklist = player.getBlacklist();
        if (blacklist == null) {
            blacklist = new ArrayList<>();
            player.setBlacklist(blacklist);
        }
        if (blacklist.contains(blacklistId)) {
            throw new MwException(GameError.BLACKLIST_IS_EXIST.getCode(), " 黑名单列表中已经存在该玩家 roleId:", roleId,
                    ", blacklistId:", blacklistId);
        }
        blacklist.add(blacklistId);
        AddBlackListRs.Builder builder = AddBlackListRs.newBuilder();
        builder.setMan(PbHelper.createManPbByLord(blacklistPlayer));
        return builder.build();
    }

    /**
     * 删除黑名单
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public DelBlackListRs delBlackList(long roleId, DelBlackListRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        List<Long> blacklist = player.getBlacklist();
        if (!CheckNull.isEmpty(blacklist)) {
            List<Long> rmBlacklist = req.getRoleIdList();
            for (long rmRid : rmBlacklist) {
                blacklist.remove(Long.valueOf(rmRid));
            }
        }
        DelBlackListRs.Builder builder = DelBlackListRs.newBuilder();
        return builder.build();
    }

}
