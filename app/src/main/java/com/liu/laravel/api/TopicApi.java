package com.liu.laravel.api;

import com.liu.laravel.bean.topic.TopicList;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * 项目名称：Laravel
 * 类描述：
 * 创建人：liuxuhui
 * 创建时间：2017/3/15 16:23
 * 修改人：liuxuhui
 * 修改时间：2017/3/15 16:23
 * 修改备注：
 */

public interface TopicApi {

    /**
     * topic列表数据
     * @param options
     * @return
     */
    @GET("topics")
    Observable<TopicList> getTopics(@QueryMap Map<String, String> options);
}
