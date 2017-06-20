# Google Assistant Shoebox Robot

![](https://raw.githubusercontent.com/Nilhcem/assistant-androidthings/master/photo.jpg)

Blog post: [http://nilhcem.com/android-things/create-your-google-assistant-robots](http://nilhcem.com/android-things/create-your-google-assistant-robots)


## Wiring (Raspberry Pi 3)

- Connect an LED to BCM25 (pin #22)
- Connect a button to BCM23 (pin #16)
- Connect a Servo to PWM0 (pin #12)
- Connect an LED Matrix to SPI0.0 (DIN to MOSI (pin #19), CS to SS0 (pin #24), CLK to SCLK (pin #23), VCC to 5V, GND to Ground)
- Connect a USB microphone
- Connect an audio speaker


## Getting started

For readability, the app is composed of 3 gradle modules and 1 google cloud functions directory

- `:app`: Android Things Kotlin application
- `:events-pubsub`: Subscribes to Pub/Sub via gRPC to return events in a LiveData object
- `:google-assistant`: Google Assistant
- `cloud-functions`: Google Cloud Functions directory


### Create a Google Cloud Platform Project

- Create a new project from the [Google Developers Console](https://console.developers.google.com/)
- Install the Google Cloud SDK and enable Google Cloud Functions API ([quickstart guide](https://cloud.google.com/functions/docs/quickstart))
- Locate your project ID in the dashboard and note for later use. (Read [here](https://support.google.com/cloud/answer/6158840?hl=en) for help)
- Enable the Pub/Sub API (Read [here](https://cloud.google.com/pubsub/docs/quickstart-console) for help), and create a topic named "PubSubMessages and a subscription named "PubSubMessagesSub":

```bash
gcloud init
gcloud beta pubsub topics create PubSubMessages
gcloud beta pubsub subscriptions create --topic PubSubMessages PubSubMessagesSub
```
- [Generate a JSON service account key](https://cloud.google.com/storage/docs/authentication?hl=en#service_accounts) (name=androidthings-pubsub), with the Pub/Sub Subscriber role, and save it to `events-pubsub/src/main/assets/service-account.json`


### Set your Google Project ID

- Update "your-google-project-id" on line 4 of `cloud-functions/index.js`.
- Update `GOOGLE_PROJECT_ID` value in `events-pubsub/src/main/java/com/nilhcem/assistant/androidthings/pubsub/EventLiveData.java`.


### Deploy the Google Cloud Function

-  Deploy the `webhook` function with the following command
```bash
cd cloud-functions
gcloud beta functions deploy webhook --stage-bucket staging.<PROJECT ID>.appspot.com --trigger-http
```
* This script will deploy the function to Google Cloud and give you the endpoint address. Keep the address somewhere, you'll need it (something like `https://us-central1-<PROJECT ID>.cloudfunctions.net/webhook`).


### Create an api.ai project

- Create an [api.ai](https://api.ai/) project
- Import the `ApiAi_ShoeboxRobot.zip` file
- Go to the Fulfillment tab, update the webhook address, and set basic auth (name=shoebox, pass=robot)


### Enable the Google Assistant

- Enable the Google Assistant API and create an OAuth Client ID (name=androidthings-googleassistant) (Read [here](https://developers.google.com/assistant/sdk/prototype/getting-started-other-platforms/config-dev-project-and-account) for help)
- Download the `client_secret_NNNN.json` file from the [credentials section of the Console](https://console.developers.google.com/apis/credentials)
- Use the [`google-oauthlib-tool`](https://github.com/GoogleCloudPlatform/google-auth-library-python-oauthlib) to generate credentials:
```
pip install google-auth-oauthlib[tool]
google-oauthlib-tool --client-secrets client_secret_NNNN.json \
					 --credentials ./google-assistant/src/main/res/raw/credentials.json \
					 --scope https://www.googleapis.com/auth/assistant-sdk-prototype \
					 --save
```
- Make sure to set the [Activity Controls](https://developers.google.com/assistant/sdk/prototype/getting-started-other-platforms/config-dev-project-and-account#set-activity-controls) for the Google Account using the application.

### Deploy the Android Things app

- On the first install, grant the sample required permissions for audio and internet access:
```bash
./gradlew assembleDebug
adb install -g app/build/outputs/apk/app-debug.apk
```
- On Android Studio, click on the "Run" button or on the command line, type:
```bash
adb shell am start com.nilhcem.assistant.androidthings/.ui.main.MainActivity
```
- Try the assistant demo:

  - Press the button: recording starts.
  - Ask a question in the microphone.
  - Release the button: recording stops.
  - The Google Assistant answer should playback on the speaker.
