package ru.bridgemedia.mediainfo;

import java.util.Properties;

import java.util.*;
import java.io.*;

public class Config {

    public String startPath = ".\\";
    public String ffProbePath = "ffmpeg\\ffprobe.exe";
    public Integer maxDepth = 1024;

    public void load() {
        Properties p = new Properties();

        try {
//            p.load(new FileInputStream("config.ini" ) );
//            p.load(new InputStreamReader(new FileInputStream("config.ini"), "UTF8"));
            p.load(new InputStreamReader(new FileInputStream("config.ini"), "windows-1251"));

        } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Ошибка загрузки файла конфигурации config.ini");

        }

        try {
            this.ffProbePath = p.getProperty("ffProbePath");
            this.startPath = p.getProperty("startPath");
            this.maxDepth = Integer.parseInt( p.getProperty("maxDepth") );

            System.out.println( "Config.ffProbePath: " + this.ffProbePath );
            System.out.println( "Config.startPath: " + this.startPath );
            System.out.println( "Config.maxDepth: " + this.maxDepth );

        } catch ( Exception e) {
            e.printStackTrace();
            System.out.println("Ошибка загрузки файла конфигурации config.ini");
            System.out.println("Нужны параметры путей: startPath, ffProbePath");

        }

    }


}
