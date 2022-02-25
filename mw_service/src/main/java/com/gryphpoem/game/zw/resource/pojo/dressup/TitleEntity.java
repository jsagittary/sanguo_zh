package com.gryphpoem.game.zw.resource.pojo.dressup;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardType;

import java.util.List;

/**
 * @author zhangzxy
 * @date 创建时间:2021/12/16
 * @Description
 */
public class TitleEntity extends BaseDressUpEntity{

	private long progress;

	public TitleEntity(CommonPb.DressUpEntity data) {
		super(data);
		CommonPb.TitleEntity te = data.getTe();
		this.progress = te.getProgress();
	}
	public TitleEntity(int id, boolean permanentHas) {
		super(AwardType.TITLE, id, permanentHas);
	}
	@Override
	public List<List<Integer>> convertProps() {
		return null;
	}

	public long getProgress() {
		return progress;
	}

	public void setProgress(long progress) {
		this.progress = progress;
	}
	
	@Override
	public CommonPb.DressUpEntity.Builder toData() {
		CommonPb.DressUpEntity.Builder builder = super.toData();
		builder.setTe(CommonPb.TitleEntity.newBuilder().setProgress(progress).build());
		return builder;
	}
}
