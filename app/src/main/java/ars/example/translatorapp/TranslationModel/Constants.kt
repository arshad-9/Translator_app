package ars.example.translatorapp.TranslationModel

import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.Locale

object Constants {

    val languages = mapOf(
        "English (US)" to Locale.US,
        "Hindi" to Locale("hi", "IN"),
        "French" to Locale.FRENCH,
        "Spanish" to Locale("es", "ES"),
        "German" to Locale.GERMAN
    )
    val languagesTranslation = mapOf(
    "English (US)" to TranslateLanguage.ENGLISH,
    "Hindi" to TranslateLanguage.HINDI,
    "French" to TranslateLanguage.FRENCH,
    "Spanish" to TranslateLanguage.SPANISH,
    "German" to TranslateLanguage.GERMAN

    )
   val NotWorked = "Something went wrong.."
}