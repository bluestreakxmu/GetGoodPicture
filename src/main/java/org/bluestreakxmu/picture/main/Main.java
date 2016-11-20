package org.bluestreakxmu.picture.main;

import org.bluestreakxmu.picture.thread.ThreadPoolMananger;

import java.io.File;

/**
 * 主调用类
 *
 * @author lixibo
 */
public class Main {
    private static final String filePath = "D:" + File.separator + "jiandan_picture"; // 图片保存的本地地址
    private static final String url = "http://jandan.net/ooxx";
    private static final String referer = "http://jandan.net/ooxx"; // 链接过来的页面的URL

    public static void main(String[] args) {
        new ThreadPoolMananger(filePath, url, referer);
    }
}
