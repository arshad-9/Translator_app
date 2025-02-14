package ars.example.translatorapp.TranslationModel


import android.app.Activity
import android.util.Log
import ars.example.translatorapp.MainActivity
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class TranslatorClass {

   fun translateText(activity : Activity, inputText: String, sourceLang: String, targetLang: String) {

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()

       val translator = Translation.getClient(options)

        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(inputText)
                    .addOnSuccessListener { translatedText ->
                        ( activity as MainActivity).onSuccessfullTranslation(translatedText)
                        Log.d("TESTX",translatedText)

                    }
                    .addOnFailureListener { exception ->
                        ( activity as MainActivity).onSuccessfullTranslation(Constants.NotWorked)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("TESTX", "Model Download Failed: ${exception.localizedMessage}", exception)
            }


    }

}