package kx;

public interface ProgressCallback {
    void setCompressed(boolean compressed);
    void setMsgLength(int msgLength);
    void setCurrentProgress(int total);
}
