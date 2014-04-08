YoutubeServerAPI
================
Put your API-keys in the appropiate *.properties key. Use the sample files to see how the files have to be structured.

Freebase and Youtube API-keys have to be obtained from the [google developer console](https://code.google.com/apis/console). There you need to enable *YouTube Data API v3* and *Freebase API*. To do so, navigate to "APIs & auth" --> "APIs" and switch the status of these APIs to **on**.
Now you are allowed to use you API-Key. To actually get the key, click on "Credentials" and create a new key for browser applications.
Copypaste it into the *freebase.properties* file like this

    API_KEY = your_api_key
	
in the same folder the `freebase.properties.sample` can be found.
Be advised you must not push your api-key to a public repository!
