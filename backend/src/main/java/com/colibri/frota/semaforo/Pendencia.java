package com.colibri.frota.semaforo;

public record Pendencia(String tipo, StatusSemaforo status, String motivo,
                        Integer kmRestantes, Integer diasRestantes) {
}