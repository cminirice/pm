package com.guttv.pm.platform.action.script.execute;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author donghongchen
 * @create 2018-01-17 14:39
 **/
public class RmtShellExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RmtShellExecutor.class);
    private static String DEFAULTCHART = "UTF-8";
    private Connection conn;
    private Session session ;
    private String ip;
    private int port;
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private static RmtShellExecutor instance;

    public synchronized static RmtShellExecutor getInstance(String ip, int port, String username, String passward) {
        if (instance == null) {
            instance = new RmtShellExecutor(ip, port, username, passward);
        }
        return instance;
    }

    public RmtShellExecutor(String ip, int port, String username, String passward) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = passward;
    }

    /**
     * 获取远程机上的文件到本地
     * @param remoteFile
     * @param localTargetDirectory
     */
    public void getFile(String remoteFile, String localTargetDirectory) {
        Connection connection = new Connection(ip, port);
        try {
            connection.connect();
            boolean isAuthenticated = connection.authenticateWithPassword(username, password);
            if (!isAuthenticated) {
                logger.error("authentication failed");
            }
            SCPClient client = new SCPClient(connection);
            client.get(remoteFile, localTargetDirectory);
            connection.close();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }


    /**
     * 上传文件
     * @param localFile
     * @param remoteTargetDirectory
     */
    public void putFile(String localFile, String remoteTargetDirectory) {
        Connection connection = new Connection(ip, port);
        try {
            connection.connect();
            boolean isAuthenticated = connection.authenticateWithPassword(username, password);
            if (!isAuthenticated) {
                logger.error("authentication failed");
            }
            SCPClient client = new SCPClient(connection);
            client.put(localFile, remoteTargetDirectory);
            connection.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }


    /**
     *
     * @param localFile 本地路径
     * @param fileName  文件名称
     * @param remoteTargetDirectory 远程文件夹
     */
    public void uploadFile(String localFile,String fileName, String remoteTargetDirectory) throws IOException {
        try {
            if (login()) {
                SCPClient client = new SCPClient(conn);
                createDir(remoteTargetDirectory);
                if(!localFile.endsWith("/")){
                    localFile=localFile+"/";
                }
                client.put(localFile+fileName, remoteTargetDirectory);
            }
        } finally {
            close();
        }
    }

    /**
     * 远程登录linux的主机
     *
     * @return 登录成功返回true，否则返回false
     * @author Ickes
     * @since V0.1
     */
    public boolean login() {
        boolean flg;
        try {
            conn = new Connection(ip,port);
            conn.connect();//连接
            flg = conn.authenticateWithPassword(username, password);//认证
        } catch (IOException e) {
            flg = false;
            e.printStackTrace();
        }
        return flg;
    }

    public String createDir(String remoteTargetDirectory) throws IOException {
        return execute(" mkdir "+remoteTargetDirectory);
    }

    /**
     * @param cmd 即将执行的命令
     * @return 命令执行完后返回的结果值
     * @author Ickes
     * 远程执行shll脚本或者命令
     * @since V0.1
     */
    public String execute(String cmd) throws IOException {
        String result="";
        try {
            if (login()) {
                session = conn.openSession();
                session.execCommand(cmd);
                result = processStdout(session.getStdout(), DEFAULTCHART);
            }
        } finally {
            close();
        }
        return result;
    }

    /**
     * 解析脚本执行返回的结果集
     *
     * @param in      输入流对象
     * @param charset 编码
     * @return 以纯文本的格式返回
     * @author Ickes
     * @since V0.1
     */
    private String processStdout(InputStream in, String charset) throws IOException {
        StringBuffer buffer = new StringBuffer();
        try(InputStream stdout = new StreamGobbler(in);
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout, charset))){
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line + "\n");
                if (line.contains("JVM running for")){
                    break;
                }
            }
        }
        return buffer.toString();
    }


    public void close(){
        try {
            session.close();
        }catch (Exception e){

        }
        try {
            conn.close();
        }catch (Exception e){

        }
    }



}
