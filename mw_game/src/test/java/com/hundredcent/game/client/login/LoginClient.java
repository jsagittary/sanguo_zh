package com.hundredcent.game.client.login;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.pb.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb1.*;
import com.gryphpoem.game.zw.pb.GamePb2.*;
import com.gryphpoem.game.zw.pb.GamePb3.*;
import com.gryphpoem.game.zw.pb.GamePb4.*;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.pb.GamePb6.*;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.hundredcent.game.client.BaseClient;
import com.hundredcent.game.client.ClientLogger;

;

/**
 * @author TanDonghai
 * @Description 简单测试登录协议的客户端模拟类
 * @date 创建时间：2016年10月31日 下午2:36:25
 */
public class LoginClient extends BaseClient {

    private Object monthCard;
    private Object displayActList;
    private Object area;

    public LoginClient(String serverIp, int port) {
        super(serverIp, port);
    }

    /**
     * 开始游戏
     *
     * @return
     */
    private BeginGameRs beginGame() {
        BeginGameRq.Builder builder = BeginGameRq.newBuilder();
        builder.setServerId(120);
//        builder.setKeyId(53231);
//        builder.setToken("a331443cdc1f4284a2b9f5d871e1fb22");

        builder.setKeyId(59979);
        builder.setToken("813118cf2feb463bbec1a85ef651f365");
//        builder.setKeyId(59690);
//        builder.setToken("645025379fbd4e3c9e2072921189960b");

        builder.setDeviceNo("00000000-2625-0b64-7b72-55e30033c587");
        builder.setCurVersion("1.0.0");

        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(BeginGameRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(BeginGameRq.ext, builder.build());
        sendMsgToServer(baseBuilder);

        Base rs = getMessage(BeginGameRs.EXT_FIELD_NUMBER, 5000);
        if (null != rs && rs.getCmd() == BeginGameRs.EXT_FIELD_NUMBER && rs.getCode() == GameError.OK.getCode()) {
            return rs.getExtension(BeginGameRs.ext);
        }
        return null;
    }

    public static void main(String[] args) throws InterruptedException {
        // 游戏服务器IP
        String serverIp = "127.0.0.1";
        // 游戏服务器端口
        int port = 9203;
        if (null != args && args.length >= 2) {
            serverIp = args[0].trim();
            port = Integer.parseInt(args[1].trim());
        }

        // 启动客户端线程
        LoginClient client = new LoginClient(serverIp, port);
        Thread thread = new Thread(client);
        thread.start();

        // 等待与服务器建立连接
        while (client.connecting) {
            Thread.sleep(1000);
            ClientLogger.print("连接服务器...");
        }

        if (!client.connected) {
            ClientLogger.print("连接服务器失败，即将退出");
            return;
        }

        // 发送开始游戏协议
        BeginGameRs rs = client.beginGame();
        System.out.println("rs:" + rs);
        if (null == rs) {
            ClientLogger.print("开始游戏失败，退出...");
            return;
        }

        // 角色未创建，创建角色
        if (rs.getState() == 1) {
            CreateRoleRs createRs = client.createRole();
            if (null == createRs || createRs.getState() != 1) {
                LogUtil.error("角色创建失败，退出...");
                return;
            }
        }

//        client.doSome("clear airship");
//        client.getMap(474);
        // client.wishing();
//        client.GetActivityDataInfoRq();


//        client.buyCrossWarFireBuff(1);
//        client.getCrossWarFireRanks();
//        client.getCrossWarFirePlayerLive();
//        client.getAllCampSummary();
//        client.getCrossWarFireRanks();
//        client.getPlayerShow();
//        client.sendCrossChat();
//        client.getGamePlayChatRoom();
//        client.getChatRoomMsg();
          client.getAsyncMailReport();

//        client.drawSmallGameAwardRq();
//        client.getSmallGame();
//        client.CrtMusicCampRankRq();
//        client.crtMusicPersonRankRq();
//        client.finishMusicCrtTask();
//        client.giveUpCrtMusicRq();
//        client.getChooseDifficultRq();
//        client.getGetActivityDataInfoRq();
//        client.getCrossRechargeActivityRank();
//        client.getActivityData();
//        client.upgradeHeroCgy();
//        client.upgradeHeroSkillRq();
//        client.synthSeasonHeroRq();
//        client.getActivityListRq();
//        client.getActivityRq(ActivityConst.ACT_MONSTER_NIAN);
//        client.setOffFirecrackers();
//        client.getWarFireScoreRank();
//        Thread.sleep(500);
//        client.getCampCitys();
//        Thread.sleep(500);
//        client.getWarFireCampSummary();
//        client.getPlayerWarFireRq();

        // client.getPayTurnplate();
        // client.playPayTurnplate();
        // client.interactionRq();
        // client.openCiaRq();
        // client.gestapoKillCampRankRq();
        // client.getDailyTask();
        // client.dailyAwardRq();
        // client.buyStoneCombat();
        // client.doStoneCombat();
        // client.getStoneCombat();
        // client.stoneMounting();
        // client.stoneUpLv();
        // client.getStoneInfo();
        // client.getMyRank();
        // client.getArea();
        // client.getDisplayActList();
        // 角色登录
        // client.roleLogin();
        // client.moveCity();
        // client.getRankAct();
        // client.getMonthCard();
        // client.giftShow();
        // client.getAct(102);
        // client.getAct(305);
        // client.getActivityAward(305, );
        // client.getOnLineAward(3050102);
        // client.getAreaCentreCity();

    }

    private void buyCrossWarFireBuff(int buffType){
        BuyCrossWarFireBuffRq.Builder builder = BuyCrossWarFireBuffRq.newBuilder();
        builder.setBuffType(1);
        builder.setFunctionId(3001);
        Base.Builder baseBuilder = PbHelper.createRqBase(BuyCrossWarFireBuffRq.EXT_FIELD_NUMBER, null,
                BuyCrossWarFireBuffRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getCrossWarFirePlayerLive(){
        GetCrossWarFirePlayerLiveRq.Builder builder = GetCrossWarFirePlayerLiveRq.newBuilder();
        builder.setFunctionId(3001);
        Base.Builder baseBuilder = PbHelper.createRqBase(GetCrossWarFirePlayerLiveRq.EXT_FIELD_NUMBER, null,
                GetCrossWarFirePlayerLiveRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getAllCampSummary(){
        GetCrossWarFireCampSummaryRq.Builder builder = GetCrossWarFireCampSummaryRq.newBuilder();
        builder.setFunctionId(3001);
        Base.Builder baseBuilder = PbHelper.createRqBase(GetCrossWarFireCampSummaryRq.EXT_FIELD_NUMBER, null,
                GetCrossWarFireCampSummaryRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getCrossWarFireRanks(){
        GetCrossWarFireRanksRq.Builder builder = GetCrossWarFireRanksRq.newBuilder();
        builder.setFunctionId(3001);
        builder.setCrossCamp(0);
        builder.setPage(1);
        Base.Builder baseBuilder = PbHelper.createRqBase(GetCrossWarFireRanksRq.EXT_FIELD_NUMBER, null,
                GetCrossWarFireRanksRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getPlayerShow(){
        GamePb7.GetRoomPlayerShowRq.Builder builder = GamePb7.GetRoomPlayerShowRq.newBuilder();
        builder.setRoomId(1467783110605058050L);
        builder.setChlId(1);
        builder.setChatMsgId(1);
        Base.Builder baseBuilder = PbHelper.createRqBase(GamePb7.GetRoomPlayerShowRq.EXT_FIELD_NUMBER, null,
                GamePb7.GetRoomPlayerShowRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void sendCrossChat(){
        SendChatRq.Builder builder = SendChatRq.newBuilder();
        builder.setChannel(6);
        builder.addContent("2");
        builder.setRoomId(1467783110605058050L);
        builder.setChlId(1);
        builder.setMemberId(1);
        Base.Builder baseBuilder = PbHelper.createRqBase(SendChatRq.EXT_FIELD_NUMBER, null,
                SendChatRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getAsyncMailReport() {
        for (int i=0; i < 30; i++) {
            GamePb5.GetMailReportRq.Builder builder = GamePb5.GetMailReportRq.newBuilder();
            builder.setMailKeyId(50);
            Base.Builder baseBuilder = PbHelper.createRqBase(GamePb5.GetMailReportRq.EXT_FIELD_NUMBER, null,
                    GamePb5.GetMailReportRq.ext, builder.build());
            sendMsgToServer(baseBuilder);
        }
    }

    private void getChatRoomMsg(){
        GamePb7.GetChatRoomMsgRq.Builder builder = GamePb7.GetChatRoomMsgRq.newBuilder();
        builder.setRoomId(2)
                .setChlId(1)
                .setMemberId(5);
        Base.Builder baseBuilder = PbHelper.createRqBase(GamePb7.GetChatRoomMsgRq.EXT_FIELD_NUMBER, null,
                GamePb7.GetChatRoomMsgRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getGamePlayChatRoom(){
        GamePb7.GetGamePlayChatRoomRq.Builder builder = GamePb7.GetGamePlayChatRoomRq.newBuilder();
        builder.setGamePlayPlanId(1200101);
        Base.Builder baseBuilder = PbHelper.createRqBase(GamePb7.GetGamePlayChatRoomRq.EXT_FIELD_NUMBER, null,
                GamePb7.GetGamePlayChatRoomRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void drawSmallGameAwardRq(){
        DrawSmallGameAwardRq.Builder builder = DrawSmallGameAwardRq.newBuilder();
        builder.setAwardType(1);
        builder.setId(6);
        Base.Builder baseBuilder = PbHelper.createRqBase(DrawSmallGameAwardRq.EXT_FIELD_NUMBER, null, DrawSmallGameAwardRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getSmallGame(){
        GetSmallGameRq.Builder builder = GetSmallGameRq.newBuilder();
        Base.Builder baseBuilder = PbHelper.createRqBase(GetSmallGameRq.EXT_FIELD_NUMBER, null, GetSmallGameRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void GetActivityDataInfoRq(){
        GetActivityDataInfoRq.Builder builder = GetActivityDataInfoRq.newBuilder();
        builder.setActivityType(ActivityConst.ACT_MUSIC_FESTIVAL_CREATIVE_OFFICE);
        Base.Builder baseBuilder = PbHelper.createRqBase(GetActivityDataInfoRq.EXT_FIELD_NUMBER, null, GetActivityDataInfoRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void CrtMusicCampRankRq(){
        CrtMusicCampRankRq.Builder builder = CrtMusicCampRankRq.newBuilder();
        builder.setActType(ActivityConst.ACT_MUSIC_FESTIVAL_CREATIVE_OFFICE);
        Base.Builder baseBuilder = PbHelper.createRqBase(CrtMusicCampRankRq.EXT_FIELD_NUMBER, null, CrtMusicCampRankRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void crtMusicPersonRankRq(){
        CrtMusicPersonRankRq.Builder builder = CrtMusicPersonRankRq.newBuilder();
        builder.setActType(ActivityConst.ACT_MUSIC_FESTIVAL_CREATIVE_OFFICE);
        builder.setPage(1);
        builder.setPageSize(20);
        Base.Builder baseBuilder = PbHelper.createRqBase(CrtMusicPersonRankRq.EXT_FIELD_NUMBER, null, CrtMusicPersonRankRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void finishMusicCrtTask(){
        FinishCrtTaskRq.Builder builder = FinishCrtTaskRq.newBuilder();
        builder.setActType(ActivityConst.ACT_MUSIC_FESTIVAL_CREATIVE_OFFICE);
        builder.setTaskUid(6501011);
        Base.Builder baseBuilder = PbHelper.createRqBase(FinishCrtTaskRq.EXT_FIELD_NUMBER, null, FinishCrtTaskRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void giveUpCrtMusicRq(){
        GiveUpCrtMusicRq.Builder builder = GiveUpCrtMusicRq.newBuilder();
        builder.setActType(ActivityConst.ACT_MUSIC_FESTIVAL_CREATIVE_OFFICE);
        Base.Builder baseBuilder = PbHelper.createRqBase(GiveUpCrtMusicRq.EXT_FIELD_NUMBER, null, GiveUpCrtMusicRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getChooseDifficultRq(){
        ChooseDifficultRq.Builder builder = ChooseDifficultRq.newBuilder();
        builder.setActType(ActivityConst.ACT_MUSIC_FESTIVAL_CREATIVE_OFFICE);
        builder.setDifficultModel(1);
        builder.setFree(1);
        Base.Builder baseBuilder = PbHelper.createRqBase(ChooseDifficultRq.EXT_FIELD_NUMBER, null, ChooseDifficultRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getGetActivityDataInfoRq(){
        GetActivityDataInfoRq.Builder builder = GetActivityDataInfoRq.newBuilder();
        builder.setActivityType(ActivityConst.ACT_MUSIC_FESTIVAL_CREATIVE_OFFICE);
        Base.Builder baseBuilder = PbHelper.createRqBase(GetActivityDataInfoRq.EXT_FIELD_NUMBER, null, GetActivityDataInfoRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getCrossRechargeActivityRank(){
        GetCrossRechargeRankingRq.Builder builder = GetCrossRechargeRankingRq.newBuilder();
        builder.setActType(ActivityConst.CROSS_ACT_RECHARGE_RANK);
        builder.setRankType(0);
        Base.Builder baseBuilder = PbHelper.createRqBase(GetCrossRechargeRankingRq.EXT_FIELD_NUMBER, null, GetCrossRechargeRankingRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getActivityData(){
        GamePb4.GetActivityDataInfoRq.Builder builder = GamePb4.GetActivityDataInfoRq.newBuilder();
        builder.setActivityType(ActivityConst.ACT_ANNIVERSARY_SKIN);
        Base.Builder baseBuilder = PbHelper.createRqBase(GamePb4.GetActivityDataInfoRq.EXT_FIELD_NUMBER, null, GamePb4.GetActivityDataInfoRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void upgradeHeroCgy(){
        GamePb4.UpgradeHeroCgyRq.Builder builder = GamePb4.UpgradeHeroCgyRq.newBuilder();
        builder.setHeroId(2000);
        builder.setStage(1);
        builder.setLv(1);
        Base.Builder baseBuilder = PbHelper.createRqBase(GamePb4.UpgradeHeroCgyRq.EXT_FIELD_NUMBER, null, GamePb4.UpgradeHeroCgyRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void upgradeHeroSkillRq(){
        GamePb4.UpgradeHeroSkillRq.Builder builder = GamePb4.UpgradeHeroSkillRq.newBuilder();
        builder.setHeroId(2000);
        builder.setSkillId(1001);
        Base.Builder baseBuilder = PbHelper.createRqBase(GamePb4.UpgradeHeroSkillRq.EXT_FIELD_NUMBER, null, GamePb4.UpgradeHeroSkillRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void synthSeasonHeroRq(){
        GamePb4.SynthSeasonHeroRq.Builder builder = GamePb4.SynthSeasonHeroRq.newBuilder();
        builder.setHeroId(2000);
        Base.Builder baseBuilder = PbHelper.createRqBase(GamePb4.SynthSeasonHeroRq.EXT_FIELD_NUMBER, null, GamePb4.SynthSeasonHeroRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void setOffFirecrackers(){
        GamePb4.SetOffFirecrackersRq.Builder builer = GamePb4.SetOffFirecrackersRq.newBuilder();
        Base.Builder baseBuilder = PbHelper.createRqBase(GamePb4.SetOffFirecrackersRq.EXT_FIELD_NUMBER, null, GamePb4.SetOffFirecrackersRq.ext, builer.build());
        sendMsgToServer(baseBuilder);
    }

    private void getActivityRq(int atype){
        GetActivityRq.Builder rqb = GetActivityRq.newBuilder();
        rqb.setType(atype);
        Base.Builder baseBuilder = PbHelper.createRqBase(GamePb3.GetActivityRq.EXT_FIELD_NUMBER, null, GamePb3.GetActivityRq.ext, rqb.build());
        sendMsgToServer(baseBuilder);
    }

    private void getActivityListRq(){
        GamePb3.GetActivityListRq.Builder builer = GamePb3.GetActivityListRq.newBuilder();
        Base.Builder baseBuilder = PbHelper.createRqBase(GamePb3.GetActivityListRq.EXT_FIELD_NUMBER, null, GamePb3.GetActivityListRq.ext, builer.build());
        sendMsgToServer(baseBuilder);
    }

    private void getPlayerWarFireRq(){
        GetPlayerWarFireRq.Builder rqb = GetPlayerWarFireRq.newBuilder();
        rqb.setMapId(CrossWorldMapConstant.CROSS_MAP_ID);
        Base.Builder baseBuilder = PbHelper.createRqBase(GetPlayerWarFireRq.EXT_FIELD_NUMBER, null, GetPlayerWarFireRq.ext, rqb.build());
        sendMsgToServer(baseBuilder);
    }

    private void getWarFireCampSummary(){
        GetWarFireCampSummaryRq.Builder rqb = GetWarFireCampSummaryRq.newBuilder();
        rqb.setMapId(CrossWorldMapConstant.CROSS_MAP_ID);
        Base.Builder baseBuilder = PbHelper.createRqBase(GetWarFireCampSummaryRq.EXT_FIELD_NUMBER, null, GetWarFireCampSummaryRq.ext, rqb.build());
        sendMsgToServer(baseBuilder);
    }

    private void getCampCitys(){
        GetWarFireCampScoreRq.Builder rqb = GetWarFireCampScoreRq.newBuilder();
        rqb.setMapId(CrossWorldMapConstant.CROSS_MAP_ID);
        Base.Builder baseBuilder = PbHelper.createRqBase(GetWarFireCampScoreRq.EXT_FIELD_NUMBER, null, GetWarFireCampScoreRq.ext, rqb.build());
        sendMsgToServer(baseBuilder);
    }

    private void getWarFireScoreRank(){
        GetWarFireCampRankRq.Builder rqb = GetWarFireCampRankRq.newBuilder();
        rqb.setMapId(CrossWorldMapConstant.CROSS_MAP_ID);
        rqb.setCamp(1);
        rqb.setPage(1);
        Base.Builder baseBuilder = PbHelper.createRqBase(GetWarFireCampRankRq.EXT_FIELD_NUMBER, null, GetWarFireCampRankRq.ext, rqb.build());
        sendMsgToServer(baseBuilder);
    }

    private void doSome(String str) {
        DoSomeRq.Builder builder = DoSomeRq.newBuilder();
        builder.setStr(str);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(DoSomeRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(DoSomeRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getMap(int block) {
        GetMapRq.Builder builder = GetMapRq.newBuilder();
        builder.addBlock(block);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GetMapRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GetMapRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void wishing() {
        WishingRq.Builder builder = WishingRq.newBuilder();
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(WishingRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(WishingRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void playPayTurnplate() {
        PlayPayTurnplateRq.Builder builder = PlayPayTurnplateRq.newBuilder();
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(PlayPayTurnplateRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(PlayPayTurnplateRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getPayTurnplate() {
        GetPayTurnplateRq.Builder builder = GetPayTurnplateRq.newBuilder();
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GetPayTurnplateRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GetPayTurnplateRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void interactionRq() {
        InteractionRq.Builder builder = InteractionRq.newBuilder();
        builder.setId(1);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(InteractionRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(InteractionRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void gestapoKillCampRankRq() {
        GestapoKillCampRankRq.Builder builder = GestapoKillCampRankRq.newBuilder();
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GestapoKillCampRankRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GestapoKillCampRankRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void dailyAwardRq() {
        DailyAwardRq.Builder builder = DailyAwardRq.newBuilder();
        builder.setId(1);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(DailyAwardRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(DailyAwardRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getDailyTask() {
        GetDailyTaskRq.Builder builder = GetDailyTaskRq.newBuilder();
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GetDailyTaskRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GetDailyTaskRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void buyStoneCombat() {
        BuyStoneCombatRq.Builder builder = BuyStoneCombatRq.newBuilder();
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(BuyStoneCombatRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(BuyStoneCombatRq.ext, builder.build());
        sendMsgToServer(baseBuilder);

    }

    private void doStoneCombat() {
        DoStoneCombatRq.Builder builder = DoStoneCombatRq.newBuilder();
        builder.setCombatId(2011);
        builder.setWipe(0);
        builder.addHeroId(1494);
        builder.addHeroId(1464);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(DoStoneCombatRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(DoStoneCombatRq.ext, builder.build());
        sendMsgToServer(baseBuilder);

    }

    private void stoneMounting() {
        StoneMountingRq.Builder builder = StoneMountingRq.newBuilder();
        builder.setHole(1);
        builder.setStoneId(101);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(StoneMountingRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(StoneMountingRq.ext, builder.build());
        sendMsgToServer(baseBuilder);

    }

    private void stoneUpLv() {
        StoneUpLvRq.Builder builder = StoneUpLvRq.newBuilder();
        builder.setType(1);
        builder.setParam(1);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(StoneUpLvRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(StoneUpLvRq.ext, builder.build());
        sendMsgToServer(baseBuilder);

    }

    private void getStoneInfo() {
        GetStoneInfoRq.Builder builder = GetStoneInfoRq.newBuilder();
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GetStoneInfoRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GetStoneInfoRq.ext, builder.build());
        sendMsgToServer(baseBuilder);

    }

    private void getStoneCombat() {
        GetStoneCombatRq.Builder builder = GetStoneCombatRq.newBuilder();
        builder.setOption(0);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GetStoneCombatRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GetStoneCombatRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getMyRank() {
        GetMyRankRq.Builder builder = GetMyRankRq.newBuilder();
        builder.setType(6);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GetMyRankRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GetMyRankRq.ext, builder.build());
        sendMsgToServer(baseBuilder);

    }

    private void getAreaCentreCity() {
        GetAreaCentreCityRq.Builder builder = GetAreaCentreCityRq.newBuilder();
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GetAreaCentreCityRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GetAreaCentreCityRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    // 获取夜袭信息
    private void getNightRaidInfo() {
        GetNightRaidInfoRq.Builder builder = GetNightRaidInfoRq.newBuilder();
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GetNightRaidInfoRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GetNightRaidInfoRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void getOnLineAward(int keyId) {
        GamePb3.GetOnLineAwardRq.Builder builder = GamePb3.GetOnLineAwardRq.newBuilder();
        builder.setKeyId(keyId);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GamePb3.GetOnLineAwardRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GamePb3.GetOnLineAwardRq.ext, builder.build());
        sendMsgToServer(baseBuilder);

    }

    private void giftShow() {
        GamePb3.GiftShowRq.Builder builder = GamePb3.GiftShowRq.newBuilder();
        builder.setParam(0);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GamePb3.GiftShowRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GamePb3.GiftShowRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    public void getMonthCard() {
        GamePb4.GetMonthCardRq.Builder builder = GamePb4.GetMonthCardRq.newBuilder();
        builder.setParam(0);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GamePb4.GetMonthCardRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GamePb4.GetMonthCardRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    private void moveCity() {
        MoveCityRq.Builder builder = MoveCityRq.newBuilder();
        builder.setPos(8755);
        builder.setType(4);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(MoveCityRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(MoveCityRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    /**
     * 节气
     */
    private void getSolarTerms() {
        GetActRankRq.Builder builder = GetActRankRq.newBuilder();
        builder.setActivityType(402);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GetActRankRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GetActRankRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    /**
     * 获取某个排行活动
     */
    private void getRankAct() {
        GetActRankRq.Builder builder = GetActRankRq.newBuilder();
        builder.setActivityType(402);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GetActRankRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GetActRankRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    /**
     * 获取活动
     */
    private void getAct(int type) {
        GetActivityRq.Builder builder = GetActivityRq.newBuilder();
        builder.setType(type);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GetActivityRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GetActivityRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    /**
     * 活动领奖
     */
    private void getActivityAward(int actType, int keyId) {
        GetActivityAwardRq.Builder builder = GetActivityAwardRq.newBuilder();
        builder.setActivityType(actType);
        builder.setKeyId(keyId);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GetActivityAwardRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GetActivityAwardRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    /**
     * 角色登录
     */
    private void roleLogin() {
        RoleLoginRq.Builder builder = RoleLoginRq.newBuilder();

        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(RoleLoginRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(RoleLoginRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    /**
     * 创建角色
     *
     * @return
     */
    private CreateRoleRs createRole() {
        CreateRoleRq.Builder builder = CreateRoleRq.newBuilder();
        builder.setCamp(0);
        builder.build();

        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(CreateRoleRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(CreateRoleRq.ext, builder.build());
        sendMsgToServer(baseBuilder);

        Base rs = getMessage(CreateRoleRs.EXT_FIELD_NUMBER, 5000);
        if (null != rs && rs.getCmd() == CreateRoleRs.EXT_FIELD_NUMBER && rs.getCode() == GameError.OK.getCode()) {
            return rs.getExtension(CreateRoleRs.ext);
        }
        return null;
    }

    public void getDisplayActList() {
        GamePb3.GetDisplayActListRq.Builder builder = GamePb3.GetDisplayActListRq.newBuilder();
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GamePb3.GetDisplayActListRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GamePb3.GetDisplayActListRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }

    public void getArea() {
        GamePb2.GetAreaRq.Builder builder = GamePb2.GetAreaRq.newBuilder();
        builder.setArea(1);
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(GamePb2.GetAreaRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GamePb2.GetAreaRq.ext, builder.build());
        sendMsgToServer(baseBuilder);
    }
}
