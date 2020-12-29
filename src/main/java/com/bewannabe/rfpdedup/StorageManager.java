package com.bewannabe.rfpdedup;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;

public class StorageManager {
    static enum Cloud { LOCAL, AZURE }

    private Cloud cloud;
    public StorageManager() { this.cloud = Cloud.LOCAL; }
    public StorageManager(Cloud cloud) { this.cloud = cloud; }

    public String readIndexFile() {
        if(cloud==Cloud.LOCAL) {
            // TODO: the below is just temporary code which needs to be fixed
            try {
                byte[] data = Files.readAllBytes(Paths.get("./storage/storage.index"));
                return new String(data);
            } catch(IOException e) {
                return "";
            }
        } else {
            // TODO: implement reading from azure
            return "";
        }
    }
    
    public void writeIndexFile(String data) {
        if(cloud==Cloud.LOCAL) {
            // TODO: the below is just temporary code which needs to be fixed
            try {
                new File("./storage/storage.index").createNewFile();
                Files.write(Paths.get("./storage/storage.index"), data.getBytes());
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: implement saving to azure
        }
    }

    public String readRecipeFile(String hash) {
        if(cloud==Cloud.LOCAL) {
            // TODO: the below is just temporary code which needs to be fixed
            try {
                String filename = String.format("./storage/recipe/%s", hash);
                byte[] data = Files.readAllBytes(Paths.get(filename));
                return new String(data);
            } catch(IOException e) {
                return "";
            }
        } else {
            // TODO: implement reading from azure
            return "";
        }
    }

    public void writeRecipeFile(String hash, String data) {
        if(cloud==Cloud.LOCAL) {
            // TODO: the below is just temporary code which needs to be fixed
            try {
                String filename = String.format("./storage/recipe/%s", hash);
                new File(filename).createNewFile();
                Files.write(Paths.get(filename), data.getBytes());
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: implement saving to azure
        }
    }

    public void deleteRecipeFile(String hash) {
        if(cloud==Cloud.LOCAL) {
            // TODO: the below is just temporary code which needs to be fixed
            String filename = String.format("./storage/recipe/%s", hash);
            new File(filename).delete();
        } else {
            // TODO: implement deleting from azure
        }
    }

    public byte[] readChunkData(String hash) {
        if(cloud==Cloud.LOCAL) {
            // TODO: the below is just temporary code which needs to be fixed
            try {
                String filename = String.format("./storage/chunk/%s", hash);
                return Files.readAllBytes(Paths.get(filename));
            } catch(IOException e) {
                return new byte[0];
            }
        } else {
            // TODO: implement reading from azure
            return new byte[0];
        }
    }

    public void writeChunkData(String hash, byte[] data) {
        if(cloud==Cloud.LOCAL) {
            // TODO: the below is just temporary code which needs to be fixed
            try {
                String filename = String.format("./storage/chunk/%s", hash);
                new File(filename).createNewFile();
                Files.write(Paths.get(filename), data);
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: implement saving to azure
        }
    }

    public void deleteChunkData(String hash) {
        if(cloud==Cloud.LOCAL) {
            // TODO: the below is just temporary code which needs to be fixed
            String filename = String.format("./storage/chunk/%s", hash);
            new File(filename).delete();
        } else {
            // TODO: implement deleting from azure
        }
    }
}