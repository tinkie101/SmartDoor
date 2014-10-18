SmartDoor
=========
<h2>Project Overview</h2>
Android application that uses facial regocnition and voice identification to authenticate users. The project contains the following: <br>
Face,Eyes and Nose Detection <br>
Facial Recognition <br>
Voice Identification <br>
Object oriented database <br>
Text-to-speech <br>
Speech-to-text <br>
Twitter <br>
Raspberry Pi python server <br>

<h2>Libraries Used</h2>
JavaCV <br>
DB4o <br>
Twitter4j <br>
Android User Auth <br>

<h2>Project Contributors</h2>
Eduan Bekkker<br>
Albert Volschenk<br>
Zühnja Riekert<br>

<h2>SDK</h2>
<h3>Recommended SDK</h3>

At the time of development Eclipse was the default SDK for Android by Google thus the whole project was developed in Eclipse. Other options like NetBeans and Android Studio do exist, but this guide will be focussing on Eclipse.

<h3>Installing Eclipse and Android SDK</h3>

Download the latest version of eclipse with the SDK from:
https://developer.android.com/sdk/index.html

Follow the installation guide for the your specific system here: https://developer.android.com/sdk/installing/index.html?pkg=adt

<h3>Installing specific APIs</h3>

Within Eclipse go to Window - Android SDK Manager and install the latest SDK tools and SDK Platform. Additionally to be able to compile the unit tests API 15 is required since it is the oldest compatible version. After installation is complete restarting Eclipse is recommended.


<h2>GIT</h2>

<h3>EGIT</h3>

The SDK downloaded from Google does come with a version of EGIT pre-installed, but it is still recommended to install the latest version. 

<h3>Installing EGIT</h3>

Within Eclipse go to Help - Install New Software. At the top right click Add. Use the name EGIT and location http://download.eclipse.org/egit/updates and click ok. Then select all and next. Accept all the licences and wait for installation to complete. After installation is complete restarting Eclipse is recommended.
 
<h3>Importing project from GIT</h3>

Within Eclipse go to File - Import. Select Git - Projects From Git and click next. Select URI and next. Fill in the Github Project URL in URI. It currently is: 
https://github.com/tinkie101/SmartDoor. Eclipse will automatically complete all the rest. If you wish to be able to push to the project a valid Github username and password for a contributor can be added. 

<h3>Pulling from Github</h3>

In the project explorer right click on a Github project and go to Team - Pull.

<h3>Committing to Github</h3>

In the project explorer right click on a Github project and go to Team - Commit.

<h2>Test Device</h2>

To be able to deploy the application a target device is needed. Unit tests also need to be executed in Dalvik or ART(Android run time) thus it also requires a device.

<h3>Emulator</h3>

An emulator is NOT recommended due to poor performance and complexity of setting up a microphone and camera. The offifial emulator can be installed with the following guide http://developer.android.com/tools/help/emulator.html

A emulator that performs better can be installed from http://www.genymotion.com
Android device

An Android device can be connected via USB. The drivers for this device needs to be installed. On a Ubuntu Linux system no driver installation is needed. A guide can be found here http://developer.android.com/tools/device.html


<h2>Compiling and Running</h2>

<h3>Running the project</h3>

Right click the project in the Project and select Run As - Android Application. Alternatively open any Java file within the project and click the green run button at the top. When a popup comes up select Android Application. 

A popup might come up to select target device to run on. Select the appropriate device and click run.

<h3>Running the Unit and Integration tests</h3>

Right click the project in the test project and select Run As - Android JUnit Test. Alternatively each individual test can be runned by opening the respective java class and clicking the green run button at the top. When a popup comes up select Android JUnit Test.

A popup might come up to select target device to run on. Select the appropriate device and click run.

<h3>Setting default values for settings</h3>

All default settings can easily be changed by editing the strings.xml file which can be found in the res - values folder. The application needs to be completely reinstalled on a device for this to take affect.

<h2>Twitter setup</h2>

For the application to be able to link a twitter account some setup on Twitter’s side will be needed. This will be a small guide on where to get the key, secret, token and token secret.

Regisister a Twitter account for the application at https://twitter.com
Go to https://apps.twitter.com/ and sign in with that specific account.
Create a new app.
Fill in the details. (you can use http://www.example.com for your required website)
Go to keys and Access Tokens tab
Create a new access token. On this page you will find the key, secret, token and token secret.

<h2>Device setup</h2>

For the application to perform optimally some setup is required.

<h3>Text-to-speech</h3>
It is recommended to download the offline package for text-to-speech. This is a quick general guide. This might differ for specific Android devices with skins like Samsung, Sony and HTC. <br>
Go to device settings settings. <br>
Go to Language & Input - Text-to-speech. <br>
Select “Google Text-to-speech Engine” as default and click the settings button next to it. <br>
Select the language “English (United Kingdom)”. <br>
Click Install voice data. Select “English (United Kingdom)” and select a prefered voice. High quality is recommended.

<h3>Speech-to-text</h3>
It is recommended to download the offline package for speech-to-text. This is a quick general guide. This might differ for specific Android devices with skins like Samsung, Sony and HTC. <br>
Go to device settings settings. <br>
Go to Language & Input - Voice Search.  <br>
Select “English (UK)” as the language.<br>
The following is only available on Android version 4.3 and newer: <br>
	Go to Offline speech recognition. <br>
	In the all tab select “English (UK)” <br>
