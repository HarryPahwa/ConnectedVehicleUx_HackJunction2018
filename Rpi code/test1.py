import argparse
import base64
import picamera
import json

from googleapiclient import discovery
from oauth2client.client import GoogleCredentials

import os
import glob
import subprocess
import calendar
import time
import urllib2
import serial
import firebase

from collections import Counter

url = 'https://maplevodkahr.firebaseio.com/DoorBell/Name.json'
fb = firebase.FirebaseApplication('https://maplevodkahr.firebaseio.com/', None)


from pprint import pprint		

def most_common(lst):
	data=Counter(lst)
	return max(lst,key=data.get)


def takephoto():
    camera = picamera.PiCamera()
    camera.capture('image.jpg')

def main():
    strs = ["" for x in range(5)]
    zzz = 1
    camera = picamera.PiCamera()
    while 1:
        camera.capture('image.jpg')
        #takephoto() # First take a picture
   # """Run a label request on a single image"""

        credentials = GoogleCredentials.get_application_default()
        service = discovery.build('vision', 'v1', credentials=credentials)

        with open('image.jpg', 'rb') as image:
            image_content = base64.b64encode(image.read())
            service_request = service.images().annotate(body={
                'requests': [{
                    'image': {
                        'content': image_content.decode('UTF-8')
                    },
                    'features': [{
                        'type': 'FACE_DETECTION',
                        'maxResults': 10
                    }]
                }]
            })
            response = service_request.execute()
#	print response[0]
#	config = json.loads(response)
#	postdata = {
 #               'Name':config["faceAnnotations"]
  #              }
#
 #       req = urllib2.Request(url)
  #      req.add_header('Content-Type','application/json')
   #     data = json.dumps(postdata)

    #    response2 = urllib2.urlopen(req,data)
            #print json.dumps(response, indent=4, sort_keys=True)	#Print it out and make it somewhat pretty.
	    try:
                a=response["responses"][0]["faceAnnotations"][0]["panAngle"]
		b="Straight"
		if(float(a)>20.0):
			b="Left"
		elif(float(a)<-20.0):
			b="Right"
                print a
		print b
		strs[zzz]=b
		zzz=zzz+1
		if(zzz==5):
			zzz=1
            except:
                a="NoData"
		b="LookAtTheCamera"
                print a	
		strs[zzz]=b
		zzz=zzz+1
		if(zzz==5):
			zzz=1

	    print("Most Common:"+most_common(strs))
#print ("LIKELY_ANGRY")	
	#if a=="VERY_UNLIKELY"
	#putdata={"Okay": "There is someone at the door. Image analysis tells me that it is very unlikely that they are angry. It is safe to open the door. Harry" }
	#else
            #putdata={"Okay": "There is someone at the door. Image analysis tells me that they are angry. Please exercise caution. 911 is only a phone call away. Harry"}
	    #req=urllib2.Request(url)
	    #req.add_header('Content-Type','application/json')
	    #data=json.dumps(putdata)
	    #response2=urllib2.urlopen(req,data)
	    fb.put('Sleep',"value",most_common(strs))


if __name__ == '__main__':

    main()
