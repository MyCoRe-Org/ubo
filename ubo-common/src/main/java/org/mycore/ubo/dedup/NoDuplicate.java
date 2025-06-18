package org.mycore.ubo.dedup;

import java.util.Date;


public class NoDuplicate {

    private Integer id;

    private String mcrId1;

    private String mcrId2;

    private String creator;

    private Date date;


    public NoDuplicate() {
    }

    public NoDuplicate(Integer id, String mcrId1, String mcrId2, String creator, Date date) {
        this.id = id;
        this.mcrId1 = mcrId1;
        this.mcrId2 = mcrId2;
        this.creator = creator;
        this.date = date;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMcrId1() {
        return mcrId1;
    }

    public void setMcrId1(String mcrId1) {
        this.mcrId1 = mcrId1;
    }

    public String getMcrId2() {
        return mcrId2;
    }

    public void setMcrId2(String mcrId2) {
        this.mcrId2 = mcrId2;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
