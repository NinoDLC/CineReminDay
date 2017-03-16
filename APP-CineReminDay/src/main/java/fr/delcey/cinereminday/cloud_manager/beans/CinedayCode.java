package fr.delcey.cinereminday.cloud_manager.beans;

/**
 * Created by Nino on 15/03/2017.
 */

public class CinedayCode {
    private String mCode;
    private long mTimestamp;
    private boolean mAvailable;

    public CinedayCode() {

    }

    public CinedayCode(String code, long timestamp, boolean available) {
        this.mTimestamp = timestamp;
        this.mCode = code;
        this.mAvailable = available;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public boolean isAvailable() {
        return mAvailable;
    }

    public void setAvailable(boolean available) {
        mAvailable = available;
    }
}
