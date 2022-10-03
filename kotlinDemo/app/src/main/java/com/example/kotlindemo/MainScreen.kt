package com.example.kotlindemo

import android.app.*
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import org.jsoup.Jsoup
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class MainScreen : AppCompatActivity() {

    lateinit var ImageUri : Uri
    var webUrl = " "
    private val CHANNEL_ID ="channel_id_example_01"
    private val notificationId = 101
    private val SDPath = Environment.getExternalStorageDirectory().absolutePath
    private val dataPath = "$SDPath/AndroidCodility/zipunzipFile/data/"
    private val zipPath = "$SDPath/AndroidCodility/zipunzipFile/zip/"
    private val unzipPath = "$SDPath/AndroidCodility/zipunzipFile/unzip/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)
        createNotificationChannel()

        val selectBtn = findViewById<Button>(R.id.selectImage)
        val uploadBtn = findViewById<Button>(R.id.uploadButton)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val downloadUrl = findViewById<Button>(R.id.downloadBtn)
        val txtView = findViewById<TextView>(R.id.textView2)
        val _mWebUploadBtn = findViewById<Button>(R.id.upldBtn)



        var webBtn = findViewById<Button>(R.id.webBtn)



        selectBtn.setOnClickListener(){
            selectImage()
        }
        uploadBtn.setOnClickListener(){
            uploadImageToFirebase(ImageUri)
            //zipping(ImageUri);
        }
        downloadUrl.setOnClickListener(){
            urlValidation()
        }
        webBtn.setOnClickListener(){
            webUrl = findViewById<EditText>(R.id.webUrl).text.toString()
            doIT(webUrl).execute()
        }
        _mWebUploadBtn.setOnClickListener(){
            doIT(webUrl).execute()
            var d = doIT(webUrl)
            println(d.total)

        }

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val name = "Notification Title"
        val descriptionText="Notification Description"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun sendNotification(){
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alert")
            .setContentText("Something is added on your cloud firestore image")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)){
            notify(notificationId, builder.build())
        }
    }


    fun selectImage(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK){
            ImageUri = data?.data!!
            val imageView = findViewById<ImageView>(R.id.imageView)
            imageView.setImageURI(ImageUri)





        }
    }


    fun zipping(fileUri: Uri){
        //Utility().checkPermission(this)
    }



     fun uploadImageToFirebase(fileUri: Uri) {

        if (fileUri != null) {
            val fileName = UUID.randomUUID().toString() +".jpg"

            val refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
            val reducedImage: ByteArray = byteArrayOutputStream.toByteArray()

            refStorage.putBytes(reducedImage)
                .addOnSuccessListener(
                    OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                            val imageUrl = it.toString()
                            if (imageUrl!=null){
                                val db = FirebaseFirestore.getInstance()

                                val user = hashMapOf(
                                    "first" to imageUrl,

                                )

// Add a new document with a generated ID
                                db.collection("images")
                                    .add(user)
                                    .addOnSuccessListener { documentReference ->
                                        Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                                        db.collection("images")

                                            .addSnapshotListener { snapshots, e ->
                                                if (e != null) {
                                                    Log.w(TAG, "listen:error", e)
                                                    return@addSnapshotListener
                                                }

                                                for (dc in snapshots!!.documentChanges) {
                                                    when (dc.type) {
                                                        DocumentChange.Type.ADDED -> sendNotification()
                                                        DocumentChange.Type.MODIFIED -> Log.d(TAG, "Modified city: ${dc.document.data}")
                                                        DocumentChange.Type.REMOVED -> Log.d(TAG, "Removed city: ${dc.document.data}")
                                                    }
                                                }
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(TAG, "Error adding document", e)
                                    }
                            }
                            Toast.makeText(applicationContext, imageUrl, Toast.LENGTH_SHORT).show()
                            val browserIntent =
                                Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl))
                            startActivity(browserIntent)

                        }
                    })

                ?.addOnFailureListener(OnFailureListener { e ->
                    print(e.message)
                })
        }
    }

    fun btnNotify(view: View) {

    }

    fun upldToFirestore(urll: String){
        val db = FirebaseFirestore.getInstance()

        val user = hashMapOf(
            "first" to "Ada",
            "last" to "Lovelace",
            "born" to 1815
        )

// Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }


    }

    fun urlValidation(){
        val pasteUrl = findViewById<EditText>(R.id.pasteUrl)
        val url = pasteUrl.text.toString()
        if (url.equals(null)){
            Toast.makeText(applicationContext, "Please enter your URL", Toast.LENGTH_SHORT).show()
        }
        else{
            val myExecutor = Executors.newSingleThreadExecutor()
            val myHandler = Handler(Looper.getMainLooper())
            var mImage: Bitmap?
            myExecutor.execute {
                mImage = mLoad(url)
                myHandler.post {

                    if(mImage!=null){
                        mSaveMediaToStorage(mImage)
                    }
                }
            }

        }
    }

    private fun mLoad(string: String): Bitmap? {
        val url: URL = mStringToURL(string)!!
        val connection: HttpURLConnection?
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return BitmapFactory.decodeStream(bufferedInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
        }
        return null
    }

    private fun mStringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }

    private fun mSaveMediaToStorage(bitmap: Bitmap?) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this , "Saved to Gallery" , Toast.LENGTH_SHORT).show()
        }
    }



    class doIT(url: String) : AsyncTask<Void?, Void?, Void?>() {
        var words: String? = null
        var webFin: String = url
        var total: Int = 0;

        var imggg : HashMap<Int, String>
                = HashMap<Int, String> ()


        override fun doInBackground(vararg p0: Void?): Void? {
            try {
                val document = Jsoup.connect(webFin).get()
                words = document.text()
                val img = document.getElementsByTag("img")
                var k =0;
                for (imageElement in img) {

                    //make sure to get the absolute URL using abs: prefix
                    val strImageURL = imageElement.attr("abs:src")


                    //download image one by one
                    //println(strImageURL)
                    //println(k++)



                    imggg.put(k++, strImageURL)

//                    println(imggg)







                }
                println(img.size)




//                println(imggg)





            } catch (e: IOException) {
                e.printStackTrace()
            }


            return null
        }

        private fun demo(links: String) {
            TODO("Not yet implemented")
            println(String)

        }
        inline fun Context.toast(message:String){
            Toast.makeText(this, message ,  Toast.LENGTH_SHORT).show()
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            println(imggg)


        }

        fun uploadBunch(){

                val db = FirebaseFirestore.getInstance()


                db.collection("users")
                    .add(imggg)
                    .addOnSuccessListener {
                        println("Success")
                    }
                    .addOnFailureListener(){
                        println("Failed")
                    }





        }
        fun dem(){
            println("Hello")

        }


    }


    fun listAdpt(){

        val arrayAdapter: ArrayAdapter<*>
        val users = arrayOf(
            "Virat Kohli", "Rohit Sharma", "Steve Smith",
            "Kane Williamson", "Ross Taylor"
        )

        var mListView = findViewById<ListView>(R.id.list)
        arrayAdapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, users)
        mListView.adapter = arrayAdapter
    }


    }
