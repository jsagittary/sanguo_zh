package com.gryphpoem.game.zw.resource.domain.p;

/**
 * 历史记录
 * 
 * @author tyler
 *
 */
public class History {
	private int id;
	private int param;

	public History(int id, int param) {
		this.id = id;
		this.param = param;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getParam() {
		return param;
	}

	public void setParam(int param) {
		this.param = param;
	}

    @Override
    public String toString() {
        return "History [id=" + id + ", param=" + param + "]";
    }

}
