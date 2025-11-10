package src.main.java.ecojuego.logic;

public enum Category {
    ORGANICO("Organico"),
    RECICLABLE("Reciclable"),
    PELIGROSO("Peligroso"),
    DESCONOCIDO("Desconocido");

    private final String display;

    Category(String display) {
        this.display = display;
    }

    public String display() {
        return display;
    }
}
