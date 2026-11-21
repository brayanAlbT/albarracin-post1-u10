package com.universidad.enviosservice.estrategia;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvioInternacionalStrategyTest {

    private final EnvioInternacionalStrategy estrategia = new EnvioInternacionalStrategy();

    @ParameterizedTest
    @CsvSource({
            "3.0, 100.0, 230.0",     // peso <= 5: 180 + 50
            "10.0, 100.0, 245.0",    // 5 < peso <= 20: 230 + 15
            "25.0, 100.0, 297.5",    // peso > 20: 230 + 45 + 22.5
            "3.0, 6000.0, 10950.0"   // distancia > 5000: 10800 + 50 + 100
    })
    void calculaCostoPorTramoDePesoYDistancia(double pesoKg, double distanciaKm, double esperado) {
        assertEquals(esperado, estrategia.calcular(pesoKg, distanciaKm), 0.01);
    }
}