package com.jiehun.veigar.common;

/**
 * @description:
 * @author: houwj
 * @date: 2019/7/24
 */
public class PathBean {
    private String path;
    private Class clazz;

    public PathBean() {
    }

    public PathBean(String path, Class clazz) {
        this.path = path;
        this.clazz = clazz;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }
}
