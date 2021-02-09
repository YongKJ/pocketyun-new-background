/*
 Navicat Premium Data Transfer

 Source Server         : 我的阿里云
 Source Server Type    : MySQL
 Source Server Version : 100038
 Source Host           : 47.106.102.217:3306
 Source Schema         : pocket_yun_new

 Target Server Type    : MySQL
 Target Server Version : 100038
 File Encoding         : 65001

 Date: 09/02/2021 09:38:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for py_email
-- ----------------------------
DROP TABLE IF EXISTS `py_email`;
CREATE TABLE `py_email`  (
  `emailUUID` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '邮件UUID',
  `sendUserUUID` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '发送用户UUID',
  `receiveUserUUID` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '接收用户UUID',
  `emailTitle` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '邮件标题',
  `emailContent` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '邮件内容',
  `sendTime` datetime(0) NOT NULL COMMENT '发送时间',
  PRIMARY KEY (`emailUUID`) USING BTREE,
  INDEX `e_u_1`(`sendUserUUID`) USING BTREE,
  INDEX `e_u_2`(`receiveUserUUID`) USING BTREE,
  CONSTRAINT `e_u_1` FOREIGN KEY (`sendUserUUID`) REFERENCES `py_user` (`userUUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `e_u_2` FOREIGN KEY (`receiveUserUUID`) REFERENCES `py_user` (`userUUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for py_paths
-- ----------------------------
DROP TABLE IF EXISTS `py_paths`;
CREATE TABLE `py_paths`  (
  `pathsUUID` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件相对路径UUID',
  `userUUID` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户UUID',
  `path` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件相对路径',
  `filename` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件名',
  `size` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件大小',
  `depth` int(3) NOT NULL COMMENT '路径深度',
  `addTime` datetime(0) NOT NULL COMMENT '文件添加时间',
  `modTime` datetime(0) NOT NULL COMMENT '文件修改时间',
  PRIMARY KEY (`pathsUUID`) USING BTREE,
  INDEX `p_u`(`userUUID`) USING BTREE,
  CONSTRAINT `p_u` FOREIGN KEY (`userUUID`) REFERENCES `py_user` (`userUUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for py_recycle_bin
-- ----------------------------
DROP TABLE IF EXISTS `py_recycle_bin`;
CREATE TABLE `py_recycle_bin`  (
  `pathsUUID` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件相对路径UUID',
  `userUUID` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户UUID',
  `path` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件相对路径',
  `filename` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件名',
  `size` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件大小',
  `depth` int(3) NOT NULL COMMENT '路径深度',
  `addTime` datetime(0) NOT NULL COMMENT '文件添加时间',
  `modTime` datetime(0) NOT NULL COMMENT '文件修改时间',
  PRIMARY KEY (`pathsUUID`) USING BTREE,
  INDEX `p_r_b_u`(`userUUID`) USING BTREE,
  CONSTRAINT `p_r_b_u` FOREIGN KEY (`userUUID`) REFERENCES `py_user` (`userUUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for py_server
-- ----------------------------
DROP TABLE IF EXISTS `py_server`;
CREATE TABLE `py_server`  (
  `serverUUID` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '服务器ID',
  `accessKeyId` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'AccessKey ID',
  `secretAccessKey` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'AccessKey Secret',
  `endPiont` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'Endpoint',
  `endPiontInternal` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'EndPiont Internal',
  `bucketName` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'BucketName',
  PRIMARY KEY (`serverUUID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for py_user
-- ----------------------------
DROP TABLE IF EXISTS `py_user`;
CREATE TABLE `py_user`  (
  `userUUID` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户UUID',
  `userName` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `regSex` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '性别',
  `regAge` int(3) NOT NULL COMMENT '年龄',
  `regEmail` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '电子邮箱',
  `regTime` datetime(0) NOT NULL COMMENT '注册时间',
  `loginTime` datetime(0) NULL DEFAULT NULL COMMENT '登录时间',
  `admin` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '是否为管理员',
  PRIMARY KEY (`userUUID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;
