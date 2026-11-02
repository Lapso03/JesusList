package com.lapso.gdtracker.web;

/**
 * Entrada del historial apuntada a mano. "type" es el nombre de LevelLogType; "date" viene del
 * input datetime-local del navegador (yyyy-MM-ddTHH:mm), o solo la fecha (yyyy-MM-dd), o vacio = ahora.
 */
public record LogEntryRequest(
        String tipo,
        String type,
        String levelName,
        Integer oldPosition,
        Integer newPosition,
        String actor,
        String date
) {
}
