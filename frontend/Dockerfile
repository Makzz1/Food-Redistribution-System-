# Stage 1: Build the React App
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

# Copy all the frontend code and build it for production
COPY . .
RUN npm run build

# Stage 2: Serve the built app using Nginx (a lightweight web server)
FROM nginx:alpine

# Copy custom Nginx configuration for SPA routing
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the built React files from the 'builder' stage into Nginx
COPY --from=builder /app/dist /usr/share/nginx/html

# Expose port 80 (default HTTP port)
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
