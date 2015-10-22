#####These instructions will help you to set up development environment, get the source code of the moVirt and build it on Android Studio.

### 1. Software dependencies.
[Git]( https://git-scm.com/): Downlaod and install the version appropriate for your operating system from [here](https://git-scm.com/downloads). Add the full path to the 'bin/' directory from your git installation into the PATH variable of your environment so that it can be used from any location.

Android Studio:
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


### 2. Fork and download the moVirt repository.
* After logging into your Github account, go to https://github.com/matobet/moVirt, and click the 'Fork' button near the top right corner.
* Open a terminal and clone your forked repository: ```git clone https://github.com/YOUR_GITHUB_USERNAME/moVirt.git```.
* Move to the project folder with ```cd moVirt```.
* Checkout the remote branch 'master' in your own local branch 'master': ```git checkout master remotes/origin/master```.
* Pull any changes from your remote branch 'master': ```git pull origin master```
* Make official moVirt repo as upstream: ```git remote add upstream https://github.com/matobet/moVirt.git```
* Make sure to get the latest changes from official android/master branch: ```git pull upstream master```

### 3. Import project in Android Studio and get it running
* This step only for Windows users.
  * Before importing you need to change symlink. To do this run terminal with admin rights, change directory with `cd [/d] path\to\movirt\root`, and run `symlink.bat`. Repeat every time after updating repo.
  * Also you might want to exclude symlink from your local repo. To do this add `AndroidManifest.xml` to the end of `.git\info\exclude` file.
* File -> Open -> ~/" *path to moVirt project* "/build.gradle. Or select "Import project" from Welcome page.
* On the import dialog : Select local gradle distribution. Give the path to gradle version 2.2.1.
After this, the prompt asks you to enter the path to the android sdk.
Browse to the location of the SDK you downloaded.
* The build will take a while to complete after which the project is reloaded. Now the code is all setup.
* Run the android application on an emulator([guide](http://developer.android.com/tools/devices/managing-avds.html)) or a developer's device([guide](http://developer.android.com/tools/device.html)).  
    Run -> Run -> Select the module as moVirt-moVirt  
      *For the first time* Run -> Edit Configurations  
      Click on the + icon -> Android application -> in right panel General tab select the moVirt-moVirt module.  
      Click Apply. Click OK.  

The generated APK file is saved in android/build/outputs/apk as android-debug.apk which can be tranferred externally, put on an android device and installed.
##### Now make changes in the local version of code and see them showing up in the real app you are running.
### Happy coding!

