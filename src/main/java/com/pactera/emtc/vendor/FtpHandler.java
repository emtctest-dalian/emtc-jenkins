package com.pactera.emtc.vendor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.util.Properties;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FtpHandler
{
    private FTPClient ftpClient;

    public FtpHandler(Enviroment env){
        this.setFtpClient(env);
    }

    private void setFtpClient(Enviroment env){
        ftpClient = new FTPClient();
        try{
            ftpClient.connect(env.getIp());
            ftpClient.login(env.getUsername(), env.getPassword());
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//            ftpClient.setControlEncoding("UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("resource")
    public Boolean ftpUpload(FileHandler file,String path){
        Boolean success = false;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            int reply = 0;
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                in.close();
                ftpClient.disconnect();
                System.err.println("FTP server refused connection.");
                return success;
            }
            //mkdirs(in case of multiple level directory)
            if(path != null && !path.isEmpty()){
                this.mkdirs(path);
                ftpClient.changeWorkingDirectory(path);
            }
            ftpClient.storeFile(file.getMd5Name(), in);
            ftpClient.sendSiteCommand("chmod 777 " + file.getMd5Name());
            in.close();
            ftpClient.logout();
            success = true;
        }catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close(); // 关闭流
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    public Boolean mkdirs(String path){
        Boolean success = false;
        String[] subDirs = path.split("/");

        //check if is absolute path
        if(path.substring(0, 0).equalsIgnoreCase("/")){
            subDirs[0] = "/" + subDirs[0];
        }
        boolean tmpMkdirs = false;
        try {
            for(String subDir : subDirs){
                //encoding
                tmpMkdirs = ftpClient.makeDirectory(subDir);
                boolean tmpDoCommand = ftpClient.sendSiteCommand("chmod 777 " + subDir);
                System.out.println("chmod:"+tmpDoCommand);
                ftpClient.changeWorkingDirectory(subDir);
                success = success || tmpMkdirs;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    public enum Enviroment{
        PRODUCE("139.217.28.230","jenkins","12345"),
        TEST("58.215.221.218","emtc","12345");

        private String ip;
        private String username;
        private String password;

        Enviroment(String ip,String username,String password){
            this.ip = ip;
            this.username = username;
            this.password=password;
        }

        public String getIp() {
            return ip;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}


