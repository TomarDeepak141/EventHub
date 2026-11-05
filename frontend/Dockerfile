FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ARG VITE_ENABLE_MOCK_PAYMENTS=false
ENV VITE_ENABLE_MOCK_PAYMENTS=${VITE_ENABLE_MOCK_PAYMENTS}
RUN npm run build

FROM nginx:1.28-alpine AS runtime
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/nginx.conf
COPY nginx.default.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
ENV PORT=8080 NGINX_ENVSUBST_FILTER=^PORT$
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=5s --start-period=5s --retries=5 \
  CMD wget -q -O /dev/null "http://127.0.0.1:${PORT:-8080}/healthz" || exit 1
