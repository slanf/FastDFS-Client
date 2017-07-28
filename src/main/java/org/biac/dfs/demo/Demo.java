package org.biac.dfs.demo;

import org.biac.dfs.Client;
import org.biac.dfs.exception.MyException;

import java.io.*;
import java.util.Arrays;

public class Demo {

    //最基本的上传下载
    public static void main(String[] args) {
        Client client = new Client();
        client.connect();

        try {

            //AbstractJson[] result1= client.download("JYMF","358000221_json");
            //RespContentJson respContentJson = (RespContentJson)result1[1];
            //TreeMap<Integer, String> urls = respContentJson.getUrls();
            //System.out.println(urls);
            /****String base = "."+File.separator;
            File baseFileDic = new File(base);
            for (File file : baseFileDic.listFiles()) {

                String name = file.getName();
                if(name.charAt(0)>'9'||name.charAt(0)<'0'){
                    continue;
                }
                int dot = name.lastIndexOf(".");
                int underLine = name.lastIndexOf("_");
                String itemId = name.substring(0,underLine);
                int order = Integer.parseInt(name.substring(underLine + 1, dot));
                String ext = name.substring(dot + 1);
                System.out.println(itemId+" "+order+"  "+ext);
                client.upload("JYMF", itemId, order, ext, getBytes(file));
            }****/
            //byte[] data = client.fileAccess("JYMF","358000251_json",1);
            //System.out.println(new String(data,"UTF-8"));
            client.upload("lala","test_json_3",1,"txt","hellow world 3".getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }

    //文件导入
    /*public static void main(String[] args) {

        int code = 0;
        try {
            code = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("code不对");
            return;
        }
        switch (code){
            case 1:
                upProJson();
                return;
            case 2:
                upComJson();
                return;
            case 3:
                upComAndProImg();
                return;
            case 4:
                upOrder();
                return;
            case 5:
                upNoImgLogo();
                return;
            case 6:
                upAdvertisement();
                return;
            case 7:
                upListJson();
                return;
            case 8:
                upDocument();
                return;
            case 9:
                upXKZ();
                return;
        }
    }*/

    //上传产品json
    public static void upProJson() {
        Client client = new Client();
        client.connect();
        String base = "/home/jymf/jymfweb/json";
        String[] companyIds = new File(base).list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return  !name.startsWith("c");
            }
        });
        try {
            for (String companyId : companyIds) {
                File file = new File(base + "/" + companyId);
                for (File file1 : file.listFiles()) {
                    String fileName = file1.getName();
                    String proId = fileName.substring(0,fileName.lastIndexOf("."));
                    client.upload("JYMF",companyId+"_"+proId+"_json",1,"json",getBytes(file1));
                    System.out.println(companyId+"_"+proId+"_json"+"_1");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        client.close();
    }

    //上传公司json
    public static void upComJson() {
        Client client = new Client();
        client.connect();
        String base = "/home/jymf/jymfweb/json/company";

        try {
            for (File file : new File(base).listFiles()) {
                String fileName = file.getName();
                String companyId = fileName.substring(0,fileName.lastIndexOf("."));
                client.upload("JYMF",companyId+"_json",1,"json",getBytes(file));
                System.out.println(companyId+"_json_1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.close();
    }

    //上传公司和产品img
    public static void upComAndProImg() {
        Client client = new Client();
        client.connect();
        String base = "/home/jymf/jymfweb/img";
        String[] companyIds = new File(base).list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return  !(name.startsWith("a")||name.startsWith("n"));
            }
        });

        for (String companyId : companyIds) {
            String companyBase = base+"/"+companyId+"/company";
            String productBase = base+"/"+companyId+"/product";

            dealMinDirectory(companyBase,client,companyId+"_img");
            dealProduct(companyId,productBase,client);
        }
        client.close();

    }

    private static void dealMinDirectory(String URL,Client client,String itemId){
        File directory = new File(URL);
        if (!directory.exists()) {
            return;
        }

        String[] fileNames = directory.list();
        Arrays.sort(fileNames);
        int index = 0;
        for (String fileName : fileNames) {
            String ext = fileName.substring(fileName.lastIndexOf(".")+1);
            try {
                client.upload("JYMF",itemId,++index,ext,getBytes(new File(URL+"/"+fileName)));
                System.out.println(itemId+"_"+(index)+"_"+ext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void dealProduct(String companyId,String productBase,Client client) {
        File fileBase = new File(productBase);
        if (!fileBase.exists()){
            return;
        }
        String[] pro1Id = fileBase.list();
        for (String s : pro1Id) {
            String oneProInfoBase = productBase+"/"+s+"/info";
            dealMinDirectory(oneProInfoBase,client,companyId+"_"+s+"_info");
            String oneProSummBase = productBase+"/"+s+"/summ";
            dealMinDirectory(oneProSummBase,client,companyId+"_"+s+"_summ");
        }
    }

    //上传order信息,只有公司358000120有
    public static void upOrder(){
        String base = "/home/jymf/jymfweb/img/358000120/order";
        String companyId = "358000120";
        Client client = new Client();
        client.connect();
        File fileBase = new File(base);
        if (!fileBase.exists()){
            return;
        }
        String[] order1 = fileBase.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.startsWith("x");
            }
        });

        for (String s : order1) {
            dealOneOrder(client,base,s,companyId);
        }
        client.close();
    }

    private static void dealOneOrder(Client client,String base,String orderNum,String companyId){
        String oneOrderBase = base+"/"+orderNum;
        File oneOrderFile = new File(oneOrderBase);
        String[] rgdNames = oneOrderFile.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("rgd");
            }
        });
        Arrays.sort(rgdNames);
        String[] jyzNames = oneOrderFile.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("jyz");
            }
        });
        Arrays.sort(jyzNames);
        try {
            for (int i = 0; i < rgdNames.length; i++) {
                client.upload("JYMF",companyId+"_order_"+orderNum+"_rgd",i+1,"jpg",getBytes(new File(oneOrderBase+"/"+rgdNames[i])));
                System.out.println(companyId+"_order_"+orderNum+"_rgd_"+(i+1));
            }
            for (int i = 0; i < jyzNames.length; i++) {
                client.upload("JYMF",companyId+"_order_"+orderNum+"_jyz",i+1,"jpg",getBytes(new File(oneOrderBase+"/"+jyzNames[i])));
                System.out.println(companyId+"_order_"+orderNum+"_jyz_"+(i+1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //上传默认img
    public static void upNoImgLogo(){
        String path = "/home/jymf/jymfweb/img/noImgLogo.png";
        Client client = new Client();
        client.connect();
        try {
            client.upload("JYMF","noImgLogo",1,"png",getBytes(new File(path)));
            System.out.println("noImgLogo_1");
        } catch (MyException e) {
            e.printStackTrace();
        }
        client.close();
    }

    //上传轮播图
    public static void upAdvertisement(){
        Client client = new Client();
        client.connect();
        String path = "/home/jymf/jymfweb/img/advertisement";
        try {
            for (int i = 1; i < 6; i++) {
                client.upload("JYMF","advertisement_group1",i,"jpg",getBytes(new File(path+"/0"+i+"_00_01.jpg")));
                System.out.println("advertisement_group1_"+i);
            }
            for (int i = 1; i < 5; i++) {
                client.upload("JYMF","advertisement_group2",i,"jpg",getBytes(new File(path+"/1"+i+"_00_01.jpg")));
                System.out.println("advertisement_group2_"+i);
            }
        } catch (MyException e) {
            e.printStackTrace();
        }
        client.close();
    }

    //上传轮播图配套json,json需要手动编辑,然后放在/home/zya/list.json待传
    public static void upListJson(){

        Client client = new Client();
        client.connect();
        String listBase = "/home/zya/list.json";
        try {
            client.upload("JYMF","advertisement_json",1,"json",getBytes(new File(listBase)));
        } catch (MyException e) {
            e.printStackTrace();
        }
        client.close();

    }

    //上传Document图片
    public static void upDocument(){
        Client client = new Client();
        client.connect();
        String base = "/home/jymf/jymfweb/img";
        File[] files = new File(base).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !(name.startsWith("a") || name.startsWith("n"));
            }
        });
        for (int i = 0; i < files.length; i++) {
            File oneCompany = files[i];
            boolean hasDocumet = false;
            String[] list = oneCompany.list();
            for (String s : list) {
                if (s.equals("document")){
                    hasDocumet = true;
                    break;
                }
            }
            if (hasDocumet){
                String documentPath = oneCompany.getAbsolutePath()+"/document";
                String companyId = oneCompany.getName();
                String[] packageIds = new File(documentPath).list();
                for (int i1 = 0; i1 < packageIds.length; i1++) {
                    String packageId = packageIds[i1];
                    String packagePath = documentPath+"/"+packageId;
                    File[] files1 = new File(packagePath).listFiles();
                    try {
                        for (int i2 = 0; i2 < files1.length; i2++) {
                            client.upload("JYMF",companyId+"_documnet_"+packageId,i2+1,"jpg",getBytes(files1[i2]));
                            System.out.println(companyId+"_documnet_"+packageId+"_"+(i2+1));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }

        }
        client.close();
    }

    //上传许可证
    public static void upXKZ(){
        Client client = new Client();
        client.connect();
        try {
            client.upload("JYMF","358000120_certificate",1,"jpg",getBytes(new File("/home/zya/xkz.jpg")));
        } catch (MyException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }


    //用于文件转byte[]
    private static byte[] getBytes(File file){
        byte[] buffer = null;
        try {
            //File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }
}