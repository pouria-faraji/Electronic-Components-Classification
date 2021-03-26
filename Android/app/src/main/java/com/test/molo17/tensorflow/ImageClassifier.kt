package com.test.molo17.tensorflow

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class ImageClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    var isInitialized = false
        private set

    private var gpuDelegate: GpuDelegate? = null
    var labels = ArrayList<String>()

    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0

    var inputImageBuffer: TensorImage? = null

    fun initialize(){
        val assetManager = context.assets
        val model = loadModelFile(assetManager, "mobilenet_v2_fine_tuned.tflite")
        labels = loadLines(context, "labels.txt")

        val compatList = CompatibilityList()

        val options = Interpreter.Options().apply{
            if(compatList.isDelegateSupportedOnThisDevice){
                // if the device has a supported GPU, add the GPU delegate
                val delegateOptions = compatList.bestOptionsForThisDevice
                this.addDelegate(GpuDelegate(delegateOptions))
                //this.setNumThreads(4)
            } else {
                this.setNumThreads(4)
            }
        }


        val interpreter = Interpreter(model, options)

        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        val imageDataType: DataType = interpreter.getInputTensor(0).dataType()

        // Creates the input tensor.
        inputImageBuffer = TensorImage(imageDataType)

        this.interpreter = interpreter

        isInitialized = true
    }

    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager, filename: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    @Throws(IOException::class)
    fun loadLines(context: Context, filename: String): ArrayList<String> {
        val s = Scanner(InputStreamReader(context.assets.open(filename)))
        val labels = ArrayList<String>()
        while (s.hasNextLine()) {
            labels.add(s.nextLine())
        }
        s.close()
        return labels
    }

    fun classify(bitmap: Bitmap): HashMap<String, String> {
        check(isInitialized) { "TF Lite Interpreter is not initialized yet." }
        Log.d(TAG, "0")
        inputImageBuffer = loadImage(bitmap)

        val output = Array(1) { FloatArray(labels.size) }

        val startTime = SystemClock.uptimeMillis()
        interpreter?.run(inputImageBuffer?.buffer, output)
        val endTime = SystemClock.uptimeMillis()
        var inferenceTime = endTime - startTime
        var indexArray = getMaxResult(output[0])
        Log.d(TAG, output.contentDeepToString())

        var prob1 = (output[0][indexArray[0]]*100).roundToInt().toString() + "%"
        var prob2 = (output[0][indexArray[1]]*100).roundToInt().toString() + "%"
        var prob3 = (output[0][indexArray[2]]*100).roundToInt().toString() + "%"

        return hashMapOf("prediction_1" to labels[indexArray[0]], "prob_1" to prob1,
                "prediction_2" to labels[indexArray[1]], "prob_2" to prob2,
                "prediction_3" to labels[indexArray[2]], "prob_3" to prob3,
                "inference" to "$inferenceTime ms")
    }
    private fun loadImage(bitmap: Bitmap): TensorImage? {

        // Loads bitmap into a TensorImage.
        inputImageBuffer!!.load(bitmap)

        // TODO: Define an ImageProcessor from TFLite Support Library to do preprocessing
        val imageProcessor: ImageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(inputImageWidth, inputImageHeight, ResizeMethod.NEAREST_NEIGHBOR))
                .add(NormalizeOp(IMAGE_MEAN, IMAGE_STD))
                .build()
        return imageProcessor.process(inputImageBuffer)
    }
    private fun getMaxResult(result: FloatArray): IntArray {

        var sortedResult = result.sortedDescending()
        var indexArray = IntArray(3)
        for (i in 0..2){
            indexArray[i] = result.indexOf(sortedResult[i])
        }

        return indexArray
    }
    fun close(){
        if (interpreter != null) {
            // TODO: Close the interpreter
            interpreter!!.close()
            interpreter = null
        }
        // TODO: Close the GPU delegate
        if (gpuDelegate != null) {
            gpuDelegate!!.close()
            gpuDelegate = null
        }
    }
    companion object {
        private const val TAG = "TfliteClassifier"
        private const val FLOAT_TYPE_SIZE = 4
        private const val CHANNEL_SIZE = 3
        private const val IMAGE_MEAN = 0f
        private const val IMAGE_STD = 255f
    }
}