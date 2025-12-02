-- Fix for foreign key constraint error between cart and users tables
-- This script drops and recreates the cart-related tables with correct column types

-- Drop tables in correct order (respecting foreign key dependencies)
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS cart;

-- The cart table will be automatically recreated by Hibernate with the correct schema
-- when you restart the application

-- Note: If you have important data in these tables, back them up first!
-- You can backup with:
-- mysqldump -u root -p elsun cart cart_item > cart_backup.sql

