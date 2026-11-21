package com.universidad.enviosservice.estrategia;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvioExpressStrategyTest {

    private final EnvioExpressStrategy estrategia = new EnvioExpressStrategy();

    @ParameterizedTest
    @CsvSource({
            "3.0, 100.0, 156.0",    // (120) * 1.3
            "10.0, 100.0, 165.75",  // (120 + 7.5) * 1.3
            "25.0, 100.0, 198.25",  // (120 + 22.5 + 10) * 1.3
            "3.0, 600.0, 968.5"     // (720 + 25) * 1.3
    })
    void calculaCostoPorTramoDePesoYDistancia(double pesoKg, double distanciaKm, double esperado) {
        assertEquals(esperado, estrategia.calcular(pesoKg, distanciaKm), 0.01);
    }
}