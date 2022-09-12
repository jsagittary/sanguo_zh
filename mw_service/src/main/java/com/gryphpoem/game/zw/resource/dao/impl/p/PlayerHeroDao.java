package com.gryphpoem.game.zw.resource.dao.impl.p;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.dao.sqlMap.p.PlayerHeroMapper;
import com.gryphpoem.game.zw.resource.domain.p.DbPlayerHero;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-08-12 17:48
 */
public class PlayerHeroDao extends BaseDao {
    public DbPlayerHero selectPlayerHero(long lordId) {
        return this.getMapper(PlayerHeroMapper.class).selectByLordId(lordId);
    }

    public int insertPlayerHero(DbPlayerHero playerHero) {
        return this.getMapper(PlayerHeroMapper.class).insert(playerHero);
    }

    public int updatePlayerHero(DbPlayerHero playerHero) {
        return this.getMapper(PlayerHeroMapper.class).update(playerHero);
    }

    public int replacePlayerHero(DbPlayerHero playerHero) {
        return this.getMapper(PlayerHeroMapper.class).insert(playerHero);
    }


    public void save(DbPlayerHero playerHero) {
        if (updatePlayerHero(playerHero) == 0) {
            replacePlayerHero(playerHero);
        }
    }

    public List<DbPlayerHero> load() {
        List<DbPlayerHero> list = new ArrayList<>();
        long curIndex = 0L;
        int count = 1000;
        int pageSize = 0;
        while (true) {
            List<DbPlayerHero> page = load(curIndex, count);
            pageSize = page.size();
            if (pageSize > 0) {
                list.addAll(page);
                curIndex = page.get(pageSize - 1).getLordId();
            } else {
                break;
            }

            if (pageSize < count) {
                break;
            }
        }
        return list;
    }

    private List<DbPlayerHero> load(long curIndex, int count) {
        Map<String, Object> params = paramsMap();
        params.put("curIndex", curIndex);
        params.put("count", count);
        return this.getMapper(PlayerHeroMapper.class).selectList(params);
    }
}
