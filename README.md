# SmartShip Package Management System

SmartShip Ltd. is a courier company that ships packages for customers both locally and internationally. To modernize its operations, SmartShip has developed this comprehensive system enabling customers and staff to efficiently manage all aspects of package shipping.

## Table of Contents

- [Description](#description)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)
- [Authors & Acknowledgments](#authors--acknowledgments)

---

## Description

This SmartShip Package Management System streamlines the courier process for customers, staff, and managers by offering:

- Customer shipment requests and tracking
- Automated invoice generation
- Vehicle and delivery schedule management
- Managerial reporting on shipments, revenue, and delivery performance

## Features

- **Customer Portal**: Create shipment requests, track packages in real-time, receive invoices.
- **Staff Dashboard**: Manage vehicles, assign and schedule deliveries, avoid vehicle overbooking.
- **Manager Portal**: Access reports on shipments, monitor revenue, and review delivery performance.
- **Secure Authentication**: Role-based access for customers, staff, and managers.
- **Comprehensive Reporting**: Generate and export shipment and revenue reports.

## Technology Stack

- **Java Swing**: User interface design
- **Log4j**: Logging and application event tracking
- **MySQL**: Database backend
- **JFreeChart (or JCharts)**: Graphs and statistical reports
- **iText**: PDF invoice and report generation

## Installation

### Prerequisites

- **Java JDK 8 or later**  
  [Download from Oracle](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)  
- **MySQL Server**  
  [MySQL Download](https://dev.mysql.com/downloads/mysql/)
- **Dependencies:**  
  Download the following JAR files and save them in a folder called `lib` at the root of the project:  
  - [Log4j](https://logging.apache.org/log4j/2.x/download.html)
  - [JFreeChart](http://www.jfree.org/jfreechart/) (or [JCharts](http://jcharts.sourceforge.net/))
  - [iText](https://itextpdf.com/)
  - [MySQL Connector/J (JDBC driver)](https://dev.mysql.com/downloads/connector/j/)

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/Apollo1729/AP_Group_Project-SmartShip-Package-Management-.git
   ```

2. **Create the `lib` directory for dependencies**
   ```bash
   mkdir lib
   # Place all required JARs into the ./lib directory
   ```

3. **Configure the Database**
   - Create a MySQL database for the application.
   - Update database connection details in a configuration file such as `config.properties` located in the project.  
     Example entries in `config.properties`:
     ```
     db.url=jdbc:mysql://localhost:3306/smartship
     db.username=your_mysql_user
     db.password=your_mysql_password
     ```
   - Run the provided SQL scripts (schema and migrations) in the `/sql` or `/docs` folder to initialize the database.

4. **Compile the source code**
   - Create a `bin` directory for compiled classes:
     ```bash
     mkdir bin
     ```
   - Compile the Java source files, setting the classpath to include all JARs in `lib`:
     - On **Windows**:
       ```bat
       javac -cp "lib/*" -d bin src\**\*.java
       ```
     - On **Linux/macOS**:
       ```bash
       javac -cp "lib/*" -d bin src/**/*.java
       ```
     > If you encounter issues with the `**` wildcard, you may need to compile file-by-file or use a build script.

5. **Run the application**
   - Identify the main class file (e.g., `Main` or `SmartShipApp`). Run:
     - On **Windows**:
       ```bat
       java -cp "bin;lib/*" MainClass
       ```
     - On **Linux/macOS**:
       ```bash
       java -cp "bin:lib/*" MainClass
       ```
     > Replace `MainClass` with your project's actual entry point class name.

---

## Usage

- **Customers:** Register, log in, create shipment requests, and track packages.
- **Staff:** Log in to manage vehicles, schedule and assign deliveries.
- **Managers:** Log in to generate reports on shipments, revenue, and delivery performance.

Default roles and credentials may be set for demo purposes (check documentation or ask team lead).

---

## Contributing

Contributions are welcome! Please fork the repository and submit pull requests.
- Follow code style guidelines.
- Ensure all new features are tested.
- Open issues for bugs or feature requests.

---

## License

This project is licensed under the [MIT License](LICENSE).

---

## Authors & Acknowledgments

- [Apollo1729](https://github.com/Apollo1729), https://github.com/Ajlearnscode ,https://github.com/NotN8 AND AP team

Special thanks to contributors and the open-source community.

---

*For screenshots, demo videos, or additional documentation, please see the `/docs` folder or open an issue.*
