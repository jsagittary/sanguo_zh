package com.gryphpoem.game.zw.resource.dao.impl.p;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.DbParty;

import java.util.List;

/**
 * @ClassName CampDao.java
 * @Description 军团Dao
 * @author TanDonghai
 * @date 创建时间：2017年4月25日 下午5:07:13
 *
 */
public class CampDao extends BaseDao {
	public DbParty selectPartyByCamp(int camp) {
		return this.getSqlSession().selectOne("CampDao.selectPartyByCamp", camp);
	}

	public List<DbParty> selectParty() {
		return this.getSqlSession().selectList("CampDao.selectParty");
	}

	public int updateParty(DbParty dbParty) {
		return this.getSqlSession().update("CampDao.updateParty", dbParty);
	}
	
	public int replaceParty(DbParty dbParty) {
        return this.getSqlSession().insert("CampDao.replaceParty", dbParty);
    }

    public void save(DbParty dbParty) {
        if (updateParty(dbParty)==0){
             replaceParty(dbParty);
         }
    }
	public void insertParty(DbParty dbParty) {
		this.getSqlSession().insert("CampDao.insertParty", dbParty);
	}
	
	
	
}
