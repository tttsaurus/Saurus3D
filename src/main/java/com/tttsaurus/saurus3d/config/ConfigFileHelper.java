package com.tttsaurus.saurus3d.config;

import java.io.File;
import java.io.RandomAccessFile;

public final class ConfigFileHelper
{
    public static File makeFile(String name)
    {
        File dir = new File("config/saurus3d");
        if (!dir.exists()) dir.mkdirs();

        try
        {
            RandomAccessFile raf = new RandomAccessFile("config/saurus3d/" + name + ".cfg", "rw");
            raf.close();
        }
        catch (Exception ignored) { }

        return new File("config/saurus3d/" + name + ".cfg");
    }
}
