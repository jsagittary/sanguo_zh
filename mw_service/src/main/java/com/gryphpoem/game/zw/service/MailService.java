package com.gryphpoem.game.zw.service;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.*;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.GamePb2.*;
import com.gryphpoem.game.zw.pb.GamePb3.SendCampMailRq;
import com.gryphpoem.game.zw.pb.GamePb3.SendCampMailRs;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.DbMailReport;
import com.gryphpoem.game.zw.resource.pojo.Mail;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.server.SaveMailReportServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @ClassName MailService.java
 * @Description 邮件相关
 * @date 创建时间：2017年4月4日 下午5:00:13
 */
@Service
public class MailService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private ActivityDataManager activityDataManager;

    public void deleteCampMail(long lordId, int moldId) {
        GamePb3.SyncChatMailChangeRs.Builder syncBuilder = GamePb3.SyncChatMailChangeRs.newBuilder();
        syncBuilder.addMailChange(GamePb3.SyncChatMailChangeRs.MailChange.newBuilder().setLordId(lordId).setAction(1).setMoldId(moldId).build());
        BasePb.Base.Builder builder = PbHelper.createSynBase(GamePb3.SyncChatMailChangeRs.EXT_FIELD_NUMBER, GamePb3.SyncChatMailChangeRs.ext, syncBuilder.build());
        // 删除本服阵营邮件
        playerDataManager.getAllPlayer().values().stream().filter(p -> !p.mails.isEmpty())
                // 删除该模板, 该发件人的邮件
                .forEach(p -> {
                    Set<Integer> removeSet = p.mails.values().stream().filter(mail -> mail.getMoldId() == moldId && mail.getOriginator() == lordId).map(Mail::getKeyId).collect(Collectors.toSet());
                    if (CheckNull.nonEmpty(removeSet)) {
                        removeSet.forEach(p.mails::remove);
                        if (p.ctx == null || !p.isLogin || p.isRobot)
                            return;
                        // 阵营邮件删除, 同步给客户端
                        MsgDataManager.getIns().add(new Msg(p.ctx, builder.build(), p.roleId));
                    }
                });
    }

    /**
     * 获取邮件列表
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetMailListRs getMailList(long roleId, GetMailListRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GetMailListRs.Builder builder = GetMailListRs.newBuilder();
        Mail mail;
        int now = TimeHelper.getCurrentSecond();
        int delTime = now - MailConstant.MAIL_MAX_TIME;
        if (req.hasType()) {
            int type = req.getType();
            Iterator<Mail> it = player.mails.values().iterator();
            while (it.hasNext()) {
                mail = it.next();

                if (mail.getTime() < delTime) {// 邮件超时删除
                    it.remove();
                    continue;
                }

                if (type == mail.getType()) {
                    builder.addMailShow(PbHelper.createMailShowPb(mail));
                }
            }
        } else {
            Iterator<Mail> it = player.mails.values().iterator();
            while (it.hasNext()) {
                mail = it.next();

                if (mail.getTime() < delTime) {// 邮件超时删除
                    it.remove();
                    LogUtil.common(String.format("玩家 %d 删除过期邮件 %d mailTime %s", player.getLordId(), mail.getKeyId(), DateHelper.simpleTimeFormat(TimeHelper.secondToDate(mail.getTime()))));
                    continue;
                }

                builder.addMailShow(PbHelper.createMailShowPb(mail));
            }
        }

        return builder.build();
    }

    /**
     * 根据邮件id获取邮件内容
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetMailByIdRs getMailById(long roleId, GetMailByIdRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GetMailByIdRs.Builder builder = GetMailByIdRs.newBuilder();
        int type = 0;
        if (req.hasType()) {
            type = req.getType();
        }

        Mail mail = null;
        int keyId = req.getKeyId();
        mail = player.mails.get(keyId);

        if (mail == null) {
            throw new MwException(GameError.MAIL_NOT_EXIST.getCode(), "获取邮件列表为空, roleId:", roleId);
        }

        // 更新邮件状态
        int state = mail.getState();
        if (state == MailConstant.STATE_UNREAD) {
            mail.setState(MailConstant.STATE_READ);
        }

        if (state == MailConstant.STATE_UNREAD_ITEM) {
            mail.setState(MailConstant.STATE_READ_ITEM);
        }

        int mailtype = mail.getType();
        if (mailtype == MailConstant.TYPE_ROLE) {
            String sendNane = mail.getSendName();
            if (sendNane != null && !sendNane.equals("")) {
                Player send = playerDataManager.getPlayer(sendNane);
                if (send != null && send.lord != null) {
                    mail.setLv(send.lord.getLevel());
                    mail.setVipLv(send.lord.getVip());
                }
            }
        }

        // 返回邮件详细信息
        CommonPb.Mail pbMail = PbHelper.createMailPb(mail, player);
        builder.setMail(pbMail);
        return builder.build();
    }

    /**
     * 领取邮件奖励
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public RewardMailRs rewardMail(long roleId, RewardMailRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        RewardMailRs.Builder builder = RewardMailRs.newBuilder();
        int keyId = req.getKeyId();
        Mail mail = player.mails.get(keyId);
        if (mail == null) {
            throw new MwException(GameError.MAIL_NOT_EXIST.getCode(), "获取邮件为空, roleId:", roleId, ", keyId:", keyId);
        }

        if (mail.getState() != MailConstant.STATE_UNREAD_ITEM && mail.getState() != MailConstant.STATE_READ_ITEM) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "领取邮件奖励，状态不正确, roleId:", roleId, ", state:",
                    mail.getState());
        }

        List<CommonPb.Award> awards = mail.getRewardList();
        // 检查奖励中装备的数量
        int equipCnt = getEquipCnt(awards);
        if (equipCnt != 0) {
            rewardDataManager.checkBagCnt(player, equipCnt); // 判断背包格子是否足够
        }
        int moldId = mail.getMoldId();// 模板id
        String tParam = CheckNull.isEmpty(mail.gettParam()) ? "0" : mail.gettParam().get(0);// 邮件的标题参数,如果没有就为0
        // 发送邮件奖励
        if (!CheckNull.isEmpty(awards)) {
            //校验当前奖励所有宝具是否可以塞进背包
            DataResource.getBean(TreasureWareService.class).checkMailAward(awards, player);
            for (CommonPb.Award e : awards) {
                int type = e.getType();
                int id = e.getId();
                int count = e.getCount();
                if(type == AwardType.HERO && id == HeroConstant.GQS_HERO_ID){
                    chatDataManager.sendSysChat(ChatConst.CHAT_FIRST_FLUSH, player.lord.getCamp(), 0,
                            player.lord.getCamp(), player.lord.getNick(), id);
                }
                // 这里是对转盘的的免费道具做特殊处理
                if (type == AwardType.PROP && id == 26006) {
                    Activity activity = activityDataManager.getActivityInfo(player, 209);
                    ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(209);
                    if (activity == null || activityBase == null) {
                        type = AwardType.RESOURCE;
                        id = 1;
                        count = 10000 * count;
                    }
                }
                if (type == Constant.CROSS_WAR_FIRE_WINNER_PORTRAIT.get(0) &&
                        id == Constant.CROSS_WAR_FIRE_WINNER_PORTRAIT.get(1)) {
                    builder.addAward(e);
                    continue;
                }

                LogUtil.debug(roleId + ",领取附件,type=" + type + ",id=" + id + ",count=" + count);
                int itemKeyId = rewardDataManager.addMailAward(player, type, id, count, e.hasKeyId() ? e.getKeyId() : 0, AwardFrom.MAIL_ATTACH, moldId,
                        tParam);

                builder.addAward(PbHelper.createAwardPbWithParam(type, id, count, itemKeyId, e.getParamList()));
            }
        }

        // 更新邮件状态
        mail.setState(MailConstant.STATE_NO_ITEM);
        return builder.build();
    }

    /**
     * 奖励中是否有装备
     *
     * @param awards
     * @return true 有状态吧
     */
    private boolean hasEquip(List<CommonPb.Award> awards) {
        if (!CheckNull.isEmpty(awards)) {
            for (CommonPb.Award e : awards) {
                int type = e.getType();
                if (type == AwardType.EQUIP) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取奖励中装备的数量
     *
     * @param awards
     * @return
     */
    private int getEquipCnt(List<CommonPb.Award> awards) {
        int cnt = 0;
        if (!CheckNull.isEmpty(awards)) {
            for (CommonPb.Award e : awards) {
                int type = e.getType();
                if (type == AwardType.EQUIP) {
                    cnt += e.getCount();
                }
            }
        }
        return cnt;
    }

    /**
     * 设置邮件全部已读
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public ReadAllMailRs readAllMail(long roleId, List<Integer> keyIds) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        List<CommonPb.Award> allAward = new ArrayList<>(); // 所有附件的id
        List<Integer> notAwardId = new ArrayList<>(); // 不能领取邮件的id
        for (Integer mailKeyId : keyIds) {
            Mail mail = player.mails.get(mailKeyId);
            if (mail == null) continue;
            // 设置邮件状态
            if (mail.getState() == MailConstant.STATE_UNREAD) {
                mail.setState(MailConstant.STATE_READ);
            } else if (mail.getState() == MailConstant.STATE_UNREAD_ITEM
                    || mail.getState() == MailConstant.STATE_READ_ITEM) {// 带附件的邮件
/*                mail.setState(MailConstant.STATE_READ_ITEM);
                if (mail.getLock() == MailConstant.STARE_LOCK) {
                    continue;
                }
                // 领取附件
                List<CommonPb.Award> awards = mail.getRewardList();
                try {
                    // 检查奖励中装备的数量
                    int equipCnt = getEquipCnt(awards);
                    if (equipCnt != 0) {
                        rewardDataManager.checkBagCnt(player, equipCnt); // 判断背包格子是否足够
                    }
                } catch (Exception e) {
                    notAwardId.add(mail.getKeyId()); // 背包满了不让领取
                    continue;
                }
                // 更新邮件状态
                mail.setState(MailConstant.STATE_NO_ITEM);
                int moldId = mail.getMoldId();// 模板id
                String tParam = CheckNull.isEmpty(mail.gettParam()) ? "0" : mail.gettParam().get(0);// 邮件的标题参数,如果没有就为0
                // 发送邮件奖励
                if (!CheckNull.isEmpty(awards)) {
                    for (CommonPb.Award e : awards) {
                        int type = e.getType();
                        int id = e.getId();
                        int count = e.getCount();
                        if(type == AwardType.HERO && id == HeroConstant.GQS_HERO_ID){
                            chatDataManager.sendSysChat(ChatConst.CHAT_FIRST_FLUSH, player.lord.getCamp(), 0,
                                    player.lord.getCamp(), player.lord.getNick(), id);
                        }
                        LogUtil.debug(roleId + ",一键领取附件,type=" + type + ",id=" + id + ",count=" + count);
                        int itemKeyId = rewardDataManager.addAward(player, type, id, count, AwardFrom.MAIL_ATTACH,
                                moldId, tParam);
                        allAward.add(PbHelper.createAwardPb(type, id, count, itemKeyId));
                    }
                }*/
            }
        }
        ReadAllMailRs.Builder builder = ReadAllMailRs.newBuilder();
        if (!CheckNull.isEmpty(allAward)) builder.addAllAward(allAward);// 领取的奖励
        if (!CheckNull.isEmpty(notAwardId)) builder.addAllKeyId(notAwardId);// 不能领取邮件的keyid
        return builder.build();
    }

    /**
     * 删除邮件
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public DelMailRs delMail(long roleId, DelMailRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        List<Integer> keyIdList = req.getKeyIdList();
        if (!CheckNull.isEmpty(keyIdList)) {
            Mail mail;
            for (Integer keyId : keyIdList) {
                mail = player.mails.get(keyId);
                if (null == mail) {
                    LogUtil.error("删除邮件不存在, roleId:", roleId, ", keyId:", keyId);
                } else {
                    // 如果对于有附件的邮件需要特殊处理，在删除前处理（暂时在日志中打印附件信息）
                    if (!CheckNull.isEmpty(mail.getRewardList()) && (mail.getState() == MailConstant.STATE_UNREAD_ITEM
                            || mail.getState() == MailConstant.STATE_READ_ITEM)) {
                        LogUtil.common("玩家删除带附件的邮件, roleId:", roleId, ", mailKey:", mail.getKeyId(), ", rewardList:",
                                mail.getRewardList());
                    }
                    if (mail.getLock() == MailConstant.STARE_LOCK) {
                        continue;
                    } else {
                        player.mails.remove(keyId);
                        if (mail.getReportStatus() == MailConstant.EXISTENCE_REPORT) {
                            SaveMailReportServer.getIns().removeData(new DbMailReport(roleId, mail.getKeyId(), null));
                        }
                    }
                }
            }
        }

        DelMailRs.Builder builder = DelMailRs.newBuilder();
        Iterator<Mail> it = player.mails.values().iterator();
        while (it.hasNext()) {
            builder.addMailShow(PbHelper.createMailShowPb(it.next()));
        }

        return builder.build();
    }

    /**
     * 发送邮件
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public SendMailRs sendMail(long roleId, SendMailRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        CommonPb.Mail sendMail = req.getMail();
        String title = sendMail.getTitle();
        String content = sendMail.getContent();
        // 长度判断
        if (title.length() > MailConstant.MAIL_TITLE_LEN) {
            title = title.substring(0, MailConstant.MAIL_TITLE_LEN);
        }
        if (content.length() > MailConstant.MAIL_CONTENT_LEN) {
            content = content.substring(0, MailConstant.MAIL_CONTENT_LEN);
        }
        String sendName = player.lord.getNick();
        Mail mail = mailDataManager.addPlayerSendMail(player, MailConstant.STATE_UNREAD, title, content, sendName);

        SendMailRs.Builder builder = SendMailRs.newBuilder();
        builder.setMail(PbHelper.createMailPb(mail, player));
        return builder.build();
    }

    /**
     * @Title: sendCampMail @Description:  @param @param roleId @param @param
     * req @param @return @param @throws MwException 参数 @return SendCampMailRs 返回类型 @throws
     */
    public SendCampMailRs sendCampMail(long roleId, SendCampMailRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int now = TimeHelper.getCurrentSecond();

        String content = req.getContent();
        // 邮件内容非空判断
        if (content == null || "".equals(content)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "邮件内容为空, content:", content);
        }

        // 邮件内容长度判断
        if (content.length() > MailConstant.CAMP_MAIL_CONTENT_MAX) {
            throw new MwException(GameError.CAMP_MAIL_CONTENT_TO_LONG.getCode(), "阵营邮件内容过长, length:", content.length());
        }

        // 禁言只对普通聊天生效
        int silenceTime = player.lord.getSilence();
        if (silenceTime > 0) {
            if (silenceTime == 1 ){
                throw new MwException(GameError.CHAT_SILENCE.getCode(), "玩家已被禁言，不能发送聊天, roleId:", roleId,
                        ", silence:", silenceTime);
            }
            if (silenceTime > now) {
                throw new MwException(GameError.CHAT_SILENCE.getCode(), "玩家已被禁言，不能发送聊天, roleId:", roleId,
                        ", silence:", silenceTime);
            }
        }

        // 判断玩家VIP等级
        if (player.lord.getVip() < MailConstant.CAMP_MAIL_VIP) {
            throw new MwException(GameError.VIP_NOT_ENOUGH.getCode(), "VIP等级不足, VIP:", player.lord.getVip());
        }

        // 判断角色等级
        if (player.lord.getLevel() < MailConstant.SEND_CAMP_MAIL_LEVEL) {
            throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "角色等级必须到达55级, lv:", player.lord.getLevel());
        }
        // 发送间隔
        if (now - player.pCampMailTime < MailConstant.CAMP_MAIL_CD) {
            throw new MwException(GameError.CAMP_MAIL_CD.getCode(), "玩家发送阵营邮件过于频繁, roleId:", roleId, ", pCampMailTime:",
                    player.pCampMailTime);
        }

        // 判断玩家官职
        boolean flag = false;
        for (int job : MailConstant.CAMP_MAIL_FREE_JOB) {
            if (job == player.lord.getJob()) {
                flag = true;// 免费
                break;
            }
        }
        if (!flag) {
            // 金币扣除
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                    getSendCampMailGold(player), AwardFrom.SEND_CAMP_MAIL);
            // 记录发送阵营邮件的次数
            player.setMixtureData(PlayerConstant.DAILY_SEND_MAIL_CNT, player.getMixtureDataById(PlayerConstant.DAILY_SEND_MAIL_CNT) + 1);
        }

        LogLordHelper.commonChat("campMail",AwardFrom.SEND_CAMP_MAIL, player.account, player.lord, content, serverSetting.getServerID());
        // 创建聊天对象
        /*int channel = ChatConst.CHANNEL_PRIVATE;//私聊
        RoleChat chat = (RoleChat) createRoleChat(player, content);
        chat.setChannel(channel);
        chat.setIsCampChat(ChatConst.IS_CAMP_MAIL_CHAT);*/

        // 获取同阵营所有玩家
        ConcurrentHashMap<Long, Player> map = playerDataManager.getPlayerByCamp(player.lord.getCamp());
        for (Player recvPlayer : map.values()) {
            // 发送邮件
            mailDataManager.sendCampMail(player, recvPlayer, MailConstant.MOLD_CAMP_MAIL, now, player.lord.getLevel(),
                    player.lord.getNick(), player.lord.getPortrait(), content, content, player.lord.getLordId(), player.getDressUp().getCurPortraitFrame());

            // 判断玩家等级和离线时长 推送 消息
            if (!recvPlayer.isLogin
                    && recvPlayer.lord.getLevel() >= MailConstant.CAMP_MAIL_MIN_GRADE_AND_OFFLINE_LONG.get(0)
                    && (now - recvPlayer.lord.getOffTime()) <= MailConstant.CAMP_MAIL_MIN_GRADE_AND_OFFLINE_LONG.get(1)
                    * 24 * 3600) {
                PushMessageUtil.pushMessage(recvPlayer.account, PushConstant.CAMP_MAIL_NOTICE);
            }

            // 发送私聊
            /*Player targetP = playerDataManager.checkPlayerIsExist(recvPlayer.lord.getLordId());
            sendPrivateChat(chat, player, targetP);*/
        }
        player.pCampMailTime = now;

        SendCampMailRs.Builder builder = SendCampMailRs.newBuilder();
        // 当天发送的阵营邮件次数
        builder.setSendCnt(player.getMixtureDataById(PlayerConstant.DAILY_SEND_MAIL_CNT));
        return builder.build();
    }

    /**
     * 获取发送阵营邮件所需要的价格
     *
     * @param player 玩家对象
     * @return 金币价格
     */
    private int getSendCampMailGold(Player player) {
        // 默认是0次
        int cnt = player.getMixtureDataById(PlayerConstant.DAILY_SEND_MAIL_CNT);
        // 最大是7次
        int maxGoldIndex = MailConstant.CAMP_MAIL_GOLD.size() - 1;
        // 每次的发送金币价格
        int index = cnt >= maxGoldIndex ? maxGoldIndex : cnt;
        return MailConstant.CAMP_MAIL_GOLD.get(index);
    }

    /**
     * 创建角色聊天对象
     *
     * @param player
     * @param msg
     * @return
     */
    /*private Chat createRoleChat(Player player, String msg) {
        RoleChat chat = new RoleChat();
        chat.setPlayer(player);
        chat.setTime(TimeHelper.getCurrentSecond());
        chat.setMsg(msg);
        return chat;
    }*/

    /**
     * 发送私聊消息
     *
     * @param chat
     * @param roleId
     * @return 返回是否发送成功
     */
    /*private boolean sendPrivateChat(Chat chat, Player my, Player targetP) {
        CommonPb.Chat b = chatDataManager.createPrivateChat(chat, my.roleId, targetP.roleId);
        if (targetP != null && targetP.isLogin) {// 在线消息
            SyncChatRs.Builder chatBuilder = SyncChatRs.newBuilder();
            chatBuilder.setChat(b);
            Base.Builder builder = PbHelper.createSynBase(SyncChatRs.EXT_FIELD_NUMBER, SyncChatRs.ext,
                    chatBuilder.build());
            MsgDataManager.getIns().add(new Msg(targetP.ctx, builder.build(), targetP.roleId));
            return true;
        } else {
            return false;
        }
    }*/

    /**
     * 获取玩家分享的邮件信息
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetShareMailRs getShareMail(long roleId, GetShareMailRq req) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        int mailKeyId = req.getMailKeyId();
        Long targetId = req.getTargetId();
        Player target = playerDataManager.getPlayer(targetId);
        if (null == target) {
            throw new MwException(GameError.NO_LORD.getCode(), "获取分享邮件信息，未找到玩家, roleId:", roleId, ", targetId:",
                    targetId);
        }
        Mail mail = chatDataManager.getShareMail(target, mailKeyId);
        //若邮件过期，则显示邮件不存在
        Mail realMail = target.mails.get(mailKeyId);
        if (null == mail || realMail == null) {
            throw new MwException(GameError.MAIL_NOT_EXIST.getCode(), "获取分享邮件信息，未找到邮件, roleId:", roleId, ", mailKeyId:",
                    mailKeyId);
        }

        GetShareMailRs.Builder builder = GetShareMailRs.newBuilder();
        builder.setMail(PbHelper.createMailPb(realMail, target));
        return builder.build();
    }

    /**
     * 删除过期邮件
     */
    public void delExpiredMail() {
        LogUtil.debug("mailService delExpiredMail ==start== ");
        // 在此时间之前的发送邮件都删除
        int mailExpiredTime = TimeHelper.getCurrentSecond() - Constant.DEL_MAIL_DAY * TimeHelper.DAY_S;
        Map<Long, Player> players = playerDataManager.getPlayers();
        Iterator<Player> playersIt = players.values().iterator();
        Player player;
        Iterator<Mail> mailIt;
        Mail mail;

        while (playersIt.hasNext()) {
            player = playersIt.next();
            if (CheckNull.isNull(player) || CheckNull.isNull(player.lord)) continue;
            mailIt = player.mails.values().iterator();
            while (mailIt.hasNext()) {
                mail = mailIt.next();
                if (mail.getTime() <= mailExpiredTime) {
                    mailIt.remove();
                    // 添加删除的战报信息
                    DbMailReport dbMailReport = new DbMailReport();
                    dbMailReport.setLordId(player.lord.getLordId());
                    dbMailReport.setKeyId(mail.getKeyId());
                    SaveMailReportServer.getIns().removeData(dbMailReport);
                    //删除邮件，即删除战报
                    continue;
                }

//                reportExpiredTime = now - getExpiredTimeInterval(mail.getMoldId());
//                if (mail.getTime() <= reportExpiredTime && MailConstant.EXPIRED_REPORT != mail.getReportStatus()) {
//                    delMailReportIds = delMailReportIds == null ? new HashMap<>() : delMailReportIds;
//                    delMailReportIds.computeIfAbsent(mail.getMoldId(), list -> new ArrayList<>()).add(mail.getKeyId());
//                }
            }

//            if (!ObjectUtils.isEmpty(delMailIds))
//                delMailIds.clear();
//            if (!ObjectUtils.isEmpty(delMailReportIds))
//                delMailReportIds.clear();
        }
    }

    private int getExpiredTimeInterval(int moldId) {
        List<Integer> rules = Constant.ATTACK_REPORT_EXPIRE_TIME.get(moldId);
        rules = rules == null ? Constant.DEFENCE_REPORT_EXPIRE_TIME.get(moldId) : rules;
        return rules == null ? (Constant.DEL_MAIL_DAY * TimeHelper.DAY_S) : rules.get(0) * TimeHelper.DAY_S;
    }

    /**
     * 自动删除邮件之前帮用户领取有附件的邮件
     */
    public List<Award> getDelRewardMail(Player player, Mail mail) {
        if (mail == null) {
            return null;
        }

        List<Award> awards = mail.getRewardList();

        if (awards != null && awards.size() > 0) {
            // 附件已取
            if (mail.getState() == MailConstant.STATE_NO_ITEM) {
                return null;
            }
            mail.setState(MailConstant.STATE_NO_ITEM);
            List<Award> returnAwards = new ArrayList<>();
            int moldId = mail.getMoldId();// 模板id
            String tParam = CheckNull.isEmpty(mail.gettParam()) ? "0" : mail.gettParam().get(0);// 邮件的标题参数,如果没有就为0
            for (CommonPb.Award e : awards) {
                int type = e.getType();
                int id = e.getId();
                long count = e.getCount();
                int itemKeyId = rewardDataManager.addAward(player, AwardType.ARMY, id, (int) count,
                        AwardFrom.MAIL_ATTACH, moldId, tParam);
                returnAwards.add(PbHelper.createAwardPb(type, id, (int) count, itemKeyId));
                // CommonPb.Award pbAward = getReward(e, type, id, count, player);
                // returnAwards.add(pbAward);
            }
            LogUtil.error(player.lord, mail.getMoldId(), mail.getKeyId());
            return returnAwards;
        }

        return null;
    }

    /**
     * 设置邮件锁定或者解锁
     *
     * @param roleId
     * @param keyIds
     * @return
     * @throws MwException
     */
    public GamePb2.LockMailRs lockMail(Long roleId, List<Integer> keyIds) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        for (Integer mailKeyId : keyIds) {
            Mail mail = player.mails.get(mailKeyId);
            if (mail == null) continue;
            // 设置邮件状态
            mail.setLock(mail.getLock() == MailConstant.STATE_NO_LOCK ? MailConstant.STARE_LOCK
                    : MailConstant.STATE_NO_LOCK);
        }
        LockMailRs.Builder builder = LockMailRs.newBuilder();
        return builder.build();
    }

    /**
     * 领取指定模板邮件
     *
     * @param player
     */
    public void gainAttrMaill(Player player) {
        for (Mail mail : player.mails.values()) {
            if (mail.getState() == MailConstant.STATE_UNREAD_ITEM || mail.getState() == MailConstant.STATE_READ_ITEM) {// 带附件的邮件
                // 更新邮件状态
                mail.setState(MailConstant.STATE_NO_ITEM);
                // 领取附件
                List<CommonPb.Award> awards = mail.getRewardList();
                String tParam = CheckNull.isEmpty(mail.gettParam()) ? "0" : mail.gettParam().get(0);// 邮件的标题参数,如果没有就为0
                // 发送邮件奖励
                if (!CheckNull.isEmpty(awards)) {
                    for (CommonPb.Award e : awards) {
                        int type = e.getType();
                        int id = e.getId();
                        int count = e.getCount();
                        LogUtil.debug(player.roleId + ",gm一键领取附件,type=" + type + ",id=" + id + ",count=" + count);
                        rewardDataManager.addAward(player, type, id, count, AwardFrom.MAIL_ATTACH, mail.getMoldId(),
                                tParam);
                    }
                }
            }
        }
    }

    // <editor-fold desc="GM 命令" defaultstate="collapsed">
    public void gm(Player player,String...params){
        String cmd = params[1];
        if(cmd.equalsIgnoreCase("sendAward")){//mail sendAward [4,1937,1]
            List<Integer> list = JSON.parseArray(params[2],Integer.class);
            List<Award> awards = new ArrayList<>();
            awards.add(PbHelper.createAward(list));
            mailDataManager.sendAttachMail(player,awards,999999,AwardFrom.DO_SOME,TimeHelper.getCurrentSecond());
        }
        if(cmd.equalsIgnoreCase("clear")){
            player.mails.clear();
        }
    }
    //</editor-fold>
}
