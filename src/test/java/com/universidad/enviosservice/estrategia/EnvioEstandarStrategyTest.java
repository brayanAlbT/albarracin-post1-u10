package com.universidad.enviosservice.estrategia;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvioEstandarStrategyTest {

    private final EnvioEstandarStrategy estrategia = new EnvioEstandarStrategy();

    @ParameterizedTest
    @CsvSource({
            "3.0, 100.0, 50.0",     // peso <= 5, distancia <= 500
            "10.0, 100.0, 54.0",    // 5 < peso <= 20: 50 + 5*0.8
            "25.0, 100.0, 68.0",    // peso > 20: 50 + 12 + 5*1.2
            "3.0, 600.0, 310.0"     // distancia > 500: 300 + 10
    })
    void calculaCostoPorTramoDePesoYDistancia(double pesoKg, double distanciaKm, double esperado) {
        assertEquals(esperado, estrategia.calcular(pesoKg, distanciaKm), 0.01);
    }
}