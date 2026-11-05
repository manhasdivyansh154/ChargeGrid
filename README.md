# ChargeGrid - EV Charging Station Network Management System

Console-based, menu-driven Java project with a MySQL backend.
Pure Java + JDBC: no frameworks. The only external file is the MySQL JDBC driver jar.

## Features
- **3 roles:** Admin, Station Manager, Customer (role-based menus, SHA-256 password hashing)
- **Admin:** stations, chargers, users, station managers, technicians, tickets, network reports
- **Manager:** own station only: chargers, bookings, sessions, tickets, reports
- **Customer:** register, vehicles, find stations, book slots (conflict-checked), start/stop charging,
  wallet top-up, invoices and payments, report faulty chargers
- **Business rules:** peak-hour pricing (18:00-22:00, polymorphic per charger type), 5% GST,
  auto-pay from wallet, unpaid invoices otherwise, booking overlap detection
- **JDBC transactions** (commit/rollback) for start session, stop session + invoice + payment,
  pay invoice, resolve ticket

## 1. Set up the database
```
mysql -u root -p < sql/schema.sql
```
This creates the `chargegrid` database, all tables and sample data.

## 2. Add the MySQL driver
Download **Connector/J** (Platform Independent) from https://dev.mysql.com/downloads/connector/j/
and copy `mysql-connector-j-8.x.x.jar` into the `lib/` folder.

## 3. Configure the password
Edit `db.properties` and set `db.password` to your MySQL password.

## 4. Compile and run
Linux / macOS:
```
./compile.sh
./run.sh
```
Windows:
```
compile.bat
run.bat
```
Manual: `javac -d out $(find src -name "*.java")` then
`java -cp "out:lib/*" chargegrid.Main` (use `;` instead of `:` on Windows).

Requires Java 8 or newer.

## Demo logins
| Role     | Username    | Password     |
|----------|-------------|--------------|
| Admin    | admin       | admin123     |
| Manager  | manager1    | manager123   |
| Customer | customer1   | customer123  |

customer1 has Rs.500 in the wallet and one registered vehicle (HR26AB1234).

## Charging simulation
A console app cannot stream real charging. When you stop a session you enter the minutes you charged;
energy = min(charger kW, vehicle max kW) x hours x 0.92 efficiency (capped at 90% of battery).

## Project structure (82 types)
```
src/chargegrid/
  Main.java
  model/      Users (Admin, StationManager, Customer), Charger hierarchy (AC / DC Fast / Ultra Fast),
              Station, Vehicle, Booking, ChargingSession, Invoice, Payment, Wallet,
              MaintenanceTicket, Technician, Billable, 8 enums
  dao/        10 interfaces + 10 implementations (all SQL lives here)
  service/    9 services + AppContext (business rules, validation, transactions)
  ui/         13 menu classes (BaseMenu, MainMenu, AdminMenu, ManagerMenu, CustomerMenu, ...)
  util/       DBConnection, InputHelper, Validator, PasswordUtil, DateTimeUtil, ConsoleUtil
  exception/  ChargeGridException + 5 specific exceptions
sql/schema.sql   tables, foreign keys, sample data
```

## Common problems
| Message | Fix |
|---|---|
| MySQL JDBC driver not found | Put the connector jar in `lib/` |
| Access denied for user 'root' | Wrong password in `db.properties` |
| Unknown database 'chargegrid' | Run `sql/schema.sql` |
| Communications link failure | Start the MySQL server |
