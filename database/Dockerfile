# Start with the official MySQL 8.0 image from Docker Hub
FROM mysql:8.0


# (Optional) If we had a file called init.sql with raw SQL commands to create tables,
# we could copy it into this special folder. MySQL automatically runs any .sql files found here on startup!
COPY init.sql /docker-entrypoint-initdb.d/

# Expose the default MySQL port
EXPOSE 3306
