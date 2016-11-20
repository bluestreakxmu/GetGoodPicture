package org.bluestreakxmu.picture.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.bluestreakxmu.picture.utils.HtmlParser;
import org.bluestreakxmu.picture.utils.SimpleHttpClient;

/**
 * 线程池.
 *
 * @author lixibo
 */
public class ThreadPoolMananger {
    private final int DOWNLOAD_THREAD = 30;
    private final int PAGE_THREAD = 2;
    // 定义一个页面导航的队列,大概看了一下估计有200多页吧
    private final BlockingQueue<String> naviQueue = new LinkedBlockingQueue<>(3);
    // 定义一个图片网址的队列,这个估计很多
    private final BlockingQueue<String> imgQueue = new LinkedBlockingQueue<>(100);
    private final ExecutorService exec = Executors
            .newFixedThreadPool((DOWNLOAD_THREAD + PAGE_THREAD));
    // 定义一个开始的倒数锁
    private final CountDownLatch begin = new CountDownLatch(1);
    // 定义一个结束的倒数锁
    private final CountDownLatch end = new CountDownLatch(
            (DOWNLOAD_THREAD + PAGE_THREAD));

    private final String referer;

    /**
     * 默认构造方法
     *
     * @param filePath 图片本地保存路径
     */
    public ThreadPoolMananger(String filePath, String url, String referer) {
        this.referer = referer;
        int i = 1;
        for (; i <= PAGE_THREAD; i++) {
            exec.submit(new PageThread(i, begin, end));
        }
        for (; i <= (DOWNLOAD_THREAD + PAGE_THREAD); i++) {
            exec.submit(new ImageThread(i, filePath, begin, end));
        }

        HtmlParser parser = new HtmlParser();
        SimpleHttpClient client = new SimpleHttpClient(referer);
        parser.setHtml(client.get(url));
        System.out.println("====开始抓取首页");
        try {
            naviQueue.put(parser.getPageNavi());
            parser.handleImgs(imgQueue);
        } catch (InterruptedException e) {
            // FIXME do nothing
        }
        client.close();
        System.out.println("首页结束，开始执行多线程抓取");
        begin.countDown();
    }

    private class ImageThread implements Runnable {
        private final CountDownLatch startSignal; // 开始倒数锁
        private final CountDownLatch stopSignal; // 结束倒数锁
        private int threadIdx; // 线程序号
        private String destFilePath; // 目标文件位置

        /**
         * ImageThread构造函数
         *
         * @param index        线程序号
         * @param destFilePath 目标文件位置
         * @param start        开始倒数锁
         * @param end          结束倒数锁
         */
        ImageThread(int index, String destFilePath, CountDownLatch start,
                    CountDownLatch end) {
            this.threadIdx = index;
            this.destFilePath = destFilePath;
            this.startSignal = start;
            this.stopSignal = end;
        }

        @Override
        public void run() {
            try {
                // 等待初始的线程结束
                startSignal.await();
            } catch (Exception e) {
                // do nothing
            }
            System.out.println("[" + threadIdx + "]线程开始");
            SimpleHttpClient client = new SimpleHttpClient(referer);
            String picUrl = "";
            int left = 0;
            // 这个线程不断的从图片队列里面取出图片的地址
            while (true) {
                // 取出一个图片地址
                try {
                    picUrl = imgQueue.take();
                    left = imgQueue.size();
                } catch (InterruptedException e1) {
                    System.err
                            .println("[" + threadIdx + "]:" + e1.getMessage());
                }
                if ("".equals(picUrl)) {
                    try {
                        // 结束标志，丢回去，其他的线程要根据这个判断结束
                        imgQueue.put("");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // 如果说，取到图片地址为空而且页面的已经解析完毕，这个就应该要结束了。
                    break;
                }
                try {
                    System.out.println("[" + threadIdx + "][图片left:" + left
                            + "]线程开始抓取image-->" + picUrl);
                    client.downloadFile(picUrl, destFilePath);
                } catch (Exception e) {
                    System.err.println("[" + threadIdx + "]:" + e.getMessage());
                }
            }
            client.close();
            stopSignal.countDown();
        }
    }

    private class PageThread implements Runnable {
        private final CountDownLatch startSignal;
        private final CountDownLatch stopSignal;
        private int index;

        PageThread(int index, CountDownLatch start, CountDownLatch end) {
            this.startSignal = start;
            this.stopSignal = end;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                startSignal.await();
                System.out.println("[" + index + "]线程开始");
            } catch (Exception e) {
                // FIXME do nothing
            }
            System.out.println("[" + index + "]线程开始");
            String html = "";
            String url = "";
            int left = 0;
            HtmlParser parser = new HtmlParser();
            SimpleHttpClient client = new SimpleHttpClient(referer);
            while (true) {
                try {
                    url = naviQueue.take();
                    left = naviQueue.size();
                    if ("".equals(url)) {
                        // 把结束的标志放回去，其他的线程也应该要调用
                        naviQueue.put("");
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("[" + index + "]:" + e.getMessage());
                }
                System.out.println("[" + index + "][页面left:" + left
                        + "]线程抓取html-->" + url);
                try {
                    html = client.get(url);
                } catch (Exception e1) {
                    // FIXME do nothing
                }
                parser.setHtml(html);
                String next = parser.getPageNavi();
                try {
                    if (next == null) {

                        naviQueue.put("");
                        parser.handleImgs(imgQueue);
                        // 在图片队列的最后也放上一个""作为结束的标志吧
                        imgQueue.put("");

                    } else {
                        naviQueue.put(next);
                        parser.handleImgs(imgQueue);
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            client.close();
            stopSignal.countDown();
        }

    }

}
