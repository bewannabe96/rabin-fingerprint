package com.bewannabe.rfpdedup;

import java.util.List;

public class RecipeManager {
    StorageManager storageManager;

    public RecipeManager(StorageManager storageManager) { this.storageManager = storageManager; }

    /*
    **  USAGE: exportRecipe();
    **  DESCRIPTION:
    **      Imports file recipe and decodes.
    **  @throws
    **      Exception("FileNotExist");
    */
    public FileRecipe importRecipe(String hash) throws Exception {
        FileRecipe recipe = new FileRecipe();

        String data = storageManager.readRecipeFile(hash);
        if(data.length()==0) throw new Exception("FileNotExist");

        String[] pair, chunks = data.split("\\|");
        for(String chunk : chunks) {
            pair = chunk.split(":");
            if(pair[0].equals("0")) recipe.appendZeroChunk(Integer.parseInt(pair[1]));
            else recipe.appendChunk(pair[0], Integer.parseInt(pair[1]));
        }
        return recipe;
    }

    /*
    **  USAGE: exportRecipe();
    **  DESCRIPTION:
    **      Encodes file recipe and exports.
    **      The format of each chunk in a recipe is as the following:
    **          Normal Chunk - <MD5 HASH>:<SUCCESSION OF THE CHUNK> 
    **          Zeor-run Chunk - 0:<LENGTH OF ZEROS>
    **      and all chunks are joined with "|"(colon) separator.
    **      For example,
    **              0:131072|V9nN+9hxG5wisJCmwNkY9w==:2|rnIrh3VtIP6Ph8bRlWrTfw==:1|<...continues>
    **      is interpreted as
    **              131072 bytes of 0
    **              followed by 2 chunks of "V9nN+9hxG5wisJCmwNkY9w=="
    **              followed by 1 chunk of "rnIrh3VtIP6Ph8bRlWrTfw=="
    **              ...and so on
    */
    public void exportRecipe(String hash, FileRecipe recipe) {
        StringBuilder data = new StringBuilder();
        List<FileRecipe.Chunk> chunks = recipe.getChunks();
        for(FileRecipe.Chunk chunk : chunks)
            data.append(String.format("%s|", chunk.toString()));
        storageManager.writeRecipeFile(hash, data.toString());
    }

    /*
    **  USAGE: deleteRecipe();
    **  DESCRIPTION:
    **      Deletes file recipe from storage.
    */
    public void deleteRecipe(String hash) { storageManager.deleteRecipeFile(hash); }
}