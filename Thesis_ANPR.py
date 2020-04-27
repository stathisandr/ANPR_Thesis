from PyQt5 import QtCore, QtGui, QtWidgets
from PyQt5.QtWidgets import QFileDialog
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
net = cv2.dnn.readNet("/home/stathisandr/Desktop/yolov3-GRLP_final.weights", "/home/stathisandr/Desktop/yolov3-GRLP.cfg")
classes = []
with open("/home/stathisandr/Desktop/obj.names", "r") as f:
    classes = [line.strip() for line in f.readlines()]
layer_names = net.getLayerNames()
output_layers = [layer_names[i[0] - 1] for i in net.getUnconnectedOutLayers()]
colors = np.random.uniform(0, 255, size=(len(classes), 3))

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

class Ui_MainWindow(object):
    def setupUi(self, MainWindow):
        MainWindow.setObjectName("MainWindow")
        MainWindow.setFixedSize(QtCore.QSize(790, 455))
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap("../Downloads/auto_toll_gr_logo(1).png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        MainWindow.setWindowIcon(icon)

        self.centralwidget = QtWidgets.QWidget(MainWindow)
        self.centralwidget.setObjectName("centralwidget")

        self.frame = QtWidgets.QFrame(self.centralwidget)
        self.frame.setGeometry(QtCore.QRect(10, 10, 331, 421))
        self.frame.setFrameShape(QtWidgets.QFrame.StyledPanel)
        self.frame.setFrameShadow(QtWidgets.QFrame.Raised)
        self.frame.setObjectName("frame")

        self.originalphoto = QtWidgets.QLabel(self.frame)
        self.originalphoto.setGeometry(QtCore.QRect(10, 20, 311, 341))
        self.originalphoto.setScaledContents(True)
        self.originalphoto.setObjectName("originalphoto")

        self.selectimage = QtWidgets.QPushButton(self.frame)
        self.selectimage.setGeometry(QtCore.QRect(10, 380, 151, 25))
        self.selectimage.setObjectName("selectimage")

        self.anprimage = QtWidgets.QPushButton(self.frame)
        self.anprimage.setGeometry(QtCore.QRect(168, 380, 151, 25))
        self.anprimage.setObjectName("anprimage")
        self.anprimage.setDisabled(True)

        self.frame_2 = QtWidgets.QFrame(self.centralwidget)
        self.frame_2.setGeometry(QtCore.QRect(360, 10, 421, 421))
        self.frame_2.setFrameShape(QtWidgets.QFrame.StyledPanel)
        self.frame_2.setFrameShadow(QtWidgets.QFrame.Raised)
        self.frame_2.setObjectName("frame_2")

        self.carlicenceplate = QtWidgets.QLabel(self.frame_2)
        self.carlicenceplate.setGeometry(QtCore.QRect(20, 20, 381, 131))
        self.carlicenceplate.setScaledContents(True)
        self.carlicenceplate.setObjectName("carlicenceplate")

        self.bikelicenceplate = QtWidgets.QLabel(self.frame_2)
        self.bikelicenceplate.setGeometry(QtCore.QRect(46, 26, 331, 251))
        self.bikelicenceplate.setScaledContents(True)
        self.bikelicenceplate.setObjectName("bikelicenceplate")

        self.licenceplatetext = QtWidgets.QLineEdit(self.frame_2)
        self.licenceplatetext.setGeometry(QtCore.QRect(120, 350, 181, 25))
        self.licenceplatetext.setReadOnly(True)
        self.licenceplatetext.setObjectName("licenceplatetext")

        self.extravalidation = QtWidgets.QPlainTextEdit(self.frame_2)
        self.extravalidation.setGeometry(QtCore.QRect(120, 321, 181, 20))
        self.extravalidation.setStyleSheet("background-color: rgb(239, 236, 231); color: #ea0d0d")
        self.extravalidation.setFrameShape(QtWidgets.QFrame.NoFrame)
        self.extravalidation.setFrameShadow(QtWidgets.QFrame.Raised)
        self.extravalidation.setVerticalScrollBarPolicy(QtCore.Qt.ScrollBarAlwaysOff)
        self.extravalidation.setHorizontalScrollBarPolicy(QtCore.Qt.ScrollBarAlwaysOff)
        self.extravalidation.setReadOnly(True)
        self.extravalidation.setObjectName("extravalidation")

        MainWindow.setCentralWidget(self.centralwidget)
        self.statusbar = QtWidgets.QStatusBar(MainWindow)
        self.statusbar.setObjectName("statusbar")
        MainWindow.setStatusBar(self.statusbar)

        self.retranslateUi(MainWindow)
        QtCore.QMetaObject.connectSlotsByName(MainWindow)
    
    def retranslateUi(self, MainWindow):
        _translate = QtCore.QCoreApplication.translate
        MainWindow.setWindowTitle(_translate("MainWindow", "Auto Licence Plate Recognition System"))
        self.selectimage.setText(_translate("MainWindow", "Select Image"))
        self.selectimage.clicked.connect(self.select_image)
        self.anprimage.setText(_translate("MainWindow", "ANPR Image"))
        self.anprimage.clicked.connect(self.anpr_image)

    def select_image(self):
        global path
        filename = QFileDialog.getOpenFileName()
        path = filename[0]
        self.originalphoto.setPixmap(QtGui.QPixmap(path))
        self.carlicenceplate.clear()
        self.bikelicenceplate.clear()
        self.licenceplatetext.clear()
        self.extravalidation.clear()
        self.anprimage.setDisabled(False)
    
    def anpr_image(self):
        image_path = path
        detect_plate(self, image_path)

def detect_plate(self, image_path):
    img = cv2.imread(image_path)
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
    #print(indexes)
    font = cv2.FONT_HERSHEY_PLAIN
    for i in range(len(boxes)):
        if i in indexes:
            x, y, w, h = boxes[i]
            label = str(classes[class_ids[i]])
            color = colors[i]
            cv2.rectangle(img, (x, y), (x + w, y + h), color, 4)
            cv2.putText(img, "Licence Plate", (x, y), font, 3, color, 3)

    try:
        # Create new image
        prediction = img[y:y+h,x:x+w]
    except:
        message_error ="No licence plate found!"
        self.licenceplatetext.setText(message_error)
            
    # Save Licence Plate
    cv2.imwrite('/home/stathisandr/Desktop/7.jpg',prediction)

    Cropped_img = '/home/stathisandr/Desktop/7.jpg'

    im = Image.open(Cropped_img)
    im_width, im_height = im.size

    if im_width>im_height and (im_width/im_height<0.8 or im_width/im_height>1.2):
        type_v = "car"
        # Rectangle
        self.carlicenceplate.setPixmap(QtGui.QPixmap(Cropped_img))
        im_left = im.crop((0,0, im_width/2, im_height))

        im_right = im.crop((im_width/2, 0, im_width, im_height))
        plate_recognition(self,im_left,im_right, type_v)
    else:
        type_v = "bike"
        # Square
        self.bikelicenceplate.setPixmap(QtGui.QPixmap(Cropped_img))
        im_top = im.crop((0,0, im_width, im_height/2))

        im_bot = im.crop((0, im_height/2, im_width, im_height))
        plate_recognition(self,im_top, im_bot,type_V)        
  
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

def datapost(self, LPtoString):
    customer = db.child("Users").child(LPtoString).get().val()

    self.licenceplatetext.setText(LPtoString)

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

def plate_recognition(self,firstImage, secondImage,type_v):
    
    # Use tesseract to convert image into string
    first_half = pytesseract.image_to_string(firstImage, lang = 'grlp', config='--psm 9')
    first_half = "".join(re.findall("[A-Z]+", first_half))
    first_half = removeFL(first_half)

    # Use tesseract to convert image into string
    second_half = pytesseract.image_to_string(secondImage, lang = 'grlp', config='--psm 10')
    second_half = "".join(filter(lambda i: i.isdigit(), second_half))

    LP = first_half+"-"+second_half

    if type_v == "car":
        if len(LP)!=8:
            self.extravalidation.setPlainText("Needs further validation")
    else:
        if len(LP)!=6:
            self.extravalidation.setPlainText("Needs further validation")

    datapost(self,LP)

if __name__ == "__main__":
    import sys
    app = QtWidgets.QApplication(sys.argv)
    MainWindow = QtWidgets.QMainWindow()
    ui = Ui_MainWindow()
    ui.setupUi(MainWindow)
    MainWindow.show()
    sys.exit(app.exec_())