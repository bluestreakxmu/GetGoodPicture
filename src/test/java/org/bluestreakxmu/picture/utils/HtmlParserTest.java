package org.bluestreakxmu.picture.utils;

/**
 * HTML解析类测试类
 *
 * @author lixibo
 */
public class HtmlParserTest {

    public static void main(String[] args) {
        HtmlParser parser = new HtmlParser();
        String referer = "http://jandan.net/ooxx"; // 链接过来的页面的URL

        SimpleHttpClient client = new SimpleHttpClient(referer);
        String[] urlList = new String[5];
        urlList[0] = "http://jandan.net/ooxx/page-216";
        urlList[1] = "http://jandan.net/ooxx/page-217";
        urlList[2] = "http://jandan.net/ooxx/page-207";
        urlList[3] = "http://jandan.net/ooxx/page-117";
        urlList[4] = "http://jandan.net/ooxx/page-7";
        String html;
        for (String url : urlList) {
            System.out.println("fecthing url===>" + url);
            html = client.get(url);
            parser.setHtml(html);
            System.out.println("Now get Next Page url:" + parser.getPageNavi());
            System.out.println("Now get Images URLs:");
            System.out.println(parser.handleImgs());
            System.out.println("========");
        }
    }
}
