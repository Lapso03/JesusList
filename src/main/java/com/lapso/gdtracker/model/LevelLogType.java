package com.lapso.gdtracker.model;

/** Tipos de movimiento que quedan registrados en el historial de una lista. */
public enum LevelLogType {
    /** Un nivel nuevo entra en la lista. */
    ADDED,
    /** Un nivel cambia de posicion a mano (desde el panel de admin). */
    MOVED,
    /** Un nivel se elimina de la lista. */
    REMOVED,
    /** Un nivel pasa de la Main List a la Legacy List (p.ej. empujado por otro que entra). */
    MOVED_TO_LEGACY,
    /** Un nivel vuelve de la Legacy List a la Main List (p.ej. porque otro se elimina). */
    MOVED_TO_MAIN
}
