package com.gryphpoem.game.zw.message;

import com.gryphpoem.game.zw.core.handler.AbsClientHandler;
import com.gryphpoem.game.zw.core.handler.AbsHttpHandler;
import com.gryphpoem.game.zw.core.handler.AbsInnerHandler;
import com.gryphpoem.game.zw.core.message.IMessagePool;
import com.gryphpoem.game.zw.handler.client.account.BeginGameHandler;
import com.gryphpoem.game.zw.handler.client.account.CastleSkinStarUpHandler;
import com.gryphpoem.game.zw.handler.client.account.ChangeBodyImageHandler;
import com.gryphpoem.game.zw.handler.client.account.ChangeCastleSkinHandler;
import com.gryphpoem.game.zw.handler.client.account.ChangeLordNameHandler;
import com.gryphpoem.game.zw.handler.client.account.ChangePortraitHandler;
import com.gryphpoem.game.zw.handler.client.account.ChangeSignatureHandler;
import com.gryphpoem.game.zw.handler.client.account.CompareNotesHandler;
import com.gryphpoem.game.zw.handler.client.account.CreateRoleHandler;
import com.gryphpoem.game.zw.handler.client.account.GetBodyImageHandler;
import com.gryphpoem.game.zw.handler.client.account.GetCastleSkinHandler;
import com.gryphpoem.game.zw.handler.client.account.GetLordHandler;
import com.gryphpoem.game.zw.handler.client.account.GetMixtureDataHandler;
import com.gryphpoem.game.zw.handler.client.account.GetMonthCardHandler;
import com.gryphpoem.game.zw.handler.client.account.GetMyRankHandler;
import com.gryphpoem.game.zw.handler.client.account.GetNamesHandler;
import com.gryphpoem.game.zw.handler.client.account.GetOffLineIncome;
import com.gryphpoem.game.zw.handler.client.account.GetPortraitHandler;
import com.gryphpoem.game.zw.handler.client.account.GiftCodeHandler;
import com.gryphpoem.game.zw.handler.client.account.JoinCommunityHandler;
import com.gryphpoem.game.zw.handler.client.account.RoleLoginHandler;
import com.gryphpoem.game.zw.handler.client.account.SeachPlayerHandler;
import com.gryphpoem.game.zw.handler.client.account.SetGuideHandler;
import com.gryphpoem.game.zw.handler.client.acquisition.AcquisiteRewradHandler;
import com.gryphpoem.game.zw.handler.client.acquisition.BeginAcquisiteHandler;
import com.gryphpoem.game.zw.handler.client.acquisition.GetAcquisitionHandler;
import com.gryphpoem.game.zw.handler.client.acquisition.GetStatusHandler;
import com.gryphpoem.game.zw.handler.client.acquisition.GetVipCntHandler;
import com.gryphpoem.game.zw.handler.client.acquisition.UseFreeSpeedHandler;
import com.gryphpoem.game.zw.handler.client.active.ActGoodLuckAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.ActGrowBuyHandler;
import com.gryphpoem.game.zw.handler.client.active.ActHotProductAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.AdvertisementRewardHandler;
import com.gryphpoem.game.zw.handler.client.active.AnniversaryEggOpenHandler;
import com.gryphpoem.game.zw.handler.client.active.AnniversaryJigsawHandler;
import com.gryphpoem.game.zw.handler.client.active.AnniversaryTurntablePlayHandler;
import com.gryphpoem.game.zw.handler.client.active.AutumnTurnplateGetProgressAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.AutumnTurnplatePlayHandler;
import com.gryphpoem.game.zw.handler.client.active.AutumnTurnplateRefreshHandler;
import com.gryphpoem.game.zw.handler.client.active.BlackhawkBuyHandler;
import com.gryphpoem.game.zw.handler.client.active.BlackhawkHeroHandler;
import com.gryphpoem.game.zw.handler.client.active.BlackhawkRefreshHandler;
import com.gryphpoem.game.zw.handler.client.active.BoxOfficeActionHandler;
import com.gryphpoem.game.zw.handler.client.active.ChristmasGetAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.ChristmasGetInfoHandler;
import com.gryphpoem.game.zw.handler.client.active.ChristmasHandInChipHandler;
import com.gryphpoem.game.zw.handler.client.active.DailyKeepRechargeGetAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.DiaoChanGetAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.DiaoChanGetInfoHandler;
import com.gryphpoem.game.zw.handler.client.active.DiaoChanGetRankInfoHandler;
import com.gryphpoem.game.zw.handler.client.active.DragonBoatExchangeHandler;
import com.gryphpoem.game.zw.handler.client.active.DrawMagicTwTurntableAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.EmpireFarmOpenTreasureChestHandler;
import com.gryphpoem.game.zw.handler.client.active.EmpireFarmSowingHandler;
import com.gryphpoem.game.zw.handler.client.active.EquipTurnplateHandler;
import com.gryphpoem.game.zw.handler.client.active.ExchangeActAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.FireworkLetoffHandler;
import com.gryphpoem.game.zw.handler.client.active.GetActBlackhawkHandler;
import com.gryphpoem.game.zw.handler.client.active.GetActRankHandler;
import com.gryphpoem.game.zw.handler.client.active.GetActTurnplatHandler;
import com.gryphpoem.game.zw.handler.client.active.GetActivityAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.GetActivityDataInfoHandler;
import com.gryphpoem.game.zw.handler.client.active.GetActivityHandler;
import com.gryphpoem.game.zw.handler.client.active.GetActivityListHandler;
import com.gryphpoem.game.zw.handler.client.active.GetAtkCityActHandler;
import com.gryphpoem.game.zw.handler.client.active.GetDay7ActHandler;
import com.gryphpoem.game.zw.handler.client.active.GetDayDiscountsHandler;
import com.gryphpoem.game.zw.handler.client.active.GetDisplayActListHandler;
import com.gryphpoem.game.zw.handler.client.active.GetEasterActAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.GetEquipTurnplatHandler;
import com.gryphpoem.game.zw.handler.client.active.GetFreePowerHandler;
import com.gryphpoem.game.zw.handler.client.active.GetLuckyPoolHandler;
import com.gryphpoem.game.zw.handler.client.active.GetLuckyPoolRankHandler;
import com.gryphpoem.game.zw.handler.client.active.GetMonopolyHandler;
import com.gryphpoem.game.zw.handler.client.active.GetOnLineAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.GetOreTurnplateHandler;
import com.gryphpoem.game.zw.handler.client.active.GetOreTurnplateNewHandler;
import com.gryphpoem.game.zw.handler.client.active.GetPayTurnplateHandler;
import com.gryphpoem.game.zw.handler.client.active.GetPowerGiveDataHandler;
import com.gryphpoem.game.zw.handler.client.active.GetSpecialActHandler;
import com.gryphpoem.game.zw.handler.client.active.GetSupplyDorpHandler;
import com.gryphpoem.game.zw.handler.client.active.GetTriggerGiftHandler;
import com.gryphpoem.game.zw.handler.client.active.GiftShowHandler;
import com.gryphpoem.game.zw.handler.client.active.GoldenAutumnFruitfulHandler;
import com.gryphpoem.game.zw.handler.client.active.GoldenAutumnGetTaskAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.GoldenAutumnOpenTreasureChestHandler;
import com.gryphpoem.game.zw.handler.client.active.HelpShengYuGetAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.LongLightIgniteHandler;
import com.gryphpoem.game.zw.handler.client.active.LuckyTurnplateHandler;
import com.gryphpoem.game.zw.handler.client.active.PlayLuckyPoolHandler;
import com.gryphpoem.game.zw.handler.client.active.PlayMonopolyHandler;
import com.gryphpoem.game.zw.handler.client.active.PlayOreTurnplateHandler;
import com.gryphpoem.game.zw.handler.client.active.PlayOreTurnplateNewHandler;
import com.gryphpoem.game.zw.handler.client.active.PlayPayTurnplateHandler;
import com.gryphpoem.game.zw.handler.client.active.PromotionGiftBuyHandler;
import com.gryphpoem.game.zw.handler.client.active.ReceiveMTwtCntAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.RecvActiveHandler;
import com.gryphpoem.game.zw.handler.client.active.RecvDay7ActAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.SetOffFirecrackersNianHandler;
import com.gryphpoem.game.zw.handler.client.active.SummerCastleGetAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.SummerTurntableNextHandler;
import com.gryphpoem.game.zw.handler.client.active.SummerTurntablePlayHandler;
import com.gryphpoem.game.zw.handler.client.active.SupplyDorpAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.SupplyDorpBuyHandler;
import com.gryphpoem.game.zw.handler.client.active.ThreeRebateHandler;
import com.gryphpoem.game.zw.handler.client.active.TriggerGIftBuyHandler;
import com.gryphpoem.game.zw.handler.client.active.TurnplatCntAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.WishingHandler;
import com.gryphpoem.game.zw.handler.client.active.YearFishBeginHandler;
import com.gryphpoem.game.zw.handler.client.active.YearFishEndHandler;
import com.gryphpoem.game.zw.handler.client.active.YearFishShopExchangeHandler;
import com.gryphpoem.game.zw.handler.client.active.anniversary.AnniversaryFireFireWorkHandler;
import com.gryphpoem.game.zw.handler.client.active.anniversary.BuyEncoreSkinHandler;
import com.gryphpoem.game.zw.handler.client.active.auction.FollowAuctionsHandler;
import com.gryphpoem.game.zw.handler.client.active.auction.GetActAuctionInfoHandler;
import com.gryphpoem.game.zw.handler.client.active.auction.GetActAuctionItemRecordHandler;
import com.gryphpoem.game.zw.handler.client.active.auction.GetActAuctionRecordHandler;
import com.gryphpoem.game.zw.handler.client.active.auction.GetActAuctionTypeHandler;
import com.gryphpoem.game.zw.handler.client.active.auction.GetMyAuctionRecordHandler;
import com.gryphpoem.game.zw.handler.client.active.auction.PurchaseAuctionItemHandler;
import com.gryphpoem.game.zw.handler.client.active.barton.ActBartonBuyHandler;
import com.gryphpoem.game.zw.handler.client.active.barton.GetActBartonHandler;
import com.gryphpoem.game.zw.handler.client.active.music.CrtMusicCampRankHandler;
import com.gryphpoem.game.zw.handler.client.active.music.CrtMusicPersonRankHandler;
import com.gryphpoem.game.zw.handler.client.active.music.DrawProgressHandler;
import com.gryphpoem.game.zw.handler.client.active.music.FinishCreateMusicHandler;
import com.gryphpoem.game.zw.handler.client.active.music.GetSpecialThanksHandler;
import com.gryphpoem.game.zw.handler.client.active.music.MusicCrtOfficeChooseDifficultHandler;
import com.gryphpoem.game.zw.handler.client.active.music.MusicCrtOfficeFinishTaskHandler;
import com.gryphpoem.game.zw.handler.client.active.music.MusicCrtOfficeGiveUpCrtHandler;
import com.gryphpoem.game.zw.handler.client.active.ramadan.GetAltarHandler;
import com.gryphpoem.game.zw.handler.client.active.robin.ActRobinHoodAwardHandler;
import com.gryphpoem.game.zw.handler.client.active.robin.GetActRobinHoodHandler;
import com.gryphpoem.game.zw.handler.client.army.AutoAddArmyHandler;
import com.gryphpoem.game.zw.handler.client.army.GetArmyHandler;
import com.gryphpoem.game.zw.handler.client.army.QuickBuyArmyHandler;
import com.gryphpoem.game.zw.handler.client.army.ReplenishHandler;
import com.gryphpoem.game.zw.handler.client.bandit.SearchBanditHandler;
import com.gryphpoem.game.zw.handler.client.battlepass.BuyBattlePassLvHandler;
import com.gryphpoem.game.zw.handler.client.battlepass.GetBattlePassHandler;
import com.gryphpoem.game.zw.handler.client.battlepass.ReceiveBPAwardHandler;
import com.gryphpoem.game.zw.handler.client.battlepass.ReceiveTaskAwardHandler;
import com.gryphpoem.game.zw.handler.client.building.BuildingHanlder;
import com.gryphpoem.game.zw.handler.client.building.CommandAddHandler;
import com.gryphpoem.game.zw.handler.client.building.DesBuildingHanlder;
import com.gryphpoem.game.zw.handler.client.building.EquipFactoryRecruitHandler;
import com.gryphpoem.game.zw.handler.client.building.GainResHandler;
import com.gryphpoem.game.zw.handler.client.building.GetCommandHandler;
import com.gryphpoem.game.zw.handler.client.building.GetEquipFactoryHandler;
import com.gryphpoem.game.zw.handler.client.building.OnOffAutoBuildHandler;
import com.gryphpoem.game.zw.handler.client.building.ReBuildHanlder;
import com.gryphpoem.game.zw.handler.client.building.RebuildRewardHanlder;
import com.gryphpoem.game.zw.handler.client.building.SpeedBuildingHandler;
import com.gryphpoem.game.zw.handler.client.building.SyncRoleRebuildHanlder;
import com.gryphpoem.game.zw.handler.client.building.TrainingBuildingHandler;
import com.gryphpoem.game.zw.handler.client.building.UpBuildingHanlder;
import com.gryphpoem.game.zw.handler.client.building.UptBuildingHanlder;
import com.gryphpoem.game.zw.handler.client.chapterTask.GetChapterAwardHandler;
import com.gryphpoem.game.zw.handler.client.chapterTask.GetChapterTaskAwardHandler;
import com.gryphpoem.game.zw.handler.client.chapterTask.GetChapterTaskHandler;
import com.gryphpoem.game.zw.handler.client.chat.AcceptRedPacketHandler;
import com.gryphpoem.game.zw.handler.client.chat.DelDialogHandler;
import com.gryphpoem.game.zw.handler.client.chat.GetActivityChatHandler;
import com.gryphpoem.game.zw.handler.client.chat.GetChatHandler;
import com.gryphpoem.game.zw.handler.client.chat.GetDialogHandler;
import com.gryphpoem.game.zw.handler.client.chat.GetFmsGelTunChatsHandler;
import com.gryphpoem.game.zw.handler.client.chat.GetPrivateChatHandler;
import com.gryphpoem.game.zw.handler.client.chat.GetRedPacketHandler;
import com.gryphpoem.game.zw.handler.client.chat.GetRedPacketListRqHandler;
import com.gryphpoem.game.zw.handler.client.chat.ReadDialogHandler;
import com.gryphpoem.game.zw.handler.client.chat.SendChatHandler;
import com.gryphpoem.game.zw.handler.client.chat.ShareReportHandler;
import com.gryphpoem.game.zw.handler.client.chemical.ChemicalExpandHandler;
import com.gryphpoem.game.zw.handler.client.chemical.ChemicalFinishHandler;
import com.gryphpoem.game.zw.handler.client.chemical.ChemicalRecruitHandler;
import com.gryphpoem.game.zw.handler.client.chemical.GetChemicalHandler;
import com.gryphpoem.game.zw.handler.client.cia.AgentUpgradeStarHandler;
import com.gryphpoem.game.zw.handler.client.cia.AppointmentHandler;
import com.gryphpoem.game.zw.handler.client.cia.GetCiaHandler;
import com.gryphpoem.game.zw.handler.client.cia.InteractionHandler;
import com.gryphpoem.game.zw.handler.client.cia.PresentGiftHandler;
import com.gryphpoem.game.zw.handler.client.cia.UnlockAgentHandler;
import com.gryphpoem.game.zw.handler.client.combat.BuyCombatHandler;
import com.gryphpoem.game.zw.handler.client.combat.BuyStoneCombatHandler;
import com.gryphpoem.game.zw.handler.client.combat.CreateCombatTeamHandler;
import com.gryphpoem.game.zw.handler.client.combat.DoCombatHandler;
import com.gryphpoem.game.zw.handler.client.combat.DoCombatWipeHandler;
import com.gryphpoem.game.zw.handler.client.combat.DoPitchCombatHandler;
import com.gryphpoem.game.zw.handler.client.combat.DoStoneCombatHandler;
import com.gryphpoem.game.zw.handler.client.combat.GetCombatHandler;
import com.gryphpoem.game.zw.handler.client.combat.GetMultCombatHandler;
import com.gryphpoem.game.zw.handler.client.combat.GetPitchCombatHandler;
import com.gryphpoem.game.zw.handler.client.combat.GetRankHandler;
import com.gryphpoem.game.zw.handler.client.combat.GetStoneCombatHandler;
import com.gryphpoem.game.zw.handler.client.combat.GetTeamMemberListHandler;
import com.gryphpoem.game.zw.handler.client.combat.JoinCombatTeamHandler;
import com.gryphpoem.game.zw.handler.client.combat.LeaveCombatTeamHandler;
import com.gryphpoem.game.zw.handler.client.combat.ModifyCombatTeamHandler;
import com.gryphpoem.game.zw.handler.client.combat.MultCombatShopBuyHandler;
import com.gryphpoem.game.zw.handler.client.combat.SendInvitationHandler;
import com.gryphpoem.game.zw.handler.client.combat.StartMultCombatHandler;
import com.gryphpoem.game.zw.handler.client.combat.TickTeamMemberHandler;
import com.gryphpoem.game.zw.handler.client.combat.WipeMultCombatHandler;
import com.gryphpoem.game.zw.handler.client.common.ActionPointHandler;
import com.gryphpoem.game.zw.handler.client.common.DoSomeHandler;
import com.gryphpoem.game.zw.handler.client.common.GetBannerHandler;
import com.gryphpoem.game.zw.handler.client.common.GetTimeHandler;
import com.gryphpoem.game.zw.handler.client.cross.AttackCrossPosHandler;
import com.gryphpoem.game.zw.handler.client.cross.CrossMoveCityHandler;
import com.gryphpoem.game.zw.handler.client.cross.EnterLeaveCrossHandler;
import com.gryphpoem.game.zw.handler.client.cross.GetCrossAreaHandler;
import com.gryphpoem.game.zw.handler.client.cross.GetCrossArmyHandler;
import com.gryphpoem.game.zw.handler.client.cross.GetCrossBattleHandler;
import com.gryphpoem.game.zw.handler.client.cross.GetCrossCityInfoHandler;
import com.gryphpoem.game.zw.handler.client.cross.GetCrossMapHandler;
import com.gryphpoem.game.zw.handler.client.cross.GetCrossMapInfoHandler;
import com.gryphpoem.game.zw.handler.client.cross.GetCrossMarchHandler;
import com.gryphpoem.game.zw.handler.client.cross.GetCrossMineHandler;
import com.gryphpoem.game.zw.handler.client.cross.JoinBattleCrossHandler;
import com.gryphpoem.game.zw.handler.client.cross.RetreatCrossHandler;
import com.gryphpoem.game.zw.handler.client.cross.activity.GetCrossRechargeActivityRankHandler;
import com.gryphpoem.game.zw.handler.client.cross.chat.GetChatRoomMsgHandler;
import com.gryphpoem.game.zw.handler.client.cross.chat.GetCrossPlayerShowHandler;
import com.gryphpoem.game.zw.handler.client.cross.chat.GetGamePlayChatRoomHandler;
import com.gryphpoem.game.zw.handler.client.cross.chat.GetRoomPlayerShowHandler;
import com.gryphpoem.game.zw.handler.client.cross.newyork.NewYorkWarAchievementHandler;
import com.gryphpoem.game.zw.handler.client.cross.newyork.NewYorkWarInfoHandler;
import com.gryphpoem.game.zw.handler.client.cross.newyork.NewYorkWarPlayerRankDataHandler;
import com.gryphpoem.game.zw.handler.client.cross.newyork.NewYorkWarProgressDataHandler;
import com.gryphpoem.game.zw.handler.client.cross.warfire.BuyWarFireBuffHandler;
import com.gryphpoem.game.zw.handler.client.cross.warfire.BuyWarFireShopHandler;
import com.gryphpoem.game.zw.handler.client.cross.warfire.GetWarFireCampRankHandler;
import com.gryphpoem.game.zw.handler.client.cross.warfire.GetWarFireCampScoreHandler;
import com.gryphpoem.game.zw.handler.client.cross.warfire.GetWarFireCampSummaryHandler;
import com.gryphpoem.game.zw.handler.client.cross.warfire.GetWarFirePlayerInfoHandler;
import com.gryphpoem.game.zw.handler.client.cross.worldwar.GetWorldWarAwardHandler;
import com.gryphpoem.game.zw.handler.client.cross.worldwar.GetWorldWarCampDateHandler;
import com.gryphpoem.game.zw.handler.client.cross.worldwar.GetWorldWarCampRankPlayersDateHandler;
import com.gryphpoem.game.zw.handler.client.cross.worldwar.GetWorldWarSeasonShopGoodsHandler;
import com.gryphpoem.game.zw.handler.client.cross.worldwar.GetWorldWarTaskDateHandler;
import com.gryphpoem.game.zw.handler.client.crosssimple.BuyCrossBuffHandler;
import com.gryphpoem.game.zw.handler.client.crosssimple.ChoiceHeroJoinHandler;
import com.gryphpoem.game.zw.handler.client.crosssimple.CrossTrophyAwardHandler;
import com.gryphpoem.game.zw.handler.client.crosssimple.CrossTrophyInfoHandler;
import com.gryphpoem.game.zw.handler.client.crosssimple.DirectForwardClientHandler;
import com.gryphpoem.game.zw.handler.client.crosssimple.EnterCrossHandler;
import com.gryphpoem.game.zw.handler.client.crosssimple.GetCrossInfoHandler;
import com.gryphpoem.game.zw.handler.client.crosssimple.OpFortHeroHandler;
import com.gryphpoem.game.zw.handler.client.dressup.ChangeDressUpHandler;
import com.gryphpoem.game.zw.handler.client.dressup.GetDressUpDataHandler;
import com.gryphpoem.game.zw.handler.client.equip.BagExpandHandler;
import com.gryphpoem.game.zw.handler.client.equip.DoJewelImproveHandler;
import com.gryphpoem.game.zw.handler.client.equip.EquipBaptizeHandler;
import com.gryphpoem.game.zw.handler.client.equip.EquipBatchDecomposeHandler;
import com.gryphpoem.game.zw.handler.client.equip.EquipDecomposeHandler;
import com.gryphpoem.game.zw.handler.client.equip.EquipForgeHandler;
import com.gryphpoem.game.zw.handler.client.equip.EquipGainHandler;
import com.gryphpoem.game.zw.handler.client.equip.EquipLockedHandler;
import com.gryphpoem.game.zw.handler.client.equip.GetEquipsHandler;
import com.gryphpoem.game.zw.handler.client.equip.GetJewelsHandler;
import com.gryphpoem.game.zw.handler.client.equip.GetSuperEquipHandler;
import com.gryphpoem.game.zw.handler.client.equip.GrowSuperEquipHandler;
import com.gryphpoem.game.zw.handler.client.equip.InlaidJewelHandler;
import com.gryphpoem.game.zw.handler.client.equip.OnEquipHandler;
import com.gryphpoem.game.zw.handler.client.equip.RingUpLvHandler;
import com.gryphpoem.game.zw.handler.client.equip.SpeedForgeHandler;
import com.gryphpoem.game.zw.handler.client.equip.SpeedSuperEquipHandler;
import com.gryphpoem.game.zw.handler.client.equip.SuperEquipForgeHandler;
import com.gryphpoem.game.zw.handler.client.equip.UpSuperEquipHandler;
import com.gryphpoem.game.zw.handler.client.factory.AddArmHandler;
import com.gryphpoem.game.zw.handler.client.factory.FactoryExpandHandler;
import com.gryphpoem.game.zw.handler.client.factory.FactoryRecruitHandler;
import com.gryphpoem.game.zw.handler.client.factory.GetFactoryHandler;
import com.gryphpoem.game.zw.handler.client.factory.RecruitCancelHandler;
import com.gryphpoem.game.zw.handler.client.factory.RecruitSpeedHandler;
import com.gryphpoem.game.zw.handler.client.factory.UpRecruitHandler;
import com.gryphpoem.game.zw.handler.client.fish.FishingAltasHandler;
import com.gryphpoem.game.zw.handler.client.fish.FishingCollectBaitDispatchHerosHandler;
import com.gryphpoem.game.zw.handler.client.fish.FishingCollectBaitGetAwardHandler;
import com.gryphpoem.game.zw.handler.client.fish.FishingCollectBaitGetInfoHandler;
import com.gryphpoem.game.zw.handler.client.fish.FishingFishLogHandler;
import com.gryphpoem.game.zw.handler.client.fish.FishingFisheryGetInfoHandler;
import com.gryphpoem.game.zw.handler.client.fish.FishingFisheryStowRodHandler;
import com.gryphpoem.game.zw.handler.client.fish.FishingFisheryThrowRodHandler;
import com.gryphpoem.game.zw.handler.client.fish.FishingGetFishAltasAwardHandler;
import com.gryphpoem.game.zw.handler.client.fish.FishingShareFishLogHandler;
import com.gryphpoem.game.zw.handler.client.fish.FishingShopExchangeHandler;
import com.gryphpoem.game.zw.handler.client.fish.FishingShopGetInfoHandler;
import com.gryphpoem.game.zw.handler.client.friend.AddBlackListHandler;
import com.gryphpoem.game.zw.handler.client.friend.AddFriendHandler;
import com.gryphpoem.game.zw.handler.client.friend.AddMasterHandler;
import com.gryphpoem.game.zw.handler.client.friend.AgreeRejectHandler;
import com.gryphpoem.game.zw.handler.client.friend.AgreeRejectMasterHandler;
import com.gryphpoem.game.zw.handler.client.friend.CheckFirendHandler;
import com.gryphpoem.game.zw.handler.client.friend.CreditExchangeHandler;
import com.gryphpoem.game.zw.handler.client.friend.DelBlackListHandler;
import com.gryphpoem.game.zw.handler.client.friend.DelFriendHandler;
import com.gryphpoem.game.zw.handler.client.friend.DelMasterApprenticeHandler;
import com.gryphpoem.game.zw.handler.client.friend.GetBlacklistHandler;
import com.gryphpoem.game.zw.handler.client.friend.GetFriendsHandler;
import com.gryphpoem.game.zw.handler.client.friend.GetMasterApprenticeHandler;
import com.gryphpoem.game.zw.handler.client.friend.GetRecommendLordHandler;
import com.gryphpoem.game.zw.handler.client.friend.MasterRewardHandler;
import com.gryphpoem.game.zw.handler.client.hero.ChooseWishedHeroHandler;
import com.gryphpoem.game.zw.handler.client.hero.DrawHeroCardHandler;
import com.gryphpoem.game.zw.handler.client.hero.ExchangeHeroFragmentHandler;
import com.gryphpoem.game.zw.handler.client.hero.GetAllHeroFragmentHandler;
import com.gryphpoem.game.zw.handler.client.hero.GetDrawHeroCardHandler;
import com.gryphpoem.game.zw.handler.client.hero.GetHeroBattlePosHandler;
import com.gryphpoem.game.zw.handler.client.hero.GetHeroBiographyInfoHandler;
import com.gryphpoem.game.zw.handler.client.hero.GetHeroByIdsHandler;
import com.gryphpoem.game.zw.handler.client.hero.GetHeroWashInfoHandler;
import com.gryphpoem.game.zw.handler.client.hero.GetHerosHandler;
import com.gryphpoem.game.zw.handler.client.hero.HeroBattleHandler;
import com.gryphpoem.game.zw.handler.client.hero.HeroDecoratedHandler;
import com.gryphpoem.game.zw.handler.client.hero.HeroPosSetHandler;
import com.gryphpoem.game.zw.handler.client.hero.HeroQuickUpHandler;
import com.gryphpoem.game.zw.handler.client.hero.HeroUpLvHandler;
import com.gryphpoem.game.zw.handler.client.hero.HeroUpgradeHandler;
import com.gryphpoem.game.zw.handler.client.hero.HeroWashHandler;
import com.gryphpoem.game.zw.handler.client.hero.ReceiveNewWishHeoHandler;
import com.gryphpoem.game.zw.handler.client.hero.SaveHeroWashHandler;
import com.gryphpoem.game.zw.handler.client.hero.StudyHeroTalentHandler;
import com.gryphpoem.game.zw.handler.client.hero.SynthesizingHeroFragmentsHandler;
import com.gryphpoem.game.zw.handler.client.hero.UpgradeHeroBiographyHandler;
import com.gryphpoem.game.zw.handler.client.hero.UpgradeHeroTalentHandler;
import com.gryphpoem.game.zw.handler.client.hero.function.BuyOptionalBoxFromTimeLimitedDrawCardHandler;
import com.gryphpoem.game.zw.handler.client.hero.function.DrawActHeroCardHandler;
import com.gryphpoem.game.zw.handler.client.hero.function.GetDrawHeroCardActInfoHandler;
import com.gryphpoem.game.zw.handler.client.hero.function.GetDrawHeroCardPlanListHandler;
import com.gryphpoem.game.zw.handler.client.hero.function.ReceiveTimeLimitedDrawCountHandler;
import com.gryphpoem.game.zw.handler.client.mail.DelMailHandler;
import com.gryphpoem.game.zw.handler.client.mail.GetMailByIdHandler;
import com.gryphpoem.game.zw.handler.client.mail.GetMailListHandler;
import com.gryphpoem.game.zw.handler.client.mail.GetShareMailHandler;
import com.gryphpoem.game.zw.handler.client.mail.LockMailHandler;
import com.gryphpoem.game.zw.handler.client.mail.ReadAllMailHandler;
import com.gryphpoem.game.zw.handler.client.mail.RewardMailHandler;
import com.gryphpoem.game.zw.handler.client.mail.SendCampMailHandler;
import com.gryphpoem.game.zw.handler.client.mail.SendMailHandler;
import com.gryphpoem.game.zw.handler.client.medal.BuyHonorHandler;
import com.gryphpoem.game.zw.handler.client.medal.BuyMedalHandler;
import com.gryphpoem.game.zw.handler.client.medal.DonateMedalHandler;
import com.gryphpoem.game.zw.handler.client.medal.GetHeroMedalHandler;
import com.gryphpoem.game.zw.handler.client.medal.GetHonorGoldBarHandler;
import com.gryphpoem.game.zw.handler.client.medal.GetMedalGoodsHandler;
import com.gryphpoem.game.zw.handler.client.medal.GetMedalsHandler;
import com.gryphpoem.game.zw.handler.client.medal.IntensifyMedalHandler;
import com.gryphpoem.game.zw.handler.client.medal.MedalLockHandler;
import com.gryphpoem.game.zw.handler.client.medal.UptHeroMedalHandler;
import com.gryphpoem.game.zw.handler.client.mentor.AutoWearEquipHandler;
import com.gryphpoem.game.zw.handler.client.mentor.GetMentorAwardHandler;
import com.gryphpoem.game.zw.handler.client.mentor.GetMentorsHandler;
import com.gryphpoem.game.zw.handler.client.mentor.GetSpecialEquipsHandler;
import com.gryphpoem.game.zw.handler.client.mentor.MentorActivateHandler;
import com.gryphpoem.game.zw.handler.client.mentor.MentorsQuickUpHandler;
import com.gryphpoem.game.zw.handler.client.mentor.MentorsSkillUpHandler;
import com.gryphpoem.game.zw.handler.client.mentor.SellMentorEquipHandler;
import com.gryphpoem.game.zw.handler.client.newcross.EnterLeaveNewCrossMapHandler;
import com.gryphpoem.game.zw.handler.client.newcross.GetCrossGroupInfoHandler;
import com.gryphpoem.game.zw.handler.client.newcross.GetCrossPlayerDataHandler;
import com.gryphpoem.game.zw.handler.client.newcross.GetNewCrossAreaHandler;
import com.gryphpoem.game.zw.handler.client.newcross.GetNewCrossArmyHandler;
import com.gryphpoem.game.zw.handler.client.newcross.GetNewCrossBattleHandler;
import com.gryphpoem.game.zw.handler.client.newcross.GetNewCrossCityInfoHandler;
import com.gryphpoem.game.zw.handler.client.newcross.GetNewCrossMapHandler;
import com.gryphpoem.game.zw.handler.client.newcross.GetNewCrossMarchHandler;
import com.gryphpoem.game.zw.handler.client.newcross.GetNewCrossMilitarySituationHandler;
import com.gryphpoem.game.zw.handler.client.newcross.GetNewCrossMineHandler;
import com.gryphpoem.game.zw.handler.client.newcross.NewCrossAccelerateArmyHandler;
import com.gryphpoem.game.zw.handler.client.newcross.NewCrossAttackPosHandler;
import com.gryphpoem.game.zw.handler.client.newcross.NewCrossMoveCityHandler;
import com.gryphpoem.game.zw.handler.client.newcross.RetreatNewCrossArmyHandler;
import com.gryphpoem.game.zw.handler.client.newcross.ScoutCrossPosHandler;
import com.gryphpoem.game.zw.handler.client.newcross.crosswarfire.BuyCrossWarFireBuffHandler;
import com.gryphpoem.game.zw.handler.client.newcross.crosswarfire.GetCrossWarFireCampSummaryHandler;
import com.gryphpoem.game.zw.handler.client.newcross.crosswarfire.GetCrossWarFireCityOccupyHandler;
import com.gryphpoem.game.zw.handler.client.newcross.crosswarfire.GetCrossWarFirePlayerLiveHandler;
import com.gryphpoem.game.zw.handler.client.newcross.crosswarfire.GetCrossWarFireRanksHandler;
import com.gryphpoem.game.zw.handler.client.newcross.crosswarfire.RefreshGetPlayerInfoHandler;
import com.gryphpoem.game.zw.handler.client.party.GetInvitesBattleHandler;
import com.gryphpoem.game.zw.handler.client.party.GetPartyBattleHandler;
import com.gryphpoem.game.zw.handler.client.party.GetPartyCityHandler;
import com.gryphpoem.game.zw.handler.client.party.GetPartyHandler;
import com.gryphpoem.game.zw.handler.client.party.GetPartyHonorHandler;
import com.gryphpoem.game.zw.handler.client.party.GetPartyJobHandler;
import com.gryphpoem.game.zw.handler.client.party.GetPartyLogHandler;
import com.gryphpoem.game.zw.handler.client.party.MakeInvitesHandler;
import com.gryphpoem.game.zw.handler.client.party.ModifySloganHandler;
import com.gryphpoem.game.zw.handler.client.party.PartyAppointHandler;
import com.gryphpoem.game.zw.handler.client.party.PartyBuildHandler;
import com.gryphpoem.game.zw.handler.client.party.PartyCanvassHandler;
import com.gryphpoem.game.zw.handler.client.party.PartyHonorRewardHandler;
import com.gryphpoem.game.zw.handler.client.party.PartyVoteHandler;
import com.gryphpoem.game.zw.handler.client.party.PromoteRanksHandler;
import com.gryphpoem.game.zw.handler.client.party.SupplyHallHandler;
import com.gryphpoem.game.zw.handler.client.party.SupplyRewardHandler;
import com.gryphpoem.game.zw.handler.client.plane.GetPlaneByIdsHandler;
import com.gryphpoem.game.zw.handler.client.plane.GetSearchAwardHandler;
import com.gryphpoem.game.zw.handler.client.plane.GetWarPlanesHandler;
import com.gryphpoem.game.zw.handler.client.plane.PlaneFactoryHandler;
import com.gryphpoem.game.zw.handler.client.plane.PlaneQuickUpHandler;
import com.gryphpoem.game.zw.handler.client.plane.PlaneRemouldHandler;
import com.gryphpoem.game.zw.handler.client.plane.PlaneSwapHandler;
import com.gryphpoem.game.zw.handler.client.plane.SearchPlaneHandler;
import com.gryphpoem.game.zw.handler.client.plane.SyntheticPlaneHandler;
import com.gryphpoem.game.zw.handler.client.player.OnHookGetAwardHandler;
import com.gryphpoem.game.zw.handler.client.player.OnHookGetInfoHandler;
import com.gryphpoem.game.zw.handler.client.player.OnHookOperateHandler;
import com.gryphpoem.game.zw.handler.client.player.OnHookReplenishHandler;
import com.gryphpoem.game.zw.handler.client.prop.BattleFailMessageHandler;
import com.gryphpoem.game.zw.handler.client.prop.BuyBuildHandler;
import com.gryphpoem.game.zw.handler.client.prop.BuyPropHandler;
import com.gryphpoem.game.zw.handler.client.prop.DecisiveBattleHandler;
import com.gryphpoem.game.zw.handler.client.prop.GainInstructionsHandler;
import com.gryphpoem.game.zw.handler.client.prop.GetPropsHandler;
import com.gryphpoem.game.zw.handler.client.prop.SyntheticPropHandler;
import com.gryphpoem.game.zw.handler.client.prop.UsePropHandler;
import com.gryphpoem.game.zw.handler.client.rebellion.BuyRebelBuffHandler;
import com.gryphpoem.game.zw.handler.client.rebellion.BuyRebelShopHandler;
import com.gryphpoem.game.zw.handler.client.rebellion.GetRebelBuffHandler;
import com.gryphpoem.game.zw.handler.client.rebellion.GetRebellionHandler;
import com.gryphpoem.game.zw.handler.client.relic.GetRelicDataInfoHandler;
import com.gryphpoem.game.zw.handler.client.relic.GetRelicDetailHandler;
import com.gryphpoem.game.zw.handler.client.relic.GetRelicScoreAwardHandler;
import com.gryphpoem.game.zw.handler.client.royalarena.GetRoyalArenaHandler;
import com.gryphpoem.game.zw.handler.client.royalarena.RoyalArenaAwardHandler;
import com.gryphpoem.game.zw.handler.client.royalarena.RoyalArenaSkillHandler;
import com.gryphpoem.game.zw.handler.client.royalarena.RoyalArenaTaskHandler;
import com.gryphpoem.game.zw.handler.client.sandtable.SandTableAdjustLineHandler;
import com.gryphpoem.game.zw.handler.client.sandtable.SandTableChangeLineHandler;
import com.gryphpoem.game.zw.handler.client.sandtable.SandTableEnrollHandler;
import com.gryphpoem.game.zw.handler.client.sandtable.SandTableGetInfoHandler;
import com.gryphpoem.game.zw.handler.client.sandtable.SandTableGetLinePlayersHandler;
import com.gryphpoem.game.zw.handler.client.sandtable.SandTableHisContestHandler;
import com.gryphpoem.game.zw.handler.client.sandtable.SandTableHisRankHandler;
import com.gryphpoem.game.zw.handler.client.sandtable.SandTablePlayerFightDetailHandler;
import com.gryphpoem.game.zw.handler.client.sandtable.SandTableReplayHandler;
import com.gryphpoem.game.zw.handler.client.sandtable.SandTableShopBuyHandler;
import com.gryphpoem.game.zw.handler.client.sandtable.SandTableUpdateArmyHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonChangeTalentHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonGenerateTreasuryAwardHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonGetInfoHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonGetRankHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonGetTalentHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonGetTaskAwardHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonGetTaskInfoHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonGetTreasuryAwardHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonGetTreasuryInfoHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonOpenTalentHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonStudyTalentHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonSynthHeroHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonTalentChooseClassifierHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonUpgradeHeroCgyHandler;
import com.gryphpoem.game.zw.handler.client.season.SeasonUpgradeHeroSkillHandler;
import com.gryphpoem.game.zw.handler.client.shop.BerlinShopBuyHandler;
import com.gryphpoem.game.zw.handler.client.shop.BuyActHandler;
import com.gryphpoem.game.zw.handler.client.shop.BuyMentorShopHandler;
import com.gryphpoem.game.zw.handler.client.shop.ChatBubbleBuyHandler;
import com.gryphpoem.game.zw.handler.client.shop.GetBerlinShopHandler;
import com.gryphpoem.game.zw.handler.client.shop.GetPaySerialIdHandler;
import com.gryphpoem.game.zw.handler.client.shop.GetShopHandler;
import com.gryphpoem.game.zw.handler.client.shop.ShopBuyHandler;
import com.gryphpoem.game.zw.handler.client.shop.VipBuyHandler;
import com.gryphpoem.game.zw.handler.client.signin.GetSignInInfoRqHandler;
import com.gryphpoem.game.zw.handler.client.signin.GetSignInRewardRqHandler;
import com.gryphpoem.game.zw.handler.client.simulator.RecordSimulatorHandler;
import com.gryphpoem.game.zw.handler.client.smallgame.DrawSmallGameAwardHandler;
import com.gryphpoem.game.zw.handler.client.smallgame.GetSmallGameHandler;
import com.gryphpoem.game.zw.handler.client.stone.DoStoneImproveHandler;
import com.gryphpoem.game.zw.handler.client.stone.GetStoneInfoHandler;
import com.gryphpoem.game.zw.handler.client.stone.StoneImproveUpLvHandler;
import com.gryphpoem.game.zw.handler.client.stone.StoneMountingHandler;
import com.gryphpoem.game.zw.handler.client.stone.StoneUpLvHandler;
import com.gryphpoem.game.zw.handler.client.task.AdvanceAwardHandler;
import com.gryphpoem.game.zw.handler.client.task.AtkWorldBossHandler;
import com.gryphpoem.game.zw.handler.client.task.DailyAwardHandler;
import com.gryphpoem.game.zw.handler.client.task.GainWorldTaskHandler;
import com.gryphpoem.game.zw.handler.client.task.GetAdvanceTaskHandler;
import com.gryphpoem.game.zw.handler.client.task.GetDailyTaskHandler;
import com.gryphpoem.game.zw.handler.client.task.GetPartyTaskHandler;
import com.gryphpoem.game.zw.handler.client.task.GetWorlTaskHandler;
import com.gryphpoem.game.zw.handler.client.task.LivenssAwardHandler;
import com.gryphpoem.game.zw.handler.client.task.PartyTaskAwardHandler;
import com.gryphpoem.game.zw.handler.client.task.ScorpionActivateRsHandler;
import com.gryphpoem.game.zw.handler.client.tech.GetTechHandler;
import com.gryphpoem.game.zw.handler.client.tech.TechAddHandler;
import com.gryphpoem.game.zw.handler.client.tech.TechFinishHandler;
import com.gryphpoem.game.zw.handler.client.tech.TechSpeedHandler;
import com.gryphpoem.game.zw.handler.client.tech.UpTechHandler;
import com.gryphpoem.game.zw.handler.client.totem.TotemBreakHandler;
import com.gryphpoem.game.zw.handler.client.totem.TotemDecomposeHandler;
import com.gryphpoem.game.zw.handler.client.totem.TotemLockHandler;
import com.gryphpoem.game.zw.handler.client.totem.TotemPutonHandler;
import com.gryphpoem.game.zw.handler.client.totem.TotemResonateHandler;
import com.gryphpoem.game.zw.handler.client.totem.TotemStrengthenHandler;
import com.gryphpoem.game.zw.handler.client.totem.TotemSyntheticHandler;
import com.gryphpoem.game.zw.handler.client.treasure.GetActTwJourneyInfoHandler;
import com.gryphpoem.game.zw.handler.client.treasure.GetTreasureHandler;
import com.gryphpoem.game.zw.handler.client.treasure.ReceiveActTwJourneyAwardHandler;
import com.gryphpoem.game.zw.handler.client.treasure.TreasureOpenHandler;
import com.gryphpoem.game.zw.handler.client.treasure.TreasureTradeHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.DoTreasureCombatHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.GetTreasureCombatHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.GetTreasureWaresHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.MakeTreasureWareHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.OnTreasureWareHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.StrengthenTreasureWareHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.TreasureChallengePlayerHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.TreasureChallengePurchaseHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.TreasureOnHookAwardHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.TreasureRefreshChallengeHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.TreasureSectionAwardHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.TreasureWareBagExpandHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.TreasureWareDecomposeHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.TreasureWareLockedHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.TreasureWareSaveTrainHandler;
import com.gryphpoem.game.zw.handler.client.treasureware.TreasureWareTrainHandler;
import com.gryphpoem.game.zw.handler.client.wall.FixWallHandler;
import com.gryphpoem.game.zw.handler.client.wall.GetWallHandler;
import com.gryphpoem.game.zw.handler.client.wall.WallCallBackHandler;
import com.gryphpoem.game.zw.handler.client.wall.WallGetOutHandler;
import com.gryphpoem.game.zw.handler.client.wall.WallHelpHandler;
import com.gryphpoem.game.zw.handler.client.wall.WallHelpInfoHandler;
import com.gryphpoem.game.zw.handler.client.wall.WallNpcArmyHandler;
import com.gryphpoem.game.zw.handler.client.wall.WallNpcAutoHandler;
import com.gryphpoem.game.zw.handler.client.wall.WallNpcFullHandler;
import com.gryphpoem.game.zw.handler.client.wall.WallNpcHandler;
import com.gryphpoem.game.zw.handler.client.wall.WallNpcLvUpHandler;
import com.gryphpoem.game.zw.handler.client.wall.WallSetHandler;
import com.gryphpoem.game.zw.handler.client.warfactory.AcqHeroSetHandler;
import com.gryphpoem.game.zw.handler.client.warfactory.CabinetFinishHandler;
import com.gryphpoem.game.zw.handler.client.warfactory.CabinetLvFinishHandler;
import com.gryphpoem.game.zw.handler.client.warfactory.CommandoHeroSetHandler;
import com.gryphpoem.game.zw.handler.client.warfactory.CreateLeadHandler;
import com.gryphpoem.game.zw.handler.client.warfactory.GetCabinetHandler;
import com.gryphpoem.game.zw.handler.client.world.AppointBerlinJobHandler;
import com.gryphpoem.game.zw.handler.client.world.AttackAirshipHandler;
import com.gryphpoem.game.zw.handler.client.world.AttackBerlinWarHandler;
import com.gryphpoem.game.zw.handler.client.world.AttackCounterBossHandler;
import com.gryphpoem.game.zw.handler.client.world.AttackDecisiveBattleHandler;
import com.gryphpoem.game.zw.handler.client.world.AttackGestapoHandler;
import com.gryphpoem.game.zw.handler.client.world.AttackPosHandler;
import com.gryphpoem.game.zw.handler.client.world.AttackRolesHandler;
import com.gryphpoem.game.zw.handler.client.world.AttackStateHandler;
import com.gryphpoem.game.zw.handler.client.world.AttackSuperMineHandler;
import com.gryphpoem.game.zw.handler.client.world.AttckScheduleBossHandler;
import com.gryphpoem.game.zw.handler.client.world.BerlinCityInfoHandler;
import com.gryphpoem.game.zw.handler.client.world.BerlinInfoHandler;
import com.gryphpoem.game.zw.handler.client.world.BerlinIntegralHandler;
import com.gryphpoem.game.zw.handler.client.world.BerlinRankInfoHandler;
import com.gryphpoem.game.zw.handler.client.world.BuyCounterAtkAwardHandler;
import com.gryphpoem.game.zw.handler.client.world.CityLevyHandler;
import com.gryphpoem.game.zw.handler.client.world.CityRebuildHandler;
import com.gryphpoem.game.zw.handler.client.world.CityRenameHandler;
import com.gryphpoem.game.zw.handler.client.world.CityRepairHandler;
import com.gryphpoem.game.zw.handler.client.world.ClearCDHandler;
import com.gryphpoem.game.zw.handler.client.world.EnterWorldHandler;
import com.gryphpoem.game.zw.handler.client.world.GainGoalAwardHandler;
import com.gryphpoem.game.zw.handler.client.world.GestapoKillCampRankHandler;
import com.gryphpoem.game.zw.handler.client.world.GetAirshipInfoHandler;
import com.gryphpoem.game.zw.handler.client.world.GetAirshipListHandler;
import com.gryphpoem.game.zw.handler.client.world.GetAllLightningWarListHandler;
import com.gryphpoem.game.zw.handler.client.world.GetAreaCentreCityHandler;
import com.gryphpoem.game.zw.handler.client.world.GetAreaHandler;
import com.gryphpoem.game.zw.handler.client.world.GetBattleByIdHandler;
import com.gryphpoem.game.zw.handler.client.world.GetBattleHandler;
import com.gryphpoem.game.zw.handler.client.world.GetBerlinJobHandler;
import com.gryphpoem.game.zw.handler.client.world.GetBerlinWinnerListHandler;
import com.gryphpoem.game.zw.handler.client.world.GetCampBattleHandler;
import com.gryphpoem.game.zw.handler.client.world.GetCampLevyHandler;
import com.gryphpoem.game.zw.handler.client.world.GetCityCampaignHandler;
import com.gryphpoem.game.zw.handler.client.world.GetCityFirstKillHandler;
import com.gryphpoem.game.zw.handler.client.world.GetCityHandler;
import com.gryphpoem.game.zw.handler.client.world.GetCounterAtkShopHandler;
import com.gryphpoem.game.zw.handler.client.world.GetCounterAttackHandler;
import com.gryphpoem.game.zw.handler.client.world.GetLightningWarHandler;
import com.gryphpoem.game.zw.handler.client.world.GetMapHandler;
import com.gryphpoem.game.zw.handler.client.world.GetMarchHandler;
import com.gryphpoem.game.zw.handler.client.world.GetMineHandler;
import com.gryphpoem.game.zw.handler.client.world.GetNightRaidInfoHandler;
import com.gryphpoem.game.zw.handler.client.world.GetScheduleBossHandler;
import com.gryphpoem.game.zw.handler.client.world.GetScheduleHandler;
import com.gryphpoem.game.zw.handler.client.world.GetSolarTermsHandler;
import com.gryphpoem.game.zw.handler.client.world.GetSummonHandler;
import com.gryphpoem.game.zw.handler.client.world.GetSuperMineHandler;
import com.gryphpoem.game.zw.handler.client.world.HonorReportsHandler;
import com.gryphpoem.game.zw.handler.client.world.ImmediatelyAttackHandler;
import com.gryphpoem.game.zw.handler.client.world.InitiateGatherEntranceHandler;
import com.gryphpoem.game.zw.handler.client.world.JoinBattleHandler;
import com.gryphpoem.game.zw.handler.client.world.JoinGestapoBattleHandler;
import com.gryphpoem.game.zw.handler.client.world.JoinLightningWarBattleHandler;
import com.gryphpoem.game.zw.handler.client.world.LeaveCityHandler;
import com.gryphpoem.game.zw.handler.client.world.MoveCDHandler;
import com.gryphpoem.game.zw.handler.client.world.MoveCityHandler;
import com.gryphpoem.game.zw.handler.client.world.PrewarBuffHandler;
import com.gryphpoem.game.zw.handler.client.world.RecentlyBerlinReportHandler;
import com.gryphpoem.game.zw.handler.client.world.ResumeImmediatelyHandler;
import com.gryphpoem.game.zw.handler.client.world.RetreatHandler;
import com.gryphpoem.game.zw.handler.client.world.ScoutPosHandler;
import com.gryphpoem.game.zw.handler.client.world.ScreenAreaFocusHandler;
import com.gryphpoem.game.zw.handler.client.world.SummonGestapoHandler;
import com.gryphpoem.game.zw.handler.client.world.SummonRespondHandler;
import com.gryphpoem.game.zw.handler.client.world.SummonTeamHandler;
import com.gryphpoem.game.zw.handler.client.world.UpCityHandler;
import com.gryphpoem.game.zw.handler.http.ForbiddenRqHandler;
import com.gryphpoem.game.zw.handler.http.GetLordBaseRqHandler;
import com.gryphpoem.game.zw.handler.http.GmHandler;
import com.gryphpoem.game.zw.handler.http.ModLordRqHandler;
import com.gryphpoem.game.zw.handler.http.ModNameRqHandler;
import com.gryphpoem.game.zw.handler.http.ModPropRqHandler;
import com.gryphpoem.game.zw.handler.http.ModVipRqHandler;
import com.gryphpoem.game.zw.handler.http.ModifyServerInfoHandler;
import com.gryphpoem.game.zw.handler.http.NoticeRqHandler;
import com.gryphpoem.game.zw.handler.http.PayApplyRsHandler;
import com.gryphpoem.game.zw.handler.http.PayBackRqHandler;
import com.gryphpoem.game.zw.handler.http.RegisterRsHandler;
import com.gryphpoem.game.zw.handler.http.ReloadParamRqHandler;
import com.gryphpoem.game.zw.handler.http.RobotsCountByAreaRqHandler;
import com.gryphpoem.game.zw.handler.http.RobotsExternalBehaviorRqHandler;
import com.gryphpoem.game.zw.handler.http.SendToMailRqHandler;
import com.gryphpoem.game.zw.handler.http.ShareRewardRsHandler;
import com.gryphpoem.game.zw.handler.http.UseGiftCodeRsHandler;
import com.gryphpoem.game.zw.handler.http.VerifyRsHandler;
import com.gryphpoem.game.zw.handler.http.WeChatSignRewardHandler;
import com.gryphpoem.game.zw.handler.inner.ClientRoutHandler;
import com.gryphpoem.game.zw.handler.inner.CrossAwardOpHandler;
import com.gryphpoem.game.zw.handler.inner.CrossLoginHandler;
import com.gryphpoem.game.zw.handler.inner.HeartHandler;
import com.gryphpoem.game.zw.handler.inner.SyncCrossWarFinishHandler;
import com.gryphpoem.game.zw.handler.inner.SyncFortHeroHandler;
import com.gryphpoem.game.zw.pb.CrossPb.CrossAwardOpRs;
import com.gryphpoem.game.zw.pb.CrossPb.CrossLoginRs;
import com.gryphpoem.game.zw.pb.CrossPb.HeartRs;
import com.gryphpoem.game.zw.pb.GamePb1.*;
import com.gryphpoem.game.zw.pb.GamePb2.AttackPosRq;
import com.gryphpoem.game.zw.pb.GamePb2.AttackPosRs;
import com.gryphpoem.game.zw.pb.GamePb2.AttackRolesRq;
import com.gryphpoem.game.zw.pb.GamePb2.AttackRolesRs;
import com.gryphpoem.game.zw.pb.GamePb2.AttackStateRq;
import com.gryphpoem.game.zw.pb.GamePb2.AttackStateRs;
import com.gryphpoem.game.zw.pb.GamePb2.BuyCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.BuyCombatRs;
import com.gryphpoem.game.zw.pb.GamePb2.BuyStoneCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.BuyStoneCombatRs;
import com.gryphpoem.game.zw.pb.GamePb2.CityLevyRq;
import com.gryphpoem.game.zw.pb.GamePb2.CityLevyRs;
import com.gryphpoem.game.zw.pb.GamePb2.CityRebuildRq;
import com.gryphpoem.game.zw.pb.GamePb2.CityRebuildRs;
import com.gryphpoem.game.zw.pb.GamePb2.CityRenameRq;
import com.gryphpoem.game.zw.pb.GamePb2.CityRenameRs;
import com.gryphpoem.game.zw.pb.GamePb2.CityRepairRq;
import com.gryphpoem.game.zw.pb.GamePb2.CityRepairRs;
import com.gryphpoem.game.zw.pb.GamePb2.ClearCDRq;
import com.gryphpoem.game.zw.pb.GamePb2.ClearCDRs;
import com.gryphpoem.game.zw.pb.GamePb2.CreateCombatTeamRq;
import com.gryphpoem.game.zw.pb.GamePb2.CreateCombatTeamRs;
import com.gryphpoem.game.zw.pb.GamePb2.DelMailRq;
import com.gryphpoem.game.zw.pb.GamePb2.DelMailRs;
import com.gryphpoem.game.zw.pb.GamePb2.DoCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.DoCombatRs;
import com.gryphpoem.game.zw.pb.GamePb2.DoCombatWipeRq;
import com.gryphpoem.game.zw.pb.GamePb2.DoCombatWipeRs;
import com.gryphpoem.game.zw.pb.GamePb2.DoPitchCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.DoPitchCombatRs;
import com.gryphpoem.game.zw.pb.GamePb2.DoStoneCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.DoStoneCombatRs;
import com.gryphpoem.game.zw.pb.GamePb2.EnterWorldRq;
import com.gryphpoem.game.zw.pb.GamePb2.EnterWorldRs;
import com.gryphpoem.game.zw.pb.GamePb2.EquipBatchDecomposeRq;
import com.gryphpoem.game.zw.pb.GamePb2.EquipBatchDecomposeRs;
import com.gryphpoem.game.zw.pb.GamePb2.EquipLockedRq;
import com.gryphpoem.game.zw.pb.GamePb2.EquipLockedRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetAreaRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetAreaRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetArmyRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetArmyRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetBattleRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetBattleRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetCampBattleRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetCampBattleRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetCampCityLevyRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetCampCityLevyRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetCityCampaignRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetCityCampaignRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetCityRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetCityRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetCombatRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetMailByIdRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetMailByIdRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetMailListRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetMailListRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetMapRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetMapRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetMarchRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetMarchRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetMineRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetMineRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetMultCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetMultCombatRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetPitchCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetPitchCombatRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetRankRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetRankRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetShareMailRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetShareMailRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetSolarTermsRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetSolarTermsRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetStoneCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetStoneCombatRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetSummonRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetSummonRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetTeamMemberListRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetTeamMemberListRs;
import com.gryphpoem.game.zw.pb.GamePb2.InitiateGatherEntranceRq;
import com.gryphpoem.game.zw.pb.GamePb2.InitiateGatherEntranceRs;
import com.gryphpoem.game.zw.pb.GamePb2.JoinBattleRq;
import com.gryphpoem.game.zw.pb.GamePb2.JoinBattleRs;
import com.gryphpoem.game.zw.pb.GamePb2.JoinCombatTeamRq;
import com.gryphpoem.game.zw.pb.GamePb2.JoinCombatTeamRs;
import com.gryphpoem.game.zw.pb.GamePb2.LeaveCityRq;
import com.gryphpoem.game.zw.pb.GamePb2.LeaveCityRs;
import com.gryphpoem.game.zw.pb.GamePb2.LeaveCombatTeamRq;
import com.gryphpoem.game.zw.pb.GamePb2.LeaveCombatTeamRs;
import com.gryphpoem.game.zw.pb.GamePb2.LockMailRq;
import com.gryphpoem.game.zw.pb.GamePb2.LockMailRs;
import com.gryphpoem.game.zw.pb.GamePb2.ModifyCombatTeamRq;
import com.gryphpoem.game.zw.pb.GamePb2.ModifyCombatTeamRs;
import com.gryphpoem.game.zw.pb.GamePb2.MoveCDRq;
import com.gryphpoem.game.zw.pb.GamePb2.MoveCDRs;
import com.gryphpoem.game.zw.pb.GamePb2.MoveCityRq;
import com.gryphpoem.game.zw.pb.GamePb2.MoveCityRs;
import com.gryphpoem.game.zw.pb.GamePb2.MultCombatShopBuyRq;
import com.gryphpoem.game.zw.pb.GamePb2.MultCombatShopBuyRs;
import com.gryphpoem.game.zw.pb.GamePb2.ReadAllMailRq;
import com.gryphpoem.game.zw.pb.GamePb2.ReadAllMailRs;
import com.gryphpoem.game.zw.pb.GamePb2.ReplenishRq;
import com.gryphpoem.game.zw.pb.GamePb2.ReplenishRs;
import com.gryphpoem.game.zw.pb.GamePb2.RetreatRq;
import com.gryphpoem.game.zw.pb.GamePb2.RetreatRs;
import com.gryphpoem.game.zw.pb.GamePb2.RewardMailRq;
import com.gryphpoem.game.zw.pb.GamePb2.RewardMailRs;
import com.gryphpoem.game.zw.pb.GamePb2.ScoutPosRq;
import com.gryphpoem.game.zw.pb.GamePb2.ScoutPosRs;
import com.gryphpoem.game.zw.pb.GamePb2.ScreenAreaFocusRq;
import com.gryphpoem.game.zw.pb.GamePb2.ScreenAreaFocusRs;
import com.gryphpoem.game.zw.pb.GamePb2.SendInvitationRq;
import com.gryphpoem.game.zw.pb.GamePb2.SendInvitationRs;
import com.gryphpoem.game.zw.pb.GamePb2.SendMailRq;
import com.gryphpoem.game.zw.pb.GamePb2.SendMailRs;
import com.gryphpoem.game.zw.pb.GamePb2.StartMultCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.StartMultCombatRs;
import com.gryphpoem.game.zw.pb.GamePb2.SummonRespondRq;
import com.gryphpoem.game.zw.pb.GamePb2.SummonRespondRs;
import com.gryphpoem.game.zw.pb.GamePb2.SummonTeamRq;
import com.gryphpoem.game.zw.pb.GamePb2.SummonTeamRs;
import com.gryphpoem.game.zw.pb.GamePb2.TickTeamMemberRq;
import com.gryphpoem.game.zw.pb.GamePb2.TickTeamMemberRs;
import com.gryphpoem.game.zw.pb.GamePb2.UpCityRq;
import com.gryphpoem.game.zw.pb.GamePb2.UpCityRs;
import com.gryphpoem.game.zw.pb.GamePb2.WipeMultCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.WipeMultCombatRs;
import com.gryphpoem.game.zw.pb.GamePb3.AcceptRedPacketRq;
import com.gryphpoem.game.zw.pb.GamePb3.AcceptRedPacketRs;
import com.gryphpoem.game.zw.pb.GamePb3.ActGrowBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.ActGrowBuyRs;
import com.gryphpoem.game.zw.pb.GamePb3.AdvanceAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.AdvanceAwardRs;
import com.gryphpoem.game.zw.pb.GamePb3.AtkWorldBossRq;
import com.gryphpoem.game.zw.pb.GamePb3.AtkWorldBossRs;
import com.gryphpoem.game.zw.pb.GamePb3.BerlinShopBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.BerlinShopBuyRs;
import com.gryphpoem.game.zw.pb.GamePb3.BlackhawkBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.BlackhawkBuyRs;
import com.gryphpoem.game.zw.pb.GamePb3.BlackhawkHeroRq;
import com.gryphpoem.game.zw.pb.GamePb3.BlackhawkHeroRs;
import com.gryphpoem.game.zw.pb.GamePb3.BlackhawkRefreshRq;
import com.gryphpoem.game.zw.pb.GamePb3.BlackhawkRefreshRs;
import com.gryphpoem.game.zw.pb.GamePb3.BuyActRq;
import com.gryphpoem.game.zw.pb.GamePb3.BuyActRs;
import com.gryphpoem.game.zw.pb.GamePb3.BuyMentorShopRq;
import com.gryphpoem.game.zw.pb.GamePb3.BuyMentorShopRs;
import com.gryphpoem.game.zw.pb.GamePb3.ChatBubbleBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.ChatBubbleBuyRs;
import com.gryphpoem.game.zw.pb.GamePb3.DailyAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.DailyAwardRs;
import com.gryphpoem.game.zw.pb.GamePb3.DelDialogRq;
import com.gryphpoem.game.zw.pb.GamePb3.DelDialogRs;
import com.gryphpoem.game.zw.pb.GamePb3.ExchangeActAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.ExchangeActAwardRs;
import com.gryphpoem.game.zw.pb.GamePb3.GainWorldTaskRq;
import com.gryphpoem.game.zw.pb.GamePb3.GainWorldTaskRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetActBlackhawkRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetActBlackhawkRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetActRankRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetActRankRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetActTurnplatRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetActTurnplatRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityAwardRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityChatRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityChatRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityListRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityListRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetAdvanceTaskRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetAdvanceTaskRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetAtkCityActRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetAtkCityActRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetBerlinShopRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetBerlinShopRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetChatRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetChatRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetDailyTaskRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetDailyTaskRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetDay7ActRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetDay7ActRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetDayDiscountsRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetDayDiscountsRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetDialogRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetDialogRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetDisplayActListRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetDisplayActListRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetFmsGelTunChatsRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetFmsGelTunChatsRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetFreePowerRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetFreePowerRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetInvitesBattleRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetInvitesBattleRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetOnLineAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetOnLineAwardRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyBattleRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyBattleRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyCityRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyCityRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyHonorRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyHonorRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyJobRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyJobRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyLogRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyLogRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyTaskRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyTaskRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetPaySerialIdRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPaySerialIdRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetPayTurnplateRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPayTurnplateRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetPowerGiveDataRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPowerGiveDataRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetPrivateChatRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPrivateChatRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetRedPacketListRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetRedPacketListRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetRedPacketRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetRedPacketRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetShopRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetShopRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetSupplyDorpRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetSupplyDorpRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetTriggerGiftRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetTriggerGiftRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetWorldTaskRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetWorldTaskRs;
import com.gryphpoem.game.zw.pb.GamePb3.GiftShowRq;
import com.gryphpoem.game.zw.pb.GamePb3.GiftShowRs;
import com.gryphpoem.game.zw.pb.GamePb3.LivenssAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.LivenssAwardRs;
import com.gryphpoem.game.zw.pb.GamePb3.LuckyTurnplateRq;
import com.gryphpoem.game.zw.pb.GamePb3.LuckyTurnplateRs;
import com.gryphpoem.game.zw.pb.GamePb3.MakeInvitesRq;
import com.gryphpoem.game.zw.pb.GamePb3.MakeInvitesRs;
import com.gryphpoem.game.zw.pb.GamePb3.ModifySloganRq;
import com.gryphpoem.game.zw.pb.GamePb3.ModifySloganRs;
import com.gryphpoem.game.zw.pb.GamePb3.PartyAppointRq;
import com.gryphpoem.game.zw.pb.GamePb3.PartyAppointRs;
import com.gryphpoem.game.zw.pb.GamePb3.PartyBuildRq;
import com.gryphpoem.game.zw.pb.GamePb3.PartyBuildRs;
import com.gryphpoem.game.zw.pb.GamePb3.PartyCanvassRq;
import com.gryphpoem.game.zw.pb.GamePb3.PartyCanvassRs;
import com.gryphpoem.game.zw.pb.GamePb3.PartyHonorRewardRq;
import com.gryphpoem.game.zw.pb.GamePb3.PartyHonorRewardRs;
import com.gryphpoem.game.zw.pb.GamePb3.PartyTaskAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.PartyTaskAwardRs;
import com.gryphpoem.game.zw.pb.GamePb3.PartyVoteRq;
import com.gryphpoem.game.zw.pb.GamePb3.PartyVoteRs;
import com.gryphpoem.game.zw.pb.GamePb3.PlayPayTurnplateRq;
import com.gryphpoem.game.zw.pb.GamePb3.PlayPayTurnplateRs;
import com.gryphpoem.game.zw.pb.GamePb3.PromoteRanksRq;
import com.gryphpoem.game.zw.pb.GamePb3.PromoteRanksRs;
import com.gryphpoem.game.zw.pb.GamePb3.PromotionPropBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.PromotionPropBuyRs;
import com.gryphpoem.game.zw.pb.GamePb3.ReadDialogRq;
import com.gryphpoem.game.zw.pb.GamePb3.ReadDialogRs;
import com.gryphpoem.game.zw.pb.GamePb3.RecvActiveRq;
import com.gryphpoem.game.zw.pb.GamePb3.RecvActiveRs;
import com.gryphpoem.game.zw.pb.GamePb3.RecvDay7ActAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.RecvDay7ActAwardRs;
import com.gryphpoem.game.zw.pb.GamePb3.SendCampMailRq;
import com.gryphpoem.game.zw.pb.GamePb3.SendCampMailRs;
import com.gryphpoem.game.zw.pb.GamePb3.SendChatRq;
import com.gryphpoem.game.zw.pb.GamePb3.SendChatRs;
import com.gryphpoem.game.zw.pb.GamePb3.ShareReportRq;
import com.gryphpoem.game.zw.pb.GamePb3.ShareReportRs;
import com.gryphpoem.game.zw.pb.GamePb3.ShopBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.ShopBuyRs;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyDorpAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyDorpAwardRs;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyDorpBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyDorpBuyRs;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyHallRq;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyHallRs;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyRewardRq;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyRewardRs;
import com.gryphpoem.game.zw.pb.GamePb3.TriggerGiftBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.TriggerGiftBuyRs;
import com.gryphpoem.game.zw.pb.GamePb3.TurnplatCntAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.TurnplatCntAwardRs;
import com.gryphpoem.game.zw.pb.GamePb3.VipBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.VipBuyRs;
import com.gryphpoem.game.zw.pb.GamePb4.*;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.pb.GamePb5.AttackCrossPosRq;
import com.gryphpoem.game.zw.pb.GamePb5.AttackCrossPosRs;
import com.gryphpoem.game.zw.pb.GamePb5.BuyCrossBuffRq;
import com.gryphpoem.game.zw.pb.GamePb5.BuyCrossBuffRs;
import com.gryphpoem.game.zw.pb.GamePb5.BuyOptionalBoxFromTimeLimitedDrawCardRq;
import com.gryphpoem.game.zw.pb.GamePb5.BuyOptionalBoxFromTimeLimitedDrawCardRs;
import com.gryphpoem.game.zw.pb.GamePb5.BuyWarFireBuffRq;
import com.gryphpoem.game.zw.pb.GamePb5.BuyWarFireBuffRs;
import com.gryphpoem.game.zw.pb.GamePb5.BuyWarFireShopRq;
import com.gryphpoem.game.zw.pb.GamePb5.BuyWarFireShopRs;
import com.gryphpoem.game.zw.pb.GamePb5.ChoiceHeroJoinRq;
import com.gryphpoem.game.zw.pb.GamePb5.ChoiceHeroJoinRs;
import com.gryphpoem.game.zw.pb.GamePb5.ChooseNewWishHeroRq;
import com.gryphpoem.game.zw.pb.GamePb5.ChooseNewWishHeroRs;
import com.gryphpoem.game.zw.pb.GamePb5.CrossMoveCityRq;
import com.gryphpoem.game.zw.pb.GamePb5.CrossMoveCityRs;
import com.gryphpoem.game.zw.pb.GamePb5.CrossTrophyAwardRq;
import com.gryphpoem.game.zw.pb.GamePb5.CrossTrophyAwardRs;
import com.gryphpoem.game.zw.pb.GamePb5.CrossTrophyInfoRq;
import com.gryphpoem.game.zw.pb.GamePb5.CrossTrophyInfoRs;
import com.gryphpoem.game.zw.pb.GamePb5.DrawActHeroCardRq;
import com.gryphpoem.game.zw.pb.GamePb5.DrawActHeroCardRs;
import com.gryphpoem.game.zw.pb.GamePb5.DrawHeroCardRq;
import com.gryphpoem.game.zw.pb.GamePb5.DrawHeroCardRs;
import com.gryphpoem.game.zw.pb.GamePb5.DrawSmallGameAwardRq;
import com.gryphpoem.game.zw.pb.GamePb5.DrawSmallGameAwardRs;
import com.gryphpoem.game.zw.pb.GamePb5.DrawTwTurntableAwardRq;
import com.gryphpoem.game.zw.pb.GamePb5.DrawTwTurntableAwardRs;
import com.gryphpoem.game.zw.pb.GamePb5.EnterCrossRq;
import com.gryphpoem.game.zw.pb.GamePb5.EnterCrossRs;
import com.gryphpoem.game.zw.pb.GamePb5.EnterLeaveCrossRq;
import com.gryphpoem.game.zw.pb.GamePb5.EnterLeaveCrossRs;
import com.gryphpoem.game.zw.pb.GamePb5.ExchangeHeroFragmentRs;
import com.gryphpoem.game.zw.pb.GamePb5.FireworkLetoffRq;
import com.gryphpoem.game.zw.pb.GamePb5.FireworkLetoffRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetActTwJourneyRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetActTwJourneyRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetAllHeroFragmentRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetAllHeroFragmentRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetChapterAwardRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetChapterAwardRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetChapterTaskAwardRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetChapterTaskAwardRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetChapterTaskRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetChapterTaskRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossAreaRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossAreaRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossArmyRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossArmyRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossBattleRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossBattleRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossChatRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossChatRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossCityInfoRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossCityInfoRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossFortRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossFortRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossInfoRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossInfoRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMapInfoRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMapInfoRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMapRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMapRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMarchRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMarchRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMineRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMineRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossRankRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossRankRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetDrawHeroCardActInfoRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetDrawHeroCardActInfoRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetDrawHeroCardPlanRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetDrawHeroCardPlanRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetDrawHeroCardRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetDrawHeroCardRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetHeroBiographyInfoRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetPlayerWarFireRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetPlayerWarFireRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetSmallGameRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetSmallGameRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetWarFireCampRankRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetWarFireCampRankRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetWarFireCampScoreRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetWarFireCampScoreRs;
import com.gryphpoem.game.zw.pb.GamePb5.GetWarFireCampSummaryRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetWarFireCampSummaryRs;
import com.gryphpoem.game.zw.pb.GamePb5.JoinBattleCrossRq;
import com.gryphpoem.game.zw.pb.GamePb5.JoinBattleCrossRs;
import com.gryphpoem.game.zw.pb.GamePb5.LongLightIgniteRq;
import com.gryphpoem.game.zw.pb.GamePb5.LongLightIgniteRs;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarAchievementRq;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarAchievementRs;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarInfoRq;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarInfoRs;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarPlayerRankDataRq;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarPlayerRankDataRs;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarProgressDataRq;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarProgressDataRs;
import com.gryphpoem.game.zw.pb.GamePb5.OpFortHeroRq;
import com.gryphpoem.game.zw.pb.GamePb5.OpFortHeroRs;
import com.gryphpoem.game.zw.pb.GamePb5.ReceiveActTwJourneyAwardRq;
import com.gryphpoem.game.zw.pb.GamePb5.ReceiveActTwJourneyAwardRs;
import com.gryphpoem.game.zw.pb.GamePb5.ReceiveMtwTurntableCntAwardRq;
import com.gryphpoem.game.zw.pb.GamePb5.ReceiveMtwTurntableCntAwardRs;
import com.gryphpoem.game.zw.pb.GamePb5.ReceiveNewWishHeoRq;
import com.gryphpoem.game.zw.pb.GamePb5.ReceiveNewWishHeoRs;
import com.gryphpoem.game.zw.pb.GamePb5.ReceiveTimeLimitedDrawCountRq;
import com.gryphpoem.game.zw.pb.GamePb5.ReceiveTimeLimitedDrawCountRs;
import com.gryphpoem.game.zw.pb.GamePb5.RetreatCrossRq;
import com.gryphpoem.game.zw.pb.GamePb5.RetreatCrossRs;
import com.gryphpoem.game.zw.pb.GamePb5.SearchBanditRq;
import com.gryphpoem.game.zw.pb.GamePb5.SearchBanditRs;
import com.gryphpoem.game.zw.pb.GamePb5.StudyHeroTalentRs;
import com.gryphpoem.game.zw.pb.GamePb5.SyncCrossWarFinishRs;
import com.gryphpoem.game.zw.pb.GamePb5.SyncFortHeroRs;
import com.gryphpoem.game.zw.pb.GamePb5.SynthesizingHeroFragmentsRs;
import com.gryphpoem.game.zw.pb.GamePb5.TotemBreakRq;
import com.gryphpoem.game.zw.pb.GamePb5.TotemBreakRs;
import com.gryphpoem.game.zw.pb.GamePb5.TotemDecomposeRq;
import com.gryphpoem.game.zw.pb.GamePb5.TotemDecomposeRs;
import com.gryphpoem.game.zw.pb.GamePb5.TotemLockRq;
import com.gryphpoem.game.zw.pb.GamePb5.TotemLockRs;
import com.gryphpoem.game.zw.pb.GamePb5.TotemPutonRq;
import com.gryphpoem.game.zw.pb.GamePb5.TotemPutonRs;
import com.gryphpoem.game.zw.pb.GamePb5.TotemResonateRq;
import com.gryphpoem.game.zw.pb.GamePb5.TotemResonateRs;
import com.gryphpoem.game.zw.pb.GamePb5.TotemStrengthenRq;
import com.gryphpoem.game.zw.pb.GamePb5.TotemStrengthenRs;
import com.gryphpoem.game.zw.pb.GamePb5.TotemSyntheticRq;
import com.gryphpoem.game.zw.pb.GamePb5.TotemSyntheticRs;
import com.gryphpoem.game.zw.pb.GamePb5.UpgradeHeroBiographyRs;
import com.gryphpoem.game.zw.pb.GamePb5.UpgradeHeroRs;
import com.gryphpoem.game.zw.pb.GamePb5.UpgradeHeroTalentRs;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarAwardRq;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarAwardRs;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarCampDateRq;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarCampDateRs;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarCampRankPlayersDateRq;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarCampRankPlayersDateRs;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarSeasonShopGoodsRq;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarSeasonShopGoodsRs;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarTaskDateRq;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarTaskDateRs;
import com.gryphpoem.game.zw.pb.GamePb5.YearFishBeginRq;
import com.gryphpoem.game.zw.pb.GamePb5.YearFishBeginRs;
import com.gryphpoem.game.zw.pb.GamePb5.YearFishEndRq;
import com.gryphpoem.game.zw.pb.GamePb5.YearFishEndRs;
import com.gryphpoem.game.zw.pb.GamePb5.YearFishShopExchangeRq;
import com.gryphpoem.game.zw.pb.GamePb5.YearFishShopExchangeRs;
import com.gryphpoem.game.zw.pb.GamePb6.BuyCrossWarFireBuffRq;
import com.gryphpoem.game.zw.pb.GamePb6.BuyCrossWarFireBuffRs;
import com.gryphpoem.game.zw.pb.GamePb6.CrossWarFireAttackPosRq;
import com.gryphpoem.game.zw.pb.GamePb6.CrossWarFireAttackPosRs;
import com.gryphpoem.game.zw.pb.GamePb6.CrossWarFireMoveCityRq;
import com.gryphpoem.game.zw.pb.GamePb6.CrossWarFireMoveCityRs;
import com.gryphpoem.game.zw.pb.GamePb6.EnterLeaveCrossWarFireRq;
import com.gryphpoem.game.zw.pb.GamePb6.EnterLeaveCrossWarFireRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossGroupInfoRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossGroupInfoRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossPlayerDataRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossPlayerDataRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossRechargeRankingRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossRechargeRankingRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireAreaRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireAreaRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireArmyRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireArmyRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireBattleRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireBattleRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireCampSummaryRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireCampSummaryRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireCityInfoRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireCityInfoRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireCityOccupyRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireCityOccupyRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireMapRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireMapRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireMarchRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireMarchRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireMilitarySituationRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireMilitarySituationRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFirePlayerLiveRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFirePlayerLiveRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireRanksRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireRanksRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetNewCrossMineRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetNewCrossMineRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetRelicDataInfoRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetRelicDataInfoRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetRelicDetailRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetRelicDetailRs;
import com.gryphpoem.game.zw.pb.GamePb6.GetRelicScoreAwardRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetRelicScoreAwardRs;
import com.gryphpoem.game.zw.pb.GamePb6.NewCrossAccelerateArmyRq;
import com.gryphpoem.game.zw.pb.GamePb6.NewCrossAccelerateArmyRs;
import com.gryphpoem.game.zw.pb.GamePb6.RefreshGetCrossWarFirePlayerInfoRq;
import com.gryphpoem.game.zw.pb.GamePb6.RefreshGetCrossWarFirePlayerInfoRs;
import com.gryphpoem.game.zw.pb.GamePb6.RetreatCrossWarFireRq;
import com.gryphpoem.game.zw.pb.GamePb6.RetreatCrossWarFireRs;
import com.gryphpoem.game.zw.pb.GamePb6.ScoutCrossPosRq;
import com.gryphpoem.game.zw.pb.GamePb6.ScoutCrossPosRs;
import com.gryphpoem.game.zw.pb.GamePb7.GetChatRoomMsgRq;
import com.gryphpoem.game.zw.pb.GamePb7.GetChatRoomMsgRs;
import com.gryphpoem.game.zw.pb.GamePb7.GetCrossPlayerShowRq;
import com.gryphpoem.game.zw.pb.GamePb7.GetCrossPlayerShowRs;
import com.gryphpoem.game.zw.pb.GamePb7.GetGamePlayChatRoomRq;
import com.gryphpoem.game.zw.pb.GamePb7.GetGamePlayChatRoomRs;
import com.gryphpoem.game.zw.pb.GamePb7.GetRoomPlayerShowRq;
import com.gryphpoem.game.zw.pb.GamePb7.GetRoomPlayerShowRs;
import com.gryphpoem.game.zw.pb.HttpPb;
import com.gryphpoem.game.zw.resource.constant.FunctionConstant;

