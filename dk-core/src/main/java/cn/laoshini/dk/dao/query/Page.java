package cn.laoshini.dk.dao.query;

import java.util.List;

/**
 * 分页查询结果
 *
 * @author fagarine
 */
public class Page<Type> {

    private int pageNo = 1;

    private int pageSize;

    private long total;

    private List<Type> result;

    public Page() {
    }

    public Page(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public Page(int pageNo, int pageSize, List<Type> result) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        if (result != null) {
            this.total = result.size();
        }
        this.result = result;
    }

    public Page(int pageNo, int pageSize, long total, List<Type> result) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.total = total;
        this.result = result;
    }

    /**
     * 返回总页数，如果是0条数据，总页数也是1页
     *
     * @return
     */
    public int getTotalPage() {
        if (pageSize <= 0) {
            return 1;
        }
        int totalPage = (int) (total / pageSize + (total % pageSize == 0 ? 0 : 1));
        totalPage = total == 0 ? 1 : totalPage;
        return totalPage;
    }

    /**
     * 返回偏移量，如果越界，则返回最后一页的偏移量
     *
     * @return
     */
    public int getOffset() {
        if (total == 0) {
            return 0;
        }
        if ((pageNo - 1) * pageSize >= total) {
            pageNo = getTotalPage();
        }
        return (pageNo - 1) * pageSize;
    }

    public int getPageNo() {
        if (pageNo > this.getTotalPage()) {
            return this.getTotalPage();
        } else if (pageNo < 1) {
            return 1;
        }
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<Type> getResult() {
        return result;
    }

    public void setResult(List<Type> result) {
        this.result = result;
    }
}
