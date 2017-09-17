package mobi.acpm.inspeckage.hooks.entities;

public class FingerprintItem {
    public boolean enable;
    public String name;
    public String newValue;
    public String type;
    public String value;

    public FingerprintItem(String type, String name, String value, String newValue, boolean enable) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.newValue = newValue;
        this.enable = enable;
    }
}
