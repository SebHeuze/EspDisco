package espdisco.hodor.ninja.espdisco.model;

public class Esp8266 {

    private String ipAdress;
    private boolean selected;

    public Esp8266(String ipAdress,boolean selected) {
        this.ipAdress = ipAdress;
        this.selected = selected;
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


}