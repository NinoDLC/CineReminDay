package fr.delcey.cinereminday.cloud_manager.beans;

/**
 * Created by Nino on 15/03/2017.
 */

public class CinedayCode {
    private String mCode;
    private boolean mAvailable;

    public CinedayCode() { // Needed for Firebase deserialization

    }

    public CinedayCode(String code) {
        this.mCode = code;
        this.mAvailable = true;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }

    public boolean isAvailable() {
        return mAvailable;
    }

    public void setAvailable(boolean available) {
        mAvailable = available;
    }

    @Override
    public String toString() {
        return "CinedayCode{" +
                "code='" + mCode + '\'' +
                ", available=" + mAvailable +
                '}';
    }
}
