package com.bewannabe.rfpdedup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    private static void sampleUpload() {
        try {
            byte[] rawData = Files.readAllBytes(Paths.get("./inputfile"));
            Core.upload("./inputfile", rawData, StorageManager.Cloud.LOCAL);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void sampleDownload() {
        try {
            byte[] rawData = Core.download("./inputfile", StorageManager.Cloud.LOCAL);
            Files.write(Paths.get("./download"), rawData);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void sampleDelete() {
        try {
            Core.delete("./inputfile", StorageManager.Cloud.LOCAL);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        sampleUpload();
        sampleDelete();
    }
}