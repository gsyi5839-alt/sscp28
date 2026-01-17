-- MySQL dump 10.13  Distrib 5.7.44, for Linux (x86_64)
--
-- Host: localhost    Database: xie080886
-- ------------------------------------------------------
-- Server version	5.7.44-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `access_lines`
--

DROP TABLE IF EXISTS `access_lines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `access_lines` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `last_ping_ms` int(11) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `sort_order` int(11) NOT NULL,
  `type` enum('MEMBER','AGENT') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `url` varchar(500) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `access_lines`
--

LOCK TABLES `access_lines` WRITE;
/*!40000 ALTER TABLE `access_lines` DISABLE KEYS */;
/*!40000 ALTER TABLE `access_lines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `captcha_tokens`
--

DROP TABLE IF EXISTS `captcha_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `captcha_tokens` (
  `token` varchar(64) NOT NULL,
  `code` varchar(10) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `used` bit(1) NOT NULL,
  PRIMARY KEY (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `captcha_tokens`
--

LOCK TABLES `captcha_tokens` WRITE;
/*!40000 ALTER TABLE `captcha_tokens` DISABLE KEYS */;
INSERT INTO `captcha_tokens` VALUES ('0853c3e722694de3b9a81818c94ffd40','4490','2026-01-17 09:12:56.641056',_binary ''),('0ac72e56351545278a24b1be3b8f0f1b','9088','2026-01-17 21:02:41.454963',_binary '\0'),('0fb7187d35524922a69fa2b0237b6f60','6863','2026-01-17 08:25:22.324669',_binary '\0'),('1723196a90aa42e3a6785510043c9b5d','2791','2026-01-17 21:02:36.745380',_binary '\0'),('1959239653864731bc89040b919a4abd','1169','2026-01-17 08:28:13.598872',_binary '\0'),('2f935251003f4770921b4ed18fba9025','1675','2026-01-17 08:25:11.505751',_binary ''),('31230ce9211144e0aae229138522fbbc','4142','2026-01-17 08:28:56.116372',_binary ''),('32f9269b72854ae786bc37871c737678','6922','2026-01-18 04:43:22.916476',_binary ''),('369ab724084e4dd08ce454180514c27a','1001','2026-01-18 04:47:54.499644',_binary ''),('3e97d92dabbb463f8cecb1d56ca15471','0190','2026-01-18 02:47:16.961998',_binary '\0'),('407ae457fc4d4b44808128228b9e87f1','1330','2026-01-17 08:29:03.265212',_binary '\0'),('42188ec46d9a4f388bff93e0d0616303','7281','2026-01-18 02:07:46.723119',_binary '\0'),('4292846564944a56835f57f63e339dd9','3965','2026-01-18 02:23:48.813355',_binary ''),('49ffd947c7b04842b095dde30962c90f','0228','2026-01-18 03:29:20.945456',_binary '\0'),('4abd48097ebf47f8aabf887e7f81111f','1251','2026-01-18 02:31:46.696392',_binary '\0'),('5a0b585105a44231b9ce2c1c3e6048e9','5666','2026-01-17 08:50:36.965534',_binary ''),('79e2066ed7ed4c8a95f5395ccfadf3e1','1566','2026-01-18 02:31:03.216322',_binary ''),('7a965d576cfe4462bfbcc412959af816','4213','2026-01-17 08:25:23.152493',_binary ''),('7c311bb1a6034c2cbac7335e7bf03b03','9931','2026-01-18 03:28:22.393387',_binary '\0'),('807660b7c6b24a669017906dd946b2e9','7192','2026-01-17 08:28:49.832051',_binary '\0'),('999a386541b14120a1c3651004aebd4e','6509','2026-01-18 02:31:47.155313',_binary ''),('9bcf53a96e4a47d495211ab666e09072','1068','2026-01-17 06:29:04.785407',_binary '\0'),('a5d044caa203498c972819635a01cf6a','9169','2026-01-17 08:25:28.869670',_binary '\0'),('b955cb056c994c0f80160c361e5773ca','9077','2026-01-17 08:46:24.707184',_binary ''),('bd036d5ab8314541b92f583108c2b7b0','1347','2026-01-17 06:29:08.436525',_binary '\0'),('c262791f2d9c4d1998a351eeffd25fa1','7934','2026-01-18 02:31:59.493448',_binary '\0'),('ca133d64bda64b23957bde7d7d6c19d3','6490','2026-01-18 00:05:11.750474',_binary '\0'),('ce33ad8a6f7246609e81be018c203bd8','6517','2026-01-17 21:31:17.197936',_binary ''),('db46de32de9e4ec8a2f91e778841dab3','1240','2026-01-17 23:37:21.870726',_binary ''),('dce2a1bf7e514e2cbacc802bdf76e9ab','7615','2026-01-18 00:03:47.223330',_binary ''),('df26329b409747dbb50a11b592841160','8767','2026-01-17 06:28:57.320079',_binary ''),('e7555a545c6547fd91bea296bbafe0ed','1035','2026-01-17 21:05:00.967851',_binary '\0'),('f377be62c0404a0b94a3031553868231','5184','2026-01-17 06:29:07.493874',_binary '\0'),('f4de34d6b9d64cdd8dc3f73ab4621dcb','9699','2026-01-18 03:27:26.634082',_binary ''),('fb8090f1a37e4096b789a59c77bb206b','9812','2026-01-18 02:27:10.023888',_binary ''),('ff5e45b1aac94d599bab5267feec5d50','1174','2026-01-17 08:25:17.982965',_binary '\0');
/*!40000 ALTER TABLE `captcha_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `search_items`
--

