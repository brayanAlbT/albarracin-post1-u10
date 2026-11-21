# Envios Service — Análisis de Métricas de Calidad (Parte 1)

## Entorno de análisis
SonarQube Community Edition local con Docker (`sonarqube:community` en `http://localhost:9000`) y JaCoCo 0.8.12.

## Estado inicial del análisis
- `ServicioCalculoEnvio.calcularCosto()` concentra una complejidad ciclomática de **20** (CC = 1 + 19 puntos de decisión) y una suite inicial que solo ejercita 2 caminos (`ESTANDAR` con peso <= 5 km/distancia <= 500, cliente `REGULAR` y `PREMIUM`).
