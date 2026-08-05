package com.registro.sistema

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class MenuActivity : AppCompatActivity() {

    private lateinit var txtNombre: TextView
    private lateinit var txtUnidad: TextView
    private lateinit var btnAsistencia: Button
    private lateinit var btnRegistroActividades: Button
    private lateinit var btnSupervisionOperativa: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var txtHoraIngreso: TextView
    private lateinit var txtActividades: TextView
    private lateinit var txtSupervisiones: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        txtNombre = findViewById(R.id.txtNombre)
        txtUnidad = findViewById(R.id.txtUnidad)
        txtHoraIngreso = findViewById(R.id.txtHoraIngreso)
        txtActividades = findViewById(R.id.txtActividades)
        txtSupervisiones = findViewById(R.id.txtSupervisiones)
        btnAsistencia = findViewById(R.id.btnAsistencia)
        btnRegistroActividades = findViewById(R.id.btnRegistroActividades)
        btnSupervisionOperativa = findViewById(R.id.btnSupervisionOperativa)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        val session = SessionManager(this)

        txtNombre.text = session.obtenerNombre()
        txtUnidad.text = session.obtenerUnidad()

        btnAsistencia.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    AsistenciaActivity::class.java
                )
            )
        }

        btnSupervisionOperativa.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    SupervisionActivity::class.java
                )
            )
        }

        btnRegistroActividades.setOnClickListener {

            startActivity(

                Intent(

                    this,

                    ActividadesActivity::class.java

                )

            )

        }




        btnCerrarSesion.setOnClickListener {

            session.cerrarSesion()

            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )

            finish()
        }
        cargarResumen()
    }
    override fun onResume() {
        super.onResume()
        cargarResumen()
    }
    private fun cargarResumen() {

        val session =
            SessionManager(this)

        val empleadoId =
            session.obtenerEmpleadoId()

        val url =
            "https://agentesdmq.com/cacmq/api/resumen_dia.php?empleado_id=$empleadoId"

        val request = JsonObjectRequest(

            Request.Method.GET,
            url,
            null,

            { response ->

                txtHoraIngreso.text =
                    "Hora ingreso: " +
                            response.getString("hora_entrada")

                txtActividades.text =
                    "Actividades hoy: " +
                            response.getInt("actividades")

                txtSupervisiones.text =
                    "Supervisiones hoy: " +
                            response.getInt("supervisiones")
            },

            { error ->

                Toast.makeText(
                    this,
                    error.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        Volley.newRequestQueue(this)
            .add(request)
    }
}
