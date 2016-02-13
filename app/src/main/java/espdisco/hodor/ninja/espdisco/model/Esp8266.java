package espdisco.hodor.ninja.espdisco.model;

public class Esp8266 {

    private String ipAdress;
    private String name;
    private boolean selected;

    public Esp8266(String ipAdress, String name) {
        this.ipAdress = ipAdress;
        this.name = name;
        this.selected = true;
    }

    public String getIpAdress() {
        return ipAdress;
    }

    public void setIpAdress(String ipAdress) {
        this.ipAdress = ipAdress;
    }

    public boolean isSelected(){
        return this.selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}