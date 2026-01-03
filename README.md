# 👻 Ghost: On-Device AI Chat Assistant

**Ghost** is a privacy-first Android application that provides smart chat suggestions using on-device AI. By combining **MediaPipe LLM Inference** with **ML Kit Text Recognition**, Ghost "reads" your screen and suggests witty replies in real-time without your data ever leaving the device.

---

## 🚀 Key Features

* **On-Device LLM:** Powered by Google's **Gemma 2B**, running locally via MediaPipe.
* **Real-time OCR:** Uses ML Kit to recognize text from screen captures.
* **Floating UI:** A non-intrusive Jetpack Compose overlay bubble that floats over any messaging app.
* **Privacy Focused:** Zero internet required for AI processing; chat data is never uploaded to a cloud.

---

## 🛠️ Setup & Installation

### 1. Prerequisites
* **Android Device:** Android 14 (API 34) or higher (Required for modern MediaProjection).
* **RAM:** Minimum 4GB (8GB recommended for smooth Gemma 2B performance).
* **ADB Tools:** Installed on your computer to push the model file.

### 2. Download the Model
Due to the file size (~1.5GB) and licensing, you must manually download the Gemma model weights:

1.  Visit the [Gemma 2B IT TFLite Repository](https://huggingface.co/google/gemma-2b-it-tflite/blob/main/gemma-2b-it-cpu-int4.bin).
2.  Log in to Hugging Face and accept the **Gemma Terms of Use**.
3.  Download the file: **`gemma-2b-it-cpu-int4.bin`**.

### 3. Deploy the Model using ADB
Android apps cannot bundle 1.5GB files easily without exceeding APK limits. Use the **Android Debug Bridge (ADB)** to push the model to your device's internal storage:



```bash
# 1. Connect your device and ensure USB Debugging is enabled
# 2. Create the target directory on the phone
adb shell mkdir -p /data/local/tmp/llm/

# 3. Push the model file (Rename it to model.bin for the code to find it easily)
# Replace 'path/to/' with your actual download folder path
adb push path/to/gemma-2b-it-cpu-int4.bin /data/local/tmp/llm/model.bin