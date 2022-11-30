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
import com.gryphpoem.game.zw.handler.client.buildHomeCity.ClearBanditHandler;
import com.gryphpoem.game.zw.handler.client.buildHomeCity.ExploreHandler;
import com.gryphpoem.game.zw.handler.client.buildHomeCity.PeaceAndWelfareHandler;
import com.gryphpoem.game.zw.handler.client.buildHomeCity.ReclaimHandler;
import com.gryphpoem.game.zw.handler.client.buildHomeCity.SwapBuildingPosHandler;
import com.gryphpoem.game.zw.handler.client.building.AssignEconomicCropHandler;
import com.gryphpoem.game.zw.handler.client.building.BuildingHanlder;
import com.gryphpoem.game.zw.handler.client.building.CommandAddHandler;
import com.gryphpoem.game.zw.handler.client.building.CreateBuildingHandler;
import com.gryphpoem.game.zw.handler.client.building.DesBuildingHanlder;
import com.gryphpoem.game.zw.handler.client.building.DispatchHeroHandler;
import com.gryphpoem.game.zw.handler.client.building.DispatchResidentHandler;
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
import com.gryphpoem.game.zw.handler.client.economicOrder.GetEconomicOrderHandler;
import com.gryphpoem.game.zw.handler.client.economicOrder.SubmitEconomicOrderHandler;
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
            // 注册与客户端交互相关协议
            clientMessagePool();

            // 与帐号服、后台交互相关协议
            httpMessagePool();

            // 游戏服、跨服 交互相关协议
            innerMessagePool();

            // 功能解锁相关协议注册
            functionUnlockMessagePool();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // @formatter:on

    /**
     * 注册需要检查功能是否解锁的协议
     */
    private void functionUnlockMessagePool() {
        // 世界功能解锁检查
        registerFunctionUnlockInRange(GetAreaRq.EXT_FIELD_NUMBER, AttackStateRq.EXT_FIELD_NUMBER,
                FunctionConstant.FUNC_ID_WORLD);
        registerFunctionUnlockInRange(GetMarchRq.EXT_FIELD_NUMBER, LeaveCityRq.EXT_FIELD_NUMBER,
                FunctionConstant.FUNC_ID_WORLD);
    }

    /**
     * 集中注册与客户端交互的协议与处理handler
     */
    private void clientMessagePool() {
        // 帐号相关协议

        // 客户端向游戏服务器请求所选服武器角色状态
        registerC(BeginGameRq.EXT_FIELD_NUMBER, BeginGameRs.EXT_FIELD_NUMBER, BeginGameHandler.class);
        // 获取随机名字
        registerC(GetNamesRq.EXT_FIELD_NUMBER, GetNamesRs.EXT_FIELD_NUMBER, GetNamesHandler.class);
        // 创建服务器角色
        registerC(CreateRoleRq.EXT_FIELD_NUMBER, CreateRoleRs.EXT_FIELD_NUMBER, CreateRoleHandler.class);
        // 角色登陆进入游戏
        registerC(RoleLoginRq.EXT_FIELD_NUMBER, RoleLoginRs.EXT_FIELD_NUMBER, RoleLoginHandler.class);
        // 获取玩家数据
        registerC(GetLordRq.EXT_FIELD_NUMBER, GetLordRs.EXT_FIELD_NUMBER, GetLordHandler.class);
        // 获取扩展的混合数据
        registerC(GetMixtureDataRq.EXT_FIELD_NUMBER, GetMixtureDataRs.EXT_FIELD_NUMBER, GetMixtureDataHandler.class);
        // 搜索玩家
        registerC(SeachPlayerRq.EXT_FIELD_NUMBER, SeachPlayerRs.EXT_FIELD_NUMBER, SeachPlayerHandler.class);
        // 设置玩家当前新手引导进度
        registerC(SetGuideRq.EXT_FIELD_NUMBER, SetGuideRs.EXT_FIELD_NUMBER, SetGuideHandler.class);
        // 使用兑换码
        registerC(GiftCodeRq.EXT_FIELD_NUMBER, GiftCodeRs.EXT_FIELD_NUMBER, GiftCodeHandler.class);
        // 获取离线收益
        registerC(OffLineIncomeRq.EXT_FIELD_NUMBER, OffLineIncomeRs.EXT_FIELD_NUMBER, GetOffLineIncome.class);
        // 获取加入社群奖励
        registerC(JoinCommunityRq.EXT_FIELD_NUMBER, JoinCommunityRs.EXT_FIELD_NUMBER, JoinCommunityHandler.class);
        // 切磋
        registerC(CompareNotesRq.EXT_FIELD_NUMBER, CompareNotesRs.EXT_FIELD_NUMBER, CompareNotesHandler.class);

        // 修改名字
        registerC(ChangeLordNameRq.EXT_FIELD_NUMBER, ChangeLordNameRs.EXT_FIELD_NUMBER, ChangeLordNameHandler.class);
        // 修改头像
        registerC(ChangePortraitRq.EXT_FIELD_NUMBER, ChangePortraitRs.EXT_FIELD_NUMBER, ChangePortraitHandler.class);
        // 修改个性签名
        registerC(ChangeSignatureRq.EXT_FIELD_NUMBER, ChangeSignatureRs.EXT_FIELD_NUMBER, ChangeSignatureHandler.class);
        // 修改拥有头像
        registerC(GetPortraitRq.EXT_FIELD_NUMBER, GetPortraitRs.EXT_FIELD_NUMBER, GetPortraitHandler.class);
        // 获取月卡
        registerC(GetMonthCardRq.EXT_FIELD_NUMBER, GetMonthCardRs.EXT_FIELD_NUMBER, GetMonthCardHandler.class);
        // 获取玩家聊天气泡框
//        registerC(GetChatBubbleRq.EXT_FIELD_NUMBER, GetChatBubbleRs.EXT_FIELD_NUMBER, GetChatBubbleHandler.class);
        // 修改聊天气泡框
//        registerC(ChangeChatBubbleRq.EXT_FIELD_NUMBER, ChangeChatBubbleRs.EXT_FIELD_NUMBER,ChangeChatBubbleHandler.class);
        // 获得形象
        registerC(GetBodyImageRq.EXT_FIELD_NUMBER, GetBodyImageRs.EXT_FIELD_NUMBER, GetBodyImageHandler.class);
        // 修改形象
        registerC(ChangeBodyImageRq.EXT_FIELD_NUMBER, ChangeBodyImageRs.EXT_FIELD_NUMBER, ChangeBodyImageHandler.class);
        // 获取城堡拥有皮肤
        registerC(GetCastleSkinRq.EXT_FIELD_NUMBER, GetCastleSkinRs.EXT_FIELD_NUMBER, GetCastleSkinHandler.class);
        // 更换城堡皮肤
        registerC(ChangeCastleSkinRq.EXT_FIELD_NUMBER, ChangeCastleSkinRs.EXT_FIELD_NUMBER, ChangeCastleSkinHandler.class);
        //城堡皮肤升星
        registerC(CastleSkinStarUpRq.EXT_FIELD_NUMBER, CastleSkinStarUpRs.EXT_FIELD_NUMBER, CastleSkinStarUpHandler.class);

        // 装扮相关协议
        registerC(GetDressUpDataRq.EXT_FIELD_NUMBER, GetDressUpDataRs.EXT_FIELD_NUMBER, GetDressUpDataHandler.class);
        registerC(ChangeDressUpRq.EXT_FIELD_NUMBER, ChangeDressUpRs.EXT_FIELD_NUMBER, ChangeDressUpHandler.class);


        // 公用模块协议

        // 获取服务器时间
        registerC(GetTimeRq.EXT_FIELD_NUMBER, GetTimeRs.EXT_FIELD_NUMBER, GetTimeHandler.class);
        // 执行GM指令，想干啥干啥
        registerC(DoSomeRq.EXT_FIELD_NUMBER, DoSomeRs.EXT_FIELD_NUMBER, DoSomeHandler.class);

        // 建筑相关协议
        // 建筑信息
        registerC(GetBuildingRq.EXT_FIELD_NUMBER, GetBuildingRs.EXT_FIELD_NUMBER, BuildingHanlder.class);
        // 建筑升级
        registerC(UpBuildingRq.EXT_FIELD_NUMBER, UpBuildingRs.EXT_FIELD_NUMBER, UpBuildingHanlder.class);
        // 建筑改建
        registerC(UptBuildingRq.EXT_FIELD_NUMBER, UptBuildingRs.EXT_FIELD_NUMBER, UptBuildingHanlder.class);
        // 建筑加速
        registerC(SpeedBuildingRq.EXT_FIELD_NUMBER, SpeedBuildingRs.EXT_FIELD_NUMBER, SpeedBuildingHandler.class);
        // 建筑拆除
        registerC(DesBuildingRq.EXT_FIELD_NUMBER, DesBuildingRs.EXT_FIELD_NUMBER, DesBuildingHanlder.class);
        // 建筑重建
        registerC(ReBuildRq.EXT_FIELD_NUMBER, ReBuildRs.EXT_FIELD_NUMBER, ReBuildHanlder.class);
        // 领取资源
        registerC(GainResRq.EXT_FIELD_NUMBER, GainResRs.EXT_FIELD_NUMBER, GainResHandler.class);
        // 司令部信息
        registerC(GetCommandRq.EXT_FIELD_NUMBER, GetCommandRs.EXT_FIELD_NUMBER, GetCommandHandler.class);
        // 司令部招募
        registerC(CommandAddRq.EXT_FIELD_NUMBER, CommandAddRs.EXT_FIELD_NUMBER, CommandAddHandler.class);
        // 兵营领取兵力
        registerC(AddArmRq.EXT_FIELD_NUMBER, AddArmRs.EXT_FIELD_NUMBER, AddArmHandler.class);
        // 兵营招募
        registerC(FactoryRecruitRq.EXT_FIELD_NUMBER, FactoryRecruitRs.EXT_FIELD_NUMBER, FactoryRecruitHandler.class);
        // 兵营界面
        registerC(GetFactoryRq.EXT_FIELD_NUMBER, GetFactoryRs.EXT_FIELD_NUMBER, GetFactoryHandler.class);
        // 兵营招募加时
        registerC(UpRecruitRq.EXT_FIELD_NUMBER, UpRecruitRs.EXT_FIELD_NUMBER, UpRecruitHandler.class);
        // 兵营扩建
        registerC(FactoryExpandRq.EXT_FIELD_NUMBER, FactoryExpandRs.EXT_FIELD_NUMBER, FactoryExpandHandler.class);
        // 兵营招募取消
        registerC(RecruitCancelRq.EXT_FIELD_NUMBER, RecruitCancelRs.EXT_FIELD_NUMBER, RecruitCancelHandler.class);
        // 兵营招募加速
        registerC(RecruitSpeedRq.EXT_FIELD_NUMBER, RecruitSpeedRs.EXT_FIELD_NUMBER, RecruitSpeedHandler.class);
        // 兵工厂信息
        registerC(GetEquipFactoryRq.EXT_FIELD_NUMBER, GetEquipFactoryRs.EXT_FIELD_NUMBER, GetEquipFactoryHandler.class);
        // 兵工厂雇佣
        registerC(EquipFactoryRecruitRq.EXT_FIELD_NUMBER, EquipFactoryRecruitRs.EXT_FIELD_NUMBER,
                EquipFactoryRecruitHandler.class);
        // 获取重建家园信息
        registerC(SyncRoleRebuildRq.EXT_FIELD_NUMBER, SyncRoleRebuildRs.EXT_FIELD_NUMBER, SyncRoleRebuildHanlder.class);
        // 领取重建家园奖励
        registerC(RebuildRewardRq.EXT_FIELD_NUMBER, RebuildRewardRs.EXT_FIELD_NUMBER, RebuildRewardHanlder.class);
        registerC(TrainingBuildingRq.EXT_FIELD_NUMBER, TrainingBuildingRs.EXT_FIELD_NUMBER,
                // 建造建筑(暂时只有训练中心,需要在拆除建筑之后)
                TrainingBuildingHandler.class);

        registerC(OnOffAutoBuildRq.EXT_FIELD_NUMBER, OnOffAutoBuildRs.EXT_FIELD_NUMBER, OnOffAutoBuildHandler.class);

        registerC(QuickBuyArmyRq.EXT_FIELD_NUMBER, QuickBuyArmyRs.EXT_FIELD_NUMBER, QuickBuyArmyHandler.class);

        // 城墙相关协议

        // 城墙
        registerC(GetWallRq.EXT_FIELD_NUMBER, GetWallRs.EXT_FIELD_NUMBER, GetWallHandler.class);
        // 城墙招募NPC
        registerC(WallNpcRq.EXT_FIELD_NUMBER, WallNpcRs.EXT_FIELD_NUMBER, WallNpcHandler.class);
        // 城墙NPC升级
        registerC(WallNpcLvUpRq.EXT_FIELD_NUMBER, WallNpcLvUpRs.EXT_FIELD_NUMBER, WallNpcLvUpHandler.class);
        // 城墙布置
        registerC(WallSetRq.EXT_FIELD_NUMBER, WallSetRs.EXT_FIELD_NUMBER, WallSetHandler.class);
        // 城墙驻防召回
        registerC(WallCallBackRq.EXT_FIELD_NUMBER, WallCallBackRs.EXT_FIELD_NUMBER, WallCallBackHandler.class);
        // 城墙驻防遣返
        registerC(WallGetOutRq.EXT_FIELD_NUMBER, WallGetOutRs.EXT_FIELD_NUMBER, WallGetOutHandler.class);
        // 城墙NPC更换兵种
        registerC(WallNpcArmyRq.EXT_FIELD_NUMBER, WallNpcArmyRs.EXT_FIELD_NUMBER, WallNpcArmyHandler.class);
        // 城墙驻防
        registerC(WallHelpRq.EXT_FIELD_NUMBER, WallHelpRs.EXT_FIELD_NUMBER, WallHelpHandler.class);
        // 城墙驻防召回
        registerC(WallCallBackRq.EXT_FIELD_NUMBER, WallCallBackRs.EXT_FIELD_NUMBER, WallCallBackHandler.class);
        // 城墙驻防遣返
        registerC(WallGetOutRq.EXT_FIELD_NUMBER, WallGetOutRs.EXT_FIELD_NUMBER, WallGetOutHandler.class);
        // 城墙NPC更换兵种
        registerC(WallNpcArmyRq.EXT_FIELD_NUMBER, WallNpcArmyRs.EXT_FIELD_NUMBER, WallNpcArmyHandler.class);
        // 城墙NPC开启自动补兵
        registerC(WallNpcAutoRq.EXT_FIELD_NUMBER, WallNpcAutoRs.EXT_FIELD_NUMBER, WallNpcAutoHandler.class);
        // 城墙花钱满兵
        registerC(WallNpcFullRq.EXT_FIELD_NUMBER, WallNpcFullRs.EXT_FIELD_NUMBER, WallNpcFullHandler.class);
        // 城墙驻防召回
        registerC(WallCallBackRq.EXT_FIELD_NUMBER, WallCallBackRs.EXT_FIELD_NUMBER, WallCallBackHandler.class);
        // 城墙驻防遣返
        registerC(WallGetOutRq.EXT_FIELD_NUMBER, WallGetOutRs.EXT_FIELD_NUMBER, WallGetOutHandler.class);
        // 城墙NPC更换兵种
        registerC(WallNpcArmyRq.EXT_FIELD_NUMBER, WallNpcArmyRs.EXT_FIELD_NUMBER, WallNpcArmyHandler.class);
        // 城墙NPC开启自动补兵
        registerC(WallNpcAutoRq.EXT_FIELD_NUMBER, WallNpcAutoRs.EXT_FIELD_NUMBER, WallNpcAutoHandler.class);
        // 城墙花钱满兵
        registerC(WallNpcFullRq.EXT_FIELD_NUMBER, WallNpcFullRs.EXT_FIELD_NUMBER, WallNpcFullHandler.class);
        // 城墙驻防信息
        registerC(WallHelpInfoRq.EXT_FIELD_NUMBER, WallHelpInfoRs.EXT_FIELD_NUMBER, WallHelpInfoHandler.class);
        // 修复城墙
        registerC(FixWallRq.EXT_FIELD_NUMBER, FixWallRs.EXT_FIELD_NUMBER, FixWallHandler.class);

        // 战争工厂(内阁)

        // 内阁采集将领布置
        registerC(AcqHeroSetRq.EXT_FIELD_NUMBER, AcqHeroSetRs.EXT_FIELD_NUMBER, AcqHeroSetHandler.class);
        // 内阁特将领布置
        registerC(ComandoHeroSetRq.EXT_FIELD_NUMBER, ComandoHeroSetRs.EXT_FIELD_NUMBER, CommandoHeroSetHandler.class);
        // 内阁获取天策府数据
        registerC(GetCabinetRq.EXT_FIELD_NUMBER, GetCabinetRs.EXT_FIELD_NUMBER, GetCabinetHandler.class);
        // 创建点兵统领
        registerC(CreateLeadRq.EXT_FIELD_NUMBER, CreateLeadRs.EXT_FIELD_NUMBER, CreateLeadHandler.class);
        // 完成当前点兵任务
        registerC(CabinetFinishRq.EXT_FIELD_NUMBER, CabinetFinishRs.EXT_FIELD_NUMBER, CabinetFinishHandler.class);
        // 完成当前点兵级别任务
        registerC(CabinetLvFinishRq.EXT_FIELD_NUMBER, CabinetLvFinishRs.EXT_FIELD_NUMBER, CabinetLvFinishHandler.class);

        // 情报部

        // 特工解锁
        registerC(UnlockAgentRq.EXT_FIELD_NUMBER, UnlockAgentRs.EXT_FIELD_NUMBER, UnlockAgentHandler.class);
        // 获取情报部相关信息
        registerC(GetCiaRq.EXT_FIELD_NUMBER, GetCiaRs.EXT_FIELD_NUMBER, GetCiaHandler.class);
        // 特工互动
        registerC(InteractionRq.EXT_FIELD_NUMBER, InteractionRs.EXT_FIELD_NUMBER, InteractionHandler.class);
        // 特工升星
        registerC(AgentUpgradeStarRq.EXT_FIELD_NUMBER, AgentUpgradeStarRs.EXT_FIELD_NUMBER, AgentUpgradeStarHandler.class);
        // 特工送礼
        registerC(PresentGiftRq.EXT_FIELD_NUMBER, PresentGiftRs.EXT_FIELD_NUMBER, PresentGiftHandler.class);
        // 特工约会
        registerC(AppointmentAgentRq.EXT_FIELD_NUMBER, AppointmentAgentRs.EXT_FIELD_NUMBER, AppointmentHandler.class);

        // 关卡

        // 获取关卡信息
        registerC(GetCombatRq.EXT_FIELD_NUMBER, GetCombatRs.EXT_FIELD_NUMBER, GetCombatHandler.class);
        // 关卡战斗
        registerC(DoCombatRq.EXT_FIELD_NUMBER, DoCombatRs.EXT_FIELD_NUMBER, DoCombatHandler.class);
        // 关卡扫荡
        registerC(DoCombatWipeRq.EXT_FIELD_NUMBER, DoCombatWipeRs.EXT_FIELD_NUMBER, DoCombatWipeHandler.class);
        // 资源副本次数购买
        registerC(BuyCombatRq.EXT_FIELD_NUMBER, BuyCombatRs.EXT_FIELD_NUMBER, BuyCombatHandler.class);
        // 排行榜
        registerC(GetRankRq.EXT_FIELD_NUMBER, GetRankRs.EXT_FIELD_NUMBER, GetRankHandler.class);
        // 获取自己的排名
        registerC(GetMyRankRq.EXT_FIELD_NUMBER, GetMyRankRq.EXT_FIELD_NUMBER, GetMyRankHandler.class);
        // 宝石副本次数购买
        registerC(BuyStoneCombatRq.EXT_FIELD_NUMBER, BuyStoneCombatRs.EXT_FIELD_NUMBER, BuyStoneCombatHandler.class);
        // 宝石副本挑战
        registerC(DoStoneCombatRq.EXT_FIELD_NUMBER, DoStoneCombatRs.EXT_FIELD_NUMBER, DoStoneCombatHandler.class);
        // 宝石副本挑战
        registerC(GetStoneCombatRq.EXT_FIELD_NUMBER, GetStoneCombatRs.EXT_FIELD_NUMBER, GetStoneCombatHandler.class);
        // 对宝石进阶
        registerC(DoStoneImproveRq.EXT_FIELD_NUMBER, DoStoneImproveRs.EXT_FIELD_NUMBER, DoStoneImproveHandler.class);
        // 进阶宝石升星
        registerC(StoneImproveUpLvRq.EXT_FIELD_NUMBER, StoneImproveUpLvRs.EXT_FIELD_NUMBER,
                StoneImproveUpLvHandler.class);
        // 获取荣耀演习场副本
        registerC(GetPitchCombatRq.EXT_FIELD_NUMBER, GetPitchCombatRs.EXT_FIELD_NUMBER, GetPitchCombatHandler.class);
        // 挑战荣耀演习场,与扫荡
        registerC(DoPitchCombatRq.EXT_FIELD_NUMBER, DoPitchCombatRs.EXT_FIELD_NUMBER, DoPitchCombatHandler.class);
        // 创建副本队伍
        registerC(CreateCombatTeamRq.EXT_FIELD_NUMBER, CreateCombatTeamRs.EXT_FIELD_NUMBER,
                CreateCombatTeamHandler.class);
        // 修改副本队伍信息(解散队伍,修改状态)
        registerC(ModifyCombatTeamRq.EXT_FIELD_NUMBER, ModifyCombatTeamRs.EXT_FIELD_NUMBER,
                ModifyCombatTeamHandler.class);
        // 快速加入队伍(没有可加入就创建队伍);普通加入队伍
        registerC(JoinCombatTeamRq.EXT_FIELD_NUMBER, JoinCombatTeamRs.EXT_FIELD_NUMBER, JoinCombatTeamHandler.class);
        // 离开队伍
        registerC(LeaveCombatTeamRq.EXT_FIELD_NUMBER, LeaveCombatTeamRs.EXT_FIELD_NUMBER, LeaveCombatTeamHandler.class);
        // 队长踢人
        registerC(TickTeamMemberRq.EXT_FIELD_NUMBER, TickTeamMemberRs.EXT_FIELD_NUMBER, TickTeamMemberHandler.class);
        // 获取多人副本信息
        registerC(GetMultCombatRq.EXT_FIELD_NUMBER, GetMultCombatRs.EXT_FIELD_NUMBER, GetMultCombatHandler.class);
        // 获取可选队员列表
        registerC(GetTeamMemberListRq.EXT_FIELD_NUMBER, GetTeamMemberListRs.EXT_FIELD_NUMBER,
                GetTeamMemberListHandler.class);
        // 发送邀请
        registerC(SendInvitationRq.EXT_FIELD_NUMBER, SendInvitationRs.EXT_FIELD_NUMBER, SendInvitationHandler.class);
        // 开始多人副本
        registerC(StartMultCombatRq.EXT_FIELD_NUMBER, StartMultCombatRs.EXT_FIELD_NUMBER, StartMultCombatHandler.class);
        // 多人副本商店购买
        registerC(MultCombatShopBuyRq.EXT_FIELD_NUMBER, MultCombatShopBuyRs.EXT_FIELD_NUMBER,
                MultCombatShopBuyHandler.class);
        // 多人副本扫荡
        registerC(WipeMultCombatRq.EXT_FIELD_NUMBER, WipeMultCombatRs.EXT_FIELD_NUMBER, WipeMultCombatHandler.class);

        // 将领相关协议

        // 获取所有将领
        registerC(GetHerosRq.EXT_FIELD_NUMBER, GetHerosRs.EXT_FIELD_NUMBER, GetHerosHandler.class);
        // 将领上阵
        registerC(HeroBattleRq.EXT_FIELD_NUMBER, HeroBattleRs.EXT_FIELD_NUMBER, HeroBattleHandler.class);
        // 将领快速升级
        registerC(HeroQuickUpRq.EXT_FIELD_NUMBER, HeroQuickUpRs.EXT_FIELD_NUMBER, HeroQuickUpHandler.class);
        // 将领直升一级
        registerC(HeroQuickUpLvRq.EXT_FIELD_NUMBER, HeroQuickUpLvRs.EXT_FIELD_NUMBER, HeroUpLvHandler.class);
        // 将领洗髓
        registerC(HeroWashRq.EXT_FIELD_NUMBER, HeroWashRs.EXT_FIELD_NUMBER, HeroWashHandler.class);
        // 保存将领洗髓
        registerC(SaveHeroWashRq.EXT_FIELD_NUMBER, SaveHeroWashRs.EXT_FIELD_NUMBER, SaveHeroWashHandler.class);
        // 将领突破
//        registerC(HeroBreakRq.EXT_FIELD_NUMBER, HeroBreakRs.EXT_FIELD_NUMBER, HeroBreakHandler.class);
        // 获取将领洗髓次数等信息
        registerC(GetHeroWashInfoRq.EXT_FIELD_NUMBER, GetHeroWashInfoRs.EXT_FIELD_NUMBER, GetHeroWashInfoHandler.class);
        // 将领换位置
        registerC(HeroPosSetRq.EXT_FIELD_NUMBER, HeroPosSetRs.EXT_FIELD_NUMBER, HeroPosSetHandler.class);
        // 自动补兵
        registerC(AutoAddArmyRq.EXT_FIELD_NUMBER, AutoAddArmyRs.EXT_FIELD_NUMBER, AutoAddArmyHandler.class);
        // 获取将领寻访信息
//        registerC(GetHeroSearchRq.EXT_FIELD_NUMBER, GetHeroSearchRs.EXT_FIELD_NUMBER, GetHeroSearchHandler.class);
        // 将领寻访
//        registerC(SearchHeroRq.EXT_FIELD_NUMBER, SearchHeroRs.EXT_FIELD_NUMBER, SearchHeroHandler.class);
        // 获取部分将领信息
        registerC(GetHeroByIdsRq.EXT_FIELD_NUMBER, GetHeroByIdsRs.EXT_FIELD_NUMBER, GetHeroByIdsHandler.class);
        // 获取上阵将领在其他地方的位置
        registerC(GetHeroBattlePosRq.EXT_FIELD_NUMBER, GetHeroBattlePosRs.EXT_FIELD_NUMBER,
                GetHeroBattlePosHandler.class);
        // 将领授勋
        registerC(HeroDecoratedRq.EXT_FIELD_NUMBER, HeroDecoratedRs.EXT_FIELD_NUMBER, HeroDecoratedHandler.class);
        // 将领觉醒相关功能
        registerC(GamePb5.StudyHeroTalentRq.EXT_FIELD_NUMBER, StudyHeroTalentRs.EXT_FIELD_NUMBER, StudyHeroTalentHandler.class);
        registerC(GamePb5.UpgradeHeroRq.EXT_FIELD_NUMBER, UpgradeHeroRs.EXT_FIELD_NUMBER, HeroUpgradeHandler.class);
        registerC(GamePb5.ExchangeHeroFragmentRq.EXT_FIELD_NUMBER, ExchangeHeroFragmentRs.EXT_FIELD_NUMBER, ExchangeHeroFragmentHandler.class);
        registerC(GamePb5.SynthesizingHeroFragmentsRq.EXT_FIELD_NUMBER, SynthesizingHeroFragmentsRs.EXT_FIELD_NUMBER, SynthesizingHeroFragmentsHandler.class);
        registerC(GamePb5.UpgradeHeroTalentRq.EXT_FIELD_NUMBER, UpgradeHeroTalentRs.EXT_FIELD_NUMBER, UpgradeHeroTalentHandler.class);

        // 战机相关协议
        registerC(GetWarPlanesRq.EXT_FIELD_NUMBER, GetWarPlanesRs.EXT_FIELD_NUMBER, GetWarPlanesHandler.class);
        registerC(GetPlaneByIdsRq.EXT_FIELD_NUMBER, GetPlaneByIdsRs.EXT_FIELD_NUMBER, GetPlaneByIdsHandler.class);
        registerC(PlaneSwapRq.EXT_FIELD_NUMBER, PlaneSwapRs.EXT_FIELD_NUMBER, PlaneSwapHandler.class);
        registerC(PlaneRemouldRq.EXT_FIELD_NUMBER, PlaneRemouldRs.EXT_FIELD_NUMBER, PlaneRemouldHandler.class);
        registerC(PlaneFactoryRq.EXT_FIELD_NUMBER, PlaneFactoryRs.EXT_FIELD_NUMBER, PlaneFactoryHandler.class);
        registerC(SearchPlaneRq.EXT_FIELD_NUMBER, SearchPlaneRs.EXT_FIELD_NUMBER, SearchPlaneHandler.class);
        registerC(GetSearchAwardRq.EXT_FIELD_NUMBER, GetSearchAwardRs.EXT_FIELD_NUMBER, GetSearchAwardHandler.class);
        registerC(SyntheticPlaneRq.EXT_FIELD_NUMBER, SyntheticPlaneRs.EXT_FIELD_NUMBER, SyntheticPlaneHandler.class);
        registerC(PlaneQuickUpRq.EXT_FIELD_NUMBER, PlaneQuickUpRs.EXT_FIELD_NUMBER, PlaneQuickUpHandler.class);

        // 道具、装备相关

        // 获取玩家背包的道具
        registerC(GetPropsRq.EXT_FIELD_NUMBER, GetPropsRs.EXT_FIELD_NUMBER, GetPropsHandler.class);
        // 获取玩家所有装备
        registerC(GetEquipsRq.EXT_FIELD_NUMBER, GetEquipsRs.EXT_FIELD_NUMBER, GetEquipsHandler.class);
        // 装备打造
        registerC(EquipForgeRq.EXT_FIELD_NUMBER, EquipForgeRs.EXT_FIELD_NUMBER, EquipForgeHandler.class);
        // 装备打造领取
        registerC(EquipGainRq.EXT_FIELD_NUMBER, EquipGainRs.EXT_FIELD_NUMBER, EquipGainHandler.class);
        // 装备打造加速
        registerC(SpeedForgeRq.EXT_FIELD_NUMBER, SpeedForgeRs.EXT_FIELD_NUMBER, SpeedForgeHandler.class);
        // 装备洗练
        registerC(EquipBaptizeRq.EXT_FIELD_NUMBER, EquipBaptizeRs.EXT_FIELD_NUMBER, EquipBaptizeHandler.class);
        // 装备分解
        registerC(EquipDecomposeRq.EXT_FIELD_NUMBER, EquipDecomposeRs.EXT_FIELD_NUMBER, EquipDecomposeHandler.class);
        // 装备上锁
        registerC(EquipLockedRq.EXT_FIELD_NUMBER, EquipLockedRs.EXT_FIELD_NUMBER, EquipLockedHandler.class);
        // 装备批量分解
        registerC(EquipBatchDecomposeRq.EXT_FIELD_NUMBER, EquipBatchDecomposeRs.EXT_FIELD_NUMBER, EquipBatchDecomposeHandler.class);
        // 穿戴、卸下装备
        registerC(OnEquipRq.EXT_FIELD_NUMBER, OnEquipRs.EXT_FIELD_NUMBER, OnEquipHandler.class);
        // 道具使用
        registerC(UsePropRq.EXT_FIELD_NUMBER, UsePropRs.EXT_FIELD_NUMBER, UsePropHandler.class);
        // 背包扩容
        registerC(BagExpandRq.EXT_FIELD_NUMBER, BagExpandRs.EXT_FIELD_NUMBER, BagExpandHandler.class);
        // 国器信息
        registerC(GetSuperEquipRq.EXT_FIELD_NUMBER, GetSuperEquipRs.EXT_FIELD_NUMBER, GetSuperEquipHandler.class);
        // 国器打造
        registerC(SuperEquipForgeRq.EXT_FIELD_NUMBER, SuperEquipForgeRs.EXT_FIELD_NUMBER, SuperEquipForgeHandler.class);
        // 国器升级
        registerC(UpSuperEquipRq.EXT_FIELD_NUMBER, UpSuperEquipRs.EXT_FIELD_NUMBER, UpSuperEquipHandler.class);
        // 国器加速
        registerC(SpeedSuperEquipRq.EXT_FIELD_NUMBER, SpeedSuperEquipRs.EXT_FIELD_NUMBER, SpeedSuperEquipHandler.class);
        // 道具购买
        registerC(BuyPropRq.EXT_FIELD_NUMBER, BuyPropRs.EXT_FIELD_NUMBER, BuyPropHandler.class);
        // 国器进阶
        registerC(GrowSuperEquipRq.EXT_FIELD_NUMBER, GrowSuperEquipRs.EXT_FIELD_NUMBER, GrowSuperEquipHandler.class);
        // 建造队列购买
        registerC(BuyBuildRq.EXT_FIELD_NUMBER, BuyBuildRs.EXT_FIELD_NUMBER, BuyBuildHandler.class);
        // 戒指强化
        registerC(RingUpLvRq.EXT_FIELD_NUMBER, RingUpLvRs.EXT_FIELD_NUMBER, RingUpLvHandler.class);
        // 镶嵌卸下宝石
        registerC(InlaidJewelRq.EXT_FIELD_NUMBER, InlaidJewelRs.EXT_FIELD_NUMBER, InlaidJewelHandler.class);
        // 获取所有装备宝石
        registerC(GetJewelsRq.EXT_FIELD_NUMBER, GetJewelsRs.EXT_FIELD_NUMBER, GetJewelsHandler.class);
        // 进阶分解宝石
        registerC(DoJewelImproveRq.EXT_FIELD_NUMBER, DoJewelImproveRs.EXT_FIELD_NUMBER, DoJewelImproveHandler.class);

        // 获取是宝石相关信息
        registerC(GetStoneInfoRq.EXT_FIELD_NUMBER, GetStoneInfoRs.EXT_FIELD_NUMBER, GetStoneInfoHandler.class);
        // 宝石镶嵌
        registerC(StoneMountingRq.EXT_FIELD_NUMBER, StoneMountingRs.EXT_FIELD_NUMBER, StoneMountingHandler.class);
        // 宝石升级
        registerC(StoneUpLvRq.EXT_FIELD_NUMBER, StoneUpLvRs.EXT_FIELD_NUMBER, StoneUpLvHandler.class);

        // 合成道具
        registerC(SyntheticPropRq.EXT_FIELD_NUMBER, SyntheticPropRs.EXT_FIELD_NUMBER, SyntheticPropHandler.class);

        // 勋章相关
        // 获取玩家所有勋章
        registerC(GetMedalsRq.EXT_FIELD_NUMBER, GetMedalsRs.EXT_FIELD_NUMBER, GetMedalsHandler.class);
        // 获取指定将领的勋章
        registerC(GetHeroMedalRq.EXT_FIELD_NUMBER, GetHeroMedalRs.EXT_FIELD_NUMBER, GetHeroMedalHandler.class);
        // 给勋章上下锁
        registerC(MedalLockRq.EXT_FIELD_NUMBER, MedalLockRs.EXT_FIELD_NUMBER, MedalLockHandler.class);
        // 将领穿戴或更换勋章
        registerC(UptHeroMedalRq.EXT_FIELD_NUMBER, UptHeroMedalRs.EXT_FIELD_NUMBER, UptHeroMedalHandler.class);
        // 获取当前拥有的荣誉点数和金条数
        registerC(GetHonorGoldBarRq.EXT_FIELD_NUMBER, GetHonorGoldBarRs.EXT_FIELD_NUMBER, GetHonorGoldBarHandler.class);
        // 获取勋章商店的商品
        registerC(GetMedalGoodsRq.EXT_FIELD_NUMBER, GetMedalGoodsRs.EXT_FIELD_NUMBER, GetMedalGoodsHandler.class);
        // 购买勋章
        registerC(BuyMedalRq.EXT_FIELD_NUMBER, BuyMedalRs.EXT_FIELD_NUMBER, BuyMedalHandler.class);
        // 勋章强化
        registerC(IntensifyMedalRq.EXT_FIELD_NUMBER, IntensifyMedalRs.EXT_FIELD_NUMBER, IntensifyMedalHandler.class);
        // 勋章捐献
        registerC(DonateMedalRq.EXT_FIELD_NUMBER, DonateMedalRs.EXT_FIELD_NUMBER, DonateMedalHandler.class);
        // 购买荣誉
        registerC(BuyHonorRq.EXT_FIELD_NUMBER, BuyHonorRs.EXT_FIELD_NUMBER, BuyHonorHandler.class);

        // 世界相关
        // 获取某个行政区域的数据
        registerC(GetAreaRq.EXT_FIELD_NUMBER, GetAreaRs.EXT_FIELD_NUMBER, GetAreaHandler.class);
        // 获取地图中某个区块的数据
        registerC(GetMapRq.EXT_FIELD_NUMBER, GetMapRs.EXT_FIELD_NUMBER, GetMapHandler.class);
        // 攻击某个坐标的势力（包括玩家和流寇），采集
        registerC(AttackPosRq.EXT_FIELD_NUMBER, AttackPosRs.EXT_FIELD_NUMBER, AttackPosHandler.class);
        // 撤回部队
        registerC(RetreatRq.EXT_FIELD_NUMBER, RetreatRs.EXT_FIELD_NUMBER, RetreatHandler.class);
        // 获取城战或国战详情
        registerC(GetBattleRq.EXT_FIELD_NUMBER, GetBattleRs.EXT_FIELD_NUMBER, GetBattleHandler.class);
        // 获取根据id获取战斗详情
        registerC(GetBattleByIdRq.EXT_FIELD_NUMBER, GetBattleByIdRs.EXT_FIELD_NUMBER, GetBattleByIdHandler.class);
        // 加入城战（攻击玩家）或国战
        registerC(JoinBattleRq.EXT_FIELD_NUMBER, JoinBattleRs.EXT_FIELD_NUMBER, JoinBattleHandler.class);
        // 发起国战（攻击其他国家城池）
        registerC(AttackStateRq.EXT_FIELD_NUMBER, AttackStateRs.EXT_FIELD_NUMBER, AttackStateHandler.class);
        // 获取区域内的行军路线
        registerC(GetMarchRq.EXT_FIELD_NUMBER, GetMarchRs.EXT_FIELD_NUMBER, GetMarchHandler.class);
        // 获取矿点采集详情
        registerC(GetMineRq.EXT_FIELD_NUMBER, GetMineRs.EXT_FIELD_NUMBER, GetMineHandler.class);
        // 玩家迁城
        registerC(MoveCityRq.EXT_FIELD_NUMBER, MoveCityRs.EXT_FIELD_NUMBER, MoveCityHandler.class);
        // 获取阵营战信息
        registerC(GetCampBattleRq.EXT_FIELD_NUMBER, GetCampBattleRs.EXT_FIELD_NUMBER, GetCampBattleHandler.class);
        // 获取阵营城征收
        registerC(GetCampCityLevyRq.EXT_FIELD_NUMBER, GetCampCityLevyRs.EXT_FIELD_NUMBER, GetCampLevyHandler.class);
        // 侦查
        registerC(ScoutPosRq.EXT_FIELD_NUMBER, ScoutPosRs.EXT_FIELD_NUMBER, ScoutPosHandler.class);
        // 清除CD时间
        registerC(ClearCDRq.EXT_FIELD_NUMBER, ClearCDRs.EXT_FIELD_NUMBER, ClearCDHandler.class);
        // 城池征收
        registerC(CityLevyRq.EXT_FIELD_NUMBER, CityLevyRs.EXT_FIELD_NUMBER, CityLevyHandler.class);
        // 城池重建
        registerC(CityRebuildRq.EXT_FIELD_NUMBER, CityRebuildRs.EXT_FIELD_NUMBER, CityRebuildHandler.class);
        // 城池修复
        registerC(CityRepairRq.EXT_FIELD_NUMBER, CityRepairRs.EXT_FIELD_NUMBER, CityRepairHandler.class);
        // 城池名称修改
        registerC(CityRenameRq.EXT_FIELD_NUMBER, CityRenameRs.EXT_FIELD_NUMBER, CityRenameHandler.class);
        // 行军加速
        registerC(MoveCDRq.EXT_FIELD_NUMBER, MoveCDRs.EXT_FIELD_NUMBER, MoveCDHandler.class);
        // 城主撤离城池
        registerC(LeaveCityRq.EXT_FIELD_NUMBER, LeaveCityRs.EXT_FIELD_NUMBER, LeaveCityHandler.class);
        // 城池开发
        registerC(UpCityRq.EXT_FIELD_NUMBER, UpCityRs.EXT_FIELD_NUMBER, UpCityHandler.class);
        // 进入世界/返回基地
        registerC(EnterWorldRq.EXT_FIELD_NUMBER, EnterWorldRs.EXT_FIELD_NUMBER, EnterWorldHandler.class);
        // 客户端当前屏幕所在区域
        registerC(ScreenAreaFocusRq.EXT_FIELD_NUMBER, ScreenAreaFocusRs.EXT_FIELD_NUMBER, ScreenAreaFocusHandler.class);
        // 获取被攻击的情报
        registerC(AttackRolesRq.EXT_FIELD_NUMBER, AttackRolesRs.EXT_FIELD_NUMBER, AttackRolesHandler.class);
        // 获取城池竞选信息
        registerC(GetCityCampaignRq.EXT_FIELD_NUMBER, GetCityCampaignRs.EXT_FIELD_NUMBER, GetCityCampaignHandler.class);
        // 获取城池信息
        registerC(GetCityRq.EXT_FIELD_NUMBER, GetCityRs.EXT_FIELD_NUMBER, GetCityHandler.class);
        // 获取当前节气
        registerC(GetSolarTermsRq.EXT_FIELD_NUMBER, GetSolarTermsRs.EXT_FIELD_NUMBER, GetSolarTermsHandler.class);
        // 获取夜袭相关信息
        registerC(GetNightRaidInfoRq.EXT_FIELD_NUMBER, GetNightRaidInfoRs.EXT_FIELD_NUMBER,
                GetNightRaidInfoHandler.class);
        // 召唤盖世太保
        registerC(SummonGestapoRq.EXT_FIELD_NUMBER, SummonGestapoRs.EXT_FIELD_NUMBER, SummonGestapoHandler.class);
        // 发起进攻盖世太保
        registerC(AttackGestapoRq.EXT_FIELD_NUMBER, AttackGestapoRs.EXT_FIELD_NUMBER, AttackGestapoHandler.class);
        // 加入盖世太保战斗
        registerC(JoinGestapoBattleRq.EXT_FIELD_NUMBER, JoinGestapoBattleRs.EXT_FIELD_NUMBER,
                JoinGestapoBattleHandler.class);
        // 获取每个区域中心城池状态
        registerC(GetAreaCentreCityRq.EXT_FIELD_NUMBER, GetAreaCentreCityRs.EXT_FIELD_NUMBER,
                GetAreaCentreCityHandler.class);
        // 获取城池首杀显示数据
        registerC(GetCityFirstKillRq.EXT_FIELD_NUMBER, GetCityFirstKillRs.EXT_FIELD_NUMBER,
                GetCityFirstKillHandler.class);
        // 加入闪电战
        registerC(JoinLightningWarBattleRq.EXT_FIELD_NUMBER, JoinLightningWarBattleRs.EXT_FIELD_NUMBER,
                JoinLightningWarBattleHandler.class);
        // 获取当前区域的闪电战信息
        registerC(GetLightningWarRq.EXT_FIELD_NUMBER, GetLightningWarRs.EXT_FIELD_NUMBER, GetLightningWarHandler.class);
        // 获取所有闪电战信息
        registerC(GetAllLightningWarListRq.EXT_FIELD_NUMBER, GetAllLightningWarListRs.EXT_FIELD_NUMBER,
                GetAllLightningWarListHandler.class);
        // 获取盖世太保排行榜阵营信息
        registerC(GestapoKillCampRankRq.EXT_FIELD_NUMBER, GestapoKillCampRankRs.EXT_FIELD_NUMBER,
                GestapoKillCampRankHandler.class);

        // 获取荣耀日报
        registerC(GetHonorReportsRq.EXT_FIELD_NUMBER, GetHonorReportsRs.EXT_FIELD_NUMBER, HonorReportsHandler.class);

        // 超级矿点相关

        // 获取超级矿点信息
        registerC(GetSuperMineRq.EXT_FIELD_NUMBER, GetSuperMineRs.EXT_FIELD_NUMBER, GetSuperMineHandler.class);
        // 超级矿点采集,驻防,攻打
        registerC(AttackSuperMineRq.EXT_FIELD_NUMBER, AttackSuperMineRs.EXT_FIELD_NUMBER, AttackSuperMineHandler.class);

        // 飞艇相关

        // 获取飞艇列表
        registerC(GetAirshipListRq.EXT_FIELD_NUMBER, GetAirshipListRs.EXT_FIELD_NUMBER, GetAirshipListHandler.class);
        // 攻击飞艇
        registerC(AttackAirshipRq.EXT_FIELD_NUMBER, AttackAirshipRs.EXT_FIELD_NUMBER, AttackAirshipHandler.class);
        // 获取飞艇活动信息
        registerC(GetAirshipInfoRq.EXT_FIELD_NUMBER, GetAirshipInfoRs.EXT_FIELD_NUMBER, GetAirshipInfoHandler.class);

        // 匪军叛乱

        // 获取自己的匪军叛乱buff
        registerC(GetRebelBuffRq.EXT_FIELD_NUMBER, GetRebelBuffRs.EXT_FIELD_NUMBER, GetRebelBuffHandler.class);
        // 获取匪军叛乱信息
        registerC(GetRebellionRq.EXT_FIELD_NUMBER, GetRebellionRs.EXT_FIELD_NUMBER, GetRebellionHandler.class);
        // 购买匪军叛乱buff
        registerC(BuyRebelBuffRq.EXT_FIELD_NUMBER, BuyRebelBuffRs.EXT_FIELD_NUMBER, BuyRebelBuffHandler.class);
        // 购买匪军叛乱物品
        registerC(BuyRebelShopRq.EXT_FIELD_NUMBER, BuyRebelShopRs.EXT_FIELD_NUMBER, BuyRebelShopHandler.class);

        // 队伍相关

        // 获取行军队列
        registerC(GetArmyRq.EXT_FIELD_NUMBER, GetArmyRs.EXT_FIELD_NUMBER, GetArmyHandler.class);
        // 补兵
        registerC(ReplenishRq.EXT_FIELD_NUMBER, ReplenishRs.EXT_FIELD_NUMBER, ReplenishHandler.class);

        // 召唤相关
        // 发起集结入口
        registerC(InitiateGatherEntranceRq.EXT_FIELD_NUMBER, InitiateGatherEntranceRs.EXT_FIELD_NUMBER, InitiateGatherEntranceHandler.class);
        // 召唤队友
        registerC(SummonTeamRq.EXT_FIELD_NUMBER, SummonTeamRs.EXT_FIELD_NUMBER, SummonTeamHandler.class);
        // 召唤响应
        registerC(SummonRespondRq.EXT_FIELD_NUMBER, SummonRespondRs.EXT_FIELD_NUMBER, SummonRespondHandler.class);
        // 获取召唤信息
        registerC(GetSummonRq.EXT_FIELD_NUMBER, GetSummonRs.EXT_FIELD_NUMBER, GetSummonHandler.class);

        // 邮件相关

        // 获取邮件列表
        registerC(GetMailListRq.EXT_FIELD_NUMBER, GetMailListRs.EXT_FIELD_NUMBER, GetMailListHandler.class);
        // 根据邮件id获取邮件内容
        registerC(GetMailByIdRq.EXT_FIELD_NUMBER, GetMailByIdRs.EXT_FIELD_NUMBER, GetMailByIdHandler.class);
        // 领取邮件奖励
        registerC(RewardMailRq.EXT_FIELD_NUMBER, RewardMailRs.EXT_FIELD_NUMBER, RewardMailHandler.class);
        // 设置邮件全部已读
        registerC(ReadAllMailRq.EXT_FIELD_NUMBER, ReadAllMailRs.EXT_FIELD_NUMBER, ReadAllMailHandler.class);
        // 删除邮件
        registerC(DelMailRq.EXT_FIELD_NUMBER, DelMailRs.EXT_FIELD_NUMBER, DelMailHandler.class);
        // 发送邮件
        registerC(SendMailRq.EXT_FIELD_NUMBER, SendMailRs.EXT_FIELD_NUMBER, SendMailHandler.class);
        // 获取玩家分享的邮件信息
        registerC(GetShareMailRq.EXT_FIELD_NUMBER, GetShareMailRs.EXT_FIELD_NUMBER, GetShareMailHandler.class);
        // 获取玩家分享的邮件信息
        registerC(LockMailRq.EXT_FIELD_NUMBER, LockMailRs.EXT_FIELD_NUMBER, LockMailHandler.class);

        // 阵营邮件相关

        // 发送阵营邮件
        registerC(SendCampMailRq.EXT_FIELD_NUMBER, SendCampMailRs.EXT_FIELD_NUMBER, SendCampMailHandler.class);

        // 聊天相关

        // 获取最近的聊天记录
        registerC(GetChatRq.EXT_FIELD_NUMBER, GetChatRs.EXT_FIELD_NUMBER, GetChatHandler.class);
        // 发送聊天
        registerC(SendChatRq.EXT_FIELD_NUMBER, SendChatRs.EXT_FIELD_NUMBER, SendChatHandler.class);
        // 分享战报
        registerC(ShareReportRq.EXT_FIELD_NUMBER, ShareReportRs.EXT_FIELD_NUMBER, ShareReportHandler.class);
        // 获取私聊消息
        registerC(GetPrivateChatRq.EXT_FIELD_NUMBER, GetPrivateChatRs.EXT_FIELD_NUMBER, GetPrivateChatHandler.class);
        // 获取会话
        registerC(GetDialogRq.EXT_FIELD_NUMBER, GetDialogRs.EXT_FIELD_NUMBER, GetDialogHandler.class);
        // 删除会话
        registerC(DelDialogRq.EXT_FIELD_NUMBER, DelDialogRs.EXT_FIELD_NUMBER, DelDialogHandler.class);
        // 已读会话
        registerC(ReadDialogRq.EXT_FIELD_NUMBER, ReadDialogRs.EXT_FIELD_NUMBER, ReadDialogHandler.class);
        // 领取红包
        registerC(AcceptRedPacketRq.EXT_FIELD_NUMBER, AcceptRedPacketRs.EXT_FIELD_NUMBER, AcceptRedPacketHandler.class);
        // 获取红包详情
        registerC(GetRedPacketListRq.EXT_FIELD_NUMBER, GetRedPacketListRs.EXT_FIELD_NUMBER,
                GetRedPacketListRqHandler.class);
        // 获取红包详情
        registerC(GetRedPacketRq.EXT_FIELD_NUMBER, GetRedPacketRs.EXT_FIELD_NUMBER, GetRedPacketHandler.class);
        // 获取最新的推送消息
        registerC(GetFmsGelTunChatsRq.EXT_FIELD_NUMBER, GetFmsGelTunChatsRs.EXT_FIELD_NUMBER,
                GetFmsGelTunChatsHandler.class);
        // 获取最近的活动消息记录
        registerC(GetActivityChatRq.EXT_FIELD_NUMBER, GetActivityChatRs.EXT_FIELD_NUMBER, GetActivityChatHandler.class);

        // 任务

        // 获取军团任务
        registerC(GetPartyTaskRq.EXT_FIELD_NUMBER, GetPartyTaskRs.EXT_FIELD_NUMBER, GetPartyTaskHandler.class);
        // 军团任务领奖
        registerC(PartyTaskAwardRq.EXT_FIELD_NUMBER, PartyTaskAwardRs.EXT_FIELD_NUMBER, PartyTaskAwardHandler.class);
        // 七日活动界面
        registerC(GetDay7ActRq.EXT_FIELD_NUMBER, GetDay7ActRs.EXT_FIELD_NUMBER, GetDay7ActHandler.class);
        // 活动界面
        registerC(GetActivityListRq.EXT_FIELD_NUMBER, GetActivityListRs.EXT_FIELD_NUMBER, GetActivityListHandler.class);
        // 获取某个活动列表
        registerC(GetActivityRq.EXT_FIELD_NUMBER, GetActivityRs.EXT_FIELD_NUMBER, GetActivityHandler.class);
        // 领取七日活动奖励
        registerC(RecvDay7ActAwardRq.EXT_FIELD_NUMBER, RecvDay7ActAwardRs.EXT_FIELD_NUMBER,
                RecvDay7ActAwardHandler.class);
        // 领取普通活动奖励
        registerC(GetActivityAwardRq.EXT_FIELD_NUMBER, GetActivityAwardRs.EXT_FIELD_NUMBER,
                GetActivityAwardHandler.class);
        // 兑换活动奖励
        registerC(ExchangeActAwardRq.EXT_FIELD_NUMBER, ExchangeActAwardRs.EXT_FIELD_NUMBER,
                ExchangeActAwardHandler.class);
        // 领取在线活动奖励
        registerC(GetOnLineAwardRq.EXT_FIELD_NUMBER, GetOnLineAwardRs.EXT_FIELD_NUMBER, GetOnLineAwardHandler.class);
        // 获取触发式礼包
        registerC(GetTriggerGiftRq.EXT_FIELD_NUMBER, GetTriggerGiftRs.EXT_FIELD_NUMBER, GetTriggerGiftHandler.class);
        // 金币购买触发式礼包
        registerC(TriggerGiftBuyRq.EXT_FIELD_NUMBER, TriggerGiftBuyRs.EXT_FIELD_NUMBER, TriggerGIftBuyHandler.class);
        // 金币购买军备促销礼包
        registerC(PromotionPropBuyRq.EXT_FIELD_NUMBER, PromotionPropBuyRs.EXT_FIELD_NUMBER,
                PromotionGiftBuyHandler.class);
        // 世界任务信息
        registerC(GetWorldTaskRq.EXT_FIELD_NUMBER, GetWorldTaskRs.EXT_FIELD_NUMBER, GetWorlTaskHandler.class);
        // 领取世界任务奖励
        registerC(GainWorldTaskRq.EXT_FIELD_NUMBER, GainWorldTaskRs.EXT_FIELD_NUMBER, GainWorldTaskHandler.class);
        // 打世界boss
        registerC(AtkWorldBossRq.EXT_FIELD_NUMBER, AtkWorldBossRs.EXT_FIELD_NUMBER, AtkWorldBossHandler.class);
        // 获取黑鹰计划活动
        registerC(GetActBlackhawkRq.EXT_FIELD_NUMBER, GetActBlackhawkRs.EXT_FIELD_NUMBER, GetActBlackhawkHandler.class);
        // 购买黑鹰计划物品
        registerC(BlackhawkBuyRq.EXT_FIELD_NUMBER, BlackhawkBuyRs.EXT_FIELD_NUMBER, BlackhawkBuyHandler.class);
        // 黑鹰计划刷新
        registerC(BlackhawkRefreshRq.EXT_FIELD_NUMBER, BlackhawkRefreshRs.EXT_FIELD_NUMBER,
                BlackhawkRefreshHandler.class);
        // 黑鹰计划
        registerC(BlackhawkHeroRq.EXT_FIELD_NUMBER, BlackhawkHeroRs.EXT_FIELD_NUMBER, BlackhawkHeroHandler.class);
        // 招募将领
        // 空降补给购买
        registerC(SupplyDorpBuyRq.EXT_FIELD_NUMBER, SupplyDorpBuyRs.EXT_FIELD_NUMBER, SupplyDorpBuyHandler.class);
        // 空降补给获取
        registerC(GetSupplyDorpRq.EXT_FIELD_NUMBER, GetSupplyDorpRs.EXT_FIELD_NUMBER, GetSupplyDorpHandler.class);
        // 空降补给领取奖励
        registerC(SupplyDorpAwardRq.EXT_FIELD_NUMBER, SupplyDorpAwardRs.EXT_FIELD_NUMBER, SupplyDorpAwardHandler.class);
        // 体力赠送活动
        registerC(GetPowerGiveDataRq.EXT_FIELD_NUMBER, GetPowerGiveDataRs.EXT_FIELD_NUMBER,
                GetPowerGiveDataHandler.class);
        // 体力赠送活动
        registerC(GetFreePowerRq.EXT_FIELD_NUMBER, GetFreePowerRs.EXT_FIELD_NUMBER, GetFreePowerHandler.class);
        // 购买成长计划
        registerC(ActGrowBuyRq.EXT_FIELD_NUMBER, ActGrowBuyRs.EXT_FIELD_NUMBER, ActGrowBuyHandler.class);
        // 特价礼包
        registerC(GiftShowRq.EXT_FIELD_NUMBER, GiftShowRs.EXT_FIELD_NUMBER, GiftShowHandler.class);
        // 获取排行活动
        registerC(GetActRankRq.EXT_FIELD_NUMBER, GetActRankRs.EXT_FIELD_NUMBER, GetActRankHandler.class);
        // 获取排行活动
        registerC(GetDisplayActListRq.EXT_FIELD_NUMBER, GetDisplayActListRs.EXT_FIELD_NUMBER,
                GetDisplayActListHandler.class);
        // 攻城掠地活动显示数据
        registerC(GetAtkCityActRq.EXT_FIELD_NUMBER, GetAtkCityActRs.EXT_FIELD_NUMBER, GetAtkCityActHandler.class);
        // 领取目标任务的活跃度
        registerC(RecvActiveRq.EXT_FIELD_NUMBER, RecvActiveRs.EXT_FIELD_NUMBER, RecvActiveHandler.class);
        // 获取日常任务
        registerC(GetDailyTaskRq.EXT_FIELD_NUMBER, GetDailyTaskRs.EXT_FIELD_NUMBER, GetDailyTaskHandler.class);
        // 日常活跃度领奖
        registerC(DailyAwardRq.EXT_FIELD_NUMBER, DailyAwardRs.EXT_FIELD_NUMBER, DailyAwardHandler.class);
        // 日常任务活跃度领取
        registerC(LivenssAwardRq.EXT_FIELD_NUMBER, LivenssAwardRs.EXT_FIELD_NUMBER, LivenssAwardHandler.class);
        // 幸运转盘抽奖
        registerC(LuckyTurnplateRq.EXT_FIELD_NUMBER, LuckyTurnplateRs.EXT_FIELD_NUMBER, LuckyTurnplateHandler.class);
        // 转盘次数领奖
        registerC(TurnplatCntAwardRq.EXT_FIELD_NUMBER, TurnplatCntAwardRs.EXT_FIELD_NUMBER, TurnplatCntAwardHandler.class);
        // 获取幸运转盘的信息
        registerC(GetActTurnplatRq.EXT_FIELD_NUMBER, GetActTurnplatRs.EXT_FIELD_NUMBER, GetActTurnplatHandler.class);
        // 装备转盘抽奖
        registerC(EquipTurnplateRq.EXT_FIELD_NUMBER, EquipTurnplateRs.EXT_FIELD_NUMBER, EquipTurnplateHandler.class);
        // 获取装备转盘的信息
        registerC(GetEquipTurnplatRq.EXT_FIELD_NUMBER, GetEquipTurnplatRs.EXT_FIELD_NUMBER,
                GetEquipTurnplatHandler.class);
        // 获取充值转盘
        registerC(GetPayTurnplateRq.EXT_FIELD_NUMBER, GetPayTurnplateRs.EXT_FIELD_NUMBER, GetPayTurnplateHandler.class);
        // 充值转盘抽奖
        registerC(PlayPayTurnplateRq.EXT_FIELD_NUMBER, PlayPayTurnplateRs.EXT_FIELD_NUMBER,
                PlayPayTurnplateHandler.class);
        // 获取矿石转盘
        registerC(GetOreTurnplateRq.EXT_FIELD_NUMBER, GetOreTurnplateRs.EXT_FIELD_NUMBER, GetOreTurnplateHandler.class);
        // 矿石转盘抽奖
        registerC(PlayOreTurnplateRq.EXT_FIELD_NUMBER, PlayOreTurnplateRs.EXT_FIELD_NUMBER,
                PlayOreTurnplateHandler.class);
        // 获取矿石转盘-新
        registerC(GetOreTurnplateNewRq.EXT_FIELD_NUMBER, GetOreTurnplateNewRs.EXT_FIELD_NUMBER,
                GetOreTurnplateNewHandler.class);
        // 矿石转盘抽奖-新
        registerC(PlayOreTurnplateNewRq.EXT_FIELD_NUMBER, PlayOreTurnplateNewRs.EXT_FIELD_NUMBER,
                PlayOreTurnplateNewHandler.class);
        // 每日特惠
        registerC(GetDayDiscountsRq.EXT_FIELD_NUMBER, GetDayDiscountsRs.EXT_FIELD_NUMBER, GetDayDiscountsHandler.class);
        // 大富翁获取
        registerC(GetMonopolyRq.EXT_FIELD_NUMBER, GetMonopolyRs.EXT_FIELD_NUMBER, GetMonopolyHandler.class);
        // 大富翁摇色子
        registerC(PlayMonopolyRq.EXT_FIELD_NUMBER, PlayMonopolyRs.EXT_FIELD_NUMBER, PlayMonopolyHandler.class);
        // 三倍返利活动
        registerC(ThreeRebateRq.EXT_FIELD_NUMBER, ThreeRebateRs.EXT_FIELD_NUMBER, ThreeRebateHandler.class);
        // 许愿池许愿
        registerC(WishingRq.EXT_FIELD_NUMBER, WishingRs.EXT_FIELD_NUMBER, WishingHandler.class);
        // 获取特殊活动
        registerC(GetSpecialActRq.EXT_FIELD_NUMBER, GetSpecialActRs.EXT_FIELD_NUMBER, GetSpecialActHandler.class);
        // 复活节活动
        registerC(GetEasterActAwardRq.EXT_FIELD_NUMBER, GetEasterActAwardRs.EXT_FIELD_NUMBER, GetEasterActAwardHandler.class);
        // 观看广告领取的奖励
        registerC(AdvertisementRewardRq.EXT_FIELD_NUMBER, AdvertisementRewardRs.EXT_FIELD_NUMBER, AdvertisementRewardHandler.class);
        // 个人目标任务
        registerC(GetAdvanceTaskRq.EXT_FIELD_NUMBER, GetAdvanceTaskRs.EXT_FIELD_NUMBER, GetAdvanceTaskHandler.class);
        // 个人目标领取
        registerC(AdvanceAwardRq.EXT_FIELD_NUMBER, AdvanceAwardRs.EXT_FIELD_NUMBER, AdvanceAwardHandler.class);
        // 热销商品购买和领取
        registerC(ActHotProductAwardRq.EXT_FIELD_NUMBER, ActHotProductAwardRs.EXT_FIELD_NUMBER, ActHotProductAwardHandler.class);
        // 好运道活动抽奖
        registerC(ActGoodLuckAwardRq.EXT_FIELD_NUMBER, ActGoodLuckAwardRs.EXT_FIELD_NUMBER, ActGoodLuckAwardHandler.class);

        // 科技馆
        // 科技馆信息
        registerC(GetTechRq.EXT_FIELD_NUMBER, GetTechRs.EXT_FIELD_NUMBER, GetTechHandler.class);
        // 科技馆雇佣
        registerC(TechAddRq.EXT_FIELD_NUMBER, TechAddRs.EXT_FIELD_NUMBER, TechAddHandler.class);
        // 科技馆升级
        registerC(UpTechRq.EXT_FIELD_NUMBER, UpTechRs.EXT_FIELD_NUMBER, UpTechHandler.class);
        // 科技馆加速
        registerC(TechSpeedRq.EXT_FIELD_NUMBER, TechSpeedRs.EXT_FIELD_NUMBER, TechSpeedHandler.class);
        // 科技馆升级完成确认
        registerC(TechFinishRq.EXT_FIELD_NUMBER, TechFinishRs.EXT_FIELD_NUMBER, TechFinishHandler.class);

        // 化工厂
        // 化工厂信息
        registerC(GetChemicalRq.EXT_FIELD_NUMBER, GetChemicalRs.EXT_FIELD_NUMBER, GetChemicalHandler.class);
        // 化工厂生产
        registerC(ChemicalRecruitRq.EXT_FIELD_NUMBER, ChemicalRecruitRs.EXT_FIELD_NUMBER, ChemicalRecruitHandler.class);
        // 化工厂扩建
        registerC(ChemicalExpandRq.EXT_FIELD_NUMBER, ChemicalExpandRs.EXT_FIELD_NUMBER, ChemicalExpandHandler.class);
        // 化工厂生产完成
        registerC(ChemicalFinishRq.EXT_FIELD_NUMBER, ChemicalFinishRs.EXT_FIELD_NUMBER, ChemicalFinishHandler.class);

        // 聚宝盆
        // 聚宝盆信息
        registerC(GetTreasureRq.EXT_FIELD_NUMBER, GetTreasureRs.EXT_FIELD_NUMBER, GetTreasureHandler.class);
        // 聚宝盆开启
        registerC(TreasureOpenRq.EXT_FIELD_NUMBER, TreasureOpenRs.EXT_FIELD_NUMBER, TreasureOpenHandler.class);
        // 聚宝盆兑换
        registerC(TreasureTradeRq.EXT_FIELD_NUMBER, TreasureTradeRs.EXT_FIELD_NUMBER, TreasureTradeHandler.class);

        // 商店
        // 商店信息
        registerC(GetShopRq.EXT_FIELD_NUMBER, GetShopRs.EXT_FIELD_NUMBER, GetShopHandler.class);
        // 商店购买
        registerC(ShopBuyRq.EXT_FIELD_NUMBER, ShopBuyRs.EXT_FIELD_NUMBER, ShopBuyHandler.class);
        // VIP礼包购买
        registerC(VipBuyRq.EXT_FIELD_NUMBER, VipBuyRs.EXT_FIELD_NUMBER, VipBuyHandler.class);
        // 获取支付订单号
        registerC(GetPaySerialIdRq.EXT_FIELD_NUMBER, GetPaySerialIdRs.EXT_FIELD_NUMBER, GetPaySerialIdHandler.class);
        // 体力购买
        registerC(BuyActRq.EXT_FIELD_NUMBER, BuyActRs.EXT_FIELD_NUMBER, BuyActHandler.class);
        // 柏林银行购买
        registerC(BerlinShopBuyRq.EXT_FIELD_NUMBER, BerlinShopBuyRs.EXT_FIELD_NUMBER, BerlinShopBuyHandler.class);
        // 获取柏林银行信息
        registerC(GetBerlinShopRq.EXT_FIELD_NUMBER, GetBerlinShopRs.EXT_FIELD_NUMBER, GetBerlinShopHandler.class);
        // 聊天气泡购买
        registerC(ChatBubbleBuyRq.EXT_FIELD_NUMBER, ChatBubbleBuyRs.EXT_FIELD_NUMBER, ChatBubbleBuyHandler.class);
        // 荣耀演练场副本商店购买
        registerC(BuyMentorShopRq.EXT_FIELD_NUMBER, BuyMentorShopRs.EXT_FIELD_NUMBER, BuyMentorShopHandler.class);

        // 军团
        // 获取军团信息
        registerC(GetPartyRq.EXT_FIELD_NUMBER, GetPartyRs.EXT_FIELD_NUMBER, GetPartyHandler.class);
        // 军团建设
        registerC(PartyBuildRq.EXT_FIELD_NUMBER, PartyBuildRs.EXT_FIELD_NUMBER, PartyBuildHandler.class);
        // 晋升军阶
        registerC(PromoteRanksRq.EXT_FIELD_NUMBER, PromoteRanksRs.EXT_FIELD_NUMBER, PromoteRanksHandler.class);
        // 修改军团公告
        registerC(ModifySloganRq.EXT_FIELD_NUMBER, ModifySloganRs.EXT_FIELD_NUMBER, ModifySloganHandler.class);
        // 获取军团城池信息
        registerC(GetPartyCityRq.EXT_FIELD_NUMBER, GetPartyCityRs.EXT_FIELD_NUMBER, GetPartyCityHandler.class);
        // 获取军团战争信息
        registerC(GetPartyBattleRq.EXT_FIELD_NUMBER, GetPartyBattleRs.EXT_FIELD_NUMBER, GetPartyBattleHandler.class);
        // 发起邀请
        registerC(MakeInvitesRq.EXT_FIELD_NUMBER, MakeInvitesRs.EXT_FIELD_NUMBER, MakeInvitesHandler.class);
        // 获取战斗邀请
        registerC(GetInvitesBattleRq.EXT_FIELD_NUMBER, GetInvitesBattleRs.EXT_FIELD_NUMBER, GetInvitesBattleHandler.class);
        // 获取军团荣誉数据
        registerC(GetPartyHonorRq.EXT_FIELD_NUMBER, GetPartyHonorRs.EXT_FIELD_NUMBER, GetPartyHonorHandler.class);
        // 领取军团荣誉礼包
        registerC(PartyHonorRewardRq.EXT_FIELD_NUMBER, PartyHonorRewardRs.EXT_FIELD_NUMBER,
                PartyHonorRewardHandler.class);
        // 获取军团日志信息
        registerC(GetPartyLogRq.EXT_FIELD_NUMBER, GetPartyLogRs.EXT_FIELD_NUMBER, GetPartyLogHandler.class);
        // 获取军团官员信息
        registerC(GetPartyJobRq.EXT_FIELD_NUMBER, GetPartyJobRs.EXT_FIELD_NUMBER, GetPartyJobHandler.class);
        // 军团官员选举投票
        registerC(PartyVoteRq.EXT_FIELD_NUMBER, PartyVoteRs.EXT_FIELD_NUMBER, PartyVoteHandler.class);
        // 军团官员任命
        registerC(PartyAppointRq.EXT_FIELD_NUMBER, PartyAppointRs.EXT_FIELD_NUMBER, PartyAppointHandler.class);
        // 军团官员选举拉票
        registerC(PartyCanvassRq.EXT_FIELD_NUMBER, PartyCanvassRs.EXT_FIELD_NUMBER, PartyCanvassHandler.class);
        // 补给大厅的信息
        registerC(SupplyHallRq.EXT_FIELD_NUMBER, SupplyHallRs.EXT_FIELD_NUMBER, SupplyHallHandler.class);
        // 领取军团补给奖励
        registerC(SupplyRewardRq.EXT_FIELD_NUMBER, SupplyRewardRs.EXT_FIELD_NUMBER, SupplyRewardHandler.class);

        // 个人资源点
        // 获取玩家个人资源点数据
        registerC(GetAcquisitionRq.EXT_FIELD_NUMBER, GetAcquisitionRs.EXT_FIELD_NUMBER, GetAcquisitionHandler.class);
        // 个人资源点开始采集
        registerC(BeginAcquisiteRq.EXT_FIELD_NUMBER, BeginAcquisiteRs.EXT_FIELD_NUMBER, BeginAcquisiteHandler.class);
        // 领取个人资源点奖励
        registerC(AcquisiteRewradRq.EXT_FIELD_NUMBER, AcquisiteRewradRs.EXT_FIELD_NUMBER, AcquisiteRewradHandler.class);
        // 使用免费加速
        registerC(UseFreeSpeedRq.EXT_FIELD_NUMBER, UseFreeSpeedRs.EXT_FIELD_NUMBER, UseFreeSpeedHandler.class);
        // 获取成就
        registerC(GetStatusRq.EXT_FIELD_NUMBER, GetStatusRs.EXT_FIELD_NUMBER, GetStatusHandler.class);
        // vip特权次数
        registerC(GetVipCntRq.EXT_FIELD_NUMBER, GetVipCntRs.EXT_FIELD_NUMBER, GetVipCntHandler.class);

        // 好友师徒相关
        // 获取好友列表
        registerC(GetFriendsRq.EXT_FIELD_NUMBER, GetFriendsRs.EXT_FIELD_NUMBER, GetFriendsHandler.class);
        // 添加好友
        registerC(AddFriendRq.EXT_FIELD_NUMBER, AddFriendRs.EXT_FIELD_NUMBER, AddFriendHandler.class);
        // 删除好友
        registerC(DelFriendRq.EXT_FIELD_NUMBER, DelFriendRs.EXT_FIELD_NUMBER, DelFriendHandler.class);
        // 同意或拒绝好友申请
        registerC(AgreeRejectRq.EXT_FIELD_NUMBER, AgreeRejectRs.EXT_FIELD_NUMBER, AgreeRejectHandler.class);
        // 同意或者拒绝收徒
        registerC(AgreeRejectMasterRq.EXT_FIELD_NUMBER, AgreeRejectMasterRs.EXT_FIELD_NUMBER, AgreeRejectMasterHandler.class);
        // 查看好友
        registerC(CheckFirendRq.EXT_FIELD_NUMBER, CheckFirendRs.EXT_FIELD_NUMBER, CheckFirendHandler.class);
        // 获取师徒信息
        registerC(GetMasterApprenticeRq.EXT_FIELD_NUMBER, GetMasterApprenticeRs.EXT_FIELD_NUMBER,
                GetMasterApprenticeHandler.class);
        // 拜师
        registerC(AddMasterRq.EXT_FIELD_NUMBER, AddMasterRs.EXT_FIELD_NUMBER, AddMasterHandler.class);
        // 领取师徒奖励
        registerC(MasterRewardRq.EXT_FIELD_NUMBER, MasterRewardRs.EXT_FIELD_NUMBER, MasterRewardHandler.class);
        // 积分兑换
        registerC(CreditExchangeRq.EXT_FIELD_NUMBER, CreditExchangeRs.EXT_FIELD_NUMBER, CreditExchangeHandler.class);
        // 获取推荐玩家
        registerC(GetRecommendLordRq.EXT_FIELD_NUMBER, GetRecommendLordRs.EXT_FIELD_NUMBER,
                GetRecommendLordHandler.class);
        // 获取黑名单
        registerC(GetBlacklistRq.EXT_FIELD_NUMBER, GetBlacklistRs.EXT_FIELD_NUMBER, GetBlacklistHandler.class);
        // 添加黑名单
        registerC(AddBlackListRq.EXT_FIELD_NUMBER, AddBlackListRs.EXT_FIELD_NUMBER, AddBlackListHandler.class);
        // 删除黑名单
        registerC(DelBlackListRq.EXT_FIELD_NUMBER, DelBlackListRs.EXT_FIELD_NUMBER, DelBlackListHandler.class);
        // 解除师徒关系
        registerC(DelMasterApprenticeRq.EXT_FIELD_NUMBER, DelMasterApprenticeRs.EXT_FIELD_NUMBER, DelMasterApprenticeHandler.class);

        // 柏林会战相关
        // 柏林会战信息
        registerC(BerlinInfoRq.EXT_FIELD_NUMBER, BerlinInfoRs.EXT_FIELD_NUMBER, BerlinInfoHandler.class);
        // 柏林会战据点信息
        registerC(BerlinCityInfoRq.EXT_FIELD_NUMBER, BerlinCityInfoRs.EXT_FIELD_NUMBER, BerlinCityInfoHandler.class);
        // 柏林会战最近战况
        registerC(RecentlyBerlinReportRq.EXT_FIELD_NUMBER, RecentlyBerlinReportRs.EXT_FIELD_NUMBER,
                RecentlyBerlinReportHandler.class);
        // 连续击杀排行数据
        registerC(GetBerlinRankRq.EXT_FIELD_NUMBER, GetBerlinRankRs.EXT_FIELD_NUMBER, BerlinRankInfoHandler.class);
        // 获取累积击杀数据
        registerC(GetBerlinIntegralRq.EXT_FIELD_NUMBER, GetBerlinIntegralRs.EXT_FIELD_NUMBER,
                BerlinIntegralHandler.class);
        // 加入柏林会战
        registerC(AttackBerlinWarRq.EXT_FIELD_NUMBER, AttackBerlinWarRs.EXT_FIELD_NUMBER, AttackBerlinWarHandler.class);
        // 获取柏林官职
        registerC(GetBerlinJobRq.EXT_FIELD_NUMBER, GetBerlinJobRs.EXT_FIELD_NUMBER, GetBerlinJobHandler.class);
        registerC(AppointBerlinJobRq.EXT_FIELD_NUMBER, AppointBerlinJobRs.EXT_FIELD_NUMBER,
                // 任命柏林官职
                AppointBerlinJobHandler.class);
        // 获取历届霸主
        registerC(GetBerlinWinnerListRq.EXT_FIELD_NUMBER, GetBerlinWinnerListRs.EXT_FIELD_NUMBER,
                GetBerlinWinnerListHandler.class);
        // 立即恢复复活CD
        registerC(ResumeImmediatelyRq.EXT_FIELD_NUMBER, ResumeImmediatelyRs.EXT_FIELD_NUMBER,
                ResumeImmediatelyHandler.class);
        // 立即出击将领
        registerC(ImmediatelyAttackRq.EXT_FIELD_NUMBER, ImmediatelyAttackRs.EXT_FIELD_NUMBER,
                ImmediatelyAttackHandler.class);
        // 战前Buff
        registerC(PrewarBuffRq.EXT_FIELD_NUMBER, PrewarBuffRs.EXT_FIELD_NUMBER, PrewarBuffHandler.class);

        // 反攻德意志相关
        registerC(GetCounterAttackRq.EXT_FIELD_NUMBER, GetCounterAttackRs.EXT_FIELD_NUMBER,
                GetCounterAttackHandler.class);
        registerC(AttackCounterBossRq.EXT_FIELD_NUMBER, AttackCounterBossRs.EXT_FIELD_NUMBER,
                AttackCounterBossHandler.class);
        registerC(GetCounterAtkShopRq.EXT_FIELD_NUMBER, GetCounterAtkShopRs.EXT_FIELD_NUMBER,
                GetCounterAtkShopHandler.class);
        registerC(BuyCounterAtkAwardRq.EXT_FIELD_NUMBER, BuyCounterAtkAwardRs.EXT_FIELD_NUMBER,
                BuyCounterAtkAwardHandler.class);

        // 教官相关功能
        // 获取教官信息
        registerC(GetMentorsRq.EXT_FIELD_NUMBER, GetMentorsRs.EXT_FIELD_NUMBER, GetMentorsHandler.class);
        // 获取装备信息
        registerC(GetSpecialEquipsRq.EXT_FIELD_NUMBER, GetSpecialEquipsRs.EXT_FIELD_NUMBER,
                GetSpecialEquipsHandler.class);
        // 教官升级
        registerC(MentorsQuickUpRq.EXT_FIELD_NUMBER, MentorsQuickUpRs.EXT_FIELD_NUMBER, MentorsQuickUpHandler.class);
        // 教官技能升级
        registerC(MentorsSkillUpRq.EXT_FIELD_NUMBER, MentorsSkillUpRs.EXT_FIELD_NUMBER, MentorsSkillUpHandler.class);
        // 领取教官奖励
        registerC(GetMentorAwardRq.EXT_FIELD_NUMBER, GetMentorAwardRs.EXT_FIELD_NUMBER, GetMentorAwardHandler.class);
        // 自动穿戴装备
        registerC(AutoWearEquipRq.EXT_FIELD_NUMBER, AutoWearEquipRs.EXT_FIELD_NUMBER, AutoWearEquipHandler.class);
        // 贩卖教官装备
        registerC(SellMentorEquipRq.EXT_FIELD_NUMBER, SellMentorEquipRs.EXT_FIELD_NUMBER, SellMentorEquipHandler.class);
        registerC(MentorActivateRq.EXT_FIELD_NUMBER, MentorActivateRs.EXT_FIELD_NUMBER, MentorActivateHandler.class);

        // 行为埋点
        registerC(ActionPointRq.EXT_FIELD_NUMBER, ActionPointRs.EXT_FIELD_NUMBER, ActionPointHandler.class);

        // 决战协议
        // 决战道具产生
        registerC(DecisiveBattleRq.EXT_FIELD_NUMBER, DecisiveBattleRs.EXT_FIELD_NUMBER, DecisiveBattleHandler.class);
        // 决战道具领取
        registerC(GainInstructionsRq.EXT_FIELD_NUMBER, GainInstructionsRs.EXT_FIELD_NUMBER,
                GainInstructionsHandler.class);
        // 决战进攻协议
        registerC(AttackDecisiveBattleRq.EXT_FIELD_NUMBER, AttackDecisiveBattleRs.EXT_FIELD_NUMBER,
                AttackDecisiveBattleHandler.class);
        // 决战失败消息协议
        registerC(BattleFailMessageRq.EXT_FIELD_NUMBER, BattleFailMessageRs.EXT_FIELD_NUMBER,
                BattleFailMessageHandler.class);

        // 签到
        registerC(GetSignInInfoRq.EXT_FIELD_NUMBER, GetSignInInfoRs.EXT_FIELD_NUMBER, GetSignInInfoRqHandler.class);
        registerC(GetSignInRewardRq.EXT_FIELD_NUMBER, GetSignInRewardRs.EXT_FIELD_NUMBER,
                GetSignInRewardRqHandler.class);

        // 幸运转盘
        registerC(GetLuckyPoolRq.EXT_FIELD_NUMBER, GetLuckyPoolRs.EXT_FIELD_NUMBER, GetLuckyPoolHandler.class);
        registerC(PlayLuckyPoolRq.EXT_FIELD_NUMBER, PlayLuckyPoolRs.EXT_FIELD_NUMBER, PlayLuckyPoolHandler.class);
        registerC(GetLuckyPoolRankRq.EXT_FIELD_NUMBER, GetLuckyPoolRankRs.EXT_FIELD_NUMBER,
                GetLuckyPoolRankHandler.class);

        // 世界进度
        registerC(GetScheduleRq.EXT_FIELD_NUMBER, GetScheduleRs.EXT_FIELD_NUMBER, GetScheduleHandler.class);
        registerC(GainGoalAwardRq.EXT_FIELD_NUMBER, GainGoalAwardRs.EXT_FIELD_NUMBER, GainGoalAwardHandler.class);
        registerC(AttckScheduleBossRq.EXT_FIELD_NUMBER, AttckScheduleBossRs.EXT_FIELD_NUMBER,
                AttckScheduleBossHandler.class);
        registerC(GetScheduleBossRq.EXT_FIELD_NUMBER, GetScheduleBossRs.EXT_FIELD_NUMBER, GetScheduleBossHandler.class);

        registerC(ScorpionActivateRq.EXT_FIELD_NUMBER, ScorpionActivateRs.EXT_FIELD_NUMBER, ScorpionActivateRsHandler.class);

        // 新地图
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

        // 世界争霸（世界任务、赛季商店）
        registerC(WorldWarCampDateRq.EXT_FIELD_NUMBER, WorldWarCampDateRs.EXT_FIELD_NUMBER,
                GetWorldWarCampDateHandler.class);
        registerC(WorldWarTaskDateRq.EXT_FIELD_NUMBER, WorldWarTaskDateRs.EXT_FIELD_NUMBER,
                GetWorldWarTaskDateHandler.class);
        registerC(WorldWarSeasonShopGoodsRq.EXT_FIELD_NUMBER, WorldWarSeasonShopGoodsRs.EXT_FIELD_NUMBER,
                GetWorldWarSeasonShopGoodsHandler.class);
        registerC(WorldWarCampRankPlayersDateRq.EXT_FIELD_NUMBER, WorldWarCampRankPlayersDateRs.EXT_FIELD_NUMBER,
                GetWorldWarCampRankPlayersDateHandler.class);
        registerC(WorldWarAwardRq.EXT_FIELD_NUMBER, WorldWarAwardRs.EXT_FIELD_NUMBER, GetWorldWarAwardHandler.class);

        //纽约争霸
        registerC(NewYorkWarInfoRq.EXT_FIELD_NUMBER, NewYorkWarInfoRs.EXT_FIELD_NUMBER, NewYorkWarInfoHandler.class);
        registerC(NewYorkWarProgressDataRq.EXT_FIELD_NUMBER, NewYorkWarProgressDataRs.EXT_FIELD_NUMBER, NewYorkWarProgressDataHandler.class);
        registerC(NewYorkWarPlayerRankDataRq.EXT_FIELD_NUMBER, NewYorkWarPlayerRankDataRs.EXT_FIELD_NUMBER, NewYorkWarPlayerRankDataHandler.class);
        registerC(NewYorkWarAchievementRq.EXT_FIELD_NUMBER, NewYorkWarAchievementRs.EXT_FIELD_NUMBER, NewYorkWarAchievementHandler.class);

        // 巴顿活动
        registerC(ActBartonBuyRq.EXT_FIELD_NUMBER, ActBartonBuyRs.EXT_FIELD_NUMBER, ActBartonBuyHandler.class);
        registerC(GetActBartonRq.EXT_FIELD_NUMBER, GetActBartonRs.EXT_FIELD_NUMBER, GetActBartonHandler.class);

        // 罗宾汉活动
        registerC(GetActRobinHoodRq.EXT_FIELD_NUMBER, GetActRobinHoodRs.EXT_FIELD_NUMBER, GetActRobinHoodHandler.class);
        registerC(ActRobinHoodAwardRq.EXT_FIELD_NUMBER, ActRobinHoodAwardRs.EXT_FIELD_NUMBER, ActRobinHoodAwardHandler.class);

        // 战令相关
        registerC(GetBattlePassRq.EXT_FIELD_NUMBER, GetBattlePassRs.EXT_FIELD_NUMBER, GetBattlePassHandler.class);
        registerC(BuyBattlePassLvRq.EXT_FIELD_NUMBER, BuyBattlePassLvRs.EXT_FIELD_NUMBER, BuyBattlePassLvHandler.class);
        registerC(ReceiveBPAwardRq.EXT_FIELD_NUMBER, ReceiveBPAwardRs.EXT_FIELD_NUMBER, ReceiveBPAwardHandler.class);
        registerC(ReceiveTaskAwardRq.EXT_FIELD_NUMBER, ReceiveTaskAwardRs.EXT_FIELD_NUMBER, ReceiveTaskAwardHandler.class);

        // 阵营对拼相关
        registerC(GetRoyalArenaRq.EXT_FIELD_NUMBER, GetRoyalArenaRs.EXT_FIELD_NUMBER, GetRoyalArenaHandler.class);
        registerC(RoyalArenaAwardRq.EXT_FIELD_NUMBER, RoyalArenaAwardRs.EXT_FIELD_NUMBER, RoyalArenaAwardHandler.class);
        registerC(RoyalArenaTaskRq.EXT_FIELD_NUMBER, RoyalArenaTaskRs.EXT_FIELD_NUMBER, RoyalArenaTaskHandler.class);
        registerC(RoyalArenaSkillRq.EXT_FIELD_NUMBER, RoyalArenaSkillRs.EXT_FIELD_NUMBER, RoyalArenaSkillHandler.class);

        //圣诞活动
        registerC(GetChristmasInfoRq.EXT_FIELD_NUMBER, GetChristmasInfoRs.EXT_FIELD_NUMBER, ChristmasGetInfoHandler.class);
        registerC(HandInChristmasChipRq.EXT_FIELD_NUMBER, HandInChristmasChipRs.EXT_FIELD_NUMBER, ChristmasHandInChipHandler.class);
        registerC(GetChristmasAwardRq.EXT_FIELD_NUMBER, GetChristmasAwardRs.EXT_FIELD_NUMBER, ChristmasGetAwardHandler.class);

        //沙盘演武
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

        //战火燎原
        //战火燎原---积分相关
        registerC(GetWarFireCampRankRq.EXT_FIELD_NUMBER, GetWarFireCampRankRs.EXT_FIELD_NUMBER, GetWarFireCampRankHandler.class);
        registerC(GetWarFireCampScoreRq.EXT_FIELD_NUMBER, GetWarFireCampScoreRs.EXT_FIELD_NUMBER, GetWarFireCampScoreHandler.class);
        registerC(GetWarFireCampSummaryRq.EXT_FIELD_NUMBER, GetWarFireCampSummaryRs.EXT_FIELD_NUMBER, GetWarFireCampSummaryHandler.class);
        //战火燎原---购买相关
        registerC(BuyWarFireShopRq.EXT_FIELD_NUMBER, BuyWarFireShopRs.EXT_FIELD_NUMBER, BuyWarFireShopHandler.class);
        registerC(BuyWarFireBuffRq.EXT_FIELD_NUMBER, BuyWarFireBuffRs.EXT_FIELD_NUMBER, BuyWarFireBuffHandler.class);
        // 获取战火燎原城池详情
        registerC(GetCrossCityInfoRq.EXT_FIELD_NUMBER, GetCrossCityInfoRs.EXT_FIELD_NUMBER, GetCrossCityInfoHandler.class);
        //战火燎原获取玩家信息
        registerC(GetPlayerWarFireRq.EXT_FIELD_NUMBER, GetPlayerWarFireRs.EXT_FIELD_NUMBER, GetWarFirePlayerInfoHandler.class);

        //点鞭炮, 驱赶年兽
        registerC(SetOffFirecrackersRq.EXT_FIELD_NUMBER, SetOffFirecrackersRs.EXT_FIELD_NUMBER, SetOffFirecrackersNianHandler.class);

        //貂蝉活动
        registerC(DiaoChanGetInfoRq.EXT_FIELD_NUMBER, DiaoChanGetInfoRs.EXT_FIELD_NUMBER, DiaoChanGetInfoHandler.class);
        registerC(DiaoChanGetAwardRq.EXT_FIELD_NUMBER, DiaoChanGetAwardRs.EXT_FIELD_NUMBER, DiaoChanGetAwardHandler.class);
        registerC(DiaoChanGetRankInfoRq.EXT_FIELD_NUMBER, DiaoChanGetRankInfoRs.EXT_FIELD_NUMBER, DiaoChanGetRankInfoHandler.class);

        //内挂
        registerC(OnHookGetInfoRq.EXT_FIELD_NUMBER, OnHookGetInfoRs.EXT_FIELD_NUMBER, OnHookGetInfoHandler.class);
        registerC(OnHookReplenishRq.EXT_FIELD_NUMBER, OnHookReplenishRs.EXT_FIELD_NUMBER, OnHookReplenishHandler.class);
        registerC(OnHookOperateRq.EXT_FIELD_NUMBER, OnHookOperateRs.EXT_FIELD_NUMBER, OnHookOperateHandler.class);
        registerC(OnHookGetAwardRq.EXT_FIELD_NUMBER, OnHookGetAwardRs.EXT_FIELD_NUMBER, OnHookGetAwardHandler.class);

        //赛季
        registerC(SeasonGetInfoRq.EXT_FIELD_NUMBER, SeasonGetInfoRs.EXT_FIELD_NUMBER, SeasonGetInfoHandler.class);
        registerC(SeasonGetTreasuryInfoRq.EXT_FIELD_NUMBER, SeasonGetTreasuryInfoRs.EXT_FIELD_NUMBER, SeasonGetTreasuryInfoHandler.class);
        registerC(SeasonGetTaskInfoRq.EXT_FIELD_NUMBER, SeasonGetTaskInfoRs.EXT_FIELD_NUMBER, SeasonGetTaskInfoHandler.class);
        registerC(SeasonGenerateTreasuryAwardRq.EXT_FIELD_NUMBER, SeasonGenerateTreasuryAwardRs.EXT_FIELD_NUMBER, SeasonGenerateTreasuryAwardHandler.class);
        registerC(SeasonGetTreasuryAwardRq.EXT_FIELD_NUMBER, SeasonGetTreasuryAwardRs.EXT_FIELD_NUMBER, SeasonGetTreasuryAwardHandler.class);
        registerC(SeasonGetTaskAwardRq.EXT_FIELD_NUMBER, SeasonGetTaskAwardRs.EXT_FIELD_NUMBER, SeasonGetTaskAwardHandler.class);
        registerC(SeasonGetRankRq.EXT_FIELD_NUMBER, SeasonGetRankRs.EXT_FIELD_NUMBER, SeasonGetRankHandler.class);

        //赛季英雄
        registerC(SynthSeasonHeroRq.EXT_FIELD_NUMBER, SynthSeasonHeroRs.EXT_FIELD_NUMBER, SeasonSynthHeroHandler.class);
        registerC(UpgradeHeroCgyRq.EXT_FIELD_NUMBER, UpgradeHeroCgyRs.EXT_FIELD_NUMBER, SeasonUpgradeHeroCgyHandler.class);
        registerC(UpgradeHeroSkillRq.EXT_FIELD_NUMBER, UpgradeHeroSkillRs.EXT_FIELD_NUMBER, SeasonUpgradeHeroSkillHandler.class);

        //活动
        registerC(GetActivityDataInfoRq.EXT_FIELD_NUMBER, GetActivityDataInfoRs.EXT_FIELD_NUMBER, GetActivityDataInfoHandler.class);
        registerC(DragonBoatExchangeRq.EXT_FIELD_NUMBER, DragonBoatExchangeRs.EXT_FIELD_NUMBER, DragonBoatExchangeHandler.class);
        registerC(DailyKeepRechargeGetAwardRq.EXT_FIELD_NUMBER, DailyKeepRechargeGetAwardRs.EXT_FIELD_NUMBER, DailyKeepRechargeGetAwardHandler.class);
        registerC(SummerTurntablePlayRq.EXT_FIELD_NUMBER, SummerTurntablePlayRs.EXT_FIELD_NUMBER, SummerTurntablePlayHandler.class);
        registerC(SummerTurntableNextRq.EXT_FIELD_NUMBER, SummerTurntableNextRs.EXT_FIELD_NUMBER, SummerTurntableNextHandler.class);
        registerC(SummerCastleGetAwardRq.EXT_FIELD_NUMBER, SummerCastleGetAwardRs.EXT_FIELD_NUMBER, SummerCastleGetAwardHandler.class);
        registerC(GetAltarRq.EXT_FIELD_NUMBER, GetAltarRs.EXT_FIELD_NUMBER, GetAltarHandler.class); // 获取圣坛信息
        registerC(AnniversaryTurntablePlayRq.EXT_FIELD_NUMBER, AnniversaryTurntablePlayRs.EXT_FIELD_NUMBER, AnniversaryTurntablePlayHandler.class);//周年转盘
        registerC(AnniversaryEggOpenRq.EXT_FIELD_NUMBER, AnniversaryEggOpenRs.EXT_FIELD_NUMBER, AnniversaryEggOpenHandler.class);//周年彩蛋
        registerC(AnniversaryJigsawRq.EXT_FIELD_NUMBER, AnniversaryJigsawRs.EXT_FIELD_NUMBER, AnniversaryJigsawHandler.class);//周年庆拼图
        registerC(FireFireWorkRq.EXT_FIELD_NUMBER, FireFireWorkRs.EXT_FIELD_NUMBER, AnniversaryFireFireWorkHandler.class);//周年庆烟花
        registerC(BuyEncoreSkinRq.EXT_FIELD_NUMBER, BuyEncoreSkinRs.EXT_FIELD_NUMBER, BuyEncoreSkinHandler.class);//周年庆购买钻石皮肤
        registerC(MusicFestivalBoxOfficeActionRq.EXT_FIELD_NUMBER, MusicFestivalBoxOfficeActionRs.EXT_FIELD_NUMBER, BoxOfficeActionHandler.class);// 音乐节-售票处
        registerC(ChooseDifficultRq.EXT_FIELD_NUMBER, ChooseDifficultRs.EXT_FIELD_NUMBER, MusicCrtOfficeChooseDifficultHandler.class);// 音乐节-歌曲创作-难度选择
        registerC(FinishCrtTaskRq.EXT_FIELD_NUMBER, FinishCrtTaskRs.EXT_FIELD_NUMBER, MusicCrtOfficeFinishTaskHandler.class);// 音乐节-歌曲创作-完成任务
        registerC(GiveUpCrtMusicRq.EXT_FIELD_NUMBER, GiveUpCrtMusicRs.EXT_FIELD_NUMBER, MusicCrtOfficeGiveUpCrtHandler.class);// 音乐节-歌曲创作-放弃谱曲
        registerC(FinishCrtMusicRq.EXT_FIELD_NUMBER, FinishCrtMusicRs.EXT_FIELD_NUMBER, FinishCreateMusicHandler.class);// 音乐节-歌曲创作-谱曲完成
        registerC(CrtMusicCampRankRq.EXT_FIELD_NUMBER, CrtMusicCampRankRs.EXT_FIELD_NUMBER, CrtMusicCampRankHandler.class);// 音乐节-歌曲创作-阵营排名信息
        registerC(CrtMusicPersonRankRq.EXT_FIELD_NUMBER, CrtMusicPersonRankRs.EXT_FIELD_NUMBER, CrtMusicPersonRankHandler.class);// 音乐节-歌曲创作-个人排名信息
        registerC(DrawProgressRq.EXT_FIELD_NUMBER, DrawProgressRs.EXT_FIELD_NUMBER, DrawProgressHandler.class);// 音乐节-歌曲创作-领取进度奖励
        registerC(MusicSpecialThanksRq.EXT_FIELD_NUMBER, MusicSpecialThanksRs.EXT_FIELD_NUMBER, GetSpecialThanksHandler.class);// 音乐节-歌曲创作-特别鸣谢


        //赛季天赋
        registerC(GetSeasonTalentRq.EXT_FIELD_NUMBER, GetSeasonTalentRs.EXT_FIELD_NUMBER, SeasonGetTalentHandler.class);
        registerC(ChooseClassifierRq.EXT_FIELD_NUMBER, ChooseClassifierRs.EXT_FIELD_NUMBER, SeasonTalentChooseClassifierHandler.class);
        registerC(StudyTalentRq.EXT_FIELD_NUMBER, StudyTalentRs.EXT_FIELD_NUMBER, SeasonStudyTalentHandler.class);
        registerC(ChangeTalentSkillRq.EXT_FIELD_NUMBER, ChangeTalentSkillRs.EXT_FIELD_NUMBER, SeasonChangeTalentHandler.class);
        registerC(OpenTalentRq.EXT_FIELD_NUMBER, OpenTalentRs.EXT_FIELD_NUMBER, SeasonOpenTalentHandler.class);

        //钓鱼
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

        //丰收转盘
        registerC(AutumnTurnplatePlayRq.EXT_FIELD_NUMBER, AutumnTurnplatePlayRs.EXT_FIELD_NUMBER, AutumnTurnplatePlayHandler.class);
        registerC(AutumnTurnplateGetProgressAwardRq.EXT_FIELD_NUMBER, AutumnTurnplateGetProgressAwardRs.EXT_FIELD_NUMBER, AutumnTurnplateGetProgressAwardHandler.class);
        registerC(AutumnTurnplateRefreshRq.EXT_FIELD_NUMBER, AutumnTurnplateRefreshRs.EXT_FIELD_NUMBER, AutumnTurnplateRefreshHandler.class);

        //金秋活动
        registerC(EmpireFarmRq.EXT_FIELD_NUMBER, EmpireFarmRs.EXT_FIELD_NUMBER, EmpireFarmSowingHandler.class);
        registerC(EmpireFarmOpenTreasureChestRq.EXT_FIELD_NUMBER, EmpireFarmOpenTreasureChestRs.EXT_FIELD_NUMBER, EmpireFarmOpenTreasureChestHandler.class);
        registerC(GoldenAutumnFruitfulRq.EXT_FIELD_NUMBER, GoldenAutumnFruitfulRs.EXT_FIELD_NUMBER, GoldenAutumnFruitfulHandler.class);
        registerC(GoldenAutumnSunriseGetTaskAwardRq.EXT_FIELD_NUMBER, GoldenAutumnSunriseGetTaskAwardRs.EXT_FIELD_NUMBER, GoldenAutumnGetTaskAwardHandler.class);
        registerC(GoldenAutumnSunriseOpenTreasureChestRq.EXT_FIELD_NUMBER, GoldenAutumnSunriseOpenTreasureChestRs.EXT_FIELD_NUMBER, GoldenAutumnOpenTreasureChestHandler.class);

        //跨服活动
        registerC(GetCrossRechargeRankingRq.EXT_FIELD_NUMBER, GetCrossRechargeRankingRs.EXT_FIELD_NUMBER, GetCrossRechargeActivityRankHandler.class);

        // simple跨服相关
        registerC(EnterCrossRq.EXT_FIELD_NUMBER, EnterCrossRs.EXT_FIELD_NUMBER, EnterCrossHandler.class); // 进入退出跨服
        registerC(ChoiceHeroJoinRq.EXT_FIELD_NUMBER, ChoiceHeroJoinRs.EXT_FIELD_NUMBER, ChoiceHeroJoinHandler.class); // 选将领加入跨服
        registerC(GetCrossFortRq.EXT_FIELD_NUMBER, GetCrossFortRs.EXT_FIELD_NUMBER, DirectForwardClientHandler.class); // 获取跨服城堡信息
        registerC(OpFortHeroRq.EXT_FIELD_NUMBER, OpFortHeroRs.EXT_FIELD_NUMBER, OpFortHeroHandler.class); // 获取跨服城堡信息
        registerC(GetCrossChatRq.EXT_FIELD_NUMBER, GetCrossChatRs.EXT_FIELD_NUMBER, DirectForwardClientHandler.class); // 获取跨服聊天消息
        registerC(BuyCrossBuffRq.EXT_FIELD_NUMBER, BuyCrossBuffRs.EXT_FIELD_NUMBER, BuyCrossBuffHandler.class); // 购买跨服buff
        registerC(GetCrossInfoRq.EXT_FIELD_NUMBER, GetCrossInfoRs.EXT_FIELD_NUMBER, GetCrossInfoHandler.class); // 获取跨服信息(时间信息,buff信息)
        registerC(CrossTrophyInfoRq.EXT_FIELD_NUMBER, CrossTrophyInfoRs.EXT_FIELD_NUMBER, CrossTrophyInfoHandler.class); // 获取跨服成就信息
        registerC(CrossTrophyAwardRq.EXT_FIELD_NUMBER, CrossTrophyAwardRs.EXT_FIELD_NUMBER,
                CrossTrophyAwardHandler.class); // 领取跨服成就奖励
        registerC(GetCrossRankRq.EXT_FIELD_NUMBER, GetCrossRankRs.EXT_FIELD_NUMBER, DirectForwardClientHandler.class); // 获取跨服排行榜

        //客户端拉取只读展示内容相关
        registerC(GetBannerRq.EXT_FIELD_NUMBER, GetBannerRs.EXT_FIELD_NUMBER, GetBannerHandler.class); // 获取合服banner

        //秋季拍卖活动
        registerC(FollowAuctionsRq.EXT_FIELD_NUMBER, FollowAuctionsRs.EXT_FIELD_NUMBER, FollowAuctionsHandler.class);
        registerC(GetActAuctionInfoRq.EXT_FIELD_NUMBER, GetActAuctionInfoRs.EXT_FIELD_NUMBER, GetActAuctionInfoHandler.class);
        registerC(GetActAuctionItemRecordRq.EXT_FIELD_NUMBER, GetActAuctionItemRecordRs.EXT_FIELD_NUMBER, GetActAuctionItemRecordHandler.class);
        registerC(GetActAuctionRecordRq.EXT_FIELD_NUMBER, GetActAuctionRecordRs.EXT_FIELD_NUMBER, GetActAuctionRecordHandler.class);
        registerC(GetMyAuctionRecordRq.EXT_FIELD_NUMBER, GetMyAuctionRecordRs.EXT_FIELD_NUMBER, GetMyAuctionRecordHandler.class);
        registerC(PurchaseAuctionItemRq.EXT_FIELD_NUMBER, PurchaseAuctionItemRs.EXT_FIELD_NUMBER, PurchaseAuctionItemHandler.class);
        registerC(GetActAuctionTypeRq.EXT_FIELD_NUMBER, GetActAuctionTypeRs.EXT_FIELD_NUMBER, GetActAuctionTypeHandler.class);

        //助力圣域 领取奖励
        registerC(HelpShengYuGetAwardRq.EXT_FIELD_NUMBER, HelpShengYuGetAwardRs.EXT_FIELD_NUMBER, HelpShengYuGetAwardHandler.class);

        //小游戏
        registerC(GetSmallGameRq.EXT_FIELD_NUMBER, GetSmallGameRs.EXT_FIELD_NUMBER, GetSmallGameHandler.class);
        registerC(DrawSmallGameAwardRq.EXT_FIELD_NUMBER, DrawSmallGameAwardRs.EXT_FIELD_NUMBER, DrawSmallGameAwardHandler.class);
        //阵法图腾
        registerC(TotemSyntheticRq.EXT_FIELD_NUMBER, TotemSyntheticRs.EXT_FIELD_NUMBER, TotemSyntheticHandler.class);
        registerC(TotemDecomposeRq.EXT_FIELD_NUMBER, TotemDecomposeRs.EXT_FIELD_NUMBER, TotemDecomposeHandler.class);
        registerC(TotemStrengthenRq.EXT_FIELD_NUMBER, TotemStrengthenRs.EXT_FIELD_NUMBER, TotemStrengthenHandler.class);
        registerC(TotemResonateRq.EXT_FIELD_NUMBER, TotemResonateRs.EXT_FIELD_NUMBER, TotemResonateHandler.class);
        registerC(TotemBreakRq.EXT_FIELD_NUMBER, TotemBreakRs.EXT_FIELD_NUMBER, TotemBreakHandler.class);
        registerC(TotemLockRq.EXT_FIELD_NUMBER, TotemLockRs.EXT_FIELD_NUMBER, TotemLockHandler.class);
        registerC(TotemPutonRq.EXT_FIELD_NUMBER, TotemPutonRs.EXT_FIELD_NUMBER, TotemPutonHandler.class);

        //宝具
        registerC(GetTreasureWaresRq.EXT_FIELD_NUMBER, GetTreasureWaresRs.EXT_FIELD_NUMBER, GetTreasureWaresHandler.class);
        registerC(MakeTreasureWareRq.EXT_FIELD_NUMBER, MakeTreasureWareRs.EXT_FIELD_NUMBER, MakeTreasureWareHandler.class);
        registerC(OnTreasureWareRq.EXT_FIELD_NUMBER, OnTreasureWareRs.EXT_FIELD_NUMBER, OnTreasureWareHandler.class);
        registerC(StrengthenTreasureWareRq.EXT_FIELD_NUMBER, StrengthenTreasureWareRs.EXT_FIELD_NUMBER, StrengthenTreasureWareHandler.class);
        registerC(TreasureWareBagExpandRq.EXT_FIELD_NUMBER, TreasureWareBagExpandRs.EXT_FIELD_NUMBER, TreasureWareBagExpandHandler.class);
        registerC(TreasureWareBatchDecomposeRq.EXT_FIELD_NUMBER, TreasureWareBatchDecomposeRs.EXT_FIELD_NUMBER, TreasureWareDecomposeHandler.class);
        registerC(TreasureWareLockedRq.EXT_FIELD_NUMBER, TreasureWareLockedRs.EXT_FIELD_NUMBER, TreasureWareLockedHandler.class);
        //宝具洗练
        registerC(TreasureWareTrainRq.EXT_FIELD_NUMBER, TreasureWareTrainRs.EXT_FIELD_NUMBER, TreasureWareTrainHandler.class);
        //保存洗练结果
        registerC(TreasureWareSaveTrainRq.EXT_FIELD_NUMBER, TreasureWareSaveTrainRs.EXT_FIELD_NUMBER, TreasureWareSaveTrainHandler.class);
        // 宝具副本
        registerC(GetTreasureCombatRq.EXT_FIELD_NUMBER, GetTreasureCombatRs.EXT_FIELD_NUMBER, GetTreasureCombatHandler.class);
        registerC(DoTreasureCombatRq.EXT_FIELD_NUMBER, DoTreasureCombatRs.EXT_FIELD_NUMBER, DoTreasureCombatHandler.class);
        registerC(TreasureOnHookAwardRq.EXT_FIELD_NUMBER, TreasureOnHookAwardRs.EXT_FIELD_NUMBER, TreasureOnHookAwardHandler.class);
        registerC(TreasureSectionAwardRq.EXT_FIELD_NUMBER, TreasureSectionAwardRs.EXT_FIELD_NUMBER, TreasureSectionAwardHandler.class);
        registerC(TreasureChallengePlayerRq.EXT_FIELD_NUMBER, TreasureChallengePlayerRs.EXT_FIELD_NUMBER, TreasureChallengePlayerHandler.class);
        registerC(TreasureRefreshChallengeRq.EXT_FIELD_NUMBER, TreasureRefreshChallengeRs.EXT_FIELD_NUMBER, TreasureRefreshChallengeHandler.class);
        registerC(TreasureChallengePurchaseRq.EXT_FIELD_NUMBER, TreasureChallengePurchaseRs.EXT_FIELD_NUMBER, TreasureChallengePurchaseHandler.class);

        //宝具征程活动
        registerC(ReceiveActTwJourneyAwardRq.EXT_FIELD_NUMBER, ReceiveActTwJourneyAwardRs.EXT_FIELD_NUMBER, ReceiveActTwJourneyAwardHandler.class);
        registerC(GetActTwJourneyRq.EXT_FIELD_NUMBER, GetActTwJourneyRs.EXT_FIELD_NUMBER, GetActTwJourneyInfoHandler.class);
        //神兵宝具活动
        registerC(DrawTwTurntableAwardRq.EXT_FIELD_NUMBER, DrawTwTurntableAwardRs.EXT_FIELD_NUMBER, DrawMagicTwTurntableAwardHandler.class);
        registerC(ReceiveMtwTurntableCntAwardRq.EXT_FIELD_NUMBER, ReceiveMtwTurntableCntAwardRs.EXT_FIELD_NUMBER, ReceiveMTwtCntAwardHandler.class);

        //2022新年活动
        registerC(LongLightIgniteRq.EXT_FIELD_NUMBER, LongLightIgniteRs.EXT_FIELD_NUMBER, LongLightIgniteHandler.class);
        registerC(FireworkLetoffRq.EXT_FIELD_NUMBER, FireworkLetoffRs.EXT_FIELD_NUMBER, FireworkLetoffHandler.class);
        registerC(YearFishBeginRq.EXT_FIELD_NUMBER, YearFishBeginRs.EXT_FIELD_NUMBER, YearFishBeginHandler.class);
        registerC(YearFishEndRq.EXT_FIELD_NUMBER, YearFishEndRs.EXT_FIELD_NUMBER, YearFishEndHandler.class);
        registerC(YearFishShopExchangeRq.EXT_FIELD_NUMBER, YearFishShopExchangeRs.EXT_FIELD_NUMBER, YearFishShopExchangeHandler.class);

        //跨服地图
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


        //跨服战火燎原
        registerC(BuyCrossWarFireBuffRq.EXT_FIELD_NUMBER, BuyCrossWarFireBuffRs.EXT_FIELD_NUMBER, BuyCrossWarFireBuffHandler.class);
        registerC(GetCrossWarFireRanksRq.EXT_FIELD_NUMBER, GetCrossWarFireRanksRs.EXT_FIELD_NUMBER, GetCrossWarFireRanksHandler.class);
        registerC(GetCrossWarFireCityOccupyRq.EXT_FIELD_NUMBER, GetCrossWarFireCityOccupyRs.EXT_FIELD_NUMBER, GetCrossWarFireCityOccupyHandler.class);
        registerC(GetCrossWarFireCampSummaryRq.EXT_FIELD_NUMBER, GetCrossWarFireCampSummaryRs.EXT_FIELD_NUMBER, GetCrossWarFireCampSummaryHandler.class);
        registerC(GetCrossWarFirePlayerLiveRq.EXT_FIELD_NUMBER, GetCrossWarFirePlayerLiveRs.EXT_FIELD_NUMBER, GetCrossWarFirePlayerLiveHandler.class);
        registerC(RefreshGetCrossWarFirePlayerInfoRq.EXT_FIELD_NUMBER, RefreshGetCrossWarFirePlayerInfoRs.EXT_FIELD_NUMBER, RefreshGetPlayerInfoHandler.class);

        // 章节任务
        registerC(GetChapterTaskRq.EXT_FIELD_NUMBER, GetChapterTaskRs.EXT_FIELD_NUMBER, GetChapterTaskHandler.class);
        registerC(GetChapterTaskAwardRq.EXT_FIELD_NUMBER, GetChapterTaskAwardRs.EXT_FIELD_NUMBER, GetChapterTaskAwardHandler.class);
        registerC(GetChapterAwardRq.EXT_FIELD_NUMBER, GetChapterAwardRs.EXT_FIELD_NUMBER, GetChapterAwardHandler.class);

        //跨服聊天
        registerC(GetGamePlayChatRoomRq.EXT_FIELD_NUMBER, GetGamePlayChatRoomRs.EXT_FIELD_NUMBER, GetGamePlayChatRoomHandler.class);
        registerC(GetChatRoomMsgRq.EXT_FIELD_NUMBER, GetChatRoomMsgRs.EXT_FIELD_NUMBER, GetChatRoomMsgHandler.class);
        registerC(GetRoomPlayerShowRq.EXT_FIELD_NUMBER, GetRoomPlayerShowRs.EXT_FIELD_NUMBER, GetRoomPlayerShowHandler.class);
        registerC(GetCrossPlayerShowRq.EXT_FIELD_NUMBER, GetCrossPlayerShowRs.EXT_FIELD_NUMBER, GetCrossPlayerShowHandler.class);

        // 酒馆 常驻抽卡
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

        // 武将列传相关
        registerC(GamePb5.GetHeroBiographyInfoRq.EXT_FIELD_NUMBER, GetHeroBiographyInfoRs.EXT_FIELD_NUMBER, GetHeroBiographyInfoHandler.class);
        registerC(GamePb5.UpgradeHeroBiographyRq.EXT_FIELD_NUMBER, UpgradeHeroBiographyRs.EXT_FIELD_NUMBER, UpgradeHeroBiographyHandler.class);

        // 搜索叛军
        registerC(SearchBanditRq.EXT_FIELD_NUMBER, SearchBanditRs.EXT_FIELD_NUMBER, SearchBanditHandler.class);

        //遗迹 获取信息
        registerC(GetRelicDataInfoRq.EXT_FIELD_NUMBER, GetRelicDataInfoRs.EXT_FIELD_NUMBER, GetRelicDataInfoHandler.class);
        registerC(GetRelicDetailRq.EXT_FIELD_NUMBER, GetRelicDetailRs.EXT_FIELD_NUMBER, GetRelicDetailHandler.class);
        registerC(GetRelicScoreAwardRq.EXT_FIELD_NUMBER, GetRelicScoreAwardRs.EXT_FIELD_NUMBER, GetRelicScoreAwardHandler.class);

        // 记录模拟器信息
        registerC(RecordLifeSimulatorRq.EXT_FIELD_NUMBER, RecordLifeSimulatorRs.EXT_FIELD_NUMBER, RecordSimulatorHandler.class);
        // 主城建设
        registerC(ExploreRq.EXT_FIELD_NUMBER, ExploreRs.EXT_FIELD_NUMBER, ExploreHandler.class);
        registerC(ReclaimFoundationRq.EXT_FIELD_NUMBER, ReclaimFoundationRs.EXT_FIELD_NUMBER, ReclaimHandler.class);
        registerC(SwapBuildingPosRq.EXT_FIELD_NUMBER, SwapBuildingPosRs.EXT_FIELD_NUMBER, SwapBuildingPosHandler.class);
        registerC(PeaceAndWelfareRq.EXT_FIELD_NUMBER, PeaceAndWelfareRs.EXT_FIELD_NUMBER, PeaceAndWelfareHandler.class);
        registerC(ClearBanditRq.EXT_FIELD_NUMBER, ClearBanditRs.EXT_FIELD_NUMBER, ClearBanditHandler.class);
        // 建造建筑
        registerC(CreateBuildingRq.EXT_FIELD_NUMBER, CreateBuildingRs.EXT_FIELD_NUMBER, CreateBuildingHandler.class);
        // 派遣居民
        registerC(DispatchResidentRq.EXT_FIELD_NUMBER, DispatchResidentRs.EXT_FIELD_NUMBER, DispatchResidentHandler.class);
        // 委任武将
        registerC(DispatchHeroRq.EXT_FIELD_NUMBER, DispatchHeroRs.EXT_FIELD_NUMBER, DispatchHeroHandler.class);
        // 经济订单
        registerC(AssignEconomicCropRq.EXT_FIELD_NUMBER, AssignEconomicCropRs.EXT_FIELD_NUMBER, AssignEconomicCropHandler.class);
        registerC(SubmitEconomicOrderRq.EXT_FIELD_NUMBER, SubmitEconomicOrderRs.EXT_FIELD_NUMBER, SubmitEconomicOrderHandler.class);
        registerC(GetEconomicOrderRq.EXT_FIELD_NUMBER, GetEconomicOrderRs.EXT_FIELD_NUMBER, GetEconomicOrderHandler.class);
    }

    /**
     * 集中注册通过Http交互的协议与处理handler
     */
    private void httpMessagePool() {
        // 服务器注册
        registerH(HttpPb.RegisterRs.EXT_FIELD_NUMBER, 0, RegisterRsHandler.class);

        // 帐号登录验证
        registerH(HttpPb.VerifyRs.EXT_FIELD_NUMBER, BeginGameRs.EXT_FIELD_NUMBER, VerifyRsHandler.class);

        // GM命令
        registerH(DoSomeRq.EXT_FIELD_NUMBER, DoSomeRs.EXT_FIELD_NUMBER, GmHandler.class);

        // 兑换码
        registerH(HttpPb.UseGiftCodeRs.EXT_FIELD_NUMBER, GiftCodeRs.EXT_FIELD_NUMBER, UseGiftCodeRsHandler.class);

        // 邮件
        registerH(HttpPb.SendToMailRq.EXT_FIELD_NUMBER, HttpPb.SendToMailRs.EXT_FIELD_NUMBER,
                SendToMailRqHandler.class);

        // 禁言封号
        registerH(HttpPb.ForbiddenRq.EXT_FIELD_NUMBER, HttpPb.ForbiddenRs.EXT_FIELD_NUMBER, ForbiddenRqHandler.class);

        // 热加载
        registerH(HttpPb.ReloadParamRq.EXT_FIELD_NUMBER, HttpPb.ReloadParamRs.EXT_FIELD_NUMBER,
                ReloadParamRqHandler.class);

        // 分享奖励
        registerH(HttpPb.ShareRewardRs.EXT_FIELD_NUMBER, 0, ShareRewardRsHandler.class);
        // 微信签到奖励
        registerH(HttpPb.WeChatSignRewardRq.EXT_FIELD_NUMBER, 0, WeChatSignRewardHandler.class);

        // 公告
        registerH(HttpPb.NoticeRq.EXT_FIELD_NUMBER, HttpPb.NoticeRq.EXT_FIELD_NUMBER, NoticeRqHandler.class);

        // 获取玩家信息
        registerH(HttpPb.GetLordBaseRq.EXT_FIELD_NUMBER, 0, GetLordBaseRqHandler.class);
        // 修改区服信息
        registerH(HttpPb.ModifyServerInfoRq.EXT_FIELD_NUMBER, 0, ModifyServerInfoHandler.class);

        // 修改VIP
        registerH(HttpPb.ModVipRq.EXT_FIELD_NUMBER, HttpPb.ModVipRq.EXT_FIELD_NUMBER, ModVipRqHandler.class);
        registerH(HttpPb.ModLordRq.EXT_FIELD_NUMBER, 0, ModLordRqHandler.class);
        registerH(HttpPb.ModPropRq.EXT_FIELD_NUMBER, 0, ModPropRqHandler.class);
        registerH(HttpPb.ModNameRq.EXT_FIELD_NUMBER, 0, ModNameRqHandler.class);

        // 支付订单号生成返回
        registerH(HttpPb.PayApplyRs.EXT_FIELD_NUMBER, 0, PayApplyRsHandler.class);
        registerH(HttpPb.PayBackRq.EXT_FIELD_NUMBER, 0, PayBackRqHandler.class);

        // 机器人
        registerH(HttpPb.RobotsExternalBehaviorRq.EXT_FIELD_NUMBER, 0, RobotsExternalBehaviorRqHandler.class);
        registerH(HttpPb.RobotsCountByAreaRq.EXT_FIELD_NUMBER, 0, RobotsCountByAreaRqHandler.class);

    }

    private void innerMessagePool() {
        // 跨服协议注册 是跨服 -> 游戏服的 (非直接转发的)
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
            return new ClientRoutHandler(); // 跨服直接发过来给客户端的
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
     * 注册同一个功能解锁id的某个协议段内的所有协议号
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
            if (rsMsgCmd.containsKey(cmd)) {// 当前只处理需要返回的协议
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
