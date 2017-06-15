package edu.cmu.inmind.multiuser.common.model;

/**
 * Created by sakoju on 6/7/17.
 */
public class R5StreamListener {
    private String red5StreamingUrl;
    private String streamingStatus;

    public String getRed5StreamingUrl() {
        return this.red5StreamingUrl;
    }

    public void setRed5StreamingUrl(String red5StreamingUrl) {
        this.red5StreamingUrl = red5StreamingUrl;
    }

    public String getStreamingStatus() {
        return this.streamingStatus;
    }

    public void setStreamingStatus(String streamingStatus) {
        this.streamingStatus = streamingStatus;
    }

    public R5StreamListener() {
    }

    public String toString() {
        return "Component: " + this.getClass().toString() + " red5 stream url: " + this.red5StreamingUrl + " red5 stream status: " + this.streamingStatus;
    }
}
