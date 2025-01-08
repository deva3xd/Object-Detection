from django.shortcuts import render

# Create your views here.
import os
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.core.files.storage import default_storage
from ultralytics import YOLO

# Load the YOLO model
model_path = "model/best.pt"  # Update this to your model path
model = YOLO(model_path)

@csrf_exempt
def detect_objects(request):
    if request.method == 'POST' and request.FILES.get('image'):
        # Save the uploaded image
        uploaded_file = request.FILES['image']
        file_path = default_storage.save(uploaded_file.name, uploaded_file)

        # Perform object detection
        results = model(file_path)
        result = results[0]

        # Extract detection results
        detections = []
        for box in result.boxes:
            detections.append({
                'class': result.names[int(box.cls)],
                'confidence': float(box.conf),
                'box': box.xyxy.tolist()  # Bounding box coordinates
            })

        # Clean up saved file
        os.remove(file_path)

        return JsonResponse({'detections': detections})

    return JsonResponse({'error': 'Invalid request'}, status=400)
