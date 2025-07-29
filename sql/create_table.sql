-- 创建数据库
create database if not exists yuchuang;

-- 使用数据库
use yuchuang;

-- 创建用户表
create table if not exists user
(
    id            bigint primary key auto_increment comment '主键',
    user_name      varchar(20)   not null comment '用户名',
    user_account  varchar(20)   not null comment '账号',
    user_password varchar(128)   not null comment '密码',
    user_role     varchar(20)   not null comment '用户角色：user/admin',
    user_avatar   varchar(1024) null comment '用户头像',
    user_profile  varchar(512)  null comment '用户简介',
    create_time   datetime default CURRENT_TIMESTAMP comment '创建时间',
    edit_time     datetime default CURRENT_TIMESTAMP comment '编辑时间',
    update_time    datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     int comment '逻辑删除',
    unique key uk_userAccount (user_account),
    index idx_name (username)
    ) comment '用户表';
