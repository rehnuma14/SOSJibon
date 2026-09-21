package com.example.sosjibon.ai

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IntentClassifier(
    context: Context
) {

    companion object {

        private const val TAG = "IntentClassifier"

        private const val MODEL_FILE =
            "sos_jibon_intent_classifier_final.tflite"

        private const val LABELS_FILE =
            "labels.json"

        private const val INPUT_LENGTH = 25

        private const val EXPECTED_CLASSES = 14

        /*
         * Minimum confidence required for the AI
         * to accept a prediction.
         *
         * If confidence is below this value,
         * the AI will return UNKNOWN.
         *
         * UNKNOWN is handled by AiAssistantController
         * and will open Settings.
         */
        private const val CONFIDENCE_THRESHOLD = 0.60f
    }

    private val tokenizer =
        LocalTokenizer(context)

    private val labels =
        loadLabels(context)

    private val interpreter: Interpreter

    init {

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "Initializing SOS Jibon AI"
        )

        Log.d(
            TAG,
            "Vocabulary size = ${tokenizer.vocabularySize()}"
        )

        Log.d(
            TAG,
            "Labels = ${labels.size}"
        )

        Log.d(
            TAG,
            "Labels = $labels"
        )

        val modelBuffer =
            loadModelFile(context)

        interpreter =
            Interpreter(modelBuffer)

        val inputTensor =
            interpreter.getInputTensor(0)

        val outputTensor =
            interpreter.getOutputTensor(0)

        Log.d(
            TAG,
            "Model input shape = ${inputTensor.shape().contentToString()}"
        )

        Log.d(
            TAG,
            "Model input type = ${inputTensor.dataType()}"
        )

        Log.d(
            TAG,
            "Model output shape = ${outputTensor.shape().contentToString()}"
        )

        Log.d(
            TAG,
            "Model output type = ${outputTensor.dataType()}"
        )

        Log.d(
            TAG,
            "Confidence threshold = $CONFIDENCE_THRESHOLD"
        )

        Log.d(
            TAG,
            "================================"
        )
    }

    fun classify(text: String): AiIntent {

        val query = text.trim().lowercase()

        /*
         * First handle very clear commands.
         * These rules protect common commands from
         * being rejected by the confidence threshold.
         */

        when {

            query.contains("resource") ||
                    query.contains("health resource") ||
                    query.contains("tips") ||
                    query.contains("guideline") ||
                    query.contains("health information") -> {
                Log.d(TAG, "Keyword match -> RESOURCES")
                return AiIntent.RESOURCES
            }

            query.contains("setting") ||
                    query.contains("settings") ||
                    query.contains("configuration") -> {
                Log.d(TAG, "Keyword match -> SETTINGS")
                return AiIntent.SETTINGS
            }

            query.contains("developer") ||
                    query.contains("developers") -> {
                Log.d(TAG, "Keyword match -> DEVELOPERS")
                return AiIntent.DEVELOPERS
            }

            query.contains("vault") ||
                    query.contains("medical vault") -> {
                Log.d(TAG, "Keyword match -> VAULT")
                return AiIntent.VAULT
            }

            query.contains("gps") ||
                    query.contains("map") ||
                    query.contains("location") -> {
                Log.d(TAG, "Keyword match -> GPS_MAP")
                return AiIntent.GPS_MAP
            }

            query.contains("emergency") ||
                    query.contains("sos") -> {
                Log.d(TAG, "Keyword match -> EMERGENCY_SOS")
                return AiIntent.EMERGENCY_SOS
            }

            query.contains("contact") ||
                    query.contains("contacts") -> {
                Log.d(TAG, "Keyword match -> EMERGENCY_CONTACTS")
                return AiIntent.EMERGENCY_CONTACTS
            }

            query.contains("first aid") ||
                    query.contains("first-aid") -> {
                Log.d(TAG, "Keyword match -> FIRST_AID")
                return AiIntent.FIRST_AID
            }
        }

        /*
         * If there is no obvious keyword match,
         * use the trained AI model.
         */

        val probabilities = predict(text)

        if (probabilities.isEmpty()) {
            return AiIntent.UNKNOWN
        }

        val bestIndex =
            probabilities.indices.maxByOrNull {
                probabilities[it]
            } ?: return AiIntent.UNKNOWN

        val bestConfidence =
            probabilities[bestIndex]

        val bestLabel =
            labels.getOrNull(bestIndex)

        Log.d(TAG, "--------------------------------")
        Log.d(TAG, "Query = \"$text\"")
        Log.d(TAG, "Best index = $bestIndex")
        Log.d(TAG, "Best label = $bestLabel")
        Log.d(TAG, "Confidence = $bestConfidence")

        logTopPredictions(probabilities)

        /*
         * Low confidence = unknown.
         * Unknown will later open Settings.
         */

        if (bestConfidence < CONFIDENCE_THRESHOLD) {

            Log.d(
                TAG,
                "Confidence too low -> UNKNOWN -> Settings"
            )

            return AiIntent.UNKNOWN
        }

        return if (bestLabel != null) {
            labelToIntent(bestLabel)
        } else {
            AiIntent.UNKNOWN
        }
    }

    fun getConfidence(
        text: String
    ): Float {

        val probabilities =
            predict(text)

        return probabilities.maxOrNull()
            ?: 0f
    }

    private fun predict(
        text: String
    ): FloatArray {

        val tokenIds =
            tokenizer.tokenize(text)

        Log.d(
            TAG,
            "Token IDs for \"$text\" = ${
                tokenIds.contentToString()
            }"
        )

        /*
         * Model input:
         *
         * INT32
         * Shape = [1, 25]
         */

        val inputBuffer =
            ByteBuffer
                .allocateDirect(
                    INPUT_LENGTH * 4
                )
                .order(
                    ByteOrder.nativeOrder()
                )

        for (
        i in 0 until INPUT_LENGTH
        ) {

            inputBuffer.putInt(
                tokenIds.getOrElse(i) {
                    0
                }
            )
        }

        inputBuffer.rewind()

        /*
         * Model output:
         *
         * FLOAT32
         * Shape = [1, 14]
         */

        val outputBuffer =
            ByteBuffer
                .allocateDirect(
                    EXPECTED_CLASSES * 4
                )
                .order(
                    ByteOrder.nativeOrder()
                )

        interpreter.run(
            inputBuffer,
            outputBuffer
        )

        outputBuffer.rewind()

        val probabilities =
            FloatArray(
                EXPECTED_CLASSES
            )

        for (
        i in probabilities.indices
        ) {

            probabilities[i] =
                outputBuffer.float
        }

        return probabilities
    }

    private fun logTopPredictions(
        probabilities: FloatArray
    ) {

        val topIndexes =
            probabilities.indices
                .sortedByDescending {
                    probabilities[it]
                }
                .take(3)

        for (
        index in topIndexes
        ) {

            val label =
                labels.getOrNull(index)
                    ?: "UNKNOWN"

            val confidence =
                probabilities[index]

            Log.d(
                TAG,
                "Prediction: $label = $confidence"
            )
        }
    }

    private fun labelToIntent(
        label: String
    ): AiIntent {

        return when (
            label.trim().uppercase()
        ) {

            "HOME" ->
                AiIntent.HOME

            "RESOURCES" ->
                AiIntent.RESOURCES

            "VAULT" ->
                AiIntent.VAULT

            "SETTINGS" ->
                AiIntent.SETTINGS

            "EDIT_PROFILE" ->
                AiIntent.EDIT_PROFILE

            "SECURITY" ->
                AiIntent.SECURITY

            "PRIVACY_TERMS" ->
                AiIntent.PRIVACY_TERMS

            "DEVELOPERS" ->
                AiIntent.DEVELOPERS

            "EMERGENCY_CONTACTS" ->
                AiIntent.EMERGENCY_CONTACTS

            "EMERGENCY_SOS" ->
                AiIntent.EMERGENCY_SOS

            "GPS_MAP" ->
                AiIntent.GPS_MAP

            "COMMUNITY_STORIES" ->
                AiIntent.COMMUNITY_STORIES

            "RESOURCE_ASSESSMENT" ->
                AiIntent.RESOURCE_ASSESSMENT

            "FIRST_AID" ->
                AiIntent.FIRST_AID

            else -> {

                Log.e(
                    TAG,
                    "Unknown label: $label"
                )

                AiIntent.UNKNOWN
            }
        }
    }

    private fun loadModelFile(
        context: Context
    ): ByteBuffer {

        val fileDescriptor =
            context.assets.openFd(
                MODEL_FILE
            )

        FileInputStream(
            fileDescriptor.fileDescriptor
        ).use { inputStream ->

            val channel =
                inputStream.channel

            return channel
                .map(
                    java.nio.channels.FileChannel.MapMode.READ_ONLY,
                    fileDescriptor.startOffset,
                    fileDescriptor.declaredLength
                )
                .order(
                    ByteOrder.nativeOrder()
                )
        }
    }

    private fun loadLabels(
        context: Context
    ): List<String> {

        val jsonText =
            context.assets
                .open(LABELS_FILE)
                .bufferedReader()
                .use {
                    it.readText()
                }

        val jsonArray =
            org.json.JSONArray(
                jsonText
            )

        val result =
            mutableListOf<String>()

        for (
        i in 0 until jsonArray.length()
        ) {

            result.add(
                jsonArray.getString(i)
            )
        }

        if (
            result.size != EXPECTED_CLASSES
        ) {

            Log.e(
                TAG,
                "Expected $EXPECTED_CLASSES labels but found ${result.size}"
            )
        }

        return result
    }

    fun close() {

        interpreter.close()

        Log.d(
            TAG,
            "IntentClassifier closed"
        )
    }
}