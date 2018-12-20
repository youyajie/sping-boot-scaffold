package com.yyj.springbootscaffold.mybatis.query;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通用 Mapper,实现按 BaseExample 的分页查询
 * @param <D>实体类 domain
 * @param <PK> 主键默认Integer
 * Created by yyj on 2018/12/20.
 */
public interface BaseMapper<D, PK> {
    long countByExample(BaseExample example);

    int insert(D record);

    int insertSelective(D record);

    List<D> selectByExample(BaseExample example);

    D selectByPrimaryKey(PK pk);

    int updateByExampleSelective(@Param("record") D record,
                                 @Param("example") BaseExample example);

    int updateByExample(@Param("record") D record, @Param("example") BaseExample example);

    int updateByPrimaryKeySelective(D record);

    int updateByPrimaryKey(D record);

    default Pager<D> listForPager(BaseExample example) {
        Pager<D> pager = new Pager<>();

        Integer offset = example.getOffset();
        Integer pageSize = example.getLimit();

        int currPage = (offset / pageSize) + 1;
        pager.setCurrPage(currPage);
        pager.setPageSize(pageSize);

        long count = countByExample(example);
        if(count <= 0) {
            pager.setRowsCount(0);
            return pager;
        }
        pager.setRowsCount((int)count);

        int pageCount = (int)(count / pageSize);
        if (count % pageSize > 0) {
            pageCount += 1;
        }
        pager.setPageCount(pageCount);

        if (((currPage - 1) * pageSize) < count) {
            List result = selectByExample(example);
            pager.setPageItems(result);
        }

        return pager;
    }
}
