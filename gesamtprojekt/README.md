# 📦 Integratives Gesamtprojekt

This repository contains the **Integratives Gesamtprojekt** of **Group 1.3** for the program  
**MCI – Digital Business & Software Engineering**.

---

## 🚀 Project Setup

### 1️. Clone the Repository

```bash 
git clone https://github.com/tobiasmair/Integratives_Gesamtprojekt.git
cd gesamtprojekt
```

### 2️. Prerequisites
Ensure that Docker and Docker Compose are installed and running.

The project was developed and tested with:

- Docker: 29.1.3
- Docker Compose: v5.0.0

Check your versions using:
```bash 
docker --version
docker compose version
```

### 3. Start the Docker Containers
Run the following command to start the containers:
```bash 
docker compose up -d
```
Note:
During the first build, the database image will be created automatically.

(Optional) Check Container Status:
```bash 
docker compose ps
```
Expected status:
```Up <n> seconds (healthy)```


(Optional) Verify Database Access:
```bash 
docker exec -it gesamtprojekt_db psql -U postgres -d postgres_database
```
If successful, you should see:
```postgres_database=#```
Exit the SQL shell with:
```bash 
\q
```

## ▶️ Running the Application
in the  project folder ```gesamtprojekt```, start the application using:
  ```bash
  ./mvnw spring-boot:run
  ```

### Running the application
in the  project folder ```gesamtprojekt```, run
  ```bash
  ./mvnw spring-boot:run
  ```

⚠️ Permission Issue:

If you encounter:```Permission denied```

Fix it by making the wrapper executable:
```bash
chmod u+x mvnw
```


## 🛑 Stopping Containers & Resetting the Database

### Stop the Database Container
```bash
docker stop gesamtprojekt_db
```
- Or stop it manually via Docker Desktop.

### Stop and Remove Containers
```bash
docker compose down
```

### Optional Cleanup Flags
- Remove volumes
```bash
docker compose down -v
```

- Remove database images:
```bash
docker compose down --rmi all
```

## 💡Updating the database
To add newly added data into the database dump, run
```bash
docker exec -t gesamtprojekt_db pg_dump -U postgres -d postgres_database --clean --if-exists > docker/postgres/init.sql
```
and commit the updated version of docker/postgres/init.sql to git