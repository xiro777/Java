package org.example;

public class InputFile {
    private String filename;
    private int packetsNoPP;
    private int packetsNoSM;
    private int insertsNo;

    public String getFilename() {
        return filename;
    }

    public int getPacketsNoPP() {
        return packetsNoPP;
    }

    public int getPacketsNoSM() {
        return packetsNoSM;
    }

    public int getInsertsNo() {
        return insertsNo;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setPacketsNoPP(int packetsNoPP) {
        this.packetsNoPP = packetsNoPP;
    }

    public void setPacketsNoSM(int packetsNoSM) {
        this.packetsNoSM = packetsNoSM;
    }

    public void setInsertsNo(int insertsNo) {
        this.insertsNo = insertsNo;
    }

    @Override
    public String toString() {
        return "InputFile{" +
                "filename='" + filename + '\'' +
                ", packetsNoPP=" + packetsNoPP +
                ", packetsNoSM=" + packetsNoSM +
                ", insertsNo=" + insertsNo +
                '}';
    }
}
