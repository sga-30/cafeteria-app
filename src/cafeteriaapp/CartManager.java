package cafeteriaapp;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CartManager {

    private static final CartManager instance = new CartManager();
    private final ObservableList<CartItem> items = FXCollections.observableArrayList();

    private CartManager() {}

    public static CartManager getInstance() { return instance; }

    public ObservableList<CartItem> getItems() { return items; }

    public void addItem(int menuItemId, String name, double unitPrice) {
        for (CartItem ci : items) {
            if (ci.getMenuItemId() == menuItemId) {
                ci.setQuantity(ci.getQuantity() + 1);
                return;
            }
        }
        items.add(new CartItem(menuItemId, name, unitPrice, 1));
    }

    public void removeItem(CartItem item) {
        items.remove(item);
    }

    public double getTotal() {
        return items.stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    public void clear() {
        items.clear();
    }

    public int getTotalItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
}
