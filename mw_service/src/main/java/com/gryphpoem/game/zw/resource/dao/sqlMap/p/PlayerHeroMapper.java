package com.gryphpoem.game.zw.resource.dao.sqlMap.p;

import com.gryphpoem.game.zw.resource.domain.p.DbPlayerHero;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-08-12 17:43
 */
public interface PlayerHeroMapper {
    @Select("select * from p_hero where #{lordId}")
    @Results({
            @Result(column = "lord_id", property = "lordId"),
            @Result(column = "hero_biography", property = "heroBiography")
    })
    DbPlayerHero selectByLordId(@Param("lordId") long lordId);

    @Insert({"insert into p_hero(lord_id, hero_biography) values(#{lordId},#{heroBiography})"})
    int insert(DbPlayerHero playerHero);

    @Update({"update p_hero set hero_biography=#{heroBiography} where lord_id = #{lordId}"})
    int update(DbPlayerHero playerHero);

    @Select("select * from p_hero  where lord_id > #{curIndex} order by lord_id limit 0,#{count}")
    @Results({
            @Result(column = "lord_id", property = "lordId"),
            @Result(column = "hero_biography", property = "heroBiography")
    })
    List<DbPlayerHero> selectList(Map<String, Object> params);
}
