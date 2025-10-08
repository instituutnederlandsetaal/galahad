FROM node:24.6.0-alpine3.22 AS build
COPY . .
RUN npm ci && npm run build

FROM nginx:1.29.2-alpine3.22-slim
COPY --from=build /dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
