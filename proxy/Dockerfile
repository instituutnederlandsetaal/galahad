# nginx reverse proxy to combine frontend and backend
#============================

FROM nginx:1.27.2-alpine3.20

# FIX CVE-2023-52425
RUN apk --no-cache add --upgrade expat

# Config file
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80