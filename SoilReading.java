package com.agrogeorgia.model;

public class SoilReading {
    private double moisture, ph, nitrogen, phosphorus, potassium;
    private double temperature, humidity, rainfall;
    private String soilType, suitablePlant, growthDuration, soilHealth, source;

    public SoilReading(double moisture, double ph, double nitrogen, double phosphorus,
                       double potassium, double temperature, double humidity,
                       double rainfall, String soilType, String suitablePlant,
                       String growthDuration, String soilHealth, String source) {
        this.moisture = moisture; this.ph = ph; this.nitrogen = nitrogen;
        this.phosphorus = phosphorus; this.potassium = potassium;
        this.temperature = temperature; this.humidity = humidity;
        this.rainfall = rainfall; this.soilType = soilType;
        this.suitablePlant = suitablePlant; this.growthDuration = growthDuration;
        this.soilHealth = soilHealth; this.source = source;
    }
    public double getMoisture(){return moisture;}
    public double getPh(){return ph;}
    public double getNitrogen(){return nitrogen;}
    public double getPhosphorus(){return phosphorus;}
    public double getPotassium(){return potassium;}
    public double getTemperature(){return temperature;}
    public double getHumidity(){return humidity;}
    public double getRainfall(){return rainfall;}
    public String getSoilType(){return soilType;}
    public String getSuitablePlant(){return suitablePlant;}
    public String getGrowthDuration(){return growthDuration;}
    public String getSoilHealth(){return soilHealth;}
    public String getSource(){return source;}
}
