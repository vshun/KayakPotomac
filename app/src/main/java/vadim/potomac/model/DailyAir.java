package vadim.potomac.model;

public class DailyAir {
    private String day = "";
    private long hi = 0;
    private long low = 0;
    private String wind = "";

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

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

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }
}
