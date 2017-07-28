package org.biac.dfs;

import org.biac.dfs.bean.*;
import org.biac.dfs.exception.MyException;
import org.biac.dfs.tool.ByteArrayTool;
import org.biac.dfs.tool.JsonTool;
import org.biac.dfs.tool.PacketTool;
import org.zeromq.ZMQ;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Properties;

/**
 * 客户端进行存取操作的客户端类
 * @author Zhang
 * @version 0.1
 */
public class Client {

    private static String URL = null;           //服务器URL
    private static int sendTimeout = 0;                         //传输数据链接超时时间,从配置文件中读取
    private static int recvTimeout = 0;                         //接收数据链接超时时间,从配置文件中读取

    private ZMQ.Context context = null;
    private ZMQ.Socket socket = null;

    /**
     * 读取配置文件
     */
    static{
        try {
            Properties properties = new Properties();
            //String solrConfigPath = "./client.properties";
            //InputStream in = new FileInputStream(solrConfigPath);      //从当前目录加载配置文件用这个
            InputStream in = Client.class.getResourceAsStream("/client.properties");    //打jar包时候使用
            properties.load(in);

            URL = properties.getProperty("server");

            if (URL == null){
                throw new MyException("URL不存在");
            }
            sendTimeout = Integer.parseInt(properties.getProperty("send_timeout","3"));
            recvTimeout = Integer.parseInt(properties.getProperty("recv_timeout","3"));
            in.close();
        } catch (IOException e) {
            System.out.println("配置文件加载失败");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * 用于客户端向服务器上传文件
     * @param userName String 上传人
     * @param itemId String 公司ID或产品ID
     * @param fileOrder int 文件序:当前上传文件在相应文件类型中的序号
     * @param ext String 文件扩展名
     * @param fileContent data[] 文件数据流
     * @return UploadDownJson 上传下行Json对象
     *
     */
    public UploadDownJson upload(String userName, String itemId, int fileOrder, String ext, byte[] fileContent) throws MyException{
        int copCode = 1;
        UploadUpJson json = new UploadUpJson();
        json.setUsername(userName);
        json.setBodyLength(fileContent.length);
        json.setExtension(ext);
        json.setFileOrder(fileOrder);
        json.setItemId(itemId);
        String jsonString = JsonTool.toJson(json);
        byte[] send = PacketTool.pack(copCode,jsonString,fileContent);
        boolean success = socket.send(send);
        if (!success){
            throw new MyException("上传失败");
        }
        byte[] recv = socket.recv();
        if (recv == null){
            throw new MyException("接收数据超时");
        }
        try {
            int jsonSize = ByteArrayTool.bytesToShort(recv, 4);
            String resultJson = new String(recv, 6, jsonSize, "utf-8");
            return JsonTool.toObject(resultJson,UploadDownJson.class);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new MyException("接收数据编码错误");
        }
    }

    /**
     *
     * @param userName String 删除人
     * @param itemId String 相关项目ID
     * @param fileOrder int 文件序:当前上传文件在相应文件类型中的序号
     * @return DeleteDownJson 删除操作下行Json对象
     * @throws MyException
     */
    public DeleteDownJson delete(String userName, String itemId, int fileOrder) throws MyException{

        int copCode = 3;
        DeleteUpJson json = new DeleteUpJson();
        json.setUsername(userName);
        json.setFileOrder(fileOrder);
        json.setItemId(itemId);
        String jsonString = JsonTool.toJson(json);
        byte[] send = PacketTool.pack(copCode,jsonString,null);
        boolean success = socket.send(send);
        if (!success){
            throw new MyException("上传失败");
        }
        byte[] recv = socket.recv();
        if (recv == null){
            throw new MyException("接收数据超时");
        }
        try {
            int jsonSize = ByteArrayTool.bytesToShort(recv, 4);
            String resultJson = new String(recv, 6, jsonSize, "utf-8");
            return JsonTool.toObject(resultJson,DeleteDownJson.class);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new MyException("接收数据编码错误");
        }

    }

    /**
     * 用于客户端从服务器下载文件信息
     * @param userName String 上传人
     * @param itemId String 公司ID或产品ID
     * @return AbstractJson[] AbstractJson[1]存储包头中的json对象,AbstractJson[2]中储存包体中的json对象,它们都继承于AbstractJson
     *         如果第一个Json对象中状态码不为0,后一个Json对象为null
     */
    public AbstractJson[] download(String userName, String itemId) throws MyException{
        int copCode = 2;
        AbstractJson[] result = new AbstractJson[2];
        DownloadUpJson json = new DownloadUpJson();
        json.setUsername(userName);
        json.setItemId(itemId);
        String jsonString = JsonTool.toJson(json);

        byte[] send = PacketTool.pack(copCode,jsonString,null);
        boolean success = socket.send(send);
        if (!success){

        }
        byte[] recv = socket.recv();
        if (recv == null){
            throw new MyException("接收数据超时");
        }
        try {
            int jsonSize = ByteArrayTool.bytesToShort(recv,4);
            String downloadDownJsonString = new String(recv,6,jsonSize,"utf-8");
            DownloadDownJson downloadDownJson= JsonTool.toObject(downloadDownJsonString,DownloadDownJson.class);
            result[0] = downloadDownJson;
            if (downloadDownJson.getStatusCode() != 0){
                result[1] = null;
            } else {
                int bodyLength = downloadDownJson.getBodyLength();
                result[1] = JsonTool.toObject(new String(recv,6+jsonSize,bodyLength,"utf-8"),RespContentJson.class);
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new MyException("接收数据编码错误");
        }
    }

    /**
     *
     * @param userName String 文件获取人
     * @param itemId 具体文件类型ID
     * @param fileOrder 文件序
     * @return byte[] 文件内容的直接流
     * @throws MyException
     */
    public byte[] fileAccess(String userName, String itemId, int fileOrder) throws MyException{

        int copCode = 4;
        byte[] result = null;
        FileaccessUpJson json = new FileaccessUpJson();
        json.setUsername(userName);
        json.setItemId(itemId);
        json.setFileOrder(fileOrder);
        String jsonString = JsonTool.toJson(json);
        byte[] send = PacketTool.pack(copCode,jsonString,null);
        boolean success = socket.send(send);
        if (!success){
            throw new MyException("获取失败");
        }
        byte[] recv = socket.recv();
        if (recv == null){
            throw new MyException("接收数据超时");
        }
        try {
            int jsonSize = ByteArrayTool.bytesToShort(recv,4);
            String fileaccessDownJsonString = new String(recv,6,jsonSize,"utf-8");
            FileaccessDownJson downloadDownJson= JsonTool.toObject(fileaccessDownJsonString,FileaccessDownJson.class);

            if (downloadDownJson.getStatusCode() != 0){
                return null;
            } else {
                int bodyLength = downloadDownJson.getBodyLength();
                result = Arrays.copyOfRange(recv,6+jsonSize,bodyLength+6+jsonSize);
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new MyException("接收数据编码错误");
        }
    }

    /**
     * 用于客户端和服务器建立连接
     */
    public void connect() {
        context = ZMQ.context(1);
        socket = context.socket(ZMQ.REQ);
        socket.setSendTimeOut(sendTimeout*1000);
        socket.setReceiveTimeOut(recvTimeout*1000);
        socket.connect("tcp://"+URL);
    }

    /**
     * 用于关闭客户端与服务器的链接
     */
    public void close(){
        context.term();
        socket.close();
        context = null;
        socket = null;
    }




}