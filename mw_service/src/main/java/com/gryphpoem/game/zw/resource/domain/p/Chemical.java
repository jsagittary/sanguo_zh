package com.gryphpoem.game.zw.resource.domain.p;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.gryphpoem.game.zw.pb.CommonPb.ChemicalQue;

/**
 * 化工厂
 * 
 * @author tyler
 *
 */
public class Chemical {
	private int expandLv;
	private Map<String, ChemicalQue> posQue;

	public Chemical() {
		expandLv = 1;
		this.posQue = new ConcurrentHashMap<>();
	}

	public int getExpandLv() {
		return expandLv;
	}

	public void setExpandLv(int expandLv) {
		this.expandLv = expandLv;
	}

	public Map<String, ChemicalQue> getPosQue() {
		return posQue;
	}

	public void setPosQue(Map<String, ChemicalQue> posQue) {
		this.posQue = posQue;
	}

}