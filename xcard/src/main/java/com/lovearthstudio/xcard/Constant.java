package com.lovearthstudio.xcard;

/**
 * 常量类
 * 定义项目基本的常量地址
 * Created by pro on 16/2/23.
 */
public class Constant {
    /*用户*/
    public static final String userUrl = "http://api.xdua.org/users";
    /* 项目基本地址 */
    public static final String baseUrl = "http://api.wikicivi.com/articles";
    /* 项目图片基本地址 */
    public static final String baseFileUrl = "http://files.wikicivi.com/files/";
    /*用户头像基本地址*/
    public static final String baseAvatarUrl = "http://files.xdua.org/files/avatar/";
    /*用户头像默认地址*/
    public static final String defaultAvatarUrl = "http://files.xdua.org/files/avatar/anonymous.png";
    /* 屏幕宽高 */
    public static int screenWidth;
    public static int screenHight;

    /* 主页面左右边距,在mainActivity里计算好 */
    public static int cardMargin;
    public static int cardPadding;
    /**页卡的宽度*/
    public static int cardWidth;
    /**页卡里放置内容的宽度**/
    public static int cardItemWidth;

    /**图片最大高宽*/
    public static int maxImageHight = 4096;
    public static int maxImageWidth = 4096;
}
