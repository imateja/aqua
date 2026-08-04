# AquaEngine - 2D Live2D-Style VTuber Engine

AquaEngine is a lightweight, high-performance custom 2D VTuber rendering engine built on top of **LibGDX** (Java) that real-time tracks facial expressions and head movements from a standard webcam via **MediaPipe** (Python) over a UDP network socket.

## Project Architecture

The project is split into two primary decoupled components:

1. **The Vision Tracker (`face_tracker.py`):** Captures live video frames from your webcam, utilizes Google MediaPipe's Face Landmarker task to map 3D facial landmarks, calculates horizontal turns, vertical nods, and Eye Aspect Ratio (EAR) for blinks, and streams them instantly via UDP packets.

2. **The Rendering Engine (`AquaEngine.java` & `ArtMesh.java`):** A custom OpenGL ES mesh-warping engine written in Java that receives the live UDP data packet, offsets and scales modular component layers (head, eyes, nose, eyebrows, mouth), and applies real-time vertex deformations for tracking and blinking.

## Prerequisites

### 1. Python Environment Setup (Face Tracker)

Make sure you have Python installed. It is recommended to use an isolated virtual environment:

```bash
# Create and activate virtual environment
python3 -m venv venv
source venv/bin/activate  # On Windows use: .\venv\Scripts\activate

# Install required dependencies
pip install opencv-python mediapipe
```

### 2. Java Environment Setup (AquaEngine)

Ensure you have **JDK 21+** installed and your project configured with **LibGDX**.

* Place your slice assets (`head.png`, `eyes.png`, `nose.png`, `eyebrows.png`, `mouth.png`) into the resources folder if you use different assets.

## Running the Project

1. **Start the Java Engine First:**
   Run `AquaEngine.java` from your IDE (IntelliJ IDEA, Eclipse, etc.). The console will output:

```text
listening on 9000
```

2. **Start the Python Face Tracker:**
   With your webcam connected, run the tracker script:

```bash
python3 face_tracker.py
```

A debug OpenCV window will open displaying your webcam feed. As you move your head or blink, tracking data is streamed via UDP to `127.0.0.1:9000`, instantly animating your character on screen.

## Key Technical Features

* **Zero-Allocation Network Thread:** Listens asynchronously on a background daemon UDP thread to prevent garbage collection spikes during rendering.

* **Component-Based ArtMesh:** Decouples character parts into independent 3x3 vertex grids, allowing individual offsets, scaling, and custom warping multipliers.

* **Real-time Blink Compression:** Dynamically scales vertical vertex rows toward the center when an eye-closure threshold (EAR) is crossed.