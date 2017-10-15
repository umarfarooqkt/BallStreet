# BallStreet

Welcome to our project! Here's some quick info to get started:

- Stable code lives in the master branch
- Beta code lives in the rendezvous branch

Setup/Installation Instructions:

1. Install Grails. The easiest way to do this is through SDKMAN. Follow section 2.1 of this guide: http://guides.grails.org/creating-your-first-grails-app/guide/index.html

2. Install IntelliJ, following instructions on this site: https://www.jetbrains.com/idea/#chooseYourEdition (The community edition will work)

3. If you haven't already, install NPM. To do this follow the instructions here for installing NodeJS, wich will install NPM with it and configure it correctly: https://nodejs.org/en/

4. Clone this repository.

5. In IntelliJ, choose "Open" and inside the folder you cloned, choose the file "build.gradle"(if asked to open as file or open as project, select open as project). Accept the default options for Gradle and/or Grails. Ensure that the box choosing to download dependencies is checked and "use gradle task configuration" is checked.

6. When the project opens, a background task will start downloading dependencies. Allow this to finish.

7. Open the terminal (View > Tool Windows > Terminal) and run the command `npm install recharts —save`. 

8. Refresh Gradle: In Intellij, go to View > Tool Windows > Gradle and choose refresh. 

9. Run the project. When the run is complete, your browser will open the page (http://localhost:8080). 

Ensure you are using Google Chrome, as no other browsers are supported at this time. Also please make sure your zoom is set to 100% and Chrome is fullscreen.

NOTE: If you get an error about a problem with "webpack", the workaround is to delete the ".bin" folder inside the "node_modules" folder, then open the terminal (View > Tool Windows > Terminal) and run the command `npm install webpack`. Run the project again and it will work this time.

