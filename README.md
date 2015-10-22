moVirt
======
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/matobet/moVirt?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

A mobile client for [oVirt](http://www.ovirt.org). 

Current version: 1.2
You can install it from [Google Play Store](https://play.google.com/store/apps/details?id=org.ovirt.mobile.movirt)
Or using the [direct link](https://github.com/matobet/moVirt/blob/master/moVirt/moVirt-release.apk?raw=true)

<img src = /images/screenshots/Screenshot_2015-07-15-23-55-52.png><img src = /images/screenshots/Screenshot_2015-07-15-23-56-12.png><img src = /images/screenshots/Screenshot_2015-07-15-23-57-21.png><img src = /images/screenshots/7inchDashoard.png>

###Changelog:
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
####v0.2, v1.0, v1.1:
* Non-admin users support currently disabled.
* SPICE may not work well with SSL on Android 5.+
* Certificates may not work on Android 4.1 (use "Ignore certificate checking" in advanced connection settings).
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
To start contributing to moVirt, you will need to **install** Git and Android Studio and import the moVirt code.  
Sounds scary? Don't worry, [SETUP.md](https://github.com/matobet/moVirt/blob/master/SETUP.md) is the complete walk through for the entire process step by step.

**Resources:**

* [oVirt](http://www.ovirt.org)
* [moVirt wiki](http://www.ovirt.org/Project_moVirt)
* [moVirt on YouTube](https://www.youtube.com/channel/UCX2a6qOrCrUYGQCGLlHz2-A)
