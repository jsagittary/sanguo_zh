package com.gryphpoem.game.zw.resource.domain.p;

import java.util.Comparator;

import com.gryphpoem.game.zw.pb.CommonPb;

public class SimpleRank implements Comparator<SimpleRank> {
	private long date;
	private String name;
	private int pecent;
	private int value;
	private CommonPb.SimpleRank.Builder build;

	public SimpleRank(long date, String name, int pecent, int value) {
		this.date = date;
		this.name = name;
		this.pecent = pecent;
		this.value = value;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPecent() {
		return pecent;
	}

	public void setPecent(int pecent) {
		this.pecent = pecent;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public CommonPb.SimpleRank serialize() {
		if (build == null) {
			build = CommonPb.SimpleRank.newBuilder();
			build.setDate(date);
			build.setName(name);
			build.setPecent(pecent);
			build.setValue(value);
		}
		return build.build();
	}

	@Override
	public int compare(SimpleRank o1, SimpleRank o2) {
		return o2.getValue() - o1.getValue();
	}

}
