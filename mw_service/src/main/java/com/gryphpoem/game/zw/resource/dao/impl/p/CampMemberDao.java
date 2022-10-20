package com.gryphpoem.game.zw.resource.dao.impl.p;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.CampMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TanDonghai
 * @ClassName CampMemberDao.java
 * @Description 阵营成员Dao
 * @date 创建时间：2017年4月25日 下午5:07:13
 */
public class CampMemberDao extends BaseDao {

    public CampMember selectPartyMember(long roleId) {
        return this.getSqlSession().selectOne("CampMemberDao.selectPartyMember", roleId);
    }

    public int updatePartyMember(CampMember member) {
        return this.getSqlSession().update("CampMemberDao.updatePartyMember", member);
    }

    public void insertPartyMember(CampMember member) {
        this.getSqlSession().insert("CampMemberDao.insertPartyMember", member);
    }

    public List<CampMember> load() {
        List<CampMember> list = new ArrayList<>();
        long curIndex = 0L;
        int count = 1000;
        int pageSize = 0;
        while (true) {
            List<CampMember> page = load(curIndex, count);
            pageSize = page.size();
            if (pageSize > 0) {
                list.addAll(page);
                curIndex = page.get(pageSize - 1).getRoleId();
            } else {
                break;
            }

            if (pageSize < count) {
                break;
            }
        }
        return list;
    }

    private List<CampMember> load(long curIndex, int count) {
        HashMap<String, Object> params = paramsMap();
        params.put("curIndex", curIndex);
        params.put("count", count);
        return this.getSqlSession().selectList("CampMemberDao.load", params);
    }
}
