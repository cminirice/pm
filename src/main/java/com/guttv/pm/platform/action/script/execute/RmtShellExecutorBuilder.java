package com.guttv.pm.platform.action.script.execute;

import com.guttv.pm.core.bean.ScriptBean;
import com.guttv.pm.core.bean.ServerBean;
import com.guttv.pm.core.bean.ServerScriptMapping;
import com.guttv.pm.core.fp.ScriptToZookeeper;
import com.guttv.pm.core.fp.ServerScriptMappingToZookeeper;
import com.guttv.pm.core.fp.ServerToZookeeper;
import com.guttv.pm.utils.FtpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author donghongchen
 * @create 2018-02-07 15:22
 **/
@Component
public class RmtShellExecutorBuilder {
    private static final Logger logger = LoggerFactory.getLogger(RmtShellExecutorBuilder.class);

    String dir = "/temp/pm/file/";

    public static void main(String[] args) throws IOException {
        String ip = "47.93.85.185";
        int port = 50916;
        String username = "donghongchen";
        String pwd = "donghongchen123";
        RmtShellExecutor scp = RmtShellExecutor.getInstance(ip, port, username, pwd);
        String localFile = "D://test//";
//        String fileName = "demo.zip";
        String fileName = "demo.tar.gz";
        String remoteTargetDirectory = "/home/donghongchen/test/";
        //上传文件
        scp.uploadFile(localFile, fileName, remoteTargetDirectory);
        //执行命令
        //        String cmd="unzip  demo.zip";
        String decompressionCMD = "tar -zxvf  demo.tar.gz";
        String shCMD = "sh " + remoteTargetDirectory + "demo/start.sh ";

        scp.execute("cd " + remoteTargetDirectory + " ; " + decompressionCMD + "   ");

        scp.execute(shCMD);
    }


    public Map<String,String> execute(ServerBean serverBean, ScriptBean scriptBean) {
        Map<String,String> map = new HashMap<String,String>(8);
        RmtShellExecutor scp = RmtShellExecutor.getInstance(serverBean.getIp(), serverBean.getPort(), serverBean.getUserName(), serverBean.getPassword());
        try {
            if(scriptBean.getFilePath().contains("http://")||scriptBean.getFilePath().contains("https://")){
                //下载到本地
//                String fileName = scriptBean.getFilePath().substring(scriptBean.getFilePath().lastIndexOf("/") + 1, scriptBean.getFilePath().lastIndexOf("?") > 0 ? scriptBean.getFilePath().lastIndexOf("?") : scriptBean.getFilePath().length());
                HttpInnerClient downloadManager = new HttpInnerClient();
                Map<String,String> result = downloadManager.downloadHttpFile(scriptBean.getFilePath()+scriptBean.getFileName(), dir+scriptBean.getFileName());
                if(("200").equals(result.get("code"))){
                    //本地文件，直接上传文件
                    scp.uploadFile(dir, scriptBean.getFileName(), scriptBean.getRemoteTarget());
                }else {
                    map.put("code","-1");
                    map.put("message","file downLoad is fail . ");
                    return map;
                }
            }else if(scriptBean.getFilePath().contains("ftp://")){
                boolean flag = FtpUtil.downloadFileFormFtp(dir+scriptBean.getFileName(),scriptBean.getFilePath()+scriptBean.getFileName());
                if (flag){
                    //本地文件，直接上传文件
                    scp.uploadFile(dir, scriptBean.getFileName(), scriptBean.getRemoteTarget());
                }else {
                    map.put("code","-1");
                    map.put("message","file downLoad is fail . ");
                    return map;
                }
            }else {
                //本地文件，直接上传文件
                scp.uploadFile(scriptBean.getFilePath(), scriptBean.getFileName(), scriptBean.getRemoteTarget());
            }
            scp.execute("cd " + scriptBean.getRemoteTarget() + " ; " + scriptBean.getDecompressionCMD() + "   ");
            scp.execute(scriptBean.getShCMD());
            map.put("code","0");
            map.put("message","success");
        } catch (IOException e) {
            map.put("code","-1");
            map.put("message","RmtShellExecutorBuilder execute method is fail : "+e.getMessage());
            logger.error("uploadFile method is fail : "+e.getMessage(),e);
        }
        return map;
    }

    @Async("myAsync")
    public void executeAsync(String code){
        ScriptBean scriptBean;
        ServerBean serverBean;
        try {
            scriptBean = ScriptToZookeeper.getFromZookeeper(code);
            ServerScriptMapping mapping = ServerScriptMappingToZookeeper.getFromZookeeper(code);
            if (mapping==null){
                serverBean=null;
                scriptBean.setStatus(-1);
            }else {
                serverBean= ServerToZookeeper.getFromZookeeper(mapping.getServerCode());
            }
            if ( serverBean == null){
                scriptBean.setStatus(-1);
            }else {
                logger.info("脚本开始执行");
                Map<String,String> map =execute(serverBean,scriptBean);
                logger.info("脚本执行完毕");
                if ("0".equals(map.get("code"))){
                    scriptBean.setStatus(2);
                    scriptBean.setDesc(map.get("message")+"");
                }else {
                    scriptBean.setStatus(3);
                    scriptBean.setDesc(map.get("message")+"");
                }
            }
            scriptBean.setUpdateTime(new Date());
            ScriptToZookeeper.persistanceToZookeeper(scriptBean);
        } catch (Exception e) {
            logger.error("executeAsync method is fail : "+e.getMessage(),e);
        }
    }

    @Async("myAsync")
    public void shutdown(String code){
        ScriptBean scriptBean;
        ServerBean serverBean;
        try {
            scriptBean = ScriptToZookeeper.getFromZookeeper(code);
            ServerScriptMapping mapping = ServerScriptMappingToZookeeper.getFromZookeeper(code);
            if (mapping==null){
                serverBean=null;
                scriptBean.setStatus(-1);
            }else {
                serverBean= ServerToZookeeper.getFromZookeeper(mapping.getServerCode());
            }
            if ( serverBean == null){
                scriptBean.setStatus(-1);
            }else {
                logger.info("脚本开始停止");
                Map<String,String> map =shutdown(serverBean,scriptBean);
                logger.info("脚本停止完毕");
                if ("0".equals(map.get("code"))){
                    scriptBean.setStatus(4);
                    scriptBean.setDesc(map.get("message")+"");
                }else {
                    scriptBean.setStatus(2);
                    scriptBean.setDesc(map.get("message")+"");
                }
            }
            scriptBean.setUpdateTime(new Date());
            ScriptToZookeeper.persistanceToZookeeper(scriptBean);
        } catch (Exception e) {
            logger.error("shutdown method is fail : "+e.getMessage(),e);
        }
    }

    public Map<String,String> shutdown(ServerBean serverBean, ScriptBean scriptBean) {
        Map<String,String> map = new HashMap<String,String>(8);
        RmtShellExecutor scp = RmtShellExecutor.getInstance(serverBean.getIp(), serverBean.getPort(), serverBean.getUserName(), serverBean.getPassword());
        try {
            scp.execute(scriptBean.getShutdown());
            map.put("code","0");
            map.put("message","success");
        } catch (IOException e) {
            map.put("code","-1");
            map.put("message","RmtShellExecutorBuilder execute method is fail : "+e.getMessage());
            logger.error("uploadFile method is fail : "+e.getMessage(),e);
        }
        return map;
    }

}
