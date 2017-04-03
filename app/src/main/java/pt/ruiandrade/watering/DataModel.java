package pt.ruiandrade.watering;

public class DataModel {

    String name;
    String id;
    int humidity;
    int temperature;
    int luminosity;


    public DataModel(String name, String id) {
        this.name=name;
        this.id = id;
        this.humidity = 0;
        this.temperature = 0;
        this.luminosity = 0;
    }


    public String getName() {
        return name;
    }


    public String getId() { return id; }

    public int getHumidity() { return humidity; }

    public int getTemperature() { return humidity; }

    public int getLuminosity() { return luminosity; }

}
