package com.registro.sistema

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import com.google.android.gms.location.LocationServices

import org.json.JSONObject

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AsistenciaActivity : AppCompatActivity() {

    private lateinit var btnEntrada: Button
    private lateinit var btnSalida: Button
    private lateinit var btnVolver: Button

    private lateinit var txtHoraEntrada: TextView
    private lateinit var txtUbicacionEntrada: TextView

    private lateinit var txtHoraSalida: TextView
    private lateinit var txtUbicacionSalida: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_asistencia)

        // BOTONES

        btnEntrada = findViewById(R.id.btnEntrada)
        btnSalida = findViewById(R.id.btnSalida)
        btnVolver = findViewById(R.id.btnVolver)

        // TEXTOS

        txtHoraEntrada =
            findViewById(R.id.txtHoraEntrada)

        txtUbicacionEntrada =
            findViewById(R.id.txtUbicacionEntrada)

        txtHoraSalida =
            findViewById(R.id.txtHoraSalida)

        txtUbicacionSalida =
            findViewById(R.id.txtUbicacionSalida)

        // EVENTOS

        btnEntrada.setOnClickListener {

            marcarAsistencia("entrada")
        }

        btnSalida.setOnClickListener {

            marcarAsistencia("salida")
        }

        btnVolver.setOnClickListener {

            finish()
        }
    }

    private fun marcarAsistencia(accion: String) {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )

            return
        }

        val fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(this)

        fusedLocationClient
            .lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {

                    val latitud = location.latitude
                    val longitud = location.longitude

                    val androidId =
                        Settings.Secure.getString(
                            contentResolver,
                            Settings.Secure.ANDROID_ID
                        )

                    enviarAsistencia(
                        accion,
                        latitud,
                        longitud,
                        androidId
                    )

                } else {

                    Toast.makeText(
                        this,
                        "No se pudo obtener ubicación",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun enviarAsistencia(
        accion: String,
        latitud: Double,
        longitud: Double,
        androidId: String
    ) {

        val url =
            "https://agentesdmq.com/cacmq/api/asistencia_marcar.php"

        val params = JSONObject()

        // ID REAL DEL EMPLEADO

        val session = SessionManager(this)
        val empleadoId = session.obtenerEmpleadoId()

        params.put("empleado_id", empleadoId)

        params.put("accion", accion)

        params.put("android_id", androidId)

        params.put("latitud", latitud)

        params.put("longitud", longitud)

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            params,

            { response ->

                try {

                    val success =
                        response.getBoolean("success")

                    val message =
                        response.getString("message")

                    Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                    ).show()

                    if (success) {

                        val horaActual =
                            SimpleDateFormat(
                                "HH:mm:ss",
                                Locale.getDefault()
                            ).format(Date())

                        if (accion == "entrada") {

                            txtHoraEntrada.text =
                                horaActual

                            txtUbicacionEntrada.text =
                                "$latitud, $longitud"
                        }

                        if (accion == "salida") {

                            txtHoraSalida.text =
                                horaActual

                            txtUbicacionSalida.text =
                                "$latitud, $longitud"
                        }
                    }

                } catch (e: Exception) {

                    Toast.makeText(
                        this,
                        "Error JSON: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },

            { error ->

                Toast.makeText(
                    this,
                    "Error conexión: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        val queue = Volley.newRequestQueue(this)

        queue.add(request)
    }
}