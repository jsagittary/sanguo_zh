package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.MailCollect;
import com.gryphpoem.game.zw.pb.CommonPb.MailScout;

import java.util.List;

/**
 * @ClassName Mail.java
 * @Description 邮件信息
 * @author TanDonghai
 * @date 创建时间：2017年4月4日 下午6:07:39
 *
 */
public class Mail {
	private long originator;
	private int keyId;
	private int moldId;
	private int state;
	private int type;
	private int time;
	private int lv;
	private int vipLv;
	private String title;
	private String content;
	private String sendName;
	private List<String> toName;
	private List<String> tParam;
	private List<String> cParam;
	private List<Award> rewardList;
	private List<Award> dropList;
	private MailCollect collect;
	private MailScout scout;
	private int lock = 0;
	private List<Award> recoverList;
	private CommonPb.SandTableEnrollMailInfo enrollMailInfo;
	private CommonPb.SandTableRoundOverMailInfo roundOverMailInfo;
	/** 战报状态 2: 邮件存在战报 1: 邮件战报已删除*/
	private volatile int reportStatus;
	private boolean isCross;

	public Mail() {
	}

    public Mail(com.gryphpoem.game.zw.pb.CommonPb.Mail mail) {
        setKeyId(mail.getKeyId());
        setType(mail.getType());
        setState(mail.getState());
        setTime(mail.getTime());
        setMoldId(mail.getMoldId());
        setLv(mail.getLv());
        setVipLv(mail.getVipLv());

        if (mail.hasTitle()) {
            setTitle(mail.getTitle());
        }
        if (mail.hasSendName()) {
            setSendName(mail.getSendName());
        }
        if (mail.hasContent()) {
            setContent(mail.getContent());
        }
        if (mail.hasCollect()) setCollect(mail.getCollect());
        if (mail.hasScout()) {
            setScout(mail.getScout());
        }
        setDropList(mail.getDropList());
		setToName(mail.getToNameList());
		setRewardList(mail.getAwardList());
		settParam(mail.getTParamList());
		setcParam(mail.getCParamList());
		setLock(mail.getLock());
		setRecoverList(mail.getRecoverList());
		if(mail.hasEnrollInfo()){
			setEnrollMailInfo(mail.getEnrollInfo().toBuilder().build());
		}
		if(mail.hasRoundOverInfo()){
			setRoundOverMailInfo(mail.getRoundOverInfo().toBuilder().build());
		}
		setReportStatus(mail.getReportStatus());
		setCross(mail.getIsCross());
		setOriginator(mail.getOriginator());
	}

	public Mail(long originator, int keyId, int type, int moldId, int state, int time) {
		this(keyId, type, moldId, state, time);
		this.originator = originator;
	}

	public Mail(int keyId, int type, int moldId, int state, int time) {
		this.keyId = keyId;
		this.type = type;
		this.state = state;
		this.moldId = moldId;
		this.time = time;
	}

	public Mail(int keyId, int type, int state, int time, String title, String content, String sendName, int lv,
			int vipLv) {
		this.keyId = keyId;
		this.type = type;
		this.state = state;
		this.time = time;
		this.title = title;
		this.content = content;
		this.sendName = sendName;
		this.lv = lv;
		this.vipLv = vipLv;
	}

	public int getLock() {
		return lock;
	}

	public void setLock(int lock) {
		this.lock = lock;
	}

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getMoldId() {
		return moldId;
	}

	public void setMoldId(int moldId) {
		this.moldId = moldId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isCross() {
		return isCross;
	}

	public void setCross(boolean cross) {
		isCross = cross;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public int getVipLv() {
		return vipLv;
	}

	public void setVipLv(int vipLv) {
		this.vipLv = vipLv;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSendName() {
		return sendName;
	}

	public void setSendName(String sendName) {
		this.sendName = sendName;
	}

	public List<String> getToName() {
		return toName;
	}

	public void setToName(List<String> toName) {
		this.toName = toName;
	}

	public List<String> gettParam() {
		return tParam;
	}

	public void settParam(List<String> tParam) {
		this.tParam = tParam;
	}

	public List<String> getcParam() {
		return cParam;
	}

	public void setcParam(List<String> cParam) {
		this.cParam = cParam;
	}

	public List<Award> getRewardList() {
		return rewardList;
	}

	public void setRewardList(List<Award> rewardList) {
		this.rewardList = rewardList;
	}

	public List<Award> getDropList() {
		return dropList;
	}

	public void setDropList(List<Award> dropList) {
		this.dropList = dropList;
	}

	public MailCollect getCollect() {
		return collect;
	}

	public void setCollect(MailCollect collect) {
		this.collect = collect;
	}

	public MailScout getScout() {
		return scout;
	}

	public void setScout(MailScout scout) {
		this.scout = scout;
	}

	public List<Award> getRecoverList() {
		return recoverList;
	}

	public void setRecoverList(List<Award> recoverList) {
		this.recoverList = recoverList;
	}

	public CommonPb.SandTableEnrollMailInfo getEnrollMailInfo() {
		return enrollMailInfo;
	}

	public void setEnrollMailInfo(CommonPb.SandTableEnrollMailInfo enrollMailInfo) {
		this.enrollMailInfo = enrollMailInfo;
	}

	public CommonPb.SandTableRoundOverMailInfo getRoundOverMailInfo() {
		return roundOverMailInfo;
	}

	public void setRoundOverMailInfo(CommonPb.SandTableRoundOverMailInfo roundOverMailInfo) {
		this.roundOverMailInfo = roundOverMailInfo;
	}

	public int getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(int reportStatus) {
		this.reportStatus = reportStatus;
	}

	public long getOriginator() {
		return originator;
	}

	public void setOriginator(long originator) {
		this.originator = originator;
	}
}
