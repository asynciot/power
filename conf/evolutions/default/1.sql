# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table account (
  id                            varchar(255) not null,
  username                      varchar(255) not null,
  nickname                      varchar(255) not null,
  password                      varchar(255),
  mobile                        varchar(255),
  wechat                        varchar(255),
  wechat_id                     varchar(255),
  sex                           tinyint(1) default 0,
  birthday                      datetime(6),
  profession                    varchar(255),
  introduction                  varchar(255),
  email                         varchar(255),
  create_time                   datetime(6),
  portrait                      varchar(255),
  maxfollow                     integer,
  augroup                       integer,
  constraint pk_account primary key (id)
);

create table artlocation (
  id                            integer auto_increment not null,
  lat                           varchar(255),
  lon                           varchar(255),
  user_id                       varchar(255),
  t_create                      varchar(255),
  constraint pk_artlocation primary key (id)
);

create table binaries (
  id                            integer auto_increment not null,
  name                          varchar(255),
  type                          varchar(255),
  t_create                      datetime(6),
  data                          varbinary(255),
  constraint pk_binaries primary key (id)
);

create table cellocation (
  id                            integer auto_increment not null,
  cell_mcc                      integer,
  cell_mnc                      integer,
  cell_lac                      integer,
  cell_cid                      integer,
  lat                           double,
  lon                           double,
  radius                        double,
  address                       varchar(255),
  constraint pk_cellocation primary key (id)
);

create table chat (
  id                            integer auto_increment not null,
  title                         varchar(255),
  content                       varchar(255),
  follow                        integer,
  type                          integer not null,
  from_id                       varchar(32) not null,
  info                          varchar(255),
  create_time                   datetime(6) not null,
  constraint pk_chat primary key (id)
);

create table commands (
  id                            integer auto_increment not null,
  imei                          varchar(255),
  command                       varchar(255),
  `int1`                        integer,
  `int2`                        integer,
  `int3`                        integer,
  `int4`                        integer,
  str1                          varchar(255),
  str2                          varchar(255),
  str3                          varchar(255),
  str4                          varchar(255),
  contract                      varbinary(255),
  binary_id                     integer,
  result                        varchar(255),
  submit                        datetime(6),
  execute                       datetime(6),
  finish                        datetime(6),
  `binary`                      varbinary(255),
  constraint pk_commands primary key (id)
);

create table credentials (
  id                            integer auto_increment not null,
  device_id                     integer,
  credential                    varbinary(255),
  constraint pk_credentials primary key (id)
);

create table device_info (
  id                            integer auto_increment not null,
  imei                          varchar(255),
  iplocation_id                 integer,
  cellocation_id                integer,
  device_name                   varchar(255),
  maintenance_type              varchar(255),
  maintenance_nexttime          varchar(255),
  maintenance_remind            varchar(255),
  maintenance_lasttime          varchar(255),
  inspection_type               varchar(255),
  inspection_lasttime           varchar(255),
  inspection_nexttime           varchar(255),
  inspection_remind             varchar(255),
  install_date                  varchar(255),
  install_addr                  varchar(255),
  register                      varchar(255),
  tagcolor                      varchar(255),
  state                         varchar(255),
  device_type                   varchar(255),
  commond                       varchar(255),
  delay                         varchar(255),
  rssi                          integer,
  runtime_state                 integer,
  group_id                      integer,
  constraint pk_device_info primary key (id)
);

create table devices (
  id                            integer auto_increment not null,
  t_create                      datetime(6),
  t_update                      datetime(6),
  t_logon                       datetime(6),
  t_logout                      datetime(6),
  dock_id                       integer,
  board                         varchar(255),
  cellular                      varchar(255),
  firmware                      varchar(255),
  imei                          varchar(255) not null,
  imsi                          varchar(255),
  device                        varchar(255),
  model                         varchar(255),
  contract_id                   varbinary(255),
  cell_mcc                      integer,
  cell_mnc                      integer,
  cell_lac                      integer,
  cell_cid                      integer,
  ipaddr                        varchar(255),
  constraint uq_devices_imei unique (imei),
  constraint pk_devices primary key (id)
);

create table dispatch (
  id                            integer auto_increment not null,
  device_id                     integer,
  user_id                       varchar(255),
  order_id                      integer,
  order_type                    integer,
  state                         varchar(255),
  phone                         varchar(255),
	expect                        varchar(255),
  result                        varchar(255),
  create_time                   varchar(255),
  finish_time                   varchar(255),
  confirm_time                  varchar(255),
  before_pic                    varchar(255),
  after_pic                     varchar(255),
  confirm_pic                   varchar(255),
  remarks                       varchar(255),
  constraint pk_dispatch primary key (id)
);

create table docks (
  id                            integer auto_increment not null,
  name                          varchar(255),
  `desc`                        varchar(255),
  t_create                      datetime(6),
  t_update                      datetime(6),
  t_logon                       datetime(6),
  t_logout                      datetime(6),
  ipaddr                        varchar(255),
  uuid                          varchar(255),
  constraint pk_docks primary key (id)
);

