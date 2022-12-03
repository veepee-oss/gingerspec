CREATE TABLE IF NOT EXISTS weather1 (city String, temp_lo Int, temp_hi Int, prcp Float, date Date) ENGINE = Memory;
TRUNCATE TABLE weather1;
INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Caracas', 15, 43, 0.0, '2004-11-29');
INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Barcelona', 5, 37, 0.4, '2014-11-29');
INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Madrid', 8, 37, 0.4, '2016-11-30');