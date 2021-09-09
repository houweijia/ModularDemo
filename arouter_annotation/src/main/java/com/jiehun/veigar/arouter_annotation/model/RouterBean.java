package com.jiehun.veigar.arouter_annotation.model;


import javax.lang.model.element.Element;

public class RouterBean {



    public enum Type{
        ACTIVITY
    }
    //枚举类型
    private Type    type;
    //类节点
    private Element element;
    //被@ARouter注解的类对象
    private Class<?> clazz;
    //路由的组名
    private String group;
    //路由的地址
    private String path;

    private RouterBean(Builder builder){
        this.type = builder.type;
        this.element = builder.element;
        this.group = builder.group;
        this.path = builder.path;
        this.clazz = builder.clazz;
    }

    private RouterBean(Type type, Class<?> clazz, String path, String group) {
        this.group = group;
        this.path =path;
        this.type = type;
        this.clazz = clazz;
    }

    //对外还提供了一种简单的实例化方法
    public static RouterBean create(Type type,Class<?> clazz,String path,String group){
        return new RouterBean(type,clazz,path,group);
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Type getType() {
        return type;
    }

    public Element getElement() {
        return element;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getGroup() {
        return group;
    }

    public String getPath() {
        return path;
    }

    public static class Builder{
        // 枚举类型：Activity
        private Type type;

        //类节点
        private Element element;

        private String group;
        //路由的地址
        private String path;

        private Class<?> clazz;

        public Builder setElement(Element element) {
            this.element = element;
            return this;
        }

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }


        public RouterBean build(){
            if(path==null || path.length()==0){
                throw new IllegalArgumentException("path必填项为空，如：/app/MainActivity");
            }
            return new RouterBean(this);
        }
    }


    @Override
    public String toString() {
        return "RouterBean{" +
                "type=" + type +
                ", element=" + element +
                ", clazz=" + clazz +
                ", group='" + group + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
