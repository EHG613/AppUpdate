# AppUpdate
应用更新下载库[![](https://jitpack.io/v/codyyeachann/AppUpdate.svg)](https://jitpack.io/#codyyeachann/AppUpdate)
## How to
#### Step 1. Add the JitPack repository to your build file
```
//Add it in your root build.gradle at the end of repositories:
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
#### Step 2. Add the dependency
```
dependencies {
	        implementation 'com.github.codyyeachann:AppUpdate:v1.0.0'
	}
```
#### Step 3. Use it in your project
```
        Intent intent = new Intent(this, UpdateDialogActivity.class);
        intent.putExtra(UpdateDialogActivity.ARG_CONTENT, "update dialog content");//Mandatory
        intent.putExtra(UpdateDialogActivity.ARG_TITLE, "update dialog title");//Mandatory
        intent.putExtra(UpdateDialogActivity.ARG_URL, "app file download url");//Mandatory
        intent.putExtra(UpdateDialogActivity.ARG_AUTHORITY, BuildConfig.APPLICATION_ID + ".file.provider");//when sdk >=7.0,you must add fileprovider in AndroidManifest.xml
        intent.putExtra(UpdateDialogActivity.ARG_APPLICATION_ID, BuildConfig.APPLICATION_ID);//when sdk >=8.0,you must add this argument
        intent.putExtra(UpdateDialogActivity.ARG_FILE_NAME, mFileName);//Optional argument
        startActivity(intent);
```
