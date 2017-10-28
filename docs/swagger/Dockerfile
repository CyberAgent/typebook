FROM node:6.9.5-alpine

RUN npm install -g yarn

COPY package.json /opt/app/
COPY yarn.lock /opt/app/
COPY server.js /opt/app/

WORKDIR /opt/app

RUN yarn install && \
  cp -a node_modules/swagger-ui/dist /opt/app/

COPY typebook.yml /opt/app/dist

ENTRYPOINT ["node", "server.js"]
