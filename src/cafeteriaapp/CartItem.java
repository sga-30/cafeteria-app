package cafeteriaapp;

import javafx.beans.property.*;

public class CartItem {

    private final int menuItemId;
    private final String name;
    private final double unitPrice;
    private final IntegerProperty quantity;

    public CartItem(int menuItemId, String name, double unitPrice, int quantity) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = new SimpleIntegerProperty(quantity);
    }

    public int getMenuItemId() { return menuItemId; }
    public String getName() { return name; }
    public double getUnitPrice() { return unitPrice; }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int q) { quantity.set(q); }
    public IntegerProperty quantityProperty() { return quantity; }

    public double getSubtotal() { return unitPrice * quantity.get(); }
}
