package com.universidad.enviosservice.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicioCalculoEnvioTest {

    private final ServicioCalculoEnvio servicio = new ServicioCalculoEnvio();

    @Test
    void calculaEnvioEstandarPesoBajo() {
        double costo = servicio.calcularCosto("ESTANDAR", 3.0, 100.0, "REGULAR", false);
        assertEquals(50.0, costo, 0.01);
    }

    @Test
    void aplicaDescuentoClientePremium() {
        double costo = servicio.calcularCosto("ESTANDAR", 3.0, 100.0, "PREMIUM", false);
        assertEquals(45.0, costo, 0.01);
    }

    @Test
    void aplicaDescuentoClienteCorporativo() {
        double costo = servicio.calcularCosto("ESTANDAR", 3.0, 100.0, "CORPORATIVO", false);
        assertEquals(42.5, costo, 0.01);
    }

    @Test
    void aplicaRecargoPorPaqueteFragil() {
        double costo = servicio.calcularCosto("ESTANDAR", 3.0, 100.0, "REGULAR", true);
        assertEquals(57.5, costo, 0.01);
    }

    @Test
    void delegaEnEstrategiaExpressYOvernightEInternacional() {
        assertEquals(156.0, servicio.calcularCosto("EXPRESS", 3.0, 100.0, "REGULAR", false), 0.01);
        assertEquals(220.0, servicio.calcularCosto("OVERNIGHT", 3.0, 100.0, "REGULAR", false), 0.01);
        assertEquals(230.0, servicio.calcularCosto("INTERNACIONAL", 3.0, 100.0, "REGULAR", false), 0.01);
    }

    @Test
    void rechazaMetodoDeEnvioNoSoportado() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.calcularCosto("DRON", 3.0, 100.0, "REGULAR", false));
    }
}