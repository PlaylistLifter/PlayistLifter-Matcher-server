# Node base image
FROM node:18

# Working directory
WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm install

# Copy rest of the code
COPY . .

# Start app
CMD ["npm", "start"]
