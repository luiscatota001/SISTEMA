package com.registro.sistema

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import android.widget.Button
import android.widget.EditText
import org.json.JSONObject
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import android.widget.ImageView
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import android.util.Base64

class NuevaSupervisionActivity : AppCompatActivity() {
    private lateinit var txtEmpleado: AutoCompleteTextView
    private lateinit var etObservacion: android.widget.EditText
    private lateinit var btnGuardar: android.widget.Button
    private val listaNombres = ArrayList<String>()
    private val listaEmpleados = ArrayList<Empleado>()
    private var supervisadoId = 0
    private var latitud = 0.0
    private var longitud = 0.0
    private lateinit var btnFoto: Button
    private lateinit var imgFoto: ImageView
    private val REQUEST_FOTO = 1
    private var fotoBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_nueva_supervision
        )

        txtEmpleado =
            findViewById(R.id.txtEmpleado)
        etObservacion =
            findViewById(R.id.etObservacion)
        btnFoto =
            findViewById(R.id.btnFoto)
        imgFoto =
            findViewById(R.id.imgFoto)
        btnGuardar =
            findViewById(R.id.btnGuardar)
        cargarEmpleados()
        obtenerGPS()

        btnFoto.setOnClickListener {

            val intent =
                Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            startActivityForResult(
                intent,
                REQUEST_FOTO
            )
        }

        btnGuardar.setOnClickListener {

            val observacion =
                etObservacion.text.toString()
            guardarSupervision()
            if (supervisadoId == 0) {

                Toast.makeText(
                    this,
                    "Seleccione un empleado",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "Empleado ID: $supervisadoId\nObservación: $observacion",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun cargarEmpleados() {

        val url =
            "https://agentesdmq.com/cacmq/api/lista_empleados.php"

        val request = JsonObjectRequest(

            Request.Method.GET,
            url,
            null,

            { response ->

                try {

                    val success =
                        response.getBoolean("success")

                    if (success) {

                        val data =
                            response.getJSONArray("data")

                        listaNombres.clear()
                        listaEmpleados.clear()

                        for (i in 0 until data.length()) {

                            val obj =
                                data.getJSONObject(i)

                            val id = obj.getInt("id")

                            val nombre = obj.getString("nombre")

                            listaEmpleados.add(
                                Empleado(id, nombre)
                            )

                            listaNombres.add(nombre)
                        }

                        val adapter =
                            ArrayAdapter(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                listaNombres
                            )

                        txtEmpleado.setAdapter(adapter)


                        txtEmpleado.setOnItemClickListener { parent, _, position, _ ->

                            val nombreSeleccionado =
                                parent.getItemAtPosition(position).toString()

                            val empleado =
                                listaEmpleados.firstOrNull {
                                    it.nombre == nombreSeleccionado
                                }

                            if (empleado != null) {

                                supervisadoId = empleado.id

                                Toast.makeText(
                                    this,
                                    "${empleado.nombre} -> ID ${empleado.id}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                } catch (e: Exception) {

                    Toast.makeText(
                        this,
                        e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

            },

            { error ->

                Toast.makeText(
                    this,
                    error.message,
                    Toast.LENGTH_LONG
                ).show()
            }

        )

        Volley.newRequestQueue(this)
            .add(request)
    }

    private fun guardarSupervision() {
        btnGuardar.text = "GUARDANDO..."
        btnGuardar.isEnabled = false
        if (supervisadoId == 0) {

            Toast.makeText(
                this,
                "Seleccione un empleado",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val session =
            SessionManager(this)

        val supervisorId =
            session.obtenerEmpleadoId()

        val observacion =
            etObservacion.text.toString()

        val url =
            "https://agentesdmq.com/cacmq/api/supervision_guardar.php"

        val params = JSONObject()

        params.put(
            "supervisor_id",
            supervisorId
        )

        params.put(
            "supervisado_id",
            supervisadoId
        )

        params.put(
            "observacion",
            observacion
        )

        params.put(
            "latitud",
            latitud
        )

        params.put(
            "longitud",
            longitud
        )

        params.put(
            "android_id",
            android.provider.Settings.Secure.getString(
                contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
        )

        if (fotoBitmap != null) {

            params.put(
                "foto",
                bitmapToBase64(fotoBitmap!!)
            )

        } else {

            params.put(
                "foto",
                ""
            )
        }

        val request = JsonObjectRequest(

            Request.Method.POST,
            url,
            params,

            { response ->

                val success =
                    response.optBoolean(
                        "success",
                        false
                    )

                val mensaje =
                    response.optString(
                        "message"
                    )

                Toast.makeText(
                    this,
                    mensaje,
                    Toast.LENGTH_LONG
                ).show()

                if (success) {

                    finish()
                }
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

    private fun obtenerGPS() {

        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                100
            )

            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {

                    latitud = location.latitude

                    longitud = location.longitude
                }
            }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == REQUEST_FOTO &&
            resultCode == RESULT_OK
        ) {

            fotoBitmap =
                data?.extras?.get("data") as Bitmap

            imgFoto.setImageBitmap(
                fotoBitmap
            )
        }
    }
    private fun bitmapToBase64(
        bitmap: Bitmap
    ): String {

        val baos =
            ByteArrayOutputStream()

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            80,
            baos
        )

        val imageBytes =
            baos.toByteArray()

        return Base64.encodeToString(
            imageBytes,
            Base64.DEFAULT
        )
    }
}
