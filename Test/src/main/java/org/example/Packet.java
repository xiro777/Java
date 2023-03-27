package org.example;

public class Packet {
    String id;
    int startPageNo;
    int pagesNo;
    int pageNoRMUA;
    String adresat;
    String address;
    String postCode;
    String city;
    String filename;
    boolean isSpeedmail = false;
    String UPOC = "";
    String sortInfo ="";
    String matrixValue= "";
    boolean isUlotka = false;
    boolean isLocalAddress = false;
    int sheetsNo;
    boolean isError;

    public Packet() {
    }

    public Packet(int id, int startPageNo, String adresat, String address, String postCode, String city, String filename, boolean isUlotka) {
        this.id = String.valueOf(id);
        this.startPageNo = startPageNo;
        this.adresat = adresat;
        this.address = address;
        this.postCode = postCode;
        this.city = city;
        this.filename = filename;
        this.isUlotka = isUlotka;
    }



    public String getId() {
        return id;
    }

    public int getStartPageNo() {
        return startPageNo;
    }

    public int getPagesNo() {
        return pagesNo;
    }

    public int getPageNoRMUA() {
        return pageNoRMUA;
    }

    public String getAdresat() {
        return adresat;
    }

    public String getAddress() {
        return address;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getCity() {
        return city;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isSpeedmail() {
        return isSpeedmail;
    }

    public String getUPOC() {
        return UPOC;
    }

    public String getSortInfo() {
        return sortInfo;
    }

    public String getMatrixValue() {
        return matrixValue;
    }

    public boolean isUlotka() {
        return isUlotka;
    }

    public boolean isLocalAddress() {
        return isLocalAddress;
    }

    public int getSheetsNo() {
        return sheetsNo;
    }

    public boolean isError() {
        return isError;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStartPageNo(int startPageNo) {
        this.startPageNo = startPageNo;
    }

    public void setPagesNo(int pagesNo) {
        this.pagesNo = pagesNo;
    }

    public void setPageNoRMUA(int pageNoRMUA) {
        this.pageNoRMUA = pageNoRMUA;
    }

    public void setAdresat(String adresat) {
        this.adresat = adresat;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setSpeedmail(boolean speedmail) {
        isSpeedmail = speedmail;
    }

    public void setUPOC(String UPOC) {
        this.UPOC = UPOC;
    }

    public void setSortInfo(String sortInfo) {
        this.sortInfo = sortInfo;
    }

    public void setMatrixValue(String matrixValue) {
        this.matrixValue = matrixValue;
    }

    public void setUlotka(boolean ulotka) {
        isUlotka = ulotka;
    }

    public void setLocalAddress(boolean localAddress) {
        isLocalAddress = localAddress;
    }

    public void setSheetsNo(int sheetsNo) {
        this.sheetsNo = sheetsNo;
    }

    public void setError(boolean error) {
        isError = error;
    }
}