create table events (
  id                            integer auto_increment not null,
  device_id                     integer,
  time                          datetime(6),
  `length`                      integer,
  `interval`                    integer,
  data                          varbinary(255),
  constraint pk_events primary key (id)
);

create table follow (
  id                            integer auto_increment not null,
  user_id                       varchar(255),
  imei                          varchar(255),
  device_id                     integer,
  create_time                   datetime(6),
  ftype                         varchar(255),
  constraint pk_follow primary key (id)
);

create table follow_ladder (
  id                            integer auto_increment not null,
  user_id                       varchar(255),
  ctrl                          varchar(255),
  door1                         varchar(255),
  door2                         varchar(255),
  ladder_id                     integer,
  create_time                   datetime(6),
  constraint pk_follow_ladder primary key (id)
);

create table group (
  id                            integer auto_increment not null,
  leader                        varchar(255),
  name                          varchar(255),
  mobile                        integer,
  create_time                   datetime(6),
  region                        varchar(255),
  constraint pk_group primary key (id)
);

create table iplocation (
  id                            integer auto_increment not null,
  ip                            varchar(255),
  area                          varchar(255),
  area_id                       varchar(255),
  city                          varchar(255),
  city_id                       varchar(255),
  country                       varchar(255),
  country_id                    varchar(255),
  county                        varchar(255),
  county_id                     varchar(255),
  isp                           varchar(255),
  region                        varchar(255),
  region_id                     varchar(255),
  constraint pk_iplocation primary key (id)
);

create table ladder (
  id                            integer auto_increment not null,
  name                          varchar(255),
  t_create                      datetime(6),
  t_update                      datetime(6),
  t_logon                       datetime(6),
  t_logout                      datetime(6),
  cell_mcc                      integer,
  cell_mnc                      integer,
  cell_lac                      integer,
  cell_cid                      integer,
  ipaddr                        varchar(255),
  ctrl                          varchar(255),
  door1                         varchar(255),
  door2                         varchar(255),
  install_addr                  varchar(255),
  constraint pk_ladder primary key (id)
);

create table logs (
  id                            integer auto_increment not null,
  dock_id                       integer,
  device_id                     integer,
  time                          datetime(6),
  constraint pk_logs primary key (id)
);

create table mess_record (
  id                            integer auto_increment not null,
  title                         varchar(255),
  content                       varchar(255),
  type                          integer not null,
  to_id                         varchar(32) not null,
  device_id                     integer,
  info                          varchar(255),
  create_time                   datetime(6) not null,
  constraint pk_mess_record primary key (id)
);

create table message (
  id                            integer auto_increment not null,
  title                         varchar(255),
  content                       varchar(255),
  type                          integer not null,
  to_id                         varchar(32) not null,
  from_id                       varchar(32) not null,
  info                          varchar(255),
  create_time                   datetime(6) not null,
  is_settled                    tinyint(1) default 0 not null,
  constraint pk_message primary key (id)
);

create table monitor (
  id                            integer auto_increment not null,
  device_id                     integer,
  session                       integer,
  sequence                      integer,
  `length`                      integer,
  `interval`                    integer,
  time                          datetime(6),
  data                          varbinary(255),
  constraint pk_monitor primary key (id)
);

create table `order` (
  id                            integer auto_increment not null,
  device_id                     integer,
  type                          integer,
  create_time                   varchar(255),
  state                         varchar(255),
  code                          integer,
  device_type                   varchar(255),
  producer                      varchar(255),
  islast                        integer,
  constraint pk_order primary key (id)
);

create table runtime (
  id                            integer auto_increment not null,
  device_id                     integer,
  type                          integer,
  data                          varbinary(255),
  t_update                      datetime(6),
  constraint pk_runtime primary key (id)
);

create table sms_record (
  id                            integer auto_increment not null,
  mobile                        varchar(255) not null,
  code                          varchar(4) not null,
  timestamp                     datetime(6) not null,
  constraint pk_sms_record primary key (id)
);

create index ix_account_create_time on account (create_time);
create index ix_sms_record_mobile on sms_record (mobile);

# --- !Downs

drop table if exists account;

drop table if exists artlocation;

drop table if exists binaries;

drop table if exists cellocation;

drop table if exists chat;

drop table if exists commands;

drop table if exists credentials;

drop table if exists device_info;

drop table if exists devices;

drop table if exists dispatch;

drop table if exists docks;

drop table if exists events;

drop table if exists follow;

drop table if exists follow_ladder;

drop table if exists group;

drop table if exists iplocation;

drop table if exists ladder;

drop table if exists logs;

drop table if exists mess_record;

drop table if exists message;

drop table if exists monitor;

drop table if exists `order`;

drop table if exists runtime;

drop table if exists sms_record;

drop index ix_account_create_time on account;
drop index ix_sms_record_mobile on sms_record;
