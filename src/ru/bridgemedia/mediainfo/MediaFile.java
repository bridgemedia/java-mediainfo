package ru.bridgemedia.mediainfo;

public class MediaFile {
    private String path;
    private double duration;
    private double bit_rate;
    private long size;

    private int height;
    private int width;
    private String codec_name;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getCodec_name() {
        return codec_name;
    }

    public void setCodec_name(String codec_name) {
        this.codec_name = codec_name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getBit_rate() {
        return bit_rate;
    }

    public void setBit_rate(double bit_rate) {
        this.bit_rate = bit_rate;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
