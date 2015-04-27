﻿/**
 * WeiXin
 * @title Context.java
 * @package com.chn.wx.trans
 * @author lzxz1234<lzxz1234@gmail.com>
 * @date 2014年12月16日-下午4:45:01
 * @version V1.0
 * Copyright (c) 2014 ChineseAll.com All Right Reserved
 */
package com.chn.wx.dto;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.chn.common.Castors;
import com.chn.common.FieldUtils;
import com.chn.common.StringUtils;
import com.chn.wx.annotation.Param;
import com.chn.wx.util.HttpUtils;

/**
 * @class Context
 * @author lzxz1234
 * @description 
 * @version v1.0
 */
public class Context {
    
    private static final Logger log = Logger.getLogger(Context.class);
    
    public static final String REQUEST = "request";
    public static final String RESPONSE = "response";
    
    private Map<String, Object> map;
    
    public Context(HttpServletRequest req, HttpServletResponse resp) {
        
        this.map = HttpUtils.decodeParams(req);
        this.addAttribute(REQUEST, req);
        this.addAttribute(RESPONSE, resp);
    }
    
    public HttpServletRequest getRequest() {
        
        return (HttpServletRequest) map.get(REQUEST);
    }
    
    public void setAttribute(String key, Object value) {
        
        this.map.put(key, value);
    }
    
    public void addAttribute(String key, Object value) {
        
        Object previous = this.map.put(key, value);
        if(previous != null) 
            log.info(String.format("参数[%s]原值[%s]被[%s]覆盖！", key, previous, value));
    }
    
    public void removeAttribute(String key) {
        
        this.map.remove(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(Class<T> clazz, String key) {
        
        return (T)getAttribute(key);
    }
    
    public Object getAttribute(String key) {
        
        if(key == null) return null;
        return this.map.get(key);
    }
    
    public Map<String, Object> getAttributes() {
        
        return new HashMap<>(map);
    }
    
    public void injectField(Object target) {
        
        for(Field field : FieldUtils.getFields(target.getClass(), Param.class)) {
            Param param = field.getAnnotation(Param.class);
            if(param.required()) {
                String paramName = param.value();
                //兼容不指定param名称时采用字段名
                if(StringUtils.isEmpty(paramName)) paramName = field.getName();
                Object value = this.map.get(paramName);
                try {
                    if(value == null && !StringUtils.isEmpty(param.defaultValue()))
                        value = param.defaultValue();
                    field.set(target, Castors.cast(field.getType(), value));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("不被支持的注入类型！", e);
                } catch (IllegalAccessException e) {
                    //Ignore
                }
            }
        }
    }
    
}
