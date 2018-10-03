FROM node:9
WORKDIR /app
COPY package.json .
RUN npm install
COPY . .
CMD npm start
EXPOSE 80