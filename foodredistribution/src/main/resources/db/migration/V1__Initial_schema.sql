-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: foodredistribution
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `food_claims`
--

DROP TABLE IF EXISTS `food_claims`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `food_claims` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `claimed_at` datetime(6) DEFAULT NULL,
  `quantity_claimed` int DEFAULT NULL,
  `food_post_id` bigint DEFAULT NULL,
  `receiver_id` bigint DEFAULT NULL,
  `cancellation_reason` varchar(255) DEFAULT NULL,
  `cancelled_at` datetime(6) DEFAULT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `dispute_reason` varchar(255) DEFAULT NULL,
  `disputed_at` datetime(6) DEFAULT NULL,
  `donor_confirmed` bit(1) NOT NULL,
  `donor_confirmed_at` datetime(6) DEFAULT NULL,
  `receiver_confirmed` bit(1) NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `cancellation_note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi1sugkvmqnejxj63mg5w7gybj` (`food_post_id`),
  KEY `FKsg1myhv8mqrvkgsq9ni69811` (`receiver_id`),
  CONSTRAINT `FKi1sugkvmqnejxj63mg5w7gybj` FOREIGN KEY (`food_post_id`) REFERENCES `food_posts` (`id`),
  CONSTRAINT `FKsg1myhv8mqrvkgsq9ni69811` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `food_claims`
--

LOCK TABLES `food_claims` WRITE;
/*!40000 ALTER TABLE `food_claims` DISABLE KEYS */;
/*!40000 ALTER TABLE `food_claims` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `food_post_images`
--

DROP TABLE IF EXISTS `food_post_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `food_post_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `food_post_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK85xi31d6ye4kxom36ggb0khtw` (`food_post_id`),
  CONSTRAINT `FK85xi31d6ye4kxom36ggb0khtw` FOREIGN KEY (`food_post_id`) REFERENCES `food_posts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `food_post_images`
--

LOCK TABLES `food_post_images` WRITE;
/*!40000 ALTER TABLE `food_post_images` DISABLE KEYS */;
/*!40000 ALTER TABLE `food_post_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `food_posts`
--

DROP TABLE IF EXISTS `food_posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `food_posts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `expiry_time` datetime(6) DEFAULT NULL,
  `food_name` varchar(255) DEFAULT NULL,
  `pickup_address` varchar(255) DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `status` enum('AVAILABLE','CLAIMED','EXPIRED','DELETED') DEFAULT NULL,
  `donor_id` bigint DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK85wksg2r5rhq2t4foxk3iil6l` (`donor_id`),
  CONSTRAINT `FK85wksg2r5rhq2t4foxk3iil6l` FOREIGN KEY (`donor_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `food_posts`
--

LOCK TABLES `food_posts` WRITE;
/*!40000 ALTER TABLE `food_posts` DISABLE KEYS */;
/*!40000 ALTER TABLE `food_posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profile_images`
--

DROP TABLE IF EXISTS `profile_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `profile_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhx0na5k81mx4a38c3em3gsosu` (`user_id`),
  CONSTRAINT `FK6577qi31mxp06kavqxmt68fds` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile_images`
--

LOCK TABLES `profile_images` WRITE;
/*!40000 ALTER TABLE `profile_images` DISABLE KEYS */;
/*!40000 ALTER TABLE `profile_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ratings`
--

DROP TABLE IF EXISTS `ratings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ratings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `review` varchar(255) DEFAULT NULL,
  `stars` int DEFAULT NULL,
  `claim_id` bigint DEFAULT NULL,
  `rated_user_id` bigint DEFAULT NULL,
  `reviewer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3aylwhl44rxlrfa1o0wuwbxe9` (`claim_id`),
  KEY `FKluqvvmyyo8xvjhwg50v1cwss2` (`rated_user_id`),
  KEY `FKlqk0um24dql1k0ldnfphtd2ru` (`reviewer_id`),
  CONSTRAINT `FK3aylwhl44rxlrfa1o0wuwbxe9` FOREIGN KEY (`claim_id`) REFERENCES `food_claims` (`id`),
  CONSTRAINT `FKlqk0um24dql1k0ldnfphtd2ru` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKluqvvmyyo8xvjhwg50v1cwss2` FOREIGN KEY (`rated_user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ratings`
--

LOCK TABLES `ratings` WRITE;
/*!40000 ALTER TABLE `ratings` DISABLE KEYS */;
/*!40000 ALTER TABLE `ratings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refresh_tokens`
--

DROP TABLE IF EXISTS `refresh_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expiry_date` datetime(6) NOT NULL,
  `token` varchar(512) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKghpmfn23vmxfu3spu3lfg4r2d` (`token`),
  UNIQUE KEY `UK7tdcd6ab5wsgoudnvj7xf1b7l` (`user_id`),
  CONSTRAINT `FK1lih5y2npsf8u5o3vhdb9y0os` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refresh_tokens`
--

LOCK TABLES `refresh_tokens` WRITE;
/*!40000 ALTER TABLE `refresh_tokens` DISABLE KEYS */;
/*!40000 ALTER TABLE `refresh_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reports` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_note` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `reason` enum('ABUSIVE_BEHAVIOR','DONOR_UNREACHABLE','FAKE_POSTING','FOOD_NOT_AVAILABLE','FOOD_NOT_RECEIVED','OTHER','RECEIVER_NO_SHOW','WRONG_LOCATION') DEFAULT NULL,
  `reported_at` datetime(6) DEFAULT NULL,
  `reviewed` bit(1) NOT NULL,
  `claim_id` bigint DEFAULT NULL,
  `reported_user_id` bigint DEFAULT NULL,
  `reporter_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbytl3m8486ubjpwpqmci64g5` (`claim_id`),
  KEY `FKb3bqi44mjskbnwupr31nfq5ui` (`reported_user_id`),
  KEY `FKd3qiw2om5d2oh5xb7fbdcq225` (`reporter_id`),
  CONSTRAINT `FKb3bqi44mjskbnwupr31nfq5ui` FOREIGN KEY (`reported_user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKbytl3m8486ubjpwpqmci64g5` FOREIGN KEY (`claim_id`) REFERENCES `food_claims` (`id`),
  CONSTRAINT `FKd3qiw2om5d2oh5xb7fbdcq225` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
/*!40000 ALTER TABLE `reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `phone_verified` bit(1) NOT NULL,
  `email_verified` bit(1) NOT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `verification_token` varchar(255) DEFAULT NULL,
  `password_reset_token` varchar(255) DEFAULT NULL,
  `password_reset_token_expiry` datetime(6) DEFAULT NULL,
  `rating` double DEFAULT NULL,
  `banned` bit(1) NOT NULL,
  `dispute_count` bigint DEFAULT NULL,
  `no_show_count` bigint DEFAULT NULL,
  `rating_sum` bigint DEFAULT NULL,
  `report_count` bigint DEFAULT NULL,
  `successful_donations` bigint DEFAULT NULL,
  `successful_pickups` bigint DEFAULT NULL,
  `total_ratings` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-07 18:15:23
