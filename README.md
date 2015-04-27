# Chat-App
##SMS Messaging Android Application
https://github.com/CS485-004/Chat-App

##Usage
  This is an android project with gradle files included which makes it easy to import into Android Studio.  Android Studio allows the user to pull this repository directly into a local project from this VCS repository.  This is the debugging version and will output to logs as the program runs.  
  
  The easiest way to create an executable is to import this project into Android Studio.  Android Studio allows the user to create a debug version on a connected physical device. To create a stand-alone apk file, import the project into Android Studio and choose the Generate Signed APK from the build menu to create a release version of the project.
  More information is available here:
  https://developer.android.com/tools/building/building-studio.html

##Synopsis
The program meets the following requirements.

* A main activity displaying recent contacts.
* An options menu allowing users to enable or disable notifications.
* A new message activity that allows the user to send an SMS message to a phone number.
* A chatting conversation activity listing the received and sent messages to a contact.

##Known Bugs and Limitations

* While running in the background, the app may occassionally crash when it receives a new text message.
* The app may crash when it looks up a contact that does not exist.
