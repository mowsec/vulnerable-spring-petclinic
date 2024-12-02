DROP TABLE customers IF EXISTS;

CREATE TABLE customers (
  id         INTEGER IDENTITY PRIMARY KEY,
  first_name VARCHAR(255),
  last_name  VARCHAR_IGNORECASE(255),
  address    VARCHAR(255),
  city       VARCHAR(80),
  telephone  VARCHAR(20)
);
CREATE INDEX customers_last_name ON customers (last_name);
