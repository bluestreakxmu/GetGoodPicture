package org.bluestreakxmu.picture.utils;

/**
 * HttpClient测试类
 */
public class SimpleHttpClientTest {

    public static void main(String[] args) {
        String referer = "http://jandan.net/ooxx";

        String[] urlList = new String[2];
        urlList[0] = "http://jandan.net/ooxx";
        urlList[1] = "http://jandan.net/ooxx/page-218";
        SimpleHttpClient httpclient = new SimpleHttpClient(referer);
        for (String url : urlList) {
            System.out.println(httpclient.get(url));
        }

    }
}
