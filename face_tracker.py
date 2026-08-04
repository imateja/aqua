import cv2
import mediapipe as mp
from mediapipe.tasks import python
from mediapipe.tasks.python import vision
import socket
import time
import math

UDP_IP = "127.0.0.1"
UDP_PORT = 9000
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

base_options = python.BaseOptions(model_asset_path='face_landmarker.task')
options = vision.FaceLandmarkerOptions(
    base_options=base_options,
    running_mode=vision.RunningMode.VIDEO,
    num_faces=1,
    min_face_detection_confidence=0.5,
    min_tracking_confidence=0.5
)

def calculate_ear(eye_landmarks):
    # Calculate vertical distances between upper and lower eyelid landmarks
    v1 = math.hypot(eye_landmarks[1].x - eye_landmarks[5].x, eye_landmarks[1].y - eye_landmarks[5].y)
    v2 = math.hypot(eye_landmarks[2].x - eye_landmarks[4].x, eye_landmarks[2].y - eye_landmarks[4].y)
    # Calculate horizontal distance between eye corners
    h = math.hypot(eye_landmarks[0].x - eye_landmarks[3].x, eye_landmarks[0].y - eye_landmarks[3].y)
    if h == 0:
        return 0.0
    return (v1 + v2) / (2.0 * h)

# Open the default webcam
cap = cv2.VideoCapture(0)
print(f"Streaming live face tracking data (with blink detection) to {UDP_IP}:{UDP_PORT}...")
print("Press 'ESC' in the webcam window to quit.")

with vision.FaceLandmarker.create_from_options(options) as landmarker:
    while cap.isOpened():
        success, image = cap.read()
        if not success:
            print("Ignoring empty camera frame.")
            continue

        # Flip the image horizontally for a selfie-view display
        image = cv2.flip(image, 1)

        # Convert BGR image to RGB and wrap into MediaPipe Image format
        image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=image_rgb)

        # Generate timestamp in milliseconds for video mode inference
        timestamp_ms = int(time.time() * 1000)

        # Process the image and find face landmarks
        results = landmarker.detect_for_video(mp_image, timestamp_ms)

        if results.face_landmarks:
            for face_landmarks in results.face_landmarks:
                # MediaPipe Face Landmarker indices:
                # 1: Nose tip
                # 234: Left cheek / temporal region
                # 454: Right cheek / temporal region
                # 10: Top of head
                # 152: Chin / bottom of head
                nose = face_landmarks[1]
                left_cheek = face_landmarks[234]
                right_cheek = face_landmarks[454]
                top = face_landmarks[10]
                bottom = face_landmarks[152]

                # Horizontal turn calculation
                face_width = right_cheek.x - left_cheek.x
                if face_width > 0:
                    turn_x = (nose.x - left_cheek.x) / face_width
                else:
                    turn_x = 0.5

                # Vertical nod calculation
                face_height = bottom.y - top.y
                if face_height > 0:
                    turn_y = (nose.y - top.y) / face_height
                else:
                    turn_y = 0.5

                # Clamp values between 0.0 and 1.0
                turn_x = max(0.0, min(1.0, turn_x))
                turn_y = max(0.0, min(1.0, turn_y))

                # MediaPipe landmark indices for the left eye contour
                left_eye_indices = [33, 160, 158, 133, 153, 144]
                left_eye_pts = [face_landmarks[i] for i in left_eye_indices]
                ear = calculate_ear(left_eye_pts)

                # Convert EAR into a clean blink state (1.0 = fully open, 0.0 = closed)
                # Typical open EAR is ~0.25+, closed drops below ~0.20
                blink_val = 1.0 if ear > 0.21 else 0.0

                message = f"{turn_x:.3f},{turn_y:.3f},{blink_val:.1f}"
                sock.sendto(message.encode(), (UDP_IP, UDP_PORT))

        # Debug display window
        cv2.imshow('Face Tracker', image)

        if cv2.waitKey(5) & 0xFF == 27:
            break

cap.release()
cv2.destroyAllWindows()