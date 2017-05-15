package com.pactera.emtc.vendor;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;

public class FileHandler extends File{

    private String md5Name = "";

    public FileHandler(String pathname){
        super(pathname);
        this.setMd5Name(this.getName());
    }

    public String getMd5Name(){
        return this.md5Name;
    }

    public void setMd5Name(String filename){
        String prefix = filename.substring(filename.lastIndexOf(".")+1);
        this.md5Name = DigestUtils.md5Hex(filename+System.currentTimeMillis())+"."+prefix;
    }

    public boolean equals(Object obj){
        return super.equals(obj);
    }
    public int hashCode(){
        return super.hashCode();
    }
}
