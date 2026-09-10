# =====================================================================
# Imagen del frontend. También en dos etapas:
#
#   1. "build": Node compila el proyecto Angular (ng build).
#   2. Imagen final: Caddy sirviendo los ficheros estáticos resultantes.
#
# Node NO llega a producción. Un Angular compilado son HTML, CSS y JS: no
# necesita ningún runtime de JavaScript en el servidor, solo algo que
# entregue ficheros.
#
# Este mismo Caddy hace de puerta de entrada de todo el sistema: sirve la
# web, reparte /api al contenedor de la API y saca el certificado HTTPS.
# =====================================================================

FROM node:22-alpine AS build

WORKDIR /app

# npm ci (no npm install): instala exactamente las versiones del
# package-lock.json, sin actualizar nada por su cuenta. Una build
# reproducible es una build que no cambia sola entre despliegues.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---------------------------------------------------------------------

FROM caddy:2-alpine

COPY Caddyfile /etc/caddy/Caddyfile

# El build de Angular deja los estáticos en dist/frontend/browser.
COPY --from=build /app/dist/frontend/browser /srv

EXPOSE 80 443
