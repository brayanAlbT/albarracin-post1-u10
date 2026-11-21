# Envios Service — Diagnóstico y Mejora Guiada por Métricas Reales

**Curso:** Patrones de Diseño de Software (Código: 1156701-A) — Unidad 10  
**Semestre:** Sexto Semestre  
**Estudiante:** Brayan Smith Albarracín Tarazona  
**Institución:** Universidad Francisco de Paula Santander (UFPS)  

---

## 1. Descripción del Proyecto

Repositorio del post-contenido de la **Unidad 10: Métricas de Calidad y SonarQube**. Consiste en un proyecto Maven plano en Java 21 (`envios-service`) que implementa el cálculo del costo de envío de paquetes según método de transporte, peso, distancia, fragilidad y tipo de cliente.

- **Parte 1:** Construcción de la versión inicial monolítica de `ServicioCalculoEnvio` con complejidad ciclomática elevada y cobertura deliberadamente baja, configurada con **JaCoCo** y **SonarQube**.
- **Parte 2:** Refactorización aplicando el patrón **Strategy** (`EstrategiaEnvio` y sus cuatro implementaciones), ampliación de la suite de pruebas parametrizadas con JUnit 5 para cubrir todas las ramas y casos límite, y definición de un Quality Gate realista (`Estándar Laboratorio`).

---

## 2. Decisiones de Diseño (1 a 6)

### Decisión 1 — Entorno de análisis: SonarQube local con Docker vs. SonarCloud
Se eligió **SonarQube Community Edition local con Docker** (`sonarqube:community` en `http://localhost:9000`).
- **Ventajas consideradas:** Control total sobre el servidor, los *Quality Profiles* (necesario para duplicar *Sonar Way* y activar la regla S1541 con umbral personalizado) y los *Quality Gates*, sin depender de conectividad a internet ni de cuotas o visibilidad pública obligatoria de planes gratuitos en SonarCloud.
- **Costo asumido:** Consumo local de memoria RAM/CPU mientras el contenedor Docker permanece activo durante el análisis.

### Decisión 2 — Exclusiones del análisis (`sonar.exclusions`)
Se decidió **no excluir ninguna clase** del análisis (propiedad `sonar.exclusions` comentada en `sonar-project.properties`).
- **Justificación:** El proyecto es un módulo Maven plano sin clase `Main` de arranque de Spring Boot ni DTOs autogenerados o código boilerplate sin lógica. Excluir archivos habría ocultado parte de la lógica de negocio; mantener el 100% de `src/main/java` bajo análisis garantiza que las métricas de cobertura y complejidad reflejen fielmente todo el código productivo.

### Decisión 3 — Métrica usada como señal principal para el diagnóstico
Se seleccionó **Cyclomatic Complexity (Complejidad Ciclomática)** en combinación con **Branch Coverage (Cobertura de Ramas)** como señal principal para priorizar `ServicioCalculoEnvio.calcularCosto()`.
- **Justificación:** Según la Tabla 3.2 de la guía de la unidad, `calcularCosto()` alcanza una complejidad ciclomática teórica de **20** (1 base + 4 ramas de `metodoEnvio` + 12 puntos de decisión por tramos de peso/distancia + 1 por `esFragil` + 2 por `tipoCliente`), ubicándose en el rango **"Alta"**. Esto significa que existen al menos 20 caminos independientes de ejecución; con solo 2 pruebas iniciales, la mayoría de las ramas condicionales quedan sin ejercitar, convirtiendo el método en un punto frágil ante regresiones.

### Decisión 4 — Patrón elegido: Strategy vs. State vs. Chain of Responsibility
Se eligió el patrón **Strategy** (`EstrategiaEnvio` con `EnvioEstandarStrategy`, `EnvioExpressStrategy`, `EnvioOvernightStrategy` y `EnvioInternacionalStrategy`) y se descartaron **State** y **Chain of Responsibility**:
- **Por qué Strategy:** Cada rama de `metodoEnvio` selecciona un algoritmo completo e intercambiable a partir de un valor discreto (`"ESTANDAR"`, `"EXPRESS"`, `"OVERNIGHT"`, `"INTERNACIONAL"`), calculando el mismo tipo de resultado (`double` costo) con una fórmula propia y sin relación de orden entre ramas.
- **Por qué se descarta State:** `ServicioCalculoEnvio` es un servicio sin estado (*stateless*); no recuerda un estado interno entre llamadas ni existen transiciones válidas/inválidas entre métodos de envío.
- **Por qué se descarta Chain of Responsibility:** No hay una secuencia de manejadores donde el orden de evaluación importe o varios manejadores puedan procesar parcialmente una misma solicitud; dado el `metodoEnvio`, exactamente una estrategia aplica (resuelta en `O(1)` mediante `Map<String, EstrategiaEnvio>`).
- **Qué se quedó en el orquestador y por qué:** Los ajustes por `esFragil` (recargo del 15%) y `tipoCliente` (`PREMIUM` 10% dto., `CORPORATIVO` 15% dto.) permanecieron en `ServicioCalculoEnvio.calcularCosto()` porque son **reglas transversales** comunes a todos los métodos de envío. Moverlas dentro de cada estrategia habría duplicado esa lógica cuatro veces.

