# PAU Cafeteria App — CA2 CSC310

## Setup Instructions

### 1. Create the PostgreSQL Database

Open pgAdmin or a terminal and run:

```sql
CREATE DATABASE paucafe;
```

Then run the setup script:

```bash
psql -U postgres -d paucafe -f setup_database.sql
```

This creates all 4 tables and seeds 28 menu items plus the manager account.

---

### 2. Manager Login Credentials

| Field    | Value                  |
|----------|------------------------|
| Email    | manager@paucafe.com    |
| Password | Manager@123            |

---

### 3. Open in NetBeans

1. Open NetBeans → File → Open Project → select the `CafeteriaApp` folder
2. Right-click project → Properties → Libraries → confirm PostgreSQL JDBC and jBCrypt JARs are present
3. Run the project (F6)

---

## Features Implemented

### Customer Features
- **Register**: Sign up with first name, last name, email, hashed password (BCrypt)
- **Login**: Secure email + password login with loading overlay
- **Home**: Welcome message + account balance from DB
- **Menu**: All 28 items loaded from PostgreSQL, grouped by category (Drinks / Main Meal / Swallow / Protein / Snacks), with search filter
- **Cart**: Add items, adjust quantity (+/−), remove items, running total
- **Checkout**: Places order → deducts balance → reduces stock → shows receipt
- **Order History**: Full list of past orders with itemised breakdown
- **Wallet**: Info screen linking to Profile top-up
- **Profile**: View name/email/balance, top up wallet, logout

### Manager Features
- **Separate login**: manager@paucafe.com routes to a dedicated dashboard
- **View all items**: Sortable table with ID, name, category, qty, price, description
- **Add item**: Fill form → click Add → table refreshes instantly
- **Edit item**: Click row (auto-fills form) → modify → click Update
- **Delete item**: Click row → Delete → confirmation dialog

---

## Database Schema

```
users          (id, first_name, last_name, email, password, role, account_balance)
menu_items     (id, name, category, quantity, price, description)
orders         (id, user_id, total, created_at)
order_items    (id, order_id, menu_item_id, quantity, unit_price)
```

---

## File Structure (new/modified files)

```
src/cafeteriaapp/
├── User.java                          ← updated (id, role, double balance)
├── CartItem.java                      ← NEW
├── CartManager.java                   ← NEW (singleton cart)
├── resources/
│   ├── controllers/
│   │   ├── LoginController.java       ← updated (role routing, balance from DB)
│   │   ├── MenuController.java        ← updated (DB load, Add to Cart, search)
│   │   ├── CartController.java        ← NEW
│   │   ├── OrderHistoryController.java← NEW
│   │   ├── ProfileController.java     ← NEW (top-up, logout)
│   │   ├── ManagerDashboardController ← NEW (CRUD)
│   │   ├── DashboardController.java   ← updated (cart + orders wired)
│   │   └── HomeTabController.java     ← updated (₦ formatting)
│   └── views/
│       ├── dashboard.fxml             ← updated (cart icon added)
│       ├── menu.fxml                  ← updated (searchField wired)
│       ├── cart-view.fxml             ← NEW
│       ├── order-history.fxml         ← NEW
│       ├── profile-view.fxml          ← NEW
│       ├── manager-dashboard.fxml     ← NEW
│       └── wallet-view.fxml           ← updated

setup_database.sql                     ← Run this first!
```
