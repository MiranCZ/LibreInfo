# Build Caddy with a rate-limitter (used for the WS)
FROM caddy:2.10.2-builder AS builder
RUN xcaddy build v2.10.2 --with github.com/mholt/caddy-ratelimit@5625512f24f6f59d6f64fb3aafe5eecff0b286db

FROM caddy:2.10.2-alpine
COPY --from=builder /usr/bin/caddy /usr/bin/caddy
