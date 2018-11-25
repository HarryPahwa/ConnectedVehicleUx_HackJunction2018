import firebase
import os
import glob
import subprocess
import calendar
import time
import urllib2
import json
import serial

ser = serial.Serial('/dev/ttyACM0', 9600)	
url = 'https://maplevodkahr.firebaseio.com/readings.json'
fb = firebase.FirebaseApplication('https://maplevodkahr.firebaseio.com/', None)

while 1:
	x=5
	z=ser.readline()
	print z
	postdata = {
    		'time': str(calendar.timegm(time.gmtime())),
    		'data': x
		

		}
 	postdata2=x;
	req = urllib2.Request(url)
	req.add_header('Content-Type','application/json')
	data = json.dumps(postdata2)
 
	#response = urllib2.urlopen(req,data)

	
	if(len(z)>3):
		fb.put('HeartRate',"value",int(z[0:-2]))
