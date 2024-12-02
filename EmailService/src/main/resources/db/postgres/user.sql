CREATE DATABASE petclinic;
CREATE DATABASE pii;
CREATE USER petclinic WITH PASSWORD 'petclinic';
--GRANT admins TO petclinic;
--GRANT ALL PRIVILEGES on schema public TO petclinic WITH GRANT OPTION;
GRANT ALL ON DATABASE petclinic TO petclinic;