DROP TABLE IF EXISTS `search_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `search_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `url` varchar(500) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `search_items`
--

LOCK TABLES `search_items` WRITE;
/*!40000 ALTER TABLE `search_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `search_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `avatar` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('USER','ADMIN','MEMBER','AGENT') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  `login_count_without_change` int(11) DEFAULT NULL,
  `password_changed` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,NULL,'2026-01-16 22:15:52.702419','ll48379@bcbbs3.cn',_binary '','ll48379','$2b$10$tN1uyHR.LPFuhV2TlgtzfOSvKX4O6RTpVBMBVFPpe1rdj1BEcgbAa','MEMBER','2026-01-16 22:15:52.702419','ll48379',0,_binary '\0'),(2,NULL,'2026-01-16 22:17:10.942907','ww90034@bcbbs3.cn',_binary '','ww90034','$2b$10$I1c9ARxhUJfR79pZMLhYn.kT/ps.g2AdHFCS6YCHSgnhOe.IvqzmS','AGENT','2026-01-16 22:17:10.942907','ww90034',NULL,NULL),(3,NULL,'2026-01-17 00:26:36.000000',NULL,_binary '','Aa101010','$2a$10$ctAd3L6yoQ0GXv5kb2RFQ.TgL2.VVrAuPReEHZVg1ErdKd27j1/uq','MEMBER','2026-01-18 03:23:01.479694','Aa101010',0,_binary ''),(4,NULL,'2026-01-17 20:34:14.052921','cc1000@local.test',_binary '','cc1000','$2b$10$yTAoNgqD44BDGmL2c68FIuUHKw6VIkEGbXXasRJz6y0AVb0pBvXVW','MEMBER','2026-01-18 04:44:33.655585','cc1000',2,_binary '\0'),(5,NULL,'2026-01-17 20:34:14.052921','aa1000@local.test',_binary '','aa1000','$2b$10$s.MN.3MukjRkZCWVOOvENedkFwWrnCK2810o68pPWfWAHLpL80Sfu','MEMBER','2026-01-17 20:34:14.052921','aa1000',0,_binary '\0');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'xie080886'
--

--
-- Dumping routines for database 'xie080886'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-17 23:45:19
