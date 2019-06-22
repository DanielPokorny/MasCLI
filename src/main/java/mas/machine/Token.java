package mas.machine;

/**
 * @version 0.0.0
 *          Created by daniel on 18.4.17.
 */
public class Token {
    /**
     * Určuje typ tokenu.
     */
    public enum Type {
        /**
         * Reálné číslo, obdoba Double
         */
        NUMBER,

        /**
         * Řetězec, obdoba String.
         */
        STRING,

        /**
         * Pravdivostní proměnná, obdoba boolean.
         */
        BOOLEAN
    }

    /**
     * Typ uložené proměnné.
     */
    private final Type type_;

    /**
     * Hodnota uložené proměnné.
     */
    private Object value_;

    /**
     * Vytvoří nový token.
     * @param type  typ uložené proměnné
     * @param value hodnota uložené proměnné
     */
    public Token(Type type, Object value) {
        type_ = type;
        value_ = value;
    }

    /**
     * Testuje, zda je je uložená proměnná typu boolean.
     * @return true, pokud je proměnná typu boolean
     */
    public boolean isBoolean() {
        return type_ == Type.BOOLEAN;
    }

    /**
     * Testuje, zda je je uložená proměnná typu number.
     * @return true, pokud je proměnná typu number
     */
    public boolean isNumber() {
        return type_ == Type.NUMBER;
    }

    /**
     * Testuje, zda je je uložená proměnná typu string.
     * @return true, pokud je proměnná typu string
     */
    public boolean isString() {
        return type_ == Type.STRING;
    }

    /**
     * Vrací hodnotu uložené proměnné konvertovanou do Stringu.
     * @return řetězec, reprezentující hodnotu proměnné
     */
    public String getString() {
        return value_.toString();
    }

    /**
     * Vrací hodnotu uložené proměnné konvertovanou do Double.
     * @return double, reprezentující hodnotu proměnné
     */
    public Double getDouble() {
        if(type_ == Type.NUMBER) {
            return (Double) value_;
        } else {
            return null;
        }
    }

    /**
     * Vrací hodnotu uložené proměnné konvertovanou do boolean.
     * @return boolean, reprezentující hodnotu proměnné
     */
    public Boolean getBoolean() {
        if(type_ == Type.BOOLEAN) {
            return (Boolean) value_;
        } else {
            return null;
        }
    }

    /**
     * Nastaví hodnotu uložené proměnné.
     * @param value hodnota proměnné
     */
    public void setValue(Object value) {
        value_ = value;
    }
}
