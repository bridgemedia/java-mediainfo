package ru.bridgemedia.mediainfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {

//    static String ffProbePath_string = "D:\\BABAK\\JAVA\\mediainfo-new\\ffmpeg\\ffprobe.exe";
//    static String startPath_string = "D:\\BABAK\\JAVA\\mediainfo-new\\test";

    // Это настройки для запуска программы
    static String ffProbePath_string = "\\ffmpeg\\ffprobe.exe";
    static String startPath_string = ".";
    private static Integer maxDepth_int = 32;
    private static Integer fileCounter = 0;

    // А это всякие буфферы
    static List<String> pathesToProcess = new ArrayList<>();
    static List<MediaFile> MediaFiles = null;

    private static JSONObject jsonObj;
    private static JSONArray streams;
    private static JSONObject stream;

    public static void main(String[] args) {

        Config Config = new Config();
        Config.load();
        ffProbePath_string = Config.ffProbePath;
        startPath_string = Config.startPath;
        maxDepth_int = Config.maxDepth;

        if ( args.length > 0 ) {
            startPath_string = args[0];
            System.out.println("Cmd.startPath_string: " + startPath_string );

        }

        System.out.println( "-----");

        //ОК, получили первый путь, проверяем, не файл ли это
        try {
//            byte[] utfString = startPath_string.getBytes("UTF-8") ;
//            startPath_string = new String(utfString,"UTF-8") ;

            startPath_string = java.net.URLDecoder.decode( startPath_string, "UTF-8");
            File StartPath = new File(startPath_string);

            if ( StartPath.isFile() ) {
                pathesToProcess.add( StartPath.toString() );

            } else if (StartPath.isDirectory()) {

                try ( Stream<Path> paths = Files.walk( Paths.get( startPath_string ), maxDepth_int )) {

                    paths
                            .filter( (p)->!p.toFile().isDirectory() && ( p.toFile().getAbsolutePath().endsWith("mpg") || p.toFile().getAbsolutePath().endsWith("mp4") || p.toFile().getAbsolutePath().endsWith("mpeg") || p.toFile().getAbsolutePath().endsWith("avi") || p.toFile().getAbsolutePath().endsWith("webm") ) )
                            .forEach( item->pathesToProcess.add( item.toString() ) );

                }


            } else {
                throw new Exception();

            }

        } catch ( Exception e) {
            e.printStackTrace();
            System.out.println("Ошибка чтения файла " + startPath_string );

        }

        MediaFiles = getMassFilesParams( pathesToProcess );

        try {
            processResults( MediaFiles );

        } catch (UnsupportedEncodingException e) {
            System.out.println("Некоторые ссылки не были сформированы" );
            e.printStackTrace();
        }


        System.out.println( "-----");
        System.out.println("ОК" );
        System.out.println("Обработано файлов: " + fileCounter.toString() );
        System.out.println("Результат записан в файл report.html" );
        System.out.println("Для завершения нажмите ENTER" );

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

    }

    /**
     * Обработка результатов
     *
     * @param mediaFiles
     */
    private static void processResults(List<MediaFile> mediaFiles) throws UnsupportedEncodingException {

        String htmlBuffer = "<html>";
        htmlBuffer += "<body style=\"background-color:black\">";
        htmlBuffer += "<head>";
            htmlBuffer += "<meta charset=\"utf-8\">";
        htmlBuffer += "</head>";
        htmlBuffer += "<table border=1 cellspacing=0 cellpadding=2 style=\"background-color:white\">";
            htmlBuffer += "<tr>";
            htmlBuffer += "<td>Файл</td>";
            htmlBuffer += "<td>Длительность (S.ms)</td>";
            htmlBuffer += "<td>Длительность (HH:mm:ss)</td>";
            htmlBuffer += "<td>Битрейт</td>";
            htmlBuffer += "<td>Размер файла (bytes)</td>";
            htmlBuffer += "<td colspan=\"2\">Размеры картинки</td>";
            htmlBuffer += "<td>Кодек</td>";
            htmlBuffer += "</tr>";

        String milisecondsFormatted = "";
//        long millis = 0;
//        long seconds = 0;
//        Duration d;

        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        Double millis = Double.valueOf(0);
        MediaFile element;

        for ( ListIterator<MediaFile> iter = mediaFiles.listIterator(); iter.hasNext(); ) {
            fileCounter++;

            element = iter.next();

            millis = Double.valueOf( element.getDuration() ) * 1000;

            hours = millis.intValue() / (60 * 60 * 1000);
            minutes = millis.intValue() / (60 * 1000) % 60;
            seconds = millis.intValue() / 1000 % 60;

            milisecondsFormatted = String.format( "%02d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
            );

            htmlBuffer += "\r\n";
            htmlBuffer += "<tr>";
//            htmlBuffer += "<td><a href='"+ URLEncoder.encode( element.getPath(), "UTF-8") +"'>"+ element.getPath() +"</a></td>";
            htmlBuffer += "<td><a href='"+ element.getPath() +"' target=\"_b\">"+ element.getPath() +"</a></td>";
            htmlBuffer += "<td>"+ element.getDuration() +"</td>";
            htmlBuffer += "<td>"+ milisecondsFormatted +"</td>";
            htmlBuffer += "<td>"+ element.getBit_rate() +"</td>";
            htmlBuffer += "<td>"+ Long.toString( element.getSize() ) +"</td>";
            htmlBuffer += "<td>"+ element.getWidth() +"</td><td>"+ element.getHeight() +"</td>";
            htmlBuffer += "<td>"+ element.getCodec_name() +"</td>";
            htmlBuffer += "</tr>";


        }

        htmlBuffer += "</table>";
        htmlBuffer += "</body>";


        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream("report.html"), StandardCharsets.UTF_8);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
//            Files.write( Paths.get("report.html"), htmlBuffer.getBytes());


                writer.write( htmlBuffer );
                writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка записи отчёта в файл report.html" );

        }

    }

    /**
     * получение параметров одного файла
     *
     * @param path
     * @return
     */
    public static MediaFile getFileParams(String path) {

        MediaFile resultMediaFile = new MediaFile();
        resultMediaFile.setPath( path );
        String jsonMediaInfo = null;

        try {
            //выполняем команду ffprobe и получаем результат в JSON-объект
            jsonMediaInfo = execCmd( ffProbePath_string + " -v quiet -print_format json -show_data -show_private_data -show_streams -show_format \"" + path + "\"");
            jsonObj = new JSONObject( jsonMediaInfo );

            streams = jsonObj.getJSONArray("streams");
            stream = streams.getJSONObject(0 );

//            System.out.println( jsonMediaInfo );

            //можно было бы восстановить строку в объект, но мы
            //сами раскладываем нужные нам сейчас параметры
            double duration = jsonObj.getJSONObject("format").getDouble( "duration" );
            resultMediaFile.setDuration( duration );

            double bit_rate = jsonObj.getJSONObject("format").getDouble( "bit_rate" );
            resultMediaFile.setBit_rate( bit_rate );

            long size = jsonObj.getJSONObject("format").getLong( "size" );
            resultMediaFile.setSize( size );

            int width = stream.getInt( "width" );
            resultMediaFile.setWidth( width );

            int height = stream.getInt( "height" );
            resultMediaFile.setHeight( height );

            String codec_name = stream.getString( "codec_name" );
            resultMediaFile.setCodec_name( codec_name );

            System.out.println( path + ", " + duration + ", " + width + ":"+height + ", " + codec_name );

        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("Ошибка парсинга Json-результатов от ffprobe" );
            System.out.println( jsonMediaInfo );

        }

        return resultMediaFile;

    }

    /**
     * получение параметров по списку путей к файлам
     *
     * @param pathesToProcess
     * @return
     */
    public static List<MediaFile> getMassFilesParams(List<String> pathesToProcess) {
        List<MediaFile> resultset = new ArrayList<>();

        //итерация по списку медиа-файлов (уже как объектов, а не путей)
        for (ListIterator<String> iter = pathesToProcess.listIterator(); iter.hasNext(); ) {
            String element = iter.next();
            resultset.add( getFileParams( element.toString() ) );
        }

        return resultset;

    }


    /**
     * получение параметров по списку путей к файлам
     *
     * @param cmd
     * @return
     * @throws java.io.IOException
     */
    public static String execCmd(String cmd) {
        java.util.Scanner s = null;
        try {
            s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка выполнения команды '" + cmd + "'" );
        }
        return s.hasNext() ? s.next() : "";
    }


}
