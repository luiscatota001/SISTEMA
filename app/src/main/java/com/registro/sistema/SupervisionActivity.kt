package com.registro.sistema

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
class SupervisionActivity : AppCompatActivity() {

    private lateinit var btnNueva: Button
    private lateinit var listaSupervisiones: ListView

    private val listaDatos =
        ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_supervision)

        btnNueva =
            findViewById(R.id.btnNueva)

        listaSupervisiones =
            findViewById(R.id.listaSupervisiones)

        cargarSupervisiones()

        btnNueva.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    NuevaSupervisionActivity::class.java
                )
            )
        }
    }

    private fun cargarSupervisiones() {

        val session =
            SessionManager(this)

        val supervisorId =
            session.obtenerEmpleadoId()

        val url =
            "https://agentesdmq.com/cacmq/api/supervision_listar.php?supervisor_id=$supervisorId"

        val request = JsonObjectRequest(

            Request.Method.GET,
            url,
            null,

            { response ->

                listaDatos.clear()

                val data =
                    response.getJSONArray("data")

                for (i in 0 until data.length()) {

                    val obj =
                        data.getJSONObject(i)

                    listaDatos.add(

                        obj.getString("fecha") +
                                " " +
                                obj.getString("hora") +
                                "\n" +
                                obj.getString("supervisado")

                    )
                }

                val adapter =
                    ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        listaDatos
                    )

                listaSupervisiones.adapter =
                    adapter
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
    override fun onResume() {

        super.onResume()

        cargarSupervisiones()
    }
}