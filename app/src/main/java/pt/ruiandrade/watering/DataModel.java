package pt.ruiandrade.watering;

public class DataModel {

    String name;
    String id;
    int humidity;
    int temperature;
    int luminosity;
    boolean autoHumi;
    boolean autoTemp;
    boolean autoLumi;

    public DataModel(String name, String id) {
        this.name=name;
        this.id = id;
        this.humidity = 0;
        this.temperature = 0;
        this.luminosity = 0;
        this.autoHumi = true;
        this.autoTemp = true;
        this.autoLumi = true;
    }


    public String getName() {
        return name;
    }


    public String getId() { return id; }

    public int getHumidity() { return humidity; }

    public int getTemperature() { return temperature; }

    public int getLuminosity() { return luminosity; }

}
