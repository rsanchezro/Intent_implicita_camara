package com.example.intent_implicita_camara

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.*
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    //Para guardar la imagen
    private lateinit var photoUri: Uri
    lateinit var cameraIntent:Intent
    lateinit var imagen:ImageView
    companion object{ val CODIGORESPUESTA=1}
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        //result contiene un Intent y un codigo de resultado
        if (result.resultCode == Activity.RESULT_OK) {
            // Si la foto fue tomada con éxito la voy a mostrar en la zona de la imagen pero
            // con menor calidad, el uso del get aqui esta obsoleto
           // imagen.setImageBitmap(result.data?.extras?.get("data") as Bitmap)

            //Mejor hacerlo asi
            // Mostrar la imagen tomada desde el archivo guardado
            val inputStream = contentResolver.openInputStream(photoUri)
            Log.i("fichero",photoUri.toString())
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            imagen.setImageBitmap(bitmap)


        } else {
            Toast.makeText(this, "No se tomó la foto", Toast.LENGTH_SHORT).show()
        }
    }
    lateinit var boton:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        inicializarComponentes()



    }

    private fun inicializarComponentes() {

        boton = findViewById(R.id.button)
        imagen = findViewById(R.id.imageView)
        boton.setOnClickListener {
            // Crea un URI para almacenar la imagen
            photoUri = createImageUri()

            // Intent implícito para abrir la cámara
            cameraIntent = Intent(ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(EXTRA_OUTPUT, photoUri)

            // Verifica que exista una app de cámara
            if (cameraIntent.resolveActivity(packageManager) != null) {
                //Función ContextCompat.checkSelfPermission , averiguar permiso

                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    //Muestro dialogo para solicitar el permiso al ususario
                    requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CODIGORESPUESTA)

                } else {
                    //Lanzo la intent implicita
                    takePictureLauncher.launch(cameraIntent)
                }
            } else {
                Toast.makeText(this, "No hay aplicación de cámara disponible", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // Método para crear un URI donde se almacenará la foto
    private fun createImageUri(): Uri {
        val timestamp = System.currentTimeMillis()
        val fileName = "photo_$timestamp.jpg"

        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)

        val uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            file
        )
        return uri
    }

    //Si es necesario solicitar permisos, controlamos la respuesta del usuario
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODIGORESPUESTA) {//Comprobamos si venimos de la petición de solicitud de permisos
            //1
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Si tenemos concedido el permiso de CAMARA
                takePictureLauncher.launch(cameraIntent)
            }

        }
    }



}