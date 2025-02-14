package ars.example.translatorapp


import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore

import android.speech.RecognizerIntent

import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View

import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast


import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ars.example.translatorapp.TranslationModel.Constants
import ars.example.translatorapp.TranslationModel.TranslatorClass


import ars.example.translatorapp.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale


class MainActivity : AppCompatActivity(),TextToSpeech.OnInitListener {
    private lateinit var  binding :ActivityMainBinding
    private var fromLanguage:String? = null
    private var toLanguage:String? = null
    private lateinit var translationUnit: TranslatorClass
    private lateinit var textToSpeech: TextToSpeech


    private val mOnActictivityResultLauncher = registerForActivityResult(ActivityResultContracts
        .StartActivityForResult()) {
        if(it.resultCode == RESULT_OK && it.data != null){
           val  textString  = it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            binding.inputText.setText(textString?.get(0)!!)
        }
    }



    private val permissionLauncher =  registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
           showCustomDialog()
        } else {
            Toast.makeText(this, "Permissions denied!", Toast.LENGTH_SHORT).show()
        }
    }



    private val captureImage =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let {
                processImage(it) // Call OCR function
            }
        }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            processImage(bitmap) // Call OCR function
        }
    }


    private val requestAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("Permission", "Audio recording permission granted")
            startSpeechToText(fromLanguage!!)  // Call speech-to-text function
        } else {
            Log.d("Permission", "Audio recording permission denied")
            Toast.makeText(this, "Permission required for speech input", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textToSpeech = TextToSpeech(this, this)
        translationUnit =TranslatorClass()

        binding.speakingBee.setOnClickListener{
            if(toLanguage!=null && binding.outputText.text.toString().isNotEmpty()){
                startTextToSpeech( binding.outputText.text.toString(),toLanguage!!)
            }else{
                Toast.makeText(this@MainActivity,"Select Language",Toast.LENGTH_SHORT).show()
            }
        }


        binding.camera.setOnClickListener{
               checkPermissions()
        }

        binding.inputMic.setOnClickListener{

                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    startSpeechToText(fromLanguage!!)  // Start speech recognition
                } else {
                    requestAudioPermission.launch(android.Manifest.permission.RECORD_AUDIO)
                }


        }

        val adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, Constants.languages.keys.toList())
        binding.languageSpinnerTo.adapter = adapter
        binding.languageSpinnerFrom.adapter =adapter

        binding.languageSpinnerFrom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = Constants.languages.keys.toList()[position]
               fromLanguage = selectedLanguage
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                  // do Nothing ...
            }


        }

        binding.languageSpinnerTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = Constants.languages.keys.toList()[position]
                toLanguage = selectedLanguage
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
              // do Nothing ...
                        }
        }



        binding.translationButton.setOnClickListener {
            if(fromLanguage!=null && toLanguage!=null && binding.inputText.text.toString().isNotEmpty())
            {
              translationUnit.translateText(this,binding.inputText.text.toString(),Constants.languagesTranslation[fromLanguage!!]!!,Constants.languagesTranslation[toLanguage!!]!!)
            }else{
                Toast.makeText(this@MainActivity,"Select Language or Enter Text",Toast.LENGTH_SHORT).show()

            }
        }
    }

    fun onSuccessfullTranslation(result:String)
    {
        if(result == Constants.NotWorked){
            Toast.makeText(this@MainActivity,Constants.NotWorked,Toast.LENGTH_SHORT).show()
        }else{
            binding.outputCard.visibility =View.VISIBLE
            binding.outputText.setText(result)
        }
    }


    private fun startSpeechToText(language: String) {

        val languageCode = Constants.languages[language]?.toLanguageTag()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
        Log.d("SpeechRecognizer", "Using language: ${intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE)}")

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...")
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)

        mOnActictivityResultLauncher.launch(intent)

    }


    private fun checkPermissions() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(arrayOf(
                    android.Manifest.permission.CAMERA,android.Manifest.permission.READ_MEDIA_IMAGES))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionLauncher.launch(arrayOf(
                    android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                showCustomDialog() // No runtime permission needed for Android < 6.0
            }

    }

    private fun startTextToSpeech(text:String,toLanguage: String){
        val locale = Constants.languages[toLanguage] ?: Locale.US
        textToSpeech.language = locale
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language to English
            textToSpeech.language = Locale.US
        }
    }



    fun processImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                Log.d("OCR Result", visionText.text)
                binding.inputText.setText(visionText.text)
            }
            .addOnFailureListener { e ->
                Log.e("OCR Error", "Error: ${e.message}")
            }
    }


    private fun showCustomDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true) // Dismiss when clicking outside
            .create()

        val imageView1 = dialogView.findViewById<ImageView>(R.id.cam)
        val imageView2 = dialogView.findViewById<ImageView>(R.id.file)

        // Set click listeners for images
        imageView1.setOnClickListener {
            captureImage.launch(null)
            dialog.dismiss()
        }

        imageView2.setOnClickListener {
            getContent.launch("image/*")
            dialog.dismiss()
        }

        dialog.show()
    }



}