package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName CampMember.java
 * @Description 阵营成员信息
 * @author
 * @date
 *
 */
public class CampMember {
	private long roleId;
	private int buildDate;// 玩家最后一次记录军团建设的日期
	private int build;// 玩家当天在军团中的建设次数
	private int cityDate;// 最后一次记录城战的日期
	private int cityBattle;// 记录玩家参与城战次数
	private int campDate;// 最后一次记录玩家参加阵营战的日期
	private int campBattle;// 记录玩家参与阵营战次数
	private int honorDate;// 记录玩家最后一次领取军团荣誉礼包的日期
	private int honorGift;// 记录玩家已领取的军团荣誉礼包，格式：101，表示1和3礼包已领取，2礼包未领取
	private int jobVote;// 玩家拥有的官员选举选票数
	private int canvass;// 玩家拉票次数
	private int taskTime;// 玩家军团任务刷新时间
	private int taskAwardCnt;// 玩家军团任务额外奖励次数
	private int taskLv;//军团任务等级

	private List<Integer> honorReward;// 记录玩家已领取的军团荣誉礼包index

	public CampMember() {
	}

	/**
	 * 刷新玩家军团成员相关信息
	 */
	public void refreshData() {
		int today = TimeHelper.getCurrentDay();
		if (buildDate != today) {
			build = 0;
		}

		if (cityDate != today) {
			cityBattle = 0;
		}

		if (campDate != today) {
			campBattle = 0;
		}

		if (honorDate != today) {
			honorGift = 0;
			if (null != honorReward) {
				honorReward.clear();
			}
		}
	}

	/**
	 * 记录玩家军团建设次数
	 */
	public void partyBuild() {
		build++;
		buildDate = TimeHelper.getCurrentDay();
	}

	public void cityBattle() {
		cityBattle++;
		cityDate = TimeHelper.getCurrentDay();
	}

	public void campBattle() {
		campBattle++;
		campDate = TimeHelper.getCurrentDay();
	}

	/**
	 * 记录玩家已领取的军团荣誉礼包索引
	 * 
	 * @param index
	 */
	public void recordHonorGiftReward(int index) {
		honorReward.add(index);

		honorGift += Math.pow(10, index - 1);// 数字记录方式，从右向左数，第1位表示index为1的礼包领取记录，领取为1，未领取为0

		setHonorDate(TimeHelper.getCurrentDay());
	}

	/**
	 * 返回玩家已经领取过的所有军团荣誉礼包index
	 * 
	 * @return
	 */
	public List<Integer> getHonorRewardIndex() {
		if (null == honorReward) {
			honorReward = new ArrayList<>();

			// 数字记录方式，从右向左数，第1位表示index为1的礼包领取记录，如果已领取，该位的值为1，未领取为0
			int num = honorGift;
			int count = 0;
			int flag;
			while (num > 0) {
				count++;
				flag = num % 10;
				if (flag == 1) {
					honorReward.add(count);
				}
				num /= 10;
			}
		}
		return honorReward;
	}

	/**
	 * 添加选票
	 * 
	 * @param add
	 * @return
	 */
	public int addVote(int add) {
		if (add > 0) {
			jobVote += add;
		}
		return jobVote;
	}

	/**
	 * 清空玩家上次选举投票数据
	 */
	public void clearLastEceltionData() {
		jobVote = 0;
		canvass = 0;
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public int getBuildDate() {
		return buildDate;
	}

	public void setBuildDate(int buildDate) {
		this.buildDate = buildDate;
	}

	public int getBuild() {
		return build;
	}

	public void setBuild(int build) {
		this.build = build;
	}

	public int getCityBattle() {
		return cityBattle;
	}

	public void setCityBattle(int cityBattle) {
		this.cityBattle = cityBattle;
	}

	public int getCampBattle() {
		return campBattle;
	}

	public void setCampBattle(int campBattle) {
		this.campBattle = campBattle;
	}

	public int getHonorDate() {
		return honorDate;
	}

	public void setHonorDate(int honorDate) {
		this.honorDate = honorDate;
	}

	public int getHonorGift() {
		return honorGift;
	}

	public void setHonorGift(int honorGift) {
		this.honorGift = honorGift;
	}

	public int getJobVote() {
		return jobVote;
	}

	public void setJobVote(int jobVote) {
		this.jobVote = jobVote;
	}

	public int getCityDate() {
		return cityDate;
	}

	public void setCityDate(int cityDate) {
		this.cityDate = cityDate;
	}

	public int getCampDate() {
		return campDate;
	}

	public void setCampDate(int campDate) {
		this.campDate = campDate;
	}

	public int getCanvass() {
		return canvass;
	}

	public void setCanvass(int canvass) {
		this.canvass = canvass;
	}

	public int getTaskTime() {
		return taskTime;
	}

	public void setTaskTime(int taskTime) {
		this.taskTime = taskTime;
	}

	public int getTaskAwardCnt() {
		return taskAwardCnt;
	}

	public void setTaskAwardCnt(int taskAwardCnt) {
		this.taskAwardCnt = taskAwardCnt;
	}

	public int getTaskLv() {
        return taskLv;
    }

    public void setTaskLv(int taskLv) {
        this.taskLv = taskLv;
    }

    @Override
	public String toString() {
		return "CampMember [roleId=" + roleId + ", buildDate=" + buildDate + ", build=" + build + ", cityDate="
				+ cityDate + ", cityBattle=" + cityBattle + ", campDate=" + campDate + ", campBattle=" + campBattle
				+ ", honorDate=" + honorDate + ", honorGift=" + honorGift + ", jobVote=" + jobVote + ", canvass="
				+ canvass + ", honorReward=" + honorReward + "]";
	}
}
