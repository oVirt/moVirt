moVirt
======
[![Build Status](https://travis-ci.org/matobet/moVirt.svg)](https://travis-ci.org/matobet/moVirt)
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/matobet/moVirt?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

A mobile client for [oVirt](http://www.ovirt.org). 

Current version: 1.6
You can install it from [Google Play Store](https://play.google.com/store/apps/details?id=org.ovirt.mobile.movirt)
Or using the [direct link](https://github.com/matobet/moVirt/blob/master/moVirt/moVirt-release.apk?raw=true)

###Supported oVirt and Android per moVirt version
| oVirt \ moVirt  | [1.1](https://github.com/matobet/moVirt/raw/430cb4a1aae86e66a3ed3a3b7cae7d072354cb76/moVirt/moVirt-release.apk)<sup>1</sup>  | [1.2](https://github.com/matobet/moVirt/raw/f8585585a02396de7dd555860a102e2ba4dff4f8/moVirt/moVirt-release.apk)<sup>1</sup> | [1.3](https://github.com/matobet/moVirt/raw/976cceaad5d298b9e734a9b79cf4966a088eacc3/moVirt/moVirt-release.apk)<sup>2</sup>, [1.4](https://github.com/matobet/moVirt/raw/4896f4d8cecd6b733d921324a347505bc6437f72/moVirt/moVirt-release.apk)<sup>2</sup> | [1.5](https://github.com/matobet/moVirt/raw/9ce19a6a16e76da79473887a0074066b9e3ea494/moVirt/moVirt-release.apk)<sup>2</sup>, [1.6](https://github.com/matobet/moVirt/raw/524af62b55f7490232c0476a74ea56582c1f1b5b/moVirt/moVirt-release.apk)<sup>2</sup> |
| --------------- | ---- | --- | ----| --- |
| **3.4**         | yes  | yes | no<sup>3</sup>  | no<sup>3</sup>  |
| **3.5**         |  no  | yes | yes | no<sup>3</sup>  |
| **3.6**         |  no  | no  | yes | yes |
| **4.0**         |  no  | no  | no | yes |

**[1]**: works on Android 4.0+

**[2]**: works on Android 4.1+

**[3]**: may work but no longer tested

###Screenshots

<img src = /images/screenshots/Screenshot_2015-07-15-23-55-52.png><img src = /images/screenshots/Screenshot_2015-07-15-23-56-12.png><img src = /images/screenshots/Screenshot_2015-07-15-23-57-21.png><img src = /images/screenshots/7inchDashoard.png>

###Changelog:
####v1.6:
* Added full support of v4 API and SSO OAuth2
* Fixes Authenticator and minor bugs
* Removed basic authentication

####v1.5:
* Added support of the 4.0 engine

####v1.4:
* Added new dashboard functionality (virtual consumption, clickable cpu/memory consumption)
* Dashboard's statuses now reflect state of vms, hosts, etc. instead of events.
* Better UI (dashboard, adding/editing triggers)
* Better sorting in lists
* Memory units are now displayed correctly

####v1.3:
* Added support for snapshots
* Enhanced UI (added search button, alignments, colors)
* Disks and Nics are downloaded in the main download loop making them browsable in offline mode
* Minimal Android version moved to 4.1

####v1.2:
* Added basic support for storage
* Fixes in augmented reality (scan code) part

####v1.1:
* Added max utilized resources and overview of issues to the dashboard
* Now can survive even completely broken response from the REST API
* Better error handling
* Better UX for the connection settings

####v0.2:
* Added augmented reality.
* Added icons for events.
* Refresh button on every screen.
* Added connection information icon and notification.
* Added option to set periodic sync with custom time interval.
* Added actions in Host activity (activate, deactivate).
* Added SPICE over SSL support.
* Enriched VM activity with cluster and host information.

###Known issues:
####All versions:
* Non-admin users support currently disabled.
* SPICE may not work well with SSL on Android 5.+
* Certificates may not work on Android 4.1 (use "Ignore certificate checking" in advanced connection settings).

####v1.3:
* Dashboard and Data Matrix scanner may show incorrect number of vms or wrong vms (i.e. snapshots' vms).

####v0.2, v1.0, v1.1, v1.2:
* Event triggers may spam you with notifications about old events.

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
    <img src = /images/data_matrix_guide/Screenshot_1.jpg align="center">
  * Make data matrix codes with any online generator containing these IDs as a text (one code for each ID).  
    <img src = /images/data_matrix_guide/Screenshot_2.jpg align="center">
2. Print and stick codes to physical servers.
3. Open Scan Codes option in moVirt and you are ready.  
    <img src = /images/data_matrix_guide/movirt_AR.png align="center">

###Get in Touch
* Interested about news? Follow us on [Twitter](https://twitter.com/mobileOvirt)
* Have user questions? Subscribe to [oVirt's users list](http://lists.ovirt.org/mailman/listinfo/users) and post a question with subject containing *[moVirt]*
* Want to contribute? Have development oriented questions? Subscribe to [oVirt's devel list](http://lists.ovirt.org/mailman/listinfo/devel) and post a question with subject containing *[moVirt]*
* Want to chat? We are usually hanging in *irc.oftc.net* in *#ovirt* - look for *mbetak*, *tjelinek*, *NoiseDoll* or *yixinzhang*. Or anyone else, people are friendly there ;)

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
3. On the import dialog : Select local gradle distribution. Give the path to gradle version 2.2.1.      
   <img src = /images/import_project.png align="center" height = "480px" width="640px">    
   After this, the prompt asks you to enter the path to the android sdk.  
   You can refer the following image for the complete SDK path.    
   <img src = /images/sdk_location.png align="center" height = "480px" width="640px">    
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