public class MessagePool implements IMessagePool {
    // @formatter:off
    public MessagePool() {
        try {
            // ????????????????????????????????????
            clientMessagePool();

            // ???????????????????????????????????????
            httpMessagePool();

            // ?????????????????? ??????????????????
            innerMessagePool();

            // ??????????????????????????????
            functionUnlockMessagePool();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // @formatter:on

    /**
     * ?????????????????????????????????????????????
     */
    private void functionUnlockMessagePool() {
        // ????????????????????????
        registerFunctionUnlockInRange(GetAreaRq.EXT_FIELD_NUMBER, AttackStateRq.EXT_FIELD_NUMBER,
                FunctionConstant.FUNC_ID_WORLD);
        registerFunctionUnlockInRange(GetMarchRq.EXT_FIELD_NUMBER, LeaveCityRq.EXT_FIELD_NUMBER,
                FunctionConstant.FUNC_ID_WORLD);
    }

    /**
     * ????????????????????????????????????????????????handler
     */
    private void clientMessagePool() {
        // ??????????????????

        // ????????????????????????????????????????????????????????????
        registerC(BeginGameRq.EXT_FIELD_NUMBER, BeginGameRs.EXT_FIELD_NUMBER, BeginGameHandler.class);
        // ??????????????????
        registerC(GetNamesRq.EXT_FIELD_NUMBER, GetNamesRs.EXT_FIELD_NUMBER, GetNamesHandler.class);
        // ?????????????????????
        registerC(CreateRoleRq.EXT_FIELD_NUMBER, CreateRoleRs.EXT_FIELD_NUMBER, CreateRoleHandler.class);
        // ????????????????????????
        registerC(RoleLoginRq.EXT_FIELD_NUMBER, RoleLoginRs.EXT_FIELD_NUMBER, RoleLoginHandler.class);
        // ??????????????????
        registerC(GetLordRq.EXT_FIELD_NUMBER, GetLordRs.EXT_FIELD_NUMBER, GetLordHandler.class);
        // ???????????????????????????
        registerC(GetMixtureDataRq.EXT_FIELD_NUMBER, GetMixtureDataRs.EXT_FIELD_NUMBER, GetMixtureDataHandler.class);
        // ????????????
        registerC(SeachPlayerRq.EXT_FIELD_NUMBER, SeachPlayerRs.EXT_FIELD_NUMBER, SeachPlayerHandler.class);
        // ????????????????????????????????????
        registerC(SetGuideRq.EXT_FIELD_NUMBER, SetGuideRs.EXT_FIELD_NUMBER, SetGuideHandler.class);
        // ???????????????
        registerC(GiftCodeRq.EXT_FIELD_NUMBER, GiftCodeRs.EXT_FIELD_NUMBER, GiftCodeHandler.class);
        // ??????????????????
        registerC(OffLineIncomeRq.EXT_FIELD_NUMBER, OffLineIncomeRs.EXT_FIELD_NUMBER, GetOffLineIncome.class);
        // ????????????????????????
        registerC(JoinCommunityRq.EXT_FIELD_NUMBER, JoinCommunityRs.EXT_FIELD_NUMBER, JoinCommunityHandler.class);
        // ??????
        registerC(CompareNotesRq.EXT_FIELD_NUMBER, CompareNotesRs.EXT_FIELD_NUMBER, CompareNotesHandler.class);

        // ????????????
        registerC(ChangeLordNameRq.EXT_FIELD_NUMBER, ChangeLordNameRs.EXT_FIELD_NUMBER, ChangeLordNameHandler.class);
        // ????????????
        registerC(ChangePortraitRq.EXT_FIELD_NUMBER, ChangePortraitRs.EXT_FIELD_NUMBER, ChangePortraitHandler.class);
        // ??????????????????
        registerC(ChangeSignatureRq.EXT_FIELD_NUMBER, ChangeSignatureRs.EXT_FIELD_NUMBER, ChangeSignatureHandler.class);
        // ??????????????????
        registerC(GetPortraitRq.EXT_FIELD_NUMBER, GetPortraitRs.EXT_FIELD_NUMBER, GetPortraitHandler.class);
        // ????????????
        registerC(GetMonthCardRq.EXT_FIELD_NUMBER, GetMonthCardRs.EXT_FIELD_NUMBER, GetMonthCardHandler.class);
        // ???????????????????????????
//        registerC(GetChatBubbleRq.EXT_FIELD_NUMBER, GetChatBubbleRs.EXT_FIELD_NUMBER, GetChatBubbleHandler.class);
        // ?????????????????????
//        registerC(ChangeChatBubbleRq.EXT_FIELD_NUMBER, ChangeChatBubbleRs.EXT_FIELD_NUMBER,ChangeChatBubbleHandler.class);
        // ????????????
        registerC(GetBodyImageRq.EXT_FIELD_NUMBER, GetBodyImageRs.EXT_FIELD_NUMBER, GetBodyImageHandler.class);
        // ????????????
        registerC(ChangeBodyImageRq.EXT_FIELD_NUMBER, ChangeBodyImageRs.EXT_FIELD_NUMBER, ChangeBodyImageHandler.class);
        // ????????????????????????
        registerC(GetCastleSkinRq.EXT_FIELD_NUMBER, GetCastleSkinRs.EXT_FIELD_NUMBER, GetCastleSkinHandler.class);
        // ??????????????????
        registerC(ChangeCastleSkinRq.EXT_FIELD_NUMBER, ChangeCastleSkinRs.EXT_FIELD_NUMBER, ChangeCastleSkinHandler.class);
        //??????????????????
        registerC(CastleSkinStarUpRq.EXT_FIELD_NUMBER, CastleSkinStarUpRs.EXT_FIELD_NUMBER, CastleSkinStarUpHandler.class);

        // ??????????????????
        registerC(GetDressUpDataRq.EXT_FIELD_NUMBER, GetDressUpDataRs.EXT_FIELD_NUMBER, GetDressUpDataHandler.class);
        registerC(ChangeDressUpRq.EXT_FIELD_NUMBER, ChangeDressUpRs.EXT_FIELD_NUMBER, ChangeDressUpHandler.class);


        // ??????????????????

        // ?????????????????????
        registerC(GetTimeRq.EXT_FIELD_NUMBER, GetTimeRs.EXT_FIELD_NUMBER, GetTimeHandler.class);
        // ??????GM????????????????????????
        registerC(DoSomeRq.EXT_FIELD_NUMBER, DoSomeRs.EXT_FIELD_NUMBER, DoSomeHandler.class);

        // ??????????????????
        // ????????????
        registerC(GetBuildingRq.EXT_FIELD_NUMBER, GetBuildingRs.EXT_FIELD_NUMBER, BuildingHanlder.class);
        // ????????????
        registerC(UpBuildingRq.EXT_FIELD_NUMBER, UpBuildingRs.EXT_FIELD_NUMBER, UpBuildingHanlder.class);
        // ????????????
        registerC(UptBuildingRq.EXT_FIELD_NUMBER, UptBuildingRs.EXT_FIELD_NUMBER, UptBuildingHanlder.class);
        // ????????????
        registerC(SpeedBuildingRq.EXT_FIELD_NUMBER, SpeedBuildingRs.EXT_FIELD_NUMBER, SpeedBuildingHandler.class);
        // ????????????
        registerC(DesBuildingRq.EXT_FIELD_NUMBER, DesBuildingRs.EXT_FIELD_NUMBER, DesBuildingHanlder.class);
        // ????????????
        registerC(ReBuildRq.EXT_FIELD_NUMBER, ReBuildRs.EXT_FIELD_NUMBER, ReBuildHanlder.class);
        // ????????????
        registerC(GainResRq.EXT_FIELD_NUMBER, GainResRs.EXT_FIELD_NUMBER, GainResHandler.class);
        // ???????????????
        registerC(GetCommandRq.EXT_FIELD_NUMBER, GetCommandRs.EXT_FIELD_NUMBER, GetCommandHandler.class);
        // ???????????????
        registerC(CommandAddRq.EXT_FIELD_NUMBER, CommandAddRs.EXT_FIELD_NUMBER, CommandAddHandler.class);
        // ??????????????????
        registerC(AddArmRq.EXT_FIELD_NUMBER, AddArmRs.EXT_FIELD_NUMBER, AddArmHandler.class);
        // ????????????
        registerC(FactoryRecruitRq.EXT_FIELD_NUMBER, FactoryRecruitRs.EXT_FIELD_NUMBER, FactoryRecruitHandler.class);
        // ????????????
        registerC(GetFactoryRq.EXT_FIELD_NUMBER, GetFactoryRs.EXT_FIELD_NUMBER, GetFactoryHandler.class);
        // ??????????????????
        registerC(UpRecruitRq.EXT_FIELD_NUMBER, UpRecruitRs.EXT_FIELD_NUMBER, UpRecruitHandler.class);
        // ????????????
        registerC(FactoryExpandRq.EXT_FIELD_NUMBER, FactoryExpandRs.EXT_FIELD_NUMBER, FactoryExpandHandler.class);
        // ??????????????????
        registerC(RecruitCancelRq.EXT_FIELD_NUMBER, RecruitCancelRs.EXT_FIELD_NUMBER, RecruitCancelHandler.class);
        // ??????????????????
        registerC(RecruitSpeedRq.EXT_FIELD_NUMBER, RecruitSpeedRs.EXT_FIELD_NUMBER, RecruitSpeedHandler.class);
        // ???????????????
        registerC(GetEquipFactoryRq.EXT_FIELD_NUMBER, GetEquipFactoryRs.EXT_FIELD_NUMBER, GetEquipFactoryHandler.class);
        // ???????????????
        registerC(EquipFactoryRecruitRq.EXT_FIELD_NUMBER, EquipFactoryRecruitRs.EXT_FIELD_NUMBER,
                EquipFactoryRecruitHandler.class);
        // ????????????????????????
        registerC(SyncRoleRebuildRq.EXT_FIELD_NUMBER, SyncRoleRebuildRs.EXT_FIELD_NUMBER, SyncRoleRebuildHanlder.class);
        // ????????????????????????
        registerC(RebuildRewardRq.EXT_FIELD_NUMBER, RebuildRewardRs.EXT_FIELD_NUMBER, RebuildRewardHanlder.class);
        registerC(TrainingBuildingRq.EXT_FIELD_NUMBER, TrainingBuildingRs.EXT_FIELD_NUMBER,
                // ????????????(????????????????????????,???????????????????????????)
                TrainingBuildingHandler.class);

        registerC(OnOffAutoBuildRq.EXT_FIELD_NUMBER, OnOffAutoBuildRs.EXT_FIELD_NUMBER, OnOffAutoBuildHandler.class);

        registerC(QuickBuyArmyRq.EXT_FIELD_NUMBER, QuickBuyArmyRs.EXT_FIELD_NUMBER, QuickBuyArmyHandler.class);

        // ??????????????????

        // ??????
        registerC(GetWallRq.EXT_FIELD_NUMBER, GetWallRs.EXT_FIELD_NUMBER, GetWallHandler.class);
        // ????????????NPC
        registerC(WallNpcRq.EXT_FIELD_NUMBER, WallNpcRs.EXT_FIELD_NUMBER, WallNpcHandler.class);
        // ??????NPC??????
        registerC(WallNpcLvUpRq.EXT_FIELD_NUMBER, WallNpcLvUpRs.EXT_FIELD_NUMBER, WallNpcLvUpHandler.class);
        // ????????????
        registerC(WallSetRq.EXT_FIELD_NUMBER, WallSetRs.EXT_FIELD_NUMBER, WallSetHandler.class);
        // ??????????????????
        registerC(WallCallBackRq.EXT_FIELD_NUMBER, WallCallBackRs.EXT_FIELD_NUMBER, WallCallBackHandler.class);
        // ??????????????????
        registerC(WallGetOutRq.EXT_FIELD_NUMBER, WallGetOutRs.EXT_FIELD_NUMBER, WallGetOutHandler.class);
        // ??????NPC????????????
        registerC(WallNpcArmyRq.EXT_FIELD_NUMBER, WallNpcArmyRs.EXT_FIELD_NUMBER, WallNpcArmyHandler.class);
        // ????????????
        registerC(WallHelpRq.EXT_FIELD_NUMBER, WallHelpRs.EXT_FIELD_NUMBER, WallHelpHandler.class);
        // ??????????????????
        registerC(WallCallBackRq.EXT_FIELD_NUMBER, WallCallBackRs.EXT_FIELD_NUMBER, WallCallBackHandler.class);
        // ??????????????????
        registerC(WallGetOutRq.EXT_FIELD_NUMBER, WallGetOutRs.EXT_FIELD_NUMBER, WallGetOutHandler.class);
        // ??????NPC????????????
        registerC(WallNpcArmyRq.EXT_FIELD_NUMBER, WallNpcArmyRs.EXT_FIELD_NUMBER, WallNpcArmyHandler.class);
        // ??????NPC??????????????????
        registerC(WallNpcAutoRq.EXT_FIELD_NUMBER, WallNpcAutoRs.EXT_FIELD_NUMBER, WallNpcAutoHandler.class);
        // ??????????????????
        registerC(WallNpcFullRq.EXT_FIELD_NUMBER, WallNpcFullRs.EXT_FIELD_NUMBER, WallNpcFullHandler.class);
        // ??????????????????
        registerC(WallCallBackRq.EXT_FIELD_NUMBER, WallCallBackRs.EXT_FIELD_NUMBER, WallCallBackHandler.class);
        // ??????????????????
        registerC(WallGetOutRq.EXT_FIELD_NUMBER, WallGetOutRs.EXT_FIELD_NUMBER, WallGetOutHandler.class);
        // ??????NPC????????????
        registerC(WallNpcArmyRq.EXT_FIELD_NUMBER, WallNpcArmyRs.EXT_FIELD_NUMBER, WallNpcArmyHandler.class);
        // ??????NPC??????????????????
        registerC(WallNpcAutoRq.EXT_FIELD_NUMBER, WallNpcAutoRs.EXT_FIELD_NUMBER, WallNpcAutoHandler.class);
        // ??????????????????
        registerC(WallNpcFullRq.EXT_FIELD_NUMBER, WallNpcFullRs.EXT_FIELD_NUMBER, WallNpcFullHandler.class);
        // ??????????????????
        registerC(WallHelpInfoRq.EXT_FIELD_NUMBER, WallHelpInfoRs.EXT_FIELD_NUMBER, WallHelpInfoHandler.class);
        // ????????????
        registerC(FixWallRq.EXT_FIELD_NUMBER, FixWallRs.EXT_FIELD_NUMBER, FixWallHandler.class);

        // ????????????(??????)

        // ????????????????????????
        registerC(AcqHeroSetRq.EXT_FIELD_NUMBER, AcqHeroSetRs.EXT_FIELD_NUMBER, AcqHeroSetHandler.class);
        // ?????????????????????
        registerC(ComandoHeroSetRq.EXT_FIELD_NUMBER, ComandoHeroSetRs.EXT_FIELD_NUMBER, CommandoHeroSetHandler.class);
        // ???????????????????????????
        registerC(GetCabinetRq.EXT_FIELD_NUMBER, GetCabinetRs.EXT_FIELD_NUMBER, GetCabinetHandler.class);
        // ??????????????????
        registerC(CreateLeadRq.EXT_FIELD_NUMBER, CreateLeadRs.EXT_FIELD_NUMBER, CreateLeadHandler.class);
        // ????????????????????????
        registerC(CabinetFinishRq.EXT_FIELD_NUMBER, CabinetFinishRs.EXT_FIELD_NUMBER, CabinetFinishHandler.class);
        // ??????????????????????????????
        registerC(CabinetLvFinishRq.EXT_FIELD_NUMBER, CabinetLvFinishRs.EXT_FIELD_NUMBER, CabinetLvFinishHandler.class);

        // ?????????

        // ????????????
        registerC(UnlockAgentRq.EXT_FIELD_NUMBER, UnlockAgentRs.EXT_FIELD_NUMBER, UnlockAgentHandler.class);
        // ???????????????????????????
        registerC(GetCiaRq.EXT_FIELD_NUMBER, GetCiaRs.EXT_FIELD_NUMBER, GetCiaHandler.class);
        // ????????????
        registerC(InteractionRq.EXT_FIELD_NUMBER, InteractionRs.EXT_FIELD_NUMBER, InteractionHandler.class);
        // ????????????
        registerC(AgentUpgradeStarRq.EXT_FIELD_NUMBER, AgentUpgradeStarRs.EXT_FIELD_NUMBER, AgentUpgradeStarHandler.class);
        // ????????????
        registerC(PresentGiftRq.EXT_FIELD_NUMBER, PresentGiftRs.EXT_FIELD_NUMBER, PresentGiftHandler.class);
        // ????????????
        registerC(AppointmentAgentRq.EXT_FIELD_NUMBER, AppointmentAgentRs.EXT_FIELD_NUMBER, AppointmentHandler.class);

        // ??????

        // ??????????????????
        registerC(GetCombatRq.EXT_FIELD_NUMBER, GetCombatRs.EXT_FIELD_NUMBER, GetCombatHandler.class);
        // ????????????
        registerC(DoCombatRq.EXT_FIELD_NUMBER, DoCombatRs.EXT_FIELD_NUMBER, DoCombatHandler.class);
        // ????????????
        registerC(DoCombatWipeRq.EXT_FIELD_NUMBER, DoCombatWipeRs.EXT_FIELD_NUMBER, DoCombatWipeHandler.class);
        // ????????????????????????
        registerC(BuyCombatRq.EXT_FIELD_NUMBER, BuyCombatRs.EXT_FIELD_NUMBER, BuyCombatHandler.class);
        // ?????????
        registerC(GetRankRq.EXT_FIELD_NUMBER, GetRankRs.EXT_FIELD_NUMBER, GetRankHandler.class);
        // ?????????????????????
        registerC(GetMyRankRq.EXT_FIELD_NUMBER, GetMyRankRq.EXT_FIELD_NUMBER, GetMyRankHandler.class);
        // ????????????????????????
        registerC(BuyStoneCombatRq.EXT_FIELD_NUMBER, BuyStoneCombatRs.EXT_FIELD_NUMBER, BuyStoneCombatHandler.class);
        // ??????????????????
        registerC(DoStoneCombatRq.EXT_FIELD_NUMBER, DoStoneCombatRs.EXT_FIELD_NUMBER, DoStoneCombatHandler.class);
        // ??????????????????
        registerC(GetStoneCombatRq.EXT_FIELD_NUMBER, GetStoneCombatRs.EXT_FIELD_NUMBER, GetStoneCombatHandler.class);
        // ???????????????
        registerC(DoStoneImproveRq.EXT_FIELD_NUMBER, DoStoneImproveRs.EXT_FIELD_NUMBER, DoStoneImproveHandler.class);
        // ??????????????????
        registerC(StoneImproveUpLvRq.EXT_FIELD_NUMBER, StoneImproveUpLvRs.EXT_FIELD_NUMBER,
                StoneImproveUpLvHandler.class);
        // ???????????????????????????
        registerC(GetPitchCombatRq.EXT_FIELD_NUMBER, GetPitchCombatRs.EXT_FIELD_NUMBER, GetPitchCombatHandler.class);
        // ?????????????????????,?????????
        registerC(DoPitchCombatRq.EXT_FIELD_NUMBER, DoPitchCombatRs.EXT_FIELD_NUMBER, DoPitchCombatHandler.class);
        // ??????????????????
        registerC(CreateCombatTeamRq.EXT_FIELD_NUMBER, CreateCombatTeamRs.EXT_FIELD_NUMBER,
                CreateCombatTeamHandler.class);
        // ????????????????????????(????????????,????????????)
        registerC(ModifyCombatTeamRq.EXT_FIELD_NUMBER, ModifyCombatTeamRs.EXT_FIELD_NUMBER,
                ModifyCombatTeamHandler.class);
        // ??????????????????(??????????????????????????????);??????????????????
        registerC(JoinCombatTeamRq.EXT_FIELD_NUMBER, JoinCombatTeamRs.EXT_FIELD_NUMBER, JoinCombatTeamHandler.class);
        // ????????????
        registerC(LeaveCombatTeamRq.EXT_FIELD_NUMBER, LeaveCombatTeamRs.EXT_FIELD_NUMBER, LeaveCombatTeamHandler.class);
        // ????????????
        registerC(TickTeamMemberRq.EXT_FIELD_NUMBER, TickTeamMemberRs.EXT_FIELD_NUMBER, TickTeamMemberHandler.class);
        // ????????????????????????
        registerC(GetMultCombatRq.EXT_FIELD_NUMBER, GetMultCombatRs.EXT_FIELD_NUMBER, GetMultCombatHandler.class);
        // ????????????????????????
        registerC(GetTeamMemberListRq.EXT_FIELD_NUMBER, GetTeamMemberListRs.EXT_FIELD_NUMBER,
                GetTeamMemberListHandler.class);
        // ????????????
        registerC(SendInvitationRq.EXT_FIELD_NUMBER, SendInvitationRs.EXT_FIELD_NUMBER, SendInvitationHandler.class);
        // ??????????????????
        registerC(StartMultCombatRq.EXT_FIELD_NUMBER, StartMultCombatRs.EXT_FIELD_NUMBER, StartMultCombatHandler.class);
        // ????????????????????????
        registerC(MultCombatShopBuyRq.EXT_FIELD_NUMBER, MultCombatShopBuyRs.EXT_FIELD_NUMBER,
                MultCombatShopBuyHandler.class);
        // ??????????????????
        registerC(WipeMultCombatRq.EXT_FIELD_NUMBER, WipeMultCombatRs.EXT_FIELD_NUMBER, WipeMultCombatHandler.class);

        // ??????????????????

        // ??????????????????
        registerC(GetHerosRq.EXT_FIELD_NUMBER, GetHerosRs.EXT_FIELD_NUMBER, GetHerosHandler.class);
        // ????????????
        registerC(HeroBattleRq.EXT_FIELD_NUMBER, HeroBattleRs.EXT_FIELD_NUMBER, HeroBattleHandler.class);
        // ??????????????????
        registerC(HeroQuickUpRq.EXT_FIELD_NUMBER, HeroQuickUpRs.EXT_FIELD_NUMBER, HeroQuickUpHandler.class);
        // ??????????????????
        registerC(HeroQuickUpLvRq.EXT_FIELD_NUMBER, HeroQuickUpLvRs.EXT_FIELD_NUMBER, HeroUpLvHandler.class);
        // ????????????
        registerC(HeroWashRq.EXT_FIELD_NUMBER, HeroWashRs.EXT_FIELD_NUMBER, HeroWashHandler.class);
        // ??????????????????
        registerC(SaveHeroWashRq.EXT_FIELD_NUMBER, SaveHeroWashRs.EXT_FIELD_NUMBER, SaveHeroWashHandler.class);
        // ????????????
//        registerC(HeroBreakRq.EXT_FIELD_NUMBER, HeroBreakRs.EXT_FIELD_NUMBER, HeroBreakHandler.class);
        // ?????????????????????????????????
        registerC(GetHeroWashInfoRq.EXT_FIELD_NUMBER, GetHeroWashInfoRs.EXT_FIELD_NUMBER, GetHeroWashInfoHandler.class);
        // ???????????????
        registerC(HeroPosSetRq.EXT_FIELD_NUMBER, HeroPosSetRs.EXT_FIELD_NUMBER, HeroPosSetHandler.class);
        // ????????????
        registerC(AutoAddArmyRq.EXT_FIELD_NUMBER, AutoAddArmyRs.EXT_FIELD_NUMBER, AutoAddArmyHandler.class);
        // ????????????????????????
//        registerC(GetHeroSearchRq.EXT_FIELD_NUMBER, GetHeroSearchRs.EXT_FIELD_NUMBER, GetHeroSearchHandler.class);
        // ????????????
//        registerC(SearchHeroRq.EXT_FIELD_NUMBER, SearchHeroRs.EXT_FIELD_NUMBER, SearchHeroHandler.class);
        // ????????????????????????
        registerC(GetHeroByIdsRq.EXT_FIELD_NUMBER, GetHeroByIdsRs.EXT_FIELD_NUMBER, GetHeroByIdsHandler.class);
        // ??????????????????????????????????????????
        registerC(GetHeroBattlePosRq.EXT_FIELD_NUMBER, GetHeroBattlePosRs.EXT_FIELD_NUMBER,
                GetHeroBattlePosHandler.class);
        // ????????????
        registerC(HeroDecoratedRq.EXT_FIELD_NUMBER, HeroDecoratedRs.EXT_FIELD_NUMBER, HeroDecoratedHandler.class);
        // ????????????????????????
        registerC(GamePb5.StudyHeroTalentRq.EXT_FIELD_NUMBER, StudyHeroTalentRs.EXT_FIELD_NUMBER, StudyHeroTalentHandler.class);
        registerC(GamePb5.UpgradeHeroRq.EXT_FIELD_NUMBER, UpgradeHeroRs.EXT_FIELD_NUMBER, HeroUpgradeHandler.class);
        registerC(GamePb5.ExchangeHeroFragmentRq.EXT_FIELD_NUMBER, ExchangeHeroFragmentRs.EXT_FIELD_NUMBER, ExchangeHeroFragmentHandler.class);
        registerC(GamePb5.SynthesizingHeroFragmentsRq.EXT_FIELD_NUMBER, SynthesizingHeroFragmentsRs.EXT_FIELD_NUMBER, SynthesizingHeroFragmentsHandler.class);
        registerC(GamePb5.UpgradeHeroTalentRq.EXT_FIELD_NUMBER, UpgradeHeroTalentRs.EXT_FIELD_NUMBER, UpgradeHeroTalentHandler.class);

        // ??????????????????
        registerC(GetWarPlanesRq.EXT_FIELD_NUMBER, GetWarPlanesRs.EXT_FIELD_NUMBER, GetWarPlanesHandler.class);
        registerC(GetPlaneByIdsRq.EXT_FIELD_NUMBER, GetPlaneByIdsRs.EXT_FIELD_NUMBER, GetPlaneByIdsHandler.class);
        registerC(PlaneSwapRq.EXT_FIELD_NUMBER, PlaneSwapRs.EXT_FIELD_NUMBER, PlaneSwapHandler.class);
        registerC(PlaneRemouldRq.EXT_FIELD_NUMBER, PlaneRemouldRs.EXT_FIELD_NUMBER, PlaneRemouldHandler.class);
        registerC(PlaneFactoryRq.EXT_FIELD_NUMBER, PlaneFactoryRs.EXT_FIELD_NUMBER, PlaneFactoryHandler.class);
        registerC(SearchPlaneRq.EXT_FIELD_NUMBER, SearchPlaneRs.EXT_FIELD_NUMBER, SearchPlaneHandler.class);
        registerC(GetSearchAwardRq.EXT_FIELD_NUMBER, GetSearchAwardRs.EXT_FIELD_NUMBER, GetSearchAwardHandler.class);
        registerC(SyntheticPlaneRq.EXT_FIELD_NUMBER, SyntheticPlaneRs.EXT_FIELD_NUMBER, SyntheticPlaneHandler.class);
        registerC(PlaneQuickUpRq.EXT_FIELD_NUMBER, PlaneQuickUpRs.EXT_FIELD_NUMBER, PlaneQuickUpHandler.class);

        // ?????????????????????

        // ???????????????????????????
        registerC(GetPropsRq.EXT_FIELD_NUMBER, GetPropsRs.EXT_FIELD_NUMBER, GetPropsHandler.class);
        // ????????????????????????
        registerC(GetEquipsRq.EXT_FIELD_NUMBER, GetEquipsRs.EXT_FIELD_NUMBER, GetEquipsHandler.class);
        // ????????????
        registerC(EquipForgeRq.EXT_FIELD_NUMBER, EquipForgeRs.EXT_FIELD_NUMBER, EquipForgeHandler.class);
        // ??????????????????
        registerC(EquipGainRq.EXT_FIELD_NUMBER, EquipGainRs.EXT_FIELD_NUMBER, EquipGainHandler.class);
        // ??????????????????
        registerC(SpeedForgeRq.EXT_FIELD_NUMBER, SpeedForgeRs.EXT_FIELD_NUMBER, SpeedForgeHandler.class);
        // ????????????
        registerC(EquipBaptizeRq.EXT_FIELD_NUMBER, EquipBaptizeRs.EXT_FIELD_NUMBER, EquipBaptizeHandler.class);
        // ????????????
        registerC(EquipDecomposeRq.EXT_FIELD_NUMBER, EquipDecomposeRs.EXT_FIELD_NUMBER, EquipDecomposeHandler.class);
        // ????????????
        registerC(EquipLockedRq.EXT_FIELD_NUMBER, EquipLockedRs.EXT_FIELD_NUMBER, EquipLockedHandler.class);
        // ??????????????????
        registerC(EquipBatchDecomposeRq.EXT_FIELD_NUMBER, EquipBatchDecomposeRs.EXT_FIELD_NUMBER, EquipBatchDecomposeHandler.class);
        // ?????????????????????
        registerC(OnEquipRq.EXT_FIELD_NUMBER, OnEquipRs.EXT_FIELD_NUMBER, OnEquipHandler.class);
        // ????????????
        registerC(UsePropRq.EXT_FIELD_NUMBER, UsePropRs.EXT_FIELD_NUMBER, UsePropHandler.class);
        // ????????????
        registerC(BagExpandRq.EXT_FIELD_NUMBER, BagExpandRs.EXT_FIELD_NUMBER, BagExpandHandler.class);
        // ????????????
        registerC(GetSuperEquipRq.EXT_FIELD_NUMBER, GetSuperEquipRs.EXT_FIELD_NUMBER, GetSuperEquipHandler.class);
        // ????????????
        registerC(SuperEquipForgeRq.EXT_FIELD_NUMBER, SuperEquipForgeRs.EXT_FIELD_NUMBER, SuperEquipForgeHandler.class);
        // ????????????
        registerC(UpSuperEquipRq.EXT_FIELD_NUMBER, UpSuperEquipRs.EXT_FIELD_NUMBER, UpSuperEquipHandler.class);
        // ????????????
        registerC(SpeedSuperEquipRq.EXT_FIELD_NUMBER, SpeedSuperEquipRs.EXT_FIELD_NUMBER, SpeedSuperEquipHandler.class);
        // ????????????
        registerC(BuyPropRq.EXT_FIELD_NUMBER, BuyPropRs.EXT_FIELD_NUMBER, BuyPropHandler.class);
        // ????????????
        registerC(GrowSuperEquipRq.EXT_FIELD_NUMBER, GrowSuperEquipRs.EXT_FIELD_NUMBER, GrowSuperEquipHandler.class);
        // ??????????????????
        registerC(BuyBuildRq.EXT_FIELD_NUMBER, BuyBuildRs.EXT_FIELD_NUMBER, BuyBuildHandler.class);
        // ????????????
        registerC(RingUpLvRq.EXT_FIELD_NUMBER, RingUpLvRs.EXT_FIELD_NUMBER, RingUpLvHandler.class);
        // ??????????????????
        registerC(InlaidJewelRq.EXT_FIELD_NUMBER, InlaidJewelRs.EXT_FIELD_NUMBER, InlaidJewelHandler.class);
        // ????????????????????????
        registerC(GetJewelsRq.EXT_FIELD_NUMBER, GetJewelsRs.EXT_FIELD_NUMBER, GetJewelsHandler.class);
        // ??????????????????
        registerC(DoJewelImproveRq.EXT_FIELD_NUMBER, DoJewelImproveRs.EXT_FIELD_NUMBER, DoJewelImproveHandler.class);

        // ???????????????????????????
        registerC(GetStoneInfoRq.EXT_FIELD_NUMBER, GetStoneInfoRs.EXT_FIELD_NUMBER, GetStoneInfoHandler.class);
        // ????????????
        registerC(StoneMountingRq.EXT_FIELD_NUMBER, StoneMountingRs.EXT_FIELD_NUMBER, StoneMountingHandler.class);
        // ????????????
        registerC(StoneUpLvRq.EXT_FIELD_NUMBER, StoneUpLvRs.EXT_FIELD_NUMBER, StoneUpLvHandler.class);

        // ????????????
        registerC(SyntheticPropRq.EXT_FIELD_NUMBER, SyntheticPropRs.EXT_FIELD_NUMBER, SyntheticPropHandler.class);

        // ????????????
        // ????????????????????????
        registerC(GetMedalsRq.EXT_FIELD_NUMBER, GetMedalsRs.EXT_FIELD_NUMBER, GetMedalsHandler.class);
        // ???????????????????????????
        registerC(GetHeroMedalRq.EXT_FIELD_NUMBER, GetHeroMedalRs.EXT_FIELD_NUMBER, GetHeroMedalHandler.class);
        // ??????????????????
        registerC(MedalLockRq.EXT_FIELD_NUMBER, MedalLockRs.EXT_FIELD_NUMBER, MedalLockHandler.class);
        // ???????????????????????????
        registerC(UptHeroMedalRq.EXT_FIELD_NUMBER, UptHeroMedalRs.EXT_FIELD_NUMBER, UptHeroMedalHandler.class);
        // ?????????????????????????????????????????????
        registerC(GetHonorGoldBarRq.EXT_FIELD_NUMBER, GetHonorGoldBarRs.EXT_FIELD_NUMBER, GetHonorGoldBarHandler.class);
        // ???????????????????????????
        registerC(GetMedalGoodsRq.EXT_FIELD_NUMBER, GetMedalGoodsRs.EXT_FIELD_NUMBER, GetMedalGoodsHandler.class);
        // ????????????
        registerC(BuyMedalRq.EXT_FIELD_NUMBER, BuyMedalRs.EXT_FIELD_NUMBER, BuyMedalHandler.class);
        // ????????????
        registerC(IntensifyMedalRq.EXT_FIELD_NUMBER, IntensifyMedalRs.EXT_FIELD_NUMBER, IntensifyMedalHandler.class);
        // ????????????
        registerC(DonateMedalRq.EXT_FIELD_NUMBER, DonateMedalRs.EXT_FIELD_NUMBER, DonateMedalHandler.class);
        // ????????????
        registerC(BuyHonorRq.EXT_FIELD_NUMBER, BuyHonorRs.EXT_FIELD_NUMBER, BuyHonorHandler.class);

        // ????????????
        // ?????????????????????????????????
        registerC(GetAreaRq.EXT_FIELD_NUMBER, GetAreaRs.EXT_FIELD_NUMBER, GetAreaHandler.class);
        // ????????????????????????????????????
        registerC(GetMapRq.EXT_FIELD_NUMBER, GetMapRs.EXT_FIELD_NUMBER, GetMapHandler.class);
        // ???????????????????????????????????????????????????????????????
        registerC(AttackPosRq.EXT_FIELD_NUMBER, AttackPosRs.EXT_FIELD_NUMBER, AttackPosHandler.class);
        // ????????????
        registerC(RetreatRq.EXT_FIELD_NUMBER, RetreatRs.EXT_FIELD_NUMBER, RetreatHandler.class);
        // ???????????????????????????
        registerC(GetBattleRq.EXT_FIELD_NUMBER, GetBattleRs.EXT_FIELD_NUMBER, GetBattleHandler.class);
        // ????????????id??????????????????
        registerC(GetBattleByIdRq.EXT_FIELD_NUMBER, GetBattleByIdRs.EXT_FIELD_NUMBER, GetBattleByIdHandler.class);
        // ???????????????????????????????????????
        registerC(JoinBattleRq.EXT_FIELD_NUMBER, JoinBattleRs.EXT_FIELD_NUMBER, JoinBattleHandler.class);
        // ??????????????????????????????????????????
        registerC(AttackStateRq.EXT_FIELD_NUMBER, AttackStateRs.EXT_FIELD_NUMBER, AttackStateHandler.class);
        // ??????????????????????????????
        registerC(GetMarchRq.EXT_FIELD_NUMBER, GetMarchRs.EXT_FIELD_NUMBER, GetMarchHandler.class);
        // ????????????????????????
        registerC(GetMineRq.EXT_FIELD_NUMBER, GetMineRs.EXT_FIELD_NUMBER, GetMineHandler.class);
        // ????????????
        registerC(MoveCityRq.EXT_FIELD_NUMBER, MoveCityRs.EXT_FIELD_NUMBER, MoveCityHandler.class);
        // ?????????????????????
        registerC(GetCampBattleRq.EXT_FIELD_NUMBER, GetCampBattleRs.EXT_FIELD_NUMBER, GetCampBattleHandler.class);
        // ?????????????????????
        registerC(GetCampCityLevyRq.EXT_FIELD_NUMBER, GetCampCityLevyRs.EXT_FIELD_NUMBER, GetCampLevyHandler.class);
        // ??????
        registerC(ScoutPosRq.EXT_FIELD_NUMBER, ScoutPosRs.EXT_FIELD_NUMBER, ScoutPosHandler.class);
        // ??????CD??????
        registerC(ClearCDRq.EXT_FIELD_NUMBER, ClearCDRs.EXT_FIELD_NUMBER, ClearCDHandler.class);
        // ????????????
        registerC(CityLevyRq.EXT_FIELD_NUMBER, CityLevyRs.EXT_FIELD_NUMBER, CityLevyHandler.class);
        // ????????????
        registerC(CityRebuildRq.EXT_FIELD_NUMBER, CityRebuildRs.EXT_FIELD_NUMBER, CityRebuildHandler.class);
        // ????????????
        registerC(CityRepairRq.EXT_FIELD_NUMBER, CityRepairRs.EXT_FIELD_NUMBER, CityRepairHandler.class);
        // ??????????????????
        registerC(CityRenameRq.EXT_FIELD_NUMBER, CityRenameRs.EXT_FIELD_NUMBER, CityRenameHandler.class);
        // ????????????
        registerC(MoveCDRq.EXT_FIELD_NUMBER, MoveCDRs.EXT_FIELD_NUMBER, MoveCDHandler.class);
        // ??????????????????
        registerC(LeaveCityRq.EXT_FIELD_NUMBER, LeaveCityRs.EXT_FIELD_NUMBER, LeaveCityHandler.class);
        // ????????????
        registerC(UpCityRq.EXT_FIELD_NUMBER, UpCityRs.EXT_FIELD_NUMBER, UpCityHandler.class);
        // ????????????/????????????
        registerC(EnterWorldRq.EXT_FIELD_NUMBER, EnterWorldRs.EXT_FIELD_NUMBER, EnterWorldHandler.class);
        // ?????????????????????????????????
        registerC(ScreenAreaFocusRq.EXT_FIELD_NUMBER, ScreenAreaFocusRs.EXT_FIELD_NUMBER, ScreenAreaFocusHandler.class);
        // ????????????????????????
        registerC(AttackRolesRq.EXT_FIELD_NUMBER, AttackRolesRs.EXT_FIELD_NUMBER, AttackRolesHandler.class);
        // ????????????????????????
        registerC(GetCityCampaignRq.EXT_FIELD_NUMBER, GetCityCampaignRs.EXT_FIELD_NUMBER, GetCityCampaignHandler.class);
        // ??????????????????
        registerC(GetCityRq.EXT_FIELD_NUMBER, GetCityRs.EXT_FIELD_NUMBER, GetCityHandler.class);
        // ??????????????????
        registerC(GetSolarTermsRq.EXT_FIELD_NUMBER, GetSolarTermsRs.EXT_FIELD_NUMBER, GetSolarTermsHandler.class);
        // ????????????????????????
        registerC(GetNightRaidInfoRq.EXT_FIELD_NUMBER, GetNightRaidInfoRs.EXT_FIELD_NUMBER,
                GetNightRaidInfoHandler.class);
        // ??????????????????
        registerC(SummonGestapoRq.EXT_FIELD_NUMBER, SummonGestapoRs.EXT_FIELD_NUMBER, SummonGestapoHandler.class);
        // ????????????????????????
        registerC(AttackGestapoRq.EXT_FIELD_NUMBER, AttackGestapoRs.EXT_FIELD_NUMBER, AttackGestapoHandler.class);
        // ????????????????????????
        registerC(JoinGestapoBattleRq.EXT_FIELD_NUMBER, JoinGestapoBattleRs.EXT_FIELD_NUMBER,
                JoinGestapoBattleHandler.class);
        // ????????????????????????????????????
        registerC(GetAreaCentreCityRq.EXT_FIELD_NUMBER, GetAreaCentreCityRs.EXT_FIELD_NUMBER,
                GetAreaCentreCityHandler.class);
        // ??????????????????????????????
        registerC(GetCityFirstKillRq.EXT_FIELD_NUMBER, GetCityFirstKillRs.EXT_FIELD_NUMBER,
                GetCityFirstKillHandler.class);
        // ???????????????
        registerC(JoinLightningWarBattleRq.EXT_FIELD_NUMBER, JoinLightningWarBattleRs.EXT_FIELD_NUMBER,
                JoinLightningWarBattleHandler.class);
        // ????????????????????????????????????
        registerC(GetLightningWarRq.EXT_FIELD_NUMBER, GetLightningWarRs.EXT_FIELD_NUMBER, GetLightningWarHandler.class);
        // ???????????????????????????
        registerC(GetAllLightningWarListRq.EXT_FIELD_NUMBER, GetAllLightningWarListRs.EXT_FIELD_NUMBER,
                GetAllLightningWarListHandler.class);
        // ???????????????????????????????????????
        registerC(GestapoKillCampRankRq.EXT_FIELD_NUMBER, GestapoKillCampRankRs.EXT_FIELD_NUMBER,
                GestapoKillCampRankHandler.class);

        // ??????????????????
        registerC(GetHonorReportsRq.EXT_FIELD_NUMBER, GetHonorReportsRs.EXT_FIELD_NUMBER, HonorReportsHandler.class);

        // ??????????????????

        // ????????????????????????
        registerC(GetSuperMineRq.EXT_FIELD_NUMBER, GetSuperMineRs.EXT_FIELD_NUMBER, GetSuperMineHandler.class);
        // ??????????????????,??????,??????
        registerC(AttackSuperMineRq.EXT_FIELD_NUMBER, AttackSuperMineRs.EXT_FIELD_NUMBER, AttackSuperMineHandler.class);

        // ????????????

        // ??????????????????
        registerC(GetAirshipListRq.EXT_FIELD_NUMBER, GetAirshipListRs.EXT_FIELD_NUMBER, GetAirshipListHandler.class);
        // ????????????
        registerC(AttackAirshipRq.EXT_FIELD_NUMBER, AttackAirshipRs.EXT_FIELD_NUMBER, AttackAirshipHandler.class);
        // ????????????????????????
        registerC(GetAirshipInfoRq.EXT_FIELD_NUMBER, GetAirshipInfoRs.EXT_FIELD_NUMBER, GetAirshipInfoHandler.class);

        // ????????????

        // ???????????????????????????buff
        registerC(GetRebelBuffRq.EXT_FIELD_NUMBER, GetRebelBuffRs.EXT_FIELD_NUMBER, GetRebelBuffHandler.class);
        // ????????????????????????
        registerC(GetRebellionRq.EXT_FIELD_NUMBER, GetRebellionRs.EXT_FIELD_NUMBER, GetRebellionHandler.class);
        // ??????????????????buff
        registerC(BuyRebelBuffRq.EXT_FIELD_NUMBER, BuyRebelBuffRs.EXT_FIELD_NUMBER, BuyRebelBuffHandler.class);
        // ????????????????????????
        registerC(BuyRebelShopRq.EXT_FIELD_NUMBER, BuyRebelShopRs.EXT_FIELD_NUMBER, BuyRebelShopHandler.class);

        // ????????????

        // ??????????????????
        registerC(GetArmyRq.EXT_FIELD_NUMBER, GetArmyRs.EXT_FIELD_NUMBER, GetArmyHandler.class);
        // ??????
        registerC(ReplenishRq.EXT_FIELD_NUMBER, ReplenishRs.EXT_FIELD_NUMBER, ReplenishHandler.class);

        // ????????????
        // ??????????????????
        registerC(InitiateGatherEntranceRq.EXT_FIELD_NUMBER, InitiateGatherEntranceRs.EXT_FIELD_NUMBER, InitiateGatherEntranceHandler.class);
        // ????????????
        registerC(SummonTeamRq.EXT_FIELD_NUMBER, SummonTeamRs.EXT_FIELD_NUMBER, SummonTeamHandler.class);
        // ????????????
        registerC(SummonRespondRq.EXT_FIELD_NUMBER, SummonRespondRs.EXT_FIELD_NUMBER, SummonRespondHandler.class);
        // ??????????????????
        registerC(GetSummonRq.EXT_FIELD_NUMBER, GetSummonRs.EXT_FIELD_NUMBER, GetSummonHandler.class);

        // ????????????

        // ??????????????????
        registerC(GetMailListRq.EXT_FIELD_NUMBER, GetMailListRs.EXT_FIELD_NUMBER, GetMailListHandler.class);
        // ????????????id??????????????????
        registerC(GetMailByIdRq.EXT_FIELD_NUMBER, GetMailByIdRs.EXT_FIELD_NUMBER, GetMailByIdHandler.class);
        // ??????????????????
        registerC(RewardMailRq.EXT_FIELD_NUMBER, RewardMailRs.EXT_FIELD_NUMBER, RewardMailHandler.class);
        // ????????????????????????
        registerC(ReadAllMailRq.EXT_FIELD_NUMBER, ReadAllMailRs.EXT_FIELD_NUMBER, ReadAllMailHandler.class);
        // ????????????
        registerC(DelMailRq.EXT_FIELD_NUMBER, DelMailRs.EXT_FIELD_NUMBER, DelMailHandler.class);
        // ????????????
        registerC(SendMailRq.EXT_FIELD_NUMBER, SendMailRs.EXT_FIELD_NUMBER, SendMailHandler.class);
        // ?????????????????????????????????
        registerC(GetShareMailRq.EXT_FIELD_NUMBER, GetShareMailRs.EXT_FIELD_NUMBER, GetShareMailHandler.class);
        // ?????????????????????????????????
        registerC(LockMailRq.EXT_FIELD_NUMBER, LockMailRs.EXT_FIELD_NUMBER, LockMailHandler.class);

        // ??????????????????

        // ??????????????????
        registerC(SendCampMailRq.EXT_FIELD_NUMBER, SendCampMailRs.EXT_FIELD_NUMBER, SendCampMailHandler.class);

        // ????????????

        // ???????????????????????????
        registerC(GetChatRq.EXT_FIELD_NUMBER, GetChatRs.EXT_FIELD_NUMBER, GetChatHandler.class);
        // ????????????
        registerC(SendChatRq.EXT_FIELD_NUMBER, SendChatRs.EXT_FIELD_NUMBER, SendChatHandler.class);
        // ????????????
        registerC(ShareReportRq.EXT_FIELD_NUMBER, ShareReportRs.EXT_FIELD_NUMBER, ShareReportHandler.class);
        // ??????????????????
        registerC(GetPrivateChatRq.EXT_FIELD_NUMBER, GetPrivateChatRs.EXT_FIELD_NUMBER, GetPrivateChatHandler.class);
        // ????????????
        registerC(GetDialogRq.EXT_FIELD_NUMBER, GetDialogRs.EXT_FIELD_NUMBER, GetDialogHandler.class);
        // ????????????
        registerC(DelDialogRq.EXT_FIELD_NUMBER, DelDialogRs.EXT_FIELD_NUMBER, DelDialogHandler.class);
        // ????????????
        registerC(ReadDialogRq.EXT_FIELD_NUMBER, ReadDialogRs.EXT_FIELD_NUMBER, ReadDialogHandler.class);
        // ????????????
        registerC(AcceptRedPacketRq.EXT_FIELD_NUMBER, AcceptRedPacketRs.EXT_FIELD_NUMBER, AcceptRedPacketHandler.class);
        // ??????????????????
        registerC(GetRedPacketListRq.EXT_FIELD_NUMBER, GetRedPacketListRs.EXT_FIELD_NUMBER,
                GetRedPacketListRqHandler.class);
        // ??????????????????
        registerC(GetRedPacketRq.EXT_FIELD_NUMBER, GetRedPacketRs.EXT_FIELD_NUMBER, GetRedPacketHandler.class);
        // ???????????????????????????
        registerC(GetFmsGelTunChatsRq.EXT_FIELD_NUMBER, GetFmsGelTunChatsRs.EXT_FIELD_NUMBER,
                GetFmsGelTunChatsHandler.class);
        // ?????????????????????????????????
        registerC(GetActivityChatRq.EXT_FIELD_NUMBER, GetActivityChatRs.EXT_FIELD_NUMBER, GetActivityChatHandler.class);

        // ??????

        // ??????????????????
        registerC(GetPartyTaskRq.EXT_FIELD_NUMBER, GetPartyTaskRs.EXT_FIELD_NUMBER, GetPartyTaskHandler.class);
        // ??????????????????
        registerC(PartyTaskAwardRq.EXT_FIELD_NUMBER, PartyTaskAwardRs.EXT_FIELD_NUMBER, PartyTaskAwardHandler.class);
        // ??????????????????
        registerC(GetDay7ActRq.EXT_FIELD_NUMBER, GetDay7ActRs.EXT_FIELD_NUMBER, GetDay7ActHandler.class);
        // ????????????
        registerC(GetActivityListRq.EXT_FIELD_NUMBER, GetActivityListRs.EXT_FIELD_NUMBER, GetActivityListHandler.class);
        // ????????????????????????
        registerC(GetActivityRq.EXT_FIELD_NUMBER, GetActivityRs.EXT_FIELD_NUMBER, GetActivityHandler.class);
        // ????????????????????????
        registerC(RecvDay7ActAwardRq.EXT_FIELD_NUMBER, RecvDay7ActAwardRs.EXT_FIELD_NUMBER,
                RecvDay7ActAwardHandler.class);
        // ????????????????????????
        registerC(GetActivityAwardRq.EXT_FIELD_NUMBER, GetActivityAwardRs.EXT_FIELD_NUMBER,
                GetActivityAwardHandler.class);
        // ??????????????????
        registerC(ExchangeActAwardRq.EXT_FIELD_NUMBER, ExchangeActAwardRs.EXT_FIELD_NUMBER,
                ExchangeActAwardHandler.class);
        // ????????????????????????
        registerC(GetOnLineAwardRq.EXT_FIELD_NUMBER, GetOnLineAwardRs.EXT_FIELD_NUMBER, GetOnLineAwardHandler.class);
        // ?????????????????????
        registerC(GetTriggerGiftRq.EXT_FIELD_NUMBER, GetTriggerGiftRs.EXT_FIELD_NUMBER, GetTriggerGiftHandler.class);
        // ???????????????????????????
        registerC(TriggerGiftBuyRq.EXT_FIELD_NUMBER, TriggerGiftBuyRs.EXT_FIELD_NUMBER, TriggerGIftBuyHandler.class);
        // ??????????????????????????????
        registerC(PromotionPropBuyRq.EXT_FIELD_NUMBER, PromotionPropBuyRs.EXT_FIELD_NUMBER,
                PromotionGiftBuyHandler.class);
        // ??????????????????
        registerC(GetWorldTaskRq.EXT_FIELD_NUMBER, GetWorldTaskRs.EXT_FIELD_NUMBER, GetWorlTaskHandler.class);
        // ????????????????????????
        registerC(GainWorldTaskRq.EXT_FIELD_NUMBER, GainWorldTaskRs.EXT_FIELD_NUMBER, GainWorldTaskHandler.class);
        // ?????????boss
        registerC(AtkWorldBossRq.EXT_FIELD_NUMBER, AtkWorldBossRs.EXT_FIELD_NUMBER, AtkWorldBossHandler.class);
        // ????????????????????????
        registerC(GetActBlackhawkRq.EXT_FIELD_NUMBER, GetActBlackhawkRs.EXT_FIELD_NUMBER, GetActBlackhawkHandler.class);
        // ????????????????????????
        registerC(BlackhawkBuyRq.EXT_FIELD_NUMBER, BlackhawkBuyRs.EXT_FIELD_NUMBER, BlackhawkBuyHandler.class);
        // ??????????????????
        registerC(BlackhawkRefreshRq.EXT_FIELD_NUMBER, BlackhawkRefreshRs.EXT_FIELD_NUMBER,
                BlackhawkRefreshHandler.class);
        // ????????????
        registerC(BlackhawkHeroRq.EXT_FIELD_NUMBER, BlackhawkHeroRs.EXT_FIELD_NUMBER, BlackhawkHeroHandler.class);
        // ????????????
        // ??????????????????
        registerC(SupplyDorpBuyRq.EXT_FIELD_NUMBER, SupplyDorpBuyRs.EXT_FIELD_NUMBER, SupplyDorpBuyHandler.class);
        // ??????????????????
        registerC(GetSupplyDorpRq.EXT_FIELD_NUMBER, GetSupplyDorpRs.EXT_FIELD_NUMBER, GetSupplyDorpHandler.class);
        // ????????????????????????
        registerC(SupplyDorpAwardRq.EXT_FIELD_NUMBER, SupplyDorpAwardRs.EXT_FIELD_NUMBER, SupplyDorpAwardHandler.class);
        // ??????????????????
        registerC(GetPowerGiveDataRq.EXT_FIELD_NUMBER, GetPowerGiveDataRs.EXT_FIELD_NUMBER,
                GetPowerGiveDataHandler.class);
        // ??????????????????
        registerC(GetFreePowerRq.EXT_FIELD_NUMBER, GetFreePowerRs.EXT_FIELD_NUMBER, GetFreePowerHandler.class);
        // ??????????????????
        registerC(ActGrowBuyRq.EXT_FIELD_NUMBER, ActGrowBuyRs.EXT_FIELD_NUMBER, ActGrowBuyHandler.class);
        // ????????????
        registerC(GiftShowRq.EXT_FIELD_NUMBER, GiftShowRs.EXT_FIELD_NUMBER, GiftShowHandler.class);
        // ??????????????????
        registerC(GetActRankRq.EXT_FIELD_NUMBER, GetActRankRs.EXT_FIELD_NUMBER, GetActRankHandler.class);
        // ??????????????????
        registerC(GetDisplayActListRq.EXT_FIELD_NUMBER, GetDisplayActListRs.EXT_FIELD_NUMBER,
                GetDisplayActListHandler.class);
        // ??????????????????????????????
        registerC(GetAtkCityActRq.EXT_FIELD_NUMBER, GetAtkCityActRs.EXT_FIELD_NUMBER, GetAtkCityActHandler.class);
        // ??????????????????????????????
        registerC(RecvActiveRq.EXT_FIELD_NUMBER, RecvActiveRs.EXT_FIELD_NUMBER, RecvActiveHandler.class);
        // ??????????????????
        registerC(GetDailyTaskRq.EXT_FIELD_NUMBER, GetDailyTaskRs.EXT_FIELD_NUMBER, GetDailyTaskHandler.class);
        // ?????????????????????
        registerC(DailyAwardRq.EXT_FIELD_NUMBER, DailyAwardRs.EXT_FIELD_NUMBER, DailyAwardHandler.class);
        // ???????????????????????????
        registerC(LivenssAwardRq.EXT_FIELD_NUMBER, LivenssAwardRs.EXT_FIELD_NUMBER, LivenssAwardHandler.class);
        // ??????????????????
        registerC(LuckyTurnplateRq.EXT_FIELD_NUMBER, LuckyTurnplateRs.EXT_FIELD_NUMBER, LuckyTurnplateHandler.class);
        // ??????????????????
        registerC(TurnplatCntAwardRq.EXT_FIELD_NUMBER, TurnplatCntAwardRs.EXT_FIELD_NUMBER, TurnplatCntAwardHandler.class);
        // ???????????????????????????
        registerC(GetActTurnplatRq.EXT_FIELD_NUMBER, GetActTurnplatRs.EXT_FIELD_NUMBER, GetActTurnplatHandler.class);
        // ??????????????????
        registerC(EquipTurnplateRq.EXT_FIELD_NUMBER, EquipTurnplateRs.EXT_FIELD_NUMBER, EquipTurnplateHandler.class);
        // ???????????????????????????
        registerC(GetEquipTurnplatRq.EXT_FIELD_NUMBER, GetEquipTurnplatRs.EXT_FIELD_NUMBER,
                GetEquipTurnplatHandler.class);
        // ??????????????????
        registerC(GetPayTurnplateRq.EXT_FIELD_NUMBER, GetPayTurnplateRs.EXT_FIELD_NUMBER, GetPayTurnplateHandler.class);
        // ??????????????????
        registerC(PlayPayTurnplateRq.EXT_FIELD_NUMBER, PlayPayTurnplateRs.EXT_FIELD_NUMBER,
                PlayPayTurnplateHandler.class);
        // ??????????????????
        registerC(GetOreTurnplateRq.EXT_FIELD_NUMBER, GetOreTurnplateRs.EXT_FIELD_NUMBER, GetOreTurnplateHandler.class);
        // ??????????????????
        registerC(PlayOreTurnplateRq.EXT_FIELD_NUMBER, PlayOreTurnplateRs.EXT_FIELD_NUMBER,
                PlayOreTurnplateHandler.class);
        // ??????????????????-???
        registerC(GetOreTurnplateNewRq.EXT_FIELD_NUMBER, GetOreTurnplateNewRs.EXT_FIELD_NUMBER,
                GetOreTurnplateNewHandler.class);
        // ??????????????????-???
        registerC(PlayOreTurnplateNewRq.EXT_FIELD_NUMBER, PlayOreTurnplateNewRs.EXT_FIELD_NUMBER,
                PlayOreTurnplateNewHandler.class);
        // ????????????
        registerC(GetDayDiscountsRq.EXT_FIELD_NUMBER, GetDayDiscountsRs.EXT_FIELD_NUMBER, GetDayDiscountsHandler.class);
        // ???????????????
        registerC(GetMonopolyRq.EXT_FIELD_NUMBER, GetMonopolyRs.EXT_FIELD_NUMBER, GetMonopolyHandler.class);
        // ??????????????????
        registerC(PlayMonopolyRq.EXT_FIELD_NUMBER, PlayMonopolyRs.EXT_FIELD_NUMBER, PlayMonopolyHandler.class);
        // ??????????????????
        registerC(ThreeRebateRq.EXT_FIELD_NUMBER, ThreeRebateRs.EXT_FIELD_NUMBER, ThreeRebateHandler.class);
        // ???????????????
        registerC(WishingRq.EXT_FIELD_NUMBER, WishingRs.EXT_FIELD_NUMBER, WishingHandler.class);
        // ??????????????????
        registerC(GetSpecialActRq.EXT_FIELD_NUMBER, GetSpecialActRs.EXT_FIELD_NUMBER, GetSpecialActHandler.class);
        // ???????????????
        registerC(GetEasterActAwardRq.EXT_FIELD_NUMBER, GetEasterActAwardRs.EXT_FIELD_NUMBER, GetEasterActAwardHandler.class);
        // ???????????????????????????
        registerC(AdvertisementRewardRq.EXT_FIELD_NUMBER, AdvertisementRewardRs.EXT_FIELD_NUMBER, AdvertisementRewardHandler.class);
        // ??????????????????
        registerC(GetAdvanceTaskRq.EXT_FIELD_NUMBER, GetAdvanceTaskRs.EXT_FIELD_NUMBER, GetAdvanceTaskHandler.class);
        // ??????????????????
        registerC(AdvanceAwardRq.EXT_FIELD_NUMBER, AdvanceAwardRs.EXT_FIELD_NUMBER, AdvanceAwardHandler.class);
        // ???????????????????????????
        registerC(ActHotProductAwardRq.EXT_FIELD_NUMBER, ActHotProductAwardRs.EXT_FIELD_NUMBER, ActHotProductAwardHandler.class);
        // ?????????????????????
        registerC(ActGoodLuckAwardRq.EXT_FIELD_NUMBER, ActGoodLuckAwardRs.EXT_FIELD_NUMBER, ActGoodLuckAwardHandler.class);

        // ?????????
        // ???????????????
        registerC(GetTechRq.EXT_FIELD_NUMBER, GetTechRs.EXT_FIELD_NUMBER, GetTechHandler.class);
        // ???????????????
        registerC(TechAddRq.EXT_FIELD_NUMBER, TechAddRs.EXT_FIELD_NUMBER, TechAddHandler.class);
        // ???????????????
        registerC(UpTechRq.EXT_FIELD_NUMBER, UpTechRs.EXT_FIELD_NUMBER, UpTechHandler.class);
        // ???????????????
        registerC(TechSpeedRq.EXT_FIELD_NUMBER, TechSpeedRs.EXT_FIELD_NUMBER, TechSpeedHandler.class);
        // ???????????????????????????
        registerC(TechFinishRq.EXT_FIELD_NUMBER, TechFinishRs.EXT_FIELD_NUMBER, TechFinishHandler.class);

        // ?????????
        // ???????????????
        registerC(GetChemicalRq.EXT_FIELD_NUMBER, GetChemicalRs.EXT_FIELD_NUMBER, GetChemicalHandler.class);
        // ???????????????
        registerC(ChemicalRecruitRq.EXT_FIELD_NUMBER, ChemicalRecruitRs.EXT_FIELD_NUMBER, ChemicalRecruitHandler.class);
        // ???????????????
        registerC(ChemicalExpandRq.EXT_FIELD_NUMBER, ChemicalExpandRs.EXT_FIELD_NUMBER, ChemicalExpandHandler.class);
        // ?????????????????????
        registerC(ChemicalFinishRq.EXT_FIELD_NUMBER, ChemicalFinishRs.EXT_FIELD_NUMBER, ChemicalFinishHandler.class);

        // ?????????
        // ???????????????
        registerC(GetTreasureRq.EXT_FIELD_NUMBER, GetTreasureRs.EXT_FIELD_NUMBER, GetTreasureHandler.class);
        // ???????????????
        registerC(TreasureOpenRq.EXT_FIELD_NUMBER, TreasureOpenRs.EXT_FIELD_NUMBER, TreasureOpenHandler.class);
        // ???????????????
        registerC(TreasureTradeRq.EXT_FIELD_NUMBER, TreasureTradeRs.EXT_FIELD_NUMBER, TreasureTradeHandler.class);

        // ??????
        // ????????????
        registerC(GetShopRq.EXT_FIELD_NUMBER, GetShopRs.EXT_FIELD_NUMBER, GetShopHandler.class);
        // ????????????
        registerC(ShopBuyRq.EXT_FIELD_NUMBER, ShopBuyRs.EXT_FIELD_NUMBER, ShopBuyHandler.class);
        // VIP????????????
        registerC(VipBuyRq.EXT_FIELD_NUMBER, VipBuyRs.EXT_FIELD_NUMBER, VipBuyHandler.class);
        // ?????????????????????
        registerC(GetPaySerialIdRq.EXT_FIELD_NUMBER, GetPaySerialIdRs.EXT_FIELD_NUMBER, GetPaySerialIdHandler.class);
        // ????????????
        registerC(BuyActRq.EXT_FIELD_NUMBER, BuyActRs.EXT_FIELD_NUMBER, BuyActHandler.class);
        // ??????????????????
        registerC(BerlinShopBuyRq.EXT_FIELD_NUMBER, BerlinShopBuyRs.EXT_FIELD_NUMBER, BerlinShopBuyHandler.class);
        // ????????????????????????
        registerC(GetBerlinShopRq.EXT_FIELD_NUMBER, GetBerlinShopRs.EXT_FIELD_NUMBER, GetBerlinShopHandler.class);
        // ??????????????????
        registerC(ChatBubbleBuyRq.EXT_FIELD_NUMBER, ChatBubbleBuyRs.EXT_FIELD_NUMBER, ChatBubbleBuyHandler.class);
        // ?????????????????????????????????
        registerC(BuyMentorShopRq.EXT_FIELD_NUMBER, BuyMentorShopRs.EXT_FIELD_NUMBER, BuyMentorShopHandler.class);

        // ??????
        // ??????????????????
        registerC(GetPartyRq.EXT_FIELD_NUMBER, GetPartyRs.EXT_FIELD_NUMBER, GetPartyHandler.class);
        // ????????????
        registerC(PartyBuildRq.EXT_FIELD_NUMBER, PartyBuildRs.EXT_FIELD_NUMBER, PartyBuildHandler.class);
        // ????????????
        registerC(PromoteRanksRq.EXT_FIELD_NUMBER, PromoteRanksRs.EXT_FIELD_NUMBER, PromoteRanksHandler.class);
        // ??????????????????
        registerC(ModifySloganRq.EXT_FIELD_NUMBER, ModifySloganRs.EXT_FIELD_NUMBER, ModifySloganHandler.class);
        // ????????????????????????
        registerC(GetPartyCityRq.EXT_FIELD_NUMBER, GetPartyCityRs.EXT_FIELD_NUMBER, GetPartyCityHandler.class);
        // ????????????????????????
        registerC(GetPartyBattleRq.EXT_FIELD_NUMBER, GetPartyBattleRs.EXT_FIELD_NUMBER, GetPartyBattleHandler.class);
        // ????????????
        registerC(MakeInvitesRq.EXT_FIELD_NUMBER, MakeInvitesRs.EXT_FIELD_NUMBER, MakeInvitesHandler.class);
        // ??????????????????
        registerC(GetInvitesBattleRq.EXT_FIELD_NUMBER, GetInvitesBattleRs.EXT_FIELD_NUMBER, GetInvitesBattleHandler.class);
        // ????????????????????????
        registerC(GetPartyHonorRq.EXT_FIELD_NUMBER, GetPartyHonorRs.EXT_FIELD_NUMBER, GetPartyHonorHandler.class);
        // ????????????????????????
        registerC(PartyHonorRewardRq.EXT_FIELD_NUMBER, PartyHonorRewardRs.EXT_FIELD_NUMBER,
                PartyHonorRewardHandler.class);
        // ????????????????????????
        registerC(GetPartyLogRq.EXT_FIELD_NUMBER, GetPartyLogRs.EXT_FIELD_NUMBER, GetPartyLogHandler.class);
        // ????????????????????????
        registerC(GetPartyJobRq.EXT_FIELD_NUMBER, GetPartyJobRs.EXT_FIELD_NUMBER, GetPartyJobHandler.class);
        // ????????????????????????
        registerC(PartyVoteRq.EXT_FIELD_NUMBER, PartyVoteRs.EXT_FIELD_NUMBER, PartyVoteHandler.class);
        // ??????????????????
        registerC(PartyAppointRq.EXT_FIELD_NUMBER, PartyAppointRs.EXT_FIELD_NUMBER, PartyAppointHandler.class);
        // ????????????????????????
        registerC(PartyCanvassRq.EXT_FIELD_NUMBER, PartyCanvassRs.EXT_FIELD_NUMBER, PartyCanvassHandler.class);
        // ?????????????????????
        registerC(SupplyHallRq.EXT_FIELD_NUMBER, SupplyHallRs.EXT_FIELD_NUMBER, SupplyHallHandler.class);
        // ????????????????????????
        registerC(SupplyRewardRq.EXT_FIELD_NUMBER, SupplyRewardRs.EXT_FIELD_NUMBER, SupplyRewardHandler.class);

        // ???????????????
        // ?????????????????????????????????
        registerC(GetAcquisitionRq.EXT_FIELD_NUMBER, GetAcquisitionRs.EXT_FIELD_NUMBER, GetAcquisitionHandler.class);
        // ???????????????????????????
        registerC(BeginAcquisiteRq.EXT_FIELD_NUMBER, BeginAcquisiteRs.EXT_FIELD_NUMBER, BeginAcquisiteHandler.class);
        // ???????????????????????????
        registerC(AcquisiteRewradRq.EXT_FIELD_NUMBER, AcquisiteRewradRs.EXT_FIELD_NUMBER, AcquisiteRewradHandler.class);
        // ??????????????????
        registerC(UseFreeSpeedRq.EXT_FIELD_NUMBER, UseFreeSpeedRs.EXT_FIELD_NUMBER, UseFreeSpeedHandler.class);
        // ????????????
        registerC(GetStatusRq.EXT_FIELD_NUMBER, GetStatusRs.EXT_FIELD_NUMBER, GetStatusHandler.class);
        // vip????????????
        registerC(GetVipCntRq.EXT_FIELD_NUMBER, GetVipCntRs.EXT_FIELD_NUMBER, GetVipCntHandler.class);

        // ??????????????????
        // ??????????????????
        registerC(GetFriendsRq.EXT_FIELD_NUMBER, GetFriendsRs.EXT_FIELD_NUMBER, GetFriendsHandler.class);
        // ????????????
        registerC(AddFriendRq.EXT_FIELD_NUMBER, AddFriendRs.EXT_FIELD_NUMBER, AddFriendHandler.class);
        // ????????????
        registerC(DelFriendRq.EXT_FIELD_NUMBER, DelFriendRs.EXT_FIELD_NUMBER, DelFriendHandler.class);
        // ???????????????????????????
        registerC(AgreeRejectRq.EXT_FIELD_NUMBER, AgreeRejectRs.EXT_FIELD_NUMBER, AgreeRejectHandler.class);
        // ????????????????????????
        registerC(AgreeRejectMasterRq.EXT_FIELD_NUMBER, AgreeRejectMasterRs.EXT_FIELD_NUMBER, AgreeRejectMasterHandler.class);
        // ????????????
        registerC(CheckFirendRq.EXT_FIELD_NUMBER, CheckFirendRs.EXT_FIELD_NUMBER, CheckFirendHandler.class);
        // ??????????????????
        registerC(GetMasterApprenticeRq.EXT_FIELD_NUMBER, GetMasterApprenticeRs.EXT_FIELD_NUMBER,
                GetMasterApprenticeHandler.class);
        // ??????
        registerC(AddMasterRq.EXT_FIELD_NUMBER, AddMasterRs.EXT_FIELD_NUMBER, AddMasterHandler.class);
        // ??????????????????
        registerC(MasterRewardRq.EXT_FIELD_NUMBER, MasterRewardRs.EXT_FIELD_NUMBER, MasterRewardHandler.class);
        // ????????????
        registerC(CreditExchangeRq.EXT_FIELD_NUMBER, CreditExchangeRs.EXT_FIELD_NUMBER, CreditExchangeHandler.class);
        // ??????????????????
        registerC(GetRecommendLordRq.EXT_FIELD_NUMBER, GetRecommendLordRs.EXT_FIELD_NUMBER,
                GetRecommendLordHandler.class);
        // ???????????????
        registerC(GetBlacklistRq.EXT_FIELD_NUMBER, GetBlacklistRs.EXT_FIELD_NUMBER, GetBlacklistHandler.class);
        // ???????????????
        registerC(AddBlackListRq.EXT_FIELD_NUMBER, AddBlackListRs.EXT_FIELD_NUMBER, AddBlackListHandler.class);
        // ???????????????
        registerC(DelBlackListRq.EXT_FIELD_NUMBER, DelBlackListRs.EXT_FIELD_NUMBER, DelBlackListHandler.class);
        // ??????????????????
        registerC(DelMasterApprenticeRq.EXT_FIELD_NUMBER, DelMasterApprenticeRs.EXT_FIELD_NUMBER, DelMasterApprenticeHandler.class);

        // ??????????????????
        // ??????????????????
        registerC(BerlinInfoRq.EXT_FIELD_NUMBER, BerlinInfoRs.EXT_FIELD_NUMBER, BerlinInfoHandler.class);
        // ????????????????????????
        registerC(BerlinCityInfoRq.EXT_FIELD_NUMBER, BerlinCityInfoRs.EXT_FIELD_NUMBER, BerlinCityInfoHandler.class);
        // ????????????????????????
        registerC(RecentlyBerlinReportRq.EXT_FIELD_NUMBER, RecentlyBerlinReportRs.EXT_FIELD_NUMBER,
                RecentlyBerlinReportHandler.class);
        // ????????????????????????
        registerC(GetBerlinRankRq.EXT_FIELD_NUMBER, GetBerlinRankRs.EXT_FIELD_NUMBER, BerlinRankInfoHandler.class);
        // ????????????????????????
        registerC(GetBerlinIntegralRq.EXT_FIELD_NUMBER, GetBerlinIntegralRs.EXT_FIELD_NUMBER,
                BerlinIntegralHandler.class);
        // ??????????????????
        registerC(AttackBerlinWarRq.EXT_FIELD_NUMBER, AttackBerlinWarRs.EXT_FIELD_NUMBER, AttackBerlinWarHandler.class);
        // ??????????????????
        registerC(GetBerlinJobRq.EXT_FIELD_NUMBER, GetBerlinJobRs.EXT_FIELD_NUMBER, GetBerlinJobHandler.class);
        registerC(AppointBerlinJobRq.EXT_FIELD_NUMBER, AppointBerlinJobRs.EXT_FIELD_NUMBER,
                // ??????????????????
                AppointBerlinJobHandler.class);
        // ??????????????????
        registerC(GetBerlinWinnerListRq.EXT_FIELD_NUMBER, GetBerlinWinnerListRs.EXT_FIELD_NUMBER,
                GetBerlinWinnerListHandler.class);
        // ??????????????????CD
        registerC(ResumeImmediatelyRq.EXT_FIELD_NUMBER, ResumeImmediatelyRs.EXT_FIELD_NUMBER,
                ResumeImmediatelyHandler.class);
        // ??????????????????
        registerC(ImmediatelyAttackRq.EXT_FIELD_NUMBER, ImmediatelyAttackRs.EXT_FIELD_NUMBER,
                ImmediatelyAttackHandler.class);
        // ??????Buff
        registerC(PrewarBuffRq.EXT_FIELD_NUMBER, PrewarBuffRs.EXT_FIELD_NUMBER, PrewarBuffHandler.class);

        // ?????????????????????
        registerC(GetCounterAttackRq.EXT_FIELD_NUMBER, GetCounterAttackRs.EXT_FIELD_NUMBER,
                GetCounterAttackHandler.class);
        registerC(AttackCounterBossRq.EXT_FIELD_NUMBER, AttackCounterBossRs.EXT_FIELD_NUMBER,
                AttackCounterBossHandler.class);
        registerC(GetCounterAtkShopRq.EXT_FIELD_NUMBER, GetCounterAtkShopRs.EXT_FIELD_NUMBER,
                GetCounterAtkShopHandler.class);
        registerC(BuyCounterAtkAwardRq.EXT_FIELD_NUMBER, BuyCounterAtkAwardRs.EXT_FIELD_NUMBER,
                BuyCounterAtkAwardHandler.class);

        // ??????????????????
        // ??????????????????
        registerC(GetMentorsRq.EXT_FIELD_NUMBER, GetMentorsRs.EXT_FIELD_NUMBER, GetMentorsHandler.class);
        // ??????????????????
        registerC(GetSpecialEquipsRq.EXT_FIELD_NUMBER, GetSpecialEquipsRs.EXT_FIELD_NUMBER,
                GetSpecialEquipsHandler.class);
        // ????????????
        registerC(MentorsQuickUpRq.EXT_FIELD_NUMBER, MentorsQuickUpRs.EXT_FIELD_NUMBER, MentorsQuickUpHandler.class);
        // ??????????????????
        registerC(MentorsSkillUpRq.EXT_FIELD_NUMBER, MentorsSkillUpRs.EXT_FIELD_NUMBER, MentorsSkillUpHandler.class);
        // ??????????????????
        registerC(GetMentorAwardRq.EXT_FIELD_NUMBER, GetMentorAwardRs.EXT_FIELD_NUMBER, GetMentorAwardHandler.class);
        // ??????????????????
        registerC(AutoWearEquipRq.EXT_FIELD_NUMBER, AutoWearEquipRs.EXT_FIELD_NUMBER, AutoWearEquipHandler.class);
        // ??????????????????
        registerC(SellMentorEquipRq.EXT_FIELD_NUMBER, SellMentorEquipRs.EXT_FIELD_NUMBER, SellMentorEquipHandler.class);
        registerC(MentorActivateRq.EXT_FIELD_NUMBER, MentorActivateRs.EXT_FIELD_NUMBER, MentorActivateHandler.class);

        // ????????????
        registerC(ActionPointRq.EXT_FIELD_NUMBER, ActionPointRs.EXT_FIELD_NUMBER, ActionPointHandler.class);

        // ????????????
        // ??????????????????
        registerC(DecisiveBattleRq.EXT_FIELD_NUMBER, DecisiveBattleRs.EXT_FIELD_NUMBER, DecisiveBattleHandler.class);
        // ??????????????????
        registerC(GainInstructionsRq.EXT_FIELD_NUMBER, GainInstructionsRs.EXT_FIELD_NUMBER,
                GainInstructionsHandler.class);
        // ??????????????????
        registerC(AttackDecisiveBattleRq.EXT_FIELD_NUMBER, AttackDecisiveBattleRs.EXT_FIELD_NUMBER,
                AttackDecisiveBattleHandler.class);
        // ????????????????????????
        registerC(BattleFailMessageRq.EXT_FIELD_NUMBER, BattleFailMessageRs.EXT_FIELD_NUMBER,
                BattleFailMessageHandler.class);

        // ??????
        registerC(GetSignInInfoRq.EXT_FIELD_NUMBER, GetSignInInfoRs.EXT_FIELD_NUMBER, GetSignInInfoRqHandler.class);
        registerC(GetSignInRewardRq.EXT_FIELD_NUMBER, GetSignInRewardRs.EXT_FIELD_NUMBER,
                GetSignInRewardRqHandler.class);

        // ????????????
        registerC(GetLuckyPoolRq.EXT_FIELD_NUMBER, GetLuckyPoolRs.EXT_FIELD_NUMBER, GetLuckyPoolHandler.class);
        registerC(PlayLuckyPoolRq.EXT_FIELD_NUMBER, PlayLuckyPoolRs.EXT_FIELD_NUMBER, PlayLuckyPoolHandler.class);
        registerC(GetLuckyPoolRankRq.EXT_FIELD_NUMBER, GetLuckyPoolRankRs.EXT_FIELD_NUMBER,
                GetLuckyPoolRankHandler.class);

        // ????????????
        registerC(GetScheduleRq.EXT_FIELD_NUMBER, GetScheduleRs.EXT_FIELD_NUMBER, GetScheduleHandler.class);
        registerC(GainGoalAwardRq.EXT_FIELD_NUMBER, GainGoalAwardRs.EXT_FIELD_NUMBER, GainGoalAwardHandler.class);
        registerC(AttckScheduleBossRq.EXT_FIELD_NUMBER, AttckScheduleBossRs.EXT_FIELD_NUMBER,
                AttckScheduleBossHandler.class);
        registerC(GetScheduleBossRq.EXT_FIELD_NUMBER, GetScheduleBossRs.EXT_FIELD_NUMBER, GetScheduleBossHandler.class);

        registerC(ScorpionActivateRq.EXT_FIELD_NUMBER, ScorpionActivateRs.EXT_FIELD_NUMBER, ScorpionActivateRsHandler.class);

        // ?????????
        registerC(GetCrossMapRq.EXT_FIELD_NUMBER, GetCrossMapRs.EXT_FIELD_NUMBER, GetCrossMapHandler.class);
        registerC(GetCrossAreaRq.EXT_FIELD_NUMBER, GetCrossAreaRs.EXT_FIELD_NUMBER, GetCrossAreaHandler.class);
        registerC(GetCrossMarchRq.EXT_FIELD_NUMBER, GetCrossMarchRs.EXT_FIELD_NUMBER, GetCrossMarchHandler.class);
        registerC(GetCrossArmyRq.EXT_FIELD_NUMBER, GetCrossArmyRs.EXT_FIELD_NUMBER, GetCrossArmyHandler.class);
        registerC(GetCrossMineRq.EXT_FIELD_NUMBER, GetCrossMineRs.EXT_FIELD_NUMBER, GetCrossMineHandler.class);
        registerC(AttackCrossPosRq.EXT_FIELD_NUMBER, AttackCrossPosRs.EXT_FIELD_NUMBER, AttackCrossPosHandler.class);
        registerC(RetreatCrossRq.EXT_FIELD_NUMBER, RetreatCrossRs.EXT_FIELD_NUMBER, RetreatCrossHandler.class);
        registerC(GetCrossBattleRq.EXT_FIELD_NUMBER, GetCrossBattleRs.EXT_FIELD_NUMBER, GetCrossBattleHandler.class);
        registerC(CrossMoveCityRq.EXT_FIELD_NUMBER, CrossMoveCityRs.EXT_FIELD_NUMBER, CrossMoveCityHandler.class);
        registerC(EnterLeaveCrossRq.EXT_FIELD_NUMBER, EnterLeaveCrossRs.EXT_FIELD_NUMBER, EnterLeaveCrossHandler.class);
        registerC(GetCrossMapInfoRq.EXT_FIELD_NUMBER, GetCrossMapInfoRs.EXT_FIELD_NUMBER, GetCrossMapInfoHandler.class);
        registerC(JoinBattleCrossRq.EXT_FIELD_NUMBER, JoinBattleCrossRs.EXT_FIELD_NUMBER, JoinBattleCrossHandler.class);

        // ?????????????????????????????????????????????
        registerC(WorldWarCampDateRq.EXT_FIELD_NUMBER, WorldWarCampDateRs.EXT_FIELD_NUMBER,
                GetWorldWarCampDateHandler.class);
        registerC(WorldWarTaskDateRq.EXT_FIELD_NUMBER, WorldWarTaskDateRs.EXT_FIELD_NUMBER,
                GetWorldWarTaskDateHandler.class);
        registerC(WorldWarSeasonShopGoodsRq.EXT_FIELD_NUMBER, WorldWarSeasonShopGoodsRs.EXT_FIELD_NUMBER,
                GetWorldWarSeasonShopGoodsHandler.class);
        registerC(WorldWarCampRankPlayersDateRq.EXT_FIELD_NUMBER, WorldWarCampRankPlayersDateRs.EXT_FIELD_NUMBER,
                GetWorldWarCampRankPlayersDateHandler.class);
        registerC(WorldWarAwardRq.EXT_FIELD_NUMBER, WorldWarAwardRs.EXT_FIELD_NUMBER, GetWorldWarAwardHandler.class);

        //????????????
        registerC(NewYorkWarInfoRq.EXT_FIELD_NUMBER, NewYorkWarInfoRs.EXT_FIELD_NUMBER, NewYorkWarInfoHandler.class);
        registerC(NewYorkWarProgressDataRq.EXT_FIELD_NUMBER, NewYorkWarProgressDataRs.EXT_FIELD_NUMBER, NewYorkWarProgressDataHandler.class);
        registerC(NewYorkWarPlayerRankDataRq.EXT_FIELD_NUMBER, NewYorkWarPlayerRankDataRs.EXT_FIELD_NUMBER, NewYorkWarPlayerRankDataHandler.class);
        registerC(NewYorkWarAchievementRq.EXT_FIELD_NUMBER, NewYorkWarAchievementRs.EXT_FIELD_NUMBER, NewYorkWarAchievementHandler.class);

        // ????????????
        registerC(ActBartonBuyRq.EXT_FIELD_NUMBER, ActBartonBuyRs.EXT_FIELD_NUMBER, ActBartonBuyHandler.class);
        registerC(GetActBartonRq.EXT_FIELD_NUMBER, GetActBartonRs.EXT_FIELD_NUMBER, GetActBartonHandler.class);

        // ???????????????
        registerC(GetActRobinHoodRq.EXT_FIELD_NUMBER, GetActRobinHoodRs.EXT_FIELD_NUMBER, GetActRobinHoodHandler.class);
        registerC(ActRobinHoodAwardRq.EXT_FIELD_NUMBER, ActRobinHoodAwardRs.EXT_FIELD_NUMBER, ActRobinHoodAwardHandler.class);

        // ????????????
        registerC(GetBattlePassRq.EXT_FIELD_NUMBER, GetBattlePassRs.EXT_FIELD_NUMBER, GetBattlePassHandler.class);
        registerC(BuyBattlePassLvRq.EXT_FIELD_NUMBER, BuyBattlePassLvRs.EXT_FIELD_NUMBER, BuyBattlePassLvHandler.class);
        registerC(ReceiveBPAwardRq.EXT_FIELD_NUMBER, ReceiveBPAwardRs.EXT_FIELD_NUMBER, ReceiveBPAwardHandler.class);
        registerC(ReceiveTaskAwardRq.EXT_FIELD_NUMBER, ReceiveTaskAwardRs.EXT_FIELD_NUMBER, ReceiveTaskAwardHandler.class);

        // ??????????????????
        registerC(GetRoyalArenaRq.EXT_FIELD_NUMBER, GetRoyalArenaRs.EXT_FIELD_NUMBER, GetRoyalArenaHandler.class);
        registerC(RoyalArenaAwardRq.EXT_FIELD_NUMBER, RoyalArenaAwardRs.EXT_FIELD_NUMBER, RoyalArenaAwardHandler.class);
        registerC(RoyalArenaTaskRq.EXT_FIELD_NUMBER, RoyalArenaTaskRs.EXT_FIELD_NUMBER, RoyalArenaTaskHandler.class);
        registerC(RoyalArenaSkillRq.EXT_FIELD_NUMBER, RoyalArenaSkillRs.EXT_FIELD_NUMBER, RoyalArenaSkillHandler.class);

        //????????????
        registerC(GetChristmasInfoRq.EXT_FIELD_NUMBER, GetChristmasInfoRs.EXT_FIELD_NUMBER, ChristmasGetInfoHandler.class);
        registerC(HandInChristmasChipRq.EXT_FIELD_NUMBER, HandInChristmasChipRs.EXT_FIELD_NUMBER, ChristmasHandInChipHandler.class);
        registerC(GetChristmasAwardRq.EXT_FIELD_NUMBER, GetChristmasAwardRs.EXT_FIELD_NUMBER, ChristmasGetAwardHandler.class);

        //????????????
        registerC(SandTableEnrollRq.EXT_FIELD_NUMBER, SandTableEnrollRs.EXT_FIELD_NUMBER, SandTableEnrollHandler.class);
        registerC(SandTableUpdateArmyRq.EXT_FIELD_NUMBER, SandTableUpdateArmyRs.EXT_FIELD_NUMBER, SandTableUpdateArmyHandler.class);
        registerC(SandTableChangeLineRq.EXT_FIELD_NUMBER, SandTableChangeLineRs.EXT_FIELD_NUMBER, SandTableChangeLineHandler.class);
        registerC(SandTableAdjustLineRq.EXT_FIELD_NUMBER, SandTableAdjustLineRs.EXT_FIELD_NUMBER, SandTableAdjustLineHandler.class);
        registerC(SandTableGetInfoRq.EXT_FIELD_NUMBER, SandTableGetInfoRs.EXT_FIELD_NUMBER, SandTableGetInfoHandler.class);
        registerC(SandTableHisRankRq.EXT_FIELD_NUMBER, SandTableHisRankRs.EXT_FIELD_NUMBER, SandTableHisRankHandler.class);
        registerC(SandTableHisContestRq.EXT_FIELD_NUMBER, SandTableHisContestRs.EXT_FIELD_NUMBER, SandTableHisContestHandler.class);
        registerC(SandTableReplayRq.EXT_FIELD_NUMBER, SandTableReplayRs.EXT_FIELD_NUMBER, SandTableReplayHandler.class);
        registerC(SandTableShopBuyRq.EXT_FIELD_NUMBER, SandTableShopBuyRs.EXT_FIELD_NUMBER, SandTableShopBuyHandler.class);
        registerC(SandTablePlayerFightDetailRq.EXT_FIELD_NUMBER, SandTablePlayerFightDetailRs.EXT_FIELD_NUMBER, SandTablePlayerFightDetailHandler.class);
        registerC(SandTableGetLinePlayersRq.EXT_FIELD_NUMBER, SandTableGetLinePlayersRs.EXT_FIELD_NUMBER, SandTableGetLinePlayersHandler.class);

        //????????????
        //????????????---????????????
        registerC(GetWarFireCampRankRq.EXT_FIELD_NUMBER, GetWarFireCampRankRs.EXT_FIELD_NUMBER, GetWarFireCampRankHandler.class);
        registerC(GetWarFireCampScoreRq.EXT_FIELD_NUMBER, GetWarFireCampScoreRs.EXT_FIELD_NUMBER, GetWarFireCampScoreHandler.class);
        registerC(GetWarFireCampSummaryRq.EXT_FIELD_NUMBER, GetWarFireCampSummaryRs.EXT_FIELD_NUMBER, GetWarFireCampSummaryHandler.class);
        //????????????---????????????
        registerC(BuyWarFireShopRq.EXT_FIELD_NUMBER, BuyWarFireShopRs.EXT_FIELD_NUMBER, BuyWarFireShopHandler.class);
        registerC(BuyWarFireBuffRq.EXT_FIELD_NUMBER, BuyWarFireBuffRs.EXT_FIELD_NUMBER, BuyWarFireBuffHandler.class);
        // ??????????????????????????????
        registerC(GetCrossCityInfoRq.EXT_FIELD_NUMBER, GetCrossCityInfoRs.EXT_FIELD_NUMBER, GetCrossCityInfoHandler.class);
        //??????????????????????????????
        registerC(GetPlayerWarFireRq.EXT_FIELD_NUMBER, GetPlayerWarFireRs.EXT_FIELD_NUMBER, GetWarFirePlayerInfoHandler.class);

        //?????????, ????????????
        registerC(SetOffFirecrackersRq.EXT_FIELD_NUMBER, SetOffFirecrackersRs.EXT_FIELD_NUMBER, SetOffFirecrackersNianHandler.class);

        //????????????
        registerC(DiaoChanGetInfoRq.EXT_FIELD_NUMBER, DiaoChanGetInfoRs.EXT_FIELD_NUMBER, DiaoChanGetInfoHandler.class);
        registerC(DiaoChanGetAwardRq.EXT_FIELD_NUMBER, DiaoChanGetAwardRs.EXT_FIELD_NUMBER, DiaoChanGetAwardHandler.class);
        registerC(DiaoChanGetRankInfoRq.EXT_FIELD_NUMBER, DiaoChanGetRankInfoRs.EXT_FIELD_NUMBER, DiaoChanGetRankInfoHandler.class);

        //??????
        registerC(OnHookGetInfoRq.EXT_FIELD_NUMBER, OnHookGetInfoRs.EXT_FIELD_NUMBER, OnHookGetInfoHandler.class);
        registerC(OnHookReplenishRq.EXT_FIELD_NUMBER, OnHookReplenishRs.EXT_FIELD_NUMBER, OnHookReplenishHandler.class);
        registerC(OnHookOperateRq.EXT_FIELD_NUMBER, OnHookOperateRs.EXT_FIELD_NUMBER, OnHookOperateHandler.class);
        registerC(OnHookGetAwardRq.EXT_FIELD_NUMBER, OnHookGetAwardRs.EXT_FIELD_NUMBER, OnHookGetAwardHandler.class);

        //??????
        registerC(SeasonGetInfoRq.EXT_FIELD_NUMBER, SeasonGetInfoRs.EXT_FIELD_NUMBER, SeasonGetInfoHandler.class);
        registerC(SeasonGetTreasuryInfoRq.EXT_FIELD_NUMBER, SeasonGetTreasuryInfoRs.EXT_FIELD_NUMBER, SeasonGetTreasuryInfoHandler.class);
        registerC(SeasonGetTaskInfoRq.EXT_FIELD_NUMBER, SeasonGetTaskInfoRs.EXT_FIELD_NUMBER, SeasonGetTaskInfoHandler.class);
        registerC(SeasonGenerateTreasuryAwardRq.EXT_FIELD_NUMBER, SeasonGenerateTreasuryAwardRs.EXT_FIELD_NUMBER, SeasonGenerateTreasuryAwardHandler.class);
        registerC(SeasonGetTreasuryAwardRq.EXT_FIELD_NUMBER, SeasonGetTreasuryAwardRs.EXT_FIELD_NUMBER, SeasonGetTreasuryAwardHandler.class);
        registerC(SeasonGetTaskAwardRq.EXT_FIELD_NUMBER, SeasonGetTaskAwardRs.EXT_FIELD_NUMBER, SeasonGetTaskAwardHandler.class);
        registerC(SeasonGetRankRq.EXT_FIELD_NUMBER, SeasonGetRankRs.EXT_FIELD_NUMBER, SeasonGetRankHandler.class);

        //????????????
        registerC(SynthSeasonHeroRq.EXT_FIELD_NUMBER, SynthSeasonHeroRs.EXT_FIELD_NUMBER, SeasonSynthHeroHandler.class);
        registerC(UpgradeHeroCgyRq.EXT_FIELD_NUMBER, UpgradeHeroCgyRs.EXT_FIELD_NUMBER, SeasonUpgradeHeroCgyHandler.class);
        registerC(UpgradeHeroSkillRq.EXT_FIELD_NUMBER, UpgradeHeroSkillRs.EXT_FIELD_NUMBER, SeasonUpgradeHeroSkillHandler.class);

        //??????
        registerC(GetActivityDataInfoRq.EXT_FIELD_NUMBER, GetActivityDataInfoRs.EXT_FIELD_NUMBER, GetActivityDataInfoHandler.class);
        registerC(DragonBoatExchangeRq.EXT_FIELD_NUMBER, DragonBoatExchangeRs.EXT_FIELD_NUMBER, DragonBoatExchangeHandler.class);
        registerC(DailyKeepRechargeGetAwardRq.EXT_FIELD_NUMBER, DailyKeepRechargeGetAwardRs.EXT_FIELD_NUMBER, DailyKeepRechargeGetAwardHandler.class);
        registerC(SummerTurntablePlayRq.EXT_FIELD_NUMBER, SummerTurntablePlayRs.EXT_FIELD_NUMBER, SummerTurntablePlayHandler.class);
        registerC(SummerTurntableNextRq.EXT_FIELD_NUMBER, SummerTurntableNextRs.EXT_FIELD_NUMBER, SummerTurntableNextHandler.class);
        registerC(SummerCastleGetAwardRq.EXT_FIELD_NUMBER, SummerCastleGetAwardRs.EXT_FIELD_NUMBER, SummerCastleGetAwardHandler.class);
        registerC(GetAltarRq.EXT_FIELD_NUMBER, GetAltarRs.EXT_FIELD_NUMBER, GetAltarHandler.class); // ??????????????????
        registerC(AnniversaryTurntablePlayRq.EXT_FIELD_NUMBER, AnniversaryTurntablePlayRs.EXT_FIELD_NUMBER, AnniversaryTurntablePlayHandler.class);//????????????
        registerC(AnniversaryEggOpenRq.EXT_FIELD_NUMBER, AnniversaryEggOpenRs.EXT_FIELD_NUMBER, AnniversaryEggOpenHandler.class);//????????????
        registerC(AnniversaryJigsawRq.EXT_FIELD_NUMBER, AnniversaryJigsawRs.EXT_FIELD_NUMBER, AnniversaryJigsawHandler.class);//???????????????
        registerC(FireFireWorkRq.EXT_FIELD_NUMBER, FireFireWorkRs.EXT_FIELD_NUMBER, AnniversaryFireFireWorkHandler.class);//???????????????
        registerC(BuyEncoreSkinRq.EXT_FIELD_NUMBER, BuyEncoreSkinRs.EXT_FIELD_NUMBER, BuyEncoreSkinHandler.class);//???????????????????????????
        registerC(MusicFestivalBoxOfficeActionRq.EXT_FIELD_NUMBER, MusicFestivalBoxOfficeActionRs.EXT_FIELD_NUMBER, BoxOfficeActionHandler.class);// ?????????-?????????
        registerC(ChooseDifficultRq.EXT_FIELD_NUMBER, ChooseDifficultRs.EXT_FIELD_NUMBER, MusicCrtOfficeChooseDifficultHandler.class);// ?????????-????????????-????????????
        registerC(FinishCrtTaskRq.EXT_FIELD_NUMBER, FinishCrtTaskRs.EXT_FIELD_NUMBER, MusicCrtOfficeFinishTaskHandler.class);// ?????????-????????????-????????????
        registerC(GiveUpCrtMusicRq.EXT_FIELD_NUMBER, GiveUpCrtMusicRs.EXT_FIELD_NUMBER, MusicCrtOfficeGiveUpCrtHandler.class);// ?????????-????????????-????????????
        registerC(FinishCrtMusicRq.EXT_FIELD_NUMBER, FinishCrtMusicRs.EXT_FIELD_NUMBER, FinishCreateMusicHandler.class);// ?????????-????????????-????????????
        registerC(CrtMusicCampRankRq.EXT_FIELD_NUMBER, CrtMusicCampRankRs.EXT_FIELD_NUMBER, CrtMusicCampRankHandler.class);// ?????????-????????????-??????????????????
        registerC(CrtMusicPersonRankRq.EXT_FIELD_NUMBER, CrtMusicPersonRankRs.EXT_FIELD_NUMBER, CrtMusicPersonRankHandler.class);// ?????????-????????????-??????????????????
        registerC(DrawProgressRq.EXT_FIELD_NUMBER, DrawProgressRs.EXT_FIELD_NUMBER, DrawProgressHandler.class);// ?????????-????????????-??????????????????
        registerC(MusicSpecialThanksRq.EXT_FIELD_NUMBER, MusicSpecialThanksRs.EXT_FIELD_NUMBER, GetSpecialThanksHandler.class);// ?????????-????????????-????????????


        //????????????
        registerC(GetSeasonTalentRq.EXT_FIELD_NUMBER, GetSeasonTalentRs.EXT_FIELD_NUMBER, SeasonGetTalentHandler.class);
        registerC(ChooseClassifierRq.EXT_FIELD_NUMBER, ChooseClassifierRs.EXT_FIELD_NUMBER, SeasonTalentChooseClassifierHandler.class);
        registerC(StudyTalentRq.EXT_FIELD_NUMBER, StudyTalentRs.EXT_FIELD_NUMBER, SeasonStudyTalentHandler.class);
        registerC(ChangeTalentSkillRq.EXT_FIELD_NUMBER, ChangeTalentSkillRs.EXT_FIELD_NUMBER, SeasonChangeTalentHandler.class);
        registerC(OpenTalentRq.EXT_FIELD_NUMBER, OpenTalentRs.EXT_FIELD_NUMBER, SeasonOpenTalentHandler.class);

        //??????
        registerC(FishingCollectBaitDispatchHerosRq.EXT_FIELD_NUMBER, FishingCollectBaitDispatchHerosRs.EXT_FIELD_NUMBER, FishingCollectBaitDispatchHerosHandler.class);
        registerC(FishingCollectBaitGetAwardRq.EXT_FIELD_NUMBER, FishingCollectBaitGetAwardRs.EXT_FIELD_NUMBER, FishingCollectBaitGetAwardHandler.class);
        registerC(FishingCollectBaitGetInfoRq.EXT_FIELD_NUMBER, FishingCollectBaitGetInfoRs.EXT_FIELD_NUMBER, FishingCollectBaitGetInfoHandler.class);
        registerC(FishingFisheryGetInfoRq.EXT_FIELD_NUMBER, FishingFisheryGetInfoRs.EXT_FIELD_NUMBER, FishingFisheryGetInfoHandler.class);
        registerC(FishingFisheryThrowRodRq.EXT_FIELD_NUMBER, FishingFisheryThrowRodRs.EXT_FIELD_NUMBER, FishingFisheryThrowRodHandler.class);
        registerC(FishingFisheryStowRodRq.EXT_FIELD_NUMBER, FishingFisheryStowRodRs.EXT_FIELD_NUMBER, FishingFisheryStowRodHandler.class);
        registerC(FishingAltasRq.EXT_FIELD_NUMBER, FishingAltasRs.EXT_FIELD_NUMBER, FishingAltasHandler.class);
        registerC(FishingShopGetInfoRq.EXT_FIELD_NUMBER, FishingShopGetInfoRs.EXT_FIELD_NUMBER, FishingShopGetInfoHandler.class);
        registerC(FishingShopExchangeRq.EXT_FIELD_NUMBER, FishingShopExchangeRs.EXT_FIELD_NUMBER, FishingShopExchangeHandler.class);
        registerC(FishingShareFishLogRq.EXT_FIELD_NUMBER, FishingShareFishLogRs.EXT_FIELD_NUMBER, FishingShareFishLogHandler.class);
        registerC(FishingFishLogRq.EXT_FIELD_NUMBER, FishingFishLogRs.EXT_FIELD_NUMBER, FishingFishLogHandler.class);
        registerC(FishingGetFishAltasAwardRq.EXT_FIELD_NUMBER, FishingGetFishAltasAwardRs.EXT_FIELD_NUMBER, FishingGetFishAltasAwardHandler.class);

        //????????????
        registerC(AutumnTurnplatePlayRq.EXT_FIELD_NUMBER, AutumnTurnplatePlayRs.EXT_FIELD_NUMBER, AutumnTurnplatePlayHandler.class);
        registerC(AutumnTurnplateGetProgressAwardRq.EXT_FIELD_NUMBER, AutumnTurnplateGetProgressAwardRs.EXT_FIELD_NUMBER, AutumnTurnplateGetProgressAwardHandler.class);
        registerC(AutumnTurnplateRefreshRq.EXT_FIELD_NUMBER, AutumnTurnplateRefreshRs.EXT_FIELD_NUMBER, AutumnTurnplateRefreshHandler.class);

        //????????????
        registerC(EmpireFarmRq.EXT_FIELD_NUMBER, EmpireFarmRs.EXT_FIELD_NUMBER, EmpireFarmSowingHandler.class);
        registerC(EmpireFarmOpenTreasureChestRq.EXT_FIELD_NUMBER, EmpireFarmOpenTreasureChestRs.EXT_FIELD_NUMBER, EmpireFarmOpenTreasureChestHandler.class);
        registerC(GoldenAutumnFruitfulRq.EXT_FIELD_NUMBER, GoldenAutumnFruitfulRs.EXT_FIELD_NUMBER, GoldenAutumnFruitfulHandler.class);
        registerC(GoldenAutumnSunriseGetTaskAwardRq.EXT_FIELD_NUMBER, GoldenAutumnSunriseGetTaskAwardRs.EXT_FIELD_NUMBER, GoldenAutumnGetTaskAwardHandler.class);
        registerC(GoldenAutumnSunriseOpenTreasureChestRq.EXT_FIELD_NUMBER, GoldenAutumnSunriseOpenTreasureChestRs.EXT_FIELD_NUMBER, GoldenAutumnOpenTreasureChestHandler.class);

        //????????????
        registerC(GetCrossRechargeRankingRq.EXT_FIELD_NUMBER, GetCrossRechargeRankingRs.EXT_FIELD_NUMBER, GetCrossRechargeActivityRankHandler.class);

        // simple????????????
        registerC(EnterCrossRq.EXT_FIELD_NUMBER, EnterCrossRs.EXT_FIELD_NUMBER, EnterCrossHandler.class); // ??????????????????
        registerC(ChoiceHeroJoinRq.EXT_FIELD_NUMBER, ChoiceHeroJoinRs.EXT_FIELD_NUMBER, ChoiceHeroJoinHandler.class); // ?????????????????????
        registerC(GetCrossFortRq.EXT_FIELD_NUMBER, GetCrossFortRs.EXT_FIELD_NUMBER, DirectForwardClientHandler.class); // ????????????????????????
        registerC(OpFortHeroRq.EXT_FIELD_NUMBER, OpFortHeroRs.EXT_FIELD_NUMBER, OpFortHeroHandler.class); // ????????????????????????
        registerC(GetCrossChatRq.EXT_FIELD_NUMBER, GetCrossChatRs.EXT_FIELD_NUMBER, DirectForwardClientHandler.class); // ????????????????????????
        registerC(BuyCrossBuffRq.EXT_FIELD_NUMBER, BuyCrossBuffRs.EXT_FIELD_NUMBER, BuyCrossBuffHandler.class); // ????????????buff
        registerC(GetCrossInfoRq.EXT_FIELD_NUMBER, GetCrossInfoRs.EXT_FIELD_NUMBER, GetCrossInfoHandler.class); // ??????????????????(????????????,buff??????)
        registerC(CrossTrophyInfoRq.EXT_FIELD_NUMBER, CrossTrophyInfoRs.EXT_FIELD_NUMBER, CrossTrophyInfoHandler.class); // ????????????????????????
        registerC(CrossTrophyAwardRq.EXT_FIELD_NUMBER, CrossTrophyAwardRs.EXT_FIELD_NUMBER,
                CrossTrophyAwardHandler.class); // ????????????????????????
        registerC(GetCrossRankRq.EXT_FIELD_NUMBER, GetCrossRankRs.EXT_FIELD_NUMBER, DirectForwardClientHandler.class); // ?????????????????????

        //???????????????????????????????????????
        registerC(GetBannerRq.EXT_FIELD_NUMBER, GetBannerRs.EXT_FIELD_NUMBER, GetBannerHandler.class); // ????????????banner

        //??????????????????
        registerC(FollowAuctionsRq.EXT_FIELD_NUMBER, FollowAuctionsRs.EXT_FIELD_NUMBER, FollowAuctionsHandler.class);
        registerC(GetActAuctionInfoRq.EXT_FIELD_NUMBER, GetActAuctionInfoRs.EXT_FIELD_NUMBER, GetActAuctionInfoHandler.class);
        registerC(GetActAuctionItemRecordRq.EXT_FIELD_NUMBER, GetActAuctionItemRecordRs.EXT_FIELD_NUMBER, GetActAuctionItemRecordHandler.class);
        registerC(GetActAuctionRecordRq.EXT_FIELD_NUMBER, GetActAuctionRecordRs.EXT_FIELD_NUMBER, GetActAuctionRecordHandler.class);
        registerC(GetMyAuctionRecordRq.EXT_FIELD_NUMBER, GetMyAuctionRecordRs.EXT_FIELD_NUMBER, GetMyAuctionRecordHandler.class);
        registerC(PurchaseAuctionItemRq.EXT_FIELD_NUMBER, PurchaseAuctionItemRs.EXT_FIELD_NUMBER, PurchaseAuctionItemHandler.class);
        registerC(GetActAuctionTypeRq.EXT_FIELD_NUMBER, GetActAuctionTypeRs.EXT_FIELD_NUMBER, GetActAuctionTypeHandler.class);

        //???????????? ????????????
        registerC(HelpShengYuGetAwardRq.EXT_FIELD_NUMBER, HelpShengYuGetAwardRs.EXT_FIELD_NUMBER, HelpShengYuGetAwardHandler.class);

        //?????????
        registerC(GetSmallGameRq.EXT_FIELD_NUMBER, GetSmallGameRs.EXT_FIELD_NUMBER, GetSmallGameHandler.class);
        registerC(DrawSmallGameAwardRq.EXT_FIELD_NUMBER, DrawSmallGameAwardRs.EXT_FIELD_NUMBER, DrawSmallGameAwardHandler.class);
        //????????????
        registerC(TotemSyntheticRq.EXT_FIELD_NUMBER, TotemSyntheticRs.EXT_FIELD_NUMBER, TotemSyntheticHandler.class);
        registerC(TotemDecomposeRq.EXT_FIELD_NUMBER, TotemDecomposeRs.EXT_FIELD_NUMBER, TotemDecomposeHandler.class);
        registerC(TotemStrengthenRq.EXT_FIELD_NUMBER, TotemStrengthenRs.EXT_FIELD_NUMBER, TotemStrengthenHandler.class);
        registerC(TotemResonateRq.EXT_FIELD_NUMBER, TotemResonateRs.EXT_FIELD_NUMBER, TotemResonateHandler.class);
        registerC(TotemBreakRq.EXT_FIELD_NUMBER, TotemBreakRs.EXT_FIELD_NUMBER, TotemBreakHandler.class);
        registerC(TotemLockRq.EXT_FIELD_NUMBER, TotemLockRs.EXT_FIELD_NUMBER, TotemLockHandler.class);
        registerC(TotemPutonRq.EXT_FIELD_NUMBER, TotemPutonRs.EXT_FIELD_NUMBER, TotemPutonHandler.class);

        //??????
        registerC(GetTreasureWaresRq.EXT_FIELD_NUMBER, GetTreasureWaresRs.EXT_FIELD_NUMBER, GetTreasureWaresHandler.class);
        registerC(MakeTreasureWareRq.EXT_FIELD_NUMBER, MakeTreasureWareRs.EXT_FIELD_NUMBER, MakeTreasureWareHandler.class);
        registerC(OnTreasureWareRq.EXT_FIELD_NUMBER, OnTreasureWareRs.EXT_FIELD_NUMBER, OnTreasureWareHandler.class);
        registerC(StrengthenTreasureWareRq.EXT_FIELD_NUMBER, StrengthenTreasureWareRs.EXT_FIELD_NUMBER, StrengthenTreasureWareHandler.class);
        registerC(TreasureWareBagExpandRq.EXT_FIELD_NUMBER, TreasureWareBagExpandRs.EXT_FIELD_NUMBER, TreasureWareBagExpandHandler.class);
        registerC(TreasureWareBatchDecomposeRq.EXT_FIELD_NUMBER, TreasureWareBatchDecomposeRs.EXT_FIELD_NUMBER, TreasureWareDecomposeHandler.class);
        registerC(TreasureWareLockedRq.EXT_FIELD_NUMBER, TreasureWareLockedRs.EXT_FIELD_NUMBER, TreasureWareLockedHandler.class);
        //????????????
        registerC(TreasureWareTrainRq.EXT_FIELD_NUMBER, TreasureWareTrainRs.EXT_FIELD_NUMBER, TreasureWareTrainHandler.class);
        //??????????????????
        registerC(TreasureWareSaveTrainRq.EXT_FIELD_NUMBER, TreasureWareSaveTrainRs.EXT_FIELD_NUMBER, TreasureWareSaveTrainHandler.class);
        // ????????????
        registerC(GetTreasureCombatRq.EXT_FIELD_NUMBER, GetTreasureCombatRs.EXT_FIELD_NUMBER, GetTreasureCombatHandler.class);
        registerC(DoTreasureCombatRq.EXT_FIELD_NUMBER, DoTreasureCombatRs.EXT_FIELD_NUMBER, DoTreasureCombatHandler.class);
        registerC(TreasureOnHookAwardRq.EXT_FIELD_NUMBER, TreasureOnHookAwardRs.EXT_FIELD_NUMBER, TreasureOnHookAwardHandler.class);
        registerC(TreasureSectionAwardRq.EXT_FIELD_NUMBER, TreasureSectionAwardRs.EXT_FIELD_NUMBER, TreasureSectionAwardHandler.class);
        registerC(TreasureChallengePlayerRq.EXT_FIELD_NUMBER, TreasureChallengePlayerRs.EXT_FIELD_NUMBER, TreasureChallengePlayerHandler.class);
        registerC(TreasureRefreshChallengeRq.EXT_FIELD_NUMBER, TreasureRefreshChallengeRs.EXT_FIELD_NUMBER, TreasureRefreshChallengeHandler.class);
        registerC(TreasureChallengePurchaseRq.EXT_FIELD_NUMBER, TreasureChallengePurchaseRs.EXT_FIELD_NUMBER, TreasureChallengePurchaseHandler.class);

        //??????????????????
        registerC(ReceiveActTwJourneyAwardRq.EXT_FIELD_NUMBER, ReceiveActTwJourneyAwardRs.EXT_FIELD_NUMBER, ReceiveActTwJourneyAwardHandler.class);
        registerC(GetActTwJourneyRq.EXT_FIELD_NUMBER, GetActTwJourneyRs.EXT_FIELD_NUMBER, GetActTwJourneyInfoHandler.class);
        //??????????????????
        registerC(DrawTwTurntableAwardRq.EXT_FIELD_NUMBER, DrawTwTurntableAwardRs.EXT_FIELD_NUMBER, DrawMagicTwTurntableAwardHandler.class);
        registerC(ReceiveMtwTurntableCntAwardRq.EXT_FIELD_NUMBER, ReceiveMtwTurntableCntAwardRs.EXT_FIELD_NUMBER, ReceiveMTwtCntAwardHandler.class);

        //2022????????????
        registerC(LongLightIgniteRq.EXT_FIELD_NUMBER, LongLightIgniteRs.EXT_FIELD_NUMBER, LongLightIgniteHandler.class);
        registerC(FireworkLetoffRq.EXT_FIELD_NUMBER, FireworkLetoffRs.EXT_FIELD_NUMBER, FireworkLetoffHandler.class);
        registerC(YearFishBeginRq.EXT_FIELD_NUMBER, YearFishBeginRs.EXT_FIELD_NUMBER, YearFishBeginHandler.class);
        registerC(YearFishEndRq.EXT_FIELD_NUMBER, YearFishEndRs.EXT_FIELD_NUMBER, YearFishEndHandler.class);
        registerC(YearFishShopExchangeRq.EXT_FIELD_NUMBER, YearFishShopExchangeRs.EXT_FIELD_NUMBER, YearFishShopExchangeHandler.class);

        //????????????
        registerC(EnterLeaveCrossWarFireRq.EXT_FIELD_NUMBER, EnterLeaveCrossWarFireRs.EXT_FIELD_NUMBER, EnterLeaveNewCrossMapHandler.class);
        registerC(CrossWarFireMoveCityRq.EXT_FIELD_NUMBER, CrossWarFireMoveCityRs.EXT_FIELD_NUMBER, NewCrossMoveCityHandler.class);
        registerC(GetCrossWarFireAreaRq.EXT_FIELD_NUMBER, GetCrossWarFireAreaRs.EXT_FIELD_NUMBER, GetNewCrossAreaHandler.class);
        registerC(GetCrossWarFireBattleRq.EXT_FIELD_NUMBER, GetCrossWarFireBattleRs.EXT_FIELD_NUMBER, GetNewCrossBattleHandler.class);
        registerC(GetCrossWarFireMapRq.EXT_FIELD_NUMBER, GetCrossWarFireMapRs.EXT_FIELD_NUMBER, GetNewCrossMapHandler.class);
        registerC(GetCrossWarFireMarchRq.EXT_FIELD_NUMBER, GetCrossWarFireMarchRs.EXT_FIELD_NUMBER, GetNewCrossMarchHandler.class);
        registerC(RetreatCrossWarFireRq.EXT_FIELD_NUMBER, RetreatCrossWarFireRs.EXT_FIELD_NUMBER, RetreatNewCrossArmyHandler.class);
        registerC(GetCrossPlayerDataRq.EXT_FIELD_NUMBER, GetCrossPlayerDataRs.EXT_FIELD_NUMBER, GetCrossPlayerDataHandler.class);
        registerC(CrossWarFireAttackPosRq.EXT_FIELD_NUMBER, CrossWarFireAttackPosRs.EXT_FIELD_NUMBER, NewCrossAttackPosHandler.class);
        registerC(GetCrossWarFireArmyRq.EXT_FIELD_NUMBER, GetCrossWarFireArmyRs.EXT_FIELD_NUMBER, GetNewCrossArmyHandler.class);
        registerC(GetCrossWarFireCityInfoRq.EXT_FIELD_NUMBER, GetCrossWarFireCityInfoRs.EXT_FIELD_NUMBER, GetNewCrossCityInfoHandler.class);
        registerC(GetCrossWarFireMilitarySituationRq.EXT_FIELD_NUMBER, GetCrossWarFireMilitarySituationRs.EXT_FIELD_NUMBER, GetNewCrossMilitarySituationHandler.class);
        registerC(GetCrossGroupInfoRq.EXT_FIELD_NUMBER, GetCrossGroupInfoRs.EXT_FIELD_NUMBER, GetCrossGroupInfoHandler.class);
        registerC(NewCrossAccelerateArmyRq.EXT_FIELD_NUMBER, NewCrossAccelerateArmyRs.EXT_FIELD_NUMBER, NewCrossAccelerateArmyHandler.class);
        registerC(GetNewCrossMineRq.EXT_FIELD_NUMBER, GetNewCrossMineRs.EXT_FIELD_NUMBER, GetNewCrossMineHandler.class);
        registerC(ScoutCrossPosRq.EXT_FIELD_NUMBER, ScoutCrossPosRs.EXT_FIELD_NUMBER, ScoutCrossPosHandler.class);


        //??????????????????
        registerC(BuyCrossWarFireBuffRq.EXT_FIELD_NUMBER, BuyCrossWarFireBuffRs.EXT_FIELD_NUMBER, BuyCrossWarFireBuffHandler.class);
        registerC(GetCrossWarFireRanksRq.EXT_FIELD_NUMBER, GetCrossWarFireRanksRs.EXT_FIELD_NUMBER, GetCrossWarFireRanksHandler.class);
        registerC(GetCrossWarFireCityOccupyRq.EXT_FIELD_NUMBER, GetCrossWarFireCityOccupyRs.EXT_FIELD_NUMBER, GetCrossWarFireCityOccupyHandler.class);
        registerC(GetCrossWarFireCampSummaryRq.EXT_FIELD_NUMBER, GetCrossWarFireCampSummaryRs.EXT_FIELD_NUMBER, GetCrossWarFireCampSummaryHandler.class);
        registerC(GetCrossWarFirePlayerLiveRq.EXT_FIELD_NUMBER, GetCrossWarFirePlayerLiveRs.EXT_FIELD_NUMBER, GetCrossWarFirePlayerLiveHandler.class);
        registerC(RefreshGetCrossWarFirePlayerInfoRq.EXT_FIELD_NUMBER, RefreshGetCrossWarFirePlayerInfoRs.EXT_FIELD_NUMBER, RefreshGetPlayerInfoHandler.class);

        // ????????????
        registerC(GetChapterTaskRq.EXT_FIELD_NUMBER, GetChapterTaskRs.EXT_FIELD_NUMBER, GetChapterTaskHandler.class);
        registerC(GetChapterTaskAwardRq.EXT_FIELD_NUMBER, GetChapterTaskAwardRs.EXT_FIELD_NUMBER, GetChapterTaskAwardHandler.class);
        registerC(GetChapterAwardRq.EXT_FIELD_NUMBER, GetChapterAwardRs.EXT_FIELD_NUMBER, GetChapterAwardHandler.class);

        //????????????
        registerC(GetGamePlayChatRoomRq.EXT_FIELD_NUMBER, GetGamePlayChatRoomRs.EXT_FIELD_NUMBER, GetGamePlayChatRoomHandler.class);
        registerC(GetChatRoomMsgRq.EXT_FIELD_NUMBER, GetChatRoomMsgRs.EXT_FIELD_NUMBER, GetChatRoomMsgHandler.class);
        registerC(GetRoomPlayerShowRq.EXT_FIELD_NUMBER, GetRoomPlayerShowRs.EXT_FIELD_NUMBER, GetRoomPlayerShowHandler.class);
        registerC(GetCrossPlayerShowRq.EXT_FIELD_NUMBER, GetCrossPlayerShowRs.EXT_FIELD_NUMBER, GetCrossPlayerShowHandler.class);

        // ?????? ????????????
        registerC(GetDrawHeroCardRq.EXT_FIELD_NUMBER, GetDrawHeroCardRs.EXT_FIELD_NUMBER, GetDrawHeroCardHandler.class);
        registerC(DrawHeroCardRq.EXT_FIELD_NUMBER, DrawHeroCardRs.EXT_FIELD_NUMBER, DrawHeroCardHandler.class);
        registerC(ChooseNewWishHeroRq.EXT_FIELD_NUMBER, ChooseNewWishHeroRs.EXT_FIELD_NUMBER, ChooseWishedHeroHandler.class);
        registerC(ReceiveNewWishHeoRq.EXT_FIELD_NUMBER, ReceiveNewWishHeoRs.EXT_FIELD_NUMBER, ReceiveNewWishHeoHandler.class);
        registerC(GetAllHeroFragmentRq.EXT_FIELD_NUMBER, GetAllHeroFragmentRs.EXT_FIELD_NUMBER, GetAllHeroFragmentHandler.class);
        registerC(DrawActHeroCardRq.EXT_FIELD_NUMBER, DrawActHeroCardRs.EXT_FIELD_NUMBER, DrawActHeroCardHandler.class);
        registerC(GetDrawHeroCardActInfoRq.EXT_FIELD_NUMBER, GetDrawHeroCardActInfoRs.EXT_FIELD_NUMBER, GetDrawHeroCardActInfoHandler.class);
        registerC(GetDrawHeroCardPlanRq.EXT_FIELD_NUMBER, GetDrawHeroCardPlanRs.EXT_FIELD_NUMBER, GetDrawHeroCardPlanListHandler.class);
        registerC(ReceiveTimeLimitedDrawCountRq.EXT_FIELD_NUMBER, ReceiveTimeLimitedDrawCountRs.EXT_FIELD_NUMBER, ReceiveTimeLimitedDrawCountHandler.class);
        registerC(BuyOptionalBoxFromTimeLimitedDrawCardRq.EXT_FIELD_NUMBER, BuyOptionalBoxFromTimeLimitedDrawCardRs.EXT_FIELD_NUMBER, BuyOptionalBoxFromTimeLimitedDrawCardHandler.class);

        // ??????????????????
        registerC(GamePb5.GetHeroBiographyInfoRq.EXT_FIELD_NUMBER, GetHeroBiographyInfoRs.EXT_FIELD_NUMBER, GetHeroBiographyInfoHandler.class);
        registerC(GamePb5.UpgradeHeroBiographyRq.EXT_FIELD_NUMBER, UpgradeHeroBiographyRs.EXT_FIELD_NUMBER, UpgradeHeroBiographyHandler.class);

        // ????????????
        registerC(SearchBanditRq.EXT_FIELD_NUMBER, SearchBanditRs.EXT_FIELD_NUMBER, SearchBanditHandler.class);

        //?????? ????????????
        registerC(GetRelicDataInfoRq.EXT_FIELD_NUMBER, GetRelicDataInfoRs.EXT_FIELD_NUMBER, GetRelicDataInfoHandler.class);
        registerC(GetRelicDetailRq.EXT_FIELD_NUMBER, GetRelicDetailRs.EXT_FIELD_NUMBER, GetRelicDetailHandler.class);
        registerC(GetRelicScoreAwardRq.EXT_FIELD_NUMBER, GetRelicScoreAwardRs.EXT_FIELD_NUMBER, GetRelicScoreAwardHandler.class);

        // ?????????????????????
        registerC(RecordLifeSimulatorRq.EXT_FIELD_NUMBER, RecordLifeSimulatorRs.EXT_FIELD_NUMBER, RecordSimulatorHandler.class);
    }

    /**
     * ??????????????????Http????????????????????????handler
     */
    private void httpMessagePool() {
        // ???????????????
        registerH(HttpPb.RegisterRs.EXT_FIELD_NUMBER, 0, RegisterRsHandler.class);

        // ??????????????????
        registerH(HttpPb.VerifyRs.EXT_FIELD_NUMBER, BeginGameRs.EXT_FIELD_NUMBER, VerifyRsHandler.class);

        // GM??????
        registerH(DoSomeRq.EXT_FIELD_NUMBER, DoSomeRs.EXT_FIELD_NUMBER, GmHandler.class);

        // ?????????
        registerH(HttpPb.UseGiftCodeRs.EXT_FIELD_NUMBER, GiftCodeRs.EXT_FIELD_NUMBER, UseGiftCodeRsHandler.class);

        // ??????
        registerH(HttpPb.SendToMailRq.EXT_FIELD_NUMBER, HttpPb.SendToMailRs.EXT_FIELD_NUMBER,
                SendToMailRqHandler.class);

        // ????????????
        registerH(HttpPb.ForbiddenRq.EXT_FIELD_NUMBER, HttpPb.ForbiddenRs.EXT_FIELD_NUMBER, ForbiddenRqHandler.class);

        // ?????????
        registerH(HttpPb.ReloadParamRq.EXT_FIELD_NUMBER, HttpPb.ReloadParamRs.EXT_FIELD_NUMBER,
                ReloadParamRqHandler.class);

        // ????????????
        registerH(HttpPb.ShareRewardRs.EXT_FIELD_NUMBER, 0, ShareRewardRsHandler.class);
        // ??????????????????
        registerH(HttpPb.WeChatSignRewardRq.EXT_FIELD_NUMBER, 0, WeChatSignRewardHandler.class);

        // ??????
        registerH(HttpPb.NoticeRq.EXT_FIELD_NUMBER, HttpPb.NoticeRq.EXT_FIELD_NUMBER, NoticeRqHandler.class);

        // ??????????????????
        registerH(HttpPb.GetLordBaseRq.EXT_FIELD_NUMBER, 0, GetLordBaseRqHandler.class);
        // ??????????????????
        registerH(HttpPb.ModifyServerInfoRq.EXT_FIELD_NUMBER, 0, ModifyServerInfoHandler.class);

        // ??????VIP
        registerH(HttpPb.ModVipRq.EXT_FIELD_NUMBER, HttpPb.ModVipRq.EXT_FIELD_NUMBER, ModVipRqHandler.class);
        registerH(HttpPb.ModLordRq.EXT_FIELD_NUMBER, 0, ModLordRqHandler.class);
        registerH(HttpPb.ModPropRq.EXT_FIELD_NUMBER, 0, ModPropRqHandler.class);
        registerH(HttpPb.ModNameRq.EXT_FIELD_NUMBER, 0, ModNameRqHandler.class);

        // ???????????????????????????
        registerH(HttpPb.PayApplyRs.EXT_FIELD_NUMBER, 0, PayApplyRsHandler.class);
        registerH(HttpPb.PayBackRq.EXT_FIELD_NUMBER, 0, PayBackRqHandler.class);

        // ?????????
        registerH(HttpPb.RobotsExternalBehaviorRq.EXT_FIELD_NUMBER, 0, RobotsExternalBehaviorRqHandler.class);
        registerH(HttpPb.RobotsCountByAreaRq.EXT_FIELD_NUMBER, 0, RobotsCountByAreaRqHandler.class);

    }

    private void innerMessagePool() {
        // ?????????????????? ????????? -> ???????????? (??????????????????)
        registerI(HeartRs.EXT_FIELD_NUMBER, HeartHandler.class);
        registerI(CrossLoginRs.EXT_FIELD_NUMBER, CrossLoginHandler.class);
        registerI(CrossAwardOpRs.EXT_FIELD_NUMBER, CrossAwardOpHandler.class);
        registerI(SyncFortHeroRs.EXT_FIELD_NUMBER, SyncFortHeroHandler.class);
        registerI(SyncCrossWarFinishRs.EXT_FIELD_NUMBER, SyncCrossWarFinishHandler.class);
    }

    @Override
    public void registerC(int id, int rsCmd, Class<? extends AbsClientHandler> handlerClass) {
        if (handlerClass != null) {
            clientHandlers.put(id, handlerClass);
            rsMsgCmd.put(id, rsCmd);
        }
    }

    @Override
    public void registerH(int id, int rsCmd, Class<? extends AbsHttpHandler> handlerClass) {
        if (handlerClass != null) {
            serverHandlers.put(id, handlerClass);
            rsMsgCmd.put(id, rsCmd);
        }
    }

    @Override
    public void registerI(int id, Class<? extends AbsInnerHandler> handlerClass) {
        if (handlerClass != null) {
            innerHandlers.put(id, handlerClass);
        }
    }

    @Override
    public AbsClientHandler getClientHandler(int id) throws InstantiationException, IllegalAccessException {
        if (!clientHandlers.containsKey(id)) {
            return null;
        } else {
            AbsClientHandler handler = clientHandlers.get(id).newInstance();
            handler.setRsMsgCmd(rsMsgCmd.get(id));
            return handler;
        }
    }

    @Override
    public AbsHttpHandler getHttpHandler(int id) throws InstantiationException, IllegalAccessException {
        if (!serverHandlers.containsKey(id)) {
            return null;
        } else {
            AbsHttpHandler handler = serverHandlers.get(id).newInstance();
            handler.setRsMsgCmd(rsMsgCmd.get(id));
            return handler;
        }
    }

    @Override
    public AbsInnerHandler getInnerHandler(int id) throws InstantiationException, IllegalAccessException {
        if (!innerHandlers.containsKey(id)) {
            return new ClientRoutHandler(); // ????????????????????????????????????
        } else {
            AbsInnerHandler handler = innerHandlers.get(id).newInstance();
            return handler;
        }
    }

    @Override
    public void registerFunctionUnlock(int cmd, int functionId) {
        funcUnlockMsg.put(cmd, functionId);
    }

    /**
     * ???????????????????????????id???????????????????????????????????????
     *
     * @param beginCmd
     * @param endCmd
     * @param functionId
     */
    private void registerFunctionUnlockInRange(int beginCmd, int endCmd, int functionId) {
        if (endCmd < beginCmd) {
            return;
        }

        for (int cmd = beginCmd; cmd <= endCmd; cmd++) {
            if (rsMsgCmd.containsKey(cmd)) {// ????????????????????????????????????
                registerFunctionUnlock(cmd, functionId);
            }
        }
    }

    @Override
    public Integer getFunctionUnlockId(int cmd) {
        return funcUnlockMsg.get(cmd);
    }

    @Override
    public Integer getRsCmd(int rqCmd) {
        return rsMsgCmd.get(rqCmd);
    }

}
