moVirt
======
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/matobet/moVirt?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

A mobile client for [oVirt](http://www.ovirt.org). 

The unsigned testing release is available at: [moVirt build](https://github.com/matobet/moVirt/blob/master/moVirt/moVirt-debug.apk?raw=true)

###User's guide:    
After successfully installing the application, you need to set the **connection details** up:

* **API URL**: http://host:port/ovirt-engine/api
* **Username**: name@domain - e.g. admin@internal
* **Password**: your password
* **Admin Privilege**: enable or disable the checkbox
* **Advanced Settings**: certificate management

Also you can set **optional settings**:

* **Poll Events**: enable or disable the checkbox
* **Connection Notifications**: enable or disable the checkbox
* **Periodic Sync**: enable or disable the checkbox
* **Sync Interval**: configure the polling interval in minutes
* **Max events locally**: the maximum number of events stored locally  
* **Events Search Query**: poll events for the search query. Admin users only. - e.g. severity > warning  
* **Max VMs polled**: maximum number of VMs polled.  
* **VMs Search Query**: poll VMs for the search query. Admin users only. - e.g. name = "production"   

**Video demonstrations:**

1. [bVNC and Triggers](https://www.youtube.com/watch?v=QnD9v70oefA)  
    Feature demonstration on YouTube

####Remote console
Follow these steps to connect to remote console:

#####VNC
* Install bVNC from Google Play
* Click console button in VM Detail view

#####SPICE
* Install aSPICE(v3.7.7+) from Google Play Store
* Click console button in VM Detail view

####Augmented Reality  
You can use your smartphone/tablet camera to help managing your servers. Follow these steps to access augmented reality:

1. Generate Data Matrix codes for each host you running (this step will be simplified in future).
  * Open `http://host:port/ovirt-engine/api/hosts` in your browser and find host ID.  
    <img src = https://github.com/matobet/moVirt/blob/master/images/data_matrix_guide/Screenshot_1.jpg align="center">
  * Make data matrix codes with any online generator containing these IDs as a text (one code for each ID).  
    <img src = https://github.com/matobet/moVirt/blob/master/images/data_matrix_guide/Screenshot_2.jpg align="center">
  * Print and stick codes to physical servers.
  * Open Scan Codes option in moVirt and you are ready.

###Get in Touch
* Interested about news? Follow us on [Twitter](https://twitter.com/mobileOvirt)
* Have user questions? Subscribe to [oVirt's users list](http://lists.ovirt.org/mailman/listinfo/users) and post a question with subject containing *[moVirt]*
* Want to contribute? Have development oriented questions? Subscribe to [oVirt's devel list](http://lists.ovirt.org/mailman/listinfo/devel) and post a question with subject containing *[moVirt]*
* Want to chat? We are usually hanging in *irc.oftc.net* in *#ovirt* - look for *mbetak* or *tjelinek*. Or anyone else, people are friendly there ;)

###Developer's guide:        
To start contributing to moVirt, you will need to **install** the following things  
For **Windows** users:

* Download and install [Android Studio Bundle](http://developer.android.com/sdk/index.html).
* Add packages in Android SDK Manager ([guide](http://developer.android.com/sdk/installing/adding-packages.html)), you need:
  * Android SDK Platform-tools
  * Android SDK Build-tools v.20 and 21.*.
  * Android Support Library and Repository
  * Android API 20 SDK (you can install additional SDK as per your needs).

For **Linux/MacOS** users:

* Download [Android SDK](http://developer.android.com/sdk/index.html#Other)
* Download and install [Android Studio](http://developer.android.com/sdk/index.html#Other) or any other IDE for Java development.
* Install Android SDK ([guide](http://developer.android.com/sdk/installing/adding-packages.html))
* Add packages in Android SDK Manager ([guide](http://developer.android.com/sdk/installing/adding-packages.html)), you need:
  * Android SDK Platform-tools
  * Android SDK Build-tools v.20 and 21.*.
  * Android Support Library and Repository
  * Android API 20 SDK (you can install additional SDK as per your needs).

Once the installations are done, you can proceed to **import the project** into the workspace.

1. This step only for Windows users.
  * Before importing you need to change symlink. To do this run CMD with admin rights, change directory with `cd [/d] path\to\movirt\root`, and run `symlink.bat`. Repeat every time after updating repo.
  * Also you might want to exclude symlink from your local repo. To do this add `AndroidManifest.xml` to the end of `.git\info\exclude` file.
2. File -> Open -> ~/" *path to moVirt project* "/build.gradle. Or select "Import project" from Welcome page.  
3. On the import dialog : Select local gradle distribution. Give the path to gradle version 2.2.1 .      
   <img src = https://github.com/matobet/moVirt/blob/master/images/import_project.png align="center" height = "480px" width="640px">    
   After this, the prompt asks you to enter the path to the android sdk.  
   You can refer the following image for the complete SDK path.    
   <img src = https://github.com/matobet/moVirt/blob/master/images/sdk_location.png align="center" height = "480px" width="640px">    
4. Press OK. The build takes a while to complete. After it is done, the project is reloaded.  
5. Run the android application on an emulator or a developer's device.  
    Run -> Run -> Select moVirt-moVirt   
      *For the first time* Run -> Edit Configurations  
      Click on the + icon -> Android application -> in right panel General tab select the moVirt-moVirt module.  
      Click Apply. Click OK.  

**Resources:**

* [oVirt](http://www.ovirt.org)
* [moVirt wiki](http://www.ovirt.org/Project_moVirt)
* [moVirt on YouTube](https://www.youtube.com/channel/UCX2a6qOrCrUYGQCGLlHz2-A)
* [Tutorials](https://github.com/matobet/moVirt/tree/master/tutorials/README.md)
