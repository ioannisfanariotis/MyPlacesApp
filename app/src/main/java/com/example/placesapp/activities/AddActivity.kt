package com.example.placesapp.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.placesapp.R
import com.example.placesapp.database.DatabaseHandler
import com.example.placesapp.databinding.ActivityAddBinding
import com.example.placesapp.models.PlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddActivity : AppCompatActivity(), View.OnClickListener {

    private var binding: ActivityAddBinding? = null
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var savedImage: Uri? = null
    private var mlatitude: Double = 0.0
    private var mlongitude: Double = 0.0
    private var option: PlaceModel? = null

    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "AppImages"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbar)
        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolbar?.setNavigationOnClickListener{
            onBackPressed()
        }

        if (intent.hasExtra(MainActivity.EXTRA_DETAILS)){
            option = intent.getParcelableExtra(MainActivity.EXTRA_DETAILS)
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDate()
        }
        updateDate()

        if(option !=null){
            supportActionBar?.title = "Edit Place"
            binding?.etTitle?.setText(option?.title)
            binding?.etDescription?.setText(option?.description)
            binding?.etDate?.setText(option?.date)
            binding?.etLocation?.setText(option?.location)
            mlatitude = option!!.latitude
            mlongitude = option!!.longitude
            savedImage = Uri.parse(option?.image)
            binding?.placeImage?.setImageURI(savedImage)
            binding?.save?.text = "UPDATE"
        }

        binding?.etDate?.setOnClickListener(this)
        binding?.addImage?.setOnClickListener(this)
        binding?.save?.setOnClickListener(this)
    }

    private fun updateDate(){
        val format = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.et_date -> {
                DatePickerDialog(this@AddActivity, dateSetListener, cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val dialogItems = arrayOf("Select photo from Gallery", "Capture photo from Camera")
                pictureDialog.setItems(dialogItems){
                    _, which ->
                    when(which){
                        0 -> selectPhoto()
                        1 -> useCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.save -> {
                when{
                    binding?.etTitle?.text.isNullOrEmpty() ->{
                        Toast.makeText(this, "Please insert a title", Toast.LENGTH_SHORT).show()
                    }
                    binding?.etDescription?.text.isNullOrEmpty() ->{
                        Toast.makeText(this, "Please insert a title", Toast.LENGTH_SHORT).show()
                    }
                    binding?.etLocation?.text.isNullOrEmpty() ->{
                        Toast.makeText(this, "Please insert a title", Toast.LENGTH_SHORT).show()
                    }
                    savedImage == null ->{
                        Toast.makeText(this, "Please insert an image", Toast.LENGTH_SHORT).show()
                    }else ->{
                        val model = PlaceModel(if(option == null) 0 else option!!.id,
                            binding?.etTitle?.text.toString(), savedImage.toString(),
                            binding?.etDescription?.text.toString(), binding?.etDate?.text.toString(),
                            binding?.etLocation?.text.toString(), mlatitude, mlongitude)
                        val dbHandler = DatabaseHandler(this)

                        if(option == null){
                            val addPlaceResult = dbHandler.addPlace(model)
                            if(addPlaceResult>0){
                                Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }else{
                                Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            val updatePlaceResult = dbHandler.updatePlace(model)
                            if(updatePlaceResult>0){
                                Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }else{
                                Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun selectPhoto() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?){
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                rationalDialog()
            }
        }).onSameThread().check()
    }

    private fun useCamera(){
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?){
                if(report!!.areAllPermissionsGranted()){
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                rationalDialog()
            }
        }).onSameThread().check()
    }

    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == GALLERY){
                if (data != null){
                    val contentURI = data.data
                    try {
                        val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        savedImage = saveImage(imageBitmap)
                        Log.e("Saved Image: ", "Path :: $savedImage")
                        binding?.placeImage?.setImageBitmap(imageBitmap)
                    }catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }else if(requestCode == CAMERA){
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap
                savedImage = saveImage(thumbnail)
                Log.e("Saved Image: ", "Path :: $savedImage")
                binding?.placeImage?.setImageBitmap(thumbnail)
            }
        }
    }

    private fun rationalDialog(){
        AlertDialog.Builder(this).setMessage("All permissions not granted.\nChange that in Settings").setPositiveButton("GO TO SETTINGS"){
                _, _ ->
            try{
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }catch (e: ActivityNotFoundException){
                e.printStackTrace()
            }
        }.setNegativeButton("CANCEL"){ dialog, _ ->
            dialog.dismiss()
        }.show()
    }

    private fun saveImage(bitmap: Bitmap): Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}