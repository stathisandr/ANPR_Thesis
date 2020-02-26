import pyrebase
from datetime import date
from datetime import datetime
import pytz
import cv2
import imutils
import re
import numpy as np
import pytesseract
import time
from PIL import Image, ImageDraw

Motorcycle = "2.7"
LightCar = "3.85"
Truck_3 = "9.8"
Truck_4 = "13.6"

# Firebase Database Config
firebaseConfig = {
  "apiKey": "AIzaSyDG2UafUkEpxBiyNnIS_SMKqUocXBlvKtA",
  "authDomain": "autotollgr-18d29.firebaseapp.com",
  "databaseURL": "https://autotollgr-18d29.firebaseio.com",
  "projectId": "autotollgr-18d29",
  "storageBucket": "autotollgr-18d29.appspot.com",
  "messagingSenderId": "415153179375",
  "appId": "1:415153179375:web:6d9ec25cd669614e78a91a",
  "measurementId": "G-0T3FXDSWXK"
}

# Load Yolo GRLP custom weights, object names & neural network
net = cv2.dnn.readNet("yolov3-GRLP_final.weights", "yolov3-GRLP.cfg")
classes = []
with open("obj.names", "r") as f:
    classes = [line.strip() for line in f.readlines()]
layer_names = net.getLayerNames()
output_layers = [layer_names[i[0] - 1] for i in net.getUnconnectedOutLayers()]
colors = np.random.uniform(0, 255, size=(len(classes), 3))

def vehicletype(customerID):
    vehicletypestr = db.child("Users").child(customerID).child("vehicletype").get().val()
    if vehicletype == "Light car":
        return LightCar
    elif vehicletype == "Motorcycle":
        return Motorcycle
    elif vehicletype == "4-axle truck":
        return Truck_3
    elif vehicletype == ">4-axle truck":
        return Truck_4


def datapost(LPtoString):
    customer = db.child("Users").child(LPtoString).get().val()
    if customer is None:
        violation = {"licenceplate": LPtoString,"day": day,"time": datetime_ATH, "cost": "Unknown"}
        db.child("Violations").push(violation, user['idToken'])
    else:
        CostString = vehicletype(customer)
        purchase = {"day": day,"time": datetime_ATH, "cost": CostString}
        db.child("Purchase").child(LPtoString).push(purchase)

# Whitelist function first-half Licence Plate
def removeFL(string):
	bad_chars = ['C','D','F','G','J','L','Q','R','S','U','V','W']

	for i in bad_chars:
		string = string.replace(i,'')
	return string

def fourwheel(Image):
    fw_width, fw_height = Image.size
    im1 = Image.crop((0,0, fw_width/2, fw_height))
    #print('im.size', im1.size)

    im2 = Image.crop((fw_width/2, 0, fw_width, fw_height))
    #print('im.size', im2.size)

    # Use tesseract to convert image into string
    first_half = pytesseract.image_to_string(im1, lang = 'grlp', config='--psm 9')
    #print("First half :", first_half)
    first_half = "".join(re.findall("[A-Z]+", first_half))
    first_half = removeFL(first_half)

    # Use tesseract to convert image into string
    second_half = pytesseract.image_to_string(im2, lang = 'grlp', config='--psm 10')
    #print("Second half :", second_half)
    second_half = "".join(filter(lambda i: i.isdigit(), second_half))

    LP = first_half+"-"+second_half
    print("Licence Plate :"+ str(LP))
    #print("--- %s seconds ---" %(time.time() - start_time))

    datapost(LP)

def twowheel(Image2):
    tw_width, tw_height = Image2.size
    im_top = Image2.crop((0,0, tw_width, tw_height/2))
    #print('im.size', im1.size)

    im_bot = Image2.crop((0, tw_height/2, tw_width, tw_height))
    #print('im.size', im2.size)

    # Use tesseract to convert image into string
    top = pytesseract.image_to_string(im_top, lang = 'grlp', config='--psm 9')
    print("Top :", top)
    top = "".join(re.findall("[A-Z]+", top))
    top = removeFL(top)

    # Use tesseract to convert image into string
    bottom = pytesseract.image_to_string(im_bot, lang = 'grlp', config='--psm 10')
    print("Bottom :", bottom)
    bottom = "".join(filter(lambda i: i.isdigit(), bottom))

    LP = top+"-"+bottom
    print("Licence Plate :"+ str(LP))
    #print("--- %s seconds ---" %(time.time() - start_time))

    datapost(LP)

# Tesseract-OCR bin
pytesseract.pytesseract.tesseract_cmd = r"/usr/bin/tesseract"

# Initialization of firebase
firebase = pyrebase.initialize_app(firebaseConfig)


auth = firebase.auth()
# Sign in with authenticated autoToll user
user = auth.sign_in_with_email_and_password("autoTollGR@gmail.com", "asdf1234")

# Get a reference to the database service
db = firebase.database()

day = date.today().strftime("%b-%d-%Y")

timezoneATH = pytz.timezone('Europe/Athens')
datetime_ATH = datetime.now(timezoneATH).strftime("%H:%M:%S")

#start_time = time.time()

# Loading image
img = cv2.imread("cars/37.jpg")
height, width, channels = img.shape

# Detecting objects
blob = cv2.dnn.blobFromImage(img, 0.00392, (480, 480), (0, 0, 0), True, crop=False)

net.setInput(blob)
outs = net.forward(output_layers)

# Showing informations on the screen
class_ids = []
confidences = []
boxes = []
for out in outs:
    for detection in out:
        scores = detection[5:]
        class_id = np.argmax(scores)
        confidence = scores[class_id]
        if confidence > 0.5:
            # Object detected
            center_x = int(detection[0] * width)
            center_y = int(detection[1] * height)
            w = int(detection[2] * width)
            h = int(detection[3] * height)

            # Rectangle coordinates
            x = int(center_x - w / 2)
            y = int(center_y - h / 2)

            boxes.append([x, y, w, h])
            confidences.append(float(confidence))
            class_ids.append(class_id)

indexes = cv2.dnn.NMSBoxes(boxes, confidences, 0.5, 0.4)
print(indexes)
font = cv2.FONT_HERSHEY_PLAIN
for i in range(len(boxes)):
    if i in indexes:
        x, y, w, h = boxes[i]
        label = str(classes[class_ids[i]])
        color = colors[i]
        cv2.rectangle(img, (x, y), (x + w, y + h), color, 2)
        cv2.putText(img, "Licence Plate", (x, y), font, 1, color, 1)

# Create new image
prediction = img[y:y + h, x:x + w]
# Save Licence Plate
cv2.imwrite('/home/stathisandr/Desktop/CroppedImages/7.jpg',prediction)

Cropped_img = '/home/stathisandr/Desktop/CroppedImages/7.jpg'
cv2.imshow("Cropped Image", cv2.imread(Cropped_img))

im = Image.open(Cropped_img)
im_width, im_height = im.size
#print('im.size', im.size)

if im_width>im_height and (im_width/im_height<0.8 or im_width/im_height>1.2):
    # Rectangle
    fourwheel(im)
else:
    # Square
    twowheel(im)


cv2.imshow("Image", img)
cv2.waitKey(0)
cv2.destroyAllWindows()