### Decisión 5 — Criterio de pruebas agregadas (Cobertura significativa)
En lugar de agregar pruebas triviales que solo invocan métodos para inflar el porcentaje global, cada nueva prueba se diseñó para ejercitar ramas y valores frontera exactos que la suite inicial dejaba sin cubrir:
- `EnvioEstandarStrategyTest` (`@ParameterizedTest`): cubre los 3 tramos de peso (`<= 5`, `5 < peso <= 20`, `> 20`) y el recargo por `distanciaKm > 500`.
- `EnvioExpressStrategyTest` (`@ParameterizedTest`): cubre los 3 tramos de peso, el recargo por `distanciaKm > 500` y el factor `1.3`.
- `EnvioOvernightStrategyTest`: cubre los 3 tramos de peso y el lanzamiento de `IllegalArgumentException` cuando `distanciaKm > 300`.
- `EnvioInternacionalStrategyTest` (`@ParameterizedTest`): cubre los 3 tramos de peso y el recargo por `distanciaKm > 5000`.
- `ServicioCalculoEnvioTest`: cubre las ramas transversales `esFragil = true`, `tipoCliente = "CORPORATIVO"`, delegación a los 4 métodos y `IllegalArgumentException` para método no soportado.

### Decisión 6 — Realismo del Quality Gate (`Estándar Laboratorio`)
Se definió un Quality Profile duplicando *Sonar Way* con la regla **S1541** (*Cyclomatic Complexity of methods should not exceed the authorized threshold*) configurada en **Threshold = 10**, y un Quality Gate con tres condiciones:
- **Complejidad Ciclomática por método <= 10 (S1541):** Según la Tabla 3.2 de la guía, `10` es el límite superior del rango **"Moderada"** y el estándar por defecto de SonarSource. Exigir un umbral más bajo (ej. 5) forzaría a fragmentar excesivamente métodos simples (como un `switch` o un orquestador con 3 validaciones), mientras que 20 permitiría acumular deuda técnica grave.
- **Coverage >= 70%:** Exigir 100% de cobertura genera incentivos perversos (pruebas vacías o aserciones triviales sin valor de negocio, Sección 2.3 de la guía). Un umbral del 70% garantiza que toda la lógica crítica y sus ramas principales estén probadas de forma honesta.
- **New Code Smells = 0 y Duplicated Lines <= 5%:** Previene regresiones en código nuevo sin bloquear por código heredado inevitable.

---

## 3. Comparación Teórica / Estructural Antes vs. Después

| Métrica | Antes (Parte 1 — Monolítico) | Después (Parte 2 — Strategy) |
| :--- | :---: | :---: |
| **Complejidad ciclomática máxima (`calcularCosto`)** | **20** (Alta) | **5** (Moderada — reducción del **75%**) |
| **Complejidad por clase de estrategia** | N/A | **3 – 4** por estrategia (`EstrategiaEnvio`) |
| **Caminos / ramas cubiertos por pruebas** | 2 casos (`ESTANDAR` básico + `PREMIUM`) | 21 casos parametrizados y unitarios cubriendo todas las ramas |
| **Code Smells por S1541 (CC > 10)** | 1 (en `calcularCosto()`) | 0 (ningún método supera CC = 10) |

> **Nota para completar evidencia visual en entorno con Docker y Maven:**  
> Para generar los reportes HTML/XML de JaCoCo y publicar las métricas reales en su instancia local de SonarQube, ejecute:
> ```bash
> docker run -d --name sonarqube -p 9000:9000 -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true sonarqube:community
> mvn clean verify sonar:sonar -Dsonar.token=TU_TOKEN
> ```
> Las capturas del dashboard inicial/final de SonarQube y del reporte HTML (`target/site/jacoco/index.html`) deben guardarse en la carpeta `docs/`.

---

## 4. Estructura del Repositorio

```text
albarracin-post1-u10/
├── pom.xml
├── sonar-project.properties
├── README.md
├── docs/
└── src/
    ├── main/java/com/universidad/enviosservice/
    │   ├── estrategia/
    │   │   ├── EstrategiaEnvio.java
    │   │   ├── EnvioEstandarStrategy.java
    │   │   ├── EnvioExpressStrategy.java
    │   │   ├── EnvioOvernightStrategy.java
    │   │   └── EnvioInternacionalStrategy.java
    │   └── service/
    │       └── ServicioCalculoEnvio.java
    └── test/java/com/universidad/enviosservice/
        ├── estrategia/
        │   ├── EnvioEstandarStrategyTest.java
        │   ├── EnvioExpressStrategyTest.java
        │   ├── EnvioOvernightStrategyTest.java
        │   └── EnvioInternacionalStrategyTest.java
        └── service/
            └── ServicioCalculoEnvioTest.java
```

---

## 5. Herramientas Utilizadas

- Java 21 LTS, Apache Maven
- JUnit 5 (Jupiter & Parameterized Tests), JaCoCo 0.8.12
- SonarQube Community Edition / Sonar Maven Plugin 4.0
- Git, GitHub

---

## 6. Conclusiones

1. Medir la complejidad ciclomática y la cobertura de ramas antes de refactorizar permite localizar con objetividad exactamente qué método concentra el riesgo de mantenimiento (`calcularCosto()`, con CC = 20) y qué caminos carecen de respaldo en pruebas.
2. La aplicación del patrón **Strategy** redujo la complejidad ciclomática de `ServicioCalculoEnvio.calcularCosto()` de 20 a 5 (una reducción del 75%) al encapsular cada algoritmo de tarifa en una clase independiente y cohesionada, conservando las reglas transversales (`esFragil` y `tipoCliente`) en el orquestador para evitar duplicación.
3. Combinar pruebas parametrizadas enfocadas en fronteras y excepciones con un *Quality Gate* realista (CC <= 10 por método y cobertura >= 70%) fomenta la calidad sostenible del código sin caer en pruebas superficiales diseñadas únicamente para inflar métricas.