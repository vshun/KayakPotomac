package vadim.potomac.model;

public class DailyAir {

    private String date = "";
    private long hi = 0;
    private long low = 0;

    public String getDate () { return date; }

    public void setDate(String date)
        { this.date = date;  }

    public long getHi() {
        return hi;
    }

    public void setHi(long hi) {
        this.hi = hi;
    }

    public long getLow() {
        return low;
    }

    public void setLow(long low) {
        this.low = low;
    }
}
