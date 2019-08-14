package com.cloudpaas.common.result;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author 大鱼
 *
 * @date 2019年7月26日 下午2:41:38
 */
public class TableResultResponse<T> extends BaseResponse {
	
	long count;

    List<T> data;

    public TableResultResponse(long total, List<T> rows) {
        this.data =  rows;
        this.count = total;
    }

    public TableResultResponse() {
        this.data = new ArrayList<T>();
    }

    TableResultResponse<T> count(int count) {
        this.setCount(count);
        return this;
    }

    TableResultResponse<T> data(List<T> data) {
        this.setData(data);
        return this;
    }

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

    

    
}
