package com.registro.sistema

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Button
import android.widget.ImageView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import android.util.Base64
import java.io.ByteArrayOutputStream

class NuevaActividadActivity : AppCompatActivity() {
    private lateinit var txtCategoria: AutoCompleteTextView
    private lateinit var txtActividad: AutoCompleteTextView
    private lateinit var etDetalle: EditText
    private lateinit var btnFoto: Button
    private lateinit var btnGuardar: Button
    private lateinit var imgFoto: ImageView
    private val listaCategorias =
        ArrayList<Categoria>()
    private val listaActividades =
        ArrayList<Actividad>()
    private var categoriaId = 0
    private var actividadId = 0
    private var latitud = 0.0
    private var longitud = 0.0
    private val REQUEST_FOTO = 1
    private var fotoBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_nueva_actividad
        )
        txtCategoria =
            findViewById(R.id.txtCategoria)

        txtActividad =
            findViewById(R.id.txtActividad)

        etDetalle =
            findViewById(R.id.etDetalle)

        btnFoto =
            findViewById(R.id.btnFoto)

        btnGuardar =
            findViewById(R.id.btnGuardar)

        imgFoto =
            findViewById(R.id.imgFoto)

        cargarCategorias()
        obtenerGPS()

        btnFoto.setOnClickListener {

            val intent =
                Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE
                )

            startActivityForResult(
                intent,
                REQUEST_FOTO
            )
        }
        btnGuardar.setOnClickListener {

            guardarActividad()
        }
    }
    private fun cargarCategorias() {

        val url =
            "https://agentesdmq.com/cacmq/api/lista_categorias.php"

        val request = JsonObjectRequest(

            Request.Method.GET,
            url,
            null,

            { response ->

                val data =
                    response.getJSONArray("data")

                val nombres =
                    ArrayList<String>()

                listaCategorias.clear()

                for (i in 0 until data.length()) {

                    val obj =
                        data.getJSONObject(i)

                    val categoria =
                        Categoria(

                            obj.getInt("id"),

                            obj.getString("nombre")
                        )

                    listaCategorias.add(categoria)

                    nombres.add(
                        categoria.nombre
                    )
                }

                val adapter =
                    ArrayAdapter(

                        this,

                        android.R.layout.simple_dropdown_item_1line,

                        nombres
                    )

                txtCategoria.setAdapter(adapter)

                txtCategoria.setOnItemClickListener { parent, _, position, _ ->

                    val nombreSeleccionado =
                        parent.getItemAtPosition(position).toString()

                    val categoria =
                        listaCategorias.firstOrNull {

                            it.nombre == nombreSeleccionado
                        }

                    if (categoria != null) {

                        categoriaId =
                            categoria.id

                        cargarActividades(
                            categoriaId
                        )
                    }
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

    private fun cargarActividades(
        categoriaId: Int
    ) {

        val url =
            "https://agentesdmq.com/cacmq/api/lista_actividades.php?categoria_id=$categoriaId"

        val request = JsonObjectRequest(

            Request.Method.GET,
            url,
            null,

            { response ->

                val data =
                    response.getJSONArray("data")

                val nombres =
                    ArrayList<String>()

                listaActividades.clear()

                for (i in 0 until data.length()) {

                    val obj =
                        data.getJSONObject(i)

                    val actividad =
                        Actividad(

                            obj.getInt("id"),

                            obj.getString("nombre")
                        )

                    listaActividades.add(
                        actividad
                    )

                    nombres.add(
                        actividad.nombre
                    )
                }

                val adapter =
                    ArrayAdapter(

                        this,

                        android.R.layout.simple_dropdown_item_1line,

                        nombres
                    )

                txtActividad.setAdapter(
                    adapter
                )
                txtActividad.setOnItemClickListener { parent, _, position, _ ->

                    val nombreSeleccionado =
                        parent.getItemAtPosition(position).toString()

                    val actividad =
                        listaActividades.firstOrNull {

                            it.nombre == nombreSeleccionado
                        }

                    if (actividad != null) {

                        actividadId =
                            actividad.id

                        Toast.makeText(

                            this,

                            "Actividad ID: $actividadId",

                            Toast.LENGTH_SHORT

                        ).show()
                    }
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

    private fun obtenerGPS() {

        val fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(this)

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

                    latitud =
                        location.latitude

                    longitud =
                        location.longitude
                }
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

    private fun guardarActividad() {

        if (categoriaId == 0 || actividadId == 0) {

            Toast.makeText(
                this,
                "Seleccione categoría y actividad",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val session =
            SessionManager(this)

        val empleadoId =
            session.obtenerEmpleadoId()

        val url =
            "https://agentesdmq.com/cacmq/api/actividad_guardar.php"

        val params =
            JSONObject()

        params.put(
            "empleado_id",
            empleadoId
        )

        params.put(
            "categoria_id",
            categoriaId
        )

        params.put(
            "actividad_id",
            actividadId
        )

        params.put(
            "observacion",
            etDetalle.text.toString()
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

        val request =
            JsonObjectRequest(

                Request.Method.POST,
                url,
                params,

                { response ->

                    Toast.makeText(
                        this,
                        response.optString("message"),
                        Toast.LENGTH_LONG
                    ).show()

                    if (
                        response.optBoolean(
                            "success",
                            false
                        )
                    ) {

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
}