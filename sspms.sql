-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Nov 27, 2025 at 10:31 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `sspms`
--

-- --------------------------------------------------------

--
-- Table structure for table `audit_logs`
--

CREATE TABLE `audit_logs` (
  `log_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `action` varchar(255) DEFAULT NULL,
  `table_name` varchar(50) DEFAULT NULL,
  `record_id` int(11) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `details` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `clerks`
--

CREATE TABLE `clerks` (
  `clerk_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `department` varchar(100) DEFAULT NULL,
  `employee_id` varchar(50) DEFAULT NULL,
  `hire_date` date DEFAULT NULL,
  `shift_time` varchar(50) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `clerks`
--

INSERT INTO `clerks` (`clerk_id`, `user_id`, `department`, `employee_id`, `hire_date`, `shift_time`, `created_at`) VALUES
(1, 2, 'Operations', NULL, NULL, NULL, '2025-11-24 02:02:43'),
(2, 24, 'Operations', NULL, NULL, NULL, '2025-11-27 19:45:46');

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `customer_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `company_name` varchar(255) DEFAULT NULL,
  `account_balance` decimal(10,2) DEFAULT 0.00,
  `membership_tier` varchar(50) DEFAULT 'Standard',
  `total_shipments` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`customer_id`, `user_id`, `company_name`, `account_balance`, `membership_tier`, `total_shipments`, `created_at`) VALUES
(1, 1, NULL, 0.00, 'Standard', 0, '2025-11-21 21:44:47'),
(2, 4, NULL, 0.00, 'Standard', 0, '2025-11-25 20:13:17'),
(3, 22, NULL, 0.00, 'Standard', 0, '2025-11-26 20:00:32'),
(4, 23, NULL, 0.00, 'Standard', 0, '2025-11-27 06:22:58');

-- --------------------------------------------------------

--
-- Table structure for table `drivers`
--

CREATE TABLE `drivers` (
  `driver_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `license_number` varchar(50) DEFAULT NULL,
  `license_expiry` date DEFAULT NULL,
  `vehicle_id` int(11) DEFAULT NULL,
  `total_deliveries` int(11) DEFAULT 0,
  `rating` decimal(3,2) DEFAULT 0.00,
  `status` enum('Active','Inactive','On Leave') DEFAULT 'Active',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `drivers`
--

INSERT INTO `drivers` (`driver_id`, `user_id`, `license_number`, `license_expiry`, `vehicle_id`, `total_deliveries`, `rating`, `status`, `created_at`) VALUES
(7, 18, 'DL123456', '2026-12-31', NULL, 0, 0.00, 'Active', '2025-11-26 18:57:46'),
(8, 19, 'DL789012', '2027-06-30', NULL, 0, 0.00, 'Active', '2025-11-26 18:57:46'),
(9, 20, 'DL345678', '2026-09-15', NULL, 0, 0.00, 'Active', '2025-11-26 18:57:46');

-- --------------------------------------------------------

--
-- Table structure for table `invoices`
--

CREATE TABLE `invoices` (
  `invoice_id` int(11) NOT NULL,
  `shipment_id` int(11) NOT NULL,
  `tracking_number` varchar(100) DEFAULT NULL,
  `customer_id` int(11) NOT NULL,
  `invoice_date` datetime DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `subtotal` decimal(10,2) DEFAULT NULL,
  `tax` decimal(10,2) DEFAULT NULL,
  `discount` decimal(10,2) DEFAULT NULL,
  `surcharge` decimal(10,2) DEFAULT NULL,
  `total` decimal(10,2) DEFAULT NULL,
  `status` varchar(50) DEFAULT 'Unpaid',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `invoices`
--

INSERT INTO `invoices` (`invoice_id`, `shipment_id`, `tracking_number`, `customer_id`, `invoice_date`, `due_date`, `subtotal`, `tax`, `discount`, `surcharge`, `total`, `status`, `created_at`) VALUES
(1, 1, 'SHIP1763772909182', 1, '2025-11-21 19:55:09', NULL, 67.50, 6.75, 0.00, 0.00, 74.25, 'Paid', '2025-11-22 01:14:14'),
(2, 2, 'SHIP1763778207803', 1, '2025-11-21 21:23:27', NULL, 67.50, 6.75, 0.00, 0.00, 74.25, 'Paid', '2025-11-22 02:26:22'),
(3, 3, 'SHIP1763819887426', 1, '2025-11-22 08:58:07', NULL, 77.50, 7.75, 0.00, 0.00, 85.25, 'Paid', '2025-11-22 13:59:16'),
(4, 4, 'SHIP1763864383778', 1, '2025-11-22 21:19:43', NULL, 42.50, 4.25, 0.00, 0.00, 46.75, 'Paid', '2025-11-23 02:20:09'),
(5, 10, 'SHIP1763866571445', 1, '2025-11-22 21:56:11', NULL, 27.50, 2.75, 0.00, 0.00, 30.25, 'Paid', '2025-11-23 02:56:25'),
(6, 11, 'SHIP1763867119390', 1, '2025-11-22 22:05:19', NULL, 57.50, 5.75, 0.00, 0.00, 63.25, 'Unpaid', '2025-11-23 03:05:19'),
(7, 12, 'SHIP1763909485035', 1, '2025-11-23 09:51:25', NULL, 80.00, 8.00, 0.00, 0.00, 88.00, 'Paid', '2025-11-23 14:52:07'),
(8, 13, 'SHIP1763946446721', 1, '2025-11-23 20:07:26', NULL, 57.50, 5.75, 0.00, 0.00, 63.25, 'Paid', '2025-11-24 01:08:25'),
(9, 14, 'SHIP1764184253722', 1, '2025-11-26 14:10:53', NULL, 2530.00, 253.00, 0.00, 0.00, 2783.00, 'Unpaid', '2025-11-26 19:10:53'),
(10, 16, 'SHIP1764224923755', 4, '2025-11-27 01:28:43', NULL, 615.00, 61.50, 0.00, 0.00, 676.50, 'Paid', '2025-11-27 06:30:36'),
(11, 17, 'SHIP1764260979503', 1, '2025-11-27 11:29:39', NULL, 525.00, 52.50, 0.00, 0.00, 577.50, 'Unpaid', '2025-11-27 16:29:39');

-- --------------------------------------------------------

--
-- Table structure for table `managers`
--

CREATE TABLE `managers` (
  `manager_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `department` varchar(100) DEFAULT NULL,
  `employee_id` varchar(50) DEFAULT NULL,
  `hire_date` date DEFAULT NULL,
  `permissions` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `managers`
--

INSERT INTO `managers` (`manager_id`, `user_id`, `department`, `employee_id`, `hire_date`, `permissions`, `created_at`) VALUES
(1, 3, 'Operations', NULL, NULL, NULL, '2025-11-24 02:47:14'),
(2, 21, 'Operations', NULL, NULL, NULL, '2025-11-26 18:57:46');

-- --------------------------------------------------------

--
-- Table structure for table `payments`
--

CREATE TABLE `payments` (
  `payment_id` int(11) NOT NULL,
  `invoice_id` int(11) NOT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `payment_date` datetime DEFAULT NULL,
  `reference_number` varchar(100) DEFAULT NULL,
  `status` varchar(50) DEFAULT 'Completed'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payments`
--

INSERT INTO `payments` (`payment_id`, `invoice_id`, `amount`, `payment_method`, `payment_date`, `reference_number`, `status`) VALUES
(1, 1, 74.25, 'Card', '2025-11-21 19:56:46', NULL, 'Completed'),
(2, 1, 74.25, 'Cash', '2025-11-21 20:01:28', NULL, 'Completed'),
(3, 1, 74.25, 'Cash', '2025-11-21 21:21:57', NULL, 'Completed'),
(4, 2, 74.25, 'Card', '2025-11-21 21:26:22', NULL, 'Completed'),
(5, 3, 85.25, 'Cash', '2025-11-22 08:59:16', NULL, 'Completed'),
(6, 3, 85.25, 'Cash', '2025-11-22 20:25:46', NULL, 'Completed'),
(7, 1, 74.25, 'Card', '2025-11-22 21:18:01', NULL, 'Completed'),
(8, 4, 46.75, 'Cash', '2025-11-22 21:20:09', NULL, 'Completed'),
(9, 5, 30.25, 'Card', '2025-11-22 21:56:25', NULL, 'Completed'),
(10, 7, 88.00, 'Cash', '2025-11-23 09:52:07', NULL, 'Completed'),
(11, 7, 88.00, 'Card', '2025-11-23 19:41:21', NULL, 'Completed'),
(12, 8, 63.25, 'Card', '2025-11-23 20:08:25', NULL, 'Completed'),
(13, 10, 676.50, 'Card', '2025-11-27 01:30:36', NULL, 'Completed');

-- --------------------------------------------------------

--
-- Table structure for table `recipients`
--

CREATE TABLE `recipients` (
  `recipient_id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `zone` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `shipments`
--

CREATE TABLE `shipments` (
  `shipment_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `sender_info` varchar(255) DEFAULT NULL,
  `recipient_info` varchar(255) DEFAULT NULL,
  `weight` decimal(10,2) DEFAULT NULL,
  `dimensions` varchar(100) DEFAULT NULL,
  `package_type` varchar(50) DEFAULT NULL,
  `zone` int(11) DEFAULT NULL,
  `status` varchar(50) DEFAULT 'Pending',
  `tracking_number` varchar(100) DEFAULT NULL,
  `cost` decimal(10,2) DEFAULT NULL,
  `payment_status` varchar(50) DEFAULT 'Unpaid',
  `payment_method` varchar(50) DEFAULT NULL,
  `vehicle_id` int(11) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `expected_delivery_date` date DEFAULT NULL,
  `actual_delivery_date` date DEFAULT NULL,
  `delivery_notes` text DEFAULT NULL,
  `recipient_name` varchar(255) DEFAULT NULL,
  `recipient_phone` varchar(20) DEFAULT NULL,
  `recipient_email` varchar(255) DEFAULT NULL,
  `surcharge` decimal(10,2) DEFAULT 0.00,
  `discount` decimal(10,2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `shipments`
--

INSERT INTO `shipments` (`shipment_id`, `user_id`, `sender_info`, `recipient_info`, `weight`, `dimensions`, `package_type`, `zone`, `status`, `tracking_number`, `cost`, `payment_status`, `payment_method`, `vehicle_id`, `address`, `created_at`, `expected_delivery_date`, `actual_delivery_date`, `delivery_notes`, `recipient_name`, `recipient_phone`, `recipient_email`, `surcharge`, `discount`) VALUES
(1, 1, 'Benjamin Aurelian | 8765089678', 'Bruce Wayne | 876456897478', 5.50, '2.5', 'Express', 2, 'Assigned', 'SHIP1763772909182', 67.50, 'Paid', 'Card', 12, '10 Malibu Rd LA California', '2025-11-22 00:55:09', NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00),
(2, 1, 'ben10 | 5448885565', 'tarzan | 8785545545', 3.50, '7.5', 'Express', 3, 'Assigned', 'SHIP1763778207803', 67.50, 'Paid', 'Card', 10, 'jungle avenue', '2025-11-22 02:23:27', NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00),
(3, 1, 'benjamin Aurelian | 9785464554', 'Christine Robinson | 87545564565', 7.50, '3.5', 'Express', 2, 'Assigned', 'SHIP1763819887426', 77.50, 'Paid', 'Cash', 11, '17 Beverly Hill Hollywood', '2025-11-22 13:58:07', NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00),
(4, 1, 'Benjamin Aurelian | 8764405678', 'Bob the Builder | 7778987000', 2.50, '1.0', 'Standard', 3, 'Pending', 'SHIP1763864383778', 42.50, 'Paid', 'Cash', NULL, '45 Bob Ave CI', '2025-11-23 02:19:43', NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00),
(10, 1, 'benjamin | 87965645', 'Arthur Pendragon | 87454526556', 3.50, '1.0', 'Standard', 1, 'Assigned', 'SHIP1763866571445', 27.50, 'Paid', 'Card', 12, '5 Camelot Avenue', '2025-11-23 02:56:11', NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00),
(11, 1, 'Benjamin Aurelian | 40578945623', 'Eren Jeager | 87454554513', 3.50, '3.5', 'Express', 2, 'Assigned', 'SHIP1763867119390', 57.50, 'Unpaid', NULL, 11, '1 Paradise Isle', '2025-11-23 03:05:19', NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00),
(12, 1, 'Benjamin | 8769867895', 'Marcus Aurelius | 8769875489', 4.00, '1.0', 'Express', 4, 'Pending', 'SHIP1763909485035', 80.00, 'Paid', 'Card', NULL, '45 Malibu Rd', '2025-11-23 14:51:25', NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00),
(13, 1, 'benjamin Aurelian | 8765045845', 'george iv | 8765454550', 3.50, '2.5', 'Express', 2, 'Assigned', 'SHIP1763946446721', 57.50, 'Paid', 'Card', 12, '45 Edwardian lane', '2025-11-24 01:07:26', NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00),
(14, 1, 'Benjamin | Aurelian', 'Mboka | Mensah', 500.00, '2.5', 'Express', 1, 'Assigned', 'SHIP1764184253722', 2530.00, 'Unpaid', NULL, 9, '1 Village Way', '2025-11-26 19:10:53', NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00),
(15, 22, 'Test Sender', 'Test Recipient', 150.00, '50x50x50', 'Standard', 1, 'Assigned', 'TEST1764187232', 75.00, 'Unpaid', NULL, 10, '456 Delivery Ave', '2025-11-26 20:00:32', NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00),
(16, 23, 'Neil Armstrong | 910000475', 'Buzz Aldrin | 7014587878', 111.00, '2.5', 'Express', 4, 'Pending', 'SHIP1764224923755', 615.00, 'Paid', 'Card', NULL, '1 Buzz Lightning way', '2025-11-27 06:28:43', NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00),
(17, 1, 'Benjamin | 87545566544', 'Jack the Beanstalk | 8795456654', 100.00, '1.0', 'Fragile', 1, 'Assigned', 'SHIP1764260979503', 525.00, 'Unpaid', NULL, 10, '1 Fee Fi Fum Lane', '2025-11-27 16:29:39', NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00);

-- --------------------------------------------------------

--
-- Table structure for table `shipment_assignments`
--

CREATE TABLE `shipment_assignments` (
  `assignment_id` int(11) NOT NULL,
  `shipment_id` int(11) NOT NULL,
  `vehicle_id` int(11) NOT NULL,
  `assigned_by` int(11) DEFAULT NULL,
  `assigned_date` datetime DEFAULT NULL,
  `route_order` int(11) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `role` enum('Customer','Clerk','Manager','Driver') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `email`, `phone`, `address`, `role`, `created_at`) VALUES
(1, 'benjamin', 'ben10', 'Ben@rocketuniverse.com', '876-309-9787', '45 star valley rd malibu LA', 'Customer', '2025-11-21 21:44:47'),
(2, 'ReckitRalph', 'reckit', 'ReckitRalph@mariobros.com', '8400556545', '1 Mario rd', 'Clerk', '2025-11-24 02:02:43'),
(3, 'benjamin10', 'ben10', 'ben10', '8760070070', '1 manager rd', 'Manager', '2025-11-24 02:47:14'),
(4, 'Jack', 'jack', 'jack@gmail.com', '876555655', '45 beacon way', 'Customer', '2025-11-25 20:13:16'),
(18, 'john_driver', 'pass123', 'john@smartship.com', '876-111-2222', '10 Main St', 'Driver', '2025-11-26 18:57:46'),
(19, 'jane_driver', 'pass123', 'jane@smartship.com', '876-333-4444', '20 Oak Ave', 'Driver', '2025-11-26 18:57:46'),
(20, 'bob_driver', 'pass123', 'bob@smartship.com', '876-555-6666', '30 Pine Rd', 'Driver', '2025-11-26 18:57:46'),
(21, 'admin_manager', 'admin123', 'manager@smartship.com', '876-777-8888', '100 Admin Blvd', 'Manager', '2025-11-26 18:57:46'),
(22, 'test_customer', 'test123', 'test@test.com', '876-999-0000', '123 Test St', 'Customer', '2025-11-26 20:00:32'),
(23, 'Neil', 'pass123', 'Armstrong@nasa.rockets', '910000475', '100 Luna Way', 'Customer', '2025-11-27 06:22:58'),
(24, 'nathan007', 'nate', 'n8@gmail.com', '8795456654', '1 stress lane', 'Clerk', '2025-11-27 19:45:46');

-- --------------------------------------------------------

--
-- Table structure for table `vehicles`
--

CREATE TABLE `vehicles` (
  `vehicle_id` int(11) NOT NULL,
  `vehicle_number` varchar(50) DEFAULT NULL,
  `driver_id` int(11) DEFAULT NULL,
  `vehicle_type` varchar(100) DEFAULT NULL,
  `license_plate` varchar(50) DEFAULT NULL,
  `capacity` decimal(10,2) DEFAULT NULL,
  `zone` int(11) DEFAULT 1,
  `status` enum('Available','In Use','Full','Inactive','Maintenance') DEFAULT 'Available',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `current_weight` decimal(10,2) DEFAULT 0.00,
  `current_item_count` int(11) DEFAULT 0,
  `last_maintenance_date` date DEFAULT NULL,
  `next_maintenance_date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `vehicles`
--

INSERT INTO `vehicles` (`vehicle_id`, `vehicle_number`, `driver_id`, `vehicle_type`, `license_plate`, `capacity`, `zone`, `status`, `created_at`, `current_weight`, `current_item_count`, `last_maintenance_date`, `next_maintenance_date`) VALUES
(9, 'VEH-001', 18, 'Van', 'ABC-1234', 500.00, 1, 'Available', '2025-11-26 18:57:46', 500.00, 1, NULL, NULL),
(10, 'VEH-002', 19, 'Truck', 'DEF-5678', 1000.00, 1, 'In Use', '2025-11-26 18:57:46', 253.50, 3, NULL, NULL),
(11, 'VEH-003', 20, 'Van', 'GHI-9012', 500.00, 2, 'In Use', '2025-11-26 18:57:46', 11.00, 2, NULL, NULL),
(12, 'VEH-004', NULL, 'Truck', 'JKL-3456', 1500.00, 2, 'Available', '2025-11-26 18:57:46', 12.50, 3, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `vehicle_schedules`
--

CREATE TABLE `vehicle_schedules` (
  `schedule_id` int(11) NOT NULL,
  `vehicle_id` int(11) NOT NULL,
  `scheduled_date` date DEFAULT NULL,
  `departure_time` time DEFAULT NULL,
  `expected_return_time` time DEFAULT NULL,
  `route_description` varchar(255) DEFAULT NULL,
  `status` varchar(50) DEFAULT 'Scheduled'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `audit_logs`
--
ALTER TABLE `audit_logs`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `clerks`
--
ALTER TABLE `clerks`
  ADD PRIMARY KEY (`clerk_id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD KEY `idx_clerk_user_id` (`user_id`);

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`customer_id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD KEY `idx_customer_user_id` (`user_id`);

--
-- Indexes for table `drivers`
--
ALTER TABLE `drivers`
  ADD PRIMARY KEY (`driver_id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD UNIQUE KEY `license_number` (`license_number`),
  ADD KEY `idx_driver_user_id` (`user_id`);

--
-- Indexes for table `invoices`
--
ALTER TABLE `invoices`
  ADD PRIMARY KEY (`invoice_id`),
  ADD UNIQUE KEY `shipment_id` (`shipment_id`),
  ADD KEY `customer_id` (`customer_id`),
  ADD KEY `idx_invoice_tracking` (`tracking_number`);

--
-- Indexes for table `managers`
--
ALTER TABLE `managers`
  ADD PRIMARY KEY (`manager_id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD KEY `idx_manager_user_id` (`user_id`);

--
-- Indexes for table `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`payment_id`),
  ADD KEY `invoice_id` (`invoice_id`);

--
-- Indexes for table `recipients`
--
ALTER TABLE `recipients`
  ADD PRIMARY KEY (`recipient_id`);

--
-- Indexes for table `shipments`
--
ALTER TABLE `shipments`
  ADD PRIMARY KEY (`shipment_id`),
  ADD UNIQUE KEY `tracking_number` (`tracking_number`),
  ADD KEY `idx_shipment_user_id` (`user_id`),
  ADD KEY `idx_shipment_tracking` (`tracking_number`);

--
-- Indexes for table `shipment_assignments`
--
ALTER TABLE `shipment_assignments`
  ADD PRIMARY KEY (`assignment_id`),
  ADD KEY `shipment_id` (`shipment_id`),
  ADD KEY `vehicle_id` (`vehicle_id`),
  ADD KEY `assigned_by` (`assigned_by`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD KEY `idx_user_role` (`role`);

--
-- Indexes for table `vehicles`
--
ALTER TABLE `vehicles`
  ADD PRIMARY KEY (`vehicle_id`),
  ADD UNIQUE KEY `license_plate` (`license_plate`),
  ADD UNIQUE KEY `vehicle_number` (`vehicle_number`),
  ADD KEY `driver_id` (`driver_id`);

--
-- Indexes for table `vehicle_schedules`
--
ALTER TABLE `vehicle_schedules`
  ADD PRIMARY KEY (`schedule_id`),
  ADD KEY `vehicle_id` (`vehicle_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `audit_logs`
--
ALTER TABLE `audit_logs`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `clerks`
--
ALTER TABLE `clerks`
  MODIFY `clerk_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `drivers`
--
ALTER TABLE `drivers`
  MODIFY `driver_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `invoices`
--
ALTER TABLE `invoices`
  MODIFY `invoice_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `managers`
--
ALTER TABLE `managers`
  MODIFY `manager_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `payments`
--
ALTER TABLE `payments`
  MODIFY `payment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `recipients`
--
ALTER TABLE `recipients`
  MODIFY `recipient_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `shipments`
--
ALTER TABLE `shipments`
  MODIFY `shipment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `shipment_assignments`
--
ALTER TABLE `shipment_assignments`
  MODIFY `assignment_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

--
-- AUTO_INCREMENT for table `vehicles`
--
ALTER TABLE `vehicles`
  MODIFY `vehicle_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `vehicle_schedules`
--
ALTER TABLE `vehicle_schedules`
  MODIFY `schedule_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `audit_logs`
--
ALTER TABLE `audit_logs`
  ADD CONSTRAINT `audit_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `clerks`
--
ALTER TABLE `clerks`
  ADD CONSTRAINT `clerks_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `customers`
--
ALTER TABLE `customers`
  ADD CONSTRAINT `customers_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `drivers`
--
ALTER TABLE `drivers`
  ADD CONSTRAINT `drivers_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `invoices`
--
ALTER TABLE `invoices`
  ADD CONSTRAINT `invoices_ibfk_1` FOREIGN KEY (`shipment_id`) REFERENCES `shipments` (`shipment_id`),
  ADD CONSTRAINT `invoices_ibfk_2` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`);

--
-- Constraints for table `managers`
--
ALTER TABLE `managers`
  ADD CONSTRAINT `managers_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`invoice_id`);

--
-- Constraints for table `shipments`
--
ALTER TABLE `shipments`
  ADD CONSTRAINT `shipments_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `shipment_assignments`
--
ALTER TABLE `shipment_assignments`
  ADD CONSTRAINT `shipment_assignments_ibfk_1` FOREIGN KEY (`shipment_id`) REFERENCES `shipments` (`shipment_id`),
  ADD CONSTRAINT `shipment_assignments_ibfk_2` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicles` (`vehicle_id`),
  ADD CONSTRAINT `shipment_assignments_ibfk_3` FOREIGN KEY (`assigned_by`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `vehicles`
--
ALTER TABLE `vehicles`
  ADD CONSTRAINT `vehicles_ibfk_1` FOREIGN KEY (`driver_id`) REFERENCES `drivers` (`user_id`) ON DELETE SET NULL;

--
-- Constraints for table `vehicle_schedules`
--
ALTER TABLE `vehicle_schedules`
  ADD CONSTRAINT `vehicle_schedules_ibfk_1` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicles` (`vehicle_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
