package com.guttv.pm.platform.action.script.execute;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author donghongchen
 * @create 2018-02-28 10:22
 **/
public class HttpInnerClient {
    protected Log log = LogFactory.getLog(HttpInnerClient.class.getName());

    int ConnectTimeout = 10000;
    int ReadTimeout = 10000;

    public Map<String,String> downloadHttpFile(String url, String localurl) {
        Map<String,String> result = new HashMap<String,String>(8);
        try (FileOutputStream out = new FileOutputStream(localurl)) {
            URL httpUrl = new URL(url);
            URLConnection uRLConnection = httpUrl.openConnection();
            uRLConnection.setConnectTimeout(ConnectTimeout);
            uRLConnection.setReadTimeout(ReadTimeout);
            uRLConnection.setDoOutput(true);
            uRLConnection.connect();
            long contentLength = uRLConnection.getContentLength();
            byte[] buffer = new byte[10 * 1024];
            int read;
            //重定向输入
            InputStream in = uRLConnection.getInputStream();
            //读取输出
            while ((read = in.read(buffer)) > 0) {
                //写入本地文件
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            File file = new File(localurl);
            if (contentLength == file.length()) {
                result.put("code","200");
                result.put("message", url + "   ok!");
            } else {
                result.put("code","404");
                result.put("message", url + " 下载失败，原因文件大小不一致!");
                log.error(url + "下载失败，原因文件大小不一致，源文件大小为：" + contentLength + ",下载后的大小为：" + file.length());
            }
        } catch (Exception e) {
            log.error(url + "下载;" + e.getMessage(), e);
            result.put("code","404");
            result.put("message", url + " 文件不存在!");
        }
        return result;
    }

    public static void main(String[] args) {
        HttpInnerClient downloadManager = new HttpInnerClient();
        String urlStr = "https://github.com/ctripcorp/apollo/archive/master.zip";
        // 从url中获得下载的文件格式与名字
        String fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.lastIndexOf("?") > 0 ? urlStr.lastIndexOf("?") : urlStr.length());
        String dir = "E:/test/haha/";
        Map<String,String> result = downloadManager.downloadHttpFile(urlStr, dir+fileName);
        System.out.println(result.get("code"));
        System.out.println(result.get("message"));
    }

}
