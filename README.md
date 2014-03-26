ENEE408G
========

Source for Final Project of ENEE408G (University of Maryland College Park)

Squealer_android:

This is an Android application intended to allow the user to transmit and 
receive data using the sound-pulse protocol designed for this project.


Below is a general overview of the code:
 - ReceiverFragment contains the UI code for the "Receiving" page of the 
   app. It builds its layout from the res/layout/fragment_receiver.xml
   layout.
 - TransmitterFragment contains the UI code for the "Transmitting" page of
   the app. It builds its layout from the 
   res/layout/fragment_transmitter.xml layout.
 - HelloActivity is the main Android Activity. It contains the
   ReceiverFragment and TrasmitterFragment.
 - PreferenceHelper is a static helper class that makes it easy to access
   the SharedPreferences object of the application.
