# Molo17
This is a private repository for an Electronic Components Detector application.

It's an Android applciation developed in Kotlin in collaboration with Tensroflow Lite. The application tries to detect electronic components in real-time. The 6 components are Capacitor, Diode, Resistor, Inductor, Transformer, and IC. The applciation also shows the confidence of the detection, in other words, the probability of the detected component.

The main directory contains three parts. Android related codes, Python related codes, and the dataset.

# Android
The Android is developed in Kotlin. For working with camera and analysing the camera frames in real-time, [CameraX] library is used, which is part of the [Jetpack] libraries. In order to use Tensorflow models in the Android environment, it has to be converted to Tensorflow Lite models, which have `.tflite` extensions.

# Python
Python codes contain two parts:
- Codes related to create and train the image classification Tensorflow model based on MobileNet.
- Codes related to crawl [Digi-Key], the world's largest seller of electronic components, to create the image datasets

The model whihc is used for image classification is MobileNet version 2. The model has 154 layers, and in the begining, the model is not trained and only the last layer which is the classification layer is trained with 10 epochs. Then, for fine tuning the model, the last 54 layers of the model is also trained with another 10 epochs.
## Evaluation
For evaluation, the dataset is divided into train set (70%) and validation set (30%). The following table is the results after training the model.
| Metric |Train Set | Validation Set |
|--------|--------- | ---------------|
|  Loss  | 0.4465   |  0.4956        |
|Accuracy| 83.91%   |  83.24%        |

The loss used for evaluation is Cross-entropy. 

# Dataset

[CameraX]: <https://developer.android.com/training/camerax>
[Jetpack]: <https://developer.android.com/jetpack>
[Digi-Key]: <https://www.digikey.com/>
