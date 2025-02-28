package org.mycore.ubo.dedup;

public class PossibleDuplicate {

    public PossibleDuplicate() {
    }

    public PossibleDuplicate(String mcrId1, String mcrId2, String deduplicationType, String deduplicationKey) {
        this.mcrId1 = mcrId1;
        this.mcrId2 = mcrId2;
        this.deduplicationType = deduplicationType;
        this.deduplicationKey = deduplicationKey;
    }

    private String mcrId1;

    private String mcrId2;

    private String deduplicationType;

    private String deduplicationKey;

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

    public String getDeduplicationType() {
        return deduplicationType;
    }

    public void setDeduplicationType(String deduplicationType) {
        this.deduplicationType = deduplicationType;
    }

    public String getDeduplicationKey() {
        return deduplicationKey;
    }

    public void setDeduplicationKey(String deduplicationKey) {
        this.deduplicationKey = deduplicationKey;
    }

    @Override
    public String toString() {
        return "PossibleDuplicate{" +
                "mcrId1='" + mcrId1 + '\'' +
                ", mcrId2='" + mcrId2 + '\'' +
                ", deduplicationType='" + deduplicationType + '\'' +
                ", deduplicationKey='" + deduplicationKey + '\'' +
                '}';
    }
}
