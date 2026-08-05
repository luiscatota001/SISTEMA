package com.registro.sistema

import android.content.Context

class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences(
            "SesionUsuario",
            Context.MODE_PRIVATE
        )

    fun guardarSesion(
        empleadoId: Int,
        unidadId: Int,
        cedula: String,
        nombre: String,
        unidad: String
    ) {

        prefs.edit()
            .putBoolean("logueado", true)
            .putInt("empleado_id", empleadoId)
            .putInt("unidad_id", unidadId)
            .putString("cedula", cedula)
            .putString("nombre", nombre)
            .putString("unidad", unidad)
            .apply()
    }

    fun estaLogueado(): Boolean {

        return prefs.getBoolean(
            "logueado",
            false
        )
    }

    fun obtenerCedula(): String {

        return prefs.getString(
            "cedula",
            ""
        ) ?: ""
    }

    fun obtenerEmpleadoId(): Int {

        return prefs.getInt(
            "empleado_id",
            0
        )
    }

    fun obtenerUnidadId(): Int {

        return prefs.getInt(
            "unidad_id",
            0
        )
    }

    fun obtenerNombre(): String {

        return prefs.getString(
            "nombre",
            ""
        ) ?: ""
    }

    fun obtenerUnidad(): String {

        return prefs.getString(
            "unidad",
            ""
        ) ?: ""
    }

    fun cerrarSesion() {

        prefs.edit()
            .clear()
            .apply()
    }
}