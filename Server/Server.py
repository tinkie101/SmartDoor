import RPi.GPIO as GPIO
import SocketServer
import RPi.GPIO as GPIO
import time
import threading

GPIO.setmode(GPIO.BOARD)
GPIO.setwarnings(False)
GPIO.setup(7, GPIO.OUT)

def threadWait():
	print "Door Locked"
	GPIO.output(7, GPIO.LOW)

class MyTCPHandler(SocketServer.BaseRequestHandler):
    """
    The RequestHandler class for our server.

    It is instantiated once per connection to the server, and must
    override the handle() method to implement communication to the
    client.
    """
    def handle(self):
        # self.request is the TCP socket connected to the client
        self.data = self.request.recv(1024).strip()

	print "{} wrote:".format(self.client_address[0])
	print self.data
	self.request.sendall("Opening the Door");
	
	if self.data == "open door":
		print "Door Unlocked"
		GPIO.output(7, GPIO.HIGH)
		threading.Timer(5.0, threadWait).start()

if __name__ == "__main__":
    HOST, PORT = "", 2653
	#Set the voltage to low on pin 7
    GPIO.output(7, GPIO.LOW)
    # Create the server, binding to HOST on PORT
    server = SocketServer.TCPServer((HOST, PORT), MyTCPHandler)

    # Activate the server; this will keep running until you
    # interrupt the program with Ctrl-C

    server.serve_forever()
