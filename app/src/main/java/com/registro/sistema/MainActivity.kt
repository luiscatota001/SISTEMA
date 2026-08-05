package com.registro.sistema

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var etCedula: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val session = SessionManager(this)

        if (session.estaLogueado()) {

            startActivity(
                Intent(this, MenuActivity::class.java)
            )

            finish()
            return
        }

        setContentView(R.layout.activity_main)

        etCedula = findViewById(R.id.etCedula)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {

            val cedula = etCedula.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (cedula.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "Complete todos los campos",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                login(cedula, password)
            }
        }
    }

    private fun login(cedula: String, password: String) {

        val url = "https://agentesdmq.com/cacmq/api/api_login.php"

        val params = JSONObject()

        params.put("cedula", cedula)
        params.put("password", password)

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            params,

            { response ->

                try {

                    val success = response.getBoolean("success")

                    if (success) {

                        val empleadoId =
                            response.getInt("empleado_id")

                        val unidadId =
                            response.getInt("unidad_id")

                        val nombre =
                            response.getString("nombre")

                        val unidad =
                            response.getString("unidad")

                        val session =
                            SessionManager(this)

                        session.guardarSesion(
                            empleadoId,
                            unidadId,
                            cedula,
                            nombre,
                            unidad
                        )

                        Toast.makeText(
                            this,
                            "Bienvenido $nombre",
                            Toast.LENGTH_SHORT
                        ).show()

                        // IR AL MENU

                        val intent = Intent(
                            this@MainActivity,
                            MenuActivity::class.java
                        )
                        intent.putExtra(
                            "nombre",
                            response.getString("nombre")
                        )

                        intent.putExtra(
                            "unidad",
                            response.getString("unidad")
                        )
                        startActivity(intent)

                        finish()

                    } else {

                        val mensaje = response.getString("message")

                        Toast.makeText(
                            this,
                            mensaje,
                            Toast.LENGTH_SHORT
                        ).show()
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