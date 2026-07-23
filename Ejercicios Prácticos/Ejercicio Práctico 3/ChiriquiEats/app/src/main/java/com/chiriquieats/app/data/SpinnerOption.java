package com.chiriquieats.app.data;

public class SpinnerOption {
    private final String value;
    private final String label;

    //Metodo para listar las opciones en los spinner
    public SpinnerOption(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public boolean isPlaceholder() {
        return value == null || value.isEmpty();
    }

    @Override
    public String toString() {
        return label;
    }
}
